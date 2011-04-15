package gov.usgs.dismodel.calc.greens;

import gov.usgs.dismodel.calc.SolverException;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Vector-difference between two Cartesian-coordinate points.
 * 
 * We like our point (location) classes to have information about their
 * coordinate system; for instance class LocalENU contains LatLon
 * projectionTangentPoint, so a user can discover or verify the reference frame.
 * This class does not have such geodetic info, so we labeled it for use as a
 * difference between positions, for which use this could be directly compatible
 * with local or global Cartesian coordinates.
 * 
 * @author cforden
 * 
 */
@XmlRootElement
@XmlType(propOrder = { "x", "y", "z" })
public class XyzDisplacement implements Serializable {
    private static final long serialVersionUID = -1344948768430007785L;

    public static final int AXES = 3;

    private double x;
    private double y;
    private double z;

    /**
     * @param x
     *            Easting in meters.
     * @param y
     *            Northing in meters.
     * @param z
     *            Up in meters.
     */
    public XyzDisplacement(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @param xyz
     *            An array comprising easting, northing, and up, in meters.
     */
    public XyzDisplacement(double[] xyz) {
        x = xyz[0];
        y = xyz[1];
        z = xyz[2];
    }

    /** Creates a value of 0.0, 0.0, 0.0. */
    public XyzDisplacement() {
    }

    /**
     * Copy Constructor
     * 
     * @param src
     */
    public XyzDisplacement(XyzDisplacement other) {
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Pythagorean theorem.
     * 
     * @return
     */
    public double distance() {
        double squares = x * x + y * y + z * z;
        return Math.sqrt(squares);
    }
    
    public double getAxis(final int axisIndex) throws SolverException {
        if (axisIndex >= AXES || axisIndex < 0)
            throw new SolverException("XyzDisplacement.getAxis(axisIndex) " + "parameter out of bounds.");
        else if (axisIndex == 0)
            return x;
        else if (axisIndex == 1)
            return y;
        else
            return z;
    }

    public void setAxis(final int axisIndex, double d) {
        if (axisIndex >= AXES || axisIndex < 0)
            throw new SolverException("XyzDisplacement.getAxis(axisIndex) " + "parameter out of bounds.");
        else if (axisIndex == 0)
            this.x = d;
        else if (axisIndex == 1)
            this.y = d;
        else
            this.z = d;
    }

    /**
     * Adds the passed displacement, axis by axis, into this instance. This
     * method does not create a copy of this instance.
     * 
     * @param RHS
     *            Right Hand Side operand to be added.
     * @return a reference to this instance after it is incremented by the
     *         passed parameter.
     */
    public XyzDisplacement addInto(final XyzDisplacement RHS) {
        x += RHS.x;
        y += RHS.y;
        z += RHS.z;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
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
        XyzDisplacement other = (XyzDisplacement) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "XyzDisplacement [Ux=" + x + ", Uy=" + y + ", Uz=" + z + "]";
    }

    public void setToDifference(XyzDisplacement disp, XyzDisplacement dispRef) {
        for (int i = 0; i < AXES; i++)
            setAxis(i, disp.getAxis(i) - dispRef.getAxis(i));
    }

    public void setValuesTo(XyzDisplacement displacement) {
        for (int i = 0; i < AXES; i++)
            setAxis(i, displacement.getAxis(i));
    }
}
