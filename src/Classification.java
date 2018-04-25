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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.text.similarity.LevenshteinDistance;

import static javax.swing.UIManager.get;

public class Classification {


    public static void main(String[] args) throws Exception {

        Resources res = new Resources();
        res.getResources();

        Duration dur = new Duration();
        dur.getDurations();

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
        System.out.println("\nResursai ir resursu kiekis: \n"+similarRes);


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

    public static ArrayList<String> getDurations() throws ParseException {

        ArrayList<String> eventName = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        ArrayList<Integer> minutes = new ArrayList<>();


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
            System.out.println("Trace list dydis: "+nodeList.getLength());


            //--- Get all events names ---//

            for (int x = 1, size = stringList.getLength(); x < size; x++) {
                if (stringList.item(x).getAttributes().getNamedItem("key").getNodeValue().contains("concept:name")) {
                    eventName.add(stringList.item(x).getAttributes().getNamedItem("value").getNodeValue());
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


                        System.out.println(eventName.get(i));
                        System.out.println(duration);

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
        // baigiasi trace ciklai

        HashMap<String, Integer> veikluVidutinesTrukmes = new HashMap<>();

        Iterator<String> ivykiuVardai = veikluVyksmuKiekiai.keySet().iterator();
        while (ivykiuVardai.hasNext()) {
            String ivykioVardas = ivykiuVardai.next();
            veikluVidutinesTrukmes.put(ivykioVardas, veikliuTrukmiuSumos.get(ivykioVardas) / veikluVyksmuKiekiai.get(ivykioVardas) * 2);
        }
        System.out.println("Vidutines veiklų trukmes: \n"+veikluVidutinesTrukmes);
        return eventName;
    }
}
