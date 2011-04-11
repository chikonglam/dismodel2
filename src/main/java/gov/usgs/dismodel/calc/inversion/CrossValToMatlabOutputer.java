/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.SaveAndLoad;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Appends vectors, matrices, and comments into a .m file-- the results of some
 * previously performed cross-validation.
 * 
 * @author cforden
 *
 */
public class CrossValToMatlabOutputer {

    /* Instance variables ************************************************************************/
    private CrossValidationController cvc;
    private CrossVal cv;
    private FileWriter writer;
    
    private double CVSS[];

    
    /* Methods ***********************************************************************************/

    public CrossValToMatlabOutputer(CrossValidationController cvc) {
        this.cvc = cvc;
        cv = cvc.getCrossVal();
    }
    
    public void putOut(FileWriter writer) throws IOException {
        this.writer = writer;
        
        writer.append("\n\n"); // Comments had been written by the caller
        writeOptGamma();
        writeMinCvss();  // For gamma closest or equal to optimum
        writeCvss();
        writeGams();     // All tried, with corresponding CVSS vals in trailing comments
    }

    
    private void writeOptGamma() throws IOException {
        writer.append("optgam = "); 
        writer.append(Double.toString(cv.getOptGamma()));
        writer.append(";");
        writer.append("\n\n");
    }
    

    private void writeMinCvss() throws IOException {
        writer.append("minCVSS = "); 
        writer.append(Double.toString(cv.getMinCVSS()));
        writer.append(";");
        writer.append("\n\n");
    }
    

    private void writeCvss() throws IOException {
        writer.append("CVSS = [ "); 
        CVSS = cv.getCVSS();
        for (int i = 0; i < CVSS.length; i++) {
            writer.append(Double.toString(CVSS[i]) + ",\n");
        }
        writer.append(SaveAndLoad.END_ASSIGNMENT_STRING);
        writer.append("\n\n");
    }


    /** Append definition of gam (the array of gamma values for which
     * CVSS were computed, with corresponding CVSS vals in trailing comments */
    private void writeGams() throws IOException {
        writer.append("%\tgamma\t\t\t%\tCVSS\n"); 
        writer.append("gam = [ "); 
        double gam[] = cvc.getGams();
        for (int i = 0; i < gam.length; i++) {
            writer.append(Double.toString(gam[i]));
            writer.append(",\t%\t");
            writer.append(Double.toString(CVSS[i]) + "\n");
        }
        writer.append(SaveAndLoad.END_ASSIGNMENT_STRING);
        writer.append("\n\n");
    }

}
