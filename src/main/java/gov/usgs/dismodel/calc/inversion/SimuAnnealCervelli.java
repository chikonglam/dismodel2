package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.InversionListener;
import gov.usgs.dismodel.calc.SolverException;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.LocalENU;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;

import us.fed.fs.fpl.optimization.Fmin;
import us.fed.fs.fpl.optimization.Fmin_methods;

public class SimuAnnealCervelli {
    //config
    //-------
    private static final int COOLING_SCHEDULE = 4;
    private static final int ANNEALING_RUNS = 3;
    private static final int GRID_COUNT = 4;
    private static final int CRIT_TEMP_RUN_CT = 500;
    private static final double NETWON_REL_TOL = 1e-4;
    private static final int DISP_INTV = 75;
    
    
    //useful constants
    //-----------------
    private static final int DIM_CT = 3; // Must equal gov.usgs.dismodel.calc.inversion.CovarianceWeighter.STATION_AXES
    private static final int DIM_X = 0;
    private static final int DIM_Y = 1;
    private static final int DIM_Z = 2;
    private static final double OUTOFBOUND_OFFSET = 1E20d;
    private static final double OUTOFBOUND_SLOPE = 1E10d;    //this should be < OUTOFBOUND_OFFSET 
    private static final double INF_ERROR_OFFSET = 1E40d;   //this should be > OUTOFBOUND_OFFSET 
    
    //Map and vals for the optimized vars
    //-----------------------------------
    private Index2D[] optimizedMapper;
    private double[] optimizedVarInitGuess;
    
    //Map and vals for the fixed vars
    //-------------------------------
    private Index2D[] fixedMapper;
    private double[] fixedVarVals;
    
    
    //Map and vals for linear vars
    //----------------------------
    private Index2D[] linearMapper;
    private double[] linearVarVals;  //may be it's not needed
    
    //Map for non-used vars
    //---------------------
    private Index2D[] notUsedMapper; 
    
    //1D model vars
    //--------------
    private double[] upperBound1D;
    private double[] lowerBound1D;
    private double[] delta1D;
    private int numVar1D;
    private int numModeledVar1D;
    private double[] cur1DParams;

    //model vars
    //----------
    private DisplacementSolver[] modelInwithGuess;
    private DisplacementSolver[] modelInLB;
    private DisplacementSolver[] modelInUB;
    private int numModels;
    private DisplacementSolver[] fittedModelUnshifted;
    //private DisplacementSolver[] fittedModelShiftedHeight;
    private JamaMatrix gMatrix;
    
    //station vars
    //------------
    private LocalENU[] stationPositions;
    private XyzDisplacement[] stationDisplacements;
    private XyzDisplacement[] estDisplacements;
    private double[] stationDisp1D;
    private int numStation;
    
    //annealing vars
    //--------------
    int runIter = 0;
    private double degOfFreedom;
    private double[] tempScale;
    private double[] saSoln;
    private int[] count;
    private boolean cancelled = false;
    private List<Double> curParamEnergyList;
    private List<double[]> curParamModels;
    private List<Double> curRunEnergyList;
    private List<double[]> curRunModels;
    
    double[] bestModel;
    List<Double> acrossRunsEnergyList = new ArrayList<Double>();
    List<double[]> acrossRunsModelList  = new ArrayList<double[]>();
    Double curRunBestEnergy;
    double [] curRunBestModel;
    
    private double chi2;
    private CovarianceWeighter covWeighter;
    private double refH;
    private Random rng = new Random(new Date().getTime());
    
    private InversionListener listener;
    private int dispCount = 0;
    
    //multiplier for new data types
    //------------------------------
    private double [][] measuredDataMultiplier;
    private double [][] simulatedDataMultiplier;
    JamaMatrix simulatedDataMultiplierJama;
    
    //Helper utils
    //----------------
    /**
     * Convert a Standard Error Matrix stored as XyzDisplacement[] to a covariance Matrix.
     * Squares each std. deviation, then puts it on the diagonal of the covariance matrix.
     * @param stdErrMatrix
     * @return covariance matrix
     */
    public static double[][] toCovarianceMatrix(XyzDisplacement[] stdErrMatrix){
        int stationLen = stdErrMatrix.length;
        double[][] ret = new double[stationLen* DIM_CT][stationLen* DIM_CT];
        for (int iter = stationLen -1; iter >= 0; iter--){
           ret[iter*DIM_CT + DIM_X][iter*DIM_CT + DIM_X] = stdErrMatrix[iter].getX() * stdErrMatrix[iter].getX();
           ret[iter*DIM_CT + DIM_Y][iter*DIM_CT + DIM_Y] = stdErrMatrix[iter].getY() * stdErrMatrix[iter].getY();
           ret[iter*DIM_CT + DIM_Z][iter*DIM_CT + DIM_Z] = stdErrMatrix[iter].getZ() * stdErrMatrix[iter].getZ();
        }
        return ret;
    }
    
