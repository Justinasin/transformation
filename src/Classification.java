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

            // PropertyParameters elements
            Element PropertyParameters1 = doc.createElement("ns1:PropertyParameters");
            ScenarioParameters.appendChild(PropertyParameters1);

            // ElementParameters1 elements
            Element ElementParameters1 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters1);

            // set attribute to ElementParameters1 element
            Attr ElementParameters11 = doc.createAttribute("elementRef");
            ElementParameters11.setValue("Id_4c71c2ff-4b98-44fc-a66a-e5ab640832f7");
            ElementParameters1.setAttributeNode(ElementParameters11);

            // PropertyParameters1 elements
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

            // set attribute to ElementParameters2 elements ž
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



