package llc.ufwa.geo;

import java.util.LinkedList;
import java.util.List;

public class TestPipe implements GeoPipe {
	
//	private static final Logger logger = LoggerFactory.getLogger(TestPipe.class);
	
	private final List<RawPoint> processed = new LinkedList<RawPoint>();

	private boolean closed;

	
	@Override
	public void addRaw(RawPoint rawIn) {
		
		RawPoint fakePoint = new RawPoint(rawIn.getLatitude(), rawIn.getLongitude(), rawIn.getAcquiredTime(), rawIn.getSpeed(), rawIn.getBearing(), rawIn.getAltitude(), rawIn.getAccuracy());
		
		synchronized(processed) {
			processed.add(fakePoint);
			processed.notify();
		}
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
			
			if(closed) {
				returnVal = null;
			}
			else {
				returnVal = processed.remove(processed.size() -1);
			}
		}
		
		return returnVal;
	}

	@Override
	public void close() {
		this.closed = true;
		
	}
}
