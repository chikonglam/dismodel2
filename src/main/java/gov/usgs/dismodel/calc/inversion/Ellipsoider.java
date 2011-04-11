/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.overlays.VectorXyz;

import org.ojalgo.matrix.jama.JamaMatrix;

/**
 *  Supports calculations necessary for (later) drawing 3D error-ellipsoids
 *  for one 3D vector from one (typically GPS) data-station. 
 * 
 * @author cforden
 *
 */
public class Ellipsoider {
	public static final int AXES = 3;
	/** Multiplier for 95% confidence, as per 
	 * http://www.geom.unimelb.edu.au/nicole/surveynetworks/02a/notes09_01.html */
	public static final double CONFID_95 = 2.7955; // sqrt(chi2inv(.95,3)) in MATLAB
	
	protected double[][] mat = new double[AXES][AXES];
	XyzDisplacement out = new XyzDisplacement();

	public Ellipsoider(JamaMatrix covariance, int stationIndex) {
		/* Get the blocks along the original diagonal that are each station's
		 * individual covariance matrix.	 */
		int offsetIntoBigMat = stationIndex * AXES;
		for (int subrow = 0; subrow < AXES; subrow++) {
			for (int subcol = 0; subcol < AXES; subcol++)
			mat[subrow][subcol] = covariance.doubleValue(
					offsetIntoBigMat + subrow, offsetIntoBigMat + subcol);
		}
	}

	/**
	 * @param vec An output variable that receives the error info in part of it.
	 */
	public void getErrorEllipsoid(VectorXyz vec) {
		for (int axis = 0; axis < AXES; axis++ ) {
			out.setAxis(axis, CONFID_95 * Math.sqrt(mat[axis][axis]));
		}
		vec.setError(out);
	}
	

}
