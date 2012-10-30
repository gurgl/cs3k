package se.bupp.cs3k.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:21
 * To change this template use File | Settings | File Templates.
 */
public class AbstractGateGamePass extends AbstractGamePass implements Serializable {
    Long reservationId;

    public AbstractGateGamePass() {
    }

    public AbstractGateGamePass(Long reservationId) {
        this.reservationId = reservationId;
    }
}
