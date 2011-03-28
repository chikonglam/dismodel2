package gov.usgs.dismodel.gui.ENUView;



import java.util.EventListener;

import gov.usgs.dismodel.geom.overlays.jzy.AutoKmTicker;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.gui.jzy3dOverrides.JzyMouseListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import javax.swing.JComponent;
import net.masagroup.jzy3d.chart.Chart;
import net.masagroup.jzy3d.chart.controllers.mouse.ChartMouseController;
import net.masagroup.jzy3d.maths.BoundingBox3d;
import net.masagroup.jzy3d.plot3d.primitives.axes.AxeBox;
import net.masagroup.jzy3d.plot3d.rendering.canvas.Quality;

public class EnuViewerJzy2 implements ZoomEventListener, ZoomEventFirer{
	//state vars
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;
	
	
	//chart vars
	private Chart chart;
	private JzyMouseListener mouseController;
	private BoundingBox3d chartBounds;
	private AxeBox axesBounds;
	private AutoKmTicker kmTicker = new AutoKmTicker();
	
	//Constructor
    public EnuViewerJzy2(SimulationDataModel simModel, DisplayStateStore displaySettings) {
        //state stuff
        this.simModel = simModel;
        this.displaySettings = displaySettings;
        
        //chart stuff
        this.chart = new Chart(Quality.Nicest, "swing");
        mouseController = new JzyMouseListener(displaySettings);
        mouseController.addZoomListener(this);
        chart.addController(mouseController);
        
        setAxesFromDisplaySettings();
    }
    
    public JComponent getJComponent() {
        return ((JComponent) chart.getCanvas());
    }
    
    private void setAxesFromDisplaySettings(){
    	double centerX = this.displaySettings.getxCenter();
    	double centerY = this.displaySettings.getyCenter();
    	double graphSpan = this.displaySettings.getChartSpan();
    	
    	setAxes(centerX, centerY, graphSpan);
    }
    
    public void setAxes(final double centerX, final double centerY, final double graphSpan) { 
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


    //zoom event handler: to handle zoomings requests from everywhere
	@Override
	public void updateZoomLevel(DisplayStateStore displaySettings) {
    	double centerX = displaySettings.getxCenter();
    	double centerY = displaySettings.getyCenter();
    	double axisSpan = displaySettings.getChartSpan();
    	
    	setAxes(centerX, centerY, axisSpan);
    	
	}
	
	//Zoom event firer: to make WW zoom changes
	@Override
	public void addZoomListener(EventListener listener) {
		mouseController.addZoomListener(listener);
	}

	@Override
	public void removeZoomListener(EventListener listener) {
		mouseController.removeZoomListener(listener);
	}



    
}
