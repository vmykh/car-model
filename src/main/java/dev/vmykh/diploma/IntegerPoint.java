package dev.vmykh.diploma;

public final class IntegerPoint {
	private final int x;
	private final int y;

	public IntegerPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntegerPoint)) return false;

		IntegerPoint that = (IntegerPoint) o;

		if (x != that.x) return false;
		if (y != that.y) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		return result;
	}

	@Override
	public String toString() {
		return "IntegerPoint{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
