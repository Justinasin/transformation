
public class Classification {

    public static void main(String[] args) throws Exception {

        WaitTime waitTime = new WaitTime();
        waitTime.getWaitTime();

        Resources res = new Resources();
        res.getResources();

        ResourcesCalculation resCalc = new ResourcesCalculation();
        resCalc.getResourceCalculations();

        AllProbabilities all = new AllProbabilities();
        all.getAllProbabilities();

        ActivityAttributes act = new ActivityAttributes();
        act.generateActivityAttributes();

        Duration dur = new Duration();
        dur.getDurations();

        ArrivingInterval arr = new ArrivingInterval();
        arr.getArrivingInterval();

        ProcessTime process = new ProcessTime();
        process.getProcessTime();

        XmlGenerator xml = new XmlGenerator();
        xml.generateXml();

        ParticipantFile participant = new ParticipantFile();
        participant.generateParticipantXml();

        DiagramFile diagram = new DiagramFile();
        diagram.generateDiagramXml();


    }

}










