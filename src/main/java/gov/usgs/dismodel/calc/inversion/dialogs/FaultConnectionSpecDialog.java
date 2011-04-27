package gov.usgs.dismodel.calc.inversion.dialogs;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.state.SimulationDataModel;


import java.awt.Frame;
import java.util.ArrayList;

public class FaultConnectionSpecDialog extends FaultConnectionSpecBase {

	private static final long serialVersionUID = 4222337642817275613L;
	
	private SimulationDataModel simModel;
	//private ArrayList<Integer> allDistedFaultsIndex;
	//private ArrayList<Integer> indepFaultsIndex;
	//private int numOfGroups = 0;
	
	private ArrayList<DistributedFault> allFaults;
	private ArrayList<DistributedFault> indepFaults;
	private ArrayList<ArrayList<DistributedFault>> groupedFaults;
	
	public FaultConnectionSpecDialog(Frame parent, boolean modal, SimulationDataModel simModel) {
		super(parent, modal);
		this.simModel = simModel;
		allFaults = getArrayOfAllDistFaults();
		indepFaults = (ArrayList<DistributedFault>) allFaults.clone();
		groupedFaults = new ArrayList<ArrayList<DistributedFault>>();
		updateInterface();
	}

	//-----Overriding methods
	@Override
	protected void onTxtNoSegmentsKeyReleased(java.awt.event.KeyEvent evt) {
		
		clearAllGroups();
		try {
			int numOfGroups = Integer.parseInt( this.txtNoSegments.getText() );
			for (int iter = 0; iter < numOfGroups; iter++){
				groupedFaults.add( new ArrayList<DistributedFault>() );
			}

		} catch (NumberFormatException e){
			//throw this exception, just non numeric => use default value
		}
		
		updateInterface();
	}
	
	
	private void clearAllGroups() {
		indepFaults = (ArrayList<DistributedFault>) allFaults.clone();
		groupedFaults = new ArrayList<ArrayList<DistributedFault>>();
	}

	@Override
	protected void onBtnAddActionPerformed(java.awt.event.ActionEvent evt) {
		int numOfGroups = groupedFaults.size();
		if (numOfGroups > 0 ){
			
			int curGroup = this.cbGroup.getSelectedIndex();
			if (curGroup < 0) curGroup = 0;
			ArrayList<DistributedFault> curGroupArray = this.groupedFaults.get(curGroup);
			ArrayList<DistributedFault> tally = new ArrayList<DistributedFault>(); 
			
			int rowsSelected [] = this.tabUnconned.getSelectedRows();
			//add to a tally first
			for (int iter = 0; iter < rowsSelected.length; iter++) {
				int curRow = rowsSelected[iter];
				
				DistributedFault curFault = this.indepFaults.get(curRow);
				tally.add(curFault);
				
			}
			
			//then do the switching
			curGroupArray.addAll(tally);
			indepFaults.removeAll(tally);

			updateInterface();
		}		//else no group defined, no need to do anything
	}

	@Override
	protected void onBtnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
		int numOfGroups = groupedFaults.size();
		if (numOfGroups > 0 ){

			int curGroup = this.cbGroup.getSelectedIndex();
			if (curGroup < 0) curGroup = 0;
			ArrayList<DistributedFault> curGroupArray = this.groupedFaults.get(curGroup);
			ArrayList<DistributedFault> tally = new ArrayList<DistributedFault>(); 
			
			int rowsSelected [] = this.tabGroupSelected.getSelectedRows();

			//add to a tally first
			for (int iter = 0; iter < rowsSelected.length; iter++) {
				int curRow = rowsSelected[iter];
				
				DistributedFault curFault = curGroupArray.get(curRow);
				tally.add(curFault);
			}
			
			//then switch
			indepFaults.addAll(tally);
			curGroupArray.removeAll(tally);
			
			updateInterface();
		}		//else no group defined, no need to do anything
	}
    
	@Override
	protected void onCbGroupActionPerformed(java.awt.event.ActionEvent evt) {
		updateInterface();
	}

	@Override
	protected void onBtnOKActionPerformed(java.awt.event.ActionEvent evt) {
		for (DistributedFault fault : this.indepFaults){
			fault.setNoGroup();
		}
		
		int faultGroup = 0;
		for (ArrayList<DistributedFault> group : this.groupedFaults){
			for (DistributedFault fault : group){
				fault.setGroup(faultGroup);
			}
			faultGroup++;
		}
		
		dispose();
	}
	//-------
	
	protected void updateInterface(){
		int selectedGroup = cbGroup.getSelectedIndex();
		int numOfGroups = this.groupedFaults.size();
		
		//populate the box for the disconnected faults
		updateTable(this.tabUnconned, this.indepFaults);
		
		//populate the num of group combobox
		ArrayList<String> comboBoxItems = new ArrayList<String>(numOfGroups);
		for (int groupIter = 0; groupIter < numOfGroups; groupIter++){
			comboBoxItems.add( Integer.toString( groupIter + 1) );
		}
		cbGroup.setModel(new javax.swing.DefaultComboBoxModel(comboBoxItems.toArray(new String[0])));
		
		//populated the selected group box
		if (numOfGroups > 0 && selectedGroup >= 0 && selectedGroup < numOfGroups){
			cbGroup.setSelectedIndex(selectedGroup);
			updateTable(this.tabGroupSelected, this.groupedFaults.get(selectedGroup) );	
		} else {
			updateTable(this.tabGroupSelected, null);
		}
		
	}
	
	protected void updateTable(javax.swing.JTable table, ArrayList<DistributedFault> faultGroup){
		int groupLen = 0;
		if (faultGroup != null){
			groupLen = faultGroup.size();
		}
		
		String[][] selectedModel = new String[groupLen][1];
		for (int iter = 0; iter < groupLen; iter++){
			DistributedFault curFault = faultGroup.get(iter);
			selectedModel[iter][0]= curFault.toString();  //now only the toString is used, but can be changed to incorporate names
		}
		Object [][] tableContent = new Object[][] {}; 
		if (groupLen > 0) tableContent = selectedModel;
		
		table.setModel(new javax.swing.table.DefaultTableModel(
				tableContent,
	            new String [] {"Details"}
	        ) {
	            /**
				 * 
				 */
				private static final long serialVersionUID = 1182273265851284442L;
				Class[] types = new Class [] {
	                java.lang.String.class
	            };
	            boolean[] canEdit = new boolean [] {
	                false
	            };

	            public Class getColumnClass(int columnIndex) {
	                return types [columnIndex];
	            }

	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        });

	}
	
	protected ArrayList<DistributedFault> getArrayOfAllDistFaults(){
		ArrayList<DisplacementSolver> allSources = simModel.getSourceModels();
		int allSourcesLen = allSources.size();
		//this.allDistedFaultsIndex = new ArrayList<Integer>();
		ArrayList<DistributedFault> arrayOut = new ArrayList<DistributedFault>();
		for (int iter = 0; iter < allSourcesLen; iter++){
			if (allSources.get(iter) instanceof DistributedFault){
				arrayOut.add((DistributedFault) allSources.get(iter));
			}
		}
		return arrayOut;
		
	}
	
	
	

}
