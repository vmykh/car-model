package dev.vmykh.diploma;

import java.util.Arrays;

import static java.lang.Math.PI;

public final class CellWeight {
	public static final int COMMON_WEIGHT_COEF = 1;
	public static final int DIRECTION_WEIGHT_COEF = 20;
	private static final double TWO_PI = 2 * Math.PI;

	private final int directions;
	private int commonWeight;
	private final int[] weights;
	private final double[] boundaries;

	public CellWeight(int directions) {
		this.directions = directions;
		this.weights = new int[directions];
		Arrays.fill(this.weights, 0);
		this.boundaries = createBoundaries(directions);
	}

	private double[] createBoundaries(int directions) {
		double[] boundaries = new double[directions];
		double delta = TWO_PI / directions;
		double halfDelta = delta / 2;
		for (int i = 0; i < directions; i++) {
			boundaries[i] = halfDelta + i * delta;
		}
		return boundaries;
	}

	public int getWeight(double directionAngle) {
		double adjustedAngle = modulo(directionAngle, TWO_PI);
		// TODO(vmykh): maybe add binary search here?
		int directionWeight = weights[0];
		for (int i = 0; i < directions; i++) {
			if (adjustedAngle < boundaries[i]) {
				directionWeight = weights[i];
				break;
			}
		}
		return COMMON_WEIGHT_COEF * commonWeight + DIRECTION_WEIGHT_COEF * directionWeight;
	}

	public void addWeight(double directionAngle) {
		commonWeight++;
		double adjustedAngle = modulo(directionAngle, TWO_PI);
		int indexOfAppropriatePartition = 0;
		for (int i = 0; i < directions; i++) {
			if (adjustedAngle < boundaries[i]) {
				indexOfAppropriatePartition = i;
				break;
			}
		}
		weights[indexOfAppropriatePartition]++;
	}

	private static double modulo(double number, double mod) {
		double r = number % mod;
		if (r < 0)
		{
			r += mod;
		}
		return r;
	}
}
