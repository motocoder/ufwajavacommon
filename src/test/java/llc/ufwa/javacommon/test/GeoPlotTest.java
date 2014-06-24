package llc.ufwa.javacommon.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import llc.ufwa.collections.geospatial.AbstractGeoItem;
import llc.ufwa.collections.geospatial.GeoPlot;

import org.junit.Test;


public class GeoPlotTest {
	
	
//	@Test TODO fix this bug 
//	public void testFindClosest() {
////		while(true) {
//			final TestItem center = new TestItem(1, 1, 1);
//			
//			final GeoPlot<TestItem> plot = new GeoPlot<TestItem>();
//			plot.add(center);
//			
//			Set<TestItem> closest = new HashSet<TestItem>();
//			double closestDelta = Double.MAX_VALUE; 
//			
//			for(int i = 0; i < 10000; i++) {
//				
//				final double newX = ((Math.random() - 0.5) * 100);
//			    final double newY = ((Math.random() - 0.5) * 100);
//			    final double newZ = ((Math.random() - 0.5) * 100);
//			    
//			    final TestItem newItem = new TestItem(newX, newY, newZ);
//			    plot.add(newItem);
//			    
//			    final double newDelta = GeoPlot.getDelta(center, newItem);
//			    
//			    if(newDelta < closestDelta) {
//			    	
//			    	closest.clear();
//			    	closest.add(newItem);
//			    	closestDelta = newDelta;
//			    }
//			    else if(newDelta == closestDelta) {
//			    	closest.add(newItem);
//			    }
//			}
//			
//			final long start = System.currentTimeMillis();
//			
//			final Set<TestItem> closestItems = plot.getClosest(center);
//			
//			System.out.println("Find time " + (System.currentTimeMillis() - start));
//			
//			for(TestItem thinksClosest : closestItems) {
//				System.out.println("Thinks closest " + GeoPlot.getDelta(center, thinksClosest));
//			}
//			
//			for(TestItem wasClosest : closest) {
//				System.out.println("was closest " + GeoPlot.getDelta(center, wasClosest));
//			}
//			
//			TestCase.assertTrue(closestItems.containsAll(closest));
////		}
//	}
	
	@Test
	public void testAddAll() {
		
		final GeoPlot<TestItem> origPlot = new GeoPlot<TestItem>();
		
		final TestItem item1 = new TestItem(5, 5, 5);
		final TestItem item2 = new TestItem(5, 6, 6);
		final TestItem item3 = new TestItem(6, 6, 6);
		final TestItem item4 = new TestItem(7, 7, 7);
		final TestItem item5 = new TestItem(8, 8, 8);
		final TestItem item6 = new TestItem(9, 9, 9);
		
		origPlot.add(item1);
		origPlot.connect(item1, item2);

		origPlot.add(item3);
		origPlot.connect(item2, item3);
		origPlot.add(item3);
		origPlot.connect(item3, item4);
		origPlot.add(item4);
		origPlot.add(item5);
		
		origPlot.add(item6);
		
		final GeoPlot<TestItem> newPlot = new GeoPlot<TestItem>();
		
		newPlot.addAll(origPlot);
	
		TestCase.assertTrue(newPlot.contains(item1));
		TestCase.assertTrue(newPlot.contains(item2));
		TestCase.assertTrue(newPlot.contains(item3));
		TestCase.assertTrue(newPlot.contains(item4));
		TestCase.assertTrue(newPlot.contains(item5));
		TestCase.assertTrue(newPlot.contains(item6));
		
		TestCase.assertTrue(newPlot.getConnected(item1).contains(item2));
		
		TestCase.assertTrue(newPlot.getConnected(item2).contains(item1));
		TestCase.assertTrue(newPlot.getConnected(item2).contains(item3));
		
		TestCase.assertTrue(newPlot.getConnected(item3).contains(item2));
		TestCase.assertTrue(newPlot.getConnected(item3).contains(item4));
		
	}
	
	@Test
	public void testGetWithin() {
		
		final GeoPlot<TestItem> plot = new GeoPlot<TestItem>();
		
		final double xStart = -11;
		final double xStop = 35;
		
		final double yStart = -22;
		final double yStop = 11;
		
		final double zStart = -15;
		final double zStop = 25;
		
		final Set<TestItem> inside = new HashSet<TestItem>();
		
		for(int i = 0; i < 15000; i++) {
			
		    final double newX = ((Math.random() - 0.5) * 100);
		    final double newY = ((Math.random() - 0.5) * 100);
		    final double newZ = ((Math.random() - 0.5) * 100);
		    
		    final TestItem newItem = new TestItem(newX, newY, newZ);
		    plot.add(newItem);
		    
		    if(newX < xStart || newX > xStop) {
				continue;
			}
			
			if(newY < yStart || newY > yStop) {
				continue;
			}
			
			if(newZ < zStart || newZ > zStop) {
				continue;
			}
			
			inside.add(newItem);
		}
	
		final Set<TestItem> within = plot.getAllWithin(xStart, xStop, yStart, yStop, zStart, zStop);
		
//		System.out.println("Took " + (System.currentTimeMillis() - start) + "ms");
//		System.out.println("Found " + within.size() + " within");
//		System.out.println("Should be " + inside.size() + " within");
		
		TestCase.assertTrue(within.size() == inside.size());
		TestCase.assertTrue(inside.size() > 0);
		
		TestCase.assertTrue(within.containsAll(inside));
	}
	
	@Test
	public void testGetPaths() {
		
		final GeoPlot<TestItem> plot = new GeoPlot<TestItem>();
		
		for(int i = 0; i < 10; i++) {
			
			final TestItem item1 = new TestItem(5, 5, 5);
			final TestItem item2 = new TestItem(5, 6, 6);
			final TestItem item3 = new TestItem(6, 6, 6);
			final TestItem item4 = new TestItem(7, 7, 7);
			final TestItem item5 = new TestItem(8, 8, 8);
			final TestItem item6 = new TestItem(9, 9, 9);
			
			plot.add(item1);
			plot.connect(item1, item2);
			plot.connect(item2, item3);
			plot.connect(item3, item4);
			plot.connect(item4, item5);
			plot.connect(item5, item6);
		}
		
		final Collection<List<TestItem>> paths = plot.getPaths();
		
//		System.out.println("Size " + paths.size());
		TestCase.assertTrue(paths.size() == 10);
		TestCase.assertTrue(paths.iterator().next().size() == 6);
	}

	private static final class TestItem extends AbstractGeoItem {

		public TestItem(double x, double y, double z) {
			super(x, y, z);
		}	
	}
}
