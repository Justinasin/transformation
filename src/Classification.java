import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.commons.text.similarity.LevenshteinDistance;

import static java.lang.System.in;

public class Classification {


    public static void main(String[] args) throws Exception {

        XmlGenerator xml = new XmlGenerator();
        xml.generateXml();

        AllProbabilities all = new AllProbabilities();
        all.getAllProbabilities();

        Resources res = new Resources();
        res.getResources();

        Duration dur = new Duration();
        dur.getDurations();

        Probability prob = new Probability();
        prob.getProbability();

        HashSet<String> resursai = res.getResources();

        // List<String> a= Arrays.asList(resursai);


        Map<String, Long> results = resursai.stream().collect(Collectors.groupingBy(s -> s.replaceAll("\\d", "").toString(), Collectors.counting()));


        HashMap<String, Long> similarRes = new HashMap<>();
        Iterator<String> resIter = resursai.iterator();
        Iterator<String> resIter2 = resursai.iterator();

        int fullDistance = 0;
        int counter = 0;
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
                if (LevenshteinDistance.getDefaultInstance().apply(key, dabartinisRes) < averageDistance) {
                    similarRes.put(key, similarRes.get(key) + 1); // resursai isgauti pagal levenšteino atstuma
                    pridetas = true;
                }
            }
            if (pridetas == false) {
                similarRes.put(dabartinisRes, 1l);
            }
        }


        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, Long>> iter = results.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Long> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        //System.out.println(sb.toString());
        System.out.println("\nResursai ir resursu kiekis: \n" + similarRes);


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
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\repairExample.xes"));
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

        return hs;
    }


}

class Duration {

    public static int choice;
    public static int selected1;
    public static int selected2;
    public static int selected3;
    public static int selected4;
    public static int selected5;
    public static String event1;
    public static String event2;
    public static String event3;
    public static String event4;
    public static String event5;


    public static ArrayList<String> eventName = new ArrayList<>();
    public static ArrayList<String> time = new ArrayList<>();
    public static ArrayList<String> status = new ArrayList<>();
    public static ArrayList<Integer> minutes = new ArrayList<>();

    public static ArrayList<String> getDurations() throws ParseException {

        Scanner input = new Scanner(in);


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\repairExample.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, Integer> veikliuTrukmiuSumos = new HashMap<>(); //veiklu trukmiu sumos
        HashMap<String, Integer> veikluVyksmuKiekiai = new HashMap<>(); //veiklu kiekiai
        ArrayList<String> uniqueEvents = new ArrayList<>();


        NodeList nodeList = document.getElementsByTagName("trace");


        //ciklo pradzia
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

            //----------------------------//

            //--- Get all times of every event in trace ---//

            for (int y = 0, size = dateList.getLength(); y < size; y++) {
                if (dateList.item(y).getAttributes().getNamedItem("key").getNodeValue().contains("time:timestamp")) {
                    time.add(dateList.item(y).getAttributes().getNamedItem("value").getNodeValue());
                }
            }

            //----------------------------//

            //--- Get all times in minutes ---//

            SimpleDateFormat str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SS:SS");
            Date data;

            for (int i = 0; i < time.size(); i++) {

                data = str.parse(time.get(i));
                minutes.add(data.getHours() * 60 + data.getMinutes());
            }

            //----------------------------//

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
                    veikliuTrukmiuSumos.put(ivykisI, 0);
                }
                if (!veikluVyksmuKiekiai.containsKey(ivykisI)) {
                    veikluVyksmuKiekiai.put(ivykisI, 1);
                } else {
                    veikluVyksmuKiekiai.put(ivykisI, veikluVyksmuKiekiai.get(ivykisI) + 1);
                }

