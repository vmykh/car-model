package dev.vmykh.diploma;

public class RealCarParameters {
	private final double bodyWidth;
	private final double bodyLength;
	private final double chassisWidth;
	private final double chassisLength;
	private final double steeringLeftAngle;
	private final double steeringRightAngle;
	private final double averageSpeed;

	public RealCarParameters(double bodyWidth, double bodyLength,
	                         double chassisWidth, double chassisLength,
	                         double steeringLeftAngle, double steeringRightAngle,
	                         double averageSpeed) {
		this.bodyWidth = bodyWidth;
		this.bodyLength = bodyLength;
		this.chassisWidth = chassisWidth;
		this.chassisLength = chassisLength;
		this.steeringLeftAngle = steeringLeftAngle;
		this.steeringRightAngle = steeringRightAngle;
		this.averageSpeed = averageSpeed;
	}

	public double getBodyWidth() {
		return bodyWidth;
	}

	public double getBodyLength() {
		return bodyLength;
	}

	public double getChassisWidth() {
		return chassisWidth;
	}

	public double getChassisLength() {
		return chassisLength;
	}

	public double getSteeringLeftAngle() {
		return steeringLeftAngle;
	}

	public double getSteeringRightAngle() {
		return steeringRightAngle;
	}

	public double getAverageSpeed() {
		return averageSpeed;
	}
}
