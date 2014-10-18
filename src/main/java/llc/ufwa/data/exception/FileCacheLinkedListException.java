package llc.ufwa.data.exception;

public class FileCacheLinkedListException extends Exception {

	private static final long serialVersionUID = -2789311521209920763L;

	public FileCacheLinkedListException(Exception e) {
		super(e);
	}

	public FileCacheLinkedListException(String e) {
		super(e);
	}

}
