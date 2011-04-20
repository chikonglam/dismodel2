package gov.usgs.dismodel.calc.inversion;

import java.util.ArrayList;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;

/**
 * Generate a Laplacian Smoothing matrix from a distributed fault
 * @author clam-PR
 *
 */
public class SmoothingLaplacian {
    
	 /**
	  * Batch laplacian Matrix generator
	 * @param faultIn group of connected sorted subfaults
	 * @param breakSurface
	 * @param dikeOpening
	 * @param totalNumOfSubFaults
	 * @return Laplacian matrix (the same order as the one sent in) 
	 */
	public static double [][] generate(ArrayList<ArrayList<DisplacementSolver>> twoDArrayFaults, boolean breakSurface, boolean dikeOpening, int totalNumOfSubFaults){ //, int totalCol, int totalRow){//, int segmentCt, int totalCol, int totalRow, int[] segmentColCt, int[] segmentRowCt){
	    int noOfGps = twoDArrayFaults.size();
		double [][][] lapacianArray = new double[noOfGps][][];
		
		double [][] combinedMatrix = new double[totalNumOfSubFaults][totalNumOfSubFaults];
		
		int[] totalColArray = new int[noOfGps];
		int[] totalRowArray = new int[noOfGps];
		int[][] segmentColCtArray = new int[noOfGps][];
		int[][] segmentRowCtArray = new int[noOfGps][];
		int[] numSubFaults = new int[noOfGps];
		
		int cumComboMatrixStart = 0;
		
		
		for (int groupIter = 0; groupIter < noOfGps; groupIter++ ){
			ArrayList<DisplacementSolver> curGroup = twoDArrayFaults.get(groupIter);
			
			int segmentCt = curGroup.size();
	    	int curRowCt = 0;
	    	int curColCt = 0;
	    	int rowCtCumSum = 0;
	    	int colCtCumSum = 0;
	    	int[] segmentSubFaultCt = new int[segmentCt];
	    	
	    	segmentColCtArray[groupIter] = new int[segmentCt];
	    	segmentRowCtArray[groupIter] = new int[segmentCt];
			
	    	for (int segmentIter = 0; segmentIter < segmentCt; segmentIter++){
	    		DistributedFault curSegment = (DistributedFault) curGroup.get(segmentIter);
	    		curRowCt = curSegment.getRowCt();
	    		curColCt = curSegment.getColCt();
	    		segmentRowCtArray[groupIter][segmentIter] = curRowCt;
	    		segmentColCtArray[groupIter][segmentIter] = curColCt;
	    		rowCtCumSum += curRowCt;
	    		colCtCumSum += curColCt;
	    		segmentSubFaultCt[segmentIter] = curRowCt * curColCt;
	    		
	    	}
	       	numSubFaults[groupIter] = curRowCt * colCtCumSum;
	    	
	    	totalRowArray[groupIter] = curRowCt;
	    	totalColArray[groupIter] = colCtCumSum;
	    	
	    	//get the individual smoothing matrices
	    	double[][] preArrangeMatrix = SmoothingLaplacian.generate(curGroup.toArray(new DisplacementSolver[0]), 
	    			breakSurface, dikeOpening, segmentCt, totalColArray[groupIter], totalRowArray[groupIter], segmentColCtArray[groupIter], segmentRowCtArray[groupIter]);
	    	
	    	
	    	lapacianArray[groupIter] = rolCol2SegRolCol(preArrangeMatrix,totalRowArray[groupIter], totalColArray[groupIter], segmentRowCtArray[groupIter],segmentColCtArray[groupIter], segmentSubFaultCt);
	    	
	    	//combine the separate matricies into 1
	    	for (int rowIter = 0; rowIter < numSubFaults[groupIter]; rowIter++  ){
	    		double [] curRow = lapacianArray[groupIter][rowIter];
	    		for (int colIter = 0; colIter < numSubFaults[groupIter]; colIter++ ){
	    			combinedMatrix[cumComboMatrixStart+ rowIter][cumComboMatrixStart + colIter] = curRow[colIter];
	    		}
	    		
	    	}
	    	cumComboMatrixStart += numSubFaults[groupIter];		//adjust the start position for the next group
	    	
		}
		
		return combinedMatrix;
	} 

	
	/**
     * Laplacian Matrix generator
     * @param faultIn
     * @param breakSurface
     * @param dikeOpening
     * @return Laplacian matrix (the order of this matrix is the same as the order in DistributedFault.subfaults)
     * @see DistributedFault
     */
    public static double [][] generate(DisplacementSolver[] faultIn, 
            boolean breakSurface, boolean dikeOpening, int segmentCt, int totalCol, int totalRow, int[] segmentColCt, int[] segmentRowCt){
        SmoothingLaplacian sl = new SmoothingLaplacian(faultIn, breakSurface, dikeOpening, segmentCt, totalCol, totalRow, segmentColCt, segmentRowCt);
        return sl.getSmoothingMatrix();
    }
    
