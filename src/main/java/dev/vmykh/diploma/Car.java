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
	private double frontAxisAngle;

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
		this.orientationAngle = alpha;
		return this;
	}

	public Car setInitialFrontAxisAngle(double angle) {
		this.frontAxisAngle = angle;
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

	public double getOrientationAngle() {
		return orientationAngle;
	}

	public double getFrontAxisAngle() {
		return frontAxisAngle;
	}

	public void setFrontAxisAngle(double frontAxisAngle) {
		this.frontAxisAngle = frontAxisAngle;
	}

	public void moveForward(double distance) {
		if (abs(frontAxisAngle) < 0.0001) {
			x += distance * cos(orientationAngle);
			y += distance * sin(orientationAngle);
		} else {
			double orientAngleShifted = orientationAngle + PI / 2;

			Point backAxisCenter = getBackAxisCenter();
			Point backAxisExtraPoint = new Point(
					backAxisCenter.getX() + cos(orientAngleShifted),
					backAxisCenter.getY() + sin(orientAngleShifted)
			);
			Map<String, Double> backAxisCoefs = lineEquation(backAxisCenter, backAxisExtraPoint);

			Point frontAxisCenter = getFrontAxisCenter();
			Point frontAxisExtraPoint = new Point(
					frontAxisCenter.getX() + cos(orientAngleShifted + frontAxisAngle),
					frontAxisCenter.getY() + sin(orientAngleShifted + frontAxisAngle)
			);
			Map<String, Double> frontAxisCoefs = lineEquation(frontAxisCenter, frontAxisExtraPoint);

			// find intersection point of two lines
			RealMatrix coefficients =
					new Array2DRowRealMatrix(new double[][]
							{
									{ frontAxisCoefs.get("a"), frontAxisCoefs.get("b") },
									{ backAxisCoefs.get("a"), backAxisCoefs.get("b") }
							},
							false);

			DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();

			RealVector constants = new ArrayRealVector(
					new double[] { -frontAxisCoefs.get("c"), -backAxisCoefs.get("c") },
					false);
			RealVector solution = solver.solve(constants);

			Point rotationCircleCenter = new Point(solution.getEntry(0), solution.getEntry(1));


			Point carCenter = new Point(x, y);
			Vector fromCircleCenterToCarCenter = new Vector(rotationCircleCenter, carCenter);
			double rotationRadius = fromCircleCenterToCarCenter.getMagnitude();

			Point pointOnMainAxis = new Point(x + cos(orientationAngle), y + sin(orientationAngle));
			Vector carDirection = new Vector(carCenter, pointOnMainAxis);

			boolean rotateCounterClockWise = fromCircleCenterToCarCenter.crossProduct(carDirection) > 0.0;

			double startAngle = fromCircleCenterToCarCenter.getAngle();
			double rotationAngleAbs = distance / rotationRadius;
			double rotationAngle = rotateCounterClockWise ? rotationAngleAbs : -rotationAngleAbs;
			double endAngle = startAngle + rotationAngle;

			x = rotationCircleCenter.getX() + rotationRadius * cos(endAngle);
			y = rotationCircleCenter.getY() + rotationRadius * sin(endAngle);
			orientationAngle += rotationAngle;
		}
	}

	// (x-x1)/(x2-x1) = (y-y1)/(y2-y1) => ax+by+c=0
	private static Map<String, Double> lineEquation(Point p1, Point p2) {
		double xDiff = p2.getX() - p1.getX();
		double yDiff = p2.getY() - p1.getY();

		Map<String, Double> coefs = new HashMap<>(3);
		coefs.put("a", yDiff);
		coefs.put("b", -xDiff);
		coefs.put("c", -p1.getX() * yDiff + p1.getY() * xDiff);

		return coefs;
	}

	public Point getBackAxisCenter() {
		double xShift = 0.5 * length * cos(orientationAngle);
		double yShift = 0.5 * length * sin(orientationAngle);

		double backCenterX = x - xShift;
		double backCenterY = y - yShift;

		return new Point(backCenterX, backCenterY);
	}

	public Point getFrontAxisCenter() {
		double xShift = 0.5 * length * cos(orientationAngle);
		double yShift = 0.5 * length * sin(orientationAngle);

		double frontCenterX = x + xShift;
		double frontCenterY = y + yShift;

		return new Point(frontCenterX, frontCenterY);
	}
}
