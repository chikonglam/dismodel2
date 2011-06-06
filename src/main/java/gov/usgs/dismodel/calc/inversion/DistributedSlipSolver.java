package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;

/**
 * A wrapper class to run EqualityAndBoundsSlipSolver. This wrapper generates a
 * Green's matrix from an array of sources, and fits the sources with the
 * results. This wrapper also provides multithreading capabilities down the
 * road.
 * 
 * @author clam-PR
 * 
 */
public class DistributedSlipSolver {
    // configs
    // -------

    // constants
    // --------
    protected final int DIM_CT = 3;
    protected final int DIM_X = 0;
    protected final int DIM_Y = 1;
    protected final int DIM_Z = 2;

    private static Comparator<DisplacementSolver> faultComparator = new Comparator<DisplacementSolver>() { // use
                                                                                                           // to
                                                                                                           // make
                                                                                                           // faults
                                                                                                           // in
                                                                                                           // this
                                                                                                           // order:
                                                                                                           // group
                                                                                                           // ->
                                                                                                           // easting
                                                                                                           // ->
                                                                                                           // northing
        public int compare(DisplacementSolver faultA, DisplacementSolver faultB) {
            DistributedFault faultAf = (DistributedFault) faultA;
            DistributedFault faultBf = (DistributedFault) faultB;

            int firstComp = (new Integer(faultAf.getGroup()).compareTo(faultBf.getGroup())); // first
                                                                                             // compare
                                                                                             // group
            if (firstComp != 0)
                return firstComp;

            int secondComp = Double.compare(faultAf.getXc(), faultBf.getXc()); // then
                                                                               // compare
                                                                               // easting
            if (secondComp != 0)
                return secondComp;

            return Double.compare(faultAf.getYc(), faultBf.getYc()); // then
                                                                     // compare
                                                                     // northing

        }
    };

    private static Comparator<faultWithBounds> faultNBoundComparator = new Comparator<faultWithBounds>() {
        public int compare(faultWithBounds faultA, faultWithBounds faultB) {
            return faultComparator.compare(faultA.value, faultB.value);
        }
    };

    private static class faultWithBounds {
        public DisplacementSolver value;
        public DisplacementSolver lb;
        public DisplacementSolver ub;

        public faultWithBounds(DisplacementSolver value, DisplacementSolver lb, DisplacementSolver ub) {
            super();
            this.value = value;
            this.lb = lb;
            this.ub = ub;
        }

    }

    // Vars
    // ----
    protected double[] disp1D;
    protected double[][] gMatrixSlipMajor;
    private DisplacementSolver[] originalModel;
    protected ArrayList<ArrayList<DisplacementSolver>> groupedOriginalModel;
    private DisplacementSolver[] fittedModel;
    private DisplacementSolver[] modelLB;
    private DisplacementSolver[] modelUB;
    private LocalENU[] stationPositions;
    protected int numStation;
    protected CovarianceWeighter cov;
    private SimulationDataModel simModel;
    private int segmentCt;
    //private int rowCt;
    private int colCumSum;
    private int[] subFaultColCt;
    private int[] subFaultRowCt;

    protected int numVar;

    protected int numSubFaults;
    protected int numParamPerSubFault;
    private ArrayList<Integer> linVarIndicies;
    private boolean nonNegative;
    private double targetMonent;
    private ConstraintType momentConType;
    private boolean useSmoothingOverride = false;
    protected double smoothingGamma = Double.NaN;

    private ArrayList<SlipLocation> slipLocation = new ArrayList<SlipLocation>();
    private ArrayList<ArrayList<Integer>> activeSubFaultParams;
    private double shearModulus;

    // constructors
    // ------------
    public DistributedSlipSolver(final DisplacementSolver[] modelwithGuesses, final LocalENU[] stationPositions,
            final XyzDisplacement[] stationDisplacements, CovarianceWeighter cov, SimulationDataModel simModel,
            final DisplacementSolver[] modelLB, final DisplacementSolver[] modelUB, boolean nonNegative,
            double targetMonent, ConstraintType momentConType, boolean smoothingOverride, double smoothingGamma) {
        double[] unrolledDisp1D = unrollDispMatix(stationDisplacements);
        cov.setMeasuredDisplacementData(unrolledDisp1D);
        realConstructor(modelwithGuesses, stationPositions, unrolledDisp1D, cov, simModel, modelLB, modelUB,
                nonNegative, targetMonent, momentConType, smoothingOverride, smoothingGamma);
    }