    private double[][] smoothingMatrix;
    
    private double[][] t1h, t2h, t3h, d, dp1, dm1, dp4, dm4;
    private int colCt, rowCt, segmentCt;
    private int[] subFaultColCt;
    private int[] subFaultRowCt;
    
    private static final class INDEX{
        public final static int X = 0;
        public final static int Y = 1;
        public final static int Z = 2;
    }
    
    private static double[][] rolCol2SegRolCol(double[][] preArrangeMatrix, int totalRowCt, int totalColCt, int[] segmentRowCtArray, int[] segmentColCtArray, int[] segmentSubFaultCt){
    	int numOfSubfaults = totalRowCt * totalColCt;
    	int [] destIdx = genSegRolColIdxArray(numOfSubfaults ,totalRowCt, totalColCt, segmentRowCtArray, segmentColCtArray, segmentSubFaultCt);
    	
    	//switch the row first
    	double[][] colSwitchedMatrix = new double [numOfSubfaults][numOfSubfaults];
    	for (int iter = 0; iter < numOfSubfaults; iter++){
    		int curIndex = destIdx[iter];
    		colSwitchedMatrix[curIndex] = preArrangeMatrix[iter];
    	}

    	//now switch the cols
    	double[][] colRolSwitchedMatrix = new double [numOfSubfaults][numOfSubfaults];
    	for (int rowIter = 0; rowIter < numOfSubfaults; rowIter++){
    		double[] curSrcRow = colSwitchedMatrix[rowIter];
    		double[] curDesRow = colRolSwitchedMatrix[rowIter];
    		for (int colIter = 0; colIter < numOfSubfaults; colIter++){
    			int curIndex = destIdx[colIter];
    			curDesRow[curIndex] = curSrcRow[colIter];
    		}
    	}
    	
    	return colRolSwitchedMatrix;
    }
    
    
    
    
    private static int[] genSegRolColIdxArray(int numOfSubfaults,
			int totalRowCt, int totalColCt, int[] segmentRowCtArray,
			int[] segmentColCtArray, int[] segmentSubFaultCt) {
    	int numOfSegs = segmentRowCtArray.length;
    	int curCumSum = 0;
    	int [] subFaultCumSum = new int[numOfSegs];
    	for (int segIter = 0; segIter < numOfSegs; segIter++){
    		curCumSum += segmentSubFaultCt[segIter];
    		subFaultCumSum[segIter] = curCumSum;
    	}
    	
    	SegRolColIdx[][][] segRowCol = new SegRolColIdx[numOfSegs][][];
    	for (int segIter = 0; segIter < numOfSegs; segIter++){
    		int curRowCt = segmentRowCtArray[segIter];
    		SegRolColIdx[][] curSeg = new SegRolColIdx[curRowCt][];
    		segRowCol[segIter] = curSeg;
    		for(int rowIter = 0; rowIter < curRowCt; rowIter++){
    			int curColCt = segmentColCtArray[segIter];
    			SegRolColIdx[] curCol = new SegRolColIdx[curColCt];
    			curSeg[rowIter] = curCol;
    			for(int colIter = 0; colIter < curColCt; colIter++){
    				curCol[colIter] = new SegRolColIdx(segIter, rowIter, colIter);
    			}
    			
    		}
    	}
    	
    	SegRolColIdx[] oneDIdx  = to1DByRows( extractIntoPatchArray(segRowCol) );
    	int [] intIdx = new int[numOfSubfaults];
    	for (int iter = 0; iter < numOfSubfaults; iter++){
    		SegRolColIdx curIdx = oneDIdx[iter];
    		int accumIndex = 0;
    		int curSeg = curIdx.segment;
    		if (curSeg > 0) accumIndex += subFaultCumSum[curSeg - 1];
    		int curRow = curIdx.row;
    		accumIndex += (segmentColCtArray[curSeg]) * curRow;
    		int curCol = curIdx.col;
    		accumIndex += curCol;
    		
    		intIdx[iter] = accumIndex;
    	}
    	
    	
		return intIdx;
		
	}




