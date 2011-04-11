package gov.usgs.dismodel.geom;

import gov.usgs.dismodel.calc.greens.XyzDisplacement;

import java.io.Serializable;

/**
 * 
 * @author dmcmanamon
 * @deprecated use VectorXyz
 */
public class VectorENU implements java.io.Serializable{
    private static final long serialVersionUID = 8259718619091033934L;
    
    private final LocalENU start;
	private final LocalENU end;
	private final XyzDisplacement error; 
	
	public VectorENU(LocalENU start, LocalENU end, XyzDisplacement error) {
		this.start = start;
		this.end = end;
		this.error = error;
	}
	
    public VectorENU(LocalENU start, double eastMeters, double northMeters,
            double upMeters, double scaleFactor) {
        this.start = start;
		double endEast = start.getEasting() + eastMeters * scaleFactor;
		double endNorth = start.getNorthing() + northMeters * scaleFactor;
		double endUp = start.getUp() + upMeters * scaleFactor;
        this.end = new LocalENU(endEast, endNorth, endUp, start.getProjectionTangentPoint());
        this.error = null;
	}
	
    public VectorENU(LocalENU start, double xDist, double yDist, double zDist,
            XyzDisplacement error, double scaleFactor) {
        this.start = start;
	    double endEast = start.getX() + xDist * scaleFactor;
	    double endNorth = start.getY() + yDist * scaleFactor;
	    double endUp = start.getZ() + zDist * scaleFactor;
	    this.end = new LocalENU(endEast, endNorth, endUp, start.getProjectionTangentPoint());
        this.error = new XyzDisplacement(scaleFactor * error.getX(),
                scaleFactor * error.getY(), scaleFactor * error.getZ());
    }
	
	public VectorENU scale(double ratio) {
	    double xDist = this.end.getX() - this.start.getX();
	    double yDist = this.end.getY() - this.start.getY();
	    double zDist = this.end.getZ() - this.start.getZ();
		if (this.error == null){
		    return new VectorENU(this.start, xDist, yDist, zDist, ratio);
		} else {
		    return new VectorENU(this.start, xDist, yDist, zDist, this.error, ratio);
		}
	}

	public LocalENU getStart() {
		return start;
	}

	public LocalENU getEnd() {
		return end;
	}
	
	public XyzDisplacement getError() {
	    return error;
	}

	@Override
	public String toString() {
	    return "VectorENU [end=" + end + ", error=" + error + ", start=" + start + "]";
	}
	
	private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 3387393557753260493L;
       
        private final LocalENU start;
        private final LocalENU end;
        private final XyzDisplacement error; 
        
        public SerializationProxy(VectorENU vector) {
            this.start = vector.getStart();
            this.end = vector.getEnd();
            this.error = vector.getError();
        }
        
        private Object readResolve() {
            return new VectorENU(start, end, error);
        }
    }
}