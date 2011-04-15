package gov.usgs.dismodel.geom;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A location specified in Latitude-Longitude-Height (WGS84-G1150)
 * 
 * @author David McManammon
 * @author Chi Lam
 * 
 */

@XmlRootElement
@XmlType(propOrder = { "height" })
public class LLH extends LatLon implements Serializable {
    private static final long serialVersionUID = -6603667420784852113L;

    // height in meters from ellipse surface
    private double height;

    public LLH() {
        super();
    }


    public LLH(LLH point) {
        super(point.getLatitude(), point.getLongitude());
        this.height = point.getHeight();
    }

    /**
     * Upcast LatLon to LLH by setting height to 0
     * 
     * @param point
     */
    public LLH(LatLon point) {
        super(point);
        this.height = 0;
    }

    /**
     * @param latitude
     *            Angle
     * @param longitude
     *            Angle
     * @param height
     *            meter above the WGS84-G1150 ellipsoid
     */
    public LLH(Angle latitude, Angle longitude, double height) {
        super(latitude, longitude);
        this.height = height;
    }

    public LLH(double latitude, double longitude, double height) {
        super(latitude, longitude);
        this.height = height;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LLH other = (LLH) obj;
        if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
            return false;
        return true;
    }

    public double getHeight() {
        return height;
    }
    

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "LLH [height=" + height + ", toString()=" + super.toString() + "]";
    }

    /**
     * @param origin
     * @return
     */
    public LocalENU toLocalENU(LLH origin) {
        return Convert.toLocalENU(this, origin);
    }

    public ITRFXyz toITRFXyz() {
        return Convert.toITRFXyz(this);
    }
}
