package gov.usgs.dismodel.geom.overlays.jzy;

import net.masagroup.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public class AutoKmTicker implements ITickRenderer {
	String formatString;
	    
	    public AutoKmTicker(){
			this("%9.0f");
		}
		
		public AutoKmTicker(String formatString){
			this.formatString = formatString;
		}
		
		@Override
		public String format(float value) {
			if (Math.abs(value) >= 1000.0 || value == 0f) {
				return String.format(formatString, value / 1000.0);
			} else {
				return String.format("%1.1e", value / 1000.0);
			}
			
		}

}
