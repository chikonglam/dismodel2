/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.greens.dialogs;

import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.RestorableSourceDialog;
import gov.usgs.dismodel.SourceDialogUtils;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.Fault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cforden
 *
 */
public class DislocationDialogRestorable extends DislocationDialog implements RestorableSourceDialog, GeoPosClickListener,
DataChangeEventFrier {

    private static final long serialVersionUID = 1L;

    private boolean createNew = true;
    private int modelIndex = -1;
    private SimulationDataModel simModel;
    private AllGUIVars allGuiVars;
    private ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();

    
    
    
    public DislocationDialogRestorable(Window owner, String title, AllGUIVars allGuiVars){
    	super(owner);
    	setTitle(title);
        this.allGuiVars = allGuiVars;

        this.simModel = allGuiVars.getSimModel();

        Dismodel2 mainFrame = allGuiVars.getMainFrame();
        mainFrame.addGeoPosClickListener(this);
        this.addDataChangeEventListener(mainFrame);    	
    }

    public DislocationDialogRestorable(Window owner, String title, int modelIndex, AllGUIVars allGuiVars){
        this(owner, title, allGuiVars);
		this.modelIndex = modelIndex;
		this.createNew = false;
		String name = simModel.getSourceModels().get(modelIndex).getName();
		txtName.setText(name);
		setTitle(title + " (" + name + ")");
		
		
		OkadaFault3 curFault = (OkadaFault3) simModel.getSourceModels().get(modelIndex);
		OkadaFault3 curLB = (OkadaFault3) simModel.getSourceLowerbound().get(modelIndex);
		OkadaFault3 curUB = (OkadaFault3) simModel.getSourceUpperbound().get(modelIndex);
    	
		setUIelements(curFault, curLB, curUB);

        okayButton.setText("Save");
    }
    


    protected void setUIelements(OkadaFault3 curFault, OkadaFault3 curLB, OkadaFault3 curUB){
        double [] msp = curFault.getModeledSourceParamsCopy();
    	final double x1In =             msp[OkadaFault3.X1_LOC_IDX];
        final double y1In =             msp[OkadaFault3.Y1_LOC_IDX];
        final double x2In =             msp[OkadaFault3.X2_LOC_IDX];
        final double y2In =             msp[OkadaFault3.Y2_LOC_IDX];
        final double xCIn =             msp[OkadaFault3.XC_LOC_IDX];
        final double yCIn =             msp[OkadaFault3.YC_LOC_IDX];
        final double depthIn =          msp[OkadaFault3.DEP_LOC_IDX];
        final double strikeIn =         msp[OkadaFault3.STRIKE_IDX];
        final double dipIn =            msp[OkadaFault3.DIP_IDX];
        final double aspectRatioIn =    msp[OkadaFault3.ASPECT_RATIO_IDX];
        final double lengthIn =         msp[OkadaFault3.LENGTH_IDX];
        final double widthIn =          msp[OkadaFault3.WIDTH_IDX];
        final double ssIn =             msp[OkadaFault3.STRIKE_SLIP_IDX];
        final double dsIn =             msp[OkadaFault3.DIP_SLIP_IDX];
        final double tsIn =             msp[OkadaFault3.OPENING_IDX];
        
        double [] msplb = curLB.getModeledSourceParamsCopy();
    	final double x1lb =             msplb[OkadaFault3.X1_LOC_IDX];
        final double y1lb =             msplb[OkadaFault3.Y1_LOC_IDX];
        final double x2lb =             msplb[OkadaFault3.X2_LOC_IDX];
        final double y2lb =             msplb[OkadaFault3.Y2_LOC_IDX];
        final double xClb =             msplb[OkadaFault3.XC_LOC_IDX];
        final double yClb =             msplb[OkadaFault3.YC_LOC_IDX];
        final double depthlb =          msplb[OkadaFault3.DEP_LOC_IDX];
        final double strikelb =         msplb[OkadaFault3.STRIKE_IDX];
        final double diplb =            msplb[OkadaFault3.DIP_IDX];
        final double aspectRatiolb =    msplb[OkadaFault3.ASPECT_RATIO_IDX];
        final double lengthlb =         msplb[OkadaFault3.LENGTH_IDX];
        final double widthlb =          msplb[OkadaFault3.WIDTH_IDX];
        final double sslb =             msplb[OkadaFault3.STRIKE_SLIP_IDX];
        final double dslb =             msplb[OkadaFault3.DIP_SLIP_IDX];
        final double tslb =             msplb[OkadaFault3.OPENING_IDX];
        
        double [] mspub = curUB.getModeledSourceParamsCopy();
    	final double x1ub =             mspub[OkadaFault3.X1_LOC_IDX];
        final double y1ub =             mspub[OkadaFault3.Y1_LOC_IDX];
        final double x2ub =             mspub[OkadaFault3.X2_LOC_IDX];
        final double y2ub =             mspub[OkadaFault3.Y2_LOC_IDX];
        final double xCub =             mspub[OkadaFault3.XC_LOC_IDX];
        final double yCub =             mspub[OkadaFault3.YC_LOC_IDX];
        final double depthub =          mspub[OkadaFault3.DEP_LOC_IDX];
        final double strikeub =         mspub[OkadaFault3.STRIKE_IDX];
        final double dipub =            mspub[OkadaFault3.DIP_IDX];
        final double aspectRatioub =    mspub[OkadaFault3.ASPECT_RATIO_IDX];
        final double lengthub =         mspub[OkadaFault3.LENGTH_IDX];
        final double widthub =          mspub[OkadaFault3.WIDTH_IDX];
        final double ssub =             mspub[OkadaFault3.STRIKE_SLIP_IDX];
        final double dsub =             mspub[OkadaFault3.DIP_SLIP_IDX];
        final double tsub =             mspub[OkadaFault3.OPENING_IDX];
        
        boolean isTwopointsGiven = false;
        boolean isMidPointGiven = false;
        boolean isLengthGiven = false;
        boolean isWidthGiven = false;
        
        
        
        //top half
        //--------
        if (!Double.isNaN(x1In)){		//Endpoints
			endPointsRB.doClick();
			endPointsRB.setEnabled(true);
			
			SourceDialogUtils.parseValsIntoUIBoxes(x1In, x1lb, x1ub, y1In, y1lb, y1ub, 
					endpoint1Fixed, endpoint1xValue, endpoint1xLB, endpoint1xUB, endpoint1yValue, endpoint1yLB, endpoint1yUB);
			if (!Double.isNaN(x2In)){	//endpoint 2 given
				isTwopointsGiven = true;
				SourceDialogUtils.parseValsIntoUIBoxes(x2In, x2lb, x2ub, y2In, y2lb, y2ub, 
						endpoint2Fixed, endpoint2xValue, endpoint2xLB, endpoint2xUB, endpoint2yValue, endpoint2yLB, endpoint2yUB);
				specifyEndpoint2.setSelected(true);
			} else {					//endpoint 2 is inferred
		        this.endpoint2xValue.setText(String.format("%.2f", curFault.getX2()));
		        this.endpoint2yValue.setText(String.format("%.2f", curFault.getY2()));
		        specifyEndpoint2.setSelected(false);
			}
	        midpointxValue.setText(String.format("%.2f", curFault.getXc()));
	        midpointyValue.setText(String.format("%.2f", curFault.getYc()));
        } else {					//Midpoint
        	isMidPointGiven = true;
        	midpointRB.doClick();
        	midpointRB.setSelected(true);
        	
	        this.endpoint1xValue.setText(String.format("%.2f", curFault.getX1()));
	        this.endpoint1yValue.setText(String.format("%.2f", curFault.getY1()));
	        this.endpoint2xValue.setText(String.format("%.2f", curFault.getX2()));
	        this.endpoint2yValue.setText(String.format("%.2f", curFault.getY2()));

			SourceDialogUtils.parseValsIntoUIBoxes(xCIn, xClb, xCub, yCIn, yClb, yCub, 
					midpointFixed, midpointxValue, midpointxLB, midpointxUB, midpointyValue, midpointyLB, midpointyUB);
        }
        
		SourceDialogUtils.parseValsIntoUIBoxes(depthIn, depthlb, depthub,
				depthFixed, depthValue, depthLB, depthUB	);
		
		if (curFault.isTopCoords()) {
			coordinatesAreUpperEdge.setSelected(true);
		} else {
			coordinatesAreUpperEdge.setSelected(false);
		}
		
		//Bottom half
		//-------------
		if (isTwopointsGiven) 
			strikeValue.setText(String.format("%.2f", curFault.getStrike()));
		else
			SourceDialogUtils.parseValsIntoUIBoxes(strikeIn, strikelb, strikeub,
					strikeFixed, strikeValue, strikeLB, strikeUB);
		
		SourceDialogUtils.parseValsIntoUIBoxes(dipIn, diplb, dipub,
				dipFixed, dipValue, dipLB, dipUB);
		
		
		if (!isMidPointGiven && isTwopointsGiven){
			aspectRatioValue.setText(String.format("%.2f", curFault.getAspectRatio()));
			aspectRatioCB.setSelected(false);
			lengthValue.setText(String.format("%.2f", curFault.getLength()));
			lengthCB.setSelected(false);
			SourceDialogUtils.parseValsIntoUIBoxes(widthIn, widthlb, widthub,
					widthFixed, widthValue, widthLB, widthUB	);
			widthCB.setSelected(true);
		} else {
			isLengthGiven = !Double.isNaN(lengthIn);
			isWidthGiven = !Double.isNaN(widthIn);
			
			if (!isLengthGiven || !isWidthGiven){				//aspect ratio is given
				SourceDialogUtils.parseValsIntoUIBoxes(aspectRatioIn, aspectRatiolb, aspectRatioub,
						aspectRatioFixed, aspectRatioValue, aspectRatioLB, aspectRatioUB	);
				aspectRatioCB.setSelected(true);
			} else {
				aspectRatioValue.setText(String.format("%.2f", curFault.getAspectRatio()));
				aspectRatioCB.setSelected(false);
			}
			
			if (isLengthGiven){
				SourceDialogUtils.parseValsIntoUIBoxes(lengthIn, lengthlb, lengthub,
						lengthFixed, lengthValue, lengthLB, lengthUB	);
				lengthCB.setSelected(true);
			} else {
				lengthValue.setText(String.format("%.2f", curFault.getLength()));
				lengthCB.setSelected(false);
			}
			
			if (isWidthGiven){
				SourceDialogUtils.parseValsIntoUIBoxes(widthIn, widthlb, widthub,
						widthFixed, widthValue, widthLB, widthUB	);
				widthCB.setSelected(true);
			} else {
				widthValue.setText(String.format("%.2f", curFault.getWidth()));
				widthCB.setSelected(false);
			}
			
		}
		
		//slips
		//------
        double ss = curFault.getStrikeSlip();
        double ds = curFault.getDipSlip();
        double ts = curFault.getOpening();
        
        if ( Math.abs(ss) != 0d){
            SSValue.setText(String.format("%.3e", ss));
            strikeSlipCB.setSelected(true);
        }
        
        if (Math.abs(ds) != 0d){
            DSValue.setText(String.format("%.3e", ds));
            this.dipSlipCB.setSelected(true);
        }
        
        if (Math.abs(ts) != 0d){
            TSValue.setText(String.format("%.3e", ts));
            this.openingCB.setSelected(true);
        }
    
        if ( curFault instanceof DistributedFault){     //TODO: also do the subdialog
            DistributedFault dFault = (DistributedFault) curFault;
            this.deltaLength.setText(String.format("%.2f", dFault.getdLength()));
            this.deltaWidth.setText(String.format("%.2f", dFault.getdWidth()));
        }
        

    }

    //GUI code
    //---------
    @Override
    protected void okayButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        DislocationBuilder db = new DislocationBuilder(this);
        LLH origin = simModel.getOrigin();
        DisplacementSolver of = db.getDislocation(origin);
        DisplacementSolver ub = db.getDislocationUB(origin);
        DisplacementSolver lb = db.getDislocationLB(origin);
        List<DisplacementSolver> sourceModel = simModel.getSourceModels();
        
        String name = txtName.getText();
        if (name != null && ! name.isEmpty()){
        	of.setName(name);
        }

        if (isCreateNew()) {
        	 sourceModel.add(of);
             simModel.getSourceLowerbound().add(lb);
             simModel.getSourceUpperbound().add(ub);

             System.out.println("Source Added: " + of);
             System.out.println("   LB: " + lb);
             System.out.println("   UB: " + ub);
             
             
//             if (isCurDistFault) {
//                 simModel.getNonNeg().add(db.isNonNeg());
//                 simModel.getMonentConstraint().add(db.getTargetMoment());
//                 simModel.getMonentConType().add(db.getMomentConType());  
//                 //simModel.setDistributedFaultProblem(true);
//             }
             
        } else { // just editing existing dialog
            int curModelIndex = getModelIndex();

            simModel.getSourceModels().set(curModelIndex, of);
            simModel.getSourceLowerbound().set(curModelIndex, lb);
            simModel.getSourceUpperbound().set(curModelIndex, ub);
//            if (isCurDistFault) {
            	//simModel.setDistributedFaultProblem(true);
//                if (simModel.getNonNeg().size() > curModelIndex) {
//                    simModel.getNonNeg().set(curModelIndex,db.isNonNeg());
//                    simModel.getMonentConstraint().set(curModelIndex, db.getTargetMoment());
//                    simModel.getMonentConType().set(curModelIndex,db.getMomentConType());
//                } else {
//                    simModel.getNonNeg().add(db.isNonNeg());
//                    simModel.getMonentConstraint().add(db.getTargetMoment());
//                    simModel.getMonentConType().add(db.getMomentConType());
//                }
//            }
        }
        
    	
    	lsDialog.setVisible(false);
        dispose();
    }         
    
    @Override
    public void dispose(){
        Dismodel2 mainFrame = allGuiVars.getMainFrame();
        mainFrame.removeGeoPosClickListener(this);
        this.removeDataChangeEventListener(mainFrame);
    	super.dispose();
    }
    
    //TODO really make bounds work
    public void showSource(DisplacementSolver sourceIn, int modelIndex, DisplacementSolver sourceLB, DisplacementSolver sourceUB) {
        // TODO Make this fully functional and less hackish
        if (sourceIn == null) return;
        
        this.modelIndex = modelIndex;
        this.createNew = false;
        
        //resetAll();
        
        Fault faultIn = (Fault) sourceIn;
        
        //now put the stuff in leaving the bounds alone
        this.endpoint1xValue.setText(String.format("%.2f", faultIn.getX1()));
        this.endpoint1yValue.setText(String.format("%.2f", faultIn.getY1()));
        this.endpoint2xValue.setText(String.format("%.2f", faultIn.getX2()));
        this.endpoint2yValue.setText(String.format("%.2f", faultIn.getY2()));
        this.midpointxValue.setText(String.format("%.2f", faultIn.getLowerXC()));
        this.midpointyValue.setText(String.format("%.2f", faultIn.getLowerYC()));
        this.depthValue.setText(String.format("%.2f", faultIn.getDepth()));
        this.coordinatesAreUpperEdge.setSelected(faultIn.isTopCoords());
        
        this.strikeValue.setText(String.format("%.2f", faultIn.getStrike()));
        this.dipValue.setText(String.format("%.2f", faultIn.getDip()));
        this.aspectRatioValue.setText(String.format("%.2f", faultIn.getAspectRatio()));
        this.lengthValue.setText(String.format("%.2f", faultIn.getLength()));
        this.widthValue.setText(String.format("%.2f", faultIn.getWidth()));
        
        double ss = faultIn.getStrikeSlip();
        double ds = faultIn.getDipSlip();
        double ts = faultIn.getOpening();
                
        if ( Math.abs(ss) != 0d){
            SSValue.setText(String.format("%.3e", ss));
            strikeSlipCB.setSelected(true);
        }
        
        if (Math.abs(ds) != 0d){
            DSValue.setText(String.format("%.3e", ds));
            this.dipSlipCB.setSelected(true);
        }
        
        if (Math.abs(ts) != 0d){
            TSValue.setText(String.format("%.3e", ts));
            this.openingCB.setSelected(true);
        }
        
        if ( sourceIn instanceof DistributedFault){     //TODO: also do the subdialog
            DistributedFault dFault = (DistributedFault) sourceIn;
            this.deltaLength.setText(String.format("%.2f", dFault.getdLength()));
            this.deltaWidth.setText(String.format("%.2f", dFault.getdWidth()));
        }
        
        okayButton.setText("Save");
        setTitle("Edit a dislocation");
        this.setVisible(true);
    }
    
    @Override
    public int getModelIndex() {
        return this.modelIndex;
    }

    @Override
    public boolean isCreateNew() {
        return this.createNew;
    }
    
    public void applyButton() {
        if (nFixed == TOTAL_FIXED) {

            DislocationBuilder tempBuilder = new DislocationBuilder(this);
            
            LLH origin = simModel.getOrigin();
            DisplacementSolver tempFault = tempBuilder.getDislocation(origin);
            String name = txtName.getText();
            if (name != null && !name.isEmpty()) tempFault.setName(name);
            ArrayList<DisplacementSolver> tempDrawer = new ArrayList<DisplacementSolver>();
            tempDrawer.add(tempFault);
            
            if (!isCreateNew() && modelIndex >= 0){
                simModel.getFittedModels().set(modelIndex, tempFault);
            } else {
                if (modelIndex < 0){
                    modelIndex = simModel.getFittedModels().size();
                    simModel.getFittedModels().add(modelIndex, tempFault);
                } else{
                    simModel.getFittedModels().set(modelIndex, tempFault);
                }
                
            }
            fireDataChangeEvent();

            if (lsDialog == null || !lsDialog.isVisible()) {
                DislocationBuilder db = new DislocationBuilder(this);
                OkadaFault3 of = (OkadaFault3) db.getDislocation(simModel.getOrigin());
            	
                final double lengthTotal = of.getLength();
                final double widthTotal = of.getWidth();
                final double dL = Double.parseDouble(deltaLength.getText());
                final double dW = Double.parseDouble(deltaWidth.getText());
                final int lengthStep = (int) Math.ceil( lengthTotal / dL );
                final int widthStep = (int) Math.ceil( widthTotal / dW );

                //lsDialog = new LeastSquaresDialog(lengthStep, widthStep);
                lsDialog.setRowsAndColumns(widthStep, lengthStep);
                lsDialog.setVisible(true);
            }
        }
    }

    private void fireDataChangeEvent() {
        for (DataChangeEventListener listener : dataChgListeners) {
            listener.updateAfterDataChange();
        }
    }

    @Override
    public void addDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.add(listener);
    }

    @Override
    public void removeDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.remove(listener);        
    }

    @Override
    public void latLonClicked(LLH location) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void LocalENUClicked(LocalENU location) {
        // TODO Auto-generated method stub
        
    }
    
    
}
