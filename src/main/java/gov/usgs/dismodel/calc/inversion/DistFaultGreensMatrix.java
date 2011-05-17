package gov.usgs.dismodel.calc.inversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.calc.inversion.DistributedSlipSolver.faultWithBounds;

public class DistFaultGreensMatrix {
    double[][] outG;
    private ArrayList<SlipLocation> slipLocation = new ArrayList<SlipLocation>();
    private int segmentCt;
    private int numSubFaults;
    private int[] subFaultColCt;
    private int[] subFaultRowCt;
    private int colCumSum;
    private int numParamPerSubFault;
    private ArrayList<Integer> linVarIndicies;
    private int numVar;
    private ArrayList<ArrayList<Integer>> activeSubFaultParams;

    public DistFaultGreensMatrix(DisplacementSolver[] model) {
        super();
        sortFaults(model);
        this.linVarIndicies = model[0].getLinearParameterIndices();
        this.numParamPerSubFault = linVarIndicies.size();
        putInSegRowColCts(model);
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
    }

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

    private void sortFaults(DisplacementSolver[] value) {
        Arrays.sort(value, faultComparator);

    }

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

        curModeledParams[paramIdx] = 1d;
        XyzDisplacement curDisp;

        for (int stationIter = 0; stationIter < numStation; stationIter++) {
            curDisp = cursubFault.solveDisplacement(stationPositions[stationIter]);
            unitDispVec[stationIter * DIM_CT + DIM_X] = curDisp.getX();
            unitDispVec[stationIter * DIM_CT + DIM_Y] = curDisp.getY();
            unitDispVec[stationIter * DIM_CT + DIM_Z] = curDisp.getZ();
        }

        return unitDispVec;
    }

}
