/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.SolverException;
import static gov.usgs.dismodel.calc.inversion.CovarianceWeighter.STATION_AXES;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;

/** Propagates the reference-station's error information into the covariance
 * matrix.
 * 
 * Systematic errors, common between the reference-station and the data stations,
 * can be subtracted out.  This will allow the data-weighting, error-ellipsoids,
 * and perhaps future statistical utilities, to reflect that error-cancellation.
 * This could be used to re-implement the subtraction of reference-station data
 * from the measured-data vector, as well.    
 * 
 * @author cforden
 *
 */
public class DifferenceOperator {


    /* Instance variables ******************************************************************************/
    protected JamaMatrix opMat; // Difference operator matrix
    private int stations; // number of GPS stations, including any reference station
    
    /* Methods ****************************************************************************************/

    /**
     * Constructs the matrix.
     * 
     * Assumes AXES = 3 displacement measurements (comps in the MATLAB code) per
     * GPS station.
     * 
     * @param stations
     *            The number of GPS stations, including the reference-station.
     * @param refStationIdx
     *            Index of the reference-station. This must be >= 0 or an
     *            exception will be thrown.
     */
    public DifferenceOperator(int stations, int refStationIdx) {
        this.stations = stations;
        if (refStationIdx < 0 || refStationIdx >= stations)
            throw new SolverException("Bad refStationIdx for DifferenceOperator");
        
        opMat = JamaMatrix.FACTORY.makeZero((stations - 1)*STATION_AXES, stations*STATION_AXES);
        int station = 0; 
        for (; station < refStationIdx; station++)
            makeSubmatrixIdentity(station, false);
        makeNegIdentColumn(station);
        //station++;
        for (; station < stations - 1; station++)
            makeSubmatrixIdentity(station, true);
    }

    private void makeNegIdentColumn(int refStation) {
        for (int station = 0; station < stations - 1; station++) {
            for (int axis = 0; axis < STATION_AXES; axis++) {
                final int row = axis + station * STATION_AXES;
                final int col = axis + refStation * STATION_AXES;
                opMat.set(row, col, -1.0);
            }
        }
    }

    /** Fills the diagonal of a 3x3 submatrix on the block diagonal of the main
     * matrix, with ones.  
     * 
     * @param station which group of 3 columns and rows will get a 3x3 identity
     *      matrix at their intersection.
     */
    private void makeSubmatrixIdentity(int station, boolean pastRefStationCol) {
        for (int i = 0; i < STATION_AXES; i++) {
            final int row = i + station * STATION_AXES;
            int col = row;
            if (pastRefStationCol)
                col += STATION_AXES;
            opMat.set(row, col, 1.0);
        }
    }
    
    public JamaMatrix multiplyRight(JamaMatrix rhs) {
        return opMat.multiplyRight((BasicMatrix)rhs);
    }
    
    public BasicMatrix getTranspose() {
        return opMat.transpose();
    }
    
}
