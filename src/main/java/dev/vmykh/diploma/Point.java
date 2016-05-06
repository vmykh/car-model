package dev.vmykh.diploma;

import static java.lang.Math.sqrt;

public final class Point {
	private final double x;
	private final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double distanceTo(Point other) {
		double xDiff = this.x - other.x;
		double yDiff = this.y - other.y;
		return sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	public Point add(Point other) {
		return new Point(this.x + other.x, this.y + other.y);
	}

	public Point subtract(Point other) {
		return new Point(this.x - other.x, this.y - other.y);
	}

	@Override
	public String toString() {
		return "Point{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
