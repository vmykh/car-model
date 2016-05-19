package dev.vmykh.diploma;

public class ImpossibleMovementException extends Exception {
	public ImpossibleMovementException() {
	}

	public ImpossibleMovementException(String message) {
		super(message);
	}

	public ImpossibleMovementException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImpossibleMovementException(Throwable cause) {
		super(cause);
	}

	public ImpossibleMovementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
