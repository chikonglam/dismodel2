package gov.usgs.dismodel.state;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.beanutils.BeanUtils;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType(propOrder = { "stationColor", "centerOfMap", "xCenter", "yCenter", "chartSpan", "realDisplacementVectorColor",
		"modeledDisplacementVectorColor", "displacementVectorScale", "sourceColor"})
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
    private boolean xyChartMode = false;

    // vector variables
    private Color realDisplacementVectorColor = new Color(219, 176, 4, 128);    // deep yellow
    private Color modeledDisplacementVectorColor = new Color(255, 100, 100, 128);
    private int displacementVectorScale = 20000;
    
    //source color
    private Color sourceColor = new Color(255, 0, 0, 128);

    // getters and setters
    @XmlJavaTypeAdapter(ColorAdapter.class)
    @XmlElement
    public Color getRealDisplacementVectorColor() {
        return realDisplacementVectorColor;
    }

    public void setRealDisplacementVectorColor(Color realDisplacementVectorColor) {
        this.realDisplacementVectorColor = realDisplacementVectorColor;
    }

    @XmlJavaTypeAdapter(ColorAdapter.class)
    @XmlElement
    public Color getModeledDisplacementVectorColor() {
        return modeledDisplacementVectorColor;
    }

    public void setModeledDisplacementVectorColor(Color modeledDisplacementVectorColor) {
        this.modeledDisplacementVectorColor = modeledDisplacementVectorColor;
    }

    @XmlElement
    public int getDisplacementVectorScale() {
        return displacementVectorScale;
    }

    public void setDisplacementVectorScale(int displacementVectorScale) {
        this.displacementVectorScale = displacementVectorScale;
    }

    @XmlElement
    public double getxCenter() {
        return xCenter;
    }

    public void setxCenter(double xCenter) {
        this.xCenter = xCenter;
    }

    @XmlElement
    public double getyCenter() {
        return yCenter;
    }

    public void setyCenter(double yCenter) {
        this.yCenter = yCenter;
    }

    @XmlElement
    public double getChartSpan() {
        return chartSpan;
    }

    public void setChartSpan(double chartSpan) {
        this.chartSpan = chartSpan;
    }

    @XmlJavaTypeAdapter(LatLonAdapter.class)
    @XmlElement
    public LatLon getCenterOfMap() {
        return centerOfMap;
    }

    public void setCenterOfMap(LatLon centerOfMap) {
        this.centerOfMap = centerOfMap;
    }

    @XmlJavaTypeAdapter(ColorAdapter.class)
    @XmlElement
    public Color getStationColor() {
        return stationColor;
    }

    public void setStationColor(Color stationColor) {
        this.stationColor = stationColor;
    }

    @XmlJavaTypeAdapter(ColorAdapter.class)
    @XmlElement
    public Color getSourceColor() {
        return sourceColor;
    }

    public void setSourceColor(Color sourceColor) {
        this.sourceColor = sourceColor;
    }

    public boolean isXyChartMode() {
        return xyChartMode;
    }

    public void setXyChartMode(boolean xyChartMode) {
        this.xyChartMode = xyChartMode;
    }
    
    //JAXB adapters
    //---------------
    public static class ColorAdapter extends XmlAdapter<String, Color> {
    	  public Color unmarshal(String s) {
    	    return Color.decode(s);
    	  }
    	  public String marshal(Color c) {
    	    return "#"+Integer.toHexString(c.getRGB());
    	  }
    }
    
    public static class LatLonAdapter extends XmlAdapter<String, LatLon>{
		@Override
		public LatLon unmarshal(String v) throws Exception {
			String[] splitStr = v.split(",");
			double latDeg = Double.parseDouble(splitStr[0]);
			double lonDeg = Double.parseDouble(splitStr[1]);
			return LatLon.fromDegrees(latDeg, lonDeg);
		}

		@Override
		public String marshal(LatLon v) throws Exception {
			double latDeg = v.getLatitude().getDegrees();
			double lonDeg = v.getLongitude().getDegrees();
			return String.format("%.13f,%.13f" ,latDeg, lonDeg);
		}
    	
    }
    
    public void replaceWith(DisplayStateStore that){
    	try {
			BeanUtils.copyProperties(this, that);
			System.out.println("display state read");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    }
    
    

}
