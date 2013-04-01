package se.bupp.cs3k.server.web.component.generic

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.request.resource.ResourceReference
import org.apache.wicket.request.mapper.parameter.PageParameters

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-11
 * Time: 00:15
 * To change this template use File | Settings | File Templates.
 */
class ResourceLinkComp(id:String,ref: ResourceReference, parameters: PageParameters) extends Panel(id) {
  var body = new ResourceLink[String]("link", ref, parameters)
  add(body)
}
