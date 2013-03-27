package se.bupp.cs3k.server.service

import dao.NewsItemDao
import se.bupp.cs3k.server.model._
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.model.{NewsItemType, CompetitionState}
import org.joda.time.{Weeks, Interval, Instant}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-20
 * Time: 21:55
 * To change this template use File | Settings | File Templates.
 */


@Service
class GameNewsService {

  @Autowired
  var newsItemDao:NewsItemDao = _

  @Transactional
  def competitorJoinedCompetition(competitor:Competitor,competition:Competition) {

    val item = new NewsItem(competition, competitor, null, NewsItemType.COMPETITOR_JOINED_COMPETITION, null, new Instant())
    newsItemDao.insert(item)
    broadCastToCompetitionPlayers(item,competition)
  }



  @Transactional
  def competitorLeftCompetition(competitor:Competitor,competition:Competition) {

    val item = new NewsItem(competition, competitor, null, NewsItemType.COMPETITOR_LEFT_COMPETITION, null, new Instant())
    newsItemDao.insert(item)
    broadCastToCompetitionPlayers(item,competition)
  }
  @Transactional
  def userJoinedTeam(competitor:User,team:Team) {
    val item = new NewsItem(null, team, competitor, NewsItemType.USER_JOINED_TEAM, null, new Instant())
    broadcastToTeamMembers(team, item)
    newsItemDao.insert(item)


  }

  @Transactional
  def userLeftTeam(competitor:User,team:Team) {
    val item = new NewsItem(null, team, competitor, NewsItemType.USER_LEFT_TEAM, null, new Instant())
    newsItemDao.insert(item)
    broadcastToTeamMembers(team, item)
  }
  @Transactional
  def competitionChangedState(competition:Competition, state:CompetitionState) {
    val item = new NewsItem(competition, null, null, NewsItemType.COMPETITION_STATE_CHANGE, state, new Instant())
    newsItemDao.insert(item)
    broadCastToCompetitionPlayers(item,competition)
  }
  @Transactional
  def winner(competitor:Competitor,competition:Competition) {

    val item = new NewsItem(competition, competitor, null, NewsItemType.COMPETITOR_COMPETITION_GAME_VICTORY, null, new Instant())
    newsItemDao.insert(item)
    broadCastToCompetitionPlayers(item,competition)
  }

  def broadcastToTeamMembers(team: Team, item: NewsItem) {
    import scala.collection.JavaConversions.asScalaBuffer
    import scala.collection.JavaConversions.seqAsJavaList
    var newsItems: mutable.Buffer[UserNewsItem] = team.members.map(u => new UserNewsItem(item, u.id.user))
    //item.subscribers = newsItems
    newsItems.foreach(un => newsItemDao.em.persist(un))
  }

  private def broadCastToCompetitionPlayers(item:NewsItem,competition:Competition) {
    import scala.collection.JavaConversions.asScalaBuffer
    import scala.collection.JavaConversions.seqAsJavaList
    //t.members.foreach(tm => )
    val recipiants = competition.participants.flatMap { p =>
      p.id.competitor match {
        case t:Team => t.members.map(_.id.user)
        case u:User => List(u)
      }
    }

    val newsItems: mutable.Buffer[UserNewsItem] = recipiants.map(u => new UserNewsItem(item, u))
    //item.subscribers = newsItems
    newsItems.foreach(un => newsItemDao.em.persist(un))

  }


  //private def readEvent()

  def getPlayerLatestMessages(user:User,time:Instant) = {
    val items: List[UserNewsItem] = newsItemDao.findByUser(user, getDefaultInterval(time))
    items.map(_.newsItem)
  }


  def getTeamLatestMessages(team:Team,time:Instant) = {
    // competitor
    newsItemDao.findByTeam(team,getDefaultInterval(time))
  }

  def getCompetitionLatestMessages(competition:Competition, time:Instant) = {
    // competition
    newsItemDao.findByCompetition(competition,getDefaultInterval(time))
  }

  def getGlobalLatestMessages(time:Instant) = {
    // find by time
    newsItemDao.findAll(getDefaultInterval(time))
  }

  def getDefaultInterval(time: Instant): Interval = {
    new Interval(time.minus(Weeks.weeks(4).toStandardDuration),time)
  }
}


/*


  def getPlayerLatestMessages(user:User,time:Instant) {
    // userNewsItems -> newsItem -> T/C

  def getTeamLatestMessages(team:Team,time:Instant) {
    // newsItem -> T
  def getCompetitionLatestMessages(competition:Competition, time:Instant) {
    // newsItem -> C
  def getGlobalLatestMessages(time:Instant)
    // newsItem

*/