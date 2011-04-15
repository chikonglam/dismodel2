package gov.usgs.dismodel.geom;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An angle class that can switch between radians and degrees
 * 
 * @author Chi Lam
 */
@XmlRootElement
@XmlType(propOrder = { "angleDegrees" })
public class Angle implements Serializable {
    private static final long serialVersionUID = -7045265864208986330L;

    private double angleDegrees;
    
    public Angle(){
    }

    protected Angle(double degrees) {
        this.angleDegrees = degrees;
    }

    protected Angle(double angleInRad, double angleInDeg) {
        this.angleDegrees = angleInDeg;
    }

    public double getAngleDegrees() {
        return angleDegrees;
    }

    public void setAngleDegrees(double angleDegrees) {
        this.angleDegrees = angleDegrees;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(angleDegrees);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Angle other = (Angle) obj;
        if (Double.doubleToLongBits(angleDegrees) != Double.doubleToLongBits(other.angleDegrees))
            return false;
        return true;
    }

    /**
     * Create an Angle object by specifying its value in radians
     * 
     * @param radian
     * @return An Angle object
     */
    public static Angle fromRad(double radian) {
        return (new Angle(0, Math.toDegrees(radian)));
    }

    /**
     * Create an Angle object by specifying its value in degrees
     * 
     * @param degree
     * @return An Angle object
     */
    public static Angle fromDeg(double degree) {
        return (new Angle(0, degree));
    }

    /**
     * @return the angle value in degrees
     */
    public double toDeg() {
        return this.angleDegrees;
    }

    /**
     * @return the angle value in radians
     */
    public double toRad() {
        return Math.toRadians(this.angleDegrees);
    }

    public Angle add(Angle other) {
        return new Angle(other.toDeg() + this.toDeg());
    }

    public Angle minus(Angle other) {
        return new Angle(this.toDeg() - other.toDeg());
    }

    @Override
    public String toString() {
        return "Angle [" + angleDegrees + " degs]";
    }
}
