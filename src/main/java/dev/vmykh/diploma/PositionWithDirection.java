package dev.vmykh.diploma;

public final class PositionWithDirection {
	private final Point position;
	private final Vector direction;

	public PositionWithDirection(Point position, Vector direction) {
		this.position = position;
		this.direction = direction;
	}

	public Point getPosition() {
		return position;
	}

	public Vector getDirection() {
		return direction;
	}
}
