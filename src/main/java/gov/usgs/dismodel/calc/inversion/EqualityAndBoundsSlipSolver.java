package gov.usgs.dismodel.calc.inversion;

import java.util.ArrayList;

import gov.usgs.dismodel.calc.SolverException;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.OptimisationSolver;
import org.ojalgo.optimisation.quadratic.QuadraticSolver;

/**
 * Calculates slips of modeled fault-blocks given GPS data, a Green's function,
 * and some or all of various constraints.
 * 
 * The GPS readings make our equations non-homogeneous, requiring us to use a
 * quadratic program instead of the classic linear program. This translates our
 * non-homogeneous linear system of equations into the standard Quadratic
 * Program, which various libraries can solve, in order to apply several
 * different kinds of bounds and equality constraints to the solution. See A
 * REFLECTIVE NEWTON METHOD FOR MINIMIZING A QUADRATIC FUNCTION SUBJECT TO
 * BOUNDS ON SOME OF THE VARIABLES by THOMAS F. COLEMAN AND YUYING Li SIAM J.
 * OPTIMIZATION, Vol. 6, No. 4, pp. 1040-1058, November 1996 which notes the use
 * of AtA = H = Q, and c = -ATb to translate the minimization of the sum of
 * squares of the residual, Ax - b, to minimizing [X]T[H][X] - [C]T[X]
 * 
 * @author Chris Forden
 */
public class EqualityAndBoundsSlipSolver extends ConstrainedLinearLeastSquaresSolver {

    /*
     * class constants
     * **********************************************************
     * *****************
     */
    static final private int MAKE_LOWER_BOUND = -1;
    static final private int MAKE_UPPER_BOUND = 1;
    static final private double LOWER_BOUND_THRESHOLD = -0.5;
    static final private double UPPER_BOUND_THRESHOLD = 0.5;

    static final private String ABBREV_NAME = "QuadProg";

    /*
     * Instance variables
     * ************************************************************************
     */

    protected JamaMatrix H; // Q in ojAlgo. = AtA
    protected JamaMatrix c;
    JamaMatrix At = null;
    protected OptimisationSolver.Result solutionResult = null;
    /* A previous version of this class allowed checkConstraints to be reset. */
    private boolean checkConstraints = true;

    /** Matrices for constraints */

    /** Inequality matrix, AI, and column vector, BI. */
    protected JamaMatrix AI;
    private JamaMatrix BI;

    /**
     * Created and cached on demand when a getBound...() method is first called
     */
    private int[] subfaultToLowerBIndices = null;
    private int[] subfaultToUpperBIndices = null;
    private int momentLbIndex = -1;
    private int momentUbIndex = -1;

    /** Equality matrix, AE, and column vector, BE */
    private JamaMatrix AE;
    private JamaMatrix BE;

    /**
     * Created and cached on demand when a getEqualityConstraint...() method is
     * first called
     */
    private int[] subfaultToEqualityIndices = null;
    private int momentEqualityIndex = -1;

    /** These can be used for either equality or inequality. */
    private double modulus = 0.0;
    private double[] subfaultAreas;

    private QuadraticSolver.Builder builder;
    private QuadraticSolver solver;

    /*
     * Methods
     * ******************************************************************
     * ****************
     */

    /**
     * Input the linear program, Ax - b, to be minimized, to this constructor,
     * where typically: A is the Greens function b is the measured displacements
     * x is the distributed slips to be estimated,
     * 
     * We require the constructor to take the main parameters because setting
     * them more than once could make the code prone to various subtle bugs.
     * 
     * @param greensMatrix
     *            Can be non-square, typically over-determined, that is with
     *            more rows than columns. Often, Laplacian smoothing elements
     *            are included in this matrix so that noise in the data will
     *            perturb solution-elements less. Try to avoid calling this more
     *            than once on an instance; reliability is likely to be better
     *            if you instantiate another instance and populate it, instead.
     *            Some concrete overrides might even throw a SolverException if
     *            you try to re-populate the Green's function.
     * 
     * @param measuredDisplacements
     *            a column vector, that is a matrix whose elements all have
     *            column-indices of zero. Typically the elements are
     *            measurements gathered by data stations, such as GPS stations,
     *            strain-gauges, tilt-meters, etc.
     * 
     */
    public EqualityAndBoundsSlipSolver(double[][] greensMatrix, double[] measuredDisplacements) {
	super(greensMatrix, measuredDisplacements);
    }

