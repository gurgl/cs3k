package se.bupp.cs3k.api;

import se.bupp.cs3k.api.user.AbstractPlayerIdentifier;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:21
 * To change this template use File | Settings | File Templates.
 */
public class IdentifyOnlyPass extends AbstractGamePass implements Serializable {
    AbstractPlayerIdentifier userIdentifier;

    public IdentifyOnlyPass() {
    }

    public IdentifyOnlyPass(AbstractPlayerIdentifier user) {
        this.userIdentifier = user;
    }

    public AbstractPlayerIdentifier getUserIdentifier() {
        return userIdentifier;
    }
}
