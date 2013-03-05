package se.bupp.cs3k.server.web.component.generic

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.Component

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-21
 * Time: 23:55
 * To change this template use File | Settings | File Templates.
 */
class VertTabbedPanel(id:String, tabs:List[(String, String => Component)]) extends AbstractTabbedPanel(id,tabs) {

}
