package dev.vmykh.diploma;

public final class Obstacle {
	private final Point center;
	private final double width;
	private final double height;

	public Obstacle(Point center, double width, double height) {
		this.center = center;
		this.width = width;
		this.height = height;
	}

	public Point getCenter() {
		return center;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public boolean contains(Point point) {
		double leftX = center.getX() - width / 2.0;
		double rightX = center.getX() + width / 2.0;
		double bottomY = center.getY() - height / 2.0;
		double topY = center.getY() + height / 2.0;

		return (point.getX() > leftX && point.getX() < rightX) &&
				(point.getY() > bottomY && point.getY() < topY);
	}
}