    //Constructors
    //-------------
    public SimuAnnealCervelli(final DisplacementSolver[] modelwithGuesses,
            final LocalENU[] stationPositions, final XyzDisplacement[] stationDisplacements,
            CovarianceWeighter covWeighter, final DisplacementSolver[] lowerbound,
            final DisplacementSolver[] upperbound, double refHeight, InversionListener listener) {
        super();
        this.stationDisplacements = stationDisplacements;
        double [] dispInRow = unrollDispMatix(stationDisplacements);
        covWeighter.setMeasuredDisplacementData(dispInRow);
        this.listener = listener;
        realConstructor(modelwithGuesses, stationPositions, covWeighter, lowerbound, upperbound, refHeight);      
    }

    
    public SimuAnnealCervelli(DisplacementSolver[] model,
            LocalENU[] stationPositions,
            XyzDisplacement[] stationDisplacements,
            double[][] covarianceMatrix, final DisplacementSolver[] lowerbound,
            final DisplacementSolver[] upperbound) {
        super();
        CovarianceWeighter covW = new CovarianceWeighter();
        try {
            covW.setCovarianceMatrix(covarianceMatrix);
        } catch (SolverException e) {
            // TODO Auto-generated catch block: simple error handing for now
            e.printStackTrace();
        }
        double [] dispInRow = unrollDispMatix(stationDisplacements);
        covW.setMeasuredDisplacementData(dispInRow);
        this.stationDisplacements = stationDisplacements;
        double avgHeight = AverageHeight(stationPositions);
        realConstructor(model, stationPositions, covW, lowerbound, upperbound, avgHeight);
    }
    
    
    private void realConstructor(final DisplacementSolver[] modelwithGuesses,
                final LocalENU[] stationPositions, final CovarianceWeighter covWeighter, 
                final DisplacementSolver[] lowerbound, final DisplacementSolver[] upperbound, double refHeight){
        //copy in stuff
        this.modelInwithGuess = modelwithGuesses;
        this.fittedModelUnshifted = copyOf(modelwithGuesses);
        this.stationPositions = stationPositions;
        this.modelInLB = lowerbound;
        this.modelInUB = upperbound;
        
        this.covWeighter = covWeighter;
        this.refH = refHeight;
        

        //build maps and basic vars
        List<Double> tempDoubleHolder= new ArrayList<Double>();
        fixedMapper = buildMap(tempDoubleHolder, fixedIter, true);
        fixedVarVals = unboxDouble(tempDoubleHolder);
       
        notUsedMapper = buildMap(null, notUsedIter, false);

        optimizedMapper = buildMap(tempDoubleHolder, optiIter, true);
        optimizedVarInitGuess = unboxDouble(tempDoubleHolder);
        
        linearMapper = buildMap(tempDoubleHolder, linearIter, true);
        linearVarVals = unboxDouble(tempDoubleHolder);
        
        numModeledVar1D = optimizedMapper.length;
        numVar1D = numModeledVar1D + fixedMapper.length + linearMapper.length;

        this.numStation = stationPositions.length;
        this.numModels = modelInwithGuess.length;
        this.stationDisp1D = new double[numStation * DIM_CT]; 
        this.degOfFreedom = (double)(numStation * DIM_CT - numModeledVar1D - fixedMapper.length);

        this.upperBound1D = convertModelArrayto1DwithMap(upperbound, optimizedMapper);
        this.lowerBound1D = convertModelArrayto1DwithMap(lowerbound, optimizedMapper);
        this.optimizedVarInitGuess = convertModelArrayto1DwithMap(modelInwithGuess, optimizedMapper);

        
        //Some console output for degubugging:
        System.out.println("Solving with the following models:");
        System.out.println("----------------------------------");
        System.out.println("LB:      " + Arrays.toString(modelInLB) );
        System.out.println("Starter: " + Arrays.toString(modelInwithGuess) );
        System.out.println("UB:      " + Arrays.toString(modelInUB) );
        
        //setup Annealer
        fillModelArrayWithConstantUsingMap(fittedModelUnshifted, 1.0d, linearMapper);
        fillModelArrayWithConstantUsingMap(fittedModelUnshifted, Double.NaN, notUsedMapper);
        fillModelArrayAccordingToMap(fittedModelUnshifted, fixedVarVals, fixedMapper);

    }
    
    public InversionResults calculate(){
        try {
            CervelliSimuAnnealSolver();
            return getInversionResults();
        } catch (ThreadCancelledException e){
            lumpAllResultsSoFar();
            return getInversionResults();
        }
    }
    
    
    public void setMeasuredDataMultiplier(double[][] measuredDataMultiplier) {
		if (measuredDataMultiplier != null){
			this.measuredDataMultiplier = measuredDataMultiplier;
			double [] displacements = unrollDispMatix(stationDisplacements);
			JamaMatrix dCol = JamaMatrix.FACTORY.makeColumn(displacements);
			JamaMatrix matL = JamaMatrix.FACTORY.copy(measuredDataMultiplier);
			JamaMatrix dAfter = (JamaMatrix) ((BasicMatrix)matL).multiplyRight(dCol);
			if (dAfter != null){
				double [] prodCol = getCol(dAfter, 0);
				this.covWeighter.setMeasuredDisplacementData(prodCol);
			}
		}
		
	}
    
    

