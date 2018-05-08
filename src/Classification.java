import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.text.similarity.LevenshteinDistance;

import static java.lang.System.in;

public class Classification {


    public static void main(String[] args) throws Exception {

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
        ArrayList<String> eventName = new ArrayList<>();
        Float eventCounter = Float.valueOf(0);
        Float traceCounter = Float.valueOf(0);

        String primaryTask = "";
        String toTask = "";
        ArrayList<String> to = new ArrayList<>();
        ArrayList<String> from = new ArrayList<>();


        NodeList activityList = document.getElementsByTagName("Activity");
        NodeList transitionList = document.getElementsByTagName("Transition");
        HashMap<String, String> probabilities = new HashMap<>();
        String selectedId = "";
        String selectedIdName = "";
        String toIdName = "";


        for (int x = 0, size = activityList.getLength(); x < size; x++) {

            NodeList childList = activityList.item(x).getChildNodes();
            for (int j = 0; j < childList.getLength(); j++) {
                Node childNode = childList.item(j);
                if ("Route".equals(childNode.getNodeName())) {
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
        NodeList nodeList = document1.getElementsByTagName("trace");


        // iteration BEGIN

        for (int a = 0; a < decisions.size(); a++) {
            to.clear();

            iter:
            for (int b = 0, size = transitionList.getLength(); b < size; b++) {

                if (transitionList.item(b).getAttributes().getNamedItem("To").getNodeValue().contains(decisions.get(a))) {
                    selectedId = transitionList.item(b).getAttributes().getNamedItem("From").getNodeValue();
                    for (int c = 0; c < activityList.getLength(); c++) {
                        if (activityList.item(c).getAttributes().getNamedItem("Id").getNodeValue().contains(selectedId)) {
                            selectedIdName = activityList.item(c).getAttributes().getNamedItem("Name").getNodeValue();
                        }
                    }
                    break iter;
                } else {
                    continue iter;
                }
            }


//                    for (int j = 0; j < activityList.getLength(); j++) {
//                        if (activityList.item(j).getAttributes().getNamedItem("Id").getNodeValue().contains(selectedId)) {
//                            from.add(activityList.item(j).getAttributes().getNamedItem("Name").getNodeValue());
//                        }
//                    }

            loop:
            for (int d = 0; d < transitionList.getLength(); d++) {

                if (transitionList.item(d).getAttributes().getNamedItem("From").getNodeValue().contains(decisions.get(a))) {
                    toTask = transitionList.item(d).getAttributes().getNamedItem("To").getNodeValue();

                    for (int e = 0; e < activityList.getLength(); e++) {
                        if (activityList.item(e).getAttributes().getNamedItem("Id").getNodeValue().contains(toTask)) {
                            toIdName = activityList.item(e).getAttributes().getNamedItem("Name").getNodeValue();
                        }
                    }

                    for (int j = 0; j < activityList.getLength(); j++) {
                        if (activityList.item(j).getAttributes().getNamedItem("Id").getNodeValue().contains(toTask)) {
                            to.add(activityList.item(j).getAttributes().getNamedItem("Name").getNodeValue());
                        }
                    }
                }
            }


            //iteration END


            for (int j = 0; j < to.size(); j++) {

                    eventCounter = Float.valueOf(0);
                    for (int f = 0; f < nodeList.getLength(); f++) {
                        eventName.clear();
                        Element element = (Element) nodeList.item(f);
                        NodeList stringList = element.getElementsByTagName("string");

                        for (int g = 1; g < stringList.getLength(); g++) {
                            if (stringList.item(g).getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {
                                eventName.add(stringList.item(g).getAttributes().getNamedItem("value").getNodeValue());
                            }
                        }

                        relationCounter:
                        for (int h = 0; h < eventName.size(); h++) {

                            for (int i = h + 1; i < eventName.size(); i++) {


                                if (eventName.get(h).contains(selectedIdName) && eventName.get(i).contains(to.get(j))) {
                                    eventCounter++;
                                    traceCounter++;
                                    break relationCounter;
                                }
                            }
                        }
                    }

                    float probability = eventCounter / nodeList.getLength();
                    String out = String.format("%.2f", probability);
                    probabilities.put(selectedIdName + " - " + to.get(j), out);
                    System.out.println(probabilities);


            }

        }


//            traceCounter:
//            for (int i = 0; i < eventName.size(); i++) {
//
//                for (int j = i + 1; j < eventName.size(); j++) {
//
//                    if (eventName.get(i).contains(from.get(0)) && (eventName.get(j).contains(to.get(0)) || eventName.get(j).contains(to.get(1)))) {
//                        Float value = tracecounter.get(0);
//                        value = value + 1;
//                        int index = 0;
//                        tracecounter.set(index, value);
//                        break traceCounter;
//                    }
//
//                    if (eventName.get(i).contains(from.get(1)) && (eventName.get(j).contains(to.get(2)) || eventName.get(j).contains(to.get(3)))) {
//                        tracecounter.set(1, tracecounter.get(1) + 1);
//                        break traceCounter;
//                    }
//                }
//            }


        HashSet<String> hs = new HashSet<>(decisions);

        System.out.println(hs);

        return hs;
    }
}



