package gov.usgs.dismodel.calc.inversion;

import java.util.List;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;

import gov.usgs.dismodel.SmoothingDialog;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.state.SimulationDataModel;

public class CrossValidator3 extends DistributedSlipSolver {
    protected double[] gammas;
    protected int numGam;
    protected double[] cvss;

    @Deprecated
    public CrossValidator3(DisplacementSolver[] modelwithGuesses, LocalENU[] stationPositions,
	    XyzDisplacement[] stationDisplacements, CovarianceWeighter cov, SimulationDataModel simModel,
	    DisplacementSolver[] modelLB, DisplacementSolver[] modelUB, boolean nonNegative, double targetMonent,
	    ConstraintType momentConType, boolean smoothingOverride, double smoothingGamma) {
	super(modelwithGuesses, stationPositions, stationDisplacements, cov, simModel, modelLB, modelUB, nonNegative,
	        targetMonent, momentConType, smoothingOverride, smoothingGamma);
    }

    public CrossValidator3(SimulationDataModel simModel, LLH origin) {
	super(simModel.getSourceModels().toArray(new DisplacementSolver[0]), simModel.getStationLocations(origin)
	        .toArray(new LocalENU[0]), toDispOnlyVect(simModel.getMeasuredUnrefdDispVectors()), simModel
	        .getCovarWeighter(), simModel, simModel.getSourceLowerbound().toArray(new DisplacementSolver[0]),
	        simModel.getSourceUpperbound().toArray(new DisplacementSolver[0]), simModel.getNonNeg(), simModel
	                .getMonentConstraint(), simModel.getMonentConType(), true, simModel.getSmoothingGamma());

	SmoothingDialog.Params smoothParams = simModel.getSmoothingParams();
	numGam = smoothParams.numGammaValues;
	gammas = linearSpace(smoothParams.minGamma, smoothParams.maxGamma, numGam);
	cvss = new double[numGam];

    }

    private static XyzDisplacement[] toDispOnlyVect(List<VectorXyz> measuredUnrefdDispVectors) {
	XyzDisplacement[] realDispArray = new XyzDisplacement[measuredUnrefdDispVectors.size()];
	for (int i = 0; i < realDispArray.length; i++) {
	    realDispArray[i] = measuredUnrefdDispVectors.get(i).getDisplacement();
	}
	return realDispArray;
    }

    private double[] linearSpace(double start, double end, int numOfNums) {
	double[] ret = new double[numOfNums];
	double delta = (end - start) / ((double) numOfNums - 1.0d);
	for (int iter = 0; iter < numOfNums; iter++) {
	    ret[iter] = start + iter * delta;
	}
	return ret;
    };
    

    private double calcCVSS(double gamma) {
	this.smoothingGamma = gamma; 
	
	double[] weightedD = cov.weight(disp1D);
	EqualityAndBoundsSlipSolver solver;
	JamaMatrix gDiffed = cov.autoDifferenceOutReferenceData(gMatrixSlipMajor);

	JamaMatrix weighter = cov.getAutoSmoothedWeighter(numSubFaults * numParamPerSubFault, this.smoothingGamma);
	//solver = getSmoothedShieldingSolver(gDiffed.toRawCopy(), weighter, weightedD, maskedStationIdx);
	
	double[][] unweightedGreensFunct = gDiffed.toRawCopy();
	JamaMatrix smoothedWeighter = weighter;
	double[] weightedDisp = weightedD;

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
	double[][] oneMotionSmoothingRows = SmoothingLaplacian.generate(groupedOriginalModel, // TODO:
											      // get
											      // b
											      // dikeOpening
											      // in
	        dikeOpening, numSubFaults);

	/*
	 * If there is to be more than one sense of motion, copy the smoothing
	 * matrix to the other senses.
	 */

	double[][] smoothingRows = convert1MotionTo3Motion(oneMotionSmoothingRows);

	/* Allow space for pseudodata at the bottom of the displacement vector */
	double[] smoothableData = new double[rows];

	/*
	 * Now add on the smoothing equation rows to the bottom of the Green's
	 * function matrix, and add zeros as pseudodata at the bottom of the
	 * measured displacements
	 */
	for (int row = 0; row < unsmoothedGm.length; row++) {
	    smoothableData[row] = weightedDisp[row];
	    for (int col = 0; col < cols; col++) {
		smoothedGreensMatrix[row][col] = unsmoothedGm[row][col];

	    }
	}
	for (int row = unsmoothedGm.length; row < rows; row++) {
	    smoothableData[row] = 0.0;
	    for (int col = 0; col < cols; col++) {
		smoothedGreensMatrix[row][col] = smoothingRows[row - unsmoothedGm.length][col];
	    }

	}

	JamaMatrix smoothedWeightedGreensMatrix = smoothedWeighter.multiplyRight((BasicMatrix) JamaMatrix.FACTORY
	        .copyRaw(smoothedGreensMatrix));

	double curCVSS = 0;
	
	for (int maskedStationIdx = 0; maskedStationIdx < numStation; maskedStationIdx++){
	
        	double[][] shieldGreen = shieldStationFromGreensMatrix(smoothedWeightedGreensMatrix.toRawCopy(),
        	        maskedStationIdx);
        	double[] shieldDisp = shieldStationFromDisp(smoothableData, maskedStationIdx); 
        	solver = new EqualityAndBoundsSlipSolver(shieldGreen, shieldDisp); 
        
        	applyConstraints(solver);
        	double[] slipSoln = solver.solve();
        	
        	double [][] weightedGreenAtStation = getGreensforStation(smoothedWeightedGreensMatrix.toRawCopy(), maskedStationIdx);
        	JamaMatrix WG = JamaMatrix.FACTORY.copyRaw(weightedGreenAtStation);
        	JamaMatrix slipCol = JamaMatrix.FACTORY.makeColumn(slipSoln);
        	JamaMatrix estStationDisp = WG.multiplyRight((BasicMatrix)slipCol);
        	
        	double [] realStationDisp = extract1StationDisp(smoothableData, maskedStationIdx);
        	
        	double dx = realStationDisp[DIM_X] - estStationDisp.getNumber(DIM_X, 0);
        	double dy = realStationDisp[DIM_Y] - estStationDisp.getNumber(DIM_Y, 0);
        	double dz = realStationDisp[DIM_Z] - estStationDisp.getNumber(DIM_Z, 0);
        	
        	double res = dx*dx + dy*dy + dz*dz;
        	curCVSS += res;
	}
	return (curCVSS / numStation);
    }

