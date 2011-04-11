package gov.usgs.dismodel.geom.overlays;

import gov.usgs.dismodel.geom.LLH;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PolylineParser {
	
	/**
	 * Parses a text file with lat/lng pairs, 1 per line and line breaks to indicate a new polyline.
	 * For example:
-153.34027778 59.35924946
-153.34040658 59.35972222

-153.34083333 59.36025482
-153.34088413 59.36027778
-153.34138889 59.36044028
-153.34152857 59.36083333
	 * @param filename
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static List<Polyline> parseText(File filename) throws FileNotFoundException {
		List<Polyline> result = new ArrayList<Polyline>();
		Scanner s = null;
        try {
            s = new Scanner(new BufferedReader(new FileReader(filename)));
            result = parseFile(s);
        } finally {
            if (s != null) {
                s.close();
            }
        }
        return result;
	}

	protected static List<Polyline> parseFile(Scanner s) {
		List<Polyline> result = new ArrayList<Polyline>();
		ArrayList<LLH> current = new ArrayList<LLH>();
		while (s.hasNextLine()) {
		    String line = s.nextLine();
		    LLH LatLon = getNextEntry(line);
		    if (LatLon == null) { // reached the end of 1 polyline
		    	if (current.size() > 0) {
		    		Polyline p = new Polyline(current);
		    		result.add(p);
		    		current = new ArrayList<LLH>();
		    	}
		    } else {
		    	current.add(LatLon);
		    }
		}
		if (current.size() > 0) {
			Polyline p = new Polyline(current);
			result.add(p);
		}
		return result;
	}
	
	protected static LLH getNextEntry(String line) {
		if (line.isEmpty() || !line.contains(".")) {
        	return null;
        } else {
        	String [] lonLat = line.split(" ");
        	double latitude = Double.parseDouble(lonLat[1]);
        	double longitude = Double.parseDouble(lonLat[0]);
        	return new LLH(latitude, longitude, 0.0);
        }
	}
}
