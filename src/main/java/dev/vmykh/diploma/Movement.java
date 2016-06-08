package dev.vmykh.diploma;

public enum Movement {
	FORWARD(false),
	FORWARD_LEFT(false),
	FORWARD_RIGHT(false),
	BACKWARD(true),
	BACKWARD_LEFT(true),
	BACKWARD_RIGHT(true);

	private boolean isBackward;

	private Movement(boolean isBackward) {
		this.isBackward = isBackward;
	}

	public boolean isBackward() {
		return isBackward;
	}
}
