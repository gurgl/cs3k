package se.bupp.cs3k.server.model

import se.bupp.cs3k.server.GameServerPool.GameProcessSettings
import javax.persistence._
import se.bupp.cs3k.api.AbstractGamePass
import java.io.Serializable
import se.bupp.cs3k.api.{Ticket => ApiTicket}
import se.bupp.cs3k.server.ApiPlayer

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-29
 * Time: 01:54
 * To change this template use File | Settings | File Templates.
 */

object Model {
  type UserId = Long
}

abstract class AbstractGameOccassion {
  def occassionId:Long

  def timeTriggerStart:Boolean
}

@Entity
class GameOccassion extends AbstractGameOccassion {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:Long = _

  var occassionId:Long = _

  def timeTriggerStart = true
}

class NonPersisentGameOccassion(val occassionId:Long) extends AbstractGameOccassion {

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
  var user:ApiPlayer = _

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

}