	private double[] getCol(JamaMatrix dAfter, int col) {
		int numOfRow = dAfter.getRowDim();
		double [] ret = new double[numOfRow];
		for (int iter = 0; iter < numOfRow; iter++){
			ret[iter] = dAfter.get(iter, col);
		}
		return ret;
	}

	public void setSimulatedDataMultiplier(double[][] simulatedDataMultiplier) {
		this.simulatedDataMultiplier = simulatedDataMultiplier;
		this.simulatedDataMultiplierJama = JamaMatrix.FACTORY.copy(simulatedDataMultiplier);
		
	}

	private void lumpAllResultsSoFar(){
        if (curParamEnergyList != null){
            acrossRunsEnergyList.addAll(curParamEnergyList);
            acrossRunsModelList.addAll(curParamModels);
        }
        if (curRunEnergyList != null){
            acrossRunsEnergyList.addAll(curRunEnergyList);
            acrossRunsModelList.addAll(curRunModels);
        }
        
        if (curRunBestModel != null && curRunBestEnergy != null && !Double.isNaN(curRunBestEnergy)){        
            acrossRunsEnergyList.add(curRunBestEnergy);
            acrossRunsModelList.add(curRunBestModel);
        }
        
        if (acrossRunsEnergyList.size() < 1){       //just in case nothing is completed yet
            fillModelArrayAccordingToMap(fittedModelUnshifted, bestModel, optimizedMapper);
            fillLinearVars(fittedModelUnshifted);
            updateModelDisp(fittedModelUnshifted, stationDisp1D);
            double curCost = costFunction(stationDisp1D);
            acrossRunsEnergyList.add(curCost);
            acrossRunsModelList.add(cur1DParams);
        }
        
        
    }
    
    
    public InversionResults getInversionResults(){
        int minEnergyIndex = findMinEnergy(acrossRunsEnergyList);
        this.chi2 = acrossRunsEnergyList.get(minEnergyIndex);
        this.saSoln = acrossRunsModelList.get(minEnergyIndex);
        fillModelArrayAccordingToMap(fittedModelUnshifted, saSoln, optimizedMapper);
        updateModelDisp(fittedModelUnshifted, stationDisp1D);
        this.estDisplacements = toXyzDispArray(stationDisp1D);
        
        return new InversionResults(fittedModelUnshifted, chi2, estDisplacements);
    }
    
    public void cancel(){
        cancelled = true;
    }
    
