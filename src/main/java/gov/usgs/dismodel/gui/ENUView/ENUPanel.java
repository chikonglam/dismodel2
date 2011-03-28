package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;



public class ENUPanel extends JPanel {
	private JPanel toolbar;
	private JPanel panel3d;
    private EnuViewerJzy2 enuChart;
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;

    public ENUPanel(Dimension canvasSize, SimulationDataModel simModel, DisplayStateStore displaySettings) {
        super(new BorderLayout());
        //state stuff
        this.simModel = simModel;
        this.displaySettings = displaySettings;
        
        
        // GUI stuff
        this.setPreferredSize(canvasSize);
 
        // tool bar
        toolbar = new JPanel();
        this.add(toolbar, BorderLayout.NORTH);
        
        // Jzy3d
        enuChart = new EnuViewerJzy2(simModel, displaySettings);
        panel3d = new JPanel();
        panel3d.setLayout(new BorderLayout());
        panel3d.add( enuChart.getJComponent(), BorderLayout.CENTER);

        this.add(panel3d, BorderLayout.CENTER);
    }
    
    

}

