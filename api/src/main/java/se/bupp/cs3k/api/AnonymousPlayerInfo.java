package se.bupp.cs3k.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousPlayerInfo extends AbstractPlayerInfo implements Serializable {

    public AnonymousPlayerInfo() {
    }

    public AnonymousPlayerInfo(String name) {
        this.name = name;
    }

}