package llc.ufwa.javacommon.test;

import junit.framework.TestCase;
import llc.ufwa.geo.GeoPipe;
import llc.ufwa.geo.RawPoint;
import llc.ufwa.geo.RealPipe;

import org.junit.Test;

public class TestGeoPipe {
//
//	@Test
//	public void testGeoPipeFlat() {
//		
//		final RawPoint point1 = new RawPoint(
//			37.418436, //lat
//			-121.963477, //long
//			System.currentTimeMillis(), //time
//			0, // speed
//			0, //bearing
//			0, //altitude
//			200  // accuracy
//		);
//		
//		final RawPoint point2 = new RawPoint(
//				37.417243, //lat
//				-121.961889, //long
//				System.currentTimeMillis(), //time
//				0, // speed
//				0, //bearing
//				0, //altitude
//				200  // accuracy
//			);
//		
//		final RawPoint point3 = new RawPoint(
//				37.418692, //lat
//				-121.960194, //long
//				System.currentTimeMillis(), //time
//				0, // speed
//				0, //bearing
//				0, //altitude
//				200  // accuracy
//			);
//		
//		
//		final GeoPipe pipe = new RealPipe();
//		
//		pipe.addRaw(point1);
//		pipe.addRaw(point2);
//		pipe.addRaw(point3);
//		
//		final RawPoint processed = pipe.getProcessed();
//		
//		TestCase.assertNotNull(processed);
//		
//		System.out.println("Lat " + processed.getLatitude());
//		System.out.println("Long " + processed.getLongitude());
//		System.out.println("alt " + processed.getAltitude());
//		System.out.println("accuracy " + processed.getAccuracy());
//		
//		
//	}
}
