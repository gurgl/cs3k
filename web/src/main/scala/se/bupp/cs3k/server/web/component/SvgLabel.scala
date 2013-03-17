package se.bupp.cs3k.server.web.component

import generic.AjaxLinkLabel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.model.IModel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.Model

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-16
 * Time: 02:17
 * To change this template use File | Settings | File Templates.
 */
abstract class SvgAjaxLinkLabel(id:String,txt:IModel[String]) extends AjaxLinkLabel(id,txt) {

}

class SvgLabel(id:String, xm:IModel[java.lang.Float],ym:IModel[java.lang.Float],classm:IModel[String], cm:IModel[Option[String]]) extends Panel(id) {
  add(new AttributeAppender("x", xm))
  add(new AttributeAppender("y", ym))
  add(new AttributeAppender("class", classm," "))
  cm.getObject match {
    case Some(s) => add(new SvgAjaxLinkLabel("cont",new Model(s)) {
      def onClick(p1: AjaxRequestTarget) {

      }
    })
    case None => add(new Label("cont","undecided"))
  }

}
