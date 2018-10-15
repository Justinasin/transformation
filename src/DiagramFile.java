import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DiagramFile {

    public static void generateDiagramXml() throws IOException, SAXException, ParserConfigurationException, TransformerException {
        ArrayList<ResourcesQuantity> resQ = ResourcesCalculation.getResourceCalculations();
        int a = 0;


        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new File("C:\\VGTU\\Magistras ISIfm-16\\MAGISTRINIS DARBAS\\III dalis\\Bizagi\\Claim\\Claim 05-18\\86a3a48a-379f-4403-8235-e2f3c2001fb7\\Diagram.xml"));

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