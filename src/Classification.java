import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class Classification {


    public static void main(String[] args) throws Exception {

        Resources res = new Resources();
        res.getResources();

        ResourcesCalculation resCalc = new ResourcesCalculation();
        resCalc.getResourceCalculations();

        AllProbabilities all = new AllProbabilities();
        all.getAllProbabilities();

        ActivityAttributes act = new ActivityAttributes();
        act.generateActivityAttributes();

        Duration dur = new Duration();
        dur.getDurations();

        ArrivingInterval arr = new ArrivingInterval();
        arr.getArrivingInterval();

        XmlGenerator xml = new XmlGenerator();
        xml.generateXml();

        CreateParticipantFile participant = new CreateParticipantFile();
        participant.generateParticipantXml();

        CreateDiagramFile diagram = new CreateDiagramFile();
        diagram.generateDiagramXml();


    }

}

class Resources {

    public static HashSet<String> getResources() {

        ArrayList<String> resources = new ArrayList<>();


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList nodeList = document.getElementsByTagName("string");


        for (int x = 0, size = nodeList.getLength(); x < size; x++) {

            if (nodeList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("org:resource")) {
                resources.add(nodeList.item(x).getAttributes().getNamedItem("value").getNodeValue());
            }
        }
        HashSet<String> hs = new HashSet<>(resources);

        System.out.println(hs);

        return hs; //grazina visus unikalius egzistuojancius resursus
    }


}

class ResourcesCalculation {

    public static ArrayList<ResourcesQuantity> getResourceCalculations() {

        HashSet<String> resursai = Resources.getResources();

        HashMap<String, Long> similarRes = new HashMap<>();
        Iterator<String> resIter = resursai.iterator();
        Iterator<String> resIter2 = resursai.iterator();

        int fullDistance = 0;
        int counter = 1;
        while (resIter.hasNext()) {
            String dabartinisRes = resIter.next();
            counter++;
            while (resIter2.hasNext()) {
                String kitasRes = resIter2.next();
                fullDistance += LevenshteinDistance.getDefaultInstance().apply(dabartinisRes, kitasRes);
            }
        }
        int averageDistance = fullDistance / counter;

        resIter = resursai.iterator();
        while (resIter.hasNext()) {

            String dabartinisRes = resIter.next();
            Iterator<String> keys = similarRes.keySet().iterator();
            boolean pridetas = false;
            while (keys.hasNext()) {
                String key = keys.next();
                if (LevenshteinDistance.getDefaultInstance().apply(key, dabartinisRes) < 2) {
                    similarRes.put(key, similarRes.get(key) + 1); // resursai isgauti pagal levenšteino atstuma
                    pridetas = true;
                }
            }
            if (pridetas == false) {
                similarRes.put(dabartinisRes, 1l);
            }
        }
        ArrayList<ResourcesQuantity> resQ = new ArrayList<>();
        Set<String> resourceNames = similarRes.keySet();
        for (String s : resourceNames) {
            ResourcesQuantity res = new ResourcesQuantity();
            res.resourceName = s;

            res.resourceQuantity = Long.toString(similarRes.get(s));
            resQ.add(res);
        }
        return resQ; // grazina bendrini resurso pavadinima ir jo kieki
    }
}

class ResourcesQuantity {
    String resourceName;
    String resourceQuantity;
}


class Duration {

    public static ArrayList<String> eventName = new ArrayList<>();
    public static ArrayList<String> resourceName = new ArrayList<>();
    public static ArrayList<String> time = new ArrayList<>();
    public static ArrayList<String> status = new ArrayList<>();
    public static ArrayList<Integer> minutes = new ArrayList<>();
    public static ArrayList<Integer> seconds = new ArrayList<>(); // NAUJAS

