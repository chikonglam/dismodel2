package gov.usgs.dismodel.calc.inversion;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;
import org.ojalgo.optimisation.OptimisationSolver.Result;
import org.ojalgo.optimisation.quadratic.QuadraticSolver;

/**Designed to replace EqualityAndBoundsSlipSolver
 * 
 * @author Chi
 *
 */
public class ConstrainedQuadProgSolver {
    private ConstraintType mmtConType = null;
    private double moment = Double.NaN;
    private double[][] greens = null;
    private double[] disp = null;
    private double[] area = null;
    private double[] shearModulus = null;
    private double[] lb = null;
    private double[] ub = null;
    private int slipCt;
    private int dispCompCt;
    
    public ConstrainedQuadProgSolver(double[][] greensMatrix, double[] measuredDisplacements) {
	this.greens = greensMatrix;
	this.disp = measuredDisplacements;
	this.slipCt = greens[0].length;
	this.dispCompCt = disp.length;
    }
    
    public double[] solve(){
	
	JamaMatrix WG = JamaMatrix.FACTORY.copy(greens);
	JamaMatrix WGTran = WG.transpose();

	JamaMatrix GtG = WGTran.multiplyRight((BasicMatrix)WG);
	
	JamaMatrix WD = JamaMatrix.FACTORY.makeColumn(disp);
	JamaMatrix Gtd = WGTran.multiplyRight((BasicMatrix)WD);
	
	JamaMatrix A=null, b=null, Aeq=null, Beq=null, lbM=null, ubM=null;
	
	A = makeDiagMatrix(slipCt, -1);
	b = JamaMatrix.FACTORY.makeZero(slipCt, 1);
	
	//Moment con handling
	if (!Double.isNaN(moment) && mmtConType != null){
    	    double[][] areaRow = new double[][]{area};
    	    JamaMatrix areaM = JamaMatrix.FACTORY.copy(areaRow);
    	    double[][] shearRow = new double[][]{shearModulus};
    	    JamaMatrix shearM = JamaMatrix.FACTORY.copy(shearRow);
    	    JamaMatrix shearArea = areaM.multiplyElements(shearM);
    	    JamaMatrix mmtM = JamaMatrix.FACTORY.copy(new double[][]{{moment}});
    	    
    	    switch (mmtConType){
	    	case EQUAL: {
	    	    Aeq = shearArea;
	    	    Beq = mmtM;

	    	    break;
	    	} case GREATER_THAN_OR_EQUAL: {
	    	    A = A.mergeColumns( shearArea.negate() );
	    	    b = b.mergeColumns( mmtM.negate() );
	    	    
	    	    break;
	    	} case LESS_THAN_OR_EQUAL: {
	    	    A = A.mergeColumns(shearArea);
	    	    b = b.mergeColumns(mmtM);
	    	    
	    	    break;
	    	}
	    }
	}
	
	//Bounds handling
	if (lb != null) lbM = JamaMatrix.FACTORY.makeColumn(lb);
	if (ub != null) ubM = JamaMatrix.FACTORY.makeColumn(ub);
	
	
	BasicMatrix slips = solve(GtG, Gtd, A, b, Aeq, Beq, lbM, ubM);
	return mat2double(slips);
	
    }
    
    public void setMomentCon(ConstraintType mmtConType, double moment, double[] area, double[] shearModulus){
	this.mmtConType = mmtConType;
	this.moment = moment;
	this.area = area;
	this.shearModulus = shearModulus;
    }

    public void setLB(double[] lb){
	this.lb = lb;
    }
    
    public void setUB(double[] ub){
	this.ub = ub;
    }
    
    private JamaMatrix makeDiagMatrix(final int dim, final int val) {
	JamaMatrix ret = JamaMatrix.FACTORY.makeZero(dim, dim);
	for (int dimIter = 0; dimIter < dim; dimIter++){
	    ret.set(dimIter, dimIter, val);
	}
	return ret;
    }

