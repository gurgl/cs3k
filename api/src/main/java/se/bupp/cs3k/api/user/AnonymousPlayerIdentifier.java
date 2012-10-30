package se.bupp.cs3k.api.user;

import se.bupp.cs3k.api.SimplePlayerInfo;
import se.bupp.cs3k.api.user.AbstractPlayerIdentifier;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousPlayerIdentifier extends AbstractPlayerIdentifier implements SimplePlayerInfo {

    public AnonymousPlayerIdentifier() {
    }

    public AnonymousPlayerIdentifier(String name) {
        this.name = name;
    }

    String name;

    public String getName() {
        return name;
    }
}
