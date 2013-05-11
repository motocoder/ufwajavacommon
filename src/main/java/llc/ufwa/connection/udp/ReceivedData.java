package llc.ufwa.connection.udp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReceivedData {
    
    private final String host;
    private final int port;
    private final List<Serializable> data;

    public ReceivedData(final String host, final int port, final Collection<Serializable> data) {
        
        this.host = host;
        this.port = port;
        this.data = new ArrayList<Serializable>(data);
        
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<Serializable> getData() {
        return data;
    }
    
}
