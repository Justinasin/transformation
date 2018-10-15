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
import java.text.ParseException;
import java.util.ArrayList;

public class XmlGenerator {


    public static void generateXml() throws ParseException {

        ArrayList<TransitionProbability> probs = AllProbabilities.getAllProbabilities();
        ArrayList<ResourcesQuantity> resQ = ResourcesCalculation.getResourceCalculations();
        ArrayList<ActivityProperties> properties = ActivityAttributes.generateActivityAttributes();

        String StartEventId = AllProbabilities.startEventId;

        try {

            // INICIUOJAMAS FAILO KURIMAS

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // STANDARTINIAI XML BPSimData FAILO ELEMENTAI

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


            // PropertyParameters1 elements
            Element PropertyParameters1 = doc.createElement("ns1:PropertyParameters");
            ScenarioParameters.appendChild(PropertyParameters1);

            // PRASIDEDA LOOP PASIRINKIMO MAZGU TIKIMYBEMS

            for (int i = 0; i < probs.size(); i++) {

                Element ElementParameters2 = doc.createElement("ns1:ElementParameters");
                Scenario.appendChild(ElementParameters2);

                // set attribute to ElementParameters2 elements
                Attr ElementParameters21 = doc.createAttribute("elementRef");
                ElementParameters21.setValue("Id_" + probs.get(i).transitionId);
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
                FloatingParameter11.setValue(probs.get(i).probability);
                FloatingParameter1.setAttributeNode(FloatingParameter11);

                // PropertyParameters3 elements
                Element PropertyParameters3 = doc.createElement("ns1:PropertyParameters");
                ElementParameters2.appendChild(PropertyParameters3);

            }

            // PRASIDEDA LOOP RESURSU APRASYMUI

            for (int i = 0; i < resQ.size(); i++) {

                // ElementParameters4 elements
                Element ElementParameters4 = doc.createElement("ns1:ElementParameters");
                Scenario.appendChild(ElementParameters4);

                // set attribute to ElementParameters4 elements
                Attr ElementParameters41 = doc.createAttribute("elementRef");
                ElementParameters41.setValue(resQ.get(i).resourceName);
                ElementParameters4.setAttributeNode(ElementParameters41);

                // ResourceParameters1 elements
                Element ResourceParameters1 = doc.createElement("ns1:ResourceParameters");
                ElementParameters4.appendChild(ResourceParameters1);

                // Quantity1 elements
                Element Quantity1 = doc.createElement("ns1:Quantity");
                ResourceParameters1.appendChild(Quantity1);

                // NumericParameter1 elements
                Element NumericParameter1 = doc.createElement("ns1:NumericParameter");
                Quantity1.appendChild(NumericParameter1);

                // set attribute to NumericParameter1 elements
                Attr NumericParameter11 = doc.createAttribute("value");
                NumericParameter11.setValue(resQ.get(i).resourceQuantity);
                NumericParameter1.setAttributeNode(NumericParameter11);

                // PropertyParameters5 elements
                Element PropertyParameters5 = doc.createElement("ns1:PropertyParameters");
                ElementParameters4.appendChild(PropertyParameters5);
            }

            // PRASIDEDA LOOP, KURIS SUGENERUOJA VEIKLU ID, TRUKME BEI PRIDEDA RESURSA

            for (int j = 0; j < properties.size(); j++) {
                if (properties.get(j).duration.equals("0")) {

                    // ElementParameters110 elements
                    Element ElementParameters110 = doc.createElement("ns1:ElementParameters");
                    Scenario.appendChild(ElementParameters110);

                    // set attribute to ElementParameters110 elements
                    Attr ElementParameters111 = doc.createAttribute("elementRef");
                    ElementParameters111.setValue("Id_" + properties.get(j).id);
                    ElementParameters110.setAttributeNode(ElementParameters111);

                    // PropertyParameters12 elements
                    Element PropertyParameters12 = doc.createElement("ns1:PropertyParameters");
                    ElementParameters110.appendChild(PropertyParameters12);

                }

                if (!properties.get(j).duration.equals("0")) {

                    // ElementParameters6 elements
                    Element ElementParameters6 = doc.createElement("ns1:ElementParameters");
                    Scenario.appendChild(ElementParameters6);

                    // set attribute to ElementParameters6 elements
                    Attr ElementParameters61 = doc.createAttribute("elementRef");
                    ElementParameters61.setValue("Id_" + properties.get(j).id);
                    ElementParameters6.setAttributeNode(ElementParameters61);

                    // TimeParameters1 elements
                    Element TimeParameters1 = doc.createElement("ns1:TimeParameters");
                    ElementParameters6.appendChild(TimeParameters1);

                    // ProcessingTime1 elements
                    Element ProcessingTime1 = doc.createElement("ns1:ProcessingTime");
                    TimeParameters1.appendChild(ProcessingTime1);

                    // ResultRequest1 elements
                    Element ResultRequest1 = doc.createElement("ns1:ResultRequest");
                    ResultRequest1.appendChild(doc.createTextNode("min"));
                    ProcessingTime1.appendChild(ResultRequest1);

                    // ResultRequest2 elements
                    Element ResultRequest2 = doc.createElement("ns1:ResultRequest");
                    ResultRequest2.appendChild(doc.createTextNode("max"));
                    ProcessingTime1.appendChild(ResultRequest2);

                    // ResultRequest3 elements
                    Element ResultRequest3 = doc.createElement("ns1:ResultRequest");
                    ResultRequest3.appendChild(doc.createTextNode("mean"));
                    ProcessingTime1.appendChild(ResultRequest3);

                    // ResultRequest4 elements
                    Element ResultRequest4 = doc.createElement("ns1:ResultRequest");
                    ResultRequest4.appendChild(doc.createTextNode("count"));
                    ProcessingTime1.appendChild(ResultRequest4);

                    // ResultRequest5 elements
                    Element ResultRequest5 = doc.createElement("ns1:ResultRequest");
                    ResultRequest5.appendChild(doc.createTextNode("sum"));
                    ProcessingTime1.appendChild(ResultRequest5);

                    // DurationParameter2 elements
                    Element DurationParameter2 = doc.createElement("ns1:DurationParameter");
                    ProcessingTime1.appendChild(DurationParameter2);

                    // set attribute to DurationParameter2 elements
                    Attr DurationParameter21 = doc.createAttribute("value");
                    DurationParameter21.setValue("PT" + properties.get(j).duration + "M");
                    DurationParameter2.setAttributeNode(DurationParameter21);


                    // ResourceParameters3 elements
                    Element ResourceParameters3 = doc.createElement("ns1:ResourceParameters");
                    ElementParameters6.appendChild(ResourceParameters3);

                    // Selection1 elements
                    Element Selection1 = doc.createElement("ns1:Selection");
                    ResourceParameters3.appendChild(Selection1);

                    // ResultRequest6 elements
                    Element ResultRequest6 = doc.createElement("ns1:ResultRequest");
                    ResultRequest6.appendChild(doc.createTextNode("min"));
                    Selection1.appendChild(ResultRequest6);

                    // ResultRequest7 elements
                    Element ResultRequest7 = doc.createElement("ns1:ResultRequest");
                    ResultRequest7.appendChild(doc.createTextNode("max"));
                    Selection1.appendChild(ResultRequest7);

                    // ExpressionParameter1 elements
                    Element ExpressionParameter1 = doc.createElement("ns1:ExpressionParameter");
                    Selection1.appendChild(ExpressionParameter1);

                    // set attribute to ExpressionParameter1 elements
                    Attr ExpressionParameter11 = doc.createAttribute("value");
                    ExpressionParameter11.setValue("bpsim:getResource(\"" + properties.get(j).resource + "\",1)");
                    ExpressionParameter1.setAttributeNode(ExpressionParameter11);

                    // PropertyParameters7 elements
                    Element PropertyParameters7 = doc.createElement("ns1:PropertyParameters");
                    ElementParameters6.appendChild(PropertyParameters7);


                }
            }

            // ElementParameter blokas skirtas nurodyti veiklu pradzios intervalus ir didziausia ju kieki

            Element ElementParameters7 = doc.createElement("ns1:ElementParameters");
            Scenario.appendChild(ElementParameters7);

            // set attribute to ElementParameters6 elements
            Attr ElementParameters71 = doc.createAttribute("elementRef");
            ElementParameters71.setValue("Id_" + StartEventId);
            ElementParameters7.setAttributeNode(ElementParameters71);

            Element ControlParameters2 = doc.createElement("ns1:ControlParameters");
            ElementParameters7.appendChild(ControlParameters2);

            Element InterTriggerTimer1 = doc.createElement("ns1:InterTriggerTimer");
            ControlParameters2.appendChild(InterTriggerTimer1);

            // ResultRequest7 elements
            Element ResultRequest7 = doc.createElement("ns1:ResultRequest");
            ResultRequest7.appendChild(doc.createTextNode("min"));
            InterTriggerTimer1.appendChild(ResultRequest7);

            // ResultRequest8 elements
            Element ResultRequest8 = doc.createElement("ns1:ResultRequest");
            ResultRequest8.appendChild(doc.createTextNode("max"));
            InterTriggerTimer1.appendChild(ResultRequest8);

            // ResultRequest9 elements
            Element ResultRequest9 = doc.createElement("ns1:ResultRequest");
            ResultRequest9.appendChild(doc.createTextNode("mean"));
            InterTriggerTimer1.appendChild(ResultRequest9);

            // ResultRequest10 elements
            Element ResultRequest10 = doc.createElement("ns1:ResultRequest");
            ResultRequest10.appendChild(doc.createTextNode("sum"));
            InterTriggerTimer1.appendChild(ResultRequest10);

            // NumericParameter2 elements
            Element NumericParameter2 = doc.createElement("ns1:NumericParameter");
            InterTriggerTimer1.appendChild(NumericParameter2);

            Attr NumericParameter21 = doc.createAttribute("value");
            NumericParameter21.setValue(Integer.toString(ArrivingInterval.getArrivingInterval()));
            NumericParameter2.setAttributeNode(NumericParameter21);

            Element TriggerCount1 = doc.createElement("ns1:TriggerCount");
            ControlParameters2.appendChild(TriggerCount1);

            Element TriggerCount11 = doc.createElement("ns1:ResultRequest");
            TriggerCount11.appendChild(doc.createTextNode("count"));
            TriggerCount1.appendChild(TriggerCount11);

            // NumericParameter3 elements
            Element NumericParameter3 = doc.createElement("ns1:NumericParameter");
            TriggerCount1.appendChild(NumericParameter3);

            Attr NumericParameter31 = doc.createAttribute("value");
            NumericParameter31.setValue("1104");
            NumericParameter3.setAttributeNode(NumericParameter31);

            // IRASO TURINI I XML FAILA

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\Users\\Justelio\\Desktop\\SimulationData\\BPSimData-new.xml"));

            transformer.transform(source, result);

            System.out.println("BPSimData File saved on desktop!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

}