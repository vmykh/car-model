package dev.vmykh.diploma;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PIDControllerTest {

	@Test
	public void testPDController() {
		PIDController c = new PIDController(2.0, 2.0, 0.0, 0.5);

		assertEquals(1, c.currentError(5.0));
		assertEquals(1, c.currentError(4.0));
		assertEquals(1, c.currentError(3.0));
		assertEquals(1, c.currentError(2.0));
		assertEquals(0, c.currentError(1.0));
		assertEquals(-1, c.currentError(0.0));
	}
}
