package flex;


import java.awt.Color;

import ch.ethz.rtc.kernel.Curve;
import ch.ethz.rtc.kernel.CurveFactory;
import ch.ethz.rtc.kernel.CurveMath;

public class Curves {

	public static class PJDS {
		public final long p;
		public final double j;
		public final double d;
		public final double s;

		public PJDS(long p, double j, double d, double s) {
			super();
			this.p = p;
			this.j = j;
			this.d = d;
			this.s = s;
		}

		public long getP() {
			return p;
		}

		public double getJ() {
			return j;
		}

		public double getD() {
			return d;
		}

		public double getS() {
			return s;
		}

		@Override
		public String toString() {
			return "PJDS [p=" + p + ", j=" + j + ", d=" + d + ", s=" + s + "]";
		}

	}

	public static CurveSet toCurve(long p) {
		return toCurve(p, 0);
	}

	public static CurveSet toCurve(long p, double j) {
		return toCurve(p, j, 0);
	}

	public static CurveSet toCurve(long p, double j, double d) {
		try {
			Curve upper = CurveFactory.createUpperPJDCurve(p, j, d, "void");
			Curve lower = CurveFactory.createLowerPJDCurve(p, j, d, "void");
			CurveSet curves = new CurveSet(upper, lower);
			return curves;
		} catch (IllegalArgumentException e) {
			System.err.println("p=" + p + ",j=" + j + ",d=" + d);
			throw e;
		}

	}

	public static PJDS toPJD(CurveSet curves) {
		Curve upper = curves.getUpper();
		Curve lower = curves.getLower();

		long p;
		double j;
		double d;
		double s;

		double slopeUpper = upper.tightUpperBound().s();
		double slopeLower = lower.tightUpperBound().s();

		if (slopeUpper != slopeLower) {
			Plotter plotter = new Plotter(300);
			plotter.add(upper, Color.RED, "upper");
			plotter.add(lower, Color.BLUE, "lower");
			plotter.plot();
			throw new IllegalArgumentException(
					"upper and lower curves have different slope");
		}

		double period = 1.0 / slopeUpper;

		if (Math.abs(period - Math.round(period)) > 10e-6) {
			Plotter plotter = new Plotter(300);
			plotter.add(upper, Color.RED, "upper");
			plotter.add(lower, Color.BLUE, "lower");
			plotter.plot();
			throw new IllegalArgumentException(
					"1/slope of input curves is not an integer (slope="+slopeUpper+")");
		}

		p = Math.round(period);
		s = 1;

		CurveSet a = times(toCurve(p), s);
		double jitteru = getMaxYDistance(upper, a.getUpper());
		double jitterl = getMaxYDistance(a.getLower(), lower);
		j = Math.max(jitteru, jitterl);

		// Binary search

		// determine lower bound
		double dLower = 0;

		// determine upper bound
		double dUpper = 0.;
		double diff = ((double) p) / 2.0;

		double distance = 0;
		while (dUpper / period < 0.95) { // make sure that we are not too close
			// to period
			Curve b = times(CurveFactory.createUpperPJDCurve(p, j, dUpper,
					"void"), s);
			distance = getMaxYDistance(upper, b);
			if (distance > 0) { // not feasible
				break;
			}
			dUpper = dUpper + diff;
			diff = diff / 2;
		}
		if (distance == 0) { // d is very close to period; just stop to come
			// closer
			d = dUpper - 2 * diff;
			return new PJDS(p, j, d, s);
		}

		// perform binary search; dLower feasible, dUpper not feasible
		while ((dUpper - dLower) / period > 0.0001) { // precision
			d = (dUpper + dLower) / 2;
			Curve b = times(CurveFactory.createUpperPJDCurve(p, j, d, "void"),
					s);
			distance = getMaxYDistance(upper, b);
			if (distance == 0) { // feasible
				dLower = d;
			} else {// not feasible
				dUpper = d;
			}
		}
		d = dLower;

		return new PJDS(p, j, d, s);
	}

	public static Curve times(Curve curve, double value) {
		Curve result = clone(curve);
		result.scaleY(value);
		return result;
	}

	public static Curve divide(Curve a, double v) {
		Curve result = clone(a);
		result.scaleY(1.0 / v);
		return result;
	}

	public static CurveSet times(CurveSet curves, double value) {
		CurveSet result = clone(curves);
		result.getUpper().scaleY(value);
		result.getLower().scaleY(value);
		return result;
	}

	public static Curve minus(Curve a, Curve b) {
		return CurveMath.sub(a, b);
	}
	
	public static Curve plus(Curve a, Curve b){
		return CurveMath.add(a, b);
	}

	public static Curve maxconv(Curve a, double v) {
		if (v == 0) {
			return CurveMath.maxPlusConv0(a);
		} else {
			Curve b = new Curve(new double[][] { { 0, v, 0 } });
			return CurveMath.maxPlusConv(a, b);
		}
	}

	public static Curve maxdeconv(Curve a, double v) {
		if (v == 0) {
			return CurveMath.maxPlusDeconv0(a);
		} else {
			Curve b = new Curve(new double[][] { { 0, v, 0 } });
			return CurveMath.maxPlusDeconv(a, b);
		}
	}
	
