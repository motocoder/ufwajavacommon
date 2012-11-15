package llc.ufwa.geo;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import llc.ufwa.util.PointUtils;
import llc.ufwa.util.PointUtils.InsideOutside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealPipe implements GeoPipe {	
	
	private static final Logger logger = LoggerFactory.getLogger(RealPipe.class);
		
	private RawPoint lastPoint;
	private final Set<RawPoint> allPoints = new HashSet<RawPoint>();
	
	private final List<RawPoint> processed = new LinkedList<RawPoint>();

	private boolean closed;
	
	public RealPipe() {
		
	}

	@Override
	public void addRaw(RawPoint rawIn) {
		
		synchronized(allPoints) {
			
			boolean insideCompletely = true;
			boolean outsideSomething = false;
			
			for(RawPoint outside : allPoints) {
				
				final InsideOutside relation = PointUtils.getRelation(rawIn, outside);
				
				switch(relation) {
					case INSIDE_COMPLETELY:
					{
						break;
					}
					case INSIDE_PARTIALLY:
					{
						allPoints.add(rawIn);
						insideCompletely = false;
						break;
					}
					case OUTSIDE_PARTIALLY:
					case OUTSIDE_COMPLETELY:
					{
						outsideSomething = true;
						insideCompletely = false;
						break;
					}
				}
				
				if(outsideSomething) {
					break;
				}
			}
			
			if(outsideSomething) {
				if(allPoints.size() > 0) {
					lastPoint = processPoints(allPoints, lastPoint);
					allPoints.clear();
					
					synchronized(processed) {
						processed.add(lastPoint);
						processed.notify();
					}
				}
			}
			else {
				
				
				if(insideCompletely) {
					allPoints.clear();
				}
				
				allPoints.add(rawIn);
			}
		}
	}
	
	private static RawPoint processPoints(Set<RawPoint> allPoints, RawPoint lastPoint) {
		
		double averageLat = 0;
		double averageLong = 0;
		double averageAlt = 0;
		float averageBearing = 0;
		long averageTime = 0;
		
		float smallestAccuracy = Float.MAX_VALUE;
		
		final int size = allPoints.size();
		
		for(RawPoint point : allPoints) {
			
		    averageLat +=  point.getLatitude() / size; //TODO make accuracy affect how much the delta affects the final point
		    averageLong += point.getLongitude() / size; // take inverse of the percentage sum these values then take their percentage of the new sum
		    averageAlt += point.getAltitude() / size; // this is what you multiple the new delta by to find out how much it affects the final value.
		    averageBearing += point.getBearing() / size;
		    averageTime += point.getAcquiredTime() / size;
		    
		    if(smallestAccuracy > point.getAccuracy()) {
				smallestAccuracy = point.getAccuracy();
			}
		}
		
		final float speed;
		
		if(lastPoint != null) {
			
			final RawPoint temp =
				new RawPoint(
				    averageLat,
				    averageLong,
				    System.currentTimeMillis(),
				    0,
				    averageBearing,
				    averageAlt,
				    smallestAccuracy
		    );
			
		    final long deltaTime = averageTime - lastPoint.getAcquiredTime();
		    
		    final double deltaDistance = PointUtils.computeDistance(lastPoint, temp);
		    
		    speed = (float)(deltaDistance / deltaTime);
		}
		else {
			speed = 0;
		}
		
		final RawPoint returnVal =
			new RawPoint(
			    averageLat,
			    averageLong,
			    System.currentTimeMillis(),
			    speed,
			    averageBearing,
			    averageAlt,
			    smallestAccuracy
	    );
		
		return returnVal;
	}

	@Override
	public RawPoint getProcessed() {
		
		final RawPoint returnVal;
		
		synchronized(processed) {
			while(processed.size() == 0) {
				try {
					processed.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(!closed) {
				returnVal = processed.remove(processed.size() -1);
			}
			else {
				returnVal = null;
			}
		}
		
		return returnVal;
	}

	@Override
	public void close() {
		
		this.closed = true;
		
		synchronized(processed) {
			processed.notify();
		}
	}

}
