package se.bupp.cs3k.api.user;

import se.bupp.cs3k.api.user.AbstractPlayerIdentifier;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class RegisteredPlayerIdentifier extends AbstractPlayerIdentifier {
    Long userId;

    public RegisteredPlayerIdentifier(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
