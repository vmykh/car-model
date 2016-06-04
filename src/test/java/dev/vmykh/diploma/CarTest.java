package dev.vmykh.diploma;

import org.junit.Test;

import static java.lang.Math.*;
import static org.junit.Assert.assertEquals;

public class CarTest {
	public static final double ACCEPTABLE_ERROR = 1E-5;
	public static final double CAR_WIDTH = 2;
	public static final double CAR_LENGTH = 3;

	@Test
	public void movesStraightCorrectly() {
		double orientation = PI / 6;
		Car car = new Car(CAR_WIDTH, CAR_LENGTH).setInitialOrientation(orientation).setInitialPosition(0, 0);

		double distance = 10;
		double steeringAngleZero = 0;
		car = car.withFrontAxisAngle(steeringAngleZero).movedBy(distance);

		double xExpected = distance * cos(orientation);
		double yExpected = distance * sin(orientation);
		assertEquals(xExpected, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(yExpected, car.getY(), ACCEPTABLE_ERROR);
	}

	@Test
	public void turnsLeftCorrectly() {
		double initialOrientation = 0;
		double steeringAngle = PI / 4;
		Car car = new Car(CAR_WIDTH, CAR_LENGTH)
				.setInitialOrientation(initialOrientation)
				.setInitialPosition(0, 0)
				.setInitialSteeringAngle(steeringAngle);

		double rotationCircleRadius = 3;
		double rotationCircleLength = 2 * PI * rotationCircleRadius;

		double expectedX;
		double expectedY;
		double expectedOrientation;

		// right point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = 3;
		expectedY = 3;
		expectedOrientation = PI / 2;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);

		// top point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = 0;
		expectedY = 6;
		expectedOrientation = PI;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);

		// left point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = -3;
		expectedY = 3;
		expectedOrientation = 3 * PI / 2;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);

		// bottom point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = 0;
		expectedY = 0;
		expectedOrientation = 0;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);
	}

	@Test
	public void turnsRightCorrectly() {
		double initialOrientation = 0;
		double steeringAngle = -PI / 4;
		Car car = new Car(CAR_WIDTH, CAR_LENGTH)
				.setInitialOrientation(initialOrientation)
				.setInitialPosition(0, 0)
				.setInitialSteeringAngle(steeringAngle);

		double rotationCircleRadius = 3;
		double rotationCircleLength = 2 * PI * rotationCircleRadius;

		double expectedX;
		double expectedY;
		double expectedOrientation;

		// right point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = 3;
		expectedY = -3;
		expectedOrientation = 3 * PI / 2;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);

		// top point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = 0;
		expectedY = -6;
		expectedOrientation = PI;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);

		// left point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = -3;
		expectedY = -3;
		expectedOrientation = PI / 2;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);

		// bottom point of rotation circle
		car = car.movedBy(rotationCircleLength / 4);
		expectedX = 0;
		expectedY = 0;
		expectedOrientation = 0;
		assertEquals(expectedX, car.getX(), ACCEPTABLE_ERROR);
		assertEquals(expectedY, car.getY(), ACCEPTABLE_ERROR);
		assertEquals(expectedOrientation, car.getOrientationAngle(), ACCEPTABLE_ERROR);
	}

	@Test
	public void computesProperOrientationVector() {
		Car car = new Car(10, 10).setInitialOrientation(0);
		Vector orientation = car.getOrientationVector();
		assertEquals(1, orientation.getX(), 0.001);
		assertEquals(0, orientation.getY(), 0.001);

		car = new Car(10, 10).setInitialOrientation(1.5 * PI);
		orientation = car.getOrientationVector();
		assertEquals(0, orientation.getX(), 0.001);
		assertEquals(-1, orientation.getY(), 0.001);

		car = new Car(10, 10).setInitialOrientation(PI / 4);
		orientation = car.getOrientationVector();
		assertEquals(sqrt(2.0) / 2.0, orientation.getX(), 0.001);
		assertEquals(sqrt(2.0) / 2.0, orientation.getY(), 0.001);
	}
}
