package se.bupp.cs3k.server.web

import component.GenericFormPanel
import org.apache.wicket.markup.html.form.{TextField, Form}
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import org.apache.wicket.markup.html.panel.{Panel, FeedbackPanel}
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-05
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */

class LadderFormPanel2(id:String, ladder:Ladder) extends GenericFormPanel[Ladder](id,ladder) {

  def this(id:String) = this(id,null)

  @SpringBean
  var ladderDao:LadderDao = _

  def createNew() = new Ladder()

  def populateFields(f: Form[Ladder]) {

    f.add(new TextField("name"))
  }

  def save(s: Ladder) { ladderDao.insert(s) }
}
