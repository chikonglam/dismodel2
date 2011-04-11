package gov.usgs.dismodel.geom;

public class UTM {
    private char hemisphere;
    private int zone;
    private double easting;
    private double northing;

    public static Angle getCentralMeridian(int zone) {
        return Angle.fromDeg(-177+6*(zone-1));
    }
    
    public UTM(char hemisphere, int zone, double easting, double northing) {
        super();
        this.hemisphere = Character.toUpperCase(hemisphere);
        this.zone = zone;
        this.easting = easting;
        this.northing = northing;
    }
    
    /**
     * Converting constructor
     * @param point
     */
    public UTM(LatLon point){
        this(Convert.toUTM(point));
    }
    
    public UTM(UTM that){
        super();
        this.hemisphere = that.getHemisphere();
        this.zone = that.getZone();
        this.easting = that.getEasting();
        this.northing = that.getNorthing();
    }

    public char getHemisphere() {
        return hemisphere;
    }

    public int getZone() {
        return zone;
    }

    public double getEasting() {
        return easting;
    }

    public double getNorthing() {
        return northing;
    }

    public Angle getCentralMeridian() {
        return getCentralMeridian(this.zone);
    }

    public void setHemisphere(char hemisphere) {
        this.hemisphere = Character.toUpperCase(hemisphere);
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public void setEasting(double easting) {
        this.easting = easting;
    }

    public void setNorthing(double northing) {
        this.northing = northing;
    }

    public LatLon toLatLon(){
        return (Convert.toLatLon(this));
    }

    @Override
    public String toString() {
        return "UTM [hemisphere=" + hemisphere + ", zone=" + zone
                + ", easting=" + easting + ", northing=" + northing + "]";
    }
    
}
