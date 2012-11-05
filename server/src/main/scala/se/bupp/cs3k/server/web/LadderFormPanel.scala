package se.bupp.cs3k.server.web

import org.apache.wicket.markup.html.form.{TextField, Form}
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import org.apache.wicket.markup.html.panel.{Panel, FeedbackPanel}
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-05
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */
class LadderFormPanel(id:String, var ladder:Ladder) extends Panel(id) {

  def this(id:String) = this(id,null)

  val isEdit = ladder != null
  add(new LadderForm)
  class LadderForm extends Form[Ladder]("theform") {

    @SpringBean
    var ladderDao:LadderDao = _


    setOutputMarkupId(true)

    if (ladder == null) {
      ladder = new Ladder()
      println("YEAH")
    }

    setDefaultModel(new CompoundPropertyModel(ladder))

    add(new TextField("name"))
    val link = new AjaxSubmitLink("submit") {
      override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
        //super.onSubmit(target, form)

        var lNew: Ladder = LadderForm.this.getModel.getObject

        ladderDao.insert(lNew)
        if(isEdit)
          this.setEnabled(false)
        else
          LadderForm.this.setDefaultModelObject(new Ladder())

        info("Saved" + lNew.name)

        println(ladderDao.findAll.mkString(","))

        println(ladderDao.count)
        target.add(LadderForm.this)
      }
    }
    link.setOutputMarkupId(true)
    add(link)
    add(new FeedbackPanel("feedback") {

    }.setFilter( new ContainerFeedbackMessageFilter(LadderForm.this) ).setOutputMarkupId(true))

  }
}