    /**
     * A builder-like static method.
     * 
     * @param simModel
     *            Should contain subfault values and constraint info from a
     *            user's previous filling of the Distributed-slip dialog box.
     * @param enuChart
     *            Just for setting an origin.
     * @return null if simModel lacked necessary info.
     */
    static public DistributedSlipSolver make(SimulationDataModel simModel) {
        DisplacementSolver[] modelArray = simModel.getSourceModels().toArray(new DisplacementSolver[0]);
        LocalENU[] stationLocArray = simModel.getStationLocations(simModel.getOrigin()).toArray(new LocalENU[0]);
        List<VectorXyz> vectors = simModel.getMeasuredDispVectors();
        XyzDisplacement[] realDispArray = new XyzDisplacement[vectors.size()];
        for (int i = 0; i < realDispArray.length; i++) {
            realDispArray[i] = vectors.get(i).getDisplacement();
        }
        boolean nonNegReq = simModel.getNonNeg();

        double mommentCon = simModel.getMonentConstraint();

        ConstraintType conType = simModel.getMonentConType();

        DisplacementSolver[] lowerbound = simModel.getSourceLowerbound().toArray(new DisplacementSolver[0]);
        DisplacementSolver[] upperbound = simModel.getSourceUpperbound().toArray(new DisplacementSolver[0]);
        return new DistributedSlipSolver(modelArray, stationLocArray, realDispArray, simModel.getCovarWeighter(),
                simModel, lowerbound, upperbound, nonNegReq, mommentCon, conType, simModel.isUseSmoothing(),
                simModel.getSmoothingGamma());
    }

    private faultWithBounds[] convertToFaultWithBounds(DisplacementSolver[] value, DisplacementSolver[] lb,
            DisplacementSolver[] ub) {
        int len = value.length;
        faultWithBounds[] out = new faultWithBounds[len];

        for (int iter = 0; iter < len; iter++) {
            out[iter] = new faultWithBounds(value[iter], lb[iter], ub[iter]);
        }

        return out;
    }

    private void sortFaultsAndBounds(DisplacementSolver[] value, DisplacementSolver[] lb, DisplacementSolver[] ub) {
        faultWithBounds[] boundedFaults = convertToFaultWithBounds(value, lb, ub);
        int len = value.length;
        Arrays.sort(boundedFaults, faultNBoundComparator);

        // now put back everything into the arrays
        for (int iter = 0; iter < len; iter++) {
            value[iter] = boundedFaults[iter].value;
            lb[iter] = boundedFaults[iter].lb;
            ub[iter] = boundedFaults[iter].ub;
        }
    }

    // external methods
    // ----------------
    public InversionResults calculate() {
        double[] slipMagitude = callSolver();
        if (slipMagitude != null) {
            fitSlipsIntoModels(fittedModel, slipMagitude, this.slipLocation);
            double[] modeledDisp1D = new double[numStation * DIM_CT];
            updateModelDisp(fittedModel, modeledDisp1D);
            double chi2 = costFunction(modeledDisp1D);
            XyzDisplacement[] modeledDisplacements = toXyzDispArray(modeledDisp1D);
            return new InversionResults(fittedModel, chi2,  modeledDisplacements);
        } else {
            return null;
        }

    }

    // internal utils
    // --------------

