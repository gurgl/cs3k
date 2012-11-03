package se.bupp.cs3k;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-01
 * Time: 05:11
 * To change this template use File | Settings | File Templates.
 */
public class StartGame implements Serializable {

    public static final long serialVersionUID = 104L;


    @TaggedFieldSerializer.Tag(1) public String host;
    @TaggedFieldSerializer.Tag(2) public int tcpPort;
    @TaggedFieldSerializer.Tag(3) public int udpPort;
    @TaggedFieldSerializer.Tag(4) public String jnlpURL;



    public StartGame(String host, int tcpPort, int udpPort, String jnlpURL) {
        this.host = host;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.jnlpURL = jnlpURL;
    }
    public StartGame() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public String getJnlpURL() {
        return jnlpURL;
    }

    public void setJnlpURL(String jnlpURL) {
        this.jnlpURL = jnlpURL;
    }
}
