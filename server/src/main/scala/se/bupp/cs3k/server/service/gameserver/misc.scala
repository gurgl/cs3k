package se.bupp.cs3k.server.service.gameserver


import se.bupp.cs3k.server.Cs3kConfig
import org.apache.commons.exec.CommandLine
import se.bupp.cs3k.server.service.GameReservationService._
import java.net.URL
import se.bupp.cs3k.server.model.Model._
import se.bupp.cs3k.server.service.resourceallocation.{ResourceNeeds, AllocatedResourceSet}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-21
 * Time: 00:42
 * To change this template use File | Settings | File Templates.
 */

class GameServerSpecification(val cmdStr:String,val resourceNeeds:ResourceNeeds) {

  def create(args:String, jnlpPath:String, props:Map[String,String]) = {
    new GameProcessTemplate(cmdStr + args, "http://" + Cs3kConfig.REMOTE_IP + ":8080/" + jnlpPath, props, this)
  }
}

class GameProcessSettings(var commandLine:CommandLine, var clientJNLPUrl:String, val props:Map[String,String], val resourceSet: AllocatedResourceSet, val gpt:GameProcessTemplate) {

  //def jnlpUrl(reservationId:NonPersistentOccassionTicketId,name:String) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&player_name=" + name)
  // TODO: Move to lobby someway - as it decides what params are needed
  def jnlpUrl(reservationId:GameServerReservationId,name:Option[UserId]) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId)
}



