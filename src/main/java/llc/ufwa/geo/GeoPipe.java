package llc.ufwa.geo;


public interface GeoPipe extends PointGiver {
	
	/** 
	 * Get the next processed point, blocks until new processed point exists
	 * 
	 * @return
	 */
	RawPoint getProcessed();
	void close();

}
