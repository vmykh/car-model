package dev.vmykh.diploma;

import java.util.ArrayList;
import java.util.List;

public final class CollisionDetector {
	private static final int POINTS_PER_CAR_SIDE = 8;

	private final List<Obstacle> obstacles;
	private final double worldWidth;
	private final double worldHeight;

	public CollisionDetector(List<Obstacle> obstacles, double worldWidth, double worldHeight) {
		this.obstacles = new ArrayList<>(obstacles);
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
	}

	public boolean collides(Car car) {
		double carX = car.getCenter().getX();
		double carY = car.getCenter().getY();

		if (carX < 0.0 || carX > worldWidth || carY < 0.0 || carY > worldHeight) {
			return true;
		}

		for (Obstacle obstacle : obstacles) {
			List<Point> carSidePoints = generateCarSidePoints(car);
			for (Point carSidePoint : carSidePoints) {
				if (obstacle.contains(carSidePoint)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<Point> generateCarSidePoints(Car car) {
		Vector fromRearToFront = new Vector(car.getBackAxisCenter(), car.getFrontAxisCenter());
		Vector fromRightWheelToLeftWheel = fromRearToFront.perpendicular().normalized().multipliedBy(car.getWidth());

		Point rearLeft = car.getBackAxisCenter().add(fromRightWheelToLeftWheel.multipliedBy(0.5));
		Point frontLeft = rearLeft.add(fromRearToFront);
		Point frontRight = frontLeft.add(fromRightWheelToLeftWheel.negative());
		Point rearRight = rearLeft.add(fromRightWheelToLeftWheel.negative());

		List<Point> carSidePoints = new ArrayList<>(POINTS_PER_CAR_SIDE * 4);
		carSidePoints.addAll(pointsBetween(rearLeft, frontLeft, POINTS_PER_CAR_SIDE));
		carSidePoints.addAll(pointsBetween(frontLeft, frontRight, POINTS_PER_CAR_SIDE));
		carSidePoints.addAll(pointsBetween(rearLeft, rearRight, POINTS_PER_CAR_SIDE));
		carSidePoints.addAll(pointsBetween(rearRight, frontRight, POINTS_PER_CAR_SIDE));

		return carSidePoints;
	}

	private List<Point> pointsBetween(Point p1, Point p2, int amount) {
		double xStep = (p2.getX() - p1.getX()) / (double) amount;
		double yStep = (p2.getY() - p1.getY()) / (double) amount;
		List<Point> points = new ArrayList<>(amount);
		for (int i = 0; i < amount; i++) {
			Point current = new Point(p1.getX() + xStep * i, p1.getY() + yStep * i);
			points.add(current);
		}
		return points;
	}
}