    private void CervelliSimuAnnealSolver(){
        this.tempScale = linearSpace(2,3, ANNEALING_RUNS);
        this.count = new int[ANNEALING_RUNS];
        //this.SAEnergy = new double[ANNEALING_RUNS];  //need to change each element to inf?
        
        cur1DParams = new double[numModeledVar1D];
        bestModel = new double[numModeledVar1D];
        delta1D = makeDelta(upperBound1D, lowerBound1D);
        double[] rangeGrid = makeRangeGrid(GRID_COUNT);
        int rangeGridLen = rangeGrid.length;

        for (; runIter < ANNEALING_RUNS; runIter++){
            /*get the critical temp and cooling schedule
             * -----------------------------------------*/
            double curBestCost = Double.MAX_VALUE;
            double[] critTempCosts = new double[CRIT_TEMP_RUN_CT];
            curRunEnergyList = new ArrayList<Double>();
            curRunModels = new ArrayList<double[]>();
            
            int c = 0;      //cooling runs count
             
            for (int critTempRunIter = 0; critTempRunIter < CRIT_TEMP_RUN_CT; critTempRunIter++){
                regenRandParams(cur1DParams, numModeledVar1D, lowerBound1D, upperBound1D); 
                                
                double curCost = fillModelandCalcCost(cur1DParams);
                critTempCosts[critTempRunIter] = curCost;
                if (curCost < curBestCost) {
                    curBestCost = curCost;
                    bestModel = Arrays.copyOf(cur1DParams, numModeledVar1D);
                }
            }
            double tc = Math.log10(mean(critTempCosts))-tempScale[runIter];   //critTemp
            double[] coolingTemps = buildCoolingTempSchedule(tc);
            int numTemps = coolingTemps.length;
            
            /* Begin Annealing
             * -----------------*/
            for (int tempIter = 0; tempIter < numTemps; tempIter++){
                double curTemp = coolingTemps[tempIter];
                
                c++;
                
                /*Visiting each param
                 * ------------------*/
                for (int paramIter = 0; paramIter < numModeledVar1D; paramIter++){
                    curParamEnergyList = new ArrayList<Double>();
                    curParamModels = new ArrayList<double[]>();
                   
                    double[] modelMatrix = Arrays.copyOf(bestModel, numModeledVar1D); 
                    double paramOrigVal = bestModel[paramIter];
                    for (int rangeGridIter = 0; rangeGridIter < rangeGridLen; rangeGridIter++){
                        double curParam = paramOrigVal + rangeGrid[rangeGridIter] * delta1D[paramIter];
                        if (curParam > lowerBound1D[paramIter] && curParam < upperBound1D[paramIter]){
                            modelMatrix[paramIter] = curParam;
                            double curTryCost = fillModelandCalcCost(modelMatrix); 
                            curParamEnergyList.add(new Double(curTryCost));
                            curParamModels.add( Arrays.copyOf(modelMatrix, numModeledVar1D) );
                            count[runIter]++;
                        }
                    }
                   double[] dist = MakePDF(curTemp, curParamEnergyList);
                   double rand2Beat = rng.nextDouble();
                   int first2BeatIndex = findFirst2Beat(cumsum(dist),  rand2Beat);
                   if (first2BeatIndex >= 0){
                       bestModel[paramIter] = curParamModels.get(first2BeatIndex)[paramIter];       //comment when enable newton for current param below
                       Double curFirst2BeatEnergy = curParamEnergyList.get(first2BeatIndex);        //comment when enable newton for current param below
                       curRunEnergyList.add(curFirst2BeatEnergy);                                   //comment when enable newton for current param below
                       double [] curFirst2BeatModel = curParamModels.get(first2BeatIndex);          //comment when enable newton for current param below
                       curRunModels.add(curFirst2BeatModel);                                         //comment when enable newton for current param below
                       curParamEnergyList = null;       //clear after use
                       curParamModels = null;           //clear after use
                   }
                }   //paramIter
            }   //tempIter
            
            int minEnergyIndex = findMinEnergy(curRunEnergyList);
            curRunBestEnergy = curRunEnergyList.get(minEnergyIndex);
            curRunBestModel = curRunModels.get(minEnergyIndex);
            
            curRunEnergyList = null;        //clear after use
            curRunModels = null;            //clear after use
            
            //TODO: implement a better Newton method here
            for (int ParamIter = 0; ParamIter < numModeledVar1D; ParamIter++){

                double [] tempModel = Arrays.copyOf(curRunBestModel, numModeledVar1D);
                Fminfor1DArray paramMiner = new Fminfor1DArray(ParamIter, tempModel);
                double curNetwonTol = curRunBestEnergy.doubleValue()*NETWON_REL_TOL;
                double newtonedParam = Fmin.fmin(lowerBound1D[ParamIter], upperBound1D[ParamIter], paramMiner, curNetwonTol);
                tempModel[ParamIter] = newtonedParam;
                double newtonedCost = fillModelandCalcCost(tempModel);
                if ( (curRunBestEnergy.doubleValue() - newtonedCost) > curNetwonTol ) {
                    curRunBestModel = tempModel;
                    curRunBestEnergy = new Double(newtonedCost);
                    ParamIter = (ParamIter ==0)? 1 : 0;  //one change, so the other must follow
                }
            }

            acrossRunsEnergyList.add(curRunBestEnergy);                
            acrossRunsModelList.add(curRunBestModel);

        }  //runIter
        
    }
    

    
    private double fillModelandCalcCost(double[] params2fill){
        if (Thread.currentThread().isInterrupted()){
           throw new ThreadCancelledException();
        }
        fillModelArrayAccordingToMap(fittedModelUnshifted, params2fill, optimizedMapper);
        fillLinearVars(fittedModelUnshifted);
        updateModelDisp(fittedModelUnshifted, stationDisp1D);
        double curCost = costFunction(stationDisp1D);
        dispCount++;
        if ((listener != null) && ((dispCount % DISP_INTV) == 0)){
            InversionProgressStats curStats = new InversionProgressStats(curCost, runIter, fittedModelUnshifted);
            listener.update(curStats);       //TODO:  if slow, make this only give out certain ones
        }
        //System.out.println( Arrays.toString(fittedModelUnshifted) + " Chi2:" + curCost);                          //DEBUG output
        return curCost;
    }
    
    private void fillLinearVars(DisplacementSolver[] curModels){
        /* How many senses of motion a subfault can have. 
         * See STRIKE_SLIP_IDX, DIP_SLIP_IDX, OPENING_IDX */
        int numOfFills = linearMapper.length;
        if (numOfFills < 1) return;

        /* The effective number of stations can be decremented when the user
         * selects a reference station, so we update their count:         */
        //numStation = covWeighter.dSubtracted.getRowDim() / DIM_CT;		//removed this
        double [] dispWOLinVars = new double[numStation*DIM_CT];
        fillModelArrayWithConstantUsingMap(curModels, 0d, linearMapper);
        updateModelDisp(curModels, dispWOLinVars);
        
        /* gTran will be the Green's matrix transposed: */
        double [][] gTran = new double[numOfFills][numStation*DIM_CT];    //can try to avoid new 
        for (int fillIter = 0; fillIter < numOfFills; fillIter++){
            gTran[fillIter] = isolateLinDisp(curModels, linearMapper, fillIter);
        }
        double [] linVars = covWeighter.getLinVars(gTran, dispWOLinVars);
        //Can try to do boundchecks here
        fillModelArrayAccordingToMap(curModels, linVars, linearMapper);
    }
    
    
    
