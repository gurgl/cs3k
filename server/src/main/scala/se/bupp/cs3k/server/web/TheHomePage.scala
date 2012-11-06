package se.bupp.cs3k.server.web

import org.apache.wicket.markup.html.{WebMarkupContainer, link, WebPage}
import link.{BookmarkablePageLink, ResourceLink, Link}
import org.apache.wicket.request.resource._
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.util.Scanner
import org.apache.wicket.markup.html.form.{Button, TextField, Form}
import org.apache.wicket.ajax.markup.html.form.AjaxButton
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.{IModel, Model}
import org.apache.wicket.request.{Response, Request}
import org.apache.wicket.request.cycle.RequestCycle
import org.apache.wicket.request.handler.resource.ResourceRequestHandler
import org.apache.wicket.validation.validator.StringValidator
import org.apache.wicket.markup.html.panel.{Panel, FeedbackPanel}
import org.apache.wicket.ajax.form.{OnChangeAjaxBehavior, AjaxFormValidatingBehavior}
import org.apache.wicket.validation.{IValidatable, ValidationError}
import org.apache.wicket.markup.ComponentTag
import org.apache.wicket.Component
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.springframework.context.access.ContextSingletonBeanFactoryLocator
import org.springframework.beans.factory.access.BeanFactoryLocator
import javax.persistence.{Query, EntityManager}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.markup.html.basic.Label
import se.bupp.cs3k.server.User
import org.springframework.transaction.annotation.Transactional
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter
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
    List(("Misc", (s:String) => new MiscPanel(s)), ("Ladder", (s:String) => new LadderPanel(s)))
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
