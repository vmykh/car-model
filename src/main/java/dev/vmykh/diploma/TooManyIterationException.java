package dev.vmykh.diploma;

public class TooManyIterationException extends Exception {
	public TooManyIterationException() {
	}

	public TooManyIterationException(String message) {
		super(message);
	}

	public TooManyIterationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyIterationException(Throwable cause) {
		super(cause);
	}

	public TooManyIterationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
