package dev.vmykh.diploma;

import static java.lang.Math.abs;

public class PIDController {
	private static final double SGN_ZERO_DEVIATION_DEFAULT = 1.0;

	private final double sgnZeroDeviation;
	private final double pCoef;
	private final double dCoef;
	private final double iCoef;

	private boolean firstIter = true;
	private double previousError = 0.0;
	private double accumulatedError = 0.0;

	public PIDController(double pCoef, double dCoef, double iCoef) {
		this.sgnZeroDeviation = SGN_ZERO_DEVIATION_DEFAULT;
		this.pCoef = pCoef;
		this.dCoef = dCoef;
		this.iCoef = iCoef;
	}

	public PIDController(double pCoef, double dCoef, double iCoef, double sgnZeroDeviation) {
		this.pCoef = pCoef;
		this.dCoef = dCoef;
		this.iCoef = iCoef;
		this.sgnZeroDeviation = sgnZeroDeviation;
	}

	public int currentError(double error) {
		accumulatedError += error;
		double derivative;
		if (firstIter) {
			derivative = 0.0;
			firstIter = false;
		} else {
			derivative = error - previousError;
		}
		previousError = error;
		return sgn(pCoef * error + dCoef * derivative + iCoef * accumulatedError);
	}

	public int sgn(double x) {
		if (abs(x) < sgnZeroDeviation) {
			return 0;
		} else if (x > 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
