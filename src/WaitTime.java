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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WaitTime {
    public static int getWaitTime() throws ParseException {

        String date = "";
        String childNodeName;
        boolean dateFound = false;
        int beginTime = 0;
        int endTime = 0;
        int beginDay = 0;
        int endDay = 0;
        int difference;
        int differenceSum = 0;
        int differenceCount = 0;
        int processTime;

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
        NodeList traceList = document.getElementsByTagName("trace");

        loop:
        for (int k = 0; k < traceList.getLength(); k++) {

            boolean beginDateFound = false;
            boolean endDateFound = false;

            Element trace = (Element) traceList.item(k);
            NodeList eventList = trace.getElementsByTagName("event");


            for (int i = 0; i < eventList.getLength(); i++) { // LOOP START


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

                                if (childList.item(j).getAttributes().getNamedItem("value").getNodeValue().equals("B register claim")) { // added first event from bussiness process model "Incoming claim". When it will be used with
                                    SimpleDateFormat str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SS:SS"); // another bussiness process, need to change the name
                                    Date data;
                                    data = str.parse(date);
                                    beginTime = data.getHours() * 60 + data.getMinutes();
                                    beginDay = data.getDay();
                                    beginDateFound = true;


                                }
                                if (childList.item(j).getAttributes().getNamedItem("value").getNodeValue().equals("determine likelihood of claim")) { // added first event from bussiness process model "Incoming claim". When it will be used with
                                    SimpleDateFormat str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SS:SS"); // another bussiness process, need to change the name
                                    Date data;
                                    data = str.parse(date);
                                    endTime = data.getHours() * 60 + data.getMinutes();
                                    endDay = data.getDay();
                                    endDateFound = true;
                                }
                                if (beginDateFound == true && endDateFound == true && beginDay == endDay) {

                                    difference = (endTime - beginTime); // getting difference between end and begin time
                                    differenceSum += difference;
                                    differenceCount++;// summing all differences
                                    continue loop;
                                }
                            }
                        }
                    }
                }
            }
        }// LOOP END

        processTime = (differenceSum / differenceCount);
        System.out.println(processTime);

        return processTime;
    }
}