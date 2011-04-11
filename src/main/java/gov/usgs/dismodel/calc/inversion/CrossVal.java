/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.CrossValidationProgressDlg;
import gov.usgs.dismodel.calc.SolverException;

/**
 * Cross validation.
 * 
 * A line-by-line translation of crossVal.m, which tries various strengths of
 * smoothing imperative added to a set of equations which may otherwise be
 * under-constrained or mixed-constrained.
 * 
 * @author cforden from jmurray's crossVal.m
 * 
 */
public class CrossVal {

    /* Instance variables *************************************************************************/
    /**Indexed like: solution_CV[shadedStation][gamma][modeledParam];    **/
    protected double solution_CV[][][] = null;
    
    protected double[][] res;
    protected double[] CVSS;
    protected CrossValidationProgressDlg progDlg;
    private double minCVSS;
    private double optGamma;
    private double[][] S;
    private double[][] greens;
    private int nm; // number of modeled params
    
    
    /* Methods ************************************************************************************/

    /**
     * This fully parameterized constructor does the cross validation.
     * 
     * <p>
     * This constructor receives all the parameters and solves them.
     * 
     * For descriptions of CrossVal's parameters, below, these definitions
     * apply:
     * <ul>
     * <li>nd = number of data
     * <li>nm = number of model parameters
     * <li>ng = number of smoothing parameter values to try
     * </ul>
     * 
     * @see ehz_gps/trunk/modeling/matlab/dismodel/inversion/crossVal.m's
     *      comments. "MATLAB: " comments above java statements in this method,
     *      relate those java statements to MATLAB statements in crossVal.m.
     * 
     * @param gam
     *            Vector of candidate values for smoothing parameter. The
     *            Laplacian gets weighted by (1/gam[i]) to be consistent with
     *            the weighting of the data and G. Done this way, low gamma
     *            values will produce smooth solutions and high gamma will
     *            produce rough solutions, as would be expected when gamma^2 is
     *            taken to be the variance of the smoothing constraints.
     * 
     * @param WD
     *            nd x 1 data vector weighted by chol(inv(dcov)) where dcov is
     *            the data covariance. If multiple station networks are used,
     *            all data for stations of a given network are found as a block
     *            in this vector. If a station has multiple components of data
     *            (e.g., east, north, and up), these are found as a block for
     *            that station in the vector. E.g., for two networks, the first
     *            of which has j stations with E, N, U and the second of which
     *            has k stations of E, and N data the data vector would look
     *            like this:
     * 
     *            <ul>
     *            <li>[E11; N11; U11; E12; N12; U12; ... E1j; N1j; U1j; E21;
     *            N21; E22; N22; ... E2k; N2k]
     *            </ul>
     * 
     * 
     * @param WG
     *            nd x nm design matrix relating data vector to model
     *            parameters, weighted as in WG = W*G where W=chol(inv(dcov)).
     *            This is a Green's function.
     * 
     * @param nstat
     *            nnet x 1 vector giving the number of stations in each network
     *            being used.
     * @param first_j
     *            nnet x 1 vector giving the index in the data vector of the
     *            first datum corresponding to each network
     * @param ncomps
     *            nnet x 1 vector giving the number of data components
     *            associated with a station for the data type of each network.
     *            For GPS this is typically 3 (east, north, up).
     * @param S
     *            Smoothing matrix (e.g., finite difference Laplacian). S can be
     *            null or have zero lenght (for example if smoothing has already
     *            been built into WD and WG).
     * 
     * @param Do
     *            column vector of pseudodata with length equal to first
     *            dimension of S. Typically all zeroes; will be created as
     *            zeroes if null is passed in its place. Do can be null or have
     *            zero length (for example if smoothing has already been built
     *            into WD and WG).
     * 
     * @param lsqlin_input
     *            a slip-solver that holds constraints. Typically this object
     *            will have been constructed by calling
     *            EqualityAndBoundsSlipSolver(int nm).
     * 
     * @param progDlg
     *            Dialog box that receives updates and displays them to the user
     *            during the long calculation. This was not part of the MATLAB
     *            function.
     * 
     */
    public CrossVal(double[] gam, double[] WD, double[][] WG, 
            int[] nstat, int[] first_j, int[] ncomps, 
            double[][] S, double[] Do, ConstrainedLinearLeastSquaresSolver lsqlin_input,
            CrossValidationProgressDlg progDlg) {
        this.S = S;
        this.greens = WG;
        this.progDlg = progDlg;
        
        int ng = gam.length; /* The number of smoothing params to try */
        nm = WG[0].length;
        int nd = WG.length;
        if (nd != WD.length)
            throw new SolverException("Measured data and Green's function " +
            		"have different numbers of rows");
        minCVSS = Double.MAX_VALUE; 
        
        
        
        int[][] I = statdataI(nstat, first_j, ncomps);
        
        // MATLAB: nstat_tot = sum(nstat);
        int nstat_tot = 0;
        for (int net = 0; net < nstat.length; net++)
            nstat_tot += nstat[net];
        
        CVSS = new double[ng];
        res = new double[nstat_tot][ng];
        solution_CV = new double[nstat_tot][ng][nm];

        for (int i = 0; i < ng; i++) {
            for (int dat = 0; dat < nstat_tot; dat++) {
                
                /* Leave out one site's or line's data; 
                 * extract necessary part of resampled kernel:   */
                int ind1 = I[0][dat];
                int ind2 = I[1][dat];
                
                // MATLAB: WDCV=WD;
                int numIncldData = nd - (ind2 - ind1 + 1);
                double[] WDCV = new double[numIncldData];
                int offset = 0; /* shading offset */
                // MATLAB: WGCV=WG;
                double[][] WGCV = new double[numIncldData][nm];
                /* "Shade" the Weighted Data and Green's matrices by 
                 * copying them while skipping a station: */ 
                for (int rowAfterShading = 0; rowAfterShading < numIncldData; 
                            rowAfterShading++) {
                    // MATLAB: WDCV(ind1:ind2)=[];
                    if (rowAfterShading == ind1)
                        offset = ind2 - ind1 + 1;
                    WDCV[rowAfterShading] = WD[rowAfterShading + offset];
                    
                    // MATLAB: WGCV(ind1:ind2,:)=[];
                    for (int col = 0; col < nm; col++)
                        WGCV[rowAfterShading][col] = WG[rowAfterShading + offset][col];
                }
                

                // Incorporate smoothing:
                
                double[] DCV = new double[WDCV.length + Do.length];
                double[][] GCV = new double[WDCV.length + Do.length][WGCV[0].length];

                /* Copy the real-data equations into the 
                 * first part of the matrices: */
                for (int row = 0; row < WDCV.length; row++) {
                    // MATLAB: DCV=[WDCV;Do]; (1st half)
                    DCV[row] = WDCV[row];
                    
                    // MATLAB: GCV=[WGCV; (1/gam(i))*S]; (1st half)
                    for (int col = 0; col < nm; col++)
                        GCV[row][col] = WGCV[row][col]; 
                }
                
                maybeAppendSmoothingEquations(WDCV, Do, DCV, GCV, S, gam, i, nm);
                divideSmoothingByGamma(GCV, DCV, gam[i]);
                
                // MATLAB: solution_CV{dat,i} = lsqlin(GCV, DCV, lsqlin_input{:});
                lsqlin_input.setGreensFunction(GCV);
                lsqlin_input.setMeasuredDisplacements(DCV);
                solution_CV[dat][i] = lsqlin_input.solve();
                
                // Get residual vector for station left out:
                
                // MATLAB: Wdhat = WG(ind1:ind2,:)*solution_CV{dat,i};
                final int numExcludedRows = ind2 - ind1 + 1;
                int excludedSubRow = 0; /* Index of excluded rows, starting at zero */
                double[] Wdhat = new double[numExcludedRows];
                for (int row = ind1; row <= ind2; row++, excludedSubRow++)
                    for (int col = 0; col < nm; col++) 
                        Wdhat[excludedSubRow] += WG[row][col] * 
                                solution_CV[dat][i][col];
                
                // MATLAB: Wr = WD(ind1:ind2)-Wdhat;
                double[] Wr = new double[numExcludedRows]; /* The residual */ 
                excludedSubRow = 0;
                for (int row = ind1; row <= ind2; row++, excludedSubRow++)
                    Wr[excludedSubRow] = WD[row] - Wdhat[excludedSubRow];

                // MATLAB: res(dat,i) = Wr'*Wr;
                for (excludedSubRow = 0; excludedSubRow < numExcludedRows; 
                        excludedSubRow++)
                    res[dat][i] += Wr[excludedSubRow] * Wr[excludedSubRow];                

                // MATLAB: CVSS(i,1) = sum(res(:,i))/nstat_tot; (1st part)
                CVSS[i] += res[dat][i];
                
                if (progDlg != null && dat%3 == 0) // Display frequent, intermediate results to show activity exists
                    progDlg.update(new CrossValProgressStats(res[dat][i], -1, gam[i]));

            } // loop over sites / lines / angles  (indexed by dat)
            
            // MATLAB: CVSS(i,1) = sum(res(:,i))/nstat_tot; (2nd part)
            CVSS[i] /= nstat_tot;
            
            ////DEBUG
            System.out.print("CVSS:" + CVSS[i]);
            System.out.println("  gam:" + gam[i] );
            
            // MATLAB: minCVSS=min(CVSS);
            if (minCVSS > CVSS[i]) {
                minCVSS = CVSS[i];
                optGamma = gam[i];
            }
            if (progDlg != null)
                progDlg.update(new CrossValProgressStats(CVSS[i], i, gam[i]));
        } // loop over gamma (indexed by i)
        
    } // Constructor that does the work and takes all possible params

    

