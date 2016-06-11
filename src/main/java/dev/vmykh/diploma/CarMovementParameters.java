package dev.vmykh.diploma;

public final class CarMovementParameters {
	private final double unitStepDistance;
	private final double leftSteeringAngle;
	private final double rightSteeringAngle;

	public CarMovementParameters(double unitStepDistance, double leftSteeringAngle, double rightSteeringAngle) {
		this.unitStepDistance = unitStepDistance;
		this.leftSteeringAngle = leftSteeringAngle;
		this.rightSteeringAngle = rightSteeringAngle;
	}

	public double getUnitStepDistance() {
		return unitStepDistance;
	}

	public double getLeftSteeringAngle() {
		return leftSteeringAngle;
	}

	public double getRightSteeringAngle() {
		return rightSteeringAngle;
	}
}
