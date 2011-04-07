/**
 * 
 */
package gov.usgs.dismodel.calc.greens;

import gov.usgs.dismodel.geom.LocalENU;

/** Models non-uniform elasticity of rock between and near the dislocation source 
 * and the data gathering station. 
 * @author cforden
 *
 */
public interface ElasticityModel {
	/**
	 * @param positionInEarth Point whose elasticity is queried.
	 * @return Elasticity.  If positionInEarth is above the earth's surface
	 * 		(in air), then this value is NaN, allowing topography to be modeled.
	 */
	public double getElasticity(LocalENU positionInEarth);
}