    /** Divides the bottom square submatrix and corresponding RHS elements,
     * to adjust the importance of those smoothing equations when solving the 
     * over-determined system of equations. */
    private void divideSmoothingByGamma(double[][] GCV, double[] DCV, double gamma) {
        if (DCV.length != GCV.length)
            throw new SolverException("GCV and Do did not have " +
            		"the same number of rows");
        
        int startRow = GCV.length - GCV[0].length;
        for (int row = startRow; row < GCV.length; row++) {
            DCV[row] = DCV[row] / gamma;
            for (int col = 0; col < GCV[0].length; col++)
                GCV[row][col] = GCV[row][col] / gamma;
        }
    }

    /**
     * Creates an object from simplified parameters, assuming a single network
     * and that smoothing is already factored into the Green's function. 
     * 
     * <p>
     * A static, factory-like method that makes assumptions. 
     * Assumes:
     * <ul>
     * <li>Smoothing has already been factored into the Green's function, WG,
     * and the displacement data, WD, which are passed in.
     * <li>All stations are in one network and they all have 3 comps
     * (measurements per station, XyzDisplacement.AXES), for example, X, Y, and
     * Z displacement by GPS stations.
     * </ul>
     * 
     * <p>
     * This differs from the constructor in that it does not receive:
     * <ul>
     * <li> int[] nstat - vector of number of stations in each network 
     * <li> int[] first_j - indices of first data for each network
     * <li> int[] ncomps - Number of measurements per station
     * <li> double[][] S - Smoothing rows
     * <li> double[] Do - pseudodata, typically zeros
     * </ul>
     * 
     * @param gam
     *            Vector of candidate values for smoothing parameter. The
     *            Laplacian gets weighted by (1/gam[i]) to be consistent with
     *            the weighting of the data and G. Done this way, low gamma
     *            values will produce smooth solutions and high gamma will
     *            produce rough solutions, as would be expected when gamma^2 is
     *            taken to be the variance of the smoothing constraints.
     * 
     * @param WD
     *            nd x 1 data vector weighted by chol(inv(dcov)) where dcov is
     *            the data covariance. If multiple station networks are used,
     *            all data for stations of a given network are found as a block
     *            in this vector. If a station has multiple components of data
     *            (e.g., east, north, and up), these are found as a block for
     *            that station in the vector. E.g., for two networks, the first
     *            of which has j stations with E, N, U and the second of which
     *            has k stations of E, and N data the data vector would look
     *            like this:
     * 
     *            <ul>
     *            <li>[E11; N11; U11; E12; N12; U12; ... E1j; N1j; U1j; E21;
     *            N21; E22; N22; ... E2k; N2k]
     *            </ul>
     * 
     * 
     * @param WG
     *            nd x nm design matrix relating data vector to model
     *            parameters, weighted as in WG = W*G where W=chol(inv(dcov)).
     *            This is a Green's function.
     * 
     * 
     * @param lsqlin_input
     *            a slip-solver that holds constraints. Typically this object
     *            will have been constructed by calling
     *            EqualityAndBoundsSlipSolver(int nm).
     *
     * @param progDlg
     *            Dialog box that receives updates and displays them to the user
     *            during the long calculation. This was not part of the MATLAB
     *            function.

     */
    public static CrossVal makeFromSimpleParams(double[] gam, int numStations, 
            double[] WD, double[][] WG, 
            ConstrainedLinearLeastSquaresSolver lsqlin_input,
            CrossValidationProgressDlg progDlg) {
        int [] nstat = new int[1];
        nstat[0] = numStations;
        int [] first_j = {0};
        int [] ncomps  = {(WG.length - WG[0].length) / numStations}; 
        
        return new CrossVal(gam, WD, WG, nstat, first_j, ncomps, 
                new double[][] {{}} /*S*/, new double[] {} /*Do*/, 
                lsqlin_input, progDlg);
    }

