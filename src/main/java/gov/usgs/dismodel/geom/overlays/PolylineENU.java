package gov.usgs.dismodel.geom.overlays;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LatLon;
import gov.usgs.dismodel.geom.LocalENU;

import java.util.ArrayList;
import java.util.List;

public class PolylineENU implements java.io.Serializable {
    private static final long serialVersionUID = 1862075729754595604L;
    
    private List<LocalENU> points;

    public PolylineENU(Polyline lineIn, LatLon origin) {
        this.points = new ArrayList<LocalENU>(lineIn.size());
        LLH originLLH = new LLH(origin);
        for (int iter = 0; iter < lineIn.size(); iter++) {
            points.add(lineIn.get(iter).toLocalENU(originLLH));
        }
    }

    public LocalENU get(int index) {
        return points.get(index);
    }

    public void deleteVertex(int index) {
        points.remove(index);
    }

    public void insertVertex(int index, LocalENU v) {
        points.add(index, v);
    }

    public int size() {
        return points.size();
    }

    public double getDistance() {
        double length = 0.0;
        if (points.size() == 1)
            return length;

        LocalENU prevPoint = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            LocalENU current = points.get(i);
            length += prevPoint.distanceFrom(current);
            prevPoint = current;
        }
        return length;
    }

    @Override
    public String toString() {
        return points.toString();
    }
}