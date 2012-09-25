package llc.ufwa.collections.geospatial;



public class GeoPlot {//<T extends GeoItem> implements GeoGraph<T> {
	
//	private final TreeMap<Double, Set<T>> xSorted = new TreeMap<Double, Set<T>>();
//	private final TreeMap<Double, Set<T>> ySorted = new TreeMap<Double, Set<T>>();
//	private final TreeMap<Double, Set<T>> zSorted = new TreeMap<Double, Set<T>>();
//	
//	private final Set<T> contains = new HashSet<T>();
//	
//	public GeoPlot() {
//		
//	}
//
//	public GeoPlot(GeoPlot<T> points) {
//		this.addAll(points);
//	}
//
//	@Override
//	public boolean add(T e) {
//
//		addToMaps(e);
//		
//		return true;
//	}
//	
//	private final void addToMaps(T e) {
//		
//		contains.add(e);
//		addSorted(xSorted, e, e.getLatitude());
//		addSorted(ySorted, e, e.getLongitude());
//		addSorted(zSorted, e, e.getAltitude());
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
//	private static final <T extends GeoItem> void addSorted(
//		final TreeMap<Double, Set<T>> map, 
//		final T e, double location
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
//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean addAll(Collection<? extends T> c) {
//		
//		if(c instanceof GeoPlot) {
//			
//			final GeoPlot<T> otherPlot = (GeoPlot<T>)c;
//			
//			final Set<T> added = new HashSet<T>();
//			
//			boolean returnVal = true;
//			
//			for(T elem : otherPlot) {
//				
//				returnVal = this.add(elem);
//				
//				final Set<T> linked = otherPlot.links.get(elem);
//				
//				if(linked != null) {
//					for(T link : linked) {
//						addLink(elem, link);
//					}
//				}
//				
//				if(!returnVal) {
//					remove(elem);
//					remove(added);
//					
//					for(T add : added) {
//						final Set<T> linked2 = otherPlot.links.get(elem);
//						
//						for(T link : linked2) {
//							removeLink(add, link);
//						}
//					}
//					
//					returnVal = false;
//				    break;
//				}
//				
//				added.add(elem);
//			}
//			
//			return returnVal;
//		}
//		else {
//		
//			final Set<T> added = new HashSet<T>();
//			
//			boolean returnVal = true;
//			
//			for(T elem : c) {
//				
//				returnVal = this.add(elem);
//				
//				
//				
//				if(!returnVal) {
//					remove(elem);
//					remove(added);
//					
//					returnVal = false;
//				    break;
//				}
//				
//				added.add(elem);
//			}
//			
//			return returnVal;
//		}
//	}
//
//	private void removeLink(T item1, T item2) {
//		Set<T> links1 = links.get(item1);
//		Set<T> links2 = links.get(item2);
//		
//		if(links1 == null) {
//			links.remove(item1);
//		}
//		
//		if(links2 != null) {
//			links2.remove(item1);
//		}		
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
//		if(o instanceof GeoItem) {
//			GeoItem item = (GeoItem)o;
//			
//			if(contains.remove(o)) {
//				xSorted.get(item.getLatitude()).remove(item);
//				ySorted.get(item.getLongitude()).remove(item);
//				zSorted.get(item.getAltitude()).remove(item);
//				
//				final Set<T> linkedTo = links.remove(item);
//				
//				for(GeoItem linked : linkedTo) {
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
//	private final Map<T, Set<T>> links = new IdentityHashMap<T, Set<T>>();
//
//	private void addLink(T item1, T item2) {
//		
//		Set<T> links1 = links.get(item1);
//		Set<T> links2 = links.get(item2);
//		
//		if(links1 == null) {
//			links1 = new HashSet<T>();
//			links.put(item1, links1);
//		}
//		
//		if(links2 == null) {
//			links2 = new HashSet<T>();
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
//			final Set<T> firstLinks = links.get(first);
//			final Set<T> secondLinks = links.get(second);
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
//				if(Math.abs((item.getLatitude() - newX.getLatitude())) <= closestXDelta) {
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
//				if(Math.abs((item.getLongitude() - newY.getLongitude())) <= closestYDelta) {
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
//				if(Math.abs((item.getAltitude() - newZ.getAltitude())) <= closestZDelta) {
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
//	private static final class ClosestNavigator<T extends GeoItem> {
//		
//		private enum Axis { X, Y, Z };
//		
//		private NavigableMap<Double, Set<T>> head;
//		private NavigableMap<Double, Set<T>> tail;
//		private final SortedSet<T> currentSet;
//		private double currentHead;
//		private double currentTail;
//		private final double original;
//
//		/**
//		 * 
//		 * @param parent
//		 * @param current
//		 * @param axis
//		 */
//		ClosestNavigator(
//			final TreeMap<Double, Set<T>> parent,
//			final T current,
//			final Axis axis
//		) {
//			
//			final double key = getLocation(axis, current);
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
//				Entry<Double, Set<T>> headEntry = head.lowerEntry(currentHead);
//				Entry<Double, Set<T>> tailEntry = tail.higherEntry(currentTail);				
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
//					final double deltaHead = Math.abs(original - headEntry.getKey());
//					final double deltaTail = Math.abs(original - tailEntry.getKey());
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
//		public static final Double getLocation(final Axis axis, GeoItem item) {
//			
//			final double returnVal;
//			
//			switch(axis) {
//				case X:
//				{
//					returnVal = item.getLatitude();
//					break;
//				}
//				case Y:
//				{
//					returnVal = item.getLongitude();
//					break;
//				}
//				case Z:
//				{
//					returnVal = item.getAltitude();
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
//	public static final double getDelta(GeoItem item1, GeoItem item2) {
//		
//		final double xDelta = item1.getLatitude() - item2.getLatitude();
//		final double yDelta = item1.getLongitude() - item2.getLongitude();
//		final double zDelta = item1.getAltitude() - item2.getAltitude();
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
//	@SuppressWarnings({ "unchecked", "hiding" })
//	@Override
//	public <T> T[] toArray(T[] a) {
//		return (T[]) contains.toArray();
//	}
//
//	public Set<T> getConnected(T point) {
//		
//		final Set<T> linked = links.get(point);
//		
//		final Set<T> returnVals;
//		
//		if(linked != null) {
//			returnVals = new HashSet<T>(linked);
//		}
//		else {
//			returnVals = new HashSet<T>();
//		}
//		
//		return returnVals;
//	}
//	
//	/**
//	 * O(N) *shrug*
//	 * 
//	 * @param xStart
//	 * @param xStop
//	 * @param yStart
//	 * @param yStop
//	 * @param zStart
//	 * @param zStop
//	 * @return
//	 */
//	public Set<T> getAllWithin(
//	    final double xStart,
//	    final double xStop, 
//	    final double yStart,
//	    final double yStop, 
//	    final double zStart,
//	    final double zStop
//	) {
//		
//		if(xStart > xStop) {
//			throw new IllegalArgumentException("xStart must be lessThan or equal to xStop");
//		}
//		
//		if(yStart > yStop) {
//			throw new IllegalArgumentException("yStart must be lessThan or equal to yStop");
//		}
//		
//		if(zStart > zStop) {
//			throw new IllegalArgumentException("zStart must be lessThan or equal to zStop");
//		}
//		
//		final Set<T> returnVals = new HashSet<T>();
//		
//		final NavigableMap<Double, Set<T>> xTail = xSorted.tailMap(xStart, true);
//		final NavigableMap<Double, Set<T>> yTail = ySorted.tailMap(yStart, true);
//		final NavigableMap<Double, Set<T>> zTail = zSorted.tailMap(zStart, true);
//		
//		double current = xTail.firstKey();
//		//TODO make a loop/function out of this. 3x redundant code.
//		if(current <= xStop) {
//			
//			while(true) {
//				
//				final Set<T> higher = xTail.get(current);
//				
//				if(higher != null && current <= xStop) {
//					current = xTail.higherKey(current);
//					
//					for(T oneMatch : higher) {
//						
//						final double latitude = oneMatch.getLatitude();
//						final double longitude = oneMatch.getLongitude();
//						final double altitude = oneMatch.getAltitude();
//						
//						if(latitude < xStart || latitude > xStop) {
//							continue;
//						}
//						
//						if(longitude < yStart || longitude > yStop) {
//							continue;
//						}
//						
//						if(altitude < zStart || altitude > zStop) {
//							continue;
//						}
//						
//						returnVals.add(oneMatch);
//					}
//					
//				}
//				else {
//					break;
//				}
//			}
//		}
//		
//		current = yTail.firstKey();
//		
//		if(current <= yStop) {
//			
//			while(true) {
//				
//				final Set<T> higher = yTail.get(current);
//				
//				if(higher != null && current <= yStop) {
//					current = yTail.higherKey(current);
//					
//					for(T oneMatch : higher) {
//						
//						final double latitude = oneMatch.getLatitude();
//						final double longitude = oneMatch.getLongitude();
//						final double altitude = oneMatch.getAltitude();
//						
//						if(latitude < xStart || latitude > xStop) {
//							continue;
//						}
//						
//						if(longitude < yStart || longitude > yStop) {
//							continue;
//						}
//						
//						if(altitude < zStart || altitude > zStop) {
//							continue;
//						}
//						
//						returnVals.add(oneMatch);
//					}
//				}
//				else {
//					break;
//				}
//			}
//		}
//		
//		current = zTail.firstKey();
//		
//		if(current <= zStop) {
//			
//			while(true) {
//				
//				final Set<T> higher = zTail.get(current);
//				
//				if(higher != null && current <= zStop) {
//					current = zTail.higherKey(current);
//					
//					for(T oneMatch : higher) {
//						
//						final double latitude = oneMatch.getLatitude();
//						final double longitude = oneMatch.getLongitude();
//						final double altitude = oneMatch.getAltitude();
//						
//						if(latitude < xStart || latitude > xStop) {
//							continue;
//						}
//						
//						if(longitude < yStart || longitude > yStop) {
//							continue;
//						}
//						
//						if(altitude < zStart || altitude > zStop) {
//							continue;
//						}
//						
//						returnVals.add(oneMatch);
//					}
//				}
//				else {
//					break;
//				}
//			}
//		}
//		
//		return returnVals;
//	}
//
//	public Collection<List<T>> getPaths() {
//		
//		final Collection<List<T>> returnVals = new HashSet<List<T>>();
//		
//		final Set<T> roots = new HashSet<T>();
//		
//		final Map<T, Set<T>> myConnected = new HashMap<T, Set<T>>();
//		
//		for(T point : this.getAll()) {
//			
//			final Set<T> connected = this.getConnected(point);
//			
//			myConnected.put(point, connected);
//			
//			if(connected.size() == 1) {
//				roots.add(point);
//			}
//		}
//		
//		final Set<T> inPath = new HashSet<T>();
//		
//		for(T root : roots) {
//			
//			if(inPath.contains(root)) {
//				continue;
//			}
//			
//			final List<T> path = new ArrayList<T>();
//			path.add(root);
//			inPath.add(root);
//			
//			
//			Iterator<T> iter = myConnected.get(root).iterator();
//			T last = root;
//			
//			while(iter.hasNext()) {
//				
//				final T next = iter.next();
//				
//				path.add(next);
//				inPath.add(next);
//				
//				final Set<T> connected = myConnected.get(next);
//				
//				connected.remove(last);
//				last = next;
//				
//				iter = connected.iterator();
//			}
//			
//			returnVals.add(path);
//		}
//		
//		
//		
//		return returnVals;
//	}
}
