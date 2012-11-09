package se.bupp.cs3k.server.web


import component.{TeamPanel, LadderPanel, MiscPanel}
import org.apache.wicket.markup.html.{WebMarkupContainer, link}
import link.BookmarkablePageLink
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.markup.html.basic.Label

import org.apache.wicket.markup.html.list.{ListItem, ListView}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-14
 * Time: 04:39
 * To change this template use File | Settings | File Templates.
 */

class TheHomePage extends AbstractBasePage {


  @LoggedInOnly class LogoutLink extends BookmarkablePageLink("logout", classOf[SignOutPage])
  @AnonymousOnly class LoginLink extends BookmarkablePageLink("login", classOf[SigninPage])
  @AnonymousOnly class RegisterLink extends BookmarkablePageLink("register", classOf[RegisterPage])
  add(new LogoutLink)
  add(new LoginLink)
  add(new RegisterLink)



  import scala.collection.JavaConversions.seqAsJavaList

  var menuPanel = new ListView[(String,Function1[String,Panel])]("menu",
    List(("Misc", (s:String) => new MiscPanel(s)), ("Ladder", (s:String) => new LadderPanel(s)), ("Team", (s:String) => new TeamPanel(s)))
  ) {
    def populateItem(p1: ListItem[(String,Function1[String,Panel])]) {
      val (labelText, panelGen) = p1.getModelObject

      var label = new Label("linkLabel", labelText).setOutputMarkupId(true)
      val link = new AjaxLink[String]("item") {
        def onClick(target: AjaxRequestTarget) {
          val comp = panelGen.apply("content")
          contentContainer.addOrReplace(comp)
          target.add(contentContainer)
          target.add(label)
        }
      }
      link.add(label)
      p1.add(link)

    }
  }
  add(menuPanel)


  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new MiscPanel("content"))

}
