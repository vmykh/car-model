package dev.vmykh.diploma;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ThetaStarTest {

	@Test
	/*

	s * x * *
	* * x * *
	* * x * *
	* * x x x
	* * * * f

	 */
	public void testThetaStar() {
		Field field = new Field(5, 5);
		// vertical line of obstacles
		field.addObstacle(2, 1);
		field.addObstacle(2, 2);
		field.addObstacle(2, 3);
		field.addObstacle(2, 4);
		// horizontal line of obstacles
		field.addObstacle(3, 1);
		field.addObstacle(4, 1);

		IntegerPoint start = new IntegerPoint(0, 4);
		IntegerPoint finish = new IntegerPoint(4, 0);
		ThetaStar thetaStar = new ThetaStar(field, start, finish);
		List<IntegerPoint> path = thetaStar.findPath();

		List<IntegerPoint> expectedPath = asList(start, new IntegerPoint(1, 0), finish);
		assertEquals(expectedPath, path);
	}

	@Test
	/*

	* * * * * * * * * *
	* * x x x x x * * *
	* * * * * * x * * *
	s * * * * * x * * f
	* * * * * * x * * *
	* * x x x x x * * *
	* * * * * * * * * *

	 */
	public void testThetaStar_with_U_Obstacle() {
		Field field = new Field(10, 7);
		// bottom line of obstacles
		field.addObstacle(2, 1);
		field.addObstacle(3, 1);
		field.addObstacle(4, 1);
		field.addObstacle(5, 1);
		field.addObstacle(6, 1);
		// right line of obstacles
		field.addObstacle(6, 2);
		field.addObstacle(6, 3);
		field.addObstacle(6, 4);
		// top line of obstacles
		field.addObstacle(6, 5);
		field.addObstacle(5, 5);
		field.addObstacle(4, 5);
		field.addObstacle(3, 5);
		field.addObstacle(2, 5);

		IntegerPoint start = new IntegerPoint(0, 3);
		IntegerPoint finish = new IntegerPoint(9, 3);
		ThetaStar thetaStar = new ThetaStar(field, start, finish);
		List<IntegerPoint> path = thetaStar.findPath();

		List<IntegerPoint> topShortestPath = asList(start, new IntegerPoint(1, 6), new IntegerPoint(7, 6), finish);
		List<IntegerPoint> bottomShortestPath = asList(start, new IntegerPoint(1, 0), new IntegerPoint(7, 0), finish);
		assertThat(path, anyOf(
				is(equalTo(topShortestPath)),
				is(equalTo(bottomShortestPath))
		));
	}

	@Test
	/*

	* * x * *
	s * x * *
	* * x x x
	* * * * *
	* * * f *

	 */
	public void doesntTouchEndges() {
		Field field = new Field(5, 5);
		// vertical line of obstacles
		field.addObstacle(2, 2);
		field.addObstacle(2, 3);
		field.addObstacle(2, 4);
		// horizontal line of obstacles
		field.addObstacle(3, 2);
		field.addObstacle(4, 2);

		IntegerPoint start = new IntegerPoint(0, 3);
		IntegerPoint finish = new IntegerPoint(3, 0);
		ThetaStar thetaStar = new ThetaStar(field, start, finish);
		List<IntegerPoint> path = thetaStar.findPath();

		List<IntegerPoint> expectedPath = asList(start, new IntegerPoint(1, 1), finish);
		assertEquals(expectedPath, path);
	}
}
