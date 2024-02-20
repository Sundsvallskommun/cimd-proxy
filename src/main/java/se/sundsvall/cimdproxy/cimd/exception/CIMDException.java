package se.sundsvall.cimdproxy.cimd.exception;

public class CIMDException extends RuntimeException {

	private static final long serialVersionUID = -4927486181514058756L;

	public CIMDException(final String message) {
		super(message);
	}

	public CIMDException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
