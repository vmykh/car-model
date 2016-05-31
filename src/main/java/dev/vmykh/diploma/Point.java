package dev.vmykh.diploma;

import static java.lang.Math.sqrt;

public final class Point {
	private final double x;
	private final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point(IntegerPoint point) {
		this.x = point.getX();
		this.y = point.getY();
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

	public Point add(Vector direction) {
		return new Point(this.x + direction.getX(), this.y + direction.getY());
	}

	public Point subtract(Vector direction) {
		return new Point(this.x - direction.getX(), this.y - direction.getY());
	}

	@Override
	public String toString() {
		return "Point{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Point)) return false;

		Point point = (Point) o;

		if (Double.compare(point.x, x) != 0) return false;
		if (Double.compare(point.y, y) != 0) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