    private double[] isolateLinDisp(DisplacementSolver[] modelArray,
            final Index2D[] mapper, final int mapperIdx) {
        int modelIdx = mapper[mapperIdx].modelIter;
        int paramIdx = mapper[mapperIdx].paramIter;
        double[] unitDispVec = new double [numStation*DIM_CT];
        
        DisplacementSolver originalModel = modelArray[modelIdx];
        
        DisplacementSolver curModel;
        try {
            curModel = originalModel.clone();
        } catch (CloneNotSupportedException e) {
            // good enough, because it won't happen
            e.printStackTrace();
            curModel = null;
        }
        
        ArrayList<Integer> linVarList = curModel.getLinearParameterIndices();
        double [] curModeledParams = curModel.getMsp();
        int linVarListSize = linVarList.size();
        
        if (linVarListSize > 1){            //fill the other in the model 0 if there are more than 1 lin var
            for (Integer otherParamIdx : linVarList) {
                if ( Double.isNaN(notUsedIter.iterFunction(modelIdx, otherParamIdx)) ) 
                    curModeledParams[otherParamIdx] = 0d;
            }
        }
        
        curModeledParams[paramIdx] = 1d;
        XyzDisplacement curDisp;
        
        for (int stationIter = 0; stationIter < numStation; stationIter++){
            curDisp = curModel.solveDisplacement(stationPositions[stationIter]);
            unitDispVec[stationIter*DIM_CT + DIM_X] = curDisp.getX();
            unitDispVec[stationIter*DIM_CT + DIM_Y] = curDisp.getY();
            unitDispVec[stationIter*DIM_CT + DIM_Z] = curDisp.getZ();
        }
        
        return unitDispVec;
    }

    private int findMinEnergy(List<Double> energyList) {
        int listLen = energyList.size();
        double curMin = Double.MAX_VALUE;
        int curMinIter = 0;
        
        for(int iter = 0; iter < listLen; iter++){
            double curVal = energyList.get(iter).doubleValue();
            if (curVal < curMin){
                curMin = curVal;
                curMinIter = iter;
            }
        }
        return curMinIter;
    }

    private int findFirst2Beat(double[] numCandid, double num2Beat) {
        int arrayLen = numCandid.length;
        for (int iter = 0; iter< arrayLen; iter++){
            if (numCandid[iter] >= num2Beat){
                return iter;
            }
        }
        return -1;
    }

    private double[] cumsum(final double[] arrayToSum) {
        int arraylen = arrayToSum.length;
        double [] ret = new double[arraylen];
        double curSum = 0;
        for (int iter = 0; iter < arraylen; iter++){
            curSum += arrayToSum[iter];
            ret[iter] = curSum;
        }
        return ret;
    }

    private double[] MakePDF(double curTemp, List<Double> energyList) {
        for (Double double1 : energyList) {
            if (Double.isNaN(double1)) {
                System.err.println("Nan found!");
            }
        }
        return eprob(curTemp, unboxDouble(energyList));
    }
    
    

    private double[] eprob(double curTemp, double[] energyList) {
        double tooBig = 708.3964185322641;
        double [] pdf = allDiv(energyList, curTemp);
        double mpdf = max(pdf);
        
        if (mpdf > tooBig){
            double scale = mpdf / tooBig;
            pdf = allExp(allDiv(pdf, -scale));
            double mpdf2 = max(pdf);
            pdf = allDiv(pdf, mpdf2);
            pdf = allPow(pdf, scale);
        } else {
            pdf = allExp(allDiv(pdf, -1.0d));
            double mpdf2 = max(pdf);
            pdf = allDiv(pdf, mpdf2);
        }
        pdf = allDiv(pdf, sum(pdf));
        return pdf;
    }

    private double sum(double[] pdf) {
        int arrayLen = pdf.length;
        double curSum = 0;
        for (int iter = 0; iter < arrayLen; iter++){
            double curVal = pdf[iter];
            if (!Double.isNaN(curVal)){
                curSum += curVal;
            }
        }
        return curSum;
    }

    private double[] allPow(double[] pdf, double scale) {
        int arrayLen = pdf.length;
        double [] ret = new double[arrayLen];
        for (int iter = 0; iter < arrayLen; iter++){
            double curVal = pdf[iter];
            if (Double.isNaN(curVal)){
                ret[iter] = Double.NaN;
            }else {
                ret[iter] = Math.pow(curVal, scale);
            }
        }
        return ret;
    }

