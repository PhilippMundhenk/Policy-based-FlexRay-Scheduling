package flex;


import java.io.Serializable;

import ch.ethz.rtc.kernel.Curve;

public class CurveSet implements Cloneable, Serializable {

	private static final long serialVersionUID = 3760450672266388036L;
	protected final Curve upper;
	protected final Curve lower;

	public CurveSet(Curve upper, Curve lower) {
		this.upper = upper;
		this.lower = lower;
	}

	public Curve getUpper() {
		return upper;
	}

	public Curve getLower() {
		return lower;
	}

	@Override
	public String toString() {
		return "CurveSet [lower=" + lower + ", upper=" + upper + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lower == null) ? 0 : lower.hashCode());
		result = prime * result + ((upper == null) ? 0 : upper.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CurveSet other = (CurveSet) obj;
		if (lower == null) {
			if (other.lower != null)
				return false;
		} else if (!lower.equals(other.lower))
			return false;
		if (upper == null) {
			if (other.upper != null)
				return false;
		} else if (!upper.equals(other.upper))
			return false;
		return true;
	}

	public CurveSet clone() {
		CurveSet curves = new CurveSet(upper.clone(), lower.clone());
		return curves;
	}

}
