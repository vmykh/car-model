package dev.vmykh.diploma;

public final class DubinsCurveInfo {
	private final Point firstCircleTangentPoint;
	private final Point secondCircleTangentPoint;
	private final double pathLength;

	public DubinsCurveInfo(Point firstCircleTangentPoint, Point secondCircleTangentPoint, double pathLength) {
		this.firstCircleTangentPoint = firstCircleTangentPoint;
		this.secondCircleTangentPoint = secondCircleTangentPoint;
		this.pathLength = pathLength;
	}

	public Point getFirstCircleTangentPoint() {
		return firstCircleTangentPoint;
	}

	public Point getSecondCircleTangentPoint() {
		return secondCircleTangentPoint;
	}

	public double getPathLength() {
		return pathLength;
	}
}
