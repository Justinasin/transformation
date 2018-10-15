import org.apache.commons.text.similarity.LevenshteinDistance;
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
import java.util.ArrayList;

public class ActivityAttributes {
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
            document = db.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Bizagi\\Claim\\Claim 05-18\\86a3a48a-379f-4403-8235-e2f3c2001fb7\\Diagram.xml"));
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
            document1 = db.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
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