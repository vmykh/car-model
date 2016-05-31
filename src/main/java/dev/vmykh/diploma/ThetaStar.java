package dev.vmykh.diploma;

import com.google.common.collect.Lists;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class ThetaStar {
	private final Field field;
	private final IntegerPoint start;
	private final IntegerPoint target;
	Set<IntegerPoint> exploredCells = new HashSet<>();

	public ThetaStar(Field field, IntegerPoint start, IntegerPoint target) {
		this.field = field;
		this.start = start;
		this.target = target;
	}

	public List<IntegerPoint> findPath() {
		PriorityQueue<PathCell> nextPathCells = new PriorityQueue<>();
		PathCell currentPathCell = new PathCell(start, 0, distanceBetween(start, target), null);

		while(!currentPathCell.getCoordinates().equals(target)) {
			IntegerPoint currentCell = currentPathCell.getCoordinates();
			exploredCells.add(currentCell);
			List<IntegerPoint> validNeighbors = getValidNextPathCellsOf(currentCell);

			for (IntegerPoint nextCell : validNeighbors) {
				exploredCells.add(nextCell);
				nextPathCells.add(createPathCell(currentPathCell, nextCell));
			}

			currentPathCell = nextPathCells.poll();
		}

		return createPath(currentPathCell);
	}

	private static List<IntegerPoint> createPath(PathCell lastPathCell) {
		List<IntegerPoint> path = new ArrayList<>();
		path.add(lastPathCell.getCoordinates());
		lastPathCell = lastPathCell.getParent();
		while(lastPathCell != null) {
			path.add(lastPathCell.getCoordinates());
			lastPathCell = lastPathCell.getParent();
		}
		return Lists.reverse(path);
	}

	private PathCell createPathCell(PathCell currentCell, IntegerPoint newPoint) {
		PathCell parent = currentCell.getParent();
		double distanceToTarget = distanceBetween(newPoint, target);
		if (parent != null && lineOfSight(parent.getCoordinates(), newPoint)) {
			double prevPathLength = parent.getPrevPathLenght() + distanceBetween(parent.getCoordinates(), newPoint);
			return  new PathCell(newPoint,  prevPathLength, distanceToTarget, parent);
		} else {
			double prevPathDistance =
					currentCell.getPrevPathLenght() + distanceBetween(currentCell.getCoordinates(), newPoint);
			return new PathCell(newPoint, prevPathDistance, distanceToTarget, currentCell);
		}
	}

	private boolean lineOfSight(IntegerPoint from, IntegerPoint to) {
		if (from.equals(to)) {
			return true;
		}

		int deltaX = abs(to.getX() - from.getX());
		int deltaY = abs(to.getY() - from.getY());

		if (deltaX >= deltaY) {
			if (from.getX() > to.getX()) {
				IntegerPoint temp = from;
				from = to;
				to = temp;
			}

			double yCoef = (to.getY() - from.getY()) / (double) deltaX;

			assert abs(yCoef) <= 1.0;

			int y1 = from.getY();
			int x1 = from.getX();
			int x2 = to.getX();
			int prevY = y1;
			for (int x = x1; x <= x2; x++) {
				double yReal = y1 + (x - x1) * yCoef;
				if (hardToRound(yReal)) {
					int bottomY = (int) Math.floor(yReal);
					int topY = (int) Math.ceil(yReal);
					if (field.isObstacle(x, bottomY) || field.isObstacle(x, topY)) {
						return false;
					}

					if (yCoef >= 0) {
						prevY = topY;
					} else {
						prevY = bottomY;
					}
				} else {
					int y = (int) Math.round(yReal);
					if (y == prevY) {
						if (field.isObstacle(x, y)) {
							return false;
						}
					} else {
						int bottomY = (int) Math.floor(yReal);
						int topY = (int) Math.ceil(yReal);
						if (nothingToRound(yReal) && abs(yCoef) > 0.0001) {
							if (yCoef > 0) {
								topY = (int) Math.round(yReal);
								bottomY = topY - 1;
							} else {
								bottomY = (int) Math.round(yReal);
								topY = bottomY + 1;
							}
						}
						if (field.isObstacle(x, bottomY) || field.isObstacle(x, topY)
								|| field.isObstacle(x - 1, bottomY) || field.isObstacle(x - 1, topY)) {
							return false;
						}
					}
				}
			}
		} else {
			if (from.getY() > to.getY()) {
				IntegerPoint temp = from;
				from = to;
				to = temp;
			}

			double xCoef = (to.getX() - from.getX()) / (double) deltaY;

			assert abs(xCoef) < 1.0 ;

			int x1 = from.getX();
			int y1 = from.getY();
			int y2 = to.getY();
			int prevX = x1;
			for (int y = y1; y <= y2; y++) {
				double xReal = x1 + (y - y1) * xCoef;
				if (hardToRound(xReal)) {
					int leftX = (int) Math.floor(xReal);
					int rightX = (int) Math.ceil(xReal);
					if (field.isObstacle(leftX, y) || field.isObstacle(rightX, y)) {
						return false;
					}

					if (xCoef >= 0) {
						prevX = rightX;
					} else {
						prevX = leftX;
					}
				} else {
					int x = (int) Math.round(xReal);
					if (x == prevX) {
						if (field.isObstacle(x, y)) {
							return false;
						}
					} else {
						int leftX = (int) Math.floor(xReal);
						int rightX = (int) Math.ceil(xReal);

						if (nothingToRound(xReal) && abs(xCoef) > 0.0001) {
							if (xCoef > 0) {
								rightX = (int) Math.round(xReal);
								leftX = rightX - 1;
							} else {
								leftX = (int) Math.round(xReal);
								rightX = leftX + 1;
							}
						}

						if (field.isObstacle(leftX, y) || field.isObstacle(rightX, y)
								|| field.isObstacle(leftX, y - 1) || field.isObstacle(rightX, y - 1)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	private static boolean hardToRound(double x) {
		long wholePart = (long) x;
		double fraction = x - wholePart;
		return abs(fraction - 0.5) < 0.01;
	}

	private static boolean nothingToRound(double x) {
		long wholePart = (long) x;
		double fraction = x - wholePart;
		return abs(fraction) < 0.00001;
	}

	private static double distanceBetween(IntegerPoint p1, IntegerPoint p2) {
		double xDiff = p1.getX() - p2.getX();
		double yDiff = p1.getY() - p2.getY();
		return sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	private List<IntegerPoint> getValidNextPathCellsOf(IntegerPoint cell) {
		List<IntegerPoint> neighbors = new ArrayList<>();

		IntegerPoint left = new IntegerPoint(cell.getX() - 1, cell.getY());
		if (isValidPathPoint(left)) {
			neighbors.add(left);
		}

		IntegerPoint right = new IntegerPoint(cell.getX() + 1, cell.getY());
		if (isValidPathPoint(right)) {
			neighbors.add(right);
		}

		IntegerPoint bottom = new IntegerPoint(cell.getX(), cell.getY() - 1);
		if (isValidPathPoint(bottom)) {
			neighbors.add(bottom);
		}

		IntegerPoint top = new IntegerPoint(cell.getX(), cell.getY() + 1);
		if (isValidPathPoint(top)) {
			neighbors.add(top);
		}

		return neighbors;
	}

	private boolean isValidPathPoint(IntegerPoint point) {
		int x = point.getX();
		int y = point.getY();

		if (x < 0 || x >= field.getWidth()) {
			return false;
		}
		if (y < 0 || y >= field.getHeight()) {
			return false;
		}

		if (field.isObstacle(x, y)) {
			return false;
		}

		if (exploredCells.contains(point)) {
			return false;
		}

		return true;
	}

	private static final class PathCell implements Comparable<PathCell> {
		private final IntegerPoint coordinates;
		private final double prevPathLenght;
		private final double distanceToTarget;
		private final double heuristic;
		private final PathCell parent;

		public PathCell(IntegerPoint coordinates, double prevPathLenght, double distanceToTarget, PathCell parent) {
			this.coordinates = coordinates;
			this.prevPathLenght = prevPathLenght;
			this.distanceToTarget = distanceToTarget;
			this.heuristic = prevPathLenght + distanceToTarget;
			this.parent = parent;
		}

		public IntegerPoint getCoordinates() {
			return coordinates;
		}

		public double getPrevPathLenght() {
			return prevPathLenght;
		}

		public double getDistanceToTarget() {
			return distanceToTarget;
		}

		public PathCell getParent() {
			return parent;
		}

		public double getHeuristic() {
			return heuristic;
		}

		@Override
		public int compareTo(PathCell another) {
			return Double.compare(this.heuristic, another.heuristic);
		}

		@Override
		public String toString() {
			return "PathCell{" +
					"coordinates=" + coordinates +
					", parent=" + parent +
					", heuristic=" + heuristic +
					'}';
		}
	}
}
