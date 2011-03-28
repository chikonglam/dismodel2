package gov.usgs.dismodel.gui.jzy3dOverrides;

import gov.usgs.dismodel.gui.ENUView.EnuViewerJzy2;
import net.masagroup.jzy3d.chart.Chart;
import net.masagroup.jzy3d.chart.controllers.mouse.ChartMouseController;
import net.masagroup.jzy3d.maths.BoundingBox3d;

public class JzyMouseListener extends ChartMouseController {
	
	EnuViewerJzy2 enuController;
	
    public JzyMouseListener(EnuViewerJzy2 enuController) {
		super();
		this.enuController = enuController;
	}

	protected void zoom(final float factor){
		enuController.zoomBy(factor);
	}
}