    /**
     * Append smoothing equations (rows) at the bottoms of data and Green's
     * function, if there are any.
     * 
     * The parameters are those originally passed to the fully parameterized 
     * constructor.
     */
    private void maybeAppendSmoothingEquations(double[] WDCV, double[] Do,
            double[] DCV, double[][] GCV, double[][] S, 
            double[] gam, int i, int nm) {
        if (Do == null || Do.length == 0)
            if (S == null || S.length == 0 || S[0].length == 0)
                return;
            else
                throw new SolverException("Do was null or empty, but not S");
        
        int smoothingSubRow = 0;
        for (int row = WDCV.length; row < WDCV.length + Do.length; row++) {
            // MATLAB: DCV=[WDCV;Do]; (2nd half)
            DCV[row] = Do[smoothingSubRow];
            
            // MATLAB: GCV=[WGCV; (1/gam(i))*S]; (2nd half)
            // In the following loop, the G matrices are weighted by the Cholesky
            // factorization of the inverse covariance matrix.  The 
            // Laplacian is weighted by (1/gam) to be consistent with
            // the weighting of the data and G.  If it is done this way, 
            // then low gamma will produce smooth solutions and high gamma 
            // will produce rough solutions, as would be expected when 
            // gamma^2 is taken to be the variance of the smoothing 
            // constraints.
            for (int col = 0; col < nm; col++)
                GCV[row][col] = S[smoothingSubRow][col];
            smoothingSubRow++;
        }

        if (smoothingSubRow != S.length)
            throw new SolverException("smoothingSubRow != S.length");
       }


