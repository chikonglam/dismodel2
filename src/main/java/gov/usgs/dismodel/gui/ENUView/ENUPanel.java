package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.geom.overlays.jzy.AutoKmTicker;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.gui.jzy3dOverrides.JzyMouseListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.masagroup.jzy3d.chart.Chart;
import net.masagroup.jzy3d.maths.BoundingBox3d;
import net.masagroup.jzy3d.plot3d.primitives.axes.AxeBox;
import net.masagroup.jzy3d.plot3d.rendering.canvas.Quality;



public class ENUPanel extends JPanel implements ZoomEventListener, ZoomEventFirer {
	private JPanel toolbar;
	private JPanel panel3d;
//    private EnuViewerJzy2 enuChart;
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;
    
    //Chart stuff
	private Chart chart;
	private JzyMouseListener mouseController;
	private BoundingBox3d chartBounds;
	private AxeBox axesBounds;
	private AutoKmTicker kmTicker = new AutoKmTicker();

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
        
        //chart stuff
        this.chart = new Chart(Quality.Nicest, "swing");
        this.mouseController = new JzyMouseListener(displaySettings);
        mouseController.addZoomListener(this);
        chart.addController(mouseController);
        
        setAxesFromDisplaySettings();
        
        //enuChart = new EnuViewerJzy2(simModel, displaySettings);
        panel3d = new JPanel();
        panel3d.setLayout(new BorderLayout());
        panel3d.add( (JComponent)chart.getCanvas(), BorderLayout.CENTER);

        this.add(panel3d, BorderLayout.CENTER);
    }
    
    private void setAxesFromDisplaySettings(){
    	double centerX = this.displaySettings.getxCenter();
    	double centerY = this.displaySettings.getyCenter();
    	double graphSpan = this.displaySettings.getChartSpan();
    	
    	setAxes(centerX, centerY, graphSpan);
    }
    
    public void setAxes(final double centerX, final double centerY, final double graphSpan) { 
        Runnable threadedsetAxes = new Runnable() {
    		@Override
    		public void run() {
    	    	final double halfSpan = graphSpan / 2.0;
    	        
    	        chartBounds = new BoundingBox3d();
    	        chartBounds.setXmin((float) (centerX - halfSpan));
    	        chartBounds.setXmax((float) (centerX + halfSpan));
    	        chartBounds.setYmin((float) (centerY - halfSpan));
    	        chartBounds.setYmax((float) (centerY + halfSpan));
    	        chartBounds.setZmin((float) (-halfSpan));
    	        chartBounds.setZmax((float) (halfSpan));
    	        
    	        axesBounds = new AxeBox(chartBounds);
    	        
    	        chart.getView().setBoundManual(chartBounds);
    	        chart.getView().setAxe(axesBounds);
    	        
    	        chart.getAxeLayout().setYAxeLabel("Northing(km)");
    	        chart.getAxeLayout().setXAxeLabel("Easting(km)");

    	        chart.getAxeLayout().setZTickRenderer( kmTicker );
    	        chart.getAxeLayout().setXTickRenderer( kmTicker );
    	        chart.getAxeLayout().setYTickRenderer( kmTicker );

    	        chart.getView().updateBounds();

    		}
    	};
    	SwingUtilities.invokeLater( threadedsetAxes );

    }
    
    
    //zoom event handler: to handle zoomings requests from everywhere
	@Override
	public void updateZoomLevelAfterSettingsChanged(
			DisplayStateStore displaySettings) {
    	double centerX = displaySettings.getxCenter();
    	double centerY = displaySettings.getyCenter();
    	double axisSpan = displaySettings.getChartSpan();
    	
    	setAxes(centerX, centerY, axisSpan);
	}
	
	//Zoom event firer: to make WW zoom changes
	@Override
	public void addZoomListener(ZoomEventListener listener) {
		mouseController.addZoomListener(listener);
	}

	@Override
	public void removeZoomListener(ZoomEventListener listener) {
		mouseController.removeZoomListener(listener);
	}


    

}

