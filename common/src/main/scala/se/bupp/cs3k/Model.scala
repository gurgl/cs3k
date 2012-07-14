package se.bupp.cs3k

import akka.actor.{ActorLogging, Actor}


class Tjena(val a:String, var b:Int) {
  def this() = this("",0)
}

case class Greeting(who: String) //extends Serializable

class GreetingActor extends Actor with ActorLogging {
  def receive = {
    case Greeting(who) ⇒ log.info("Hello " + who)
  }
}
