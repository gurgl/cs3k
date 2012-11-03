package se.bupp.cs3k;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.io.Serializable;


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-01
 * Time: 05:02
 * To change this template use File | Settings | File Templates.
 */
public class LobbyJoinResponse {

    public int participantsRequired;


    public LobbyJoinResponse(int participantsRequired) {
        this.participantsRequired = participantsRequired;
    }

    public LobbyJoinResponse() {
    }

    public int getParticipantsRequired() {
        return participantsRequired;
    }

    public void setParticipantsRequired(int participantsRequired) {
        this.participantsRequired = participantsRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LobbyJoinResponse)) return false;

        LobbyJoinResponse that = (LobbyJoinResponse) o;

        if (participantsRequired != that.participantsRequired) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return participantsRequired;
    }

    /*class StartGame(val host:String, val tcpPort:Int, val udpPort:Int,val jnlpURL:String) {
        def this() = this("",0,0,"")
    }*/

}
