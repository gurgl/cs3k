package se.bupp.cs3k.api;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-20
 * Time: 05:46
 * To change this template use File | Settings | File Templates.
 */
public interface GameServerFacade {

    public AbstractPlayerInfo evaluateGamePass(String pass) throws RemoteException;

    //public void setScore(Integer occassionId, String serializedScore) throws RemoteException;

    public void startGame(Integer occassionId, Map<Integer,Integer> teamsByBlayers) throws RemoteException;

    public void startGame(Integer occassionId, List<Integer> players) throws RemoteException;

    public void endGame(Integer occassionId, String serializedScore) throws RemoteException;
}
