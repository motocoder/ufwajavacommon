package llc.ufwa.connection.udp;

import java.io.Serializable;
import java.util.Set;

public class ReceivedData {
    
    private final String host;
    private final int port;
    private final Set<Serializable> data;

    public ReceivedData(final String host, final int port, final Set<Serializable> data) {
        
        this.host = host;
        this.port = port;
        this.data = data;
        
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Set<Serializable> getData() {
        return data;
    }
    
}
