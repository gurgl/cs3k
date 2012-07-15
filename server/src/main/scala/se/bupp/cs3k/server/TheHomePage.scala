package se.bupp.cs3k.server

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.link.{ResourceLink, Link}
import org.apache.wicket.request.resource.{ContextRelativeResource, ResourceReference}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-14
 * Time: 04:39
 * To change this template use File | Settings | File Templates.
 */

class TheHomePage extends WebPage {

  val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp")

  add(new ResourceLink[ContextRelativeResource]("lobbyLink",lobbyJnlpFile))
}