    public EqualityAndBoundsSlipSolver() {
	super();
    }

    /**
     * This method allows a client (for example, cross validation) to use this
     * class to store constraints across many settings and solvings of Green's
     * function and measured data.
     * 
     * TODO Add Check that the greensMatrix dims are compatible with any
     * constraints previously set, and that setMeasuredDisplacements() had not
     * been called before this and after previous solve() (in which case At
     * would have been used without being properly set).
     */
    @Override
    public void setGreensFunction(double[][] greensMatrix) {
	super.setGreensFunction(greensMatrix);
	At = (JamaMatrix) greensFunct.transpose();
	H = (JamaMatrix) At.multiplyRight((MatrixStore<Double>) (greensFunct));
    }

    /**
     * Set the Green's Function before calling this. (So At gets properly set)
     */
    @Override
    public void setMeasuredDisplacements(double[] disps) {
	super.setMeasuredDisplacements(disps);
	c = (JamaMatrix) At.multiplyRight((MatrixStore<Double>) (this.measuredDisplacements));
    }

    /**
     * Set matrices directly that control upper and lower bounds for everything.
     * This makes no attempt yet to notice whether other inequality or bounds
     * have previously been set, and hence will be overwritten by this call. If
     * you set arbitrary matrices, such as not keeping obeying the rules of
     * having AI made of -1, 0, or 1, and having only diagonal elements or full
     * rows, then you might want to first call setCheckContraints(false), at
     * least before calling solve().
     */
    public void setAllInequalityConstraints(MatrixStore<Double> AI, MatrixStore<Double> BI) {
	this.AI = (JamaMatrix) AI;
	this.BI = (JamaMatrix) BI;
    }

    /*
     * TODO: Check for conflicts: equality vs. inequality, block bounds whose
     * sum violates moment bounds.
     */

    @Override
    public String getAbbreviatedSolverName() {
	return ABBREV_NAME;
    }

    @Override
    public String getSolutionNotes() {
	StringBuilder solNotes = new StringBuilder();
	String iterSse = "After " + Integer.toString(solutionResult.getIterationsCount())
	        + " iterations, the raw SSE= " + Double.toString(calcSSE(solutionResult.getSolution()));
	solNotes.append(iterSse);
	if (AI != null) {
	    /* Vector of predicted displacements */
	    BasicMatrix sol = solutionResult.getSolution();

	    double tol = sol.getOneNorm().getReal() * 1e-10 + Double.MIN_NORMAL;
	    solNotes.append("\n <(one-based) index: bound, value;>:    ");
	    int numActivelyBounded = 0;
	    for (int i = 0; i < getSolutionSize(); i++) {
		double ithPred = sol.doubleValue(i, 0);
		/* Is the i'th value of the solution close to the lower bound? */
		if (AI.doubleValue(i, i) < LOWER_BOUND_THRESHOLD && -BI.doubleValue(i, 0) + tol > ithPred) {
		    solNotes.append("   ");
		    solNotes.append(i + 1);
		    solNotes.append(": ");
		    solNotes.append(-BI.doubleValue(i, 0));
		    solNotes.append(", ");
		    solNotes.append(ithPred);
		    solNotes.append("; ");
		    numActivelyBounded++;
		}
	    }
	    solNotes.append("  The preceding " + numActivelyBounded + " solution values, were within " + tol
		    + " of their constraints.  Therefore they probably " + "were actively limited by those bounds.");
	}
	return solNotes.toString();
    }

