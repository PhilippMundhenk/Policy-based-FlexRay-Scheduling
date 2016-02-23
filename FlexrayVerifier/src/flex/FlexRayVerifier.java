package flex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.rtc.kernel.Curve;
import flex.Curves.MPAResult;

public class FlexRayVerifier<M> {

	protected final int numberOfSlots;
	protected final int numberOfCycles;
	protected final int cycleLengthsInMs;
	protected final double slotLengthsInMs;
	protected final int slotSizeInBytes;

	protected boolean[][] useSlot;
	protected Map<M, Integer> messagePriority = new HashMap<M, Integer>();
	protected Map<M, Integer> messageSizeInBytes = new HashMap<M, Integer>();
	protected Map<M, String> messageNames = new HashMap<M, String>();
	protected Map<M, Integer> messagePeriodInMs = new HashMap<M, Integer>();
	protected Map<M, Double> messageDelay = new HashMap<M, Double>();
	protected Map<M, Double> messageBuffer = new HashMap<M, Double>();

	public FlexRayVerifier(int numberOfSlots, int numberOfCycles, int cycleLengthsInMs, double slotLengthsInMs, int slotSizeInBytes) {
		super();
		this.numberOfSlots = numberOfSlots;
		this.numberOfCycles = numberOfCycles;
		this.cycleLengthsInMs = cycleLengthsInMs;
		this.slotLengthsInMs = slotLengthsInMs;
		this.slotSizeInBytes = slotSizeInBytes;

		useSlot = new boolean[numberOfSlots][numberOfCycles];

		if (slotLengthsInMs * numberOfSlots > cycleLengthsInMs) {
			throw new IllegalArgumentException("Invalid values");
		}
	}

	public void addSlotAtCycle(int slot, int cycle) {
		useSlot[slot][cycle] = true;
	}

	public void addMessage(M message, int priority, int sizeInBytes, int periodInMs, String messageName) {
		messagePriority.put(message, priority);
		messageSizeInBytes.put(message, sizeInBytes);
		messagePeriodInMs.put(message, periodInMs);
		messageNames.put(message, messageName);
	}
	
	public Double getDelay(M message){
		return messageDelay.get(message);
	}
	
	public Double getBuffer(M message){
		return messageBuffer.get(message);
	}
	

	public void calculate() {

		// determine service curve
		List<List<Double>> values = new ArrayList<List<Double>>();

		if (!useSlot[0][0]) {
			values.add(Arrays.asList(0d, 0d, 0d));
		}

		double y = 0d;

		for (int c = 0; c < numberOfCycles; c++) {
			for (int s = 0; s < numberOfSlots; s++) {
				if (useSlot[s][c]) {
					double x = s * slotLengthsInMs + c * cycleLengthsInMs;
					y += slotSizeInBytes;
					values.add(Arrays.asList(x, y, 0d));
				}
			}
		}

		double aValues[][] = new double[values.size()][3];
		for (int i = 0; i < aValues.length; i++) {
			for (int j : Arrays.asList(0, 1, 2)) {
				aValues[i][j] = values.get(i).get(j);
			}
		}

		Curve curve = new Curve(aValues, numberOfCycles * cycleLengthsInMs);
		Curve upper = Curves.mindeconv(curve, curve);
		Curve lower = Curves.maxdeconv(curve, curve);
		CurveSet service = new CurveSet(upper, lower);

		
		
		List<M> messages = new ArrayList<M>(messagePriority.keySet());
		Collections.sort(messages, new Comparator<M>() {
			@Override
			public int compare(M o1, M o2) {
				return messagePriority.get(o1).compareTo(messagePriority.get(o2));
			}
		});
		
		for(M message: messages){
			int sizeInBytes = messageSizeInBytes.get(message);
			int periodInMs = messagePeriodInMs.get(message);
			
			CurveSet arrival = Curves.times(Curves.toCurve(periodInMs), sizeInBytes);
		
//			Plotter plotter = new Plotter((numberOfCycles * cycleLengthsInMs + 20)/30);
//			plotter.add(arrival, Color.BLACK, "arrival");
//			plotter.add(service, Color.RED, "Service");
//			plotter.plot();
			
			MPAResult result = Curves.greedy(arrival, service);
			messageDelay.put(message, result.getDelay()+slotLengthsInMs);
			messageBuffer.put(message, result.getBuffer());
			
			service = result.getService();
			
		}
		
		
	}

}
