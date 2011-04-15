package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.OkadaFault3;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.geom.overlays.jzy.AutoKmTicker;
import gov.usgs.dismodel.geom.overlays.jzy.ColorStrip;
import gov.usgs.dismodel.geom.overlays.jzy.DistributedFaultViewable;
import gov.usgs.dismodel.geom.overlays.jzy.Marker;
import gov.usgs.dismodel.geom.overlays.jzy.ScreenToGraphMap;
import gov.usgs.dismodel.geom.overlays.jzy.Vector3D;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.global.Settings;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.axes.AxeBox;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

public class ENUPanel extends JPanel implements ZoomEventListener, ZoomEventFirer, DataChangeEventListener {
    private static final long serialVersionUID = -1463458221429777048L;

    private ENUToolBar toolbar;
    private JPanel panel3d;
    // private EnuViewerJzy2 enuChart;
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;
    private Dismodel2 mainFrame;

    // Chart stuff
    private Chart chart;
    private BoundingBox3d chartBounds;
    private AxeBox axesBounds;
    private AutoKmTicker kmTicker = new AutoKmTicker();
    private ScreenToGraphMap mapper = new ScreenToGraphMap();
    private JzyMouseListener mouseController;

    // plotted stuff
    private List<Marker> stations = new ArrayList<Marker>();
    private List<Vector3D> measedVectors = new ArrayList<Vector3D>();
    private List<AbstractDrawable> sourceModels = new ArrayList<AbstractDrawable>();
    private List<Vector3D> modeledVectors = new ArrayList<Vector3D>();
    private ColorStrip colorBar;

