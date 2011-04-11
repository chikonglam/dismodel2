package gov.usgs.dismodel.geom;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * An ITRF 05 Cartesian Space class
 * Convert objects to ITRFXyz class (ITRF00 G1150) to perform simulations and calculations
 * @author clam-PR
 */
public class ITRF05Xyz {
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT");
    public static final int DEFAULT_EPOCH_YEAR = 2000;
    public static final int DEFAULT_EPOCH_MONTH = 1;
    public static final int DEFAULT_EPOCH_DAY = 1;
    
    private double x;
    private double y;
    private double z;
    private GregorianCalendar epoch = new GregorianCalendar(DEFAULT_TIME_ZONE);
    
    public ITRF05Xyz(double x, double y, double z, Date epoch) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.epoch.setTimeInMillis(epoch.getTime());
    }
   
    public ITRF05Xyz(double x, double y, double z, GregorianCalendar epoch) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.epoch = epoch;
    }

    public ITRF05Xyz(double x, double y, double z, int epochYear, int epochMonth, int epochDay){
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.epoch.set(epochYear, epochMonth-1, epochDay);
    }

    public ITRF05Xyz(ITRF05Xyz that) {
        this(that.getX(), that.getY(), that.getZ(), that.getEpoch());
    }
    
    public ITRF05Xyz(double x, double y, double z){
        this(x,y,z, DEFAULT_EPOCH_YEAR, DEFAULT_EPOCH_MONTH, DEFAULT_EPOCH_DAY);
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

    public GregorianCalendar getEpoch() {
        return epoch;
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

    public void setEpoch(GregorianCalendar epoch) {
        this.epoch = epoch;
    }
    
    public ITRFXyz toITRFXyz(){
        return(Convert.toITRFXyz(this));
    }

    @Override
    public String toString() {
        return "ITRF05Xyz [epoch=" + epoch + ", x=" + x + ", y=" + y + ", z="
                + z + "]";
    }
    
            
    
    
    
}
