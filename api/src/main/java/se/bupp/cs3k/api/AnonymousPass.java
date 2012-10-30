package se.bupp.cs3k.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:21
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousPass extends AbstractGateGamePass implements Serializable {
    String name;

    public AnonymousPass() {
    }

    public AnonymousPass(Long reservationId, String name) {
        super(reservationId);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
