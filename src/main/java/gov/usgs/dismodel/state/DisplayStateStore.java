package gov.usgs.dismodel.state;

import java.awt.Color;

public class DisplayStateStore {
	//vector variables
	private Color realDisplacementVectorColor = new Color(219, 176, 4); // deep yellow
	private Color modeledDisplacementVectorColor  = Color.RED;
	private int displacementVectorScale = 20000;
	
	//ENU display bounds
	private double xCenter = 0;
	private double yCenter = 0;
	private double chartSpan = 40000;
	
	
	
	
	//getters and setters
	public Color getRealDisplacementVectorColor() {
		return realDisplacementVectorColor;
	}
	public void setRealDisplacementVectorColor(Color realDisplacementVectorColor) {
		this.realDisplacementVectorColor = realDisplacementVectorColor;
	}
	public Color getModeledDisplacementVectorColor() {
		return modeledDisplacementVectorColor;
	}
	public void setModeledDisplacementVectorColor(
			Color modeledDisplacementVectorColor) {
		this.modeledDisplacementVectorColor = modeledDisplacementVectorColor;
	}
	public int getDisplacementVectorScale() {
		return displacementVectorScale;
	}
	public void setDisplacementVectorScale(int displacementVectorScale) {
		this.displacementVectorScale = displacementVectorScale;
	}
	public double getxCenter() {
		return xCenter;
	}
	public void setxCenter(double xCenter) {
		this.xCenter = xCenter;
	}
	public double getyCenter() {
		return yCenter;
	}
	public void setyCenter(double yCenter) {
		this.yCenter = yCenter;
	}
	public double getChartSpan() {
		return chartSpan;
	}
	public void setChartSpan(double chartSpan) {
		this.chartSpan = chartSpan;
	}
	
	
	
}

