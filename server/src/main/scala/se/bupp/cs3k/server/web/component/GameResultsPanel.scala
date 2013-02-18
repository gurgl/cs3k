package se.bupp.cs3k.server.web.component

import generic.ListSelector
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.{GameResult, GameOccassion, Ladder}
import org.apache.wicket.markup.repeater.data.{DataView, IDataProvider}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{GameResultDao, GameOccassionDao, LadderDao}
import org.apache.wicket.model.LoadableDetachableModel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.repeater.Item
import java.lang
import org.apache.wicket.Component
import org.springframework.beans.factory.annotation.Autowired
import com.fasterxml.jackson.databind.ObjectMapper
import se.bupp.cs3k.api.score.{ScoreScheme, ContestScore}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class GameResultsPanel(id:String, scoreScheme:ScoreScheme) extends Panel(id) {

  @SpringBean
  var gameResultDao:GameResultDao = _

  @SpringBean
  var objectMapper:ObjectMapper = _


  val provider = new IDataProvider[GameResult]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = gameResultDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = gameResultDao.selectRangeCount

    def model(p1: GameResult) = new LoadableDetachableModel[GameResult](p1) {
      def load() = gameResultDao.find(p1.id).get
    }

    def detach() {}
  }
  import scala.collection.JavaConversions.asScalaBuffer
  import scala.collection.JavaConversions.mapAsJavaMap


  var selector:WebMarkupContainer = _

  selector = new DataView[GameResult]("listSelector", provider) {
    def populateItem(p1: Item[GameResult]) {
      var gameResult: GameResult = p1.getModelObject
      add(new Component("container"){
        def onRender() {
          val bupp = objectMapper.readValue(gameResult.resultSerialized,classOf[ContestScore])
          val competitorsByName = gameResult.game.participants.map( p => (p.id.competitor.id -> p.id.competitor.nameAccessor)).toMap //.toSet[java.lang.Long]
          scoreScheme.renderToHtml(bupp,competitorsByName)
        }
      })

    }
  }
  add(selector.setOutputMarkupId(true))

  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
}
