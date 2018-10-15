import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Resources {

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
            document = db.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Event_logs\\Repair\\example-logs\\teleclaims.xes"));
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