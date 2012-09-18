package llc.ufwa.collections.spatial;


public class SpatialPlot { //<T extends SpatialItem> implements SpatialGraph<T> {
//	
//	private final TreeMap<Long, Set<T>> xSorted = new TreeMap<Long, Set<T>>();
//	private final TreeMap<Long, Set<T>> ySorted = new TreeMap<Long, Set<T>>();
//	private final TreeMap<Long, Set<T>> zSorted = new TreeMap<Long, Set<T>>();
//	
//	private final Set<T> contains = new HashSet<T>();
//
//	@Override
//	public boolean add(T e) {
//		
//		if(contains.contains(e)) {
//			throw new IllegalArgumentException("Item already in collection");
//		}
//
//		addToMaps(e);
//		
//		return true;
//	}
//	
//	private final void addToMaps(T e) {
//		
//		contains.add(e);
//		addSorted(xSorted, e, e.getX());
//		addSorted(ySorted, e, e.getY());
//		addSorted(zSorted, e, e.getZ());
//
//	}
//
//	/**
//	 * convenience method that creates a list if the key isn't in the map yet.
//	 * 
//	 * @param <T>
//	 * @param map
//	 * @param e
//	 * @param location
//	 */
//	private static final <T extends SpatialItem> void addSorted(
//		final TreeMap<Long, Set<T>> map, 
//		final T e, long location
//    ) {
//		
//		Set<T> list = map.get(location);
//		
//		if(list == null) {
//			list = new HashSet<T>();
//			map.put(location, list);
//		}
//		
//		list.add(e);		
//	}
//
//	@Override
//	public boolean addAll(Collection<? extends T> c) {
//		
//		final Set<T> added = new HashSet<T>();
//		
//		boolean returnVal = false;
//		
//		for(T elem : c) {
//			
//			returnVal = this.add(elem);
//			
//			if(!returnVal) {
//				remove(elem);
//				remove(added);
//			    break;
//			}
//			
//			added.add(elem);
//		}
//		
//		return returnVal;
//	}
//
//	@Override
//	public void clear() {
//		
//		xSorted.clear();
//		contains.clear();
//		ySorted.clear();
//		zSorted.clear();		
//		links.clear();
//		
//	}
//
//	@Override
//	public boolean contains(Object o) {
//		return contains.contains(o);
//	}
//
//	@Override
//	public boolean containsAll(Collection<?> c) {
//		return contains.containsAll(c);
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return contains.isEmpty();
//	}
//
//	@Override
//	public Iterator<T> iterator() {
//		return contains.iterator();
//	}
//
//	@Override
//	public boolean remove(Object o) {
//		
//		final boolean returnVal;
//		
//		if(o instanceof SpatialItem) {
//			SpatialItem item = (SpatialItem)o;
//			
//			if(contains.remove(o)) {
//				xSorted.get(item.getX()).remove(item);
//				ySorted.get(item.getY()).remove(item);
//				zSorted.get(item.getZ()).remove(item);
//				
//				final Set<SpatialItem> linkedTo = links.remove(item);
//				
//				for(SpatialItem linked : linkedTo) {
//					links.get(linked).remove(item);
//				}
//				
//				returnVal = true;
//			}
//			else {
//				returnVal = false;
//			}
//		}
//		else {
//			returnVal = false;
//		}
//		
//		return returnVal;
//	}
//
//	@Override
//	public boolean removeAll(Collection<?> c) {
//		
//		boolean returnVal = true;
//		
//		for(Object item : c) {
//			
//			//TODO optimize
//			if(!remove(item)) {
//				returnVal = false;
//				break;
//			}
//		}
//		
//		return returnVal;
//	}
//
//	@Override
//	public boolean retainAll(Collection<?> c) {
//		throw new RuntimeException("Method not supported yet");
//	}
//
//	@Override
//	public int size() {
//		return contains.size();
//	}
//
//	@Override
//	public Object[] toArray() {
//		return contains.toArray();
//	}
//
//	@Override
//	public boolean connect(T inGraph, T item) {
//		
//		final boolean returnVal;
//		
//		if(contains.contains(inGraph)) {
//			
//			addLink(inGraph, item);
//			addToMaps(item);
//			
//			returnVal = true;
//		}
//		else {
//			throw new IllegalArgumentException("First parameter must already be in the graph");
//		}
//		
//		return returnVal;
//	}
//	
//	private final Map<SpatialItem, Set<SpatialItem>> links = new IdentityHashMap<SpatialItem, Set<SpatialItem>>();
//
//	private void addLink(SpatialItem item1, SpatialItem item2) {
//		
//		Set<SpatialItem> links1 = links.get(item1);
//		Set<SpatialItem> links2 = links.get(item2);
//		
//		if(links1 == null) {
//			links1 = new HashSet<SpatialItem>();
//			links.put(item1, links1);
//		}
//		
//		if(links2 == null) {
//			links2 = new HashSet<SpatialItem>();
//			links.put(item2, links2);
//		}
//		
//		links1.add(item2);
//		links2.add(item1);
//		
//	}
//
//	@Override
//	public boolean remove(T item) {
//		return remove((Object)item);
//	}
//
//	@Override
//	public boolean disconnect(T first, T second) {
//		
//		final boolean returnVal;
//		
//		if(contains.contains(first) && contains.contains(second)) {
//			
//			final Set<SpatialItem> firstLinks = links.get(first);
//			final Set<SpatialItem> secondLinks = links.get(second);
//			
//			if(firstLinks == null || secondLinks == null) {
//				throw new IllegalArgumentException("Not linked");
//			}
//			
//			returnVal = firstLinks.remove(second) && secondLinks.remove(first);
//			
//		}
//		else {
//			returnVal = false;
//		}
//		
//		return returnVal;
//	}
//
//	@Override
//	public Set<T> getAll(T item) {
//		return new HashSet<T>(contains);
//	}
//
//	@Override
//	public Set<T> getAll() {
//		return new HashSet<T>(contains);
//	}
//
//	@Override
//	public Set<T> getClosest(T item) {
//		
//		if(!contains.contains(item)) {
//			throw new IllegalArgumentException("Must be in plot to have a closest");
//		}
//		else if(contains.size() == 1) {
//			throw new IllegalArgumentException("Must not be the only one in the plot to have a closest");
//		}
//		
//		final Set<Integer> maxSet = new TreeSet<Integer>(new Comparator<Integer>() {
//
//			@Override
//			public int compare(Integer first, Integer second) {
//				return second - first;
//			}});
//		
//		final ClosestNavigator<T> xNav = new ClosestNavigator<T>(xSorted, item, ClosestNavigator.Axis.X);
//		final ClosestNavigator<T> yNav = new ClosestNavigator<T>(ySorted, item, ClosestNavigator.Axis.Y);
//		final ClosestNavigator<T> zNav = new ClosestNavigator<T>(zSorted, item, ClosestNavigator.Axis.Z);
//		
//		final Set<T> closestX = new HashSet<T>();		
//		final T firstX = xNav.getNextClosest();
//		closestX.add(firstX);		
//		double closestXDelta = getDelta(item, firstX);
//		
//		final Set<T> closestY = new HashSet<T>();		
//		final T firstY = yNav.getNextClosest();
//		closestY.add(firstY);		
//		double closestYDelta = getDelta(item, firstY);
//		
//		final Set<T> closestZ = new HashSet<T>();		
//		final T firstZ = zNav.getNextClosest();
//		closestZ.add(firstZ);		
//		double closestZDelta = getDelta(item, firstZ);
//
//		while(true) {
//			
//			final T newX = xNav.getNextClosest();
//			final T newY = yNav.getNextClosest();
//			final T newZ = zNav.getNextClosest();
//		
//			if(newX != null) {
//				
//				boolean skipped = false;
//				
//				if(Math.abs((item.getX() - newX.getX())) <= closestXDelta) {
//					
//					final double newXDelta = getDelta(item, newX);
//				
//					if(newXDelta == closestXDelta) {
//						closestX.add(newX);
//					} 
//					else if(newXDelta < closestXDelta) {
//						
//						closestXDelta = newXDelta;
//						
//						closestX.clear();
//						closestX.add(newX);
//					}
//				}
//				else {
//					skipped = true;
//				}
//				
//				
//				if(Math.abs((item.getY() - newY.getY())) <= closestYDelta) {
//					
//					skipped = false;
//					
//					final double newYDelta = getDelta(item, newY);
//					
//					if(newYDelta == closestYDelta) {
//						closestY.add(newY);
//					} 
//					else if(newYDelta < closestYDelta) {
//						
//						closestYDelta = newYDelta;
//						
//						closestY.clear();
//						closestY.add(newY);
//					}
//				}
//				
//				
//				if(Math.abs((item.getZ() - newZ.getZ())) <= closestZDelta) {
//					
//					skipped = false;
//					
//					final double newZDelta = getDelta(item, newZ);
//					
//					if(newZDelta == closestZDelta) {
//						closestZ.add(newZ);
//					} 
//					else if(newZDelta < closestZDelta) {
//						
//						closestZDelta = newZDelta;
//						
//						closestZ.clear();
//						closestZ.add(newZ);
//					}
//				}
//				
//				if(skipped) {
//					break;
//				}
//				else {
//					
//					maxSet.clear();
//					
//					maxSet.add(closestX.size());
//					maxSet.add(closestY.size());
//					maxSet.add(closestZ.size());
//					
//					final int maxSize = maxSet.iterator().next();
//					
//					final Set<T> all = new HashSet<T>(closestX);
//					all.addAll(closestY);
//					all.addAll(closestZ);
//					
//					if(all.size() == maxSize) {
//						break;
//					}
//					
//					
//				}
//			}
//			else {
//				break;
//			}
//		}
//			
//		final Set<T> allClosest = new HashSet<T>();
//		
//		allClosest.addAll(closestZ);
//		allClosest.addAll(closestY);
//		allClosest.addAll(closestX);
//		
//		final Set<T> returnVals = new HashSet<T>(allClosest);
//		
//		final double closestDelta = Double.MAX_VALUE;
//		
//		for(final T possible : allClosest) {
//			
//			final double newDelta = getDelta(item, possible);
//			
//			if(newDelta < closestDelta) {
//				returnVals.clear();
//				returnVals.add(possible);
//			}
//			else if(newDelta == closestDelta) {
//				returnVals.add(possible);
//			}
//		}
//		
//		return returnVals;
//	}
//	
//	private static final class ClosestNavigator<T extends SpatialItem> {
//		
//		private enum Axis { X, Y, Z };
//		
//		private NavigableMap<Long, Set<T>> head;
//		private NavigableMap<Long, Set<T>> tail;
//		private final SortedSet<T> currentSet;
//		private long currentHead;
//		private long currentTail;
//		private final long original;
//
//		/**
//		 * 
//		 * @param parent
//		 * @param current
//		 * @param axis
//		 */
//		ClosestNavigator(
//			final TreeMap<Long, Set<T>> parent,
//			final T current,
//			final Axis axis
//		) {
//			
//			final long key = getLocation(axis, current);
//			
//			//head is less than.
//			this.head = parent.headMap(key, true);
//			
//			//tail higher
//			this.tail = parent.tailMap(key, false);
//			
//			currentSet = new TreeSet<T>(
//				new Comparator<T>() {
//	
//					@Override
//					public int compare(T first, T last) {
//						
//						final double delta1 = getDelta(current, first);
//						final double delta2 = getDelta(current, last);
//						
//						final int returnVal;
//						
//						if(delta1 == delta2) {
//							returnVal = 0;
//						}
//						else if(delta1 < delta2) {
//							returnVal = -1;
//						}
//						else {
//							returnVal = 1;
//						}
//						
//						return returnVal;
//					}
//				}
//			);
//			
//			this.currentHead = key;
//			this.currentTail = key;
//			this.original = getLocation(axis, current);
//
//			currentSet.addAll(head.get(key));
//			
//			currentSet.remove(current);
//		}
//		
//		public final T getNextClosest() {
//			
//			final T returnVal;
//			
//			if(currentSet.size() == 0) {
//				
//				Entry<Long, Set<T>> headEntry = head.lowerEntry(currentHead);
//				Entry<Long, Set<T>> tailEntry = tail.higherEntry(currentTail);				
//				
//				if(headEntry != null || tailEntry != null) {
//					
//					if(tailEntry == null) {
//						tailEntry = headEntry;
//					} 
//					else if(headEntry == null) {
//						headEntry = tailEntry;
//					}
//					
//					final long deltaHead = Math.abs(original - headEntry.getKey());
//					final long deltaTail = Math.abs(original - tailEntry.getKey());
//					
//					if(deltaTail < deltaHead) {
//						
//						currentSet.addAll(tailEntry.getValue());
//						currentTail = tailEntry.getKey();
//					}
//					else if(deltaHead < deltaTail) {
//						
//						currentSet.addAll(headEntry.getValue());
//						currentHead = headEntry.getKey();
//					}
//					else {
//						currentSet.addAll(headEntry.getValue());
//						currentSet.addAll(tailEntry.getValue());
//						
//						if(currentHead > headEntry.getKey()) {
//							currentHead = headEntry.getKey();
//						}
//						
//						if(currentTail < tailEntry.getKey()) {
//							currentTail = tailEntry.getKey();
//						}
//					}
//					
//					returnVal = currentSet.first();
//					currentSet.remove(returnVal);
//				}
//				else {
//					
//					returnVal = null;
//				}
//			}
//			else {
//				
//				returnVal = currentSet.first();
//				
//				currentSet.remove(returnVal);
//			}
//						
//			return returnVal;
//		}
//		
//		public static final long getLocation(final Axis axis, SpatialItem item) {
//			
//			final long returnVal;
//			
//			switch(axis) {
//				case X:
//				{
//					returnVal = item.getX();
//					break;
//				}
//				case Y:
//				{
//					returnVal = item.getY();
//					break;
//				}
//				case Z:
//				{
//					returnVal = item.getZ();
//					break;
//				}
//				default:
//					throw new IllegalArgumentException("Invalid axis");
//			}
//			
//			return returnVal;
//		}
//	}
//	
//	public static final double getDelta(SpatialItem item1, SpatialItem item2) {
//		
//		final long xDelta = item1.getX() - item2.getX();
//		final long yDelta = item1.getY() - item2.getY();
//		final long zDelta = item1.getZ() - item2.getZ();
//		
//		return Math.sqrt(
//				(xDelta * xDelta)
//				+ (yDelta * yDelta) 
//				+ (zDelta * zDelta)
//		);
//	}
//
//	@Override
//	public T connectToClosest(T toConnect) {
//		
//		final T connectTo = getClosest(toConnect).iterator().next();
//		
//		final T returnVal = connect(connectTo, toConnect) ? connectTo : null;
//		
//		return returnVal;
//	}
//
//	@Override
//	public List<T> shortestPath(T first, T second) {
//		throw new RuntimeException("Not yet supported");
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public <V> V[] toArray(V[] a) {
//		return (V[]) contains.toArray();
//	}
}
