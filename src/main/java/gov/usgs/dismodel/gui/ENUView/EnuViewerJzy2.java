package gov.usgs.dismodel.gui.ENUView;


import gov.usgs.dismodel.geom.Earth;
import gov.usgs.dismodel.geom.overlays.jzy.DecimalTickRenderer;
import gov.usgs.dismodel.gui.jzy3dOverrides.JzyMouseListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import javax.swing.JComponent;
import net.masagroup.jzy3d.chart.Chart;
import net.masagroup.jzy3d.chart.controllers.mouse.ChartMouseController;
import net.masagroup.jzy3d.maths.BoundingBox3d;
import net.masagroup.jzy3d.plot3d.primitives.axes.AxeBox;
import net.masagroup.jzy3d.plot3d.rendering.canvas.Quality;

public class EnuViewerJzy2 {
	//state vars
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;
	
	
	//chart vars
	private Chart chart;
	private ChartMouseController mouseController;
	private BoundingBox3d chartBounds;
	private AxeBox axesBounds;
	
	
  
    public EnuViewerJzy2(SimulationDataModel simModel, DisplayStateStore displaySettings) {
        //state stuff
        this.simModel = simModel;
        this.displaySettings = displaySettings;
        
        //chart stuff
        this.chart = new Chart(Quality.Nicest, "swing");
        mouseController = new JzyMouseListener(this);
        chart.addController(mouseController);
        
        setAxesFromDisplaySettings();
    }
    
    public JComponent getJComponent() {
        return ((JComponent) chart.getCanvas());
    }
    
    public void setAxesFromDisplaySettings(){
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
        
        chart.getAxeLayout().setYAxeLabel("Northing (km)");
        chart.getAxeLayout().setXAxeLabel("Easting (km)");

        chart.getAxeLayout().setZTickRenderer( new DecimalTickRenderer(9) );
        chart.getAxeLayout().setXTickRenderer( new DecimalTickRenderer(9) );
        chart.getAxeLayout().setYTickRenderer( new DecimalTickRenderer(9) );

        chart.getView().updateBounds();
    }

	public void zoomBy(float factor) {
		double newSpan = factor * this.displaySettings.getChartSpan();
		this.displaySettings.setChartSpan(newSpan);
		setAxesFromDisplaySettings();
	}
    
}
