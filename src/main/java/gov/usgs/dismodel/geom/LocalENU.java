package gov.usgs.dismodel.geom;

/**
 * A Transverse Mercator East-North-Up local coordinate point.
 * East, North, and Up are all in meters.
 * @author Chi Lam
 */
public class LocalENU implements java.io.Serializable {
    private static final long serialVersionUID = 1826563692359750119L;
    
    /**
	 * distance east of the Projection Tangent Point in meters, also x
	 */
	private double easting;
	/**
	 * distance north of the Projection Tangent Point in meters, also y
	 */	
	private double northing;
	/**
	 * elevation difference between this point and the Projection Tangent Point in meters, also z (positive up)
	 */
	private double up;
	/**
	 * A point on a reference ellipsoid that this local coordinate is relative to 
	 */
	private LLH projectionTangentPoint;
	
	/**
	 * Converting Constructor
	 * @param point
	 * @param projectionTangentPoint
	 */
	public LocalENU(LLH point, LLH projectionTangentPoint){
		this(Convert.toLocalENU(point, projectionTangentPoint));
	}
	
	/**
	 * Copy Constructor.
	 */
	public LocalENU(LocalENU other){
		this.easting = other.getEasting();
		this.northing = other.getNorthing();
		this.up = other.getUp();
		this.projectionTangentPoint = other.getProjectionTangentPointLLH();
	}
	
	/**
	 * @param easting in meters
	 * @param northing in meters
	 * @param up in meters
	 * @param projectionTangentPoint
	 */
	public LocalENU(double easting, double northing, double up,
			LLH projectionTangentPoint) {
		super();
		this.easting = easting;
		this.northing = northing;
		this.up = up;
		this.projectionTangentPoint = projectionTangentPoint;
	}
	
	/**
	 * @param easting in meters
	 * @param northing in meters
	 * @param up in meters
	 * @param projectionTangentPoint
	 */
	public LocalENU(double easting, double northing, double up,
            ITRFXyz projectionTangentPoint) {
        this(easting, northing, up, Convert.toLLH(projectionTangentPoint));
	}

	public double getEasting() {
		return easting;
	}

	public double getX() {
		return easting;
	}

	public void setEasting(double easting) {
		this.easting = easting;
	}

	public void setX(double x) {
		this.easting = x;
	}

	public double getNorthing() {
		return northing;
	}
	
	public double getY() {
		return northing;
	}

	public void setNorthing(double northing) {
		this.northing = northing;
	}

	public void setY(double y) {
		this.northing = y;
	}

	public double getUp() {
		return up;
	}
	
	public double getZ() {
		return up;
	}
	
	/**
	 * @return -up (depth compared to the projection tangent point)
	 */
	public double getDepth() {
		return (-up);
	}

	public void setUp(double up) {
		this.up = up;
	}
	
	public void setZ(double z) {
		this.up = z;
	}
	
	public void setDepth(double depth) {
		this.up = -depth;
	}

	public ITRFXyz getProjectionTangentPoint() {
		return getProjectionTangentPointITRFXyz();
	}
	
   public ITRFXyz getProjectionTangentPointITRFXyz() {
        return Convert.toITRFXyz(projectionTangentPoint);
   }

   public LLH getProjectionTangentPointLLH() {
        return projectionTangentPoint;
   }

	public void setProjectionTangentPoint(ITRFXyz projectionTangentPoint) {
		this.projectionTangentPoint = Convert.toLLH(projectionTangentPoint);
	}

   public void setProjectionTangentPoint(LLH projectionTangentPoint) {
       this.projectionTangentPoint = projectionTangentPoint;
   }

	
    public double distanceFrom(LocalENU that) {
        return Math.sqrt(Math.pow(this.getEasting() - that.getEasting(), 2.0)
                + Math.pow(this.getNorthing() - that.getNorthing(), 2.0)
                + Math.pow(this.getUp() - that.getUp(), 2.0));
    }
    
    public LLH toLLH(){
        return Convert.toLLH(this);
    }
    
    public ITRFXyz toITRFXyz(){
        return Convert.toITRFXyz(this);
    }
    

	@Override
	public String toString() {
		return "LocalENU [easting=" + easting + ", northing=" + northing
				+ ", up=" + up + ", projectionTangentPoint="
				+ projectionTangentPoint + "]";
	}

    /** Tells the position-value for the specified axis.
     * @param axis 0, 1, or 2, for x, y, or z (Easting, Northing, or Up)
     * @return The position along the specified axis
     */
    public double getAxis(final int axis) {
        if (axis == 0)
            return easting;
        if (axis == 1)
            return northing;
        return up;
    }
    
    /** Returns axis locations as an array. */
    public double[] copyAxisLocations() {
        double axes[] = new double[3];
        axes[0] = getEasting();
        axes[1] = getNorthing();
        axes[2] = getUp();
        return axes; 
    }

}
