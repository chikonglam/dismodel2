/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.inversion;

/**  
 * 
 * @author cforden
 *
 */
public class CrossValResults {
    private double optGam;
    private double minCVSS;
    // TODO: private double unsmoothedCVSS;
    // TODO: private double doubleSmoothedCVSS;
    // TODO: double[] CVSS, etc.

    
    public CrossValResults(double optGam, double minCVSS) {
        super();
        this.optGam = optGam;
        this.minCVSS = minCVSS;
    }

    public CrossValResults() {
        super();
    }


    @Override
    public String toString() {
        String solnStr = "Cross Validation Results: minCVSS = " + minCVSS + 
                " at gamma = " + optGam + "\n";
        return solnStr;
    }


     
}