    /**
     * This makes no attempt to notice whether the bounds being set, overwrite
     * previously set constraints.
     * 
     * @param faultBlock
     *            Index into the grid of cells ("blocks") in the fault being
     *            modeled. Zero-based.
     * @param lb
     *            The lower bound for the fault-block being constrained.
     * @throws SolverException
     * @see #setUpperBoundForABlockSlip()
     * @see #setMomentLowerBound()
     * @see #setMomentUpperBound()
     */
    public void setLowerBoundForABlockSlip(int faultBlock, double lb) throws SolverException {
	double ub = getUpperBound(faultBlock);
	if (!Double.isNaN(ub) && ub < lb && checkConstraints)
	    throw new SolverException("Lower bound greater than " + "upper bound for a subfault's slip");
	addBoundForABlockSlip(faultBlock, lb, MAKE_LOWER_BOUND);
    }

    /**
     * This makes no attempt yet to notice whether bounds being set overwrite or
     * conflict with previously set bounds or inequalities.
     * 
     * @param index
     *            Which fault-block is being constrained. Zero-based.
     * @param ub
     *            The new upper bound for the fault-block being constrained.
     * @throws SolverException
     * @see #setLowerBoundForABlockSlip()
     * @see #setMomentLowerBound()
     * @see #setMomentUpperBound()
     */
    public void setUpperBoundForABlockSlip(int index, double ub) throws SolverException {
	double lb = getLowerBound(index);
	if (!Double.isNaN(lb) && ub < lb && checkConstraints)
	    throw new SolverException("Upper bound less than " + "lower bound for a subfault's slip");
	addBoundForABlockSlip(index, ub, MAKE_UPPER_BOUND);
    }

    /**
     * If you call any of these constraint-setting methods, you must call
     * setSubfaultAreas() before solving.
     */
    private void addBoundForABlockSlip(int index, double bound, int makeWhichBound) throws SolverException {
	maybeCreateInequalityMatrices();
	switch (makeWhichBound) {
	case MAKE_LOWER_BOUND:
	    AI.set(index, index, makeWhichBound);
	    BI.set(index, 0, bound * makeWhichBound);
	    break;
	case MAKE_UPPER_BOUND:
	    if (getSolutionSize() < 1)
		throw new SolverException("EqualityAndBoundsSlipSolver." + "addBoundForABlockSlip bad solutionSize");
	    /*
	     * Index into the second set of bounds, by jumping over the first
	     * solutionSize set of equations:
	     */
	    AI.set(index + getSolutionSize(), index, makeWhichBound);
	    BI.set(index + getSolutionSize(), 0, bound * makeWhichBound);
	    break;
	default:
	    throw new SolverException("EqualityAndBoundsSlipSolver." + "addBoundForABlockSlip bad bound.");
	}
    }

    /**
     * Forces all sub-faults to be constrained to be non-negative.
     * 
     * If you call any of these constraint-setting methods, you must call
     * setSubfaultAreas() before solving.
     */
    public void setAllBlksNonNeg() {
	assertConstrainability();
	for (int i = 0; i < getSolutionSize(); i++)
	    addBoundForABlockSlip(i, 0.0, MAKE_LOWER_BOUND);
    }

    private void assertConstrainability() {
	if (getSolutionSize() < 1)
	    throw new SolverException("You must either set the Green's "
		    + "function or call setNumberOfModeledParameters() " + "before setting constraints");
	if (subfaultToLowerBIndices != null || subfaultToUpperBIndices != null || momentLbIndex != -1
	        || momentUbIndex != -1 || subfaultToEqualityIndices != null || momentEqualityIndex != -1)
	    throw new SolverException("After solving, you must remove all "
		    + "constraints before adding any constraints.");
    }

    /*
     * It would be feasible to add a reduceNumberOfModeledParameters() method,
     * although it would not be feasible to increase them after setting AE and
     * AI unless you reimplemented constraints without prepopulating those
     * matrices with zeroes. --Chris Forden 2010 Aug. 13
     */
    /**
     * If you set constraints before setting the Green's function, you must
     * first call this
     * 
     * so the implementation can know the size of the constraints matrices to
     * build.
     * 
     * @param matrixSizeTemplate
     *            A matrix the same size as the Green's function to be set
     *            later.
     */
    public void setConstraintsMatrixSize(double[][] matrixSizeTemplate) {
	int sizeOfHMatrix = matrixSizeTemplate[0].length;
	if (matrixSizeTemplate.length < sizeOfHMatrix)
	    sizeOfHMatrix = matrixSizeTemplate.length;
	setNumberOfModeledParameters(sizeOfHMatrix);
    }

