import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Duration {

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
            document = db.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
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
                minutes.add((int) Math.ceil(data.getHours() * 60.0 + data.getMinutes() + data.getSeconds() / 60.0));
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
                    if (status.size() > 0) {
                        if (ivykisI.equals(eventName.get(j)) && status.get(i).equals("start") && status.get(j).equals("complete")) {


                            duration = minutes.get(j) - minutes.get(i);
                            durationInSec = seconds.get(j) - seconds.get(i);
                            if (duration >= 1) { //NAUJAS
                                veikliuTrukmiuSumos.put(ivykisI, veikliuTrukmiuSumos.get(ivykisI) + duration);
                            } else {
                                veikliuTrukmiuSumos.put(ivykisI, (veikliuTrukmiuSumos.get(ivykisI) + durationInSec / 60.0));
                            } // NAUJAS

                            break;
                        }
                    } else { // NAUJAS
                        veikliuTrukmiuSumos.put(ivykisI, veikliuTrukmiuSumos.get(ivykisI) + duration); // NAUJAS
                    } // NAUJAS
                }

            }
        }


        System.out.println("Visi ivykiai: " + eventName);

        System.out.println("Unikalus ivikiai: " + uniqueEvents);

        HashMap<String, Integer> veikluVidutinesTrukmes = new HashMap<>();

        Iterator<String> ivykiuVardai = veikluVyksmuKiekiai.keySet().iterator();
        while (ivykiuVardai.hasNext()) {
            String ivykioVardas = ivykiuVardai.next();
            veikluVidutinesTrukmes.put(ivykioVardas, (int) Math.ceil(veikliuTrukmiuSumos.get(ivykioVardas) / ((veikluVyksmuKiekiai.get(ivykioVardas)) / 2)));
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