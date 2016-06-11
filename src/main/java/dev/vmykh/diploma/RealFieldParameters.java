package dev.vmykh.diploma;

public class RealFieldParameters {
	private final double width;
	private final double length;
	private final double cellSize;

	public RealFieldParameters(double width, double length, double cellSize) {
		this.width = width;
		this.length = length;
		this.cellSize = cellSize;
	}

	public double getWidth() {
		return width;
	}

	public double getLength() {
		return length;
	}

	public double getCellSize() {
		return cellSize;
	}
}