    private double[] allExp(double[] pdf) {
        int arrayLen = pdf.length;
        double [] ret = new double[arrayLen];
        for (int iter = 0; iter < arrayLen; iter++){
            double curVal = pdf[iter];
            if (Double.isNaN(curVal)){
                ret[iter] = Double.NaN;
            }else {
                ret[iter] = Math.exp(curVal);
            }
        }
        return ret;
    }

    private double max(double[] pdf) {
        int arrayLen = pdf.length;
        double curMax = 0;
        for (int iter = 0; iter < arrayLen; iter++){
            double curVal = pdf[iter];
            if ((!Double.isNaN(curVal)) && curVal > curMax){
                curMax = curVal;
            }
        }
        return curMax;
    }

    private double[] allDiv(double[] pdf, double scale) {
        int arrayLen = pdf.length;
        double [] ret = new double[arrayLen];
        for (int iter = 0; iter < arrayLen; iter++){
            double curVal = pdf[iter];
            if (Double.isNaN(curVal)){
                ret[iter] = Double.NaN;
            }else {
                ret[iter] = curVal / scale;
            }
        }
        return ret;
    }

    private double[] makeDelta(double[] upperBound, double[] lowerBound) {
        int arrayLen = upperBound.length;
        double [] delta = new double[arrayLen];
        for (int iter = 0; iter < arrayLen; iter++){
            delta[iter] = 0.5d * (upperBound[iter] - lowerBound[iter]);
        }
        return delta;
    }

    private double[] makeRangeGrid(int gridCount) {
        int arrayLen = gridCount * 2 + 1;
        double[] grid = new double[arrayLen];
        for (int iter = 0; iter< gridCount; iter++){
            double curVal = Math.pow(2d, -(iter+1));
            grid[iter] =  curVal;
            grid[iter+gridCount+1] = -curVal;
        }
        return grid;
    }

    private double[] buildCoolingTempSchedule(double critTemp){
        int[] coolingIterArray= new int[]{1*COOLING_SCHEDULE, 2*COOLING_SCHEDULE, 4*COOLING_SCHEDULE, 
                6*COOLING_SCHEDULE, 10*COOLING_SCHEDULE, 6*COOLING_SCHEDULE, 
                4*COOLING_SCHEDULE, 2*COOLING_SCHEDULE, 1*COOLING_SCHEDULE};
        int tempCt = sum(coolingIterArray);
        double [] tempTemplate = log10Space(critTemp+1, critTemp-1, 9);
        int outputIter = 0;
        double [] out = new double[tempCt];
        for (int iterArrayIter = 0; iterArrayIter < coolingIterArray.length; iterArrayIter++){
            for (int templateIter = 0; templateIter < coolingIterArray[iterArrayIter]; templateIter++){
                out[outputIter] = tempTemplate[iterArrayIter];
                outputIter++;
            }
        }
        return out;
    }
    


    private int sum(int[] x) {
        int ArrayLen = x.length;
        int curSum = 0;
        for (int iter = 0; iter < ArrayLen; iter++){
            curSum += x[iter];
        }
        return curSum;
    }

    private double mean(double[] x) {
        int ArrayLen = x.length;
        double curSum = 0;
        for (int iter = 0; iter < ArrayLen; iter++){
            curSum += x[iter];
        }
        return curSum / ArrayLen;
    }

