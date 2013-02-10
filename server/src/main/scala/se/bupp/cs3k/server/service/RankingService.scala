package se.bupp.cs3k.server.service

import se.bupp.cs3k.server.model.Model._
import org.springframework.stereotype.Service


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-10
 * Time: 01:42
 * To change this template use File | Settings | File Templates.
 */
@Service
class RankingService {


  // TODO: Add game / rules param
  def getRanking(id:UserId) : Option[Int] = {
    throw new NotImplementedError("not implemented")
  }
}
