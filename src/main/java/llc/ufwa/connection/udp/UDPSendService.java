package llc.ufwa.connection.udp;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import llc.ufwa.util.DataUtils;

public class UDPSendService {
    
    private static final int MAX_PACKET_SIZE = 1400;
    
    private final DatagramSocket serverSocket;

    /**
     * 
     * @param clientPort
     * @param serverHost
     * @param serverPort
     * @throws SocketException
     * @throws UnknownHostException
     */
    public UDPSendService(
        final int localPort
    ) throws SocketException {
                
        this.serverSocket = new DatagramSocket(localPort);
       
    }
    
    /**
     * 
     * @param data
     * @throws IOException
     */
    public void send(final String remoteHost, final int remotePort, final Serializable data) throws IOException {
        
        final InetAddress serverHost = InetAddress.getByName(remoteHost);
        
        final byte [] dataBytes = DataUtils.serialize(data);
        
        if(dataBytes.length > MAX_PACKET_SIZE) {
            throw new RuntimeException("Data exceeded max UDP size");
        }
        
        final DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, serverHost, remotePort);
        
        synchronized(serverSocket) {
            serverSocket.send(packet);
        }
        
    }
    
}