    private double costFunction(double[] dispVect){
    	double [] processedDisp = dispVect;			//default
    	if (simulatedDataMultiplierJama != null){
    		JamaMatrix dCol = JamaMatrix.FACTORY.makeColumn(dispVect);
    		JamaMatrix prod = simulatedDataMultiplierJama.multiplyRight((BasicMatrix)dCol);
    		if (prod != null){
    			processedDisp = getCol(prod, 0);
    		} 
    	}
        double sse = covWeighter.calcSSE(processedDisp);
        if (Double.isNaN(sse) || Double.isInfinite(sse)){
            return INF_ERROR_OFFSET;
        } else {
            return sse; 
        }
    }

    
private double[] linearSpace(double start, double end, int numOfNums){
    double [] ret = new double [numOfNums];
    double delta = (end - start) / ((double)numOfNums - 1.0d);
    for (int iter = 0; iter < numOfNums; iter++){
        ret[iter] = start + iter * delta;
    }
    return ret;
};

private double[] log10Space(double start, double end, int numOfNums){
    double[] ret = linearSpace(start, end, numOfNums);
    for (int iter = 0; iter < numOfNums; iter++){
        ret[iter] = Math.pow(10d, ret[iter]);
    }
    return ret;
};



private void regenRandParams(double[] randArrayOut, final int numOfNums, final double[] lowerbounds, final double[] upperbounds){
    for (int iter = 0; iter < numOfNums; iter++){
        randArrayOut[iter] = rng.nextDouble() * (upperbounds[iter]- lowerbounds[iter]) + lowerbounds[iter];
    }
}





private DisplacementSolver[] MakeShiftedModelArray(DisplacementSolver[] unshiftedModels, double refHeight) {
    DisplacementSolver[] shiftedModels = copyOf(unshiftedModels);
    for (DisplacementSolver curModel : shiftedModels){
        curModel.offsetLocation(0d, 0d, refHeight);
    }
    return shiftedModels;
}

private XyzDisplacement[] toXyzDispArray(double[] dispArray1D ){
    XyzDisplacement[] retDisps = new XyzDisplacement[numStation];
    for (int iter = numStation -1; iter >=0; iter--){
        retDisps[iter] = new XyzDisplacement(dispArray1D[iter*DIM_CT + DIM_X], dispArray1D[iter*DIM_CT + DIM_Y], dispArray1D[iter*DIM_CT + DIM_Z]);
    }
    return retDisps;
}

public static double AverageHeight(LocalENU[] stationsLocations){
    int numStation = stationsLocations.length;
    double sumOfHeight = 0;
    for (int iter = 0; iter < numStation; iter++){
        sumOfHeight += stationsLocations[iter].getUp();
    }
    return (sumOfHeight/numStation);
}

/**
 * Unroll an array of displacement into a simple 1d array (x1, y1, z1, x2, y2, z2)
 * @param displacements
 * @return
 */
private double[] unrollDispMatix(XyzDisplacement[] displacements) {
    int numStat = displacements.length;
    double[] dispUnrolled = new double [numStat*DIM_CT];
    for (int iter =  numStat - 1; iter >=0; iter--){
        XyzDisplacement curDist = displacements[iter];
        dispUnrolled[iter*DIM_CT+DIM_X] = curDist.getX();
        dispUnrolled[iter*DIM_CT+DIM_Y] = curDist.getY();
        dispUnrolled[iter*DIM_CT+DIM_Z] = curDist.getZ();
    }
    return dispUnrolled;
}

/**
 * Returns a copy of the model sources
 * @param source
 * @return
 */
private DisplacementSolver[] copyOf(DisplacementSolver[] source){
    final int sourceLen = source.length;
    DisplacementSolver[] ret = new DisplacementSolver[source.length];
    for (int iter = sourceLen -1; iter >= 0; iter--){
        try {
            ret[iter] = source[iter].clone();
        } catch (Exception e){  //tossing it, because it won't happen
            e.printStackTrace();
        }
    }
    return ret;
}



private double[] convertModelArrayto1DwithMap(final DisplacementSolver[] models, final Index2D[] mapper ){
    int numOfVars = mapper.length;
    double[] ret = new double[numOfVars];
    for (int iter = 0; iter < numOfVars; iter++){
        Index2D curMapper = mapper[iter];
        ret[iter] = models[curMapper.modelIter].getMsp()[curMapper.paramIter];
    }
    return ret; 
}

private void fillModelArrayAccordingToMap(DisplacementSolver[] models, final double[] srcArray, final Index2D[] mapper){
    int arrayLen = mapper.length;
    int modelIdx, paramIdx;
    for (int iter =0; iter < arrayLen; iter++){
        modelIdx = mapper[iter].modelIter;
        paramIdx = mapper[iter].paramIter;
        models[modelIdx].getMsp()[paramIdx] = srcArray[iter]; 
    }
}

private void fillModelArrayWithConstantUsingMap(DisplacementSolver[] models, final double valueIn, final Index2D[] mapper){
    int arrayLen = mapper.length;
    int modelIdx, paramIdx;
    for (int iter =0; iter < arrayLen; iter++){
        modelIdx = mapper[iter].modelIter;
        paramIdx = mapper[iter].paramIter;
        models[modelIdx].getMsp()[paramIdx] = valueIn;
    }
}

private int[] unboxInt(List<Integer> boxed){
    int arrayLen = boxed.size();
    int[] ret = new int[arrayLen];
    for (int iter = 0; iter < arrayLen; iter++){
        ret[iter] = boxed.get(iter).intValue();
    }
    return ret;
}

private double[] unboxDouble(List<Double> boxed){
    int arrayLen = boxed.size();
    double[] ret = new double[arrayLen];
    for (int iter = 0; iter < arrayLen; iter++){
        ret[iter] = boxed.get(iter).doubleValue();
    }
    return ret;
}

//2D index class (to convert from 1D to 2d)
private static class Index2D{
    public int modelIter;
    public int paramIter;
    
