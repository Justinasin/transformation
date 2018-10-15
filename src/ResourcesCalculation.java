import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;

public class ResourcesCalculation {

    public static ArrayList<ResourcesQuantity> getResourceCalculations() {

        HashSet<String> resursai = Resources.getResources();

        HashMap<String, Long> similarRes = new HashMap<>();
        Iterator<String> resIter = resursai.iterator();
        Iterator<String> resIter2 = resursai.iterator();

        int fullDistance = 0;
        int counter = 1;
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
        ArrayList<ResourcesQuantity> resQ = new ArrayList<>();
        Set<String> resourceNames = similarRes.keySet();
        for (String s : resourceNames) {
            ResourcesQuantity res = new ResourcesQuantity();
            res.resourceName = s;

            res.resourceQuantity = Long.toString(similarRes.get(s));
            resQ.add(res);
        }
        return resQ; // grazina bendrini resurso pavadinima ir jo kieki
    }
}