    private double[] extract1StationDisp(double[] smoothableData, int maskedStationIdx) {
	double [] ret = new double[DIM_CT];
	int startIdx = maskedStationIdx * DIM_CT;
	ret[DIM_X] = smoothableData[startIdx + DIM_X];
	ret[DIM_Y] = smoothableData[startIdx + DIM_Y];
	ret[DIM_Z] = smoothableData[startIdx + DIM_Z];
	return ret;
    }

    private double[][] getGreensforStation(double[][] rawCopy, int maskedStationIdx) {
	double [][] ret = new double[DIM_CT][];
	int startIdx = maskedStationIdx * DIM_CT;
	ret[DIM_X] = rawCopy[startIdx + DIM_X];
	ret[DIM_Y] = rawCopy[startIdx + DIM_Y];
	ret[DIM_Z] = rawCopy[startIdx + DIM_Z];
	return ret;
    }

    private EqualityAndBoundsSlipSolver getSmoothedShieldingSolver(double[][] unweightedGreensFunct,
	    JamaMatrix smoothedWeighter, double[] weightedDisp, final int maskedStationIdx) {
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
	double[][] oneMotionSmoothingRows = SmoothingLaplacian.generate(groupedOriginalModel, // TODO:
											      // get
											      // b
											      // dikeOpening
											      // in
	        dikeOpening, numSubFaults);

	/*
	 * If there is to be more than one sense of motion, copy the smoothing
	 * matrix to the other senses.
	 */

	double[][] smoothingRows = convert1MotionTo3Motion(oneMotionSmoothingRows);

	/* Allow space for pseudodata at the bottom of the displacement vector */
	double[] smoothableData = new double[rows];

	/*
	 * Now add on the smoothing equation rows to the bottom of the Green's
	 * function matrix, and add zeros as pseudodata at the bottom of the
	 * measured displacements
	 */
	for (int row = 0; row < unsmoothedGm.length; row++) {
	    smoothableData[row] = weightedDisp[row];
	    for (int col = 0; col < cols; col++) {
		smoothedGreensMatrix[row][col] = unsmoothedGm[row][col];

	    }
	}
	for (int row = unsmoothedGm.length; row < rows; row++) {
	    smoothableData[row] = 0.0;
	    for (int col = 0; col < cols; col++) {
		smoothedGreensMatrix[row][col] = smoothingRows[row - unsmoothedGm.length][col];
	    }

	}

	JamaMatrix smoothedWeightedGreensMatrix = smoothedWeighter.multiplyRight((BasicMatrix) JamaMatrix.FACTORY
	        .copyRaw(smoothedGreensMatrix));

	double[][] shieldGreen = shieldStationFromGreensMatrix(smoothedWeightedGreensMatrix.toRawCopy(),
	        maskedStationIdx); // only difference from getSmoothedSolver()
	double[] shieldDisp = shieldStationFromDisp(smoothableData, maskedStationIdx); // only
										       // difference
										       // from
										       // getSmoothedSolver()

	return new EqualityAndBoundsSlipSolver(shieldGreen, shieldDisp); // only
									 // difference
									 // from
									 // getSmoothedSolver()
    }

    // TODO: test this
    private double[] shieldStationFromDisp(double[] source, int index) {
	if (index < 0)
	    return source;
	int oldLength = source.length;
	int newLength = oldLength - DIM_CT;
	int offset = 0;
	double[] ret = new double[newLength];
	for (int retIter = 0; retIter < newLength; retIter++) {
	    if (retIter == index * DIM_CT)
		offset = DIM_CT;
	    ret[retIter] = source[retIter + offset];
	}
	return ret;
    }

    // TODO: Test this
    private double[][] shieldStationFromGreensMatrix(double[][] source, int index) {
	if (index < 0)
	    return source;
	int numCol = source[0].length;
	int oldLength = source.length;
	int newLength = oldLength - DIM_CT;
	int offset = 0;
	double[][] ret = new double[newLength][numCol];
	for (int retIter = 0; retIter < newLength; retIter++) {
	    if (retIter == index)
		offset = DIM_CT;
	    ret[retIter] = source[retIter + offset];
	}
	return ret;
    }
    
    public CVResult crossValidate(){
	double minCVSS = Double.MAX_VALUE;
	double gamAtMinCVSS = Double.NaN;
	
	for(int gamIter = 0; gamIter < this.gammas.length; gamIter++){
	    double curGam = gammas[gamIter];
	    double curCVSS = calcCVSS(curGam);
	    this.cvss[gamIter] = curCVSS;
	    
	    System.out.println("gam="+ curGam + ", cvss=" + curCVSS);		////DEBUG
	    if (curCVSS < minCVSS){
		minCVSS = curCVSS;
		gamAtMinCVSS = curGam;
	    }
	}
	
	return new CVResult(minCVSS, gamAtMinCVSS);
    }



}