    public Index2D(int modelIter, int paramIter) {
        super();
        this.modelIter = modelIter;
        this.paramIter = paramIter;
    }
    
    
}

/**
 * Update the estimated displacement vector
 * @param estimatedDisp the array to be updated
 */
private void updateModelDisp(DisplacementSolver[] modelArray, double[] estimatedDisp){
    int modelCt = modelArray.length;
    for (int stationIter = numStation -1; stationIter >= 0; stationIter--){
        double xDisp = 0d;
        double yDisp = 0d;
        double zDisp = 0d;
        for (int modelIter = modelCt - 1; modelIter >= 0; modelIter--){
            XyzDisplacement curDisp = modelArray[modelIter].solveDisplacement(stationPositions[stationIter]);
            xDisp += curDisp.getX();
            yDisp += curDisp.getY();
            zDisp += curDisp.getZ();
        }
        estimatedDisp[stationIter*DIM_CT + DIM_X] = xDisp;
        estimatedDisp[stationIter*DIM_CT + DIM_Y] = yDisp;
        estimatedDisp[stationIter*DIM_CT + DIM_Z] = zDisp;
    }
}




//class and functions to build map
//--------------------------------
private Index2D[] buildMap(List<Double> valArray, final MapBuildIterFunct iterFunc, final boolean hasValArray){
    int modelSize = modelInwithGuess.length;
    List<Index2D> indexTemp = new ArrayList<Index2D>();
    if (hasValArray){
        valArray.clear();
    }
    
    for (int iterModel = 0; iterModel < modelSize; iterModel++){
        int paramCt = modelInwithGuess[iterModel].getNumSolutionParams();
        for (int iterParam = 0; iterParam < paramCt; iterParam++){
            double returnTemp = iterFunc.iterFunction(iterModel, iterParam);
            if (! Double.isNaN(returnTemp)){
                indexTemp.add(new Index2D(iterModel, iterParam));
                if (hasValArray) {
                    valArray.add(returnTemp) ;
                }
            }
        }
    }
    
    return indexTemp.toArray(new Index2D[0]); 
}

private static abstract class MapBuildIterFunct{
    public abstract double iterFunction(final int modelIdx, final int paramIdx);
}

private MapBuildIterFunct optiIter = new MapBuildIterFunct() {
    @Override
    public double iterFunction(final int modelIdx, final int paramIdx) {
        
        if (Double.isNaN(fixedIter.iterFunction(modelIdx, paramIdx)) && (Double.isNaN(notUsedIter.iterFunction(modelIdx, paramIdx))) && (Double.isNaN(linearIter.iterFunction(modelIdx, paramIdx)))){
                return modelInwithGuess[modelIdx].getMsp()[paramIdx];
        } else {
            return Double.NaN;
        }
    }
};
private MapBuildIterFunct fixedIter = new MapBuildIterFunct() {
    @Override
    public double iterFunction(final int modelIdx, final int paramIdx) {
        double curLB = modelInLB[modelIdx].getMsp()[paramIdx];
        double curUB = modelInUB[modelIdx].getMsp()[paramIdx]; 
        if ( Double.isNaN(curLB) || Double.isNaN(curUB) ){
            return modelInwithGuess[modelIdx].getMsp()[paramIdx];
        } else if (curUB == curLB) {
            return curUB;
        } else {
            return Double.NaN;
        }
   }
};

private MapBuildIterFunct notUsedIter = new MapBuildIterFunct() {
    @Override
    public double iterFunction(final int modelIdx, final int paramIdx) {
        if (  Double.isNaN( modelInwithGuess[modelIdx].getMsp()[paramIdx])){
            return 0d;
        } else { 
            return Double.NaN;
        }
    }
};

private MapBuildIterFunct linearIter = new MapBuildIterFunct() {
    @Override
    public double iterFunction(final int modelIdx, final int paramIdx) {
        if (Double.isNaN(fixedIter.iterFunction(modelIdx, paramIdx)) && (Double.isNaN(notUsedIter.iterFunction(modelIdx, paramIdx))) ){
            List<Integer> curModelLinVar = modelInwithGuess[modelIdx].getLinearParameterIndices();
            if (curModelLinVar != null && curModelLinVar.contains(paramIdx)){
                return 1d;
            } else {
            return Double.NaN;
            }
        }else {
            return Double.NaN;
        }
        
    }
};

//helper for fmin
private class Fminfor1DArray implements Fmin_methods{
    private int index;
    private double[] allParams;
    
    public Fminfor1DArray(int index, double[] allParams) {
         this.index = index;
         this.allParams = Arrays.copyOf(allParams, numModeledVar1D);
    }

    @Override
    public double f_to_minimize(double x) {
        allParams[index] = x;
        double curCost = fillModelandCalcCost(allParams);
        return curCost;
    }
    
}

//getters and setters
//-------------------
//public DisplacementSolver[] getFittedModelUnshifted() {
//    return fittedModelUnshifted;
//}
//
//public DisplacementSolver[] getFittedModels() {
//    return fittedModelShiftedHeight;
//}
//
//public XyzDisplacement[] getStationDisplacements() {
//    return estDisplacements;
//}
//
//public double getChiSquared(){
//    return chi2;
//}
//
//public double getRefHeight(){
//    return refH;
//}
//
//public void setFittedModelUnshifted(DisplacementSolver[] fittedModelUnshifted) {
//    this.fittedModelUnshifted = fittedModelUnshifted;
//}


}