	private static class SegRolColIdx{
    	public int segment;
    	public int row;
    	public int col;
    	
    	public SegRolColIdx(){
    		this(-1, -1, -1);
    	}
    	
		public SegRolColIdx(int segment, int row, int col) {
			super();
			this.segment = segment;
			this.row = row;
			this.col = col;
		}

		@Override
		public String toString() {
			return "SegRolColIdx [seg=" + segment + ", row=" + row
					+ ", col=" + col + "]";
		}

		
    	
    }
    
    private SmoothingLaplacian(DisplacementSolver[] faultIn, boolean breakSurface, boolean dikeOpening, int segmentCt, int totalCol, int totalRow, int[] segmentColCt, int[] segmentRowCt){
    	colCt = totalCol;
    	rowCt = totalRow;
    	this.segmentCt = segmentCt;
    	
        int endCol = colCt -1;          
        int endRow = rowCt -1;
        this.subFaultColCt = segmentColCt;
        this.subFaultRowCt = segmentRowCt;
        
        OkadaFault3[][] allSubFaults = extractIntoPatchArray(faultIn);
        
//        % COMPUTE INCREMENTS ******************************************************

        //% Vertical increments
        double vertDist = distanceBetween(allSubFaults[0][0],allSubFaults[1][0]);
        t2h = newArrayWithVal( vertDist, rowCt, colCt );
        
        //% Horizontal increments
        //% Left edge (first column)
        t3h = new double[rowCt][colCt];
        double leftHoriDist = distanceBetween(allSubFaults[0][0],allSubFaults[0][1]);
        fillArrayRange(t3h, leftHoriDist, 0, endRow, 0, 0);
        t1h = new double[rowCt][colCt];
        fillArrayRange(t1h, leftHoriDist, 0, endRow, 0, 0);
        //% Right edge (last column)
        double rightHoriDist = distanceBetween(allSubFaults[0][endCol],allSubFaults[0][endCol-1]);
        fillArrayRange(t1h, rightHoriDist, 0, endRow, endCol, endCol);
        fillArrayRange(t3h, rightHoriDist, 0, endRow, endCol, endCol);
        
        //% Central columns
        for (int colIter = 1; colIter < endCol; colIter++){
            double horiLeftDist = distanceBetween(allSubFaults[0][colIter-1],allSubFaults[0][colIter]);
            fillArrayRange(t1h, horiLeftDist, 0, endRow, colIter, colIter);
            double horiRightDist = distanceBetween(allSubFaults[0][colIter+1],allSubFaults[0][colIter]);
            fillArrayRange(t3h, horiRightDist, 0, endRow, colIter, colIter);
        }
        
        
//        % COMPUTE SMOOTHING OPERATOR D ********************************************
//
//        % 1) toggle=[0 0], fault does not break the surface and no dike intrusion                  
//        % compute central diagonal (0)
            d = new double[rowCt][colCt];
            visitMatrix(d, dNoBreak, 0, endRow, 0, endCol);
//        % compute diagonal (+1)
            dp1 = new double[rowCt][colCt];
            visitMatrix(dp1, dp1NoBreak, 0, endRow-1, 0, endCol);
            fillArrayRange(dp1, 0d, endRow, endRow, 0, endCol);
//        % compute diagonal (-1)
            dm1 = new double[rowCt][colCt];
            visitMatrix(dm1, dm1NoBreak, 1, endRow, 0, endCol);
            fillArrayRange(dm1, 0d, 0, 0, 1, endCol);
//        % compute diagonal (+P)  
            dp4 = new double[rowCt][colCt];
            visitMatrix(dp4, dp4NoBreak, 0, endRow, 0, endCol-1);
//            % compute diagonal (-P) 
            dm4 = new double[rowCt][colCt];
            visitMatrix(dm4, dm4NoBreak, 0, endRow, 1, endCol);
            
//            % 2) top =1, fault break the surface
            if (breakSurface){
//                % central diagonal (0)
                visitMatrix(d, dTopBreak, 0, 0, 0, endCol); 
//                diagonal (+1)
                visitMatrix(dp1, dp1TopBreak, 0, 0, 0, endCol);
//                % compute diagonal (-1)
                fillArrayRange(dm1, 0d, 0, 0, 1, endCol);
//                % compute diagonal (+P) 
                fillArrayRange(dp4, 0d, 0, 0, 0, endCol-1);
//                % compute diagonal (-P) 
                fillArrayRange(dm4, 0d, 0, 0, 1, endCol);
            }
            
            if(dikeOpening){
//                % central diagonal (0)
                visitMatrix(d, dBottomOpen, endRow, endRow, 0, endCol);
//                % compute diagonal (+1)
                fillArrayRange(dp1, 0d, endRow, endRow, 0, endCol-1);
//                % diagonal (-1)
                visitMatrix(dm1, dm1BottomOpen, endRow, endRow, 0, endCol);
//                % compute diagonal (+P) 
                fillArrayRange(dp4, 0d, endRow, endRow, 0, endCol-1);
//                % compute diagonal (-P) 
                fillArrayRange(dm4, 0d, endRow, endRow, 1, endCol);

            }
            
            //% smoothing operator
            double [][] diagD = diag(d, 0);
            double [][] diagDp1 = diag(dp1, 1);
            double [][] diagDm1 = diag(dm1, -1);
            double [][] diagDp4 = diag(dp4, rowCt);
            double [][] diagDm4 = diag(dm4, -rowCt);
            
            double [][] sum12 = matrixAdd(diagD, diagDp1);
            double [][] sum123 = matrixAdd(sum12, diagDm1);
            double [][] sum1234 = matrixAdd(sum123, diagDp4);
            double [][] bigDMatrix = matrixAdd(sum1234, diagDm4);
            
            smoothingMatrix = bigDMatrix;
            
    }
    