    /** a Java interface to look like MATLAB's quadprog => easy to port to MATLAB's engine if needed
     */
    //TODO: make another layer of wrapper
    public static double[] solve(final double[][] H, final double [] f, final double[][] A, final double[] b, final double [][]Aeq, final double[] beq, double[] lb, double[] ub){	
	JamaMatrix HM = JamaMatrix.FACTORY.copy(H);
	JamaMatrix fM = JamaMatrix.FACTORY.makeColumn(f);	//TODO: switch sign to match matlab
	
	JamaMatrix AM=null, bM=null, AeqM=null, beqM=null, lbM=null, ubM=null;
	if (A != null && b != null){	//the inequality bounds
	    AM = JamaMatrix.FACTORY.copy(A);
	    bM = JamaMatrix.FACTORY.makeColumn(b);
	}
	if (Aeq != null && beq != null){	//the equality bounds
	    AeqM = JamaMatrix.FACTORY.copy(Aeq);
	    beqM = JamaMatrix.FACTORY.makeColumn(beq);
	}
	//Bounds handling
	if (lb != null) lbM = JamaMatrix.FACTORY.makeColumn(lb);
	if (ub != null) ubM = JamaMatrix.FACTORY.makeColumn(ub);
	
	BasicMatrix ansMatrix = solve(HM, fM, AM, bM, AeqM, beqM, lbM, ubM);
	return mat2double(ansMatrix);
    }
    
    public static BasicMatrix solve(final JamaMatrix H, final JamaMatrix f, final JamaMatrix A, final JamaMatrix b, final JamaMatrix Aeq, final JamaMatrix beq, final JamaMatrix lb, final JamaMatrix ub){
	QuadraticSolver.Builder qpBlder = new QuadraticSolver.Builder(H, f);  //first the basics
	
	if (lb != null){
	    TwoMatrices Ab = genBoundMatrices(lb);
	    JamaMatrix ADown = Ab.A;
	    JamaMatrix BDown = Ab.B;
	    appendMatrixDownIfNotNull(A, ADown);
	    appendMatrixDownIfNotNull(b, BDown);
	}
	if (ub != null){	//TODO: decouple
	    TwoMatrices Ab = genBoundMatrices(ub);
	    JamaMatrix ADown = Ab.A.negate();
	    JamaMatrix BDown = Ab.B.negate();
	    appendMatrixDownIfNotNull(A, ADown);
	    appendMatrixDownIfNotNull(b, BDown);
	}

	if (A != null && b != null){	//the inequality bounds
	    qpBlder.inequalities(A, b);
	}
	
	if (Aeq != null && beq != null){	//the equality bounds
	    qpBlder.equalities(Aeq, beq);
	}
	
	QuadraticSolver qpSolver = qpBlder.build();
	Result result = qpSolver.solve();
	
	return  result.getSolution();
    }
    
    protected static double[] mat2double(BasicMatrix ansMatrix){
	if (ansMatrix == null){
	    return null;
	} else {
	    int rowCt = ansMatrix.getRowDim();
	    double[] ret = new double[rowCt];
	    for (int rowIter = 0; rowIter < rowCt; rowIter++)
		ret[rowIter] = ansMatrix.doubleValue(rowIter, 0);
	    return ret;
	}
    }
    
    protected static JamaMatrix appendMatrixDownIfNotNull(JamaMatrix matrixAbove, JamaMatrix matrixBelow){
	if (matrixAbove == null && matrixBelow == null){
	    return null;
	} else if (matrixAbove == null && matrixBelow != null){
	    return matrixBelow;
	} else if (matrixAbove != null && matrixBelow == null){
	    return matrixAbove;
	} else {
	    return matrixAbove.mergeColumns(matrixBelow);
	}
    }
    
    protected static TwoMatrices genBoundMatrices(JamaMatrix slipMatrix){
	if (slipMatrix == null){
	    return null;
	}
	
	int slipLen = slipMatrix.getRowDim();
	List<double[]> A = new ArrayList<double[]>();
	List<Double> b = new ArrayList<Double>();
	
	for (int i = 0; i < slipLen; i++){
	    double curSlip = slipMatrix.get(i, 0);
	    if ( !Double.isNaN(curSlip) &&  !Double.isInfinite(curSlip) ){
        	    double[] ARow = new double[slipLen];
        	    ARow[i] = 1d;
        	    A.add(ARow);
        	    b.add(curSlip);
	    }
	}
	
	JamaMatrix AM = JamaMatrix.FACTORY.copy( A.toArray(new double[0][]) );
	JamaMatrix bM = JamaMatrix.FACTORY.makeColumn( b.toArray(new Double[0]) );
	
	return new TwoMatrices(AM, bM);
    }
    
    
    
    public int getSlipCt() {
        return slipCt;
    }

    public void setSlipCt(int slipCt) {
        this.slipCt = slipCt;
    }

    public int getDispCompCt() {
        return dispCompCt;
    }

    public void setDispCompCt(int dispCompCt) {
        this.dispCompCt = dispCompCt;
    }



    protected static class TwoMatrices{
	public JamaMatrix A;
	public JamaMatrix B;
	public TwoMatrices(JamaMatrix a, JamaMatrix b) {
	    super();
	    A = a;
	    B = b;
        }
    }
    

    

}
