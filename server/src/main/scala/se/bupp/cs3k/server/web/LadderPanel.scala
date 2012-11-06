package se.bupp.cs3k.server.web

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.list.ListView
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.markup.repeater.data.{IDataProvider, DataView}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import org.apache.wicket.model.{LoadableDetachableModel, Model}
import org.apache.wicket.markup.html.basic.Label


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class LadderPanel(id:String) extends Panel(id) {

  @SpringBean
  var ladderDao:LadderDao = _

  val provider = new IDataProvider[Ladder]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = ladderDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = ladderDao.selectRangeCount

    def model(p1: Ladder) = new LoadableDetachableModel[Ladder](p1) {
      def load() = getObject

    }

    def detach() {}
  }

    add(new DataView[Ladder]("list",provider) {
    def populateItem(p1: Item[Ladder]) {
      p1.add(new Label("item",p1.getModelObject.name))

    }
  })

}
