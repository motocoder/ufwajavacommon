package llc.ufwa.data.resource.provider;

import java.net.SocketException;

import llc.ufwa.data.exception.ResourceException;


public interface ResourceProvider<TValue> {
	
	boolean exists() throws ResourceException, SocketException;
	TValue provide() throws ResourceException, SocketException; 
	
}
