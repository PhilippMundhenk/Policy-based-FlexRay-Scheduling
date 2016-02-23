package flex;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import ptolemy.plot.Plot;
import ch.ethz.rtc.kernel.Curve;
import ch.ethz.rtc.kernel.MatlabUtil;

public class Plotter {
	
	protected double maxX = 10;
	
	protected List<Curve> curves = new ArrayList<Curve>();
	protected Map<Curve,Color> colors = new HashMap<Curve,Color>();
	protected Map<Curve,String> legend = new HashMap<Curve,String>();
	
	
	public Plotter(double maxX){
		this.maxX = maxX;
	}
	
	public void add(CurveSet curves, Color color, String name){
		add(curves.getUpper(), color, name+" (upper)");
		add(curves.getLower(), color, name+" (lower)");
	}
	
	public void add(Curve curve, Color color, String name){
		curves.add(curve);
		colors.put(curve, color);
		legend.put(curve, name);
	}
	
	public void plot(){
		
		Plot plot = new Plot();

		int dataset = 0;
		
		Color[] colors = new Color[curves.size()];

		double maxY = 0;
		for (Curve curve : curves) {
			addCurveToPlot(curve, plot, maxX, dataset);
			plot.addLegend(dataset, legend.get(curve));		
			colors[dataset] = this.colors.get(curve);
			
			dataset++;
			maxY = Math.max(maxY, getMaxYValue(curve, maxX));
		}
		
		plot.setColors(colors);
		plot.setYRange(0, maxY);
		plot.setXRange(0, maxX);
		plot.revalidate();
		plot.repaint();

		JFrame frame = new JFrame();
		frame.add(plot);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.repaint();
	}
	


	protected double getMaxYValue(Curve curve, double max) {
		double[][] result = MatlabUtil.printCurve(curve, max);
		return result[result.length - 1][1];
	}

	protected void addCurveToPlot(Curve curve, Plot plot, double max,
			int dataset) {

		double[][] result = MatlabUtil.printCurve(curve, max);

		for (int i = 0; i < result.length; i++) {
			plot.addPoint(dataset, result[i][0], result[i][1], true);
		}

	}

}
