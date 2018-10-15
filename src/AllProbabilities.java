import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AllProbabilities {

    public static String startEventId;

    public static ArrayList<TransitionProbability> getAllProbabilities() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Bizagi\\Claim\\Claim 05-18\\86a3a48a-379f-4403-8235-e2f3c2001fb7\\Diagram.xml"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> decisions = new ArrayList<>();

        NodeList activityList = document.getElementsByTagName("Activity");
        NodeList transitionList = document.getElementsByTagName("Transition");

        for (int x = 0, size = activityList.getLength(); x < size; x++) {

            NodeList childList = activityList.item(x).getChildNodes();
            for (int j = 0; j < childList.getLength(); j++) {
                Node childNode = childList.item(j);
                String childNodeName = childNode.getNodeName();
                if ("Route".equals(childNodeName)) {

                    //Route elementai, kurie yra Activity vaikai, yra sprendimo mazgai. Pridedam i sprendimu mazgu masyva Route elemento id.
                    decisions.add(activityList.item(x).getAttributes().getNamedItem("Id").getNodeValue());
                }
            }

        }

        // searching for the first event (initiation)
        loop:
        for (int x = 0, size = activityList.getLength(); x < size; x++) {

            if (activityList.item(x).getAttributes().getNamedItem("Name").getNodeValue().equals("")) {

                startEventId = activityList.item(x).getAttributes().getNamedItem("Id").getNodeValue();
                System.out.println(startEventId);
                break loop;
            }

        }
        // found the first event (initiation)


        DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();
        DocumentBuilder db1 = null;

        try {
            db1 = dbf1.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document1 = null;
        try {
            document1 = db1.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList traceList = document1.getElementsByTagName("trace");
        System.out.println(traceList.getLength());

        HashMap<String, Float> transitionProbabilities = new HashMap<>();
        for (int a = 0; a < decisions.size(); a++) {

            String currentDecision = decisions.get(a);

            //hashmap for each activity and transitions leading to the decision
            HashMap<String, ArrayList<String>> activitiesLeadingToDecision = new HashMap<>();
            HashMap<String, ArrayList<String>> activitiesComingFromDecision = new HashMap<>();

            //retrieve the activities that come before the decision and the transitions that implement the lead-up to the decision
            for (int b = 0, size = transitionList.getLength(); b < size; b++) {


                String transitionId = transitionList.item(b).getAttributes().getNamedItem("Id").getNodeValue();
                String currentTransitionToValue = transitionList.item(b).getAttributes().getNamedItem("To").getNodeValue();
                String currentTransitionFromValue = transitionList.item(b).getAttributes().getNamedItem("From").getNodeValue();

                //does transition lead to decision?
                boolean transitionLeadsToDecision = currentTransitionToValue.contains(currentDecision);

                //if so, find the name of the activity that leads to transition
                if (transitionLeadsToDecision) {
                    for (int c = 0; c < activityList.getLength(); c++) {

                        String activityId = activityList.item(c).getAttributes().getNamedItem("Id").getNodeValue();
                        if (activityId.contains(currentTransitionFromValue)) {
                            String activityName = activityList.item(c).getAttributes().getNamedItem("Name").getNodeValue();

                            //add the found activity name to the list of activities leading to the decision
                            if (activitiesLeadingToDecision.containsKey(activityName)) {
                                activitiesLeadingToDecision.get(activityName).add(transitionId);
                            } else {
                                ArrayList<String> transitionIds = new ArrayList<>();
                                transitionIds.add(transitionId);
                                activitiesLeadingToDecision.put(activityName, transitionIds);
                            }
                        }
                    }
                }
            }

            //retrieve the activities that come after the decision and the transitions that implement the follow-up to the decision
            for (int d = 0; d < transitionList.getLength(); d++) {


                String transitionId = transitionList.item(d).getAttributes().getNamedItem("Id").getNodeValue();
                String currentTransitionToValue = transitionList.item(d).getAttributes().getNamedItem("To").getNodeValue();
                String currentTransitionFromValue = transitionList.item(d).getAttributes().getNamedItem("From").getNodeValue();

                //does transition lead to decision?
                boolean decisionLeadsToTransition = currentTransitionFromValue.contains(currentDecision);

                //if so, find the name of the activity that comes from the decision
                if (decisionLeadsToTransition) {

                    for (int e = 0; e < activityList.getLength(); e++) {

                        String activityId = activityList.item(e).getAttributes().getNamedItem("Id").getNodeValue();
                        if (activityId.contains(currentTransitionToValue)) {
                            String activityName = activityList.item(e).getAttributes().getNamedItem("Name").getNodeValue();
                            //add the found activity name to the list of activities coming from the decision
                            if (activitiesComingFromDecision.containsKey(activityName)) {
                                activitiesComingFromDecision.get(activityName).add(transitionId);
                            } else {
                                ArrayList<String> transitionIds = new ArrayList<>();
                                transitionIds.add(transitionId);
                                activitiesComingFromDecision.put(activityName, transitionIds);
                            }
                        }
                    }
                }
            }

            //calculate the transition probabilities
            HashMap<String, Integer> transitionCounts = new HashMap<>();
            for (int f = 0; f < traceList.getLength(); f++) {
                ArrayList<String> eventName = new ArrayList<>();
                Element trace = (Element) traceList.item(f);
                NodeList stringList = trace.getElementsByTagName("string");

                //collect all event names in the trace
                for (int g = 1; g < stringList.getLength(); g++) {
                    if (stringList.item(g).getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {
                        eventName.add(stringList.item(g).getAttributes().getNamedItem("value").getNodeValue());
                    }
                }


                for (int h = 0; h < eventName.size() - 1; h++) {
                    String firstEvent = eventName.get(h);
                    for (int i = h + 1; i < eventName.size(); i++) {
                        String secondEvent = eventName.get(i);

                        //if the first event leads to second event in the model
                        if (activitiesLeadingToDecision.containsKey(firstEvent) && activitiesComingFromDecision.containsKey(secondEvent)) {

                            //increment the counter for the transitions that lead to the second event in this decision
                            ArrayList<String> transitionList1 = activitiesComingFromDecision.get(secondEvent);
                            for (int j = 0; j < transitionList1.size(); j++) {
                                String transition = transitionList1.get(j);
                                if (transitionCounts.containsKey(transition)) {
                                    transitionCounts.put(transition, transitionCounts.get(transition) + 1);
                                } else {
                                    transitionCounts.put(transition, 1);
                                }
                            }
                        }
                    }
                }
            }

            //count the total of all transitions noticed
            int totalTransitionCount = 0;
            Set<String> transitionNames = transitionCounts.keySet();
            for (String s : transitionNames) {
                totalTransitionCount += transitionCounts.get(s);
            }

            //finally, calculate the probabilities for the transitions coming out from this decision and going to activities

            for (String s : transitionNames) {
                float transitionProbability = (float) transitionCounts.get(s) / totalTransitionCount;
                transitionProbabilities.put(s, transitionProbability);


            }
        }

        ArrayList<TransitionProbability> probs = new ArrayList<>();
        Set<String> transitionNames = transitionProbabilities.keySet();
        for (String s : transitionNames) {
            TransitionProbability prob = new TransitionProbability();
            prob.transitionId = s;
            String pr = String.format("%.2f", transitionProbabilities.get(s));
            String p = pr.replaceAll(",", ".");
            prob.probability = p;
            probs.add(prob);
        }
        System.out.println(transitionProbabilities);
        HashSet<String> hs = new HashSet<>(decisions);

        System.out.println(hs);

        return probs; //grazina transitionId ir tikimybe
    }
}