    /**
     * Calculate the number of rows AI will have. The first square of rows in AI
     * are for lower bounds for solution elements. That square will be
     * solutionSize x solutionSize, and will be zeroes except for diagonal
     * elements corresponding to lower bounds on individual fault-block slips.
     * The 2nd square of rows (below the first) are for upper bounds. Below
     * those squares, the 2nd-to-bottom row is for the lower bound of the
     * moment. The bottom row is for the upper bound of the moment.
     */
    private int calcNumRowsAI() {
	return (getSolutionSize() + 1) * 2;
    }

    private void maybeCreateInequalityMatrices() {
	assertConstrainability();
	if (subfaultToLowerBIndices != null || subfaultToUpperBIndices != null)
	    throw new SolverException("Don't add bounds after solving without " + "first removing constraints");
	int AiRowCnt = calcNumRowsAI();
	if (AI == null)
	    AI = JamaMatrix.FACTORY.makeZero(AiRowCnt, getSolutionSize());
	if (BI == null)
	    BI = JamaMatrix.FACTORY.makeZero(AiRowCnt, 1);
    }

    /**
     * Put max or min constraint on the total energy of the fault motion (summed
     * over all the sub-fault blocks).
     * 
     * @param mb
     *            The moment bound, that is the bound for the sum of the slips
     *            on all the fault-blocks modeled.
     * @param makeWhichBound
     *            MAKE_LOWER_BOUND == -1, MAKE_UPPER_BOUND == 1
     * @throws SolverException
     */
    private void setMomentBound(double mb, double makeWhichBound) throws SolverException {
	maybeCreateInequalityMatrices();
	/** Set the moment inequality: */
	int rowOffsetFromRowCount = 1; // moment lower bound
	if (makeWhichBound > 0) // moment upper bound
	    rowOffsetFromRowCount = 2;
	else
	    mb = -mb;
	for (int j = 0; j < AI.getColDim(); j++) {
	    AI.set(AI.getRowDim() - rowOffsetFromRowCount, j, makeWhichBound);
	}
	if (BI.getRowDim() != AI.getRowDim())
	    throw new SolverException("EqualityAndBoundsSlipSolver." + "setMomentLowerBound: BI.getRowDim() "
		    + "!= AI.getRowDim()");
	BI.set(BI.getRowDim() - rowOffsetFromRowCount, 0, mb / modulus);
    }

    /**
     * The fault-slip's moment is equal to the sum of all the block-slips times
     * their combined area, times the material's shear modulus. Use this method
     * to constrain that sum.
     * 
     * If you constrain the moment, you must call setSubfaultAreas() before
     * solving.
     * 
     * @param mub
     *            Moment upper bound
     * @param totArea
     *            The sum of the areas for all the fault-blocks being modeled.
     * @param shearModulus
     *            The average shear modulus for the rock or material of the
     *            fault.
     * @throws SolverException
     * @see #setMomentLowerBound()
     * @see #setLowerBoundForABlockSlip()
     * @see #setUpperBoundForABlockSlip()
     */
    public void setMomentUpperBound(double mub, double shearModulus) {
	setCheckShearModulus(shearModulus);
	setMomentBound(mub, MAKE_UPPER_BOUND);
    }

    /**
     * The fault-slip's moment (energy) is equal to the sum of all the
     * block-slips times their combined area, times the material's shear
     * modulus. Use this method to constrain that sum.
     * 
     * If you constrain the moment, you must call setSubfaultAreas() before
     * solving.
     * 
     * @param mlb
     *            Moment lower bound
     * @param totArea
     *            The sum of the areas for all the fault-blocks being modeled.
     * @param shearModulus
     *            The average shear modulus for the rock or material of the
     *            fault.
     * @throws SolverException
     * @see #setMomentUpperBound()
     * @see #setLowerBoundForABlockSlip()
     * @see #setUpperBoundForABlockSlip()
     */
    public void setMomentLowerBound(double mlb, double shearModulus) {
	setCheckShearModulus(shearModulus);
	setMomentBound(mlb, MAKE_LOWER_BOUND);
    }

