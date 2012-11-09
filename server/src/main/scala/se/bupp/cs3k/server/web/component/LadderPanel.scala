package se.bupp.cs3k.server.web.component

import generic.ListSelector
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.markup.repeater.data.IDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import org.apache.wicket.model.LoadableDetachableModel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer


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
      def load() = ladderDao.find(p1.id).get

    }

    def detach() {}
  }


  add(new ListSelector[java.lang.Long,Ladder]("listSelector", provider) {
    override def onClick(target: AjaxRequestTarget, modelObject: Ladder) {
    }

    def renderItem(t: Ladder) = {
      t.name
    }
  })



  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new LadderFormPanel("content") )
}
