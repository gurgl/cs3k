package se.bupp.cs3k.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:20
 * To change this template use File | Settings | File Templates.
 */

public abstract class Ticket extends AbstractGamePass implements Serializable {

    public abstract Long getId();

}