package gov.usgs.dismodel.geom.overlays;

import gov.usgs.dismodel.geom.LLH;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author David
 */
public class Polyline {

    private List<LLH> points;

    public Polyline(List<LLH> points) {
        this.points = new ArrayList<LLH>(points);
    }

    public LLH get(int index) {
        return points.get(index);
    }

    public void deleteVertex(int index) {
        points.remove(index);
    }

    public void insertVertex(int index, LLH v) {
        points.add(index, v);
    }

    public int size() {
        return points.size();
    }

    public double getDistance() {
        double length = 0.0;
        if (points.size() == 1)
            return length;

        LLH prevPoint = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            LLH current = points.get(i);
            length += prevPoint.distanceFrom(current);
            prevPoint = current;
        }
        return length;
    }

    @Override
    public String toString(){
        return points.toString();
    }
}