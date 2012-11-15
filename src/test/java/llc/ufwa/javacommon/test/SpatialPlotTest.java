package llc.ufwa.javacommon.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import llc.ufwa.collections.spatial.AbstractSpatialItem;
import llc.ufwa.collections.spatial.SpatialPlot;

import org.junit.Test;


public class SpatialPlotTest {
//	
//	
//	@Test
//	public void testFindClosest() {
//		while(true) {
//			final TestItem center = new TestItem(1, 1, 1);
//			
//			final SpatialPlot<TestItem> plot = new SpatialPlot<TestItem>();
//			plot.add(center);
//			
//			Set<TestItem> closest = new HashSet<TestItem>();
//			double closestDelta = Double.MAX_VALUE;
//			
//			for(int i = 0; i < 100000; i++) {
//				
//			    final int newX = (int)(Math.random() * Integer.MAX_VALUE);
//			    final int newY = (int)(Math.random() * Integer.MAX_VALUE);
//			    final int newZ = (int)(Math.random() * Integer.MAX_VALUE);
//			    
//			    final TestItem newItem = new TestItem(newX, newY, newZ);
//			    plot.add(newItem);
//			    
//			    final double newDelta = SpatialPlot.getDelta(center, newItem);
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
//				System.out.println("Thinks closest " + SpatialPlot.getDelta(center, thinksClosest));
//			}
//			
//			for(TestItem wasClosest : closest) {
//				System.out.println("was closest " + SpatialPlot.getDelta(center, wasClosest));
//			}
//			
//			TestCase.assertTrue(closestItems.containsAll(closest));
//		}
//	}
//
//	private static final class TestItem extends AbstractSpatialItem {
//
//		public TestItem(long x, long y, long z) {
//			super(x, y, z);
//		}
//
//		@Override
//		public long getX() {
//			return super.getX();
//		}
//
//		@Override
//		public long getY() {
//			return super.getY();
//		}
//
//		@Override
//		public long getZ() {
//			return super.getZ();
//		}
//		
//		
//	}
}
