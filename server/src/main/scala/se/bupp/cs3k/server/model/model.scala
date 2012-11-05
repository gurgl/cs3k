package se.bupp.cs3k.server.model

import se.bupp.cs3k.server.GameServerPool.GameProcessSettings
import javax.persistence._
import se.bupp.cs3k.api.AbstractGamePass
import java.io.Serializable
import se.bupp.cs3k.api.{Ticket => ApiTicket}
import se.bupp.cs3k.server.User

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-29
 * Time: 01:54
 * To change this template use File | Settings | File Templates.
 */

object Model {
  type UserId = java.lang.Long
}

abstract class AbstractGameOccassion {
  def occassionId:Long

  def timeTriggerStart:Boolean
}

@Entity
class Ladder extends Serializable {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _

  var name:String = _
}

@Embeddable
class LadderEnrollmentPk extends Serializable{

  @ManyToOne
  var competitor:Competitor = _

  @ManyToOne
  var ladder:Ladder = _

}

@Entity
class LadderEnrollment {

  @Id
  var id:LadderEnrollmentPk = _

}

@Entity
class Competitor {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _

}



@Entity
case class GameOccassion(var occassionId:Long) extends AbstractGameOccassion {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:Long = _

  def timeTriggerStart = true
}

case class NonPersisentGameOccassion(val occassionId:Long) extends AbstractGameOccassion {

  def timeTriggerStart = true
}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:20
 * To change this template use File | Settings | File Templates.
 */

@Entity
class Ticket() extends ApiTicket with Serializable {

  @ManyToOne
  var user:User = _

  @ManyToOne
  var game:GameOccassion = _

  def this(id: Long) {
    this()
    this.id = id
  }

  override def getId: java.lang.Long = {
    id
  }

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id: Long = _
}



case class RunningGame(var game:AbstractGameOccassion, var processSettings:GameProcessSettings) {

  def isPublic = game == null

  def requiresTicket = game != null && game.isInstanceOf[GameOccassion]
}
