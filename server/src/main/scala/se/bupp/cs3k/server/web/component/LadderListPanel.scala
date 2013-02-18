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
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar
import org.apache.wicket.event.Broadcast
import se.bupp.cs3k.server.web.component.Events.LadderSelectedEvent


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class LadderListPanel(id:String) extends Panel(id) {

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

  var selector:WebMarkupContainer = _
  selector = new ListSelector[java.lang.Long,Ladder]("listSelector", provider) {
    override def onClick(target: AjaxRequestTarget, modelObject: Ladder) {

      println("Sending event")
      send(getPage(), Broadcast.BREADTH, new LadderSelectedEvent(modelObject, target));

      contentContainer.addOrReplace(new JoinLadderPanel("content",modelObject) {
        def onUpdate(t: AjaxRequestTarget) {

          t.add(selector)
        }
      })
      target.add(contentContainer)
    }

    def renderItem(t: Ladder) = {
      t.competitorType.toString + " ladder " + t.name
    }
  }
  add(selector.setOutputMarkupId(true))



  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new LadderFormPanel("content", "Create ladder") )
}
