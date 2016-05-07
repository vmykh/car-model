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

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getMagnitude() {
		return sqrt(x*x + y*y);
	}

	public double getAngle() {
		if (abs(x) < 0.000001) {
			if (y > 0) {
				return 0.5 * PI;
			} else {
				return 1.5 * PI;
			}
		} else {
			double angle = atan(y / x);
			if (angle < 0) {
				angle += PI;
			}
			if (y > 0) {
				return angle;
			} else {
				return angle + PI;
			}
		}
	}

	public Vector negative() {
		return new Vector(-x, -y);
	}

	public Vector normalized() {
		return new Vector(x / getMagnitude(), y / getMagnitude());
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

	public double crossProduct(Vector other) {
		return this.x * other.y - other.x * this.y;
	}

	@Override
	public String toString() {
		return "Vector{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