                int duration = 0;
                for (int j = i + 1; j < eventName.size(); j++) {
                    if (ivykisI.equals(eventName.get(j)) && status.get(i).equals("start") && status.get(j).equals("complete")) {


                        duration = minutes.get(j) - minutes.get(i);
                        if (duration >= 0) {
                            veikliuTrukmiuSumos.put(ivykisI, veikliuTrukmiuSumos.get(ivykisI) + duration);
                        }


//                        System.out.println(eventName.get(i));
//                        System.out.println(duration);

//                    System.out.println(k);
//                    System.out.println(j);
//                    System.out.println(status.get(k));
//                    System.out.println(status.get(j));
//                    System.out.println(time.get(k));

                        break;
                    }
                }


            }
        }

        //--- Gauname unikalius ivykius ---//


        //---//


        //--- Ivesti pradine veikla ---//

        System.out.println("Visi ivykiai: " + eventName);

        System.out.println("Unikalus ivikiai: " + uniqueEvents);

        System.out.println("Iveskite pradine veikla (ivesti skaiciu):\n");

        for (int i = 0; i < uniqueEvents.size(); i++) {

            System.out.println(i + 1 + " - " + uniqueEvents.get(i));
        }

        selected1 = input.nextInt();

        //---//

        System.out.println("Kiek ivykiu iseina is pasirinkimo mazgo (2,3,4)?\n ");

        choice = input.nextInt();

        if (choice == 2) {

            System.out.println("Iveskite " + (choice - 1) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected2 = input.nextInt();

            System.out.println("Iveskite " + choice + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected3 = input.nextInt();

            event1 = uniqueEvents.get(selected1 - 1);
            event2 = uniqueEvents.get(selected2 - 1);
            event3 = uniqueEvents.get(selected3 - 1);

        } else if (choice == 3) {

            System.out.println("Iveskite " + (choice - 2) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected2 = input.nextInt();

            System.out.println("Iveskite " + (choice - 1) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected3 = input.nextInt();

            System.out.println("Iveskite " + (choice) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected4 = input.nextInt();

            event1 = uniqueEvents.get(selected1 - 1);
            event2 = uniqueEvents.get(selected2 - 1);
            event3 = uniqueEvents.get(selected3 - 1);
            event4 = uniqueEvents.get(selected4 - 1);
        } else if (choice == 4) {

            System.out.println("Iveskite " + (choice - 3) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected2 = input.nextInt();

            System.out.println("Iveskite " + (choice - 2) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected3 = input.nextInt();

            System.out.println("Iveskite " + (choice - 1) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected4 = input.nextInt();

            System.out.println("Iveskite " + (choice) + "-osios veiklos pavadinima, kurios skaiciuosime tikimybini ivykti (ivesti skaiciu):\n");

            for (int j = 0; j < uniqueEvents.size(); j++) {

                System.out.println(j + 1 + " - " + uniqueEvents.get(j));//
            }

            selected5 = input.nextInt();

            event1 = uniqueEvents.get(selected1 - 1);
            event2 = uniqueEvents.get(selected2 - 1);
            event3 = uniqueEvents.get(selected3 - 1);
            event4 = uniqueEvents.get(selected4 - 1);
            event5 = uniqueEvents.get(selected5 - 1);
        }


        //---//

        // baigiasi trace ciklai

        HashMap<String, Integer> veikluVidutinesTrukmes = new HashMap<>();

        Iterator<String> ivykiuVardai = veikluVyksmuKiekiai.keySet().iterator();
        while (ivykiuVardai.hasNext()) {
            String ivykioVardas = ivykiuVardai.next();
            veikluVidutinesTrukmes.put(ivykioVardas, veikliuTrukmiuSumos.get(ivykioVardas) / veikluVyksmuKiekiai.get(ivykioVardas) * 2);
        }
        System.out.println("Vidutines veiklų trukmes: \n" + veikluVidutinesTrukmes);
        return eventName;
    }


}

class Probability {


    public static ArrayList<String> eventName = new ArrayList<>();
    public static ArrayList<String> status = new ArrayList<>();

    public static ArrayList<Integer> getProbability() throws ParseException {


        Duration duration = new Duration();
        int choice = duration.choice;
        String selected1 = duration.event1;
        String selected2 = duration.event2;
        String selected3 = duration.event3;
        String selected4 = duration.event4;
        String selected5 = duration.event5;

        ArrayList<Integer> prob = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\repairExample.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList nodeList = document.getElementsByTagName("trace");
        int traceNumber = nodeList.getLength();
        int tracecounter = 0;
        int event1Counter = 0;
        int event2Counter = 0;
        int event3Counter = 0;
        int event4Counter = 0;

        for (int k = 0; k < nodeList.getLength(); k++) {
            eventName.clear();
            Element element = (Element) nodeList.item(k);
            NodeList stringList = element.getElementsByTagName("string");

            for (int x = 1, size = stringList.getLength(); x < size; x++) {
                if (stringList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {
                    eventName.add(stringList.item(x).getAttributes().getNamedItem("value").getNodeValue());
                }
            }

            for (int x = 0, size = stringList.getLength(); x < size; x++) {
                if (stringList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("lifecycle:transition")) {
                    status.add(stringList.item(x).getAttributes().getNamedItem("value").getNodeValue());
                }
            }


            traceCounter:
            for (int i = 0; i < eventName.size(); i++) {

                for (int j = i + 1; j < eventName.size(); j++) {

                    if (choice == 2) {

                        if (eventName.get(i).contains(selected1) && (eventName.get(j).contains(selected2) || eventName.get(j).contains(selected3))) {
                            tracecounter++;
                            break traceCounter;
                        }
                    }

                    if (choice == 3) {

                        if (eventName.get(i).contains(selected1) && (eventName.get(j).contains(selected2) || eventName.get(j).contains(selected3) || eventName.get(j).contains(selected4))) {
                            tracecounter++;
                            break traceCounter;
                        }
                    }

                    if (choice == 4) {

                        if (eventName.get(i).contains(selected1) && (eventName.get(j).contains(selected2) || eventName.get(j).contains(selected3) || eventName.get(j).contains(selected4) || eventName.get(j).contains(selected5))) {
                            tracecounter++;
                            break traceCounter;
                        }
                    }

                }
            }

            relationCounter:
            for (int i = 0; i < eventName.size(); i++) {

                for (int j = i + 1; j < eventName.size(); j++) {

                    if (choice == 2) {

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected2)) {
                            event1Counter++;
                            break relationCounter;
                        }

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected3)) {
                            event2Counter++;
                            break relationCounter;
                        }
                    }

                    if (choice == 3) {

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected2)) {
                            event1Counter++;
                            break relationCounter;
                        }

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected3)) {
                            event2Counter++;
                            break relationCounter;
                        }

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected4)) {
                            event3Counter++;
                            break relationCounter;
                        }
                    }

                    if (choice == 4) {

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected2)) {
                            event1Counter++;
                            break relationCounter;
                        }

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected3)) {
                            event2Counter++;
                            break relationCounter;
                        }

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected4)) {
                            event3Counter++;
                            break relationCounter;
                        }

                        if (eventName.get(i).contains(selected1) && eventName.get(j).contains(selected5)) {
                            event4Counter++;
                            break relationCounter;
                        }
                    }
                }
            }


        }

        if (choice == 2) {

            float probability1 = ((float) event1Counter) / tracecounter * 100;
            float probability2 = ((float) event2Counter) / tracecounter * 100;

            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected2 + " =  " + event1Counter);
            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected3 + " =  " + event2Counter);
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected2 + " =  " + String.format("%.2f", probability1) + "%");
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected3 + " =  " + String.format("%.2f", probability2) + "%");
        }

        if (choice == 3) {

            float probability1 = ((float) event1Counter) / tracecounter * 100;
            float probability2 = ((float) event2Counter) / tracecounter * 100;
            float probability3 = ((float) event3Counter) / tracecounter * 100;

            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected2 + " =  " + event1Counter);
            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected3 + " =  " + event2Counter);
            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected4 + " =  " + event3Counter);
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected2 + " =  " + String.format("%.2f", probability1) + "%");
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected3 + " =  " + String.format("%.2f", probability2) + "%");
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected4 + " =  " + String.format("%.2f", probability3) + "%");
        }

        if (choice == 4) {

            float probability1 = ((float) event1Counter) / tracecounter * 100;
            float probability2 = ((float) event2Counter) / tracecounter * 100;
            float probability3 = ((float) event3Counter) / tracecounter * 100;
            float probability4 = ((float) event4Counter) / tracecounter * 100;

            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected2 + " =  " + event1Counter);
            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected3 + " =  " + event2Counter);
            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected4 + " =  " + event3Counter);
            System.out.println("Seku skaicius tarp veiklu: " + selected1 + " - " + selected5 + " =  " + event4Counter);
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected2 + " =  " + String.format("%.2f", probability1) + "%");
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected3 + " =  " + String.format("%.2f", probability2) + "%");
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected4 + " =  " + String.format("%.2f", probability3) + "%");
            System.out.println("Tikimybe vykti veiklu sekai: " + selected1 + " - " + selected5 + " =  " + String.format("%.2f", probability4) + "%");
        }


        System.out.println("Is viso trace: " + tracecounter);
        System.out.println("Trace skaicius pagal COUNT TRACE: " + traceNumber);


        return prob;
    }

}

