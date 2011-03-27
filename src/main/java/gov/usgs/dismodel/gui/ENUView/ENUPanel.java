package gov.usgs.dismodel.gui.ENUView;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;



public class ENUPanel extends JPanel {
    private EnuViewerJzy2 enuChart;

    public ENUPanel(Dimension canvasSize) {
        super(new BorderLayout());
        this.setPreferredSize(canvasSize);
        enuChart = new EnuViewerJzy2();

        JPanel panel3d = new JPanel();
        panel3d.setLayout(new BorderLayout());
        panel3d.add( enuChart.getJComponent(), BorderLayout.CENTER);

        this.add(panel3d, BorderLayout.CENTER);
    }
    
    

}

