package se.bupp.cs3k.api;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class RegisteredPlayerInfo extends AbstractPlayerInfo {
    Long userId;

    public RegisteredPlayerInfo(Long userId) {
        this.userId = userId;
    }
}
