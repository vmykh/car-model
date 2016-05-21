package dev.vmykh.diploma;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CellWeightTest {

	@Test
	public void dividesCirleProperlyAccordingToDirections() {
		// divide cirle into eight parts
		// first from -PI/8 to PI/8, second from PI/8 to 2*(PI/8) and so on
		CellWeight cellWeight = new CellWeight(8);

		// first 1/8 part
		cellWeight.addWeight(0.0);
		cellWeight.addWeight(0.1);
		cellWeight.addWeight(2 * Math.PI - 0.1);
		// near second 1/8 part
		cellWeight.addWeight(Math.PI / 8 - 0.01);

		// second 1/8 part
		cellWeight.addWeight(Math.PI / 4);
		cellWeight.addWeight(Math.PI / 4 - 0.1);
		cellWeight.addWeight(Math.PI / 4 + 0.1);

		int commonWeight = (4 + 3) * CellWeight.COMMON_WEIGHT_COEF;
		int firstPartitionExpectedWeight = commonWeight + 4 * CellWeight.DIRECTION_WEIGHT_COEF;
		assertEquals(firstPartitionExpectedWeight, cellWeight.getWeight(0.0));
		assertEquals(firstPartitionExpectedWeight, cellWeight.getWeight(2 * Math.PI - 0.05));

		int secondPartitionExpectedWeight = commonWeight + 3 * CellWeight.DIRECTION_WEIGHT_COEF;
		assertEquals(secondPartitionExpectedWeight, cellWeight.getWeight(Math.PI / 4));
	}

	@Test
	public void properlyHandlesAnglesEvenIfTheyAreGreaterThanTwoTimesPI() {
		CellWeight cellWeight = new CellWeight(8);

		cellWeight.addWeight(10 * Math.PI - 0.1);

		int firstPartitionExpectedWeight = 1 * CellWeight.COMMON_WEIGHT_COEF + 1 * CellWeight.DIRECTION_WEIGHT_COEF;
		assertEquals(firstPartitionExpectedWeight, cellWeight.getWeight(0.0));
	}

	@Test
	public void properlyHandlesNegativeAngles() {
		CellWeight cellWeight = new CellWeight(8);

		cellWeight.addWeight(-0.1);
		cellWeight.addWeight(-0.1 - 2 * Math.PI);

		int firstPartitionExpectedWeight = 2 * CellWeight.COMMON_WEIGHT_COEF + 2 * CellWeight.DIRECTION_WEIGHT_COEF;
		assertEquals(firstPartitionExpectedWeight, cellWeight.getWeight(0.0));
	}
}
