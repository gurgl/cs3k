package se.bupp.cs3k.lobby;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.shaded.org.objenesis.instantiator.ObjectInstantiator;
import scala.Tuple2;
import se.bupp.cs3k.*;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-03
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public class Communication {


    private int lobbyPort;
    private String lobbyHost;

    Client client;
    private LobbyServerHandler lobbyHandler;

    public Communication(int lobbyPort, String lobbyHost, LobbyServerHandler lobbyHandler) {
        this.lobbyPort = lobbyPort;
        this.lobbyHost = lobbyHost;
        this.lobbyHandler = lobbyHandler;
    }

    public void init() {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(BeanSerializer.class);

        for(Tuple2<Class<?>,ObjectInstantiator> clz : LobbyProtocol.getTypesAndSerializer()) {
            System.err.println("Registering " + clz._1().getName());
            kryo.register(clz._1()).setInstantiator(clz._2());
        }
        KryoSerialization kryoSerialization = new KryoSerialization(kryo);
        client = new Client(8192, 2048,kryoSerialization);

        client.addListener(new Listener() {

            public void disconnected (Connection connection) {
                lobbyHandler.onDisconnected(connection);
            }

            public void received(Connection connection, Object object) {
                if (object instanceof LobbyJoinResponse) {
                    System.err.println("LobbyJoinResponse");
                    final LobbyJoinResponse connectMessage = (LobbyJoinResponse) object;
                    lobbyHandler.onLobbyJoinResponse(connectMessage);

                } else if(object instanceof ProgressUpdated) {
                    System.err.println("ProgressUpdated received");
                    final ProgressUpdated upd  =(ProgressUpdated)object;
                    lobbyHandler.onServerNotification(upd);

                } else if(object instanceof StartGame) {
                    final StartGame upd  =(StartGame)object;
                    System.err.println("Start Game Received" + upd.getJnlpURL());
                    lobbyHandler.onStartGame(upd);
                } else {
                    System.err.println("REC + " + object);
                }
            }


        });
    }


    public void join(Long userId, String playerName) {
        client.start();

        try {
            client.connect(5000, lobbyHost, lobbyPort);
        } catch (Exception e) {
            System.err.println("fel");
            e.printStackTrace();
        }

        LobbyJoinRequest r = new LobbyJoinRequest(userId,playerName);
        //r.b_$eq(1);
        client.sendTCP(r);
    }

}
