package gov.usgs.dismodel.gui.ENUView;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;



public class ENUPanel extends JPanel {
	private JPanel toolbar;
	private JPanel panel3d;
    private EnuViewerJzy2 enuChart;

    public ENUPanel(Dimension canvasSize) {
        super(new BorderLayout());
        this.setPreferredSize(canvasSize);
 
        // tool bar
        toolbar = new JPanel();
        this.add(toolbar, BorderLayout.NORTH);
        
        // Jzy3d
        enuChart = new EnuViewerJzy2();
        panel3d = new JPanel();
        panel3d.setLayout(new BorderLayout());
        panel3d.add( enuChart.getJComponent(), BorderLayout.CENTER);

        this.add(panel3d, BorderLayout.CENTER);
    }
    
    

}