    private void realConstructor(final DisplacementSolver[] modelwithGuesses, final LocalENU[] stationPositions,
            final double[] stationDisp1D, CovarianceWeighter cov, final SimulationDataModel simModel,
            final DisplacementSolver[] modelLB, final DisplacementSolver[] modelUB, boolean nonNegative,
            double targetMonent, ConstraintType momentConType, boolean smoothingOverride, double smoothingGamma) {
        this.disp1D = stationDisp1D;
        for (int iter = 0; iter < modelwithGuesses.length; iter++) {
            int curGroup = ((DistributedFault) modelwithGuesses[iter]).getGroup();
            ((DistributedFault) modelLB[iter]).setGroup(curGroup);
            ((DistributedFault) modelUB[iter]).setGroup(curGroup);
        }

        this.originalModel = modelwithGuesses;
        this.modelLB = modelLB;
        this.modelUB = modelUB;
        sortFaultsAndBounds(this.originalModel, this.modelLB, this.modelUB);
        this.linVarIndicies = this.originalModel[0].getLinearParameterIndices();
        this.activeSubFaultParams = genActiveParamsArray(this.originalModel, this.modelLB, this.modelUB,
                this.linVarIndicies);
        this.groupedOriginalModel = putModelsInGroups(this.originalModel);
        this.fittedModel = copyOf(this.originalModel);
        this.simModel = simModel;
        shearModulus = simModel.getShearModulus();
        this.cov = cov;
        this.stationPositions = stationPositions;
        this.numStation = stationPositions.length;

        this.numParamPerSubFault = linVarIndicies.size();
        putInSegRowColCts(this.originalModel);

        this.nonNegative = nonNegative;
        this.targetMonent = targetMonent;
        this.momentConType = momentConType;

        this.gMatrixSlipMajor = makeSlipMajorGMatrix(originalModel, this.slipLocation);
        this.useSmoothingOverride = smoothingOverride;
        this.smoothingGamma = smoothingGamma;

    }

    private ArrayList<ArrayList<Integer>> genActiveParamsArray(DisplacementSolver[] models,
            DisplacementSolver[] modelLB, DisplacementSolver[] modelUB, ArrayList<Integer> allLinearIndices) {
        int arrayLen = models.length;
        ArrayList<ArrayList<Integer>> returnArray = new ArrayList<ArrayList<Integer>>(arrayLen);
        for (int modelIter = 0; modelIter < arrayLen; modelIter++) {
            ArrayList<Integer> curModelIdx = new ArrayList<Integer>();
            returnArray.add(curModelIdx);
            DistributedFault curModel = (DistributedFault) models[modelIter];
            DistributedFault curLB = (DistributedFault) modelLB[modelIter];
            DistributedFault curUB = (DistributedFault) modelUB[modelIter];
            double[] valMSP = curModel.getMsp();
            double[] LBMSP = curLB.getMsp();
            double[] UBMSP = curUB.getMsp();
            for (Integer slipIndex : allLinearIndices) {
                double val = valMSP[slipIndex];
                double lb = LBMSP[slipIndex];
                double ub = UBMSP[slipIndex];
                if (!isFixed(val, lb, ub)) {
                    curModelIdx.add(slipIndex);
                }
            }

        }

        return returnArray;
    }

