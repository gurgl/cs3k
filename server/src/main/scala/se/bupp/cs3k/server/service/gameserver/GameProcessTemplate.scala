package se.bupp.cs3k.server.service.gameserver

import se.bupp.cs3k.server.service.resourceallocation.AllocatedResourceSet
import org.apache.commons.exec.CommandLine
import se.bupp.cs3k.server.Cs3kConfig

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-21
 * Time: 01:01
 * To change this template use File | Settings | File Templates.
 */
object GameProcessTemplate {
  val TcpPortExpression = """\$\{tcp\[(\d*)\]\}""".r
  val UdpPortExpression = """\$\{udp\[(\d*)\]\}""".r
  val Cs3kHostExpression = """\$\{cs3k_port\}""".r
  val Cs3kPortExpression = """\$\{cs3k_host\}""".r

  def applyInstanceCommandLinePlaceholders(s:String, resource:AllocatedResourceSet) = {

    val cmdLineWithTcpAndUdp = Map(TcpPortExpression -> resource.tcps,UdpPortExpression -> resource.udps).foldLeft(s) {
      case (cmdLien, (pattern, resources)) =>
        var resourcesLeft:Seq[Int] = Seq(resources.toSeq:_*)
        resourcesLeft.lift(3)
        val repl = pattern.replaceAllIn(cmdLien,
          matcher => {
            val index: Int = matcher.group(1).toInt
            val removedOpt = resourcesLeft.lift.apply(index)
            removedOpt.map { reducedValue =>
              val res = reducedValue
              val (s,e) = resourcesLeft splitAt index
              resourcesLeft = s ++ (e drop 1)

              "" + res + ""
            }.getOrElse(throw new IllegalArgumentException("Matcher " + matcher.toString() + " aint defined at " +  index))
          }
        )
        if(resourcesLeft.size > 0) {
          throw new IllegalArgumentException("Not all requested resources used")
        }
        repl
    }

    val cmdLineWithMasterHost = Cs3kHostExpression.replaceAllIn(cmdLineWithTcpAndUdp,
      matcher => {
        val res = Cs3kConfig.CS3K_HOST
        "" + res + ""
      })

    val cmdLineWithMasterPort = Cs3kPortExpression.replaceAllIn(cmdLineWithMasterHost,
      matcher => {
        val res = Cs3kConfig.CS3K_PORT
        "" + res + ""
      })

    cmdLineWithMasterPort
  }
}

class GameProcessTemplate(private var commandLineTemplate:String, var clientJNLPUrl:String, val props:Map[String,String],val gameSpecification:GameServerSpecification) {

  import GameProcessTemplate._

  def specifyInstance(resourceSet:AllocatedResourceSet, commandLineExtras:String) : GameProcessSettings = {
    val cmdLine = applyInstanceCommandLinePlaceholders(commandLineTemplate, resourceSet)
    var cmdLineWithExtras: CommandLine = CommandLine.parse(cmdLine + commandLineExtras)

    new GameProcessSettings(cmdLineWithExtras,clientJNLPUrl,props, resourceSet, this)
  }
}

