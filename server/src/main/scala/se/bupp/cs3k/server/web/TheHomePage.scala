package se.bupp.cs3k.server.web

import org.apache.wicket.markup.html.{WebMarkupContainer, link, WebPage}
import link.{BookmarkablePageLink, ResourceLink, Link}
import org.apache.wicket.request.resource._
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.util.Scanner
import org.apache.wicket.markup.html.form.{Button, TextField, Form}
import org.apache.wicket.ajax.markup.html.form.AjaxButton
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import org.apache.wicket.request.{Response, Request}
import org.apache.wicket.request.cycle.RequestCycle
import org.apache.wicket.request.handler.resource.ResourceRequestHandler
import org.apache.wicket.validation.validator.StringValidator
import org.apache.wicket.markup.html.panel.FeedbackPanel
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


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-14
 * Time: 04:39
 * To change this template use File | Settings | File Templates.
 */

class TheHomePage extends WebPage {


  @SpringBean(name = "mySBean")
  var beanan: MyBean = _

  @AnonymousOnly
  class AnonLaunchForm(id: String) extends Form[String](id) {

    val JS_SUPPRESS_ENTER = "if(event.keyCode==13 || window.event.keyCode==13){return false;}else{return true;}";


    override def onComponentTag(tag: ComponentTag) {
      super.onComponentTag(tag)
      tag.put("onkeydown", JS_SUPPRESS_ENTER);
      tag.put("onkeypress", JS_SUPPRESS_ENTER);
    }

    override def onError() {
      super.onError()
      println("err")

      button.setEnabled(false)
    }

    override def onSubmit() {
      println("submitting form")
      button.setEnabled(true)
      //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
      //RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.lobbyResource, new PageParameters()))
    }


    val fbp = new FeedbackPanel("feedback")
    fbp.setOutputMarkupId(true)
    add(fbp)
    var nameReference: Model[String] = new Model[String]()
    var field: TextField[String] = new TextField[String]("player_name", nameReference)

    field.add(new AjaxFormValidatingBehavior(AnonLaunchForm.this, "onblur") {
      override def onError(target: AjaxRequestTarget) {
        super.onError(target)
        println("beh error")
        //error("bupp")
        //target.add(fbp)
        button.setEnabled(true)
        target.add(button)
      }


      override def onSubmit(target: AjaxRequestTarget) {
        super.onSubmit(target)
        //var resp: Response = resp
        //RequestCycle.get().setResponse(resp)
        println("submitting behav")
        button.setEnabled(true)
        target.add(button)
        //WicketApplication.get.get
        //super.onSubmit(target, form)
      }
    })

    /*field.add(new AjaxB(AnonLaunchForm.this, "inputchange") {

      override def onSubmit(target: AjaxRequestTarget) {
        super.onSubmit(target)
        //var resp: Response = resp
        //RequestCycle.get().setResponse(resp)
        println("submitting behav")
        button.setEnabled(true)
        target.add(button)
        //WicketApplication.get.get
        //super.onSubmit(target, form)
      }
    })*/

    field.add(new OnChangeAjaxBehavior() {


      override def onError(target: AjaxRequestTarget, e: RuntimeException) {
        super.onError(target, e)
        if (e != null) {
          e.printStackTrace()
        } else {
          button.setEnabled(field.isValid)
          target.add(button)
        }
      }

      def onUpdate(target: AjaxRequestTarget) {
        println(field.isValid)
        button.setEnabled(field.isValid)
        target.add(button)
      }
    })

    //var behavior: AjaxFormValidatingBehavior =
    //*/
    /*var behavior2: AjaxFormValidatingBehavior = new AjaxFormValidatingBehavior(nameLaunchForm, "onsubmit") {
      override def onError(target: AjaxRequestTarget) {
        super.onError(target)
        println("beh error")
        error("bupp")
        target.add(fbp)
      }
    }*/


    //
    field.add(new StringValidator(3, 20) {
      override def decorate(error: ValidationError, validatable: IValidatable[String]) = {
        super.decorate(error, validatable)
        error.setMessage("Ska va mellan 3 o 20")
        error
      }
    })
    field.setRequired(true)
    AnonLaunchForm.this.add(field);

