package dev.vmykh.diploma;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.round;

public final class LineDrawingUtils {
	private LineDrawingUtils() {}

	public static Set<IntegerPoint> drawLinePixels(IntegerPoint from, IntegerPoint to, int width) {
		checkArgument(width >= 1);

		if (from.equals(to)) {
			// TODO(vmykh): it doesn't seem to be correct
			throw new RuntimeException();
		}

		double absDeltaX = abs(to.getX() - from.getX());
		double absDeltaY = abs(to.getY() - from.getY());

		if (absDeltaX > absDeltaY) {
			return computePixelsForLine(from, to, width);
		} else {
			Set<IntegerPoint> symmetricPixels = computePixelsForLine(copyAndSwapXY(from), copyAndSwapXY(to), width);
			Set<IntegerPoint> pixels = new HashSet<>();
			for (IntegerPoint pixel : symmetricPixels) {
				pixels.add(copyAndSwapXY(pixel));
			}
			return pixels;
		}
	}

//	private static Set<IntegerPoint> computePixelsForXYSymmetricLine(IntegerPoint from, IntegerPoint to, int width) {
//		if (from.getX() > to.getX()) {
//			IntegerPoint temp = from;
//			from = to;
//			to = temp;
//		}
//
//		int adjustedWidth = width % 2 == 0 ? width + 1 : width;
//		int yShift = (adjustedWidth - 1) / 2;
//		int currentY = from.getY();
//		int yStep = to.getY() - from.getY() > 0 ? 1 : -1;
//		for (int x = from.getX(); x <= to.getX(); x++) {
//			int minY = currentY - yShift;
//			int maxY = currentY + yShift;
//			for (int y = currentY - yShift; y < ; y++) {
//
//			}
//			currentY += yStep;
//		}
//	}

	private static boolean numbersAreEqual(double absDeltaX, double absDeltaY, double precision) {
		return abs(absDeltaX - absDeltaY) < precision;
	}

	private static Set<IntegerPoint> computePixelsForLine(IntegerPoint from, IntegerPoint to, int width) {
		double absDeltaX = abs(to.getX() - from.getX());
		double absDeltaY = abs(to.getY() - from.getY());
		checkArgument(absDeltaX >= absDeltaY);

		SortedSet<IntegerPoint> pixels = new TreeSet<>(new IntegerPointForLineDrawingComparator());

		if (from.getX() > to.getX()) {
			IntegerPoint temp = from;
			from = to;
			to = temp;
		}

		double ratio = (to.getY() - from.getY()) / (absDeltaX);
		double y = from.getY() + 0.5 - ratio * 0.5;
		int iters = to.getX() - from.getX();

		for (int i = 0; i < iters; i++) {
			y += ratio;
			int xLeft = from.getX() + i;
			int xRight = xLeft + 1;
			if (almostInteger(y)) {
				int upperY = (int) round(y);
				int bottomY = upperY - 1;
				pixels.add(new IntegerPoint(xLeft, bottomY));
				pixels.add(new IntegerPoint(xLeft, upperY));
				pixels.add(new IntegerPoint(xRight, bottomY));
				pixels.add(new IntegerPoint(xRight, upperY));
			} else {
				int integerY = (int) floor(y);
				pixels.add(new IntegerPoint(xLeft, integerY));
				pixels.add(new IntegerPoint(xRight, integerY));
			}
		}

		return withAdjustedLineWidth(pixels, width);
	}

	private static Set<IntegerPoint> withAdjustedLineWidth(SortedSet<IntegerPoint> pixelsOfLineWithWidthOne,
	                                                       int width) {
		checkArgument(width >= 1);

		SortedSet<IntegerPoint> pixelsWithProperLineWidth = new TreeSet<>(new IntegerPointForLineDrawingComparator());

//		TODO(vmykh): consider case of 45 degrees line

		List<List<IntegerPoint>> groupedByX = groupedAndOrderedByX(pixelsOfLineWithWidthOne);

//		List<List<IntegerPoint>> groupedByXAndHandled


		for (List<IntegerPoint> pixelsVertical : groupedByX) {
			if (pixelsVertical.size() == 1) {
				IntegerPoint pixel = pixelsVertical.get(0);
				int adjustedWidth = width % 2 == 0 ? width + 1 : width;
				int minY = pixel.getY() - (adjustedWidth - 1) / 2;
				int maxY = minY + adjustedWidth - 1;
				int x = pixel.getX();
				for (int y = minY; y <= maxY; y++) {
					pixelsWithProperLineWidth.add(new IntegerPoint(x, y));
				}
			} else if (pixelsVertical.size() == 2) {
				IntegerPoint bottomPixel = pixelsVertical.get(0);
				int adjustedWidth = width % 2 == 0 ? width : width + 1;
				int minY = bottomPixel.getY() - (adjustedWidth - 1) / 2;
				int maxY = minY + adjustedWidth - 1;
				int x = bottomPixel.getX();
				for (int y = minY; y <= maxY; y++) {
					pixelsWithProperLineWidth.add(new IntegerPoint(x, y));
				}
			} else {
				throw new RuntimeException();
			}
		}

		return pixelsWithProperLineWidth;
	}

	private static List<List<IntegerPoint>> groupedAndOrderedByX(SortedSet<IntegerPoint> pixelsOfLineWithWidthOne) {
		List<List<IntegerPoint>> groupedByX = new ArrayList<>();
		Iterator<IntegerPoint> iterator = pixelsOfLineWithWidthOne.iterator();
		List<IntegerPoint> lastGroup = null;
		while (iterator.hasNext()) {
			IntegerPoint current = iterator.next();
			if (lastGroup == null) {
				lastGroup = new ArrayList<>();
				lastGroup.add(current);
			} else {
				IntegerPoint prev = lastGroup.get(0);
				if (prev.getX() == current.getX()) {
					lastGroup.add(current);
					groupedByX.add(lastGroup);
					lastGroup = null;
				} else {
					groupedByX.add(lastGroup);
					lastGroup = new ArrayList<>();
					lastGroup.add(current);
				}
			}
		}
		if (lastGroup != null) {
			groupedByX.add(lastGroup);
		}
		return groupedByX;
	}

	private static boolean almostInteger(double x) {
		return abs(round(x) - x) < 0.001;
	}

	private static IntegerPoint copyAndSwapXY(IntegerPoint point) {
		return new IntegerPoint(point.getY(), point.getX());
	}

	private static final class IntegerPointForLineDrawingComparator implements Comparator<IntegerPoint> {

		@Override
		public int compare(IntegerPoint p1, IntegerPoint p2) {
			if (p1.getX() != p2.getX()) {
				return p1.getX() - p2.getX();
			} else {
				return p1.getY() - p2.getY();
			}
		}
	}
}