    /**
     * Constrain a fault-block to have exactly the specified slip. This is a
     * kind of equality constraint. This makes no attempt to notice whether the
     * bounds being set, overwrite or conflict with, previously set constraints.
     * 
     * @param faultBlock
     *            An index into the grid of cells modeling the fault
     * @param slip
     *            How far the block moves
     * @throws SolverException
     */
    public void setSlip(int faultBlock, double slip) throws SolverException {
	maybeCreateEqualityMatrices();
	AE.set(faultBlock, faultBlock, 1.0);
	BE.set(faultBlock, 0, slip);
    }

    /**
     * The client must set these areas if there are constraints, prior to
     * calling Solve().
     * 
     * @param areas
     *            an array of size equal to the number of columns in the
     *            greensMatrix passed to the constructor.
     */
    public void setSubfaultAreas(double[] areas) {
	this.subfaultAreas = areas;
    }

    /**
     * Constrain the sum of slips of all fault-blocks being modeled, multiplied
     * by their areas and the (shear) modulus of the rock, to equal the
     * specified moment-value. This is a kind of equality constraint. This makes
     * no attempt to notice whether the bounds being set, overwrite or conflict
     * with, previously set constraints.
     * 
     * If you constrain the moment, you must call setSubfaultAreas() before
     * solving.
     * 
     * @throws SolverException
     */
    public void setMoment(double moment, double shearModulus) throws SolverException {
	maybeCreateEqualityMatrices();
	setCheckShearModulus(shearModulus);
	for (int j = 0; j < AE.getColDim(); j++)
	    /*
	     * 1.0 will get replaced by the subfault area in
	     * purgeUnusedEqualityRows()).
	     */
	    AE.set(AE.getRowDim() - 1, j, 1.0);
	BE.set(BE.getRowDim() - 1, 0, moment / shearModulus);
    }

    private void maybeCreateEqualityMatrices() {
	assertConstrainability();
	if (subfaultToEqualityIndices != null)
	    throw new SolverException("Constraint applied after " + "solving without removing old constraints");

	if (AE == null)
	    AE = JamaMatrix.FACTORY.makeZero(getSolutionSize(), getSolutionSize());
	if (BE == null)
	    BE = JamaMatrix.FACTORY.makeZero(getSolutionSize(), 1);
    }

    /**
     * Set and check coefficients related to moment constraints. Prevent client
     * from passing one set of coefficient values to setMomentLowerBound() but
     * passing a non-equal set to setMomentUpperBound(). Also prevent passing
     * zero values.
     */
    private void setCheckShearModulus(double shearModulus) {
	if (!checkConstraints) {
	    this.modulus = shearModulus;
	    return;
	}
	if (shearModulus == 0.0)
	    throw new SolverException("Zero modulus passed as a " + "moment constraint.");

	if (modulus == 0.0)
	    modulus = shearModulus;
	else if (modulus != shearModulus)
	    throw new SolverException("New non-zero modulus set before " + "removing old constraints.");
    }

    /** Removes all constraints on moment and on slips for the fault-blocks. */
    public void removeConstraints() {
	modulus = 0.0;
	AI = null;
	BI = null;
	AE = null;
	BE = null;

	subfaultToLowerBIndices = null;
	subfaultToUpperBIndices = null;
	momentLbIndex = -1;
	momentUbIndex = -1;

	subfaultToEqualityIndices = null;
	momentEqualityIndex = -1;
    }

    @Override
    public double[] solve() {
	/* Calc some intermediate results first */
	JamaMatrix At = (JamaMatrix) greensFunct.transpose();
	H = (JamaMatrix) At.multiplyRight((MatrixStore<Double>) (greensFunct));
	c = (JamaMatrix) At.multiplyRight((MatrixStore<Double>) (measuredDisplacements));

	builder = new QuadraticSolver.Builder(H, c);
	if (AI != null) {
	    if (BI == null)
		throw new SolverException("One but not both inequality " + "matrices had been set.");
	    else {
		//purgeUnusedInequalityRows();
		builder.inequalities(AI, BI);
	    }
	}
	if (AE != null) {
	    if (BE == null)
		throw new SolverException("One but not both equality " + "matrices had been set.");
	    else {
		//purgeUnusedEqualityRows();
		builder.equalities(AE, BE);
	    }
	}

	solver = builder.build();
	solutionResult = solver.solve();

	if (solutionResult.getSolution() != null) {
	    double[] retSolution = new double[getSolutionSize()];
	    for (int i = 0; i < getSolutionSize(); i++)
		try {
		    retSolution[i] = solutionResult.getSolution().doubleValue(i, 0);
		} catch (Exception e) {
		    // //DEBUG
		    e.printStackTrace();
		    return null;
		}
	    return retSolution;
	} else {
	    return null;
	}
    }