    private OkadaFault3[][] extractIntoPatchArray(DisplacementSolver[] faultsIn){
    	OkadaFault3[][] ret = new OkadaFault3[rowCt][colCt];
    	
    	int startingCol = 0;
    	for (int segmentIter = 0; segmentIter < segmentCt; segmentIter++){
    		fillDistFaultIntoArray(ret, (DistributedFault)faultsIn[segmentIter], startingCol);
    		startingCol += subFaultColCt[segmentIter];
    	}
    	
    	return ret;
    	
    }
    
    private static SegRolColIdx[][] extractIntoPatchArray(final SegRolColIdx[][][] faultsIn){
    	final int numOfRows = faultsIn[0].length;
    	final int numOfSegs = faultsIn.length;
    	final int[] numOfCols  = new int[numOfSegs];
    	int totalNumOfCols = 0;
    	
    	for (int segIter = 0; segIter < numOfSegs; segIter++){
    		int curNumOfCol = faultsIn[segIter][0].length;
    		numOfCols[segIter] = curNumOfCol;
    		totalNumOfCols += curNumOfCol;
    	}
    	
    	SegRolColIdx[][] patchArray = new SegRolColIdx[numOfRows][totalNumOfCols];
    	
    	
    	int colOffset = 0;
    	for (int segIter = 0; segIter < numOfSegs; segIter++){
    		for (int rowIter = 0; rowIter < numOfRows; rowIter++){
    			SegRolColIdx[] curSrcRow = faultsIn[segIter][rowIter];
    			SegRolColIdx[] curDesRow = patchArray[rowIter];
    			for (int colIter = 0; colIter < numOfCols[segIter]; colIter++){
    				curDesRow[colOffset + colIter] = curSrcRow [colIter]; 
    			}
    		}
    		colOffset += numOfCols[segIter];
    	}
    	
    	
    	return patchArray;
    }
    
