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


    @TaggedFieldSerializer.Tag(1) public String jnlpURL;
    //@TaggedFieldSerializer.Tag(1) public String host;
    //@TaggedFieldSerializer.Tag(3) public int tcpPort;
    //@TaggedFieldSerializer.Tag(4) public int udpPort;




    public StartGame(String jnlpURL) {
        this.jnlpURL = jnlpURL;
    }
    public StartGame() {
    }

    /*public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }*/

    public String getJnlpURL() {
        return jnlpURL;
    }

    public void setJnlpURL(String jnlpURL) {
        this.jnlpURL = jnlpURL;
    }
}
