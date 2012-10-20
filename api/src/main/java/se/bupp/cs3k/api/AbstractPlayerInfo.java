package se.bupp.cs3k.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:21
 * To change this template use File | Settings | File Templates.
 */
public class AbstractPlayerInfo implements Serializable {
    String name;

    public String getName() {
        return name;
    }
}
