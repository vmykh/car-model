package dev.vmykh.diploma;

import org.apache.commons.math3.linear.*;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

public final class Car {
	private final double width;
	private final double length;
	private double x;
	private double y;
	private double orientationAngle;
	private double steeringAngle;

	public Car(double width, double length) {
		this.width = width;
		this.length = length;
	}

	public Car setInitialPosition(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Car setInitialPosition(Point position) {
		this.x = position.getX();
		this.y = position.getY();
		return this;
	}

	public Car setInitialOrientation(double alpha) {
		this.orientationAngle = convertTo2PISystem(alpha);
		return this;
	}

	public Car setInitialSteeringAngle(double angle) {
		this.steeringAngle = angle;
		return this;
	}

	public double getWidth() {
		return width;
	}

	public double getLength() {
		return length;
	}

	public double getX() {
		return x;
	}


	public double getY() {
		return y;
	}

	public Point getCenter() {
		return getBackAxleCenter().add(
				new Vector(getBackAxleCenter(), getFrontAxleCenter())
						.multipliedBy(0.5)
		);
//		return new Point(x, y);
	}

	public double getOrientationAngle() {
		return orientationAngle;
	}

	public Vector getOrientationVector() {
		return Vector.fromAngle(orientationAngle);
	}

	public double getSteeringAngle() {
		return steeringAngle;
	}

	public Car withFrontAxisAngle(double frontAxisAngle) {
		Car car = new Car(width, length);
		car.setInitialPosition(x, y);
		car.setInitialOrientation(orientationAngle);
		car.setInitialSteeringAngle(frontAxisAngle);
		return car;
	}

	public Car movedBy(double distance) {
		double newX;
		double newY;
		double newOrientationAngle;

		if (abs(steeringAngle) < 0.0001) {
			newX = x + distance * cos(orientationAngle);
			newY = y + distance * sin(orientationAngle);
			newOrientationAngle = orientationAngle;
		} else {
			Point rotationCircleCenter = computeRotationCircleCenter();

			Vector fromCircleCenterToBackAxleCenter = new Vector(rotationCircleCenter, getBackAxleCenter());
			double rotationRadius = fromCircleCenterToBackAxleCenter.length();

			Vector carDirection = new Vector(getBackAxleCenter(), getFrontAxleCenter());

			boolean rotateCounterClockWise = fromCircleCenterToBackAxleCenter.crossProduct(carDirection) > 0.0;

			double startAngle = fromCircleCenterToBackAxleCenter.angle();
			double rotationAngleAbs = distance / rotationRadius;
			double rotationAngle = rotateCounterClockWise ? rotationAngleAbs : -rotationAngleAbs;
			double endAngle = startAngle + rotationAngle;

			newX = rotationCircleCenter.getX() + rotationRadius * cos(endAngle);
			newY = rotationCircleCenter.getY() + rotationRadius * sin(endAngle);
			newOrientationAngle = orientationAngle + rotationAngle;
		}

		Car movedCar = new Car(width, length);
		movedCar.setInitialPosition(newX, newY);
		movedCar.setInitialOrientation(newOrientationAngle);
		movedCar.setInitialSteeringAngle(steeringAngle);
		return movedCar;
	}

	private static double convertTo2PISystem(double angle) {
		double mod = 2 * PI;
		double angleIn2PI = angle % mod;
		if (angleIn2PI < 0)
		{
			angleIn2PI += mod;
		}
		return angleIn2PI;
	}

	public double getRotationCircleRadius() {
		if (abs(steeringAngle) < 0.0001) {
			// TODO(vmykh): maybe just return infinity ?
			throw new RuntimeException();
		} else {
			Point rotationCircleCenter = computeRotationCircleCenter();
			Vector fromCircleCenterToCarCenter = new Vector(rotationCircleCenter, getBackAxleCenter());
			return fromCircleCenterToCarCenter.length();
		}
	}

	private Point computeRotationCircleCenter() {
		Point backAxleCenter = getBackAxleCenter();
		Map<String, Double> backAxleLineEquationCoefs =
				lineEquation(Vector.fromAngle(orientationAngle), backAxleCenter);

		Point frontAxleCenter = getFrontAxleCenter();
		Map<String, Double> frontAxleLineEquationCoefs =
				lineEquation(Vector.fromAngle(orientationAngle + steeringAngle), frontAxleCenter);

		RealMatrix coefficients =
				new Array2DRowRealMatrix(new double[][]
						{
								{ frontAxleLineEquationCoefs.get("a"), frontAxleLineEquationCoefs.get("b") },
								{ backAxleLineEquationCoefs.get("a"), backAxleLineEquationCoefs.get("b") }
						},
						false);

		DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();

		RealVector constants = new ArrayRealVector(
				new double[] { -frontAxleLineEquationCoefs.get("c"), -backAxleLineEquationCoefs.get("c") },
				false);
		RealVector solution = solver.solve(constants);

		return new Point(solution.getEntry(0), solution.getEntry(1));
	}

	// A(x-x0) + B(y-y0) = 0 => Ax + By + C = 0
	private static Map<String, Double> lineEquation(Vector perpendicular, Point point) {
		double a = perpendicular.getX();
		double b = perpendicular.getY();

		Map<String, Double> coefs = new HashMap<>(3);
		coefs.put("a", a);
		coefs.put("b", b);
		coefs.put("c", -(a * point.getX() + b * point.getY()));

		return coefs;
	}

	public Point getBackAxleCenter() {
		return new Point(x, y);
	}

	public Point getFrontAxleCenter() {
		return getBackAxleCenter().add(Vector.fromAngle(orientationAngle).normalized().multipliedBy(length));
	}

	@Override
	public String toString() {
		return "Position: " + getCenter() + "   Orientation: " + getOrientationAngle();
	}
}
