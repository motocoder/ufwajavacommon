package llc.ufwa.data.resource.provider;


public interface ResourceProvider<TValue> {
	
	boolean exists();
	TValue provide(); 
	
}
