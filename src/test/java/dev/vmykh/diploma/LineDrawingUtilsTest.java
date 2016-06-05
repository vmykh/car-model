package dev.vmykh.diploma;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class LineDrawingUtilsTest {
	public static final int LINE_WIDTH_ONE = 1;
	public static final int LINE_WIDTH_THREE = 3;

	@Test
	public void drawsHorizontalLineWithWidthOne() {
		// width = 1
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 0), new IntegerPoint(2, 0), LINE_WIDTH_ONE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 0),
				new IntegerPoint(1, 0),
				new IntegerPoint(2, 0)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsHorizontalLineWithWidthThree() {
		// width = 1
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 0), new IntegerPoint(2, 0), LINE_WIDTH_THREE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, -1),
				new IntegerPoint(0, 0),
				new IntegerPoint(0, 1),

				new IntegerPoint(1, -1),
				new IntegerPoint(1, 0),
				new IntegerPoint(1, 1),

				new IntegerPoint(2, -1),
				new IntegerPoint(2, 0),
				new IntegerPoint(2, 1)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsVerticalLineWithWidthOne() {
		// width = 1
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 3), new IntegerPoint(0, 0), LINE_WIDTH_ONE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 3),
				new IntegerPoint(0, 2),
				new IntegerPoint(0, 1),
				new IntegerPoint(0, 0)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsVerticalLineWithWidthThree() {
		// width = 1
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 3), new IntegerPoint(0, 0), LINE_WIDTH_THREE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(-1, 3),
				new IntegerPoint(0, 3),
				new IntegerPoint(1, 3),

				new IntegerPoint(-1, 2),
				new IntegerPoint(0, 2),
				new IntegerPoint(1, 2),

				new IntegerPoint(-1, 1),
				new IntegerPoint(0, 1),
				new IntegerPoint(1, 1),

				new IntegerPoint(-1, 0),
				new IntegerPoint(0, 0),
				new IntegerPoint(1, 0)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithPositiveAngleLessThan45DegreesAndWidthOne() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 0), new IntegerPoint(2, 1), LINE_WIDTH_ONE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 0),
				new IntegerPoint(1, 0),
				new IntegerPoint(1, 1),
				new IntegerPoint(2, 1)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithPositiveAngleLessThan45DegreesAndWidthThree() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 0), new IntegerPoint(2, 1), LINE_WIDTH_THREE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, -1),
				new IntegerPoint(0, 0),
				new IntegerPoint(0, 1),

				new IntegerPoint(1, -1),
				new IntegerPoint(1, 0),
				new IntegerPoint(1, 1),
				new IntegerPoint(1, 2),

				new IntegerPoint(2, 0),
				new IntegerPoint(2, 1),
				new IntegerPoint(2, 2)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithNegativeAngleLessThan45DegreesByAbsoluteValueAndWidthOne() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 1), new IntegerPoint(3, 0), LINE_WIDTH_ONE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 1),

				new IntegerPoint(1, 1),
				new IntegerPoint(1, 0),

				new IntegerPoint(2, 1),
				new IntegerPoint(2, 0),

				new IntegerPoint(3, 0)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithNegativeAngleLessThan45DegreesByAbsuluteValueAndWidthThree() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 1), new IntegerPoint(3, 0), LINE_WIDTH_THREE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 0),
				new IntegerPoint(0, 1),
				new IntegerPoint(0, 2),

				new IntegerPoint(1, 2),
				new IntegerPoint(1, 1),
				new IntegerPoint(1, 0),
				new IntegerPoint(1, -1),

				new IntegerPoint(2, 2),
				new IntegerPoint(2, 1),
				new IntegerPoint(2, 0),
				new IntegerPoint(2, -1),

				new IntegerPoint(3, 1),
				new IntegerPoint(3, 0),
				new IntegerPoint(3, -1)
		));
		assertEquals(expectedPixels, pixels);
	}

	// more than 45
	@Test
	public void drawsLineWithPositiveAngleMoreThan45DegreesAndWidthOne() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 0), new IntegerPoint(1, 2), LINE_WIDTH_ONE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 0),

				new IntegerPoint(0, 1),
				new IntegerPoint(1, 1),

				new IntegerPoint(1, 2)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithPositiveAngleMoreThan45DegreesAndWidthThree() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 0), new IntegerPoint(1, 2), LINE_WIDTH_THREE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 0),
				new IntegerPoint(-1, 0),
				new IntegerPoint(1, 0),

				new IntegerPoint(-1, 1),
				new IntegerPoint(0, 1),
				new IntegerPoint(1, 1),
				new IntegerPoint(2, 1),

				new IntegerPoint(0, 2),
				new IntegerPoint(1, 2),
				new IntegerPoint(2, 2)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithNegativeAngleMoreThan45DegreesByAbsoluteValueAndWidthOne() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 3), new IntegerPoint(1, 0), LINE_WIDTH_ONE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(0, 3),

				new IntegerPoint(0, 2),
				new IntegerPoint(1, 2),

				new IntegerPoint(0, 1),
				new IntegerPoint(1, 1),

				new IntegerPoint(1, 0)
		));
		assertEquals(expectedPixels, pixels);
	}

	@Test
	public void drawsLineWithNegativeAngleMoreThan45DegreesByAbsoluteValueAndWidthThree() {
		Set<IntegerPoint> pixels =
				LineDrawingUtils.drawLinePixels(new IntegerPoint(0, 3), new IntegerPoint(1, 0), LINE_WIDTH_THREE);
		Set<IntegerPoint> expectedPixels = new HashSet<>(asList(
				new IntegerPoint(-1, 3),
				new IntegerPoint(0, 3),
				new IntegerPoint(1, 3),

				new IntegerPoint(-1, 2),
				new IntegerPoint(0, 2),
				new IntegerPoint(1, 2),
				new IntegerPoint(2, 2),

				new IntegerPoint(-1, 1),
				new IntegerPoint(0, 1),
				new IntegerPoint(1, 1),
				new IntegerPoint(2, 1),

				new IntegerPoint(0, 0),
				new IntegerPoint(1, 0),
				new IntegerPoint(2, 0)
		));
		assertEquals(expectedPixels, pixels);
	}
}
