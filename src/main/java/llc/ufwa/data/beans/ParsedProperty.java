package llc.ufwa.data.beans;

public final class ParsedProperty {
	
	private String name;
	private int index;

	public ParsedProperty(int index, String name) {
		
		
		if(name == null ) {
			throw new NullPointerException("Cmon genius no nulls");
		}
		
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
}
