package gov.usgs.dismodel.calc.inversion;

import java.util.List;


import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.SmoothingDialog;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.VectorXyz;

public class CrossValidator2 {
	static final int AXIS_PER_STATION = 3; 

	SimulationDataModel simModel;
	LLH origin;
	
	DisplacementSolver[] modelArray;
	LocalENU[] stationLocArray;
	XyzDisplacement[] realDispArray;
	boolean nonNegReqList;
	double mommentConList;
	DisplacementSolver[] lowerbound;
	DisplacementSolver[] upperbound;
	ConstraintType conTypeList;
	CovarianceWeighter origCov;

	
	int stationCt;
	double[] gammas;
	int numGam;
	double[] cvss;
	
	
	
	
	public CrossValidator2(	SimulationDataModel simModel, LLH origin) {
		super();
		this.simModel = simModel;
		this.origin = origin;
		
		modelArray = simModel.getSourceModels().toArray(new DisplacementSolver[0]);
		stationLocArray = simModel.getStationLocations(origin).toArray(new LocalENU[0]);
		
		List<VectorXyz> vectors = simModel.getMeasuredUnrefdDispVectors();
        realDispArray = new XyzDisplacement[vectors.size()];
        for (int i = 0; i < realDispArray.length; i++) {
        	realDispArray[i] = vectors.get(i).getDisplacement();
        }
        
        nonNegReqList = simModel.getNonNeg();
        mommentConList = simModel.getMonentConstraint();
        conTypeList = simModel.getMonentConType();
        lowerbound = simModel.getSourceLowerbound().toArray( new DisplacementSolver[0]);
        upperbound = simModel.getSourceUpperbound().toArray( new DisplacementSolver[0]);
        
        SmoothingDialog.Params smoothParams = simModel.getSmoothingParams();
        numGam = smoothParams.numGammaValues;
		gammas = linearSpace(smoothParams.minGamma, smoothParams.maxGamma, numGam);
		cvss = new double[numGam];
		stationCt = realDispArray.length;
		
		this.origCov = simModel.getCovarWeighter();
		
	}
	
	public double calculate(){
		double minCvss = Double.MAX_VALUE;
		double minCvssGam = 0;
		for (int gamIter = 0; gamIter < numGam; gamIter++){
			double curGam = gammas[gamIter];
			double curCvss = 0;
			for (int stationIter = 0; stationIter < stationCt; stationIter++){
				//shade station loc
				LocalENU[] shadedStationLoc = shadeStationLocArray(stationLocArray, stationIter);
				//shade station disp
				XyzDisplacement[]  shadedRealDisp = shadeDisplacementArray(realDispArray, stationIter);
				//shade cov
				CovarianceWeighter shadedCov = shadeCovar(origCov, stationIter);
				
				
				DistributedSlipSolver solver = new DistributedSlipSolver(
		                modelArray, shadedStationLoc, shadedRealDisp, shadedCov,
		                simModel, lowerbound,
		                upperbound, nonNegReqList,
		                mommentConList, conTypeList, true, curGam);
				
							
				InversionResults curGamStatRes = solver.calculate();
				if (curGamStatRes == null){
					curCvss = Double.MAX_VALUE;
				} else {
					curCvss += curGamStatRes.getChi2();
				}
				
				
				
			} //stationIter
		
			////DEBUG
			System.out.println("Gam=" + curGam + ", cvss=" + curCvss);
			
			if (curCvss < minCvss){
				minCvss = curCvss;
				minCvssGam = curGam;
			}
		}	//gamIter
		
		
		return minCvssGam;
	}





	
	private double[] linearSpace(double start, double end, int numOfNums){
	    double [] ret = new double [numOfNums];
	    double delta = (end - start) / ((double)numOfNums - 1.0d);
	    for (int iter = 0; iter < numOfNums; iter++){
	        ret[iter] = start + iter * delta;
	    }
	    return ret;
	};
	
	private XyzDisplacement[] shadeDisplacementArray(XyzDisplacement[] source, int index){
		if (index < 0) return source;
		int oldLength = source.length;
		int newLength =  oldLength - 1;
		int offset = 0;
		XyzDisplacement[] ret = new XyzDisplacement[newLength];
		for (int retIter = 0; retIter < newLength; retIter++ ){
			if (retIter == index) offset = 1;
			ret[retIter] = source[retIter + offset];
		}
		
		return ret;
	}
	
	private LocalENU[] shadeStationLocArray(LocalENU[] source, int index){
		if (index < 0) return source;
		int oldLength = source.length;
		int newLength =  oldLength - 1;
		int offset = 0;
		LocalENU[] ret = new LocalENU[newLength];
		for (int retIter = 0; retIter < newLength; retIter++ ){
			if (retIter == index) offset = 1;
			ret[retIter] = source[retIter + offset];
		}
		
		return ret;
	}
	
	private CovarianceWeighter shadeCovar(CovarianceWeighter source, int index){
		if (index < 0) return source;
		double [][] fullMatrix = source.getCovarianceMatrix();		
		int matrixLen = fullMatrix.length;
		int newLen = matrixLen - AXIS_PER_STATION;
		double [][] newMatrix = new double[newLen][newLen];
		
		int rowOffset = 0;
		for (int rowIter = 0; rowIter < newLen; rowIter++){
			int colOffset = 0;
			if (rowIter == index) rowOffset = AXIS_PER_STATION;
			for (int colIter = 0; colIter < newLen; colIter++){
				if (colIter == index) colOffset = AXIS_PER_STATION;
				newMatrix[rowIter][colIter] = fullMatrix[rowIter + rowOffset][colIter + colOffset];
			}
		}
		
		return new CovarianceWeighter(newMatrix);	//TODO: use clone to create (now refstation info is lost)
	}
	
	
	

}
