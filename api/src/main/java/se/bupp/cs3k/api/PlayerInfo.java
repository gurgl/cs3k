package se.bupp.cs3k.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class PlayerInfo extends RegisteredPlayerInfo {
    String name;

    public PlayerInfo(String name, Long userId) {
        super(userId);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
