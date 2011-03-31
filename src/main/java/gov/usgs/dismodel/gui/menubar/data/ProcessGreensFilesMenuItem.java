package gov.usgs.dismodel.gui.menubar.data;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

public class ProcessGreensFilesMenuItem extends DataChangingMenuItem{
	private static final long serialVersionUID = -2785317942959011686L;

	public ProcessGreensFilesMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void menuItemClickAction(ActionEvent e) {
	    JFrame frame = allGuiVars.getMainFrame();
	    
//	    BatchProcessGreensDlg dialog = new BatchProcessGreensDlg( frame, "Batch processing of Green's function files", true);
//	    
//	   final BatchProcessGreensDlg dialog = frame.invConstrainFaultDlg;
//	   dialog.getButtonOkay().addActionListener(
//	           new ActionListener() {
//	               @Override
//	               public void actionPerformed(ActionEvent e) {
//	                   simModel
//	                           .getDistributedSlipBatchIoProcessor()
//	                           .setDialog(dialog);
//	                   simModel
//	                           .getDistributedSlipBatchIoProcessor()
//	                           .solveLoadedGreensFunctions(frame);
//	               }
//	           });
//	   dialog.setVisible(true);

	}

}
