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

    public SimplePlayerInfo evaluateGamePass(String pass, Long gameSessionId) throws RemoteException;

    //public void setScore(Integer gameSessionId, String serializedScore) throws RemoteException;

    public void startGame(Long gameSessionId, Map<Integer,Integer> teamsByBlayers) throws RemoteException;

    public void startGame(Long gameSessionId, List<Integer> players) throws RemoteException;

    public void endGame(Long gameSessionId, String serializedScore) throws RemoteException;
}
