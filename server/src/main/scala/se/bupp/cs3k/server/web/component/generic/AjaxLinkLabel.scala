package se.bupp.cs3k.server.web.component.generic

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.IModel

/**
  * Created with IntelliJ IDEA.
  * User: karlw
  * Date: 2013-02-18
  * Time: 19:56
  * To change this template use File | Settings | File Templates.
  */
abstract class AjaxLinkLabel(id:String, model:IModel[String]) extends Panel(id) {

   def onClick(p1: AjaxRequestTarget)

   setRenderBodyOnly(true)

   var link = new AjaxLink("link") {
     def onClick(p1: AjaxRequestTarget) {
       AjaxLinkLabel.this.onClick(p1)
     }
   }
   link.add(new Label("label",model))
   add(link)
 }