    private static SegRolColIdx[] to1DByRows(SegRolColIdx[][] faultsIn){
    	final int numOfRows = faultsIn.length;
    	final int numOfCols = faultsIn[0].length;
    	final int numOfPatches = numOfRows * numOfCols;
    	
    	SegRolColIdx[] oneDMatrix = new  SegRolColIdx[numOfPatches];
    	for (int rowIter = 0; rowIter < numOfRows; rowIter++){
    		SegRolColIdx[] curRow = faultsIn[rowIter];
    		for (int colIter = 0; colIter < numOfCols; colIter++){
				int destIdx = colIter * numOfRows + rowIter;
				oneDMatrix[destIdx] = curRow[colIter];
			}
		}

		return oneDMatrix;
    }
    
    
    
    
    
    private void fillDistFaultIntoArray(OkadaFault3[][] arrayToFill, DistributedFault curFault, int startingCol) {
    	int curFaultColCt = curFault.getColCt();
    	
    	for (int rowIter = 0; rowIter < rowCt; rowIter++){
    		for (int colIter =0; colIter < curFaultColCt; colIter++){
    			arrayToFill[rowIter][colIter+startingCol] = curFault.getSubfaults()[rowIter][colIter]; 
    		}
    	}
	}

	private static double[][] colTo2DMatrix(double[][] lapacianCol, int rowCt, int colCt) {
        int inRowCt = lapacianCol.length;
        
        double [][] TwoDMatrix = new double[rowCt][colCt];
        
        for (int inRowIter = 0; inRowIter < inRowCt; inRowIter++){
            int outColIter = inRowIter / rowCt;
            int outRowIter = inRowIter % rowCt;
            TwoDMatrix[outRowIter][outColIter] = lapacianCol[inRowIter][0];
        }
        return TwoDMatrix;
    }

    protected static double[][] getSlipCol(OkadaFault3[][] subfaults){
        int rowCt = subfaults.length;
        int colCt = subfaults[0].length;
        
        int subFaultCt = rowCt * colCt;
        
        double [][] retMatrix = new double[subFaultCt][1];
        int outIter = 0;
        for (int colIter =0; colIter < colCt; colIter++){
            for (int rowIter = 0; rowIter < rowCt; rowIter++){
                retMatrix[outIter][0] = subfaults[rowIter][colIter].getMagnitude(); 
                outIter++;
            }
        }
        
        return retMatrix;
    }
    
    protected static double[][] matrixAdd(final double[][] a, final double[][] b){
        final int rowCt = a.length;
        final int colCt = a[0].length;
        double [][] tempOut = new double[rowCt][colCt];
        for (int rowIter = (rowCt-1); rowIter >= 0; rowIter--){
            for (int colIter = (colCt-1); colIter >= 0; colIter--){
                tempOut[rowIter][colIter] = a[rowIter][colIter] + b[rowIter][colIter];
            }
        }
        return tempOut;
    }
    
    protected static double[][] matrixMultiply(final double[][] a,  final double[][] b){
        final int m = a.length;
        final int n = a[0].length;
        final int p = b.length;
        final int q = b[0].length;
        if (n != p) return (null);
        double[][] c = new double[m][q]; 
        final double[] bColJ = new double[p];
        for (int j = 0; j < q; j++) {
            for (int k = 0; k < p; k++) {
                bColJ[k] = b[k][j];
            }
            for (int i = 0; i < m; i++) {
                final double[] aRowI = a[i];
                double s = 0;
                for (int k = 0; k < p; k++) {
                    s += aRowI[k] * bColJ[k];
                }
                c[i][j] = s;
            }
        }
        return(c);
    }

    
    private static double[][] diag(double[][] arrayIn, int diagRowsUpRight){
        int rowCt = arrayIn.length;
        int colCt = arrayIn[0].length;
        int diagDim = rowCt * colCt;
        
        int outRowIter = 0;
        int outColIter = 0;
        int inArrayIter = 0;
        
        if (diagRowsUpRight > 0){
            outColIter += diagRowsUpRight;
        } else if (diagRowsUpRight < 0){
            outRowIter -= diagRowsUpRight;
            inArrayIter -= diagRowsUpRight;
        }
        
        double[][] outArray = new double[diagDim][diagDim];
        while (inArrayIter < diagDim && outRowIter < diagDim && outColIter < diagDim){
            int inColIter = inArrayIter / rowCt;
            int inRowIter = inArrayIter % rowCt;
            outArray[outRowIter][outColIter] = arrayIn[inRowIter][inColIter];
            inArrayIter++;
            outRowIter++;
            outColIter++;
        }
        
        return outArray;
        
    }
    
