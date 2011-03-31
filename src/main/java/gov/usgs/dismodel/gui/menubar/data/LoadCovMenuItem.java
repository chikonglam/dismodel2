package gov.usgs.dismodel.gui.menubar.data;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.calc.inversion.CovarianceWeighter;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

public class LoadCovMenuItem extends DataChangingMenuItem implements GuiUpdateRequestListener{
	private static final long serialVersionUID = -4578023705994097168L;

	public LoadCovMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
		allGuiVars.getMainFrame().addGuiUpdateRequestListener(this);
		this.addDataChangeEventListener(allGuiVars.getMainFrame());
	}

	@Override
	public void menuItemClickAction(ActionEvent e) {
		JFrame frame = allGuiVars.getMainFrame();
		SimulationDataModel simModel = allGuiVars.getSimModel();
		
        File f = SaveAndLoad.loadFile(frame);

        try {
            simModel.getCovarWeighter().readCovFile(f);
            CovarianceWeighter cw = simModel.getCovarWeighter();
            List<VectorXyz> vectors = simModel
                    .getMeasuredDispVectors();
            cw.calcErrorEllipsoids(vectors);
            
            fireDataChangeEvent();
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(frame, e1.getMessage(),
                    "Load error", JOptionPane.ERROR_MESSAGE);
        }
		
	}

	@Override
	public void guiUpdateAfterStateChange() {
		SwingUtilities.invokeLater(updateEnabled);
	}
	
	private final Runnable updateEnabled = new Runnable() {
		@Override
		public void run() {
			SimulationDataModel simModel = allGuiVars.getSimModel();
			List<Label> stations = simModel.getStations();
			List<VectorXyz> disp = simModel.getMeasuredUnrefdDispVectors();
			LoadCovMenuItem.this.setEnabled(stations != null && stations.size() > 0 && disp != null && disp.size() > 0);
		}
	};

}
