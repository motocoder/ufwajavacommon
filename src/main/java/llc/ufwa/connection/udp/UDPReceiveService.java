package llc.ufwa.connection.udp;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.util.DataUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPReceiveService {
    
    private static final Logger logger = LoggerFactory.getLogger(UDPReceiveService.class);
    private static final int MAX_PACKET_SIZE = 1400;
    
    private final DatagramSocket serverSocket;
    private final Set<Callback<Void, ReceivedData>> listeners = 
        new HashSet<Callback<Void, ReceivedData>> ();
    private final Executor bulkThreads;
    
    /**
     * 
     * @param serverPort
     * @param threads - Creates a pool of this many threads
     */
    public UDPReceiveService(
        final int serverPort, 
        final int threads
    ) {
        this(serverPort, Executors.newFixedThreadPool(threads));
    }

    public UDPReceiveService(
        final int serverPort,
        final Executor bulkThreads
    ) {
        
        this.bulkThreads = bulkThreads;
        
        try {
            
            serverSocket = new DatagramSocket(serverPort);
            
            bulkThreads.execute(
                    
                new Runnable() {

                    @Override
                    public void run() {
                        
                        
                        
                        
                        while(true) {
                            
                            try {
                                
                                final byte[] receiveData = new byte[MAX_PACKET_SIZE];
                            
                                final DatagramPacket receivePacket =
                                        new DatagramPacket(receiveData, receiveData.length);
                                
                                serverSocket.receive(receivePacket);                  
                                bulkThreads.execute(new Worker(receivePacket));
                              
                            } 
                            catch (IOException e) {
                                logger.error("<UDPReceiveService><1>, ERROR:", e);
                            }
                              
                        }
                        
                    }
                    
                }
                
            );
            
        } 
        catch (SocketException e) {
            throw new RuntimeException("<UDPReceiveService><2>, Failed to start UDP server.");
        } 
        
    }
    
    private final class Worker implements Runnable {

        private final DatagramPacket packet;
        
        public Worker(final DatagramPacket packet) {
            this.packet = packet;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            
            final String host = packet.getAddress().getHostAddress();    
            
            final int port = packet.getPort();
            
            try {
                
                //deserialize
                final byte [] dataBytes = packet.getData();                
                logger.info("READ UDP PACKET OF SIZE " + dataBytes.length);                
                final Serializable object = DataUtils.deserialize(packet.getData());
                
                //put into collection for sorting
                final Collection<Serializable> received;
                
                if(object instanceof Collection) {
                    received = (Collection<Serializable>) object;
                }
                else {
                    
                    received = new HashSet<Serializable>();
                    received.add(object);
                    
                }
                
                if(received.size() == 0) {                    
                    logger.warn("RECEIVED EMPTY COLLECTION FROM " + host);                    
                }
                else {
                    
                    //notify listeners
                    synchronized(listeners) {
                            
                        for(final Callback<Void, ReceivedData> callback : listeners) {
                            
                            final Collection<Serializable> value = received;
                            
                            //post on seperate threads;
                            bulkThreads.execute(
                                    
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        callback.call(new ReceivedData(host, port, value));
                                    }
                                    
                                }
                                
                            );
                                
                        }
                           
                    }
                    
                }
                
            }
            catch (IOException e) {
                logger.error("<UDPReceiveService><3>, ERROR:", e);
            }
            catch (ClassNotFoundException e) {
                logger.error("<UDPReceiveService><4>, ERROR:", e);
            }
            
        }
        
    }
    
    /**
     * 
     * @param clazz
     * @param listener
     */
    public void addListener(Callback<Void, ReceivedData> listener) {
        
        synchronized(listeners) {
            listeners.add(listener);            
        }
        
    }
    
}
