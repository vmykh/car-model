package dev.vmykh.diploma;

import org.junit.Test;

import static java.lang.Math.PI;

public class Temp {
	@Test
	public void test() {
//		System.out.println(new Vector(2, 2).getAngle() / Math.PI * 180);
		Car car = new Car(0.5, 1).setInitialPosition(0, 0.5).setInitialOrientation(PI/2).setInitialFrontAxisAngle(PI / 6.0);

		car.movedBy(10);
	}
}
