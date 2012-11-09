package se.bupp.cs3k.server.web.component

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
abstract class GenericFormPanel[T](id:String, var ladder:T) extends Panel(id) {

  def this(id:String) = this(id, null.asInstanceOf[T])

  val isEdit = ladder != null
  add(new GenericForm)

  def createNew() : T

  def populateFields(f:Form[T])
  def save(s:T)

  class GenericForm extends Form[T]("theform") {

    @SpringBean
    var ladderDao:LadderDao = _


    setOutputMarkupId(true)

    if (ladder == null) {
      ladder = createNew
      println("YEAH")
    }

    setDefaultModel(new CompoundPropertyModel(ladder))

    populateFields(GenericForm.this)



    val link = new AjaxSubmitLink("submit") {
      override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
        //super.onSubmit(target, form)

        var lNew: T = GenericForm.this.getModel.getObject

        save(lNew)
        if(isEdit)
          this.setEnabled(false)
        else
          GenericForm.this.setDefaultModelObject(createNew())

        info("Saved" )

        println(ladderDao.findAll.mkString(","))

        println(ladderDao.count)
        target.add(GenericForm.this)
      }
    }
    link.setOutputMarkupId(true)
    add(link)
    add(new FeedbackPanel("feedback") {

    }.setFilter( new ContainerFeedbackMessageFilter(GenericForm.this) ).setOutputMarkupId(true))

  }
}