    /** See ehz_gps/trunk/modeling/matlab/dismodel/inversion/statdataI.m's 
     * comments.
     * 
     * nnet = number of different data networks; these may correspond to
     *      different data types (e.g., GPS, strain, etc.)
     * nstat_tot = sum(nstat)
     * 
     * @param nstat nnet x 1 vector giving the number of stations in each network
     *           being used.
     * @param firstJ nnet x 1 vector giving the index in the data vector of the
     *           first datum corresponding to each network
     * @param ncomps  nnet x 1 vector giving the number of data components associated
     *           with a station for the data type of each network.  For GPS this
     *           is typically 3 (east, north, up).
     * @return 2 x nstat_tot matrix whose first row gives the index of the 
     *           corresponding station's first entry in the data vector and
     *           whose second row gives the index of that station's last entry
     *           in the data vector.
     */
    protected int[][] statdataI(int[] nstat, int[] firstJ, int[] ncomps) {
        int nstat_tot = 0;
        for (int net = 0; net < nstat.length; net++)
            nstat_tot += nstat[net];
        int[][] retAry = new int[2][nstat_tot];
        int station = 0;
        int axis = 0; // Index into list of all stations' axes
        /* Loop over the networks being used; this could potentially include using
         * data from multiple GPS networks or data from one or more GPS networks
         * used together with data from one or more strainmeter networks. */
        for (int net = 0; net < nstat.length; net++) {
            for (int stationOfNet = 0; stationOfNet < nstat[net]; stationOfNet++) {
                retAry[0][station] = axis;
                axis += ncomps[net];
                retAry[1][station++] = axis - 1;
            }
        }
        return retAry;
    }

    /**
     * @return the nstat_tot x ng x nm array of solution vectors from CV runs
     */
    public double[][][] getSolution_CV() {
        return solution_CV;
    }

    /**
     * @return the nstat x ng matrix of weighted sums of squared residuals for 
     *          the omitted station
     */
    public double[][] getRes() {
        return res;
    }

    /**
     * @return the minimum Sum of Squares of the residual for the optimal gamma.
     */
    public double getMinCVSS() {
        return minCVSS;
    }

    /**
     * @return the best value of smoothing parameter based on CV
     */
    public double getOptGamma() {
        return optGamma;
    }

    /**
     * @return the ng x 1 vector of CVSS for each value in input gam
     */
    public double[] getCVSS() {
        return CVSS;
    }
    
    
    /** Returns a copy of the Green's matrix, whose smoothing rows have 
     * been divided by the optimal gamma previously discovered.     */
    public double[][] getOptGreens() {
        /* Is S, the smoothing matrix, explicit yet? (It may or may not have
         * been passed in separately.) */
        if (S == null || S.length == 0 || S[0].length == 0) {
            /* Create an S whose rows reference the appropriate
             * rows of the Green's matrix. */
            S = new double[nm][];
            for (int localRow = 0; localRow < nm; localRow++)
                S[localRow] = greens[localRow + greens.length - nm];
        }
        double[][] greensCopy = new double[greens.length][nm];
        for (int row = 0; row < greens.length; row++)
            for (int col = 0; col < S[0].length; col++)
                if (row < greens.length - nm)
                    greensCopy[row][col] = greens[row][col];
                else
                    greensCopy[row][col] = S[row - (greens.length - nm)][col] / optGamma;
        return greensCopy;    
    }
    
}
