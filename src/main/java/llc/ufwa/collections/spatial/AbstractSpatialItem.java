package llc.ufwa.collections.spatial;

public abstract class AbstractSpatialItem implements SpatialItem {
	
	private final long x;
	private final long y;
	private final long z;
	
	/**
	 * Forcing x y and z to be immutable otherwise spatial collection cannot remain sorted at insertion.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public AbstractSpatialItem(final long x, final long y, final long z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public long getX() {
		return x;
	}
	
	public long getY() {
		return y;
	}
	
	public long getZ() {
		return z;
	}

}
