package se.bupp.cs3k.api;

import java.rmi.RemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-20
 * Time: 05:46
 * To change this template use File | Settings | File Templates.
 */
public interface GameServerFacade {

    public AbstractPlayerInfo evaluateGamePass(String pass) throws RemoteException;
}