    private double[][] newArrayWithVal(double value, int rowCt, int colCt){
        double [][] filledArray = new double[rowCt][colCt];
        for (int rowIter = 0; rowIter < rowCt; rowIter++){
            for (int colIter =0; colIter < colCt; colIter++){
                filledArray[rowIter][colIter] = value;
            }
        }
        return filledArray;
    }
    
    private void fillArrayRange(double [][] array, double value, 
            int startRow, int endRow, int startCol, int endCol){
        for (int rowIter = startRow; rowIter <= endRow; rowIter++){
            for (int colIter = startCol; colIter <= endCol; colIter++){
                array[rowIter][colIter] = value;
            }
        }
    }
    
    private double distanceBetween(OkadaFault3 fault1, OkadaFault3 fault2){
        double[] fault1MidCoords = fault1.getCenterENU();
        double[] fault2MidCoords = fault2.getCenterENU();
        double x1 = fault1MidCoords[INDEX.X];
        double y1 = fault1MidCoords[INDEX.Y];
        double z1 = fault1MidCoords[INDEX.Z];
        double x2 = fault2MidCoords[INDEX.X];
        double y2 = fault2MidCoords[INDEX.Y];
        double z2 = fault2MidCoords[INDEX.Z];
        
        double xDist = x2 - x1;
        double yDist = y2 - y1;
        double zDist = z2 - z1;
        
        double dist = Math.sqrt(xDist*xDist + yDist*yDist + zDist*zDist);
        return dist;
    }
    
    private double[][] getSmoothingMatrix() {
        return smoothingMatrix;
    }

    //helper
    //--------------
    private void visitMatrix(double[][] matrix, matrixVisitor visitor, 
            int startRow, int endRow, int startCol, int endCol){
        for (int rowIter = startRow; rowIter <= endRow; rowIter++){
            for (int colIter = startCol; colIter <= endCol; colIter++){
                matrix[rowIter][colIter] = visitor.visitor(matrix, rowIter, colIter);
            }
        }   
    }
    
    private abstract class matrixVisitor{
        public abstract double visitor(double[][] matrix, int row, int col);
    }
    
    private final matrixVisitor dNoBreak = new matrixVisitor() {
        
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT1h = t1h[row][col];
            double curT2h = t2h[row][col];
            double curT3h = t3h[row][col];
            
            double retVal = ((-(2d)) * ((1d / (curT1h * curT3h)) + (1d / (Math.pow(curT2h, 2d)))));
            return retVal;
        }
    };
    
    private final matrixVisitor dp1NoBreak = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double retNum = Math.pow(t2h[row][col], -2d);
            return retNum;
        }
    };
    
    private final matrixVisitor dm1NoBreak = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double retNum = Math.pow(t2h[row][col], -2d);
            return retNum;
        }
    };
    
    private final matrixVisitor dp4NoBreak = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT3H = t3h[row][col];
            double curT1H = t1h[row][col];
            
            double dp4 = 2d / ( curT3H * (curT1H + curT3H));
            return dp4;
        }
    };
    
    private final matrixVisitor dm4NoBreak = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT1h = t1h[row][col];
            double curT3h = t3h[row][col];
           
            double dm4 = 2d /(curT1h *( curT1h + curT3h));
            return dm4;
        }
    }    ;
    
    private final matrixVisitor dTopBreak = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT2h = t2h[row][col];
            double d = 1d / (curT2h);
            return d;
        }
    };   
    
    private final matrixVisitor dp1TopBreak = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT2h = t2h[row][col];
            double dp1 = -1d / (curT2h);
            return dp1;
        }
    };   
    
    private final matrixVisitor dBottomOpen = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT2h = t2h[row][col];
            double d = 1d / (curT2h);
            return d;
        }
    };   
    
    private final matrixVisitor dm1BottomOpen = new matrixVisitor() {
        @Override
        public double visitor(double[][] matrix, int row, int col) {
            double curT2h = t2h[row][col];
            double dm1 = -1d / (curT2h);
            return dm1;
        }
    }; 

}
