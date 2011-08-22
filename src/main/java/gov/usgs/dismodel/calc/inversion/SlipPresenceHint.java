package gov.usgs.dismodel.calc.inversion;

import java.util.ArrayList;
import java.util.List;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;

public class SlipPresenceHint {
    private List<Integer> rows = new ArrayList<Integer>();
    private List<Integer> cols = new ArrayList<Integer>();
    private List<Integer> patches = new ArrayList<Integer>();
    private List<boolean[][][]> slipPres = new ArrayList<boolean[][][]>();
    private int numSlips;
    
    public SlipPresenceHint(DisplacementSolver[] models, int numSlips){
	this.numSlips = numSlips;
	for (DisplacementSolver model : models){
	    if (model instanceof DistributedFault){
		DistributedFault fault = (DistributedFault) model;
		
		int curRow = fault.getRowCt();
		rows.add(curRow);
		
		int curCol = fault.getColCt();
		cols.add(curCol);
		
		patches.add(curRow * curCol);
		
		boolean[][][] curSlipPres = new boolean[curRow][curCol][numSlips]; 
		slipPres.add(curSlipPres);
	    }
	    
	}
    }
    
    public void setPatchSlipPres(int seg, int row, int col, int paramIter, boolean presence){
	boolean[][][] curBoolArr = this.slipPres.get(seg);
	curBoolArr[row][col][paramIter] = presence;
    }
    
    public boolean isSlipActive(final int linearPatchNo, final int paramIter){
	int curSeg = 0;
	int patchNoRes = linearPatchNo;
	while(this.patches.get(curSeg) <= patchNoRes){
	    patchNoRes -= this.patches.get(curSeg);
	    curSeg++;
	}
	
	boolean[][][] curBoolArr = this.slipPres.get(curSeg);
	int curRowCt = rows.get(curSeg);
	int curRow = patchNoRes / curRowCt;
	int curCol = patchNoRes % curRowCt;
	
	return curBoolArr[curRow][curCol][paramIter];
    }
    
}
