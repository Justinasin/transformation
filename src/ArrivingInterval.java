import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ArrivingInterval {

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
            document = db.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
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
        /*  finalArrival = allArrivals / uniqueDates.size();*/ // getting average arrival size
        finalArrival = 0; // NAUJAS

        return finalArrival; // return average arrival size

    }
}