    public static ArrayList<ActivityProperties> getDurations() throws ParseException {


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, Double> veikliuTrukmiuSumos = new HashMap<>(); //veiklu trukmiu sumos
        HashMap<String, Integer> veikluVyksmuKiekiai = new HashMap<>(); //veiklu kiekiai
        ArrayList<String> uniqueEvents = new ArrayList<>();

        NodeList nodeList = document.getElementsByTagName("trace");


        //Cicle start
        for (int k = 0; k < nodeList.getLength(); k++) {
            eventName.clear();
            time.clear();
            minutes.clear();
            status.clear();
            Element element = (Element) nodeList.item(k);
            NodeList stringList = element.getElementsByTagName("string");

            NodeList dateList = element.getElementsByTagName("date");


            //--- Get all events names ---//

            for (int x = 1, size = stringList.getLength(); x < size; x++) {
                if (stringList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("org:resource")) {
                    resourceName.add(stringList.item(x).getAttributes().getNamedItem("value").getNodeValue());
                }
                if (stringList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {
                    eventName.add(stringList.item(x).getAttributes().getNamedItem("value").getNodeValue());
                }
            }


            //--- Gaunamos unikalios veiklos ---//

            for (int i = 0; i < eventName.size(); i++) {


                String ivykisI = eventName.get(i); //gauname i-taji ivykio pavadinima
                if (!uniqueEvents.contains(ivykisI)) {
                    uniqueEvents.add(ivykisI);
                }
            }

            //--- Get all times of every event in trace ---//

            for (int y = 0, size = dateList.getLength(); y < size; y++) {
                if (dateList.item(y).getAttributes().getNamedItem("key").getNodeValue().contains("time:timestamp")) {
                    time.add(dateList.item(y).getAttributes().getNamedItem("value").getNodeValue());
                }
            }

            //--- Get all times in minutes ---//

            SimpleDateFormat str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SS:SS");
            Date data;

            for (int i = 0; i < time.size(); i++) {

                data = str.parse(time.get(i));
                minutes.add((int)Math.ceil(data.getHours() * 60.0 + data.getMinutes()+ data.getSeconds()/60.0));
                seconds.add(data.getSeconds());
            }

            //--- Get all lifecycle transitions of events ---//

            for (int x = 0, size = stringList.getLength(); x < size; x++) {
                if (stringList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("lifecycle:transition")) {
                    status.add(stringList.item(x).getAttributes().getNamedItem("value").getNodeValue());
                }
            }

//--- Calculate events durations those which has dublicated events with first status START and second with status complete ---//


            for (int i = 0; i < eventName.size(); i++) {


                String ivykisI = eventName.get(i); //gauname k-taji ivykio pavadinima
                if (!veikliuTrukmiuSumos.containsKey(ivykisI)) {
                    veikliuTrukmiuSumos.put(ivykisI, 0.); // NAUJAS
                }
                if (!veikluVyksmuKiekiai.containsKey(ivykisI)) {
                    veikluVyksmuKiekiai.put(ivykisI, 1);
                } else {
                    veikluVyksmuKiekiai.put(ivykisI, veikluVyksmuKiekiai.get(ivykisI) + 1);
                }

                int duration = 0;
                int durationInSec = 0;
                for (int j = i + 1; j < eventName.size(); j++) {
                    if (ivykisI.equals(eventName.get(j)) && status.get(i).equals("start") && status.get(j).equals("complete")) {


                        duration = minutes.get(j) - minutes.get(i);
                        durationInSec = seconds.get(j) - seconds.get(i);
                        if (duration >= 1) { //NAUJAS
                            veikliuTrukmiuSumos.put(ivykisI, veikliuTrukmiuSumos.get(ivykisI) + duration);
                        }
                        else{
                            veikliuTrukmiuSumos.put(ivykisI, (double) (veikliuTrukmiuSumos.get(ivykisI) + durationInSec/60.0));
                        } // NAUJAS

                        break;
                    }
                }

            }
        }


        System.out.println("Visi ivykiai: " + eventName);

        System.out.println("Unikalus ivikiai: " + uniqueEvents);

        HashMap<String, Integer> veikluVidutinesTrukmes = new HashMap<>();

        Iterator<String> ivykiuVardai = veikluVyksmuKiekiai.keySet().iterator();
        while (ivykiuVardai.hasNext()) {
            String ivykioVardas = ivykiuVardai.next();
            veikluVidutinesTrukmes.put(ivykioVardas, (int) Math.ceil(veikliuTrukmiuSumos.get(ivykioVardas) / veikluVyksmuKiekiai.get(ivykioVardas) * 2));
        }


        ArrayList<ActivityProperties> props = new ArrayList<>();
        Set<String> eventNames = veikluVidutinesTrukmes.keySet();
        for (String s : eventNames) {
            ActivityProperties prop = new ActivityProperties();
            prop.name = s;
            prop.duration = Integer.toString(veikluVidutinesTrukmes.get(s));
            prop.id = "";
            props.add(prop);
        }


        System.out.println("Vidutines veiklų trukmes: \n" + props);
        return props; // gaunami veiklos pavadinimai bei vidutines veiklus trukmes
    }


}

class TransitionProbability {
    String transitionId;
    String probability;
}

class ActivityAttributes {
    public static ArrayList<ActivityProperties> generateActivityAttributes() throws ParseException {

        ArrayList<ResourcesQuantity> resQuantity = ResourcesCalculation.getResourceCalculations();

        ArrayList<ActivityProperties> properties = Duration.getDurations();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Bizagi\\Claim\\Claim 05-18\\86a3a48a-379f-4403-8235-e2f3c2001fb7\\Diagram.xml"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();
        DocumentBuilder db1 = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document1 = null;
        try {
            document1 = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        NodeList activityList = document.getElementsByTagName("Activity");

        NodeList eventList = document1.getElementsByTagName("event");

        for (int i = 0; i < properties.size(); i++) {

            for (int j = 0, size = activityList.getLength(); j < size; j++) {

                if (activityList.item(j).getAttributes().getNamedItem("Name").getNodeValue().equals(properties.get(i).name)) {

                    String currentId = activityList.item(j).getAttributes().getNamedItem("Id").getNodeValue();
                    properties.get(i).id = currentId;
                    properties.get(i).resource = "";

                }

            }
        }
        String childNodeName;
        String resourceNameOfNode = "";
        String conceptNameOfNode = "";
        loop:
        for (int i = 0; i < properties.size(); i++) {
            for (int x = 0, size = eventList.getLength(); x < size; x++) {
                NodeList childList = eventList.item(x).getChildNodes(); // VIENO IVYKIO VISI VAIKAI
                for (int j = 0; j < childList.getLength(); j++) { // ITERUOJAME PER VAIKU SARASA
                    Node childNode = childList.item(j); // VIENAS VAIKAS
                    childNodeName = childNode.getNodeName();
                    if ("string".equals(childNodeName)) {
                        if (childNode.getAttributes().getNamedItem("key").getNodeValue().contains("org:resource")) {
                            resourceNameOfNode = childNode.getAttributes().getNamedItem("value").getNodeValue();

                        }
                        if (childNode.getAttributes().getNamedItem("key").getNodeValue().contains("concept:name") && childList.item(j).getAttributes().getNamedItem("value").getNodeValue().equals(properties.get(i).name)) {
                            conceptNameOfNode = childList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                            properties.get(i).resource = resourceNameOfNode;
                            System.out.println(resourceNameOfNode);
                            continue loop;
                        }

                    }
                }
            }
        }

        System.out.println(properties);

        for (int i = 0; i < properties.size(); i++) {
            String dabartinisRes = properties.get(i).resource;
            for (int j = 0; j < resQuantity.size(); j++) {
                String kitasRes = resQuantity.get(j).resourceName;
                if (LevenshteinDistance.getDefaultInstance().apply(kitasRes, dabartinisRes) < 3) {
                    properties.get(i).resource = kitasRes;
                }
            }
        }


        return properties; // GAUNAMAS IVYKIO PAVADINIMAS, ID, TRUKME, RESURSAS
    }

}

class ActivityProperties {
    String name;
    String duration;
    String id;
    String resource;
}

class AllProbabilities {

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
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Bizagi\\Claim\\Claim 05-18\\86a3a48a-379f-4403-8235-e2f3c2001fb7\\Diagram.xml"));
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
            document1 = db1.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList traceList = document1.getElementsByTagName("trace");

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

class ArrivingInterval {