class AllProbabilities {

    public static HashSet<String> getAllProbabilities() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = db.parse(new File("C:\\Users\\Justelio\\Desktop\\test\\07735e1b-95a6-4405-9182-1ac672a482b3\\Diagram.xml"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> decisions = new ArrayList<>();
        ArrayList<Float> tracecounter = new ArrayList<Float>(Collections.<Float>nCopies(4, (float) 0));
        Float eventCounter = Float.valueOf(0);
        String primaryTask = "";
        String toTask = "";
        ArrayList<String> to = new ArrayList<>();
        ArrayList<String> from = new ArrayList<>();


        NodeList activityList = document.getElementsByTagName("Activity");
        NodeList transitionList = document.getElementsByTagName("Transition");
        HashMap<String, Float> probabilities = new HashMap<>();

        for (int x = 0, size = activityList.getLength(); x < size; x++) {

            NodeList childList = activityList.item(x).getChildNodes();
            for (int j = 0; j < childList.getLength(); j++) {
                Node childNode = childList.item(j);
                String childNodeName = childNode.getNodeName();
                if ("Route".equals(childNodeName)) {
                    //Route elementai, kuria yra Activity vaikai, yra sprendimo mazgai. Pridedam i sprendimu mazgu masyva Route elemento id.
                    decisions.add(activityList.item(x).getAttributes().getNamedItem("Id").getNodeValue());
                }
            }

        }

        DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();
        DocumentBuilder db1 = null;

        try {
            db1 = dbf1.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document1 = null;
        try {
            document1 = db1.parse(new File("C:\\VGTU\\Magistaras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\repairExample.xes"));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList traceList = document1.getElementsByTagName("trace");


        String selectedId = "";
        String selectedIdName = "";
        String toIdName = "";
        HashMap<String, Float> transitionProbabilities = new HashMap<String, Float>();
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
        System.out.println(transitionProbabilities);
        HashSet<String> hs = new HashSet<>(decisions);

        System.out.println(hs);

        return hs;
    }
}

class XmlGenerator {

    public static void generateXml() {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

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

            // Duration elements
            Element Duration = doc.createElement("ns1:Duration");
            ScenarioParameters.appendChild(Duration);

            // DurationParameter elements
            Element DurationParameter = doc.createElement("ns1:DurationParameter");
            Duration.appendChild(DurationParameter);

            // PropertyParameters1 elements
            Element PropertyParameters1 = doc.createElement("ns1:PropertyParameters");
            ScenarioParameters.appendChild(PropertyParameters1);

            // ElementParameters1 elements
            Element ElementParameters1 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters1);

            // set attribute to ElementParameters1 element
            Attr ElementParameters11 = doc.createAttribute("elementRef");
            ElementParameters11.setValue("Id_4c71c2ff-4b98-44fc-a66a-e5ab640832f7");
            ElementParameters1.setAttributeNode(ElementParameters11);

            // PropertyParameters2 elements
            Element PropertyParameters2 = doc.createElement("ns1:PropertyParameters");
            ElementParameters1.appendChild(PropertyParameters2);

            // ElementParameters2 elements
            Element ElementParameters2 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters2);

            // set attribute to ElementParameters2 elements ž
            Attr ElementParameters21 = doc.createAttribute("elementRef");
            ElementParameters21.setValue("Id_3ac1ea1c-1349-4ff3-9c34-89ab9f3943bb");
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
            FloatingParameter11.setValue("0.33");
            FloatingParameter1.setAttributeNode(FloatingParameter11);

            // PropertyParameters3 elements
            Element PropertyParameters3 = doc.createElement("ns1:PropertyParameters");
            ElementParameters2.appendChild(PropertyParameters3);

            // ElementParameters3 elements
            Element ElementParameters3 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters3);

            // set attribute to ElementParameters2 elements
            Attr ElementParameters31 = doc.createAttribute("elementRef");
            ElementParameters31.setValue("Id_3eb60779-086b-48fa-9981-1005e3461871");
            ElementParameters3.setAttributeNode(ElementParameters31);

            // ControlParameters2 elements
            Element ControlParameters2 = doc.createElement("ns1:ControlParameters");
            ElementParameters3.appendChild(ControlParameters2);

            // Probability2 elements
            Element Probability2 = doc.createElement("ns1:Probability");
            ControlParameters2.appendChild(Probability2);

            // Floating2 elements
            Element FloatingParameter2 = doc.createElement("ns1:FloatingParameter");
            Probability2.appendChild(FloatingParameter2);

            // set attribute to Floating2 element
            Attr FloatingParameter21 = doc.createAttribute("value");
            FloatingParameter21.setValue("0.67");
            FloatingParameter2.setAttributeNode(FloatingParameter21);

            // PropertyParameters4 elements
            Element PropertyParameters4 = doc.createElement("ns1:PropertyParameters");
            ElementParameters3.appendChild(PropertyParameters4);

            // ElementParameters4 elements
            Element ElementParameters4 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters4);

