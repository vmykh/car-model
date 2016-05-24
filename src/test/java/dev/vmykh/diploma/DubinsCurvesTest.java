package dev.vmykh.diploma;

import org.junit.Test;

import static dev.vmykh.diploma.DubinsCurveType.RSR;
import static java.lang.Math.PI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DubinsCurvesTest {
	public static final double ACCEPTABLE_ERROR = 1E-5;

	@Test
	public void RSR() {
		double radius = 1.0;
		PositionWithDirection source = new PositionWithDirection(new Point(0, 0), new Vector(0, 1));
		PositionWithDirection target = new PositionWithDirection(new Point(5, 0), new Vector(0, -1));

		DubinsCurveInfo curveInfo = DubinsCurves.computeCurves(source, target, radius).get(RSR);

		Point expectedFirstPoint = new Point(1, 1);
		assertTrue(expectedFirstPoint.distanceTo(curveInfo.getFirstCircleTangentPoint()) < ACCEPTABLE_ERROR);
		Point expectedSecondPoint = new Point(4, 1);
		assertTrue(expectedSecondPoint.distanceTo(curveInfo.getSecondCircleTangentPoint()) < ACCEPTABLE_ERROR);
		double expectedPathLength = (PI / 2.0) * radius + 3.0 + (PI / 2.0) * radius;
		assertEquals(expectedPathLength, curveInfo.getPathLength(), ACCEPTABLE_ERROR);
	}
}