    private static boolean isFixed(double val, double lb, double ub) {
        if (!Double.isNaN(val) && Double.isNaN(lb) && Double.isNaN(ub)) {
            return true;
        } else if (Double.compare(val, lb) == 0 && Double.compare(val, ub) == 0) {
            return true;
        } else if (!Double.isNaN(val) && Double.compare(0.0, lb) == 0 && Double.compare(0.0, ub) == 0) {
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<ArrayList<DisplacementSolver>> putModelsInGroups(DisplacementSolver[] oneDArray) {
        ArrayList<ArrayList<DisplacementSolver>> twoDFaultArray = new ArrayList<ArrayList<DisplacementSolver>>();
        int activeGroupSlot = 0;

        // put in the first segment first
        DistributedFault firstFault = (DistributedFault) oneDArray[0];
        int existingGroup = firstFault.getGroup();
        ArrayList<DisplacementSolver> firstGroup = new ArrayList<DisplacementSolver>();
        firstGroup.add(firstFault);
        twoDFaultArray.add(firstGroup);

        int oneDLen = oneDArray.length;
        for (int iter = 1; iter < oneDLen; iter++) {
            DistributedFault curFault = (DistributedFault) oneDArray[iter];
            int curGroup = curFault.getGroup();
            if (curGroup == existingGroup && curGroup != -1) { // if the same
                                                               // group => add
                                                               // to the active
                                                               // group
                twoDFaultArray.get(activeGroupSlot).add(curFault);
            } else { // if different group => add to a new group
                ArrayList<DisplacementSolver> newGroup = new ArrayList<DisplacementSolver>();
                newGroup.add(curFault);
                twoDFaultArray.add(newGroup);
                existingGroup = curGroup;
                activeGroupSlot++;
            }
        }

        return twoDFaultArray;
    }

    private void putInSegRowColCts(DisplacementSolver[] faultsIn) {
        this.segmentCt = faultsIn.length;
        int curRowCt = 0;
        int curColCt = 0;
        int rowCtCumSum = 0;
        int colCtCumSum = 0;
        numSubFaults = 0;

        subFaultColCt = new int[segmentCt];
        subFaultRowCt = new int[segmentCt];
        for (int segmentIter = 0; segmentIter < segmentCt; segmentIter++) {
            DistributedFault curSegment = (DistributedFault) faultsIn[segmentIter];
            curRowCt = curSegment.getRowCt();
            curColCt = curSegment.getColCt();
            subFaultRowCt[segmentIter] = curRowCt;
            subFaultColCt[segmentIter] = curColCt;
            rowCtCumSum += curRowCt;
            colCtCumSum += curColCt;
            numSubFaults += curRowCt * curColCt;
        }
        numVar = numSubFaults * numParamPerSubFault;
        colCumSum = colCtCumSum;
    }

    private double costFunction(double[] dispVect) {
        double sse = cov.calcSSE(dispVect);
        if (Double.isNaN(sse) || Double.isInfinite(sse)) {
            return Double.MAX_VALUE;
        } else {
            return sse;
        }
    }

    private XyzDisplacement[] toXyzDispArray(double[] dispArray1D) {
        XyzDisplacement[] retDisps = new XyzDisplacement[numStation];
        for (int iter = numStation - 1; iter >= 0; iter--) {
            retDisps[iter] = new XyzDisplacement(dispArray1D[iter * DIM_CT + DIM_X],
                    dispArray1D[iter * DIM_CT + DIM_Y], dispArray1D[iter * DIM_CT + DIM_Z]);
        }
        return retDisps;
    }

    /**
     * Update the estimated displacement vector
     * 
     * @param estimatedDisp
     *            the array to be updated
     */
    private void updateModelDisp(DisplacementSolver[] modelArray, double[] estimatedDisp) {
        int modelCt = modelArray.length;
        for (int stationIter = numStation - 1; stationIter >= 0; stationIter--) {
            double xDisp = 0d;
            double yDisp = 0d;
            double zDisp = 0d;
            for (int modelIter = modelCt - 1; modelIter >= 0; modelIter--) {
                XyzDisplacement curDisp = modelArray[modelIter].solveDisplacement(stationPositions[stationIter]);
                xDisp += curDisp.getX();
                yDisp += curDisp.getY();
                zDisp += curDisp.getZ();
            }
            estimatedDisp[stationIter * DIM_CT + DIM_X] = xDisp;
            estimatedDisp[stationIter * DIM_CT + DIM_Y] = yDisp;
            estimatedDisp[stationIter * DIM_CT + DIM_Z] = zDisp;
        }
    }



    private double[][] makeSlipMajorGMatrix(DisplacementSolver[] model, ArrayList<SlipLocation> slipLocation) {
        double[][] gTran = new double[numVar][]; // can try to avoid new

        int curVarIter = 0;

        for (int paramIter = 0; paramIter < numParamPerSubFault; paramIter++) {
            int curParam = this.linVarIndicies.get(paramIter);
            for (int segmentIter = 0; segmentIter < segmentCt; segmentIter++) {
                ArrayList<Integer> curSegActiveParams = this.activeSubFaultParams.get(segmentIter);
                if (curSegActiveParams.contains(curParam)) {
                    for (int rowIter = 0; rowIter < subFaultRowCt[segmentIter]; rowIter++) {
                        for (int colIter = 0; colIter < subFaultColCt[segmentIter]; colIter++) {
                            gTran[curVarIter] = isolateLinDisp((DistributedFault) model[segmentIter], rowIter, colIter,
                                    curParam);
                            slipLocation.add(new SlipLocation(segmentIter, rowIter, colIter, curParam));
                            curVarIter++;
                        }
                    }
                } else {
                    for (int rowIter = 0; rowIter < subFaultRowCt[segmentIter]; rowIter++) {
                        for (int colIter = 0; colIter < subFaultColCt[segmentIter]; colIter++) {
                            gTran[curVarIter] = new double[numStation * DIM_CT];
                            slipLocation.add(new SlipLocation(segmentIter, rowIter, colIter, curParam));
                            curVarIter++;
                        }
                    }
                }

            }

        }

        double[][] outG = matrixTranspose(gTran);

        return outG;

    }

    private double[] isolateLinDisp(DistributedFault curFault, int rowIndex, int colIndex, int paramIdx) {
        double[] unitDispVec = new double[numStation * DIM_CT];

        OkadaFault3 originalModel = curFault.getSubfaults()[rowIndex][colIndex];

        OkadaFault3 cursubFault;
        try {
            cursubFault = originalModel.clone();
        } catch (CloneNotSupportedException e) {
            // good enough, because it won't happen
            e.printStackTrace();
            cursubFault = null;
        }

        double[] curModeledParams = cursubFault.getMsp();

        for (Integer otherParamIdx : linVarIndicies) {
            curModeledParams[otherParamIdx] = 0d;
        }

        if (curFault.getMsp()[paramIdx] >= 0){
            curModeledParams[paramIdx] = 1d;
        } else {
            curModeledParams[paramIdx] = -1d;
        }
        XyzDisplacement curDisp;

        for (int stationIter = 0; stationIter < numStation; stationIter++) {
            curDisp = cursubFault.solveDisplacement(stationPositions[stationIter]);
            unitDispVec[stationIter * DIM_CT + DIM_X] = curDisp.getX();
            unitDispVec[stationIter * DIM_CT + DIM_Y] = curDisp.getY();
            unitDispVec[stationIter * DIM_CT + DIM_Z] = curDisp.getZ();
        }

        return unitDispVec;
    }

    /**
     * Transpose a double[row][col] matrix
     * 
     * @param matrixIn
     *            double[row][col], matrix to be transposed (the matrix won't be
     *            changed)
     * @return double[row][col], tranposed matrixIn
     */
    protected static double[][] matrixTranspose(final double[][] matrixIn) {
        final int m = matrixIn.length;
        final int n = matrixIn[0].length;
        double[][] outArray = new double[n][m];
        for (int iter1 = 0; iter1 < m; iter1++) {
            for (int iter2 = 0; iter2 < n; iter2++) {
                outArray[iter2][iter1] = matrixIn[iter1][iter2];
            }
        }
        return (outArray);
    }

    private void fitSlipsIntoModels(DisplacementSolver[] model2BFitted, final double[] slipMagitude,
            ArrayList<SlipLocation> slipLocation) {

        int allMagLen = slipMagitude.length;

        for (int fitIndex = 0; fitIndex < allMagLen; fitIndex++) {
            SlipLocation curSlip = slipLocation.get(fitIndex);
            int curSubFault = curSlip.subfaultIdx;
            int curRow = curSlip.row;
            int curCol = curSlip.col;
            int curSlipIdx = curSlip.slipIndex;
            double[] curMSP = ((DistributedFault) model2BFitted[curSubFault]).getSubfaults()[curRow][curCol].getMsp();
            if ( ((DistributedFault) model2BFitted[curSubFault]).getMsp()[curSlipIdx] >= 0){
        	curMSP[curSlipIdx] = slipMagitude[fitIndex];
            } else {
        	curMSP[curSlipIdx] = -slipMagitude[fitIndex];
            }
        }
    }

    private double[] callSolver() {

        double[] weightedD = cov.weight(disp1D);
        EqualityAndBoundsSlipSolver solver;
        JamaMatrix gDiffed = cov.autoDifferenceOutReferenceData(gMatrixSlipMajor);

        if (useSmoothingOverride) {
            JamaMatrix weighter = cov.getAutoSmoothedWeighter(numSubFaults * numParamPerSubFault, this.smoothingGamma);
            solver = getSmoothedSolver(gDiffed.toRawCopy(), weighter, weightedD);
        } else {
            JamaMatrix weighter = cov.getAutoWeighterMatrix();
            double[][] weightedGreens = weighter.multiplyRight((BasicMatrix) gDiffed).toRawCopy();
            solver = new EqualityAndBoundsSlipSolver(weightedGreens, weightedD);
        }
        applyConstraints(solver);
        return solver.solve();
    }

    private double[] calcSubFaultAreas() {
        
        int fillIter = 0;
        int numOfVar = this.slipLocation.size();
        double[] areas = new double[numOfVar];
        for (int varIter = 0; varIter < numOfVar; varIter++){
            SlipLocation curSlipLoc = this.slipLocation.get(varIter);
            int curSFIdx = curSlipLoc.subfaultIdx;
            int curRow = curSlipLoc.row;
            int curCol = curSlipLoc.col;
            //int curParamIdx = curSlipLoc.slipIndex;
            areas[varIter] = ((DistributedFault) this.originalModel[curSFIdx]).getSubfaults()[curRow][curCol].getFaultSize();
        }
        
        return areas;
    }

    /**
     * Unroll an array of displacement into a simple 1d array (x1, y1, z1, x2,
     * y2, z2)
     * 
     * @param displacements
     * @return
     */
    private double[] unrollDispMatix(XyzDisplacement[] displacements) {
        int numStat = displacements.length;
        double[] dispUnrolled = new double[numStat * DIM_CT];
        for (int iter = numStat - 1; iter >= 0; iter--) {
            XyzDisplacement curDist = displacements[iter];
            dispUnrolled[iter * DIM_CT + DIM_X] = curDist.getX();
            dispUnrolled[iter * DIM_CT + DIM_Y] = curDist.getY();
            dispUnrolled[iter * DIM_CT + DIM_Z] = curDist.getZ();
        }
        return dispUnrolled;
    }

    /**
     * Returns a copy of the model sources
     * 
     * @param source
     * @return
     */
    private DisplacementSolver[] copyOf(DisplacementSolver[] source) {
        final int sourceLen = source.length;
        DisplacementSolver[] ret = new DisplacementSolver[source.length];
        for (int iter = sourceLen - 1; iter >= 0; iter--) {
            try {
                ret[iter] = source[iter].clone();
            } catch (Exception e) { // tossing it, because it won't happen
                e.printStackTrace();
            }
        }
        return ret;
    }

    public EqualityAndBoundsSlipSolver getSmoothedSolver() {
        JamaMatrix gDiffed = cov.autoDifferenceOutReferenceData(gMatrixSlipMajor);
        double[] weightedD = cov.weight(disp1D);
        JamaMatrix weighter = cov.getAutoSmoothedWeighter(numSubFaults * numParamPerSubFault, this.smoothingGamma);
        return getSmoothedSolver(gDiffed.toRawCopy(), weighter, weightedD);
    }

    /**
     * Creates and returns a new solver, from this object's other params.
     * Creates and smoothes a Green's matrix (function) in order to do so.
     * 
     * @param weightedDisp
     * @param unweightedGreensFunct
     */
    public EqualityAndBoundsSlipSolver getSmoothedSolver(double[][] unweightedGreensFunct, JamaMatrix smoothedWeighter,
            double[] weightedDisp) {
        /*
         * Allow space to append a numVar by numVar, square submatrix for
         * smoothing, at the bottom of the Green's function matrix that we will
         * soon calculate.
         */
        int rows = this.numVar + numStation * DIM_CT;
        int cols = numVar;
        double[][] smoothedGreensMatrix = new double[rows][cols];

        double[][] unsmoothedGm = unweightedGreensFunct;
        final boolean dikeOpening = false;
        /*
         * Get the smoothing submatrix for one sense of motion (STRIKE_SLIP_IDX,
         * DIP_SLIP_IDX, OPENING_IDX)
         */
        double[][] oneMotionSmoothingRows = SmoothingLaplacian.generate(groupedOriginalModel,  //TODO: get b dikeOpening in
                dikeOpening, numSubFaults);

        /*
         * If there is to be more than one sense of motion, copy the smoothing
         * matrix to the other senses.
         */
        
        double[][] smoothingRows = convert1MotionTo3Motion(oneMotionSmoothingRows);
      //double[][] smoothingRowsB4 = convert1MotionTo3Motion(oneMotionSmoothingRows);////DEBUG remove this after stuff is done
        //double[][] smoothingRows = multiply(smoothingRowsB4, 1e6d); 	////DEBUG remove this after stuff is done

        /* Allow space for pseudodata at the bottom of the displacement vector */
        double[] smoothableData = new double[rows];

        /*
         * Now add on the smoothing equation rows to the bottom of the Green's
         * function matrix, and add zeros as pseudodata at the bottom of the
         * measured displacements
         */
        for (int row = 0; row < unsmoothedGm.length; row++)
            for (int col = 0; col < cols; col++) {
                smoothedGreensMatrix[row][col] = unsmoothedGm[row][col];
                smoothableData[row] = weightedDisp[row];
            }
        for (int row = unsmoothedGm.length; row < rows; row++)
            for (int col = 0; col < cols; col++) {
                smoothedGreensMatrix[row][col] = smoothingRows[row - unsmoothedGm.length][col];
                smoothableData[row] = 0.0;
            }

        JamaMatrix smoothedWeightedGreensMatrix = smoothedWeighter.multiplyRight((BasicMatrix) JamaMatrix.FACTORY
                .copyRaw(smoothedGreensMatrix));

        return new EqualityAndBoundsSlipSolver(smoothedWeightedGreensMatrix.toRawCopy(), smoothableData);
    }

    private double[][] multiply(double[][] smoothingRowsB4, double d) {
		int numRows = smoothingRowsB4.length;
		int numCols = smoothingRowsB4[0].length;
		
		double [][] ret = new double[numRows][numCols];
		for (int rowIter = 0; rowIter<numRows; rowIter++){
			for (int colIter = 0; colIter<numCols; colIter++){
				ret[rowIter][colIter] = smoothingRowsB4[rowIter][colIter] * d;
			}
		}
		return ret;
	}

	protected double[][] convert1MotionTo3Motion(double[][] smoothingMatrix) {
        int smoothingRowsRowCt = numSubFaults * numParamPerSubFault;
        int smoothingRowsColCt = numSubFaults * numParamPerSubFault;
        double[][] smoothingRows = new double[smoothingRowsRowCt][smoothingRowsColCt];
        for (int rowIter = 0; rowIter < numSubFaults; rowIter++) {
            for (int colIter = 0; colIter < numSubFaults; colIter++) {
                for (int diagIter = 0; diagIter < numParamPerSubFault; diagIter++) {
                    smoothingRows[diagIter * numSubFaults + rowIter][diagIter * numSubFaults + colIter] = smoothingMatrix[rowIter][colIter];
                }
            }
        }
        return smoothingRows;
    }


    protected ConstrainedLinearLeastSquaresSolver applyConstraints(EqualityAndBoundsSlipSolver solver) {
        boolean mustSetArea = false;
        if (this.nonNegative) {
            solver.setAllBlksNonNeg();
            mustSetArea = true;
        }
        
        if (!Double.isNaN(targetMonent)) {
            switch (this.momentConType) {
            case LESS_THAN_OR_EQUAL:
                solver.setMomentUpperBound(this.targetMonent, shearModulus);
                break;
            case EQUAL:
                solver.setMoment(this.targetMonent, shearModulus);
                break;
            case GREATER_THAN_OR_EQUAL:
                solver.setMomentLowerBound(this.targetMonent, shearModulus);
                break;
            }
            mustSetArea = true;
        }
        
        
        int fillIter = 0;
        int numOfVar = this.slipLocation.size();
        double[] areas = new double[numOfVar];
        for (int varIter = 0; varIter < numOfVar; varIter++){
            SlipLocation curSlipLoc = this.slipLocation.get(varIter);
            int curSFIdx = curSlipLoc.subfaultIdx;
            int curRow = curSlipLoc.row;
            int curCol = curSlipLoc.col;
            int curParamIdx = curSlipLoc.slipIndex;
            OkadaFault3 curLB = ((DistributedFault) modelLB[curSFIdx]).getSubfaults()[curRow][curCol];
            OkadaFault3 curUB = ((DistributedFault) modelUB[curSFIdx]).getSubfaults()[curRow][curCol];
            double lbVal = curLB.getMsp()[curParamIdx];
            double ubVal = curUB.getMsp()[curParamIdx];
            
            
            if (!Double.isInfinite(lbVal)){             //TODO: implement better bounding system (at least the same as the other ones)
                solver.setLowerBoundForABlockSlip(varIter, lbVal);
                mustSetArea = true;
            }
            
            if (!Double.isInfinite(ubVal)){             //TODO: implement better bounding system (at least the same as the other ones)
                solver.setUpperBoundForABlockSlip(varIter, ubVal);
                mustSetArea = true;
            }
            
            
            
        }

        if (mustSetArea) {
            double[] subFaultAreas = calcSubFaultAreas();
            solver.setSubfaultAreas(subFaultAreas);
            // set area
        }
        return solver;
    } // applyConstraints()

    private static class SlipLocation {
        public int subfaultIdx;
        public int row;
        public int col;
        public int slipIndex;

        public SlipLocation(int subfaultIdx, int row, int col, int slipIndex) {
            super();
            this.subfaultIdx = subfaultIdx;
            this.row = row;
            this.col = col;
            this.slipIndex = slipIndex;
        }

        public SlipLocation() {
            this(-1, -1, -1, -1);
        }

    }

}
