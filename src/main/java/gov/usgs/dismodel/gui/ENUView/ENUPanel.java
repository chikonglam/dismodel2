package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.geom.overlays.jzy.AutoKmTicker;
import gov.usgs.dismodel.geom.overlays.jzy.Marker;
import gov.usgs.dismodel.geom.overlays.jzy.ScreenToGraphMap;
import gov.usgs.dismodel.geom.overlays.jzy.Vector3D;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.global.Settings;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.axes.AxeBox;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class ENUPanel extends JPanel implements ZoomEventListener, ZoomEventFirer, DataChangeEventListener {
    private JPanel toolbar;
    private JPanel panel3d;
    // private EnuViewerJzy2 enuChart;
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;

    // Chart stuff
    private Chart chart;
    private BoundingBox3d chartBounds;
    private AxeBox axesBounds;
    private AutoKmTicker kmTicker = new AutoKmTicker();
    private JzyMouseListener mouseController;
    
    //plotted stuff
    private List<Marker> stations = new ArrayList<Marker>();
    private List<Vector3D> vectors3D = new ArrayList<Vector3D>();
    

    public ENUPanel(Dimension canvasSize, SimulationDataModel simModel, DisplayStateStore displaySettings) {
        super(new BorderLayout());

        //set Jzy to use hardware
        Settings.getInstance().setHardwareAccelerated(true);    //TODO check if this works everywhere

        // state stuff
        this.simModel = simModel;
        this.displaySettings = displaySettings;

        // GUI stuff
        this.setPreferredSize(canvasSize);

        // tool bar
        toolbar = new JPanel();
        this.add(toolbar, BorderLayout.NORTH);

        // chart stuff
        this.chart = new Chart(Quality.Nicest, "swing");
        this.mouseController = new JzyMouseListener(this, displaySettings, simModel);
        chart.addController(mouseController);

        
        // enuChart = new EnuViewerJzy2(simModel, displaySettings);
        panel3d = new JPanel();
        panel3d.setLayout(new BorderLayout());
        panel3d.add((Component) chart.getCanvas(), BorderLayout.CENTER);

        this.add(panel3d, BorderLayout.CENTER);
        
        //init the graph
        setAxesFromDisplaySettings();
        //panel3d.repaint();
        
        

    }

    public void setAxesFromDisplaySettings() {
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

                chart.getAxeLayout().setZTickRenderer(kmTicker);
                chart.getAxeLayout().setXTickRenderer(kmTicker);
                chart.getAxeLayout().setYTickRenderer(kmTicker);

                chart.getView().updateBounds();
     }
    


    // zoom event handler: to handle zoomings requests from everywhere
    @Override
    public void updateZoomLevelAfterSettingsChanged(DisplayStateStore displaySettings) {
        double centerX = displaySettings.getxCenter();
        double centerY = displaySettings.getyCenter();
        double axisSpan = displaySettings.getChartSpan();
        System.out.println("Jzy received a request to zoom to span=" + axisSpan);
        setAxes(centerX, centerY, axisSpan);
    }

    // Zoom event firer: to make WW zoom changes
    @Override
    public void addZoomListener(ZoomEventListener listener) {
        mouseController.addZoomListener(listener);
    }

    @Override
    public void removeZoomListener(ZoomEventListener listener) {
        mouseController.removeZoomListener(listener);
    }

    @Override
    public void updateAfterDataChange() {
        SwingUtilities.invokeLater(refreshAllElements);
    }
    
    private Runnable refreshAllElements = new Runnable(){
        @Override
        public void run() {
            refreshAllStations();
            refreshMeasuredDisps();

            
            chart.getView().updateBounds();
        }
        
    };
    
    private void refreshAllStations(){
        List<Label> newStations = simModel.getStations();
        java.awt.Color color = displaySettings.getStationColor();
        
        for (Marker m : stations) {                             //remove old ones
            chart.getScene().remove(m, false);
        }
        stations = new ArrayList<Marker>();
 
        if (newStations == null || newStations.size() == 0)
            return;
       
        LLH origin = simModel.getOrigin();
        
        for (Label station : newStations) {
            Marker marker = new Marker(origin, station, fromAWT(color));
            stations.add(marker);
            chart.getScene().add(marker, false);
        }
    }
    
    private void refreshMeasuredDisps(){
        List<VectorXyz> vectors = simModel.getMeasuredRefdDispVectors();
        double scale = displaySettings.getDisplacementVectorScale();
        Color color = fromAWT(displaySettings.getRealDisplacementVectorColor());
        LLH origin = simModel.getOrigin();
        
        for (Vector3D vector : vectors3D) {
            chart.getScene().remove(vector, false);
        }
        vectors3D = new ArrayList<Vector3D>(vectors.size());
        
        for (VectorXyz dv : vectors) {
            Vector3D v3D = new Vector3D(dv, origin, scale, color);
            vectors3D.add(v3D);
            chart.getScene().add(v3D, false);
        }
        
    }
    
    
    protected static org.jzy3d.colors.Color fromAWT(java.awt.Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

	public Chart getChart() {
		return chart;
	}
    
    

}
