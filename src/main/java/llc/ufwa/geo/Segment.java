package llc.ufwa.geo;

public class Segment {
    
	private RawPoint point;
    private RawPoint otherPoint;

    public Segment(RawPoint point, RawPoint otherPoint) {
        this.point = point;
        this.otherPoint = otherPoint;
    }

    public RawPoint getPoint() {
        return point;
    }

    public RawPoint getOtherPoint() {
        return otherPoint;
    }

    @Override
    public boolean equals(Object o) {
        boolean returnVal = false;
        
        if(o instanceof Segment) {
            Segment segment = (Segment)o;
            
            if(segment.getOtherPoint() == this.getOtherPoint() 
              && segment.getPoint() == this.getPoint()) {
                returnVal = true;
            }
            else if(segment.getPoint() == this.getOtherPoint() 
              && segment.getOtherPoint() == this.getPoint()) {
                returnVal = true;
            }
        }
        return returnVal;
    }

	@Override
	public int hashCode() {
		return point.hashCode() + otherPoint.hashCode();
	}
}
