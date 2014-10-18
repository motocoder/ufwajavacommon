package llc.ufwa.data.resource.linear;

import llc.ufwa.data.exception.FileCacheLinkedListException;

public class DNode {

	private String prevNode; //Pointer to the previous DNode
	private String nextNode; //Pointer to the next DNode

	final public static String DELIMITER = ":";
	final public static String NULL = "NULL";

	public DNode(String prevNode, String nextNode) {

		this.prevNode = prevNode;
		this.nextNode = nextNode;

	}

	public DNode(final String stringRepresentation) throws FileCacheLinkedListException {

		final String[] array = stringRepresentation.split(DELIMITER);

		if (array.length != 2) {
			String exceptionString = "DNode input string is not formatted correctly, size is: " + array.length + " - ";
			for (int x = 0; x < array.length; x++) {
				exceptionString += array[x] + ",";
			}
			throw new FileCacheLinkedListException(exceptionString);
		}

		this.prevNode = array[0];
		this.nextNode = array[1];

	}

	//Get the previous DNode
	public String getPrev() {
		if (prevNode == null) {
			return NULL;
		}
		return prevNode;
	}

	//Set the previous DNode
	public void setPrev(String newPrev) {
		this.prevNode = newPrev;
	}

	//Get the next DNode
	public String getNext() {
		if (nextNode == null) {
			return NULL;
		}
		return nextNode;
	}

	//Set the previous DNode
	public void setNext(String newNext) {
		this.nextNode = newNext;
	}

	/*
	 * Returns a String representation of a DNode which can be parsed back into
	 * a DNode (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (getPrev() + DELIMITER + getNext());
	}

}