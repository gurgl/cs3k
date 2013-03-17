package se.bupp.cs3k.server.service

import dao.GameSetupTypeDao
import gameserver.GameProcessTemplate
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model.Model._
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.{GameSetupType, GameType}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 00:17
 * To change this template use File | Settings | File Templates.
 */
@Service
class GameService {

  @Autowired
  var gameSetupTypeDao:GameSetupTypeDao = _

  @Transactional
  def getOrCreateGameSetupTypeEntity(gameTypeId:GameServerTypeId, gameSetupTypeId:GameProcessTemplateId,o:GameProcessTemplateId=>GameSetupType) = {

    gameSetupTypeDao.findGameSetupType(gameTypeId,gameSetupTypeId) match {
      case Some(s) => s
      case None =>
        val gameType = gameSetupTypeDao.findGameType(gameTypeId).get
        //o.gameType = gameType
        var o1: GameSetupType = o(gameSetupTypeId)
        o1.gameType = gameType
        var value = gameSetupTypeDao.insert(o1)
        value
    }
  }

  @Transactional
  def getOrCreateGameTypeEntity(gameTypeId:GameServerTypeId,o:GameServerTypeId=>GameType) = {
    gameSetupTypeDao.findGameType(gameTypeId) match {
      case Some(s) => s
      case None =>
        var value = gameSetupTypeDao.em.persist(o(gameTypeId))
        value
    }
  }
}
