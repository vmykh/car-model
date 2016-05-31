package dev.vmykh.diploma;

import org.junit.Test;

import static java.lang.Math.PI;

public class Temp {
	@Test
	public void test() {
//		System.out.println(new Vector(2, 2).getAngle() / Math.PI * 180);
//		Car car = new Car(0.5, 1).setInitialPosition(0, 0.5).setInitialOrientation(PI/2).setInitialFrontAxisAngle(PI / 6.0);
//
//		car.movedBy(10);

		Vector v1 = new Vector(0, 1);
		Vector v2 = new Vector(1, 0);

		System.out.println(v1.add(v2));
	}

	public static double angle(Vector v1, Vector v2) {
		return v1.angleTo(v2) * 360 / (2 * PI);
	}

	@Test
	public void test3() {
		System.out.println(Math.ceil(-1.5));
		System.out.println(Math.floor(-1.5));
	}
}
