package gov.usgs.dismodel.calc.overlays.ojalgo;

import org.ojalgo.matrix.jama.JamaMatrix;

public class JamaUtil {
    public static double[][] toRawCopy(JamaMatrix in){
	final int rowCt = in.getRowDim();
	final int colCt = in.getColDim();
	
	double[][] out = new double[rowCt][colCt];
	for (int rowIter = 0; rowIter < rowCt; rowIter++){
	    double[] curRow = out[rowIter];
	    for (int colIter = 0; colIter < colCt; colIter++){
		curRow[colIter] = in.get(rowIter, colIter);
	    }
	}
	
	return out;
    }
    
    public static double[][] genIdentMatrix(final int dim){
	double [][] diagArray = new double[dim][dim];
	
	for (int iter=0; iter < dim; iter++){
	    diagArray[iter][iter] = 1d;
	}
	return diagArray;
    }
}
