package se.bupp.cs3k.server.service.gameserver

import collection.immutable.HashMap
import se.bupp.cs3k.server.model.Model._
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model.{RunningGame, GameOccassion}
import se.bupp.cs3k.server.Init

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-21
 * Time: 00:43
 * To change this template use File | Settings | File Templates.
 */

class GameServerRepository  {


  var gameServerTypes = new HashMap[GameServerTypeId,GameServerSpecification]()
  var gameServerSetups = new HashMap[(GameServerTypeId,GameProcessTemplateId),GameProcessTemplate]()

  def reset {
    gameServerTypes = gameServerTypes.empty
    gameServerSetups = gameServerSetups.empty
  }

  /*def findByProcessTemplate(ss:GameProcessTemplateId) : Option[GameProcessTemplate] = {
    gameServerSetups.find( _._1._2 == ss).map(_._2)
  }*/
  def findBy(ss:GameAndRulesId) : Option[GameProcessTemplate] = {
    gameServerSetups.get(ss)
  }

  def add(id:GameServerTypeId,spec: GameServerSpecification) = {
    gameServerTypes = gameServerTypes + (id -> spec)
  }

  def addProcessTemplate(id:(GameServerTypeId, GameProcessTemplateId),spec: GameProcessTemplate) = {
    gameServerSetups = gameServerSetups + (id -> spec)
  }

}

@Service
class GameServerRepositoryService {
  def spawnServer(template: GameProcessTemplate, occassion: GameOccassion, processToken: Int): RunningGame  = GameServerPool.pool.spawnServer(template, occassion, processToken)

  def findBy(ss:GameAndRulesId) : Option[GameProcessTemplate] = {
    Init.gameServerRepository.findBy(ss)
  }
}