    private void checkAreasSet(String constraintKind) {
	if (subfaultAreas == null) {
	    throw new SolverException("Since you set an " + constraintKind + " constraint on the moment, you should "
		    + "setSubfaultAreas before Solving.");
	}
	if (subfaultAreas.length != this.greensFunct.getColDim())
	    throw new SolverException(subfaultAreas.length + " subfault areas " + "had been set, but there are "
		    + greensFunct.getColDim() + " subfaults.");
    }

    /**
     * Culls the rows of the inequality matrix that do not correspond to any
     * bound. We need to eliminate unused rows so ojAlgo's quad. prog. solver
     * doesn't take 40 times longer to run. Also see purgeUnusedEqualityRows()
     */
    private void purgeUnusedInequalityRows() {
	final int rows = AI.getRowDim();
	final int cols = AI.getColDim();
	assertProperNumCols(cols);
	ArrayList<Integer> rowsNeeded = new ArrayList<Integer>(rows);

	for (int i = 0; i < rows; i++) {
	    boolean rowNeeded = false;
	    int numLowersThisRow = 0;
	    int numUppersThisRow = 0;

	    for (int j = 0; j < cols; j++) {
		double value = AI.doubleValue(i, j);
		if (value != 0.0) {
		    rowNeeded = true;
		    if (value < LOWER_BOUND_THRESHOLD) {
			numLowersThisRow++;
		    }
		    if (value > UPPER_BOUND_THRESHOLD) {
			numUppersThisRow++;
		    }
		}
	    }
	    if (numLowersThisRow > 0) {
		if (numUppersThisRow > 0 && checkConstraints)
		    throw new SolverException("Can't have upper and lower " + "bounds in the same row.");
		if (numLowersThisRow > 1) {
		    if (numLowersThisRow != cols && checkConstraints)
			throw new SolverException("Allowed lower bounds " + "in a row: 0, 1, or number of columns.");
		    momentLbIndex = rowsNeeded.size();
		    checkAreasSet("inequality"); // Throws if bad areas
		    /* Copy in the subfault areas for this moment lower-bound: */
		    for (int subfault = 0; subfault < subfaultAreas.length; subfault++)
			AI.set(i, subfault, -subfaultAreas[subfault]);
		} else { /*
		          * There is only one bound in this row, a lower-bound.
		          * It must be on the diagonal.
		          */
		    if (i > cols || AI.doubleValue(i, i) > LOWER_BOUND_THRESHOLD && checkConstraints)
			throw new SolverException("The lower bound was " + "not on the lower's diagonal.");
		    /*
	             * We have not yet added this row to the growing list of
	             * rowsNeeded, so no need to subtract one from its size to
	             * get the index.
	             */
		    ensureIneqIndices(); // Inits indices to inequalities if
					 // null
		    subfaultToLowerBIndices[i] = rowsNeeded.size();
		}
	    }
	    if (numUppersThisRow > 0) {
		if (numUppersThisRow > 1) {
		    if (numUppersThisRow != cols)
			throw new SolverException("Allowed upper bounds " + "in a row: 0, 1, or number of columns.");
		    momentUbIndex = rowsNeeded.size();
		    checkAreasSet("inequality"); // Throws if bad areas
		    /* Copy in the subfault areas for this moment upper-bound: */
		    for (int subfault = 0; subfault < subfaultAreas.length; subfault++)
			AI.set(i, subfault, subfaultAreas[subfault]);
		} else { /*
		          * There is only one bound in this row, an upper bound.
		          * It must be on the diagonal.
		          */
		    if ((i < cols || AI.doubleValue(i, i - getSolutionSize()) < UPPER_BOUND_THRESHOLD)
			    && checkConstraints)
			throw new SolverException("The upper bound was " + "not on the upper's diagonal.");
		    /*
	             * We have not yet added this row to the growing list of
	             * rowsNeeded, so no need to subtract one from its size to
	             * get the index.
	             */
		    ensureIneqIndices(); // Inits indices to inequalities if
					 // null
		    subfaultToUpperBIndices[i - getSolutionSize()] = rowsNeeded.size();
		}
	    }

	    if (rowNeeded) {
		rowsNeeded.add(i);
		continue;
	    }
	    if (BI.doubleValue(i, 0) != 0)
		throw new SolverException("Inequality error on equation " + i);
	} // check all rows
	int[] ia = new int[rowsNeeded.size()];

	for (int i = 0; i < rowsNeeded.size(); i++) {
	    ia[i] = rowsNeeded.get(i);
	}
	AI = AI.getRows(ia);
	BI = BI.getRows(ia);
    } // purgeUnusedInequalityRows()

