package gov.usgs.dismodel.geom;

/**
 * ITRF00 global Cartesian location
 * 
 * @author Chi Lam
 */
public class ITRFXyz {
	final private double x;
	final private double y;
	final private double z;
	
	/**
	 * Converting Constructor
	 * @param point
	 */
	public ITRFXyz(LLH point){
		this(Convert.toITRFXyz(point));
	}
	
	/**
	 * Converting Constructor
	 * @param point
	 */
	public ITRFXyz(LocalENU point){
	    this(Convert.toITRFXyz(point));
	}
	
	/**
	 * Converting Constructor
	 * @param point
	 */
	public ITRFXyz(ITRF05Xyz point){
	    this(Convert.toITRFXyz(point));
	}
	
	
	/**
	 * Copy Constructor
	 * @param point
	 */
	public ITRFXyz(ITRFXyz point){
		this.x = point.getX();
		this.y = point.getY();
		this.z = point.getZ();
	}
	
	/**
	 * @param x meters
	 * @param y meters
	 * @param z meters
	 */
	public ITRFXyz(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @return x in meters
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return y in meters
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return z in meters
	 */
	public double getZ() {
		return z;
	}
	
	/**
	 * @return [x, y, z] in an array (unit: meters)
	 */
	public double[] getXYZ() {
		return new double[] {this.x, this.y, this.z};
	}
	
	public LLH toLLH(){
	    return Convert.toLLH(this);
	}
	
	public LocalENU toLocalENU(ITRFXyz projectionTangentPoint){
	    return Convert.toLocalENU(this, projectionTangentPoint);
	}

	@Override
	public String toString() {
		return "ITRFXyz [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
