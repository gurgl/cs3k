package se.bupp.cs3k;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import scala.Option;

import java.io.Serializable;


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-01
 * Time: 05:02
 * To change this template use File | Settings | File Templates.
 */
public class LobbyJoinRequest implements Serializable {
    public long userId;
    public String name;

    public static final long serialVersionUID = 101L;

    public LobbyJoinRequest() {
    }

    public LobbyJoinRequest(long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public scala.Option<scala.Long> userIdOpt() {
        if (userId>0)
            return new scala.Some(userId);
        else
            return Option.apply(null);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LobbyJoinRequest{" +
                "userId=" + userId +
                ", name=" + (name != null ? "'" + name + '\'' : "null") +
                '}';
    }
}
