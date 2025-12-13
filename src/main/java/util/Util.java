package util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;

public class Util {
	public static List<String> parseStringToList(String input) {
        String cleaned = input.substring(1, input.length() - 1);
        String[] elements = cleaned.split(",\\s*");
        
        return new ArrayList<>(Arrays.asList(elements));
    }
	public static List<String> getRandomElementsWithReplacement(List<String> sourceList, int N) {
        List<String> resultList = new ArrayList<>();
        Random rand = new Random();
        int listSize = sourceList.size();

        if (listSize == 0) {
            return resultList;
        }
        
        for (int i = 0; i < N; i++) {
            int randomIndex = rand.nextInt(listSize);
            resultList.add(sourceList.get(randomIndex));
       }
       return resultList;     
	}
	public static int getDepartTime(SumoTraciConnection conn) throws Exception {
		int offset = 10;
		Object timeObject = conn.do_job_get(Simulation.getTime());
		int currentTime;
		if (timeObject instanceof Double) {
		    double timeDouble = ((Double) timeObject).doubleValue();
		    currentTime = (int) Math.round(timeDouble); 
		} else if (timeObject instanceof Integer) {
		    currentTime = ((Integer) timeObject).intValue();
		} else {
		    throw new IllegalArgumentException("Expected Double or Integer for time value.");
		}
		return currentTime + offset;
	}
}
