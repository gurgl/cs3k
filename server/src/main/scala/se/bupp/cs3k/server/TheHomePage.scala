package se.bupp.cs3k.server

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.link.{ResourceLink, Link}
import org.apache.wicket.request.resource.{ByteArrayResource, ContextRelativeResource, ResourceReference}
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.util.Scanner

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-14
 * Time: 04:39
 * To change this template use File | Settings | File Templates.
 */

class TheHomePage extends WebPage {

  //val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp?port=12345")
  val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp")
  val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next

  //println(out)

  /*lobbyJnlpFile4.

  val ref = new ResourceReference("lank") {
    def getResource = lobbyJnlpFile
  }


  val parameters2: PageParameters = new PageParameters()
  parameters2.add("port","12345")
  */

  val jnlpXML2 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12345\"/>")


  val link2: ResourceLink[ContextRelativeResource] = new ResourceLink[ContextRelativeResource]("lobbyLink2", new ByteArrayResource("application/x-java-jnlp-file", jnlpXML2.getBytes, "lobby2.jnlp"))
  add(link2)

  /*val parameters4: PageParameters = new PageParameters()
  parameters4.add("port","12346")*/
  val jnlpXML4 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12346\"/>")

  val link4: ResourceLink[ContextRelativeResource] = new ResourceLink[ContextRelativeResource]("lobbyLink4", new ByteArrayResource("application/x-java-jnlp-file", jnlpXML4.getBytes, "lobby4.jnlp"))
  add(link4)
}
