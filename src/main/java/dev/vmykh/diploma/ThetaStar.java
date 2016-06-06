package dev.vmykh.diploma;

import com.google.common.collect.Lists;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.*;

public class ThetaStar {
	private final Field field;
	private final IntegerPoint start;
	private final IntegerPoint target;
	private final int minFreeCellsAtSide;
	Set<IntegerPoint> exploredCells = new HashSet<>();

	public ThetaStar(Field field, IntegerPoint start, IntegerPoint target) {
		this(field, start, target, 1);
	}

	public ThetaStar(Field field, IntegerPoint start, IntegerPoint target, int minPassageWidth) {
		this.field = field;
		this.start = start;
		this.target = target;

		checkArgument(minPassageWidth > 0);
		if (minPassageWidth % 2 == 0) {
			// min width should be odd
			minPassageWidth += 1;
		}
		this.minFreeCellsAtSide = (minPassageWidth - 1) / 2;
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

		int width = minFreeCellsAtSide * 2 + 1;
		Set<IntegerPoint> linePixels = LineDrawingUtils.drawLinePixels(from, to, width);

		for (IntegerPoint linePixel : linePixels) {
			if (field.isObstacle(linePixel)) {
				return false;
			}
		}

		return true;
	}

	private static double distanceBetween(IntegerPoint p1, IntegerPoint p2) {
		double xDiff = p1.getX() - p2.getX();
		double yDiff = p1.getY() - p2.getY();
		return sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	private List<IntegerPoint> getValidNextPathCellsOf(IntegerPoint cell) {
		List<IntegerPoint> neighbors = new ArrayList<>();

		IntegerPoint left = new IntegerPoint(cell.getX() - 1, cell.getY());
		if (isValidPathCell(left)) {
			neighbors.add(left);
		}

		IntegerPoint right = new IntegerPoint(cell.getX() + 1, cell.getY());
		if (isValidPathCell(right)) {
			neighbors.add(right);
		}

		IntegerPoint bottom = new IntegerPoint(cell.getX(), cell.getY() - 1);
		if (isValidPathCell(bottom)) {
			neighbors.add(bottom);
		}

		IntegerPoint top = new IntegerPoint(cell.getX(), cell.getY() + 1);
		if (isValidPathCell(top)) {
			neighbors.add(top);
		}

		// diagonal points
		IntegerPoint bottomLeft = new IntegerPoint(cell.getX() - 1, cell.getY() - 1);
		if (isValidPathCell(bottomLeft) && lineOfSight(cell, bottomLeft)) {
			neighbors.add(bottomLeft);
		}

		IntegerPoint bottomRight = new IntegerPoint(cell.getX() + 1, cell.getY() - 1);
		if (isValidPathCell(bottomRight) && lineOfSight(cell, bottomRight)) {
			neighbors.add(bottomRight);
		}

		IntegerPoint topRight = new IntegerPoint(cell.getX() + 1, cell.getY() + 1);
		if (isValidPathCell(topRight) && lineOfSight(cell, topRight)) {
			neighbors.add(topRight);
		}

		IntegerPoint topLeft = new IntegerPoint(cell.getX() - 1, cell.getY() + 1);
		if (isValidPathCell(topLeft) && lineOfSight(cell, topLeft)) {
			neighbors.add(topLeft);
		}

		return neighbors;
	}

	private boolean isValidPathCell(IntegerPoint cell) {
		if (exploredCells.contains(cell)) {
			return false;
		}
		return cellIsValidAndHaveEnoughSpaceAround(cell);
	}

	private boolean cellIsValidAndHaveEnoughSpaceAround(IntegerPoint cell) {
		int leftX = cell.getX() - minFreeCellsAtSide;
		int rightX = cell.getX() + minFreeCellsAtSide;
		int bottomY = cell.getY() - minFreeCellsAtSide;
		int topY = cell.getY() + minFreeCellsAtSide;
		for (int x = leftX; x <= rightX; x++) {
			for (int y = bottomY; y <= topY; y++) {
				if (x < 0 || x >= field.getWidth()) {
					return false;
				}
				if (y < 0 || y >= field.getHeight()) {
					return false;
				}

				if (field.isObstacle(x, y)) {
					return false;
				}
			}
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
