package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.geom.Earth;
import gov.usgs.dismodel.geom.overlays.jzy.DecimalTickRenderer;

import javax.swing.JComponent;
import net.masagroup.jzy3d.chart.Chart;
import net.masagroup.jzy3d.maths.BoundingBox3d;
import net.masagroup.jzy3d.plot3d.primitives.axes.AxeBox;
import net.masagroup.jzy3d.plot3d.rendering.canvas.Quality;

public class EnuViewerJzy2 {
	private Chart chart;
  
    public EnuViewerJzy2() {
        this.chart = new Chart(Quality.Nicest, "swing");
        
        setAxes(-Earth.RADIUS_APPROX, Earth.RADIUS_APPROX);
    }
    
    public JComponent getJComponent() {
        return ((JComponent) chart.getCanvas());
    }
    
    public void setAxes(double min, double max) { 
        double axesMin = min;
        double axesMax = max;
        
        BoundingBox3d bbox = new BoundingBox3d((float)axesMin, (float)axesMax, 
                (float)axesMin, (float)axesMax, (float)axesMin, (float)axesMax);
        AxeBox axes = new AxeBox(bbox);
        chart.getView().setBoundManual(bbox);
        chart.getView().setAxe(axes);
        
        chart.getAxeLayout().setYAxeLabel("Northing (km)");
        chart.getAxeLayout().setXAxeLabel("Easting (km)");

        chart.getAxeLayout().setZTickRenderer( new DecimalTickRenderer(9) );
        chart.getAxeLayout().setXTickRenderer( new DecimalTickRenderer(9) );
        chart.getAxeLayout().setYTickRenderer( new DecimalTickRenderer(9) );

        chart.getView().updateBounds();
    }
    
}
