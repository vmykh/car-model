package dev.vmykh.diploma;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.round;

public final class CollisionDetectorDiscreteField implements CollisionDetector {
	private static final int POINTS_PER_CAR_SIDE = 8;

	private final HashSet<IntegerPoint> obstacleIndexes;
	private final double obstacleSize;
	private final double worldWidth;
	private final double worldHeight;

	public CollisionDetectorDiscreteField(Set<IntegerPoint> obstacleIndexes, double obstacleSize,
	                                      double worldWidth, double worldHeight) {
		this.obstacleIndexes = new HashSet<>(obstacleIndexes);
		this.obstacleSize = obstacleSize;
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
	}

	public boolean collides(Car car) {
		double stepForCarWidth = car.getBodyWidth() / ceil(car.getBodyWidth() / (obstacleSize / 2));
		int itersForCarWidth = (int) round(car.getBodyWidth() / stepForCarWidth) + 1;
		double stepForCarLength = car.getBodyLength() / ceil(car.getBodyLength() / (obstacleSize / 2));
		int itersForCarLength = (int) round(car.getBodyLength() / stepForCarLength) + 1;

		// maybe it will be more efficient to start with point on front side of car,
		// because car usually goes forward
		Point backRightCorner = car.getBackAxleCenter()
				.add(
						car.getOrientationVector()
								.negative()
								.normalizedTo((car.getBodyLength() - car.getChassisLength()) / 2.0)
				)
				.add(
					car.getOrientationVector()
							.perpendicular()
							.negative()
							.normalizedTo(car.getBodyWidth() / 2.0)
				);

		Vector carLengthStepVector = car.getOrientationVector().normalizedTo(stepForCarLength);
		Vector carWidthStepVector = car.getOrientationVector().perpendicular().normalizedTo(stepForCarWidth);

		for (int i = 0; i < itersForCarLength; i++) {
			Point pointOnRightSide = backRightCorner.add(carLengthStepVector.multipliedBy(i));
			for (int j = 0; j < itersForCarWidth; j++) {
				Point point = pointOnRightSide.add(carWidthStepVector.multipliedBy(j));
				if (isNotOnField(point) || isObstacle(point)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isObstacle(Point point) {
		int obstacleXIndex = (int) floor(point.getX() / obstacleSize);
		int obstacleYIndex = (int) floor(point.getY() / obstacleSize);
		return obstacleIndexes.contains(new IntegerPoint(obstacleXIndex, obstacleYIndex));
	}

	private boolean isNotOnField(Point point) {
		return point.getX() < 0.0 || point.getX() > worldWidth || point.getY() < 0.0 || point.getY() > worldHeight;
	}
}