	public static Curve maxdeconv(Curve a, Curve b){
		return CurveMath.maxPlusDeconv(a, b);
	}

	public static Curve minconv(Curve a, Curve b) {
		return CurveMath.minPlusConv(a, b);
	}

	public static Curve mindeconv(Curve a, Curve b) {
		return CurveMath.minPlusDeconv(a, b);
	}

	public static Curve min(Curve a, Curve b) {
		return CurveMath.min(a, b);
	}

	public static Curve max(Curve a, Curve b) {
		return CurveMath.max(a, b);
	}
	

	public static Curve max(Curve a, double v) {
		return max(a, new Curve(new double[][] { { 0, v, 0 } }));
	}

	public static Curve max(double v, Curve a) {
		return max(a, v);
	}

	public static Curve floor(Curve a) {
		return CurveMath.floor(a);
	}

	public static Curve ceil(Curve a) {
		return CurveMath.ceil(a);
	}

	// delay
	public static double h(Curve a, Curve b) {
		return CurveMath.maxHDist(a, b);
	}

	public static double delay(Curve a, Curve b, double ed) {
		b = max(0, floor(divide(b, ed)));
		return h(a, b);
	}

	// backlog
	public static double v(Curve a, Curve b) {
		return CurveMath.maxVDist(a, b);
	}

	public static double buffer(Curve a, Curve b, double ed) {
		b = max(0, floor(divide(b, ed)));
		return v(a, b);
	}

	public static CurveSet clone(CurveSet curves) {
		return new CurveSet(clone(curves.getUpper()), clone(curves.getLower()));
	}

	public static Curve clone(Curve curve) {
		return curve.clone();
	}

	public static double getMaxYDistance(CurveSet curves) {
		return getMaxYDistance(curves.getUpper(), curves.getLower());
	}

	public static double getMaxYDistance(Curve a, Curve b) {
		return CurveMath.maxHDist(a, b);
	}

	public static CurveSet toServiceCurveFS(double bandwidth) {
		Curve upperCurve = new Curve(new double[][] { { 0, 0, bandwidth } });
		Curve lowerCurve = new Curve(new double[][] { { 0, 0, bandwidth } });
		return new CurveSet(upperCurve, lowerCurve);
	}

	public static CurveSet toServiceCurveBD(double bandwidth, double delay) {
		Curve upperCurve = CurveFactory.createUpperBoundedDelayCurve(delay,
				bandwidth, "void");
		Curve lowerCurve = CurveFactory.createLowerBoundedDelayCurve(delay,
				bandwidth, "void");
		return new CurveSet(upperCurve, lowerCurve);
	}

	public static CurveSet toServiceCurveTDMA(double slot, long cycle,
			double bandwidth) {
		Curve upper = CurveFactory.createUpperTDMACurve(slot, cycle, bandwidth,
				"void");
		Curve lower = CurveFactory.createLowerTDMACurve(slot, cycle, bandwidth,
				"void");
		
		return new CurveSet(upper, lower);
	}

	public static class MPAResult {
		protected final CurveSet service;
		protected final CurveSet arrival;
		protected final double delay;
		protected final double buffer;

		public MPAResult(CurveSet arrival, CurveSet service, double delay,
				double buffer) {
			super();
			this.service = service;
			this.arrival = arrival;
			this.delay = delay;
			this.buffer = buffer;
		}

		public MPAResult(Curve arrivalUpper, Curve arrivalLower,
				Curve serviceUpper, Curve serviceLower, double delay,
				double buffer) {
			this(new CurveSet(arrivalUpper, arrivalLower), new CurveSet(
					serviceUpper, serviceLower), delay, buffer);
		}

		public CurveSet getService() {
			return service;
		}

		public CurveSet getArrival() {
			return arrival;
		}

		public double getDelay() {
			return delay;
		}

		public double getBuffer() {
			return buffer;
		}

	}

	public static MPAResult greedy(CurveSet arrival, CurveSet service) {
		return greedy(arrival, service, 1);
	}

	public static MPAResult greedy(CurveSet arrival, CurveSet service,
			double executionDemand) {
		return greedy(arrival, service, executionDemand, executionDemand);
	}

	public static MPAResult greedy(CurveSet arrival, CurveSet service,
			double executionDemandWC, double executionDemandBC) {

		Curve aui = arrival.getUpper();
		Curve ali = arrival.getLower();
		Curve bui = service.getUpper();
		Curve bli = service.getLower();

		double bced = executionDemandBC;
		double wced = executionDemandWC;

		Curve buo = maxdeconv(minus(bui, times(ali, bced)), 0);
		Curve blo = maxconv(minus(bli, times(aui, wced)), 0);

		bui = divide(bui, bced);
		bli = divide(bli, wced);

		Curve auo = ceil(min(mindeconv(minconv(aui, bui), bli), bui));
		Curve alo = floor(min(minconv(mindeconv(ali, bui), bli), bli));

		bli = max(0, floor(bli));
		double delay = h(aui, bli);
		double buffer = v(aui, bli);

		return new MPAResult(auo, alo, buo, blo, delay, buffer);

	}

}
