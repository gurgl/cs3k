package se.bupp.cs3k.server.model

import se.bupp.cs3k.server.GameServerPool.GameProcessSettings
import javax.persistence.{GenerationType, GeneratedValue, Id, Entity}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-29
 * Time: 01:54
 * To change this template use File | Settings | File Templates.
 */

abstract class AbstractGameOccassion {
  def occassionId:Long
}

@Entity
class GameOccassion extends AbstractGameOccassion {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:Long = _

  var occassionId:Long = _
}

class NonPersisentGameOccassion(val occassionId:Long) extends AbstractGameOccassion {

}

case class RunningGame(var game:AbstractGameOccassion, var processSettings:GameProcessSettings) {

}
