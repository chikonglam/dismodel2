package gov.usgs.dismodel.geom;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * A Latitude-Longitude class to specify a location on a reference ellipsoid.
 * 
 * @author Chi Lam
 */
@XmlRootElement
@XmlType(propOrder = { "latitude", "longitude" })
@XmlSeeAlso({ LLH.class })
public class LatLon implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Angle latitude;
    protected Angle longitude;

    public LatLon() {
    }

    public LatLon(Angle latitude, Angle longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLon(LatLon point) {
        this.latitude = point.getLatitude();
        this.longitude = point.getLongitude();
    }

    public LatLon(double latitude, double longitude) {
        this(Angle.fromDeg(latitude), Angle.fromDeg(longitude));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
        result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
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
        LatLon other = (LatLon) obj;
        if (latitude == null) {
            if (other.latitude != null)
                return false;
        } else if (!latitude.equals(other.latitude))
            return false;
        if (longitude == null) {
            if (other.longitude != null)
                return false;
        } else if (!longitude.equals(other.longitude))
            return false;
        return true;
    }

    public Angle getLatitude() {
        return latitude;
    }

    public Angle getLongitude() {
        return longitude;
    }

    public void setLatitude(Angle latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Angle longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the distance from another LatLng in meters. By default, this
     * distance is calculated given the default equatorial earth radius of
     * 6378137 meters. The earth is approximated as a sphere, hence the distance
     * could be off as much as 0.3%, especially in the polar extremes.
     * 
     * @param other
     * @return
     */
    public double distanceFrom(LatLon other) {
        double lat1 = getLatitude().toRad();
        double lon1 = getLongitude().toRad();
        double lat2 = other.getLatitude().toRad();
        double lon2 = other.getLongitude().toRad();

        if (this.equals(other))
            return 0;

        // Taken from "Map Projections - A Working Manual", page 30, equation
        // 5-3a.
        // The traditional d=2*asin(a) form has been replaced with
        // d=2*atan2(sqrt(a), sqrt(1-a))
        // to reduce rounding errors with large distances.
        double a = Math.sin((lat2 - lat1) / 2.0) * Math.sin((lat2 - lat1) / 2.0) + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin((lon2 - lon1) / 2.0) * Math.sin((lon2 - lon1) / 2.0);
        double distanceRadians = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // TODO calculate according to WSG84 ellipse?
        if (Double.isNaN(distanceRadians))
            return 0.0;
        else
            return 6378137 * distanceRadians;
    }

    public LatLon add(Angle east, Angle north) {
        return new LatLon(latitude.add(north), longitude.add(east));
    }

    public LLH toLLH() {
        return Convert.toLLH(this);
    }

    public UTM toUTM() {
        return Convert.toUTM(this);
    }

    @Override
    public String toString() {
        return "LatLon [latitude=" + latitude + ", longitude=" + longitude + "]";
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = -6900624203084323928L;

        private final double lat;
        private final double lon;

        public SerializationProxy(LatLon latLon) {
            this.lat = latLon.latitude.toDeg();
            this.lon = latLon.longitude.toDeg();
        }

        private Object readResolve() {
            return new LatLon(lat, lon);
        }
    }
}
