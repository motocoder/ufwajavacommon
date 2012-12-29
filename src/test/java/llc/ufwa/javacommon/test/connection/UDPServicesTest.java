package llc.ufwa.javacommon.test.connection;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.connection.udp.ReceivedData;
import llc.ufwa.connection.udp.UDPReceiveService;
import llc.ufwa.connection.udp.UDPSendService;
import llc.ufwa.util.StopWatch;

import org.junit.Test;

public class UDPServicesTest {

    private static final int SERVER_PORT = 45677;
    private static final int CLIENT_PORT = 45678;
    
    @Test 
    public void testUDP() {
        
        final Set<String> receivedStrings = new HashSet<String>();
        
        try {
            
            final UDPReceiveService receive = new UDPReceiveService(SERVER_PORT, 50);
            final UDPSendService send = new UDPSendService(CLIENT_PORT, "localhost", SERVER_PORT);
            
            final int MAX_SEND = 50000;
            
            receive.addListener(
                String.class,
                new Callback<Void, ReceivedData>() {

                        @Override
                        public boolean call(final Void source, final ReceivedData value) {
                            
                            final String data = (String) value.getData().iterator().next();
                            
                            synchronized(receivedStrings) {
                                
                                receivedStrings.add(data);
                                
                                if(receivedStrings.size() == MAX_SEND) {
                                    receivedStrings.notifyAll();
                                }
                                
                            }
                            
                            return false;
                            
                        }
                    }
                );
            
            final StopWatch watch = new StopWatch();
            watch.start();
            
            new Thread() {
                @Override
                public void run() {
                    
                    for(int i = 0; i < MAX_SEND; i++) {
                        
                        try {
                            send.send(String.valueOf(i));
                        } 
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        
                    }
                }
            }.start();
            
            synchronized(receivedStrings) {
                
                while(receivedStrings.size() < MAX_SEND && watch.getTime() < 5000) {
                    
                    try {
                        receivedStrings.wait(5000);
                    } 
                    catch (InterruptedException e) {
                        TestCase.fail();
                    }
                    
                }
            }
            
            TestCase.assertEquals(MAX_SEND, receivedStrings.size());
            
            for(int i = 0; i < MAX_SEND; i++) {
                TestCase.assertTrue(receivedStrings.contains(String.valueOf(i)));
            }
            
            System.out.println("sent " + MAX_SEND + " in " + watch.getTime() + "ms");
            
        } 
        catch (SocketException e) {
            TestCase.fail();
        } 
        catch (UnknownHostException e) {
            TestCase.fail();
        }
        
    }
    
}