    public static int getArrivingInterval() throws ParseException {

        int arriving = 0;
        int difference;
        int differenceCount = 0;
        int differenceSum = 0;
        int allArrivals = 0;
        int finalArrival = 0;
        String childNodeName;
        boolean dateFound = false;
        String date = "";
        int arrivingInt;
        ArrayList<Integer> minutes = new ArrayList<>();
        ArrayList<Integer> dates = new ArrayList<>();
        ArrayList<Integer> uniqueDates = new ArrayList<>();


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NodeList eventList = document.getElementsByTagName("event");

        for (int i = 0; i < eventList.getLength(); i++) {
            dateFound = false;
            NodeList childList = eventList.item(i).getChildNodes();
            for (int j = 0; j < childList.getLength(); j++) {

                Node childNode = childList.item(j);
                childNodeName = childNode.getNodeName();

                if ("date".equals(childNodeName)) {
                    date = childNode.getAttributes().getNamedItem("value").getNodeValue();
                    dateFound = true;
                }

                if (dateFound) {

                    if ("string".equals(childNodeName)) {

                        if (childNode.getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {

                            if (childList.item(j).getAttributes().getNamedItem("value").getNodeValue().equals("incoming claim")) { // added first event from bussiness process model "Incoming claim". When it will be used with
                                SimpleDateFormat str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SS:SS"); // another bussiness process, need to change the name
                                Date data;
                                data = str.parse(date);

                                dates.add(data.getDate());

                            }
                        }
                    }
                }
            }
        }

// getting unique days (1,2,3,4...)
        for (int i = 0; i < dates.size(); i++) {
            int cuttentDate = dates.get(i);
            if (!uniqueDates.contains(cuttentDate)) {
                uniqueDates.add(cuttentDate);
            }
        }
        Collections.sort(uniqueDates); // sorting days from lowest to highest

        for (int a = 0; a < uniqueDates.size(); a++) {
            minutes.clear();
            difference = 0;
            differenceSum = 0;
            differenceCount = 0;
            for (int i = 0; i < eventList.getLength(); i++) {

                dateFound = false;
                NodeList childList = eventList.item(i).getChildNodes();

                for (int j = 0; j < childList.getLength(); j++) {

                    Node childNode = childList.item(j);
                    childNodeName = childNode.getNodeName();

                    if ("date".equals(childNodeName)) {
                        date = childNode.getAttributes().getNamedItem("value").getNodeValue();
                        dateFound = true;
                    }

                    if (dateFound) {

                        if ("string".equals(childNodeName)) {

                            if (childNode.getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {

                                if (childList.item(j).getAttributes().getNamedItem("value").getNodeValue().equals("incoming claim")) { // need to change the name of the first event
                                    SimpleDateFormat str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SS:SS");
                                    Date data;
                                    data = str.parse(date);


                                    if (data.getDate() == uniqueDates.get(a)) {
                                        arrivingInt = data.getHours() * 60 + data.getMinutes();
                                        minutes.add(arrivingInt);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Collections.sort(minutes); // sorting time from smallest to largest
            for (int i = 1; i < minutes.size(); i++) {

                difference = minutes.get(i) - minutes.get(i - 1); // getting difference between beggining time
                differenceSum += difference; // summing all differences
                differenceCount++; // counting difference quantity

            }
            arriving = differenceSum / differenceCount;
            allArrivals += arriving;
        }
     finalArrival = allArrivals / uniqueDates.size(); // getting average arrival size

        return finalArrival; // return average arrival size

    }
}

class XmlGenerator {


    public static void generateXml() throws ParseException {

        ArrayList<TransitionProbability> probs = AllProbabilities.getAllProbabilities();
        ArrayList<ResourcesQuantity> resQ = ResourcesCalculation.getResourceCalculations();
        ArrayList<ActivityProperties> properties = ActivityAttributes.generateActivityAttributes();

        String StartEventId = AllProbabilities.startEventId;

        try {

            // INICIUOJAMAS FAILO KURIMAS

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // STANDARTINIAI XML BPSimData FAILO ELEMENTAI

            // BPSimData elements
            Document doc = docBuilder.newDocument();
            Element BPSimData = doc.createElement("ns1:BPSimData");
            doc.appendChild(BPSimData);

            //set attribute to BPSimData element

            Attr BPSimData1 = doc.createAttribute("simulationLevel");
            BPSimData1.setValue("LevelFour");
            BPSimData.setAttributeNode(BPSimData1);

            Attr BPSimData2 = doc.createAttribute("xmlns:ns1");
            BPSimData2.setValue("http://www.bpsim.org/schemas/1.0");
            BPSimData.setAttributeNode(BPSimData2);

            // Scenario elements
            Element Scenario = doc.createElement("ns1:Scenario");
            BPSimData.appendChild(Scenario);

            // set attribute to Scenario element
            Attr Scenario1 = doc.createAttribute("id");
            Scenario1.setValue("Scenario_f3a589ef-68a5-4983-b017-407f8e264e3b");
            Scenario.setAttributeNode(Scenario1);

            Attr Scenario2 = doc.createAttribute("name");
            Scenario2.setValue("Scenario 1");
            Scenario.setAttributeNode(Scenario2);

            Attr Scenario3 = doc.createAttribute("author");
            Scenario3.setValue("Justelio");
            Scenario.setAttributeNode(Scenario3);

            Attr Scenario4 = doc.createAttribute("version");
            Scenario4.setValue("1.0");
            Scenario.setAttributeNode(Scenario4);

            // ScenarioParameters elements
            Element ScenarioParameters = doc.createElement("ns1:ScenarioParameters");
            Scenario.appendChild(ScenarioParameters);


            // PropertyParameters1 elements
            Element PropertyParameters1 = doc.createElement("ns1:PropertyParameters");
            ScenarioParameters.appendChild(PropertyParameters1);

            // PRASIDEDA LOOP PASIRINKIMO MAZGU TIKIMYBEMS

            for (int i = 0; i < probs.size(); i++) {

                Element ElementParameters2 = doc.createElement("ns1:ElementParameters");
                Scenario.appendChild(ElementParameters2);

                // set attribute to ElementParameters2 elements
                Attr ElementParameters21 = doc.createAttribute("elementRef");
                ElementParameters21.setValue("Id_" + probs.get(i).transitionId);
                ElementParameters2.setAttributeNode(ElementParameters21);

                // ControlParameters1 elements
                Element ControlParameters1 = doc.createElement("ns1:ControlParameters");
                ElementParameters2.appendChild(ControlParameters1);

                // Probability1 elements
                Element Probability1 = doc.createElement("ns1:Probability");
                ControlParameters1.appendChild(Probability1);

                // Floating1 elements
                Element FloatingParameter1 = doc.createElement("ns1:FloatingParameter");
                Probability1.appendChild(FloatingParameter1);

                // set attribute to Floating1 element
                Attr FloatingParameter11 = doc.createAttribute("value");
                FloatingParameter11.setValue(probs.get(i).probability);
                FloatingParameter1.setAttributeNode(FloatingParameter11);

                // PropertyParameters3 elements
                Element PropertyParameters3 = doc.createElement("ns1:PropertyParameters");
                ElementParameters2.appendChild(PropertyParameters3);

            }

            // PRASIDEDA LOOP RESURSU APRASYMUI

            for (int i = 0; i < resQ.size(); i++) {

                // ElementParameters4 elements
                Element ElementParameters4 = doc.createElement("ns1:ElementParameters");
                Scenario.appendChild(ElementParameters4);

                // set attribute to ElementParameters4 elements
                Attr ElementParameters41 = doc.createAttribute("elementRef");
                ElementParameters41.setValue(resQ.get(i).resourceName);
                ElementParameters4.setAttributeNode(ElementParameters41);

                // ResourceParameters1 elements
                Element ResourceParameters1 = doc.createElement("ns1:ResourceParameters");
                ElementParameters4.appendChild(ResourceParameters1);

                // Quantity1 elements
                Element Quantity1 = doc.createElement("ns1:Quantity");
                ResourceParameters1.appendChild(Quantity1);

                // NumericParameter1 elements
                Element NumericParameter1 = doc.createElement("ns1:NumericParameter");
                Quantity1.appendChild(NumericParameter1);

                // set attribute to NumericParameter1 elements
                Attr NumericParameter11 = doc.createAttribute("value");
                NumericParameter11.setValue(resQ.get(i).resourceQuantity);
                NumericParameter1.setAttributeNode(NumericParameter11);

                // PropertyParameters5 elements
                Element PropertyParameters5 = doc.createElement("ns1:PropertyParameters");
                ElementParameters4.appendChild(PropertyParameters5);
            }

            // PRASIDEDA LOOP, KURIS SUGENERUOJA VEIKLU ID, TRUKME BEI PRIDEDA RESURSA

            for (int j = 0; j < properties.size(); j++) {
                if (properties.get(j).duration.equals("0")) {

                    // ElementParameters110 elements
                    Element ElementParameters110 = doc.createElement("ns1:ElementParameters");
                    Scenario.appendChild(ElementParameters110);

                    // set attribute to ElementParameters110 elements
                    Attr ElementParameters111 = doc.createAttribute("elementRef");
                    ElementParameters111.setValue("Id_" + properties.get(j).id);
                    ElementParameters110.setAttributeNode(ElementParameters111);

                    // PropertyParameters12 elements
                    Element PropertyParameters12 = doc.createElement("ns1:PropertyParameters");
                    ElementParameters110.appendChild(PropertyParameters12);

                }

                if (!properties.get(j).duration.equals("0")) {

                    // ElementParameters6 elements
                    Element ElementParameters6 = doc.createElement("ns1:ElementParameters");
                    Scenario.appendChild(ElementParameters6);

                    // set attribute to ElementParameters6 elements
                    Attr ElementParameters61 = doc.createAttribute("elementRef");
                    ElementParameters61.setValue("Id_" + properties.get(j).id);
                    ElementParameters6.setAttributeNode(ElementParameters61);

                    // TimeParameters1 elements
                    Element TimeParameters1 = doc.createElement("ns1:TimeParameters");
                    ElementParameters6.appendChild(TimeParameters1);

                    // ProcessingTime1 elements
                    Element ProcessingTime1 = doc.createElement("ns1:ProcessingTime");
                    TimeParameters1.appendChild(ProcessingTime1);

                    // ResultRequest1 elements
                    Element ResultRequest1 = doc.createElement("ns1:ResultRequest");
                    ResultRequest1.appendChild(doc.createTextNode("min"));
                    ProcessingTime1.appendChild(ResultRequest1);

                    // ResultRequest2 elements
                    Element ResultRequest2 = doc.createElement("ns1:ResultRequest");
                    ResultRequest2.appendChild(doc.createTextNode("max"));
                    ProcessingTime1.appendChild(ResultRequest2);

                    // ResultRequest3 elements
                    Element ResultRequest3 = doc.createElement("ns1:ResultRequest");
                    ResultRequest3.appendChild(doc.createTextNode("mean"));
                    ProcessingTime1.appendChild(ResultRequest3);

                    // ResultRequest4 elements
                    Element ResultRequest4 = doc.createElement("ns1:ResultRequest");
                    ResultRequest4.appendChild(doc.createTextNode("count"));
                    ProcessingTime1.appendChild(ResultRequest4);

                    // ResultRequest5 elements
                    Element ResultRequest5 = doc.createElement("ns1:ResultRequest");
                    ResultRequest5.appendChild(doc.createTextNode("sum"));
                    ProcessingTime1.appendChild(ResultRequest5);

                    // DurationParameter2 elements
                    Element DurationParameter2 = doc.createElement("ns1:DurationParameter");
                    ProcessingTime1.appendChild(DurationParameter2);

                    // set attribute to DurationParameter2 elements
                    Attr DurationParameter21 = doc.createAttribute("value");
                    DurationParameter21.setValue("PT" + properties.get(j).duration + "M");
                    DurationParameter2.setAttributeNode(DurationParameter21);


                    // ResourceParameters3 elements
                    Element ResourceParameters3 = doc.createElement("ns1:ResourceParameters");
                    ElementParameters6.appendChild(ResourceParameters3);

                    // Selection1 elements
                    Element Selection1 = doc.createElement("ns1:Selection");
                    ResourceParameters3.appendChild(Selection1);

                    // ResultRequest6 elements
                    Element ResultRequest6 = doc.createElement("ns1:ResultRequest");
                    ResultRequest6.appendChild(doc.createTextNode("min"));
                    Selection1.appendChild(ResultRequest6);

                    // ResultRequest7 elements
                    Element ResultRequest7 = doc.createElement("ns1:ResultRequest");
                    ResultRequest7.appendChild(doc.createTextNode("max"));
                    Selection1.appendChild(ResultRequest7);

                    // ExpressionParameter1 elements
                    Element ExpressionParameter1 = doc.createElement("ns1:ExpressionParameter");
                    Selection1.appendChild(ExpressionParameter1);

                    // set attribute to ExpressionParameter1 elements
                    Attr ExpressionParameter11 = doc.createAttribute("value");
                    ExpressionParameter11.setValue("bpsim:getResource(\"" + properties.get(j).resource + "\",1)");
                    ExpressionParameter1.setAttributeNode(ExpressionParameter11);

                    // PropertyParameters7 elements
                    Element PropertyParameters7 = doc.createElement("ns1:PropertyParameters");
                    ElementParameters6.appendChild(PropertyParameters7);


                }
            }

            // ElementParameter blokas skirtas nurodyti veiklu pradzios intervalus ir didziausia ju kieki

            Element ElementParameters7 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters7);

            // set attribute to ElementParameters6 elements
            Attr ElementParameters71 = doc.createAttribute("elementRef");
            ElementParameters71.setValue("Id_" + StartEventId);
            ElementParameters7.setAttributeNode(ElementParameters71);

            Element ControlParameters2 = doc.createElement("ns1:ControlParameters");
            ElementParameters7.appendChild(ControlParameters2);

            Element InterTriggerTimer1 = doc.createElement("ns1:InterTriggerTimer");
            ControlParameters2.appendChild(InterTriggerTimer1);

            // ResultRequest7 elements
            Element ResultRequest7 = doc.createElement("ns1:ResultRequest");
            ResultRequest7.appendChild(doc.createTextNode("min"));
            InterTriggerTimer1.appendChild(ResultRequest7);

            // ResultRequest8 elements
            Element ResultRequest8 = doc.createElement("ns1:ResultRequest");
            ResultRequest8.appendChild(doc.createTextNode("max"));
            InterTriggerTimer1.appendChild(ResultRequest8);

            // ResultRequest9 elements
            Element ResultRequest9 = doc.createElement("ns1:ResultRequest");
            ResultRequest9.appendChild(doc.createTextNode("mean"));
            InterTriggerTimer1.appendChild(ResultRequest9);

            // ResultRequest10 elements
            Element ResultRequest10 = doc.createElement("ns1:ResultRequest");
            ResultRequest10.appendChild(doc.createTextNode("sum"));
            InterTriggerTimer1.appendChild(ResultRequest10);

            // NumericParameter2 elements
            Element NumericParameter2 = doc.createElement("ns1:NumericParameter");
            InterTriggerTimer1.appendChild(NumericParameter2);

            Attr NumericParameter21 = doc.createAttribute("value");
            NumericParameter21.setValue(Integer.toString(ArrivingInterval.getArrivingInterval()));
            NumericParameter2.setAttributeNode(NumericParameter21);

            Element TriggerCount1 = doc.createElement("ns1:TriggerCount");
            ControlParameters2.appendChild(TriggerCount1);

            Element TriggerCount11 = doc.createElement("ns1:ResultRequest");
            TriggerCount11.appendChild(doc.createTextNode("count"));
            TriggerCount1.appendChild(TriggerCount11);

            // NumericParameter3 elements
            Element NumericParameter3 = doc.createElement("ns1:NumericParameter");
            TriggerCount1.appendChild(NumericParameter3);

            Attr NumericParameter31 = doc.createAttribute("value");
            NumericParameter31.setValue("1104");
            NumericParameter3.setAttributeNode(NumericParameter31);

            // IRASO TURINI I XML FAILA

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\Users\\Justelio\\Desktop\\SimulationData\\BPSimData-new.xml"));

            transformer.transform(source, result);

            System.out.println("BPSimData File saved on desktop!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

}

class CreateParticipantFile {
    public static void generateParticipantXml() throws TransformerException, ParserConfigurationException {

        ArrayList<ResourcesQuantity> resQ = ResourcesCalculation.getResourceCalculations();
        int a = 0;


        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element Participants = doc.createElement("Participants");
        doc.appendChild(Participants);

        Attr Participants1 = doc.createAttribute("xmlns:xsd");
        Participants1.setValue("http://www.w3.org/2001/XMLSchema");
        Participants.setAttributeNode(Participants1);

        Attr Participants2 = doc.createAttribute("xmlns:xsi");
        Participants2.setValue("http://www.w3.org/2001/XMLSchema-instance");
        Participants.setAttributeNode(Participants2);

        Attr Participants3 = doc.createAttribute("xmlns");
        Participants3.setValue("http://www.wfmc.org/2009/XPDL2.2");
        Participants.setAttributeNode(Participants3);


        for (int i = 0; i < resQ.size(); i++) {

            // ElementParameters4 elements
            Element Participant = doc.createElement("Participant");
            Participants.appendChild(Participant);

            Attr Participant1 = doc.createAttribute("Id");
            Participant1.setValue("8d0bd9c6-d0ca-4bc2-8fa8-ae6c9d76b52" + a);
            Participant.setAttributeNode(Participant1);

            Attr Participant2 = doc.createAttribute("Name");
            Participant2.setValue(resQ.get(i).resourceName);
            Participant.setAttributeNode(Participant2);


            Element ParticipantType = doc.createElement("ParticipantType");
            Participant.appendChild(ParticipantType);

            Attr ParticipantType1 = doc.createAttribute("Type");
            ParticipantType1.setValue("ROLE");
            ParticipantType.setAttributeNode(ParticipantType1);

            Element Description = doc.createElement("Description");
            Participant.appendChild(Description);

            Element ExtendedAttributes = doc.createElement("ExtendedAttributes");
            Participant.appendChild(ExtendedAttributes);


            Element ExtendedAttribute = doc.createElement("ExtendedAttribute");
            ExtendedAttributes.appendChild(ExtendedAttribute);


            Attr ExtendedAttribute1 = doc.createAttribute("Name");
            ExtendedAttribute1.setValue(resQ.get(i).resourceName);
            ExtendedAttribute.setAttributeNode(ExtendedAttribute1);
            a++;
        }


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("C:\\Users\\Justelio\\Desktop\\SimulationData\\Participant-new.xml"));

        transformer.transform(source, result);

        System.out.println("Participant File saved on desktop!");


    }
}

class CreateDiagramFile {

    public static void generateDiagramXml() throws IOException, SAXException, ParserConfigurationException, TransformerException {
        ArrayList<ResourcesQuantity> resQ = ResourcesCalculation.getResourceCalculations();
        int a = 0;


        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Bizagi\\Claim\\Claim 05-18\\86a3a48a-379f-4403-8235-e2f3c2001fb7\\Diagram.xml"));

        NodeList nodes = doc.getElementsByTagName("Pools");


        Element Participants = doc.createElement("Participants");
        doc.appendChild(Participants);


        for (int i = 0; i < resQ.size(); i++) {

            // ElementParameters4 elements
            Element Participant = doc.createElement("Participant");
            Participants.appendChild(Participant);

            Attr Participant1 = doc.createAttribute("Id");
            Participant1.setValue("8d0bd9c6-d0ca-4bc2-8fa8-ae6c9d76b52" + a);
            Participant.setAttributeNode(Participant1);

            Attr Participant2 = doc.createAttribute("Name");
            Participant2.setValue(resQ.get(i).resourceName);
            Participant.setAttributeNode(Participant2);


            Element ParticipantType = doc.createElement("ParticipantType");
            Participant.appendChild(ParticipantType);

            Attr ParticipantType1 = doc.createAttribute("Type");
            ParticipantType1.setValue("ROLE");
            ParticipantType.setAttributeNode(ParticipantType1);

            Element Description = doc.createElement("Description");
            Participant.appendChild(Description);

            Element ExtendedAttributes = doc.createElement("ExtendedAttributes");
            Participant.appendChild(ExtendedAttributes);


            Element ExtendedAttribute = doc.createElement("ExtendedAttribute");
            ExtendedAttributes.appendChild(ExtendedAttribute);


            Attr ExtendedAttribute1 = doc.createAttribute("Name");
            ExtendedAttribute1.setValue(resQ.get(i).resourceName);
            ExtendedAttribute.setAttributeNode(ExtendedAttribute1);
            a++;
        }

        nodes.item(0).getParentNode().insertBefore(Participants, nodes.item(0));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("C:\\Users\\Justelio\\Desktop\\SimulationData\\Diagram-new.xml"));

        transformer.transform(source, result);
        System.out.println("Diagram File saved on desktop!");

    }
}