    public ENUPanel(Dimension canvasSize, SimulationDataModel simModel, DisplayStateStore displaySettings, Dismodel2 mainFrame) {
        super(new BorderLayout());

        // set Jzy to use hardware
        Settings.getInstance().setHardwareAccelerated(true); // TODO check if
                                                             // this works
                                                             // everywhere

        // state stuff
        this.simModel = simModel;
        this.displaySettings = displaySettings;
        this.mainFrame = mainFrame;

        // GUI stuff
        this.setPreferredSize(canvasSize);

        // tool bar
        toolbar = new ENUToolBar(new AllGUIVars(mainFrame, null, this, displaySettings, simModel));
        this.add(toolbar, BorderLayout.NORTH);

        // chart stuff
        this.chart = new Chart(Quality.Nicest, "swing");
        this.chart.getScene().add(mapper);
        this.mouseController = new JzyMouseListener(this, mapper, displaySettings, simModel);
        chart.addController(mouseController);

        // enuChart = new EnuViewerJzy2(simModel, displaySettings);
        panel3d = new JPanel();
        panel3d.setLayout(new BorderLayout());
        panel3d.add((Component) chart.getCanvas(), BorderLayout.CENTER);

        this.add(panel3d, BorderLayout.CENTER);

        // init the graph
        setAxesFromDisplaySettings();
        // panel3d.repaint();

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

    private Runnable refreshAllElements = new Runnable() {
        @Override
        public void run() {
            refreshAllStations();
            refreshMeasuredDisps();
            refreshVectorScaleBar();
            refreshSources();
            refreshModeledDisps();
            
            

            chart.getView().updateBounds();
        }

    };

    private void refreshAllStations() {
        List<Label> newStations = simModel.getStations();
        java.awt.Color color = displaySettings.getStationColor();

        for (Marker m : stations) { // remove old ones
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

    protected void refreshVectorScaleBar() {
        // TODO Auto-generated method stub

    }

    private void refreshMeasuredDisps() {
        List<VectorXyz> vectors = simModel.getMeasuredRefdDispVectors();
        double scale = displaySettings.getDisplacementVectorScale();
        Color color = fromAWT(displaySettings.getRealDisplacementVectorColor());
        LLH origin = simModel.getOrigin();

        for (Vector3D vector : measedVectors) {
            chart.getScene().remove(vector, false);
        }
        measedVectors = new ArrayList<Vector3D>(vectors.size());

        for (VectorXyz dv : vectors) {
            Vector3D v3D = new Vector3D(dv, origin, scale, color);
            measedVectors.add(v3D);
            chart.getScene().add(v3D, false);
        }

    }

    private void refreshSources() {
        List<DisplacementSolver> modelArray = simModel.getFittedModels();

        for (AbstractDrawable src : sourceModels) {
            chart.getScene().remove(src, false);
        }
        sourceModels = new ArrayList<AbstractDrawable>(modelArray.size());

        if (modelArray == null || modelArray.size() < 1)
            return;

        for (DisplacementSolver model : modelArray) {
            if (model != null){
                AbstractDrawable modelDrawable = model.toAbstractDrawable(displaySettings);
                chart.getScene().add(modelDrawable, true);
                this.sourceModels.add(modelDrawable);
            }
        }

        if (simModel.isDistributedFaultProblem()) {
            recolorDistributedFaults();
        }

        // TODO remember to deal with distributed fault problems (See
        // recolorDistributedFaults in EnuViewerJzy.java)
    }

    private void recolorDistributedFaults() {

        double maxSlip = 0;
        double minSlip = Double.MAX_VALUE;

        // find abs max and min among segments
        for (AbstractDrawable model : sourceModels) {
            if (model instanceof DistributedFaultViewable) {
                DistributedFaultViewable curFaultSeg = (DistributedFaultViewable) model;
                double curSegMax = curFaultSeg.getMaxMag();
                double curSegMin = curFaultSeg.getMinMag();
                if (curSegMax > maxSlip)
                    maxSlip = curSegMax;
                if (curSegMin < minSlip)
                    minSlip = curSegMin;
            }
        }

        // Display the colorbar spectrum on the jzy panel.
        if (colorBar != null) {
            chart.removeRenderer(colorBar);
        }
        colorBar = new ColorStrip((float) minSlip, (float) maxSlip);
        chart.addRenderer(colorBar);

        // now update the color
        for (AbstractDrawable model : sourceModels) {
            if (model instanceof DistributedFaultViewable) {
                DistributedFaultViewable curFaultSeg = (DistributedFaultViewable) model;
                int rowCt = curFaultSeg.getRowCt();
                int colCt = curFaultSeg.getColCt();
                OkadaFault3[][] subfaults = curFaultSeg.getSubfaults();

                for (int rowIter = 0; rowIter < rowCt; rowIter++) {
                    for (int colIter = 0; colIter < colCt; colIter++) {
                        double curMag = subfaults[rowIter][colIter].getMagnitude();
                        Color subFaultColor = colorBar.getColor((float) curMag);
                        subFaultColor.alphaSelf(0.75f);
                        curFaultSeg.setsubFaultColor(rowIter, colIter, subFaultColor);
                    }
                }

            }
        }
    }

    private void refreshModeledDisps() {
        List<VectorXyz> vectors = simModel.getModeledDispVectors();
        double scale = displaySettings.getDisplacementVectorScale();
        Color color = fromAWT(displaySettings.getModeledDisplacementVectorColor());
        LLH origin = simModel.getOrigin();

        for (Vector3D vector : modeledVectors) {
            chart.getScene().remove(vector, false);
        }
        modeledVectors = new ArrayList<Vector3D>(vectors.size());

        for (VectorXyz dv : vectors) {
            Vector3D v3D = new Vector3D(dv, origin, scale, color);
            modeledVectors.add(v3D);
            chart.getScene().add(v3D, false);
        }

    }

    protected static org.jzy3d.colors.Color fromAWT(java.awt.Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    
    public void snapToXy(){
        Camera c = chart.getView().getCamera();
        Coord3d eye = c.getEye();
        // No, I do not understand why these particular numbers work
        eye.x = -1.57f;
        eye.y = 1.5707964f;
        chart.setViewPoint(eye);       
    }

    public Chart getChart() {
        return chart;
    }
    
    public void dragModeOn(){
        this.snapToXy();
        this.mouseController.setDragXy(true);
    }
    
    public void dragModeOff(){
        this.mouseController.setDragXy(false);
    }
    
    public void toggleDragMode(){
        if (this.mouseController.isDragXy()){
            dragModeOff();
        } else {
            dragModeOn();
        }
    }

}
