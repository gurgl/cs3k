package se.bupp.cs3k

import com.sun.javaws.progress.Progress
import java.util
import java.net.URL
import com.esotericsoftware.kryo.serializers.{TaggedFieldSerializer, FieldSerializer}
import java.lang.Object
import com.esotericsoftware.shaded.org.objenesis.instantiator.ObjectInstantiator
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input

//import akka.actor.{ActorLogging, Actor}



object LobbyProtocol {
  //def getTypesAndSerializer = typesAndSerializer.forea


  def getTypes = new util.ArrayList[Class[_]](util.Arrays.asList(
    typesAndSerializer.map(_._1) :_*

  ))


  def getTypesAndSerializer = new util.ArrayList[Tuple2[Class[_],ObjectInstantiator]](
    util.Arrays.asList(
      typesAndSerializer.map { case (a,b) => new Tuple2(a,new ObjectInstantiator {
        def newInstance: java.lang.Object = {
          b.apply()
        }
      })
    } :_*
    )
  )

  val typesAndSerializer = List[(Class[_],() => _ <: AnyRef)](
    (classOf[LobbyJoinResponse], () => new LobbyJoinResponse()),
    (classOf[LobbyJoinRequest],() => new LobbyJoinRequest()),
    (classOf[ProgressUpdated],() => new ProgressUpdated()),
    (classOf[StartGame],() => new StartGame()))
}

case class Greeting(who: String) //extends Serializable

/*class GreetingActor extends Actor with ActorLogging {
  def receive = {
    case Greeting(who) ⇒ log.info("Hello " + who)
  }
}*/