    private void assertProperNumCols(int cols) {
	if (cols != greensFunct.getColDim())
	    throw new SolverException("Constraint matrix had a number of "
		    + "columns different from that of the Green's function");
    }

    /**
     * Culls the rows of the inequality matrix that do not correspond to any
     * bound. We need to eliminate unused rows so ojAlgo's quad. prog. solver
     * doesn't take 40 times longer to run. Copied and modified from
     * purgeUnusedInequalityRows()
     */
    private void purgeUnusedEqualityRows() {
	final int rows = AE.getRowDim();
	final int cols = AE.getColDim();
	assertProperNumCols(cols);
	ArrayList<Integer> rowsNeeded = new ArrayList<Integer>(rows);
	subfaultToEqualityIndices = new int[cols];

	/*
	 * Start off with invalid indices for all possible equality constraints
	 */
	momentEqualityIndex = -1;
	for (int i = 0; i < cols; i++) {
	    subfaultToEqualityIndices[i] = -1;
	}

	for (int i = 0; i < rows; i++) {
	    boolean rowNeeded = false;
	    int numNonZerosThisRow = 0;

	    for (int j = 0; j < cols; j++) {
		double value = AE.doubleValue(i, j);
		if (value != 0.0) {
		    rowNeeded = true;
		    numNonZerosThisRow++;
		}
	    }
	    if (numNonZerosThisRow > 0) {
		if (numNonZerosThisRow > 1) {
		    if (numNonZerosThisRow != cols && checkConstraints)
			throw new SolverException("Allowed non-zeros " + "in a row: 0, 1, or number of columns.");
		    momentEqualityIndex = rowsNeeded.size();
		    checkAreasSet("equality"); // Throws if bad areas
		    /* Copy in the subfault areas for this moment equality: */
		    for (int subfault = 0; subfault < subfaultAreas.length; subfault++)
			AE.set(i, subfault, subfaultAreas[subfault]);
		} else { /*
		          * There is only one non-zero in this row. It must be
		          * on the diagonal.
		          */
		    if (i > cols || AE.doubleValue(i, i) == 0.0 && checkConstraints)
			throw new SolverException("A subfault's equality " + "constraint was not on the diagonal.");
		    /*
	             * We have not yet added this row to the growing list of
	             * rowsNeeded, so no need to subtract one from its size to
	             * get the index.
	             */
		    ensureEqIndices(); // Inits indices to inequalities if null
		    subfaultToEqualityIndices[i] = rowsNeeded.size();
		}
	    }
	    if (rowNeeded) {
		rowsNeeded.add(i);
		continue;
	    }
	    if (BE.doubleValue(i, 0) != 0)
		throw new SolverException("Equality error on equation " + i);
	} // check all rows
	int[] ea = new int[rowsNeeded.size()];

	for (int i = 0; i < rowsNeeded.size(); i++) {
	    ea[i] = rowsNeeded.get(i);
	}
	AE = AE.getRows(ea);
	BE = BE.getRows(ea);
    } // purgeUnusedEqualityRows()

