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
    private final int serverPort;
    private final InetAddress serverHost;

    /**
     * 
     * @param clientPort
     * @param serverHost
     * @param serverPort
     * @throws SocketException
     * @throws UnknownHostException
     */
    public UDPSendService(
        final int clientPort,
        final String serverHost,
        final int serverPort
    ) throws SocketException, UnknownHostException {
        
        this.serverHost = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;        
        this.serverSocket = new DatagramSocket(clientPort);
       
    }
    
    /**
     * 
     * @param data
     * @throws IOException
     */
    public void send(final Serializable data) throws IOException {
        
        final byte [] dataBytes = DataUtils.serialize(data);
        
        if(dataBytes.length > MAX_PACKET_SIZE) {
            throw new RuntimeException("Data exceeded max UDP size");
        }
        
        final DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, serverHost, serverPort);
        
        synchronized(serverSocket) {
            serverSocket.send(packet);
        }
        
    }
    
    

}
