import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;

public class ParticipantFile {
    public static void generateParticipantXml() throws TransformerException, ParserConfigurationException {

        ArrayList<ResourcesQuantity> resQ = ResourcesCalculation.getResourceCalculations();
        int a = 0;


        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element Participants = doc.createElement("Participants");
        doc.appendChild(Participants);

        Attr Participants1 = doc.createAttribute("xmlns:xsd");
        Participants1.setValue("http://www.w3.org/2001/XMLSchema");
        Participants.setAttributeNode(Participants1);

        Attr Participants2 = doc.createAttribute("xmlns:xsi");
        Participants2.setValue("http://www.w3.org/2001/XMLSchema-instance");
        Participants.setAttributeNode(Participants2);

        Attr Participants3 = doc.createAttribute("xmlns");
        Participants3.setValue("http://www.wfmc.org/2009/XPDL2.2");
        Participants.setAttributeNode(Participants3);


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


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("C:\\Users\\Justelio\\Desktop\\SimulationData\\Participant-new.xml"));

        transformer.transform(source, result);

        System.out.println("Participant File saved on desktop!");


    }
}