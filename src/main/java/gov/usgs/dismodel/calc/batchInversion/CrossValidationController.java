package gov.usgs.dismodel.calc.batchInversion;

import javax.swing.JFrame;

import gov.usgs.dismodel.CrossValidationProgressDlg;
import gov.usgs.dismodel.DisModel;
import gov.usgs.dismodel.EnuViewerJzy;
import gov.usgs.dismodel.state.SimulationDataModel;
import gov.usgs.dismodel.SmoothingDialog;
import gov.usgs.dismodel.DisModel.AppFrame;
import gov.usgs.dismodel.calc.SolverException;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.calc.inversion.ConstrainedLinearLeastSquaresSolver;
import gov.usgs.dismodel.calc.inversion.CrossVal;
import gov.usgs.dismodel.calc.inversion.CrossValResults;
import gov.usgs.dismodel.calc.inversion.DistributedSlipSolver;

public class CrossValidationController {

    private double[] WD; /* Weighted, measured displacements */
    private double[][] WG; /* Green's function */
    private CrossVal cv; /* The numerical processor */
    private double[] gam; /*
                           * The values of gamma (inverse smoothness weighting)
                           * to use
                           */
    private CrossValidationProgressDlg progDlg = null;

    public CrossValidationController() {
    }

    public CrossValidationController(CrossValidationProgressDlg progDlg) {
        this.progDlg = progDlg;
    }

    public void crossValidate(JFrame frame, ConstrainedLinearLeastSquaresSolver solver, SimulationDataModel simModel) {
        SmoothingDialog.Params cvParams = simModel.getSmoothingParams();
        gam = new double[cvParams.numGammaValues];

        double incr = (cvParams.maxGamma - cvParams.minGamma) / (cvParams.numGammaValues);
        gam[0] = cvParams.minGamma;
        for (int gamIdx = 1; gamIdx < cvParams.numGammaValues; gamIdx++)
            gam[gamIdx] = incr * gamIdx + gam[0];

        WD = solver.getDisplacements();
        WG = solver.getGreens();
        cv = CrossVal.makeFromSimpleParams(gam, getNumStations(), WD, WG, solver, progDlg);
    }

    /**
     * Assumes the bottom square submatrix of greensFunct, is S, a smoothing
     * matrix!
     * 
     * <p>
     * That assumption implies that the Green's function must have been
     * previously set, and it already has a square submatrix at its bottom for
     * smoothing. Cross validation only has meaning if there is smoothing, so
     * here the assumption that the Green's function already has it, is
     * plausible.
     * 
     * <p>
     * To verify the validity of those assumptions, the displacements are
     * asserted to have zeros for pseudo-data. There might also be a 3-axis
     * assumption about displacement data measurements.
     */
    private int getNumStations() {
        int presumedNStat = (WG.length - WG[0].length) / XyzDisplacement.AXES;
        assertPseudodataAreZeros(presumedNStat);
        return presumedNStat;
    }

    private void assertPseudodataAreZeros(int presumedNStat) {
        int start = (presumedNStat * XyzDisplacement.AXES);
        if (start <= 0)
            throw new SolverException("Linear solver expected more " + "measured displacements");
        System.out.println("WD Vector");
        for (int row = start; row < WD.length; row++) {
            if (WD[row] != 0.0)
                throw new SolverException("Non-zero pseudodata while" + "trying to estimate the number of stations");
        }

    }

    public CrossVal getCrossVal() {
        return cv;
    }

    public double[] getGams() {
        return gam;
    }

    public CrossValResults crossValidate(AppFrame frame, SimulationDataModel simModel, EnuViewerJzy enuChart) {
        DistributedSlipSolver dss = DistributedSlipSolver.make(simModel, enuChart);
        if (dss == null)
            throw new SolverException("You must first configure a distributed-"
                    + "slip fault before cross-validating the solution.");
        ConstrainedLinearLeastSquaresSolver solver = dss.getSmoothedSolver();
        crossValidate(frame, solver, simModel);
        return getCrossValResults();
    }

    public CrossValResults getCrossValResults() {
        return new CrossValResults(cv.getOptGamma(), cv.getMinCVSS());
    }

}

