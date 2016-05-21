package dev.vmykh.diploma;

import org.junit.Test;

import static java.lang.Math.sqrt;
import static org.junit.Assert.assertEquals;

public class CarTest {

	@Test
	public void computesProperOrientationVector() {
		Car car = new Car(10, 10).setInitialOrientation(0.0);
		Vector orientation = car.getOrientationVector();
		assertEquals(1, orientation.getX(), 0.001);
		assertEquals(0, orientation.getY(), 0.001);

		car = new Car(10, 10).setInitialOrientation(1.5 * Math.PI);
		orientation = car.getOrientationVector();
		assertEquals(0, orientation.getX(), 0.001);
		assertEquals(-1, orientation.getY(), 0.001);

		car = new Car(10, 10).setInitialOrientation(Math.PI / 4);
		orientation = car.getOrientationVector();
		assertEquals(sqrt(2.0) / 2.0, orientation.getX(), 0.001);
		assertEquals(sqrt(2.0) / 2.0, orientation.getY(), 0.001);
	}
}
