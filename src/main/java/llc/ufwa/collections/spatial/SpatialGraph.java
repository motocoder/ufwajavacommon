package llc.ufwa.collections.spatial;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SpatialGraph<T extends SpatialItem> extends Collection<T> {
	
	boolean connect(T inGraph, T item);
	boolean remove(T item);
	boolean disconnect(T first, T second);
	Set<T> getAll(T item);
	Set<T> getAll();
	Set<T> getClosest(T item);
	T connectToClosest(T toConnect);
	List<T> shortestPath(T first, T second);

}
