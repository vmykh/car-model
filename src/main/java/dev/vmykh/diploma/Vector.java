package dev.vmykh.diploma;

import static java.lang.Math.*;

public final class Vector {
	private final double x;
	private final double y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector(Point p1, Point p2) {
		this.x = p2.getX() - p1.getX();
		this.y = p2.getY() - p1.getY();
	}

	public static Vector fromAngle(double angle) {
		return new Vector(cos(angle), sin(angle));
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double length() {
		return sqrt(x*x + y*y);
	}

	public double angle() {
		double alpha = atan2(y, x);
		return alpha > 0 ? alpha : 2 * PI + alpha;
	}

	public Vector negative() {
		return new Vector(-x, -y);
	}

	public Vector normalized() {
		return new Vector(x / length(), y / length());
	}

	public Vector perpendicular() {
		return new Vector(-y, x);
	}

	public Vector multipliedBy(double coefficient) {
		return new Vector(coefficient * x, coefficient * y);
	}

	public Vector add(Vector other) {
		return new Vector(x + other.x, y + other.y);
	}

	public double dotProduct(Vector other) {
		return this.x * other.x + this.y * other.y;
	}

	public double crossProduct(Vector other) {
		return this.x * other.y - other.x * this.y;
	}

	public double angleTo(Vector other) {
		double angle = atan2(this.crossProduct(other), this.dotProduct(other));
		return angle > 0 ? angle : 2 * PI + angle;
	}

	@Override
	public String toString() {
		return "Vector{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
