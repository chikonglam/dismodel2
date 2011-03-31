package gov.usgs.dismodel.gui.menubar.data;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

public class LoadDisplacementMenuItem extends DataChangingMenuItem implements GuiUpdateRequestListener{
	private static final long serialVersionUID = 1166963296917504942L;

	public LoadDisplacementMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
		this.addDataChangeEventListener(allGuiVars.getMainFrame());
		allGuiVars.getMainFrame().addGuiUpdateRequestListener(this);
	}

	@Override
	public void guiUpdateAfterStateChange() {
		SwingUtilities.invokeLater(updateEnabled);
	}

	private final Runnable updateEnabled = new Runnable() {
		@Override
		public void run() {
			List<Label> loadedStations = allGuiVars.getSimModel().getStations();
			LoadDisplacementMenuItem.this.setEnabled( loadedStations != null && loadedStations.size() > 0  );
		}
	};
	

	@Override
	public void menuItemClickAction(ActionEvent e) {
		SimulationDataModel simModel = allGuiVars.getSimModel();
		JFrame frame = allGuiVars.getMainFrame();
		
        if (simModel.getDistributedSlipBatchIoProcessor() != null && 
        		simModel.getDistributedSlipBatchIoProcessor().getGreensMatrix() != null) {
            /*
             * The user has previously loaded a Greens matrix, in
             * which case displacements can have extended
             * pseudo-data entries for the Laplacian smoothing
             * equations, so use loadOneColExtDisp() not
             * loadOneColDisp().
             */
            try {
                simModel
                        .getDistributedSlipBatchIoProcessor()
                        .setDisplacements(
                                SaveAndLoad
                                        .findLoadOneColExtDisp(frame));
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(frame, e1
                        .getMessage(), "Load error",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        
		List<XyzDisplacement> stationDisps = SaveAndLoad.loadOneColDisp(frame);
        List<Label> stations = simModel.getStations();

		if (stationDisps == null || stationDisps.size() < 1 || stationDisps.size() != stations.size()){
		    System.err.println("Can't match displacements to stations.");
		    return;
		}
		
		List<VectorXyz> vectors = new ArrayList<VectorXyz>(
		        stationDisps.size());
		for (int i = 0; i < stationDisps.size(); i++) {
		    VectorXyz vector = new VectorXyz(stations.get(i),
		            stationDisps.get(i), null);
		    vectors.add(vector);
		}

		vectors = simModel.setMeasuredDispVectors(vectors);
		
		fireDataChangeEvent();
	}

}