    button = new Button("launch_button") {
      /*override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
        //var resp: Response = resp
        RequestCycle.get().setResponse(resp)

        //WicketApplication.get.get
        //super.onSubmit(target, form)
      }*/

      override def onSubmit() {
        println("submitting button, player_name = " + nameReference.getObject)
        //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
        var parameters: PageParameters = new PageParameters()

        parameters.add("player_name", nameReference.getObject)
        RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.lobbyResource, parameters))
      }


    }
    button.setOutputMarkupId(true)
    button.setEnabled(false)
    //button.add(behavior2)
    AnonLaunchForm.this.add(button)

  }


  //val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp?port=12345")
  val lobbyJnlpFile = new ContextRelativeResource("./lobbyX.jnlp")
  val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next


  var button: Button = _

  add(new AnonLaunchForm("launch_with_name_form"))

  val testLinks = new WebMarkupContainer("testLinks")

  testLinks.add(new AjaxLink("testLink") {
    def onClick(target: AjaxRequestTarget) {
      //var instance: BeanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance()
      //instance.useBeanFactory()
      beanan.read();

    }
  })

  testLinks.add(new AjaxLink("testLink2") {
    def onClick(target: AjaxRequestTarget) {
      //var instance: BeanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance()
      beanan.store()
      //var q: Query = em.createQuery("from User")
      //val res = q.getResultList.mkString(",")
      //println(res)

    }
  })

  testLinks.setVisibilityAllowed(WicketApplication.get.isDevMode)

  //AjaxFormValidatingBehavior.addToAllFormComponents(nameLaunchForm, "onblur")
  //println(out)

  /*lobbyJnlpFile4.

  val ref = new ResourceReference("lank") {
    def getResource = lobbyJnlpFile
  }


  val parameters2: PageParameters = new PageParameters()
  parameters2.add("port","12345")
  */

  /*
  var resource: ByteArrayResource = _

  val link2: ResourceLink[ContextRelativeResource] = new ResourceLink[ContextRelativeResource]("lobbyLink2", resourceRef)
  var resourceRef = new ResourceReference("ref") {
    def getResource = {
      val jnlpXML2 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12345\"/>")

        .replace("http://localhost:8080/", "http://" + LobbyServer.remoteIp +":8080/")
        .replace("Test.jnlp", "http://" + LobbyServer.remoteIp +":8080/" + link2)
      new ByteArrayResource("application/x-java-jnlp-file",jnlpXML2.getBytes, "Test.jnlp")
    }
  } */


  //val link2: ResourceLink[ContextRelativeResource] = new ResourceLink[ContextRelativeResource]("lobbyLink2", new SharedResourceReference(classOf[Application], WicketApplication.resourceKey2))

  //add(link2)

  /*val parameters4: PageParameters = new PageParameters()
  parameters4.add("port","12346")*/
  /*val jnlpXML4 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12346\"/>")

  val link4: ResourceLink[ContextRelativeResource] = new ResourceLink[ContextRelativeResource]("lobbyLink4", new ByteArrayResource("application/x-java-jnlp-file", jnlpXML4.getBytes, "lobby4.jnlp"))
  add(link4)*/



  @LoggedInOnly
  class LoggedInOnlyButton(id:String) extends Link[String](id) {
    override def onClick() {


      //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
      var parameters: PageParameters = new PageParameters()
      val user: User = WiaSession.get.getUser
      parameters.add("user_id", user.id)
      RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.lobbyResource, parameters))

    }
  }
  /*
  @LoggedInOnly
  class LoggedInOnlyButton(id:String) extends Button(id) {
    /*override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
      //var resp: Response = resp
      RequestCycle.get().setResponse(resp)

      //WicketApplication.get.get
      //super.onSubmit(target, form)
    }*/

    override def onSubmit() {
      //println("submitting button, player_name = " + nameReference.getObject)
      //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
      var parameters: PageParameters = new PageParameters()

      val user: User = WiaSession.get.getUser
      parameters.add("user_id", user.id)
      RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.lobbyResource, parameters))
    }
  }       */

  add(new LoggedInOnlyButton("logged_in_launch_btn"))


  @LoggedInOnly class LogoutLink extends BookmarkablePageLink("logout", classOf[SignOutPage])
  @AnonymousOnly class LoginLink extends BookmarkablePageLink("login", classOf[SigninPage])
  @AnonymousOnly class RegisterLink extends BookmarkablePageLink("register", classOf[RegisterPage])
  add(new LogoutLink)
  add(new LoginLink)
  add(new RegisterLink)

  @LoggedInOnly
  class AdminOnlyLabel(id: String, text: String) extends Label(id, text)

  add(new AdminOnlyLabel("lbl", "Tja"))

}
