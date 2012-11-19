package llc.ufwa.util;

final class FormatException extends RuntimeException {
	
	private static final long serialVersionUID = -6411873848790873637L;

	@SuppressWarnings("unused")
	private FormatException() {
		
	}
	
	public FormatException(String message) {
		super(message);
		
	}

}