    private void ensureIneqIndices() {
	if (subfaultToLowerBIndices != null)
	    return;

	final int cols = AI.getColDim();
	subfaultToLowerBIndices = new int[cols];
	subfaultToUpperBIndices = new int[cols];

	/*
	 * Start off with invalid indices for all possible inequality
	 * constraints
	 */
	for (int i = 0; i < cols; i++) {
	    subfaultToLowerBIndices[i] = -1;
	    subfaultToUpperBIndices[i] = -1;
	}
    }

    private void ensureEqIndices() {
	if (subfaultToEqualityIndices != null)
	    return;

	final int cols = AI.getColDim();
	subfaultToEqualityIndices = new int[cols];

	/*
	 * Start off with invalid indices for all possible equality constraints
	 */
	for (int i = 0; i < cols; i++) {
	    subfaultToEqualityIndices[i] = -1;
	}
    }

    @Override
    public double getLowerBound(final int subfaultIndex) {
	if (BI == null)
	    return Double.NaN;
	if (subfaultToLowerBIndices == null)
	    if (AI.doubleValue(subfaultIndex, subfaultIndex) < LOWER_BOUND_THRESHOLD)
		return -BI.doubleValue(getLbRow(subfaultIndex), 0);
	    else
		/* This is the right row, but no bound has been set. */
		return Double.NaN;
	final int index = subfaultToLowerBIndices[subfaultIndex];
	if (index == -1)
	    return Double.NaN;
	return -BI.doubleValue(subfaultToLowerBIndices[subfaultIndex], 0);
    }

    @Override
    public double getUpperBound(final int subfaultIndex) {
	if (BI == null)
	    return Double.NaN;
	if (subfaultToUpperBIndices == null)
	    if (AI.doubleValue(subfaultIndex + getSolutionSize(), subfaultIndex) > UPPER_BOUND_THRESHOLD)
		return BI.doubleValue(getUbRow(subfaultIndex), 0);
	    else
		/* This is the right row, but no bound has been set. */
		return Double.NaN;
	final int index = subfaultToUpperBIndices[subfaultIndex];
	if (index == -1)
	    return Double.NaN;
	return BI.doubleValue(subfaultToUpperBIndices[subfaultIndex], 0);
    }

    /**
     * Gets the equation number for an upper bound, before the unused rows have
     * been culled. Does NOT check whether AI is null, because the caller is
     * probably going to have to do that itself, anyway. Created because we
     * anticipate removing rows for bounds not used, in the future, in which
     * case this will help encapsulate those effects. Will generally crash
     * (throw outOfBoundsException) if called after the solve() has been run;
     * solve() culls the unused equations from the inequality matrices.
     * 
     * @param subfaultIndex
     * @return A zero-based row-index into AI and BI.
     * 
     * @see calcNumRowsAI() comments.
     */
    private int getUbRow(int subfaultIndex) {
	return subfaultIndex + getSolutionSize();
    }

    /** @see getUbRow() comments. */
    private int getLbRow(int subfaultIndex) {
	return subfaultIndex;
    }

    /**
     * Returns the moment LB divided by the product of modulus * total_area.
     * Returns NaN if there is no upper bound on the total moment.
     */
    public double getMomentLowerBound() {
	if (BI != null && momentLbIndex != -1)
	    return -BI.doubleValue(momentLbIndex, 0);
	else
	    return Double.NaN;
    }

    /**
     * Returns the moment UB divided by the product of modulus * total_area.
     * Returns NaN if there is no upper bound on the total moment.
     */
    public double getMomentUpperBound() {
	if (BI != null && momentUbIndex != -1)
	    return BI.doubleValue(momentUbIndex, 0);
	else
	    return Double.NaN;
    }

    /**
     * @return NaN if there is no equality constraint on the total moment. If
     *         there is such a constraint the constraint divided by the product
     *         of shear modulus times total area.
     */
    public double getMomentEqualityConstraint() {
	if (BE != null)
	    return BE.doubleValue(momentEqualityIndex, 0);
	else
	    return Double.NaN;
    }

    /**
     * @return the shear modulus (in Pascals)
     */
    public double getModulus() {
	return modulus;
    }

}