            // set attribute to ElementParameters4 elements
            Attr ElementParameters41 = doc.createAttribute("elementRef");
            ElementParameters41.setValue("Tester");
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
            NumericParameter11.setValue("6");
            NumericParameter1.setAttributeNode(NumericParameter11);

            // PropertyParameters5 elements
            Element PropertyParameters5 = doc.createElement("ns1:PropertyParameters");
            ElementParameters4.appendChild(PropertyParameters5);


            // ElementParameters5 elements
            Element ElementParameters5 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters5);

            // set attribute to ElementParameters5 elements
            Attr ElementParameters51 = doc.createAttribute("elementRef");
            ElementParameters51.setValue("Solver");
            ElementParameters5.setAttributeNode(ElementParameters51);

            // ResourceParameters2 elements
            Element ResourceParameters2 = doc.createElement("ns1:ResourceParameters");
            ElementParameters5.appendChild(ResourceParameters2);

            // Quantity2 elements
            Element Quantity2 = doc.createElement("ns1:Quantity");
            ResourceParameters2.appendChild(Quantity2);

            // NumericParameter2 elements
            Element NumericParameter2 = doc.createElement("ns1:NumericParameter");
            Quantity2.appendChild(NumericParameter2);

            // set attribute to NumericParameter2 elements
            Attr NumericParameter21 = doc.createAttribute("value");
            NumericParameter21.setValue("6");
            NumericParameter2.setAttributeNode(NumericParameter21);

            // PropertyParameters6 elements
            Element PropertyParameters6 = doc.createElement("ns1:PropertyParameters");
            ElementParameters5.appendChild(PropertyParameters6);




            // ElementParameters6 elements
            Element ElementParameters6 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters6);

            // set attribute to ElementParameters6 elements
            Attr ElementParameters61 = doc.createAttribute("elementRef");
            ElementParameters61.setValue("Id_477c3e9d-6c58-4d23-93ca-71fe9f30d8e4");
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
            DurationParameter21.setValue("PT10M");
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
            ExpressionParameter11.setValue("bpsim:getResource(\"Tester\",1)");
            ExpressionParameter1.setAttributeNode(ExpressionParameter11);

            // ElementParameters7 elements
            Element ElementParameters7 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters7);

            // PropertyParameters7 elements
            Element PropertyParameters7 = doc.createElement("ns1:PropertyParameters");
            ElementParameters6.appendChild(PropertyParameters7);

            // set attribute to ElementParameters7 elements
            Attr ElementParameters71 = doc.createAttribute("elementRef");
            ElementParameters71.setValue("Id_56e28358-745e-4bf1-a602-6ae9f3133ae0");
            ElementParameters7.setAttributeNode(ElementParameters71);

            // TimeParameters2 elements
            Element TimeParameters2 = doc.createElement("ns1:TimeParameters");
            ElementParameters7.appendChild(TimeParameters2);

            // ProcessingTime2 elements
            Element ProcessingTime2 = doc.createElement("ns1:ProcessingTime");
            TimeParameters2.appendChild(ProcessingTime2);

            // ResultRequest8 elements
            Element ResultRequest8 = doc.createElement("ns1:ResultRequest");
            ResultRequest8.appendChild(doc.createTextNode("min"));
            ProcessingTime2.appendChild(ResultRequest8);

            // ResultRequest9 elements
            Element ResultRequest9 = doc.createElement("ns1:ResultRequest");
            ResultRequest9.appendChild(doc.createTextNode("max"));
            ProcessingTime2.appendChild(ResultRequest9);

            // ResultRequest10 elements
            Element ResultRequest10 = doc.createElement("ns1:ResultRequest");
            ResultRequest10.appendChild(doc.createTextNode("mean"));
            ProcessingTime2.appendChild(ResultRequest10);

            // ResultRequest11 elements
            Element ResultRequest11 = doc.createElement("ns1:ResultRequest");
            ResultRequest11.appendChild(doc.createTextNode("count"));
            ProcessingTime2.appendChild(ResultRequest11);

            // ResultRequest12 elements
            Element ResultRequest12 = doc.createElement("ns1:ResultRequest");
            ResultRequest12.appendChild(doc.createTextNode("sum"));
            ProcessingTime2.appendChild(ResultRequest12);

            // DurationParameter3 elements
            Element DurationParameter3 = doc.createElement("ns1:DurationParameter");
            ProcessingTime2.appendChild(DurationParameter3);

            // set attribute to DurationParameter3 elements
            Attr DurationParameter31 = doc.createAttribute("value");
            DurationParameter31.setValue("PT20M");
            DurationParameter3.setAttributeNode(DurationParameter31);



            // ResourceParameters4 elements
            Element ResourceParameters4 = doc.createElement("ns1:ResourceParameters");
            ElementParameters7.appendChild(ResourceParameters4);

            // Selection2 elements
            Element Selection2 = doc.createElement("ns1:Selection");
            ResourceParameters4.appendChild(Selection2);

            // ResultRequest13 elements
            Element ResultRequest13 = doc.createElement("ns1:ResultRequest");
            ResultRequest13.appendChild(doc.createTextNode("min"));
            Selection2.appendChild(ResultRequest13);

            // ResultRequest14 elements
            Element ResultRequest14 = doc.createElement("ns1:ResultRequest");
            ResultRequest14.appendChild(doc.createTextNode("max"));
            Selection2.appendChild(ResultRequest14);

            // ExpressionParameter2 elements
            Element ExpressionParameter2 = doc.createElement("ns1:ExpressionParameter");
            Selection2.appendChild(ExpressionParameter2);

            // set attribute to ExpressionParameter2 elements
            Attr ExpressionParameter21 = doc.createAttribute("value");
            ExpressionParameter21.setValue("bpsim:getResource(\"Solver\",1)");
            ExpressionParameter2.setAttributeNode(ExpressionParameter21);

            // ElementParameters8 elements
            Element ElementParameters8 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters8);

            // PropertyParameters8 elements
            Element PropertyParameters8 = doc.createElement("ns1:PropertyParameters");
            ElementParameters7.appendChild(PropertyParameters8);

            // set attribute to ElementParameters8 elements
            Attr ElementParameters81 = doc.createAttribute("elementRef");
            ElementParameters81.setValue("Id_5d83490e-90d2-45a3-91e1-b06d06892dcb");
            ElementParameters8.setAttributeNode(ElementParameters81);

            // TimeParameters3 elements
            Element TimeParameters3 = doc.createElement("ns1:TimeParameters");
            ElementParameters8.appendChild(TimeParameters3);

            // ProcessingTime3 elements
            Element ProcessingTime3 = doc.createElement("ns1:ProcessingTime");
            TimeParameters3.appendChild(ProcessingTime3);

            // ResultRequest15 elements
            Element ResultRequest15 = doc.createElement("ns1:ResultRequest");
            ResultRequest15.appendChild(doc.createTextNode("min"));
            ProcessingTime3.appendChild(ResultRequest15);

            // ResultRequest16 elements
            Element ResultRequest16 = doc.createElement("ns1:ResultRequest");
            ResultRequest16.appendChild(doc.createTextNode("max"));
            ProcessingTime3.appendChild(ResultRequest16);

            // ResultRequest17 elements
            Element ResultRequest17 = doc.createElement("ns1:ResultRequest");
            ResultRequest17.appendChild(doc.createTextNode("mean"));
            ProcessingTime3.appendChild(ResultRequest17);

            // ResultRequest18 elements
            Element ResultRequest18 = doc.createElement("ns1:ResultRequest");
            ResultRequest18.appendChild(doc.createTextNode("count"));
            ProcessingTime3.appendChild(ResultRequest18);

            // ResultRequest19 elements
            Element ResultRequest19 = doc.createElement("ns1:ResultRequest");
            ResultRequest19.appendChild(doc.createTextNode("sum"));
            ProcessingTime3.appendChild(ResultRequest19);

            // DurationParameter4 elements
            Element DurationParameter4 = doc.createElement("ns1:DurationParameter");
            ProcessingTime3.appendChild(DurationParameter4);

            // set attribute to DurationParameter4 elements
            Attr DurationParameter41 = doc.createAttribute("value");
            DurationParameter41.setValue("PT40M");
            DurationParameter4.setAttributeNode(DurationParameter41);



            // ResourceParameters5 elements
            Element ResourceParameters5 = doc.createElement("ns1:ResourceParameters");
            ElementParameters8.appendChild(ResourceParameters5);

            // Selection3 elements
            Element Selection3 = doc.createElement("ns1:Selection");
            ResourceParameters5.appendChild(Selection3);

            // ResultRequest20 elements
            Element ResultRequest20 = doc.createElement("ns1:ResultRequest");
            ResultRequest20.appendChild(doc.createTextNode("min"));
            Selection3.appendChild(ResultRequest20);

            // ResultRequest21 elements
            Element ResultRequest21 = doc.createElement("ns1:ResultRequest");
            ResultRequest21.appendChild(doc.createTextNode("max"));
            Selection3.appendChild(ResultRequest21);

            // ExpressionParameter3 elements
            Element ExpressionParameter3 = doc.createElement("ns1:ExpressionParameter");
            Selection3.appendChild(ExpressionParameter3);

            // set attribute to ExpressionParameter3 elements
            Attr ExpressionParameter31 = doc.createAttribute("value");
            ExpressionParameter31.setValue("bpsim:getResource(\"Solver\",1)");
            ExpressionParameter3.setAttributeNode(ExpressionParameter31);

            // ElementParameters9 elements
            Element ElementParameters9 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters9);

            // PropertyParameters9 elements
            Element PropertyParameters9 = doc.createElement("ns1:PropertyParameters");
            ElementParameters8.appendChild(PropertyParameters9);

            // set attribute to ElementParameters9 elements
            Attr ElementParameters91 = doc.createAttribute("elementRef");
            ElementParameters91.setValue("Id_98229681-104a-4a22-a0ef-88573797c602");
            ElementParameters9.setAttributeNode(ElementParameters91);

            // TimeParameters4 elements
            Element TimeParameters4 = doc.createElement("ns1:TimeParameters");
            ElementParameters9.appendChild(TimeParameters4);

            // ProcessingTime4 elements
            Element ProcessingTime4 = doc.createElement("ns1:ProcessingTime");
            TimeParameters4.appendChild(ProcessingTime4);

            // ResultRequest22 elements
            Element ResultRequest22 = doc.createElement("ns1:ResultRequest");
            ResultRequest22.appendChild(doc.createTextNode("min"));
            ProcessingTime4.appendChild(ResultRequest22);

            // ResultRequest23 elements
            Element ResultRequest23 = doc.createElement("ns1:ResultRequest");
            ResultRequest23.appendChild(doc.createTextNode("max"));
            ProcessingTime4.appendChild(ResultRequest23);

            // ResultRequest24 elements
            Element ResultRequest24 = doc.createElement("ns1:ResultRequest");
            ResultRequest24.appendChild(doc.createTextNode("mean"));
            ProcessingTime4.appendChild(ResultRequest24);

            // ResultRequest25 elements
            Element ResultRequest25 = doc.createElement("ns1:ResultRequest");
            ResultRequest25.appendChild(doc.createTextNode("count"));
            ProcessingTime4.appendChild(ResultRequest25);

            // ResultRequest26 elements
            Element ResultRequest26 = doc.createElement("ns1:ResultRequest");
            ResultRequest26.appendChild(doc.createTextNode("sum"));
            ProcessingTime4.appendChild(ResultRequest26);

            // DurationParameter5 elements
            Element DurationParameter5 = doc.createElement("ns1:DurationParameter");
            ProcessingTime4.appendChild(DurationParameter5);

            // set attribute to DurationParameter5 elements
            Attr DurationParameter51 = doc.createAttribute("value");
            DurationParameter51.setValue("PT15M");
            DurationParameter5.setAttributeNode(DurationParameter51);

            // ResourceParameters6 elements
            Element ResourceParameters6 = doc.createElement("ns1:ResourceParameters");
            ElementParameters9.appendChild(ResourceParameters6);

            // Selection4 elements
            Element Selection4 = doc.createElement("ns1:Selection");
            ResourceParameters6.appendChild(Selection4);

            // ResultRequest27 elements
            Element ResultRequest27 = doc.createElement("ns1:ResultRequest");
            ResultRequest27.appendChild(doc.createTextNode("min"));
            Selection4.appendChild(ResultRequest27);

            // ResultRequest28 elements
            Element ResultRequest28 = doc.createElement("ns1:ResultRequest");
            ResultRequest28.appendChild(doc.createTextNode("max"));
            Selection4.appendChild(ResultRequest28);

            // ExpressionParameter4 elements
            Element ExpressionParameter4 = doc.createElement("ns1:ExpressionParameter");
            Selection4.appendChild(ExpressionParameter4);

            // set attribute to ExpressionParameter4 elements
            Attr ExpressionParameter41 = doc.createAttribute("value");
            ExpressionParameter41.setValue("bpsim:getResource(\"Tester\",1)");
            ExpressionParameter4.setAttributeNode(ExpressionParameter41);

            // PropertyParameters10 elements
            Element PropertyParameters10 = doc.createElement("ns1:PropertyParameters");
            ElementParameters9.appendChild(PropertyParameters10);

            // ElementParameters10 elements
            Element ElementParameters10 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters10);

            // set attribute to ElementParameters10 elements
            Attr ElementParameters101 = doc.createAttribute("elementRef");
            ElementParameters101.setValue("Id_5586ee57-8143-4dc9-a502-ddcabfd0547e");
            ElementParameters10.setAttributeNode(ElementParameters101);

            // TimeParameters5 elements
            Element TimeParameters5 = doc.createElement("ns1:TimeParameters");
            ElementParameters10.appendChild(TimeParameters5);

            // ProcessingTime5 elements
            Element ProcessingTime5 = doc.createElement("ns1:ProcessingTime");
            TimeParameters5.appendChild(ProcessingTime5);

            // ResultRequest29 elements
            Element ResultRequest29 = doc.createElement("ns1:ResultRequest");
            ResultRequest29.appendChild(doc.createTextNode("min"));
            ProcessingTime5.appendChild(ResultRequest29);

            // ResultRequest30 elements
            Element ResultRequest30 = doc.createElement("ns1:ResultRequest");
            ResultRequest30.appendChild(doc.createTextNode("max"));
            ProcessingTime5.appendChild(ResultRequest30);

            // ResultRequest31 elements
            Element ResultRequest31 = doc.createElement("ns1:ResultRequest");
            ResultRequest31.appendChild(doc.createTextNode("mean"));
            ProcessingTime5.appendChild(ResultRequest31);

            // ResultRequest32 elements
            Element ResultRequest32 = doc.createElement("ns1:ResultRequest");
            ResultRequest32.appendChild(doc.createTextNode("count"));
            ProcessingTime5.appendChild(ResultRequest32);

            // ResultRequest33 elements
            Element ResultRequest33 = doc.createElement("ns1:ResultRequest");
            ResultRequest33.appendChild(doc.createTextNode("sum"));
            ProcessingTime5.appendChild(ResultRequest33);

            // DurationParameter6 elements
            Element DurationParameter6 = doc.createElement("ns1:DurationParameter");
            ProcessingTime5.appendChild(DurationParameter6);

            // set attribute to DurationParameter6 elements
            Attr DurationParameter61 = doc.createAttribute("value");
            DurationParameter61.setValue("PT10M");
            DurationParameter6.setAttributeNode(DurationParameter61);



            // ResourceParameters7 elements
            Element ResourceParameters7 = doc.createElement("ns1:ResourceParameters");
            ElementParameters10.appendChild(ResourceParameters7);

            // Selection5 elements
            Element Selection5 = doc.createElement("ns1:Selection");
            ResourceParameters7.appendChild(Selection5);

            // ResultRequest34 elements
            Element ResultRequest34 = doc.createElement("ns1:ResultRequest");
            ResultRequest34.appendChild(doc.createTextNode("min"));
            Selection5.appendChild(ResultRequest34);

            // ResultRequest35 elements
            Element ResultRequest35 = doc.createElement("ns1:ResultRequest");
            ResultRequest35.appendChild(doc.createTextNode("max"));
            Selection5.appendChild(ResultRequest35);

            // ExpressionParameter5 elements
            Element ExpressionParameter5 = doc.createElement("ns1:ExpressionParameter");
            Selection5.appendChild(ExpressionParameter5);

            // set attribute to ExpressionParameter5 elements
            Attr ExpressionParameter51 = doc.createAttribute("value");
            ExpressionParameter51.setValue("bpsim:getResource(\"Solver\",1)");
            ExpressionParameter5.setAttributeNode(ExpressionParameter51);

            // PropertyParameters11 elements
            Element PropertyParameters11 = doc.createElement("ns1:PropertyParameters");
            ElementParameters10.appendChild(PropertyParameters11);

            // ElementParameters110 elements
            Element ElementParameters110 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters110);

            // set attribute to ElementParameters110 elements
            Attr ElementParameters111 = doc.createAttribute("elementRef");
            ElementParameters111.setValue("Id_64665022-ea09-4248-8a1f-2a3a464e5c71");
            ElementParameters110.setAttributeNode(ElementParameters111);


            // PropertyParameters12 elements
            Element PropertyParameters12 = doc.createElement("ns1:PropertyParameters");
            ElementParameters110.appendChild(PropertyParameters12);

            // ElementParameters120 elements
            Element ElementParameters120 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters120);

            // set attribute to ElementParameters120 elements
            Attr ElementParameters121 = doc.createAttribute("elementRef");
            ElementParameters121.setValue("Id_dc3040e3-a968-45dc-bdb6-a76894eb31fc");
            ElementParameters120.setAttributeNode(ElementParameters121);


            // PropertyParameters13 elements
            Element PropertyParameters13 = doc.createElement("ns1:PropertyParameters");
            ElementParameters120.appendChild(PropertyParameters13);

            // ElementParameters130 elements
            Element ElementParameters130 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters130);

            // set attribute to ElementParameters130 elements
            Attr ElementParameters131 = doc.createAttribute("elementRef");
            ElementParameters131.setValue("Id_ccd37ceb-b8ab-4ce9-a6fc-e0ddf7f40bc4");
            ElementParameters130.setAttributeNode(ElementParameters131);


            // PropertyParameters14 elements
            Element PropertyParameters14 = doc.createElement("ns1:PropertyParameters");
            ElementParameters130.appendChild(PropertyParameters14);

            // ElementParameters140 elements
            Element ElementParameters140 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters140);

            // set attribute to ElementParameters140 elements
            Attr ElementParameters141 = doc.createAttribute("elementRef");
            ElementParameters141.setValue("Id_3e3d3b23-fbcf-49bb-8b3d-7a06e4391f7a");
            ElementParameters140.setAttributeNode(ElementParameters141);


            // PropertyParameters15 elements
            Element PropertyParameters15 = doc.createElement("ns1:PropertyParameters");
            ElementParameters140.appendChild(PropertyParameters15);


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\Users\\Justelio\\Desktop\\file.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved on desktop!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

}



