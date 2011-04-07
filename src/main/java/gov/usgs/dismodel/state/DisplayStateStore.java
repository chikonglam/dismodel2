package gov.usgs.dismodel.state;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

import java.awt.Color;

public class DisplayStateStore {
    // constants
    public static final double DEFAULT_ZOOM_FACTOR = 1.2d;

    // station vars
    private Color stationColor = Color.BLUE;

    // geo vars
    LatLon centerOfMap = new LatLon(Angle.fromDegrees(37.4125), Angle.fromDegrees(-102.4285));

    // ENU display bounds
    private double xCenter = 0;
    private double yCenter = 0;
    private double chartSpan = 1e7d;

    // vector variables
    private Color realDisplacementVectorColor = new Color(219, 176, 4, 128);    // deep yellow
    private Color modeledDisplacementVectorColor = new Color(255, 0, 0, 128);
    private int displacementVectorScale = 20000;
    
    //source color
    private Color sourceColor = Color.RED;

    // getters and setters
    public Color getRealDisplacementVectorColor() {
        return realDisplacementVectorColor;
    }

    public void setRealDisplacementVectorColor(Color realDisplacementVectorColor) {
        this.realDisplacementVectorColor = realDisplacementVectorColor;
    }

    public Color getModeledDisplacementVectorColor() {
        return modeledDisplacementVectorColor;
    }

    public void setModeledDisplacementVectorColor(Color modeledDisplacementVectorColor) {
        this.modeledDisplacementVectorColor = modeledDisplacementVectorColor;
    }

    public int getDisplacementVectorScale() {
        return displacementVectorScale;
    }

    public void setDisplacementVectorScale(int displacementVectorScale) {
        this.displacementVectorScale = displacementVectorScale;
    }

    public double getxCenter() {
        return xCenter;
    }

    public void setxCenter(double xCenter) {
        this.xCenter = xCenter;
    }

    public double getyCenter() {
        return yCenter;
    }

    public void setyCenter(double yCenter) {
        this.yCenter = yCenter;
    }

    public double getChartSpan() {
        return chartSpan;
    }

    public void setChartSpan(double chartSpan) {
        this.chartSpan = chartSpan;
    }

    public LatLon getCenterOfMap() {
        return centerOfMap;
    }

    public void setCenterOfMap(LatLon centerOfMap) {
        this.centerOfMap = centerOfMap;
    }

    public Color getStationColor() {
        return stationColor;
    }

    public void setStationColor(Color stationColor) {
        this.stationColor = stationColor;
    }

    public Color getSourceColor() {
        return sourceColor;
    }

    public void setSourceColor(Color sourceColor) {
        this.sourceColor = sourceColor;
    }
    
    

}
