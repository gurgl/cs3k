package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.{FeedbackPanel, Panel}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.markup.html.form._
import org.apache.wicket.markup.{MarkupStream, MarkupType, MarkupFragment, ComponentTag}
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter
import org.apache.wicket.model.{IModel, Model}
import org.apache.wicket.ajax.form.{OnChangeAjaxBehavior, AjaxFormValidatingBehavior}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.validation.validator.StringValidator
import org.apache.wicket.validation.{IValidatable, ValidationError}
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.request.cycle.RequestCycle
import org.apache.wicket.request.handler.resource.ResourceRequestHandler
import org.apache.wicket.request.resource.{ResourceReference, ContextRelativeResource}
import java.util.Scanner
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.markup.html.link.{ResourceLink, BookmarkablePageLink, Link}
import org.apache.wicket.markup.html.basic.Label
import se.bupp.cs3k.server.web._
import auth.{LoggedInOnly, AnonymousOnly}
import component.CompetitionListPanel
import se.bupp.cs3k.server.model._
import se.bupp.cs3k.server.service.{ResultLogService, ResultService, GameReservationService, CompetitionService}
import se.bupp.cs3k.server.service.dao.{GameResultDao, CompetitorDao}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.{MarkupContainer, RestartResponseException}
import org.slf4j.LoggerFactory
import org.apache.wicket.markup.repeater.RepeatingView
import se.bupp.cs3k.server.model.User
import java.io.{BufferedWriter, OutputStreamWriter, PrintWriter}
import com.fasterxml.jackson.databind.ObjectMapper
import se.bupp.cs3k.example.ExampleScoreScheme.{ExContestScore, ExCompetitorScore}
import se.bupp.cs3k.example.ExampleScoreScheme
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.facade.lobby.LobbyServer
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.model.CompetitionState

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
class PlayPanel(id:String) extends Panel(id) {

  val log = LoggerFactory.getLogger(this.getClass)

  @SpringBean
  var gameResultDao:GameResultDao = _
  @SpringBean
  var gameReservationService:GameReservationService = _

  @SpringBean
  var gameResultService:ResultService = _

  @SpringBean
  var gameLogService:ResultLogService = _

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
      log.debug("err")

      button.setEnabled(false)
    }

    override def onSubmit() {
      log.debug("submitting form")
      button.setEnabled(true)
      //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
      //RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.lobbyResource, new PageParameters()))
    }

    val games = LobbyServer.publicLobbies.map(_._1)
    import scala.collection.JavaConversions.seqAsJavaList
    var selectionModel = new Model[String](games.head)
    var gameSetupSelector = new DropDownChoice[String]("gameSelect", selectionModel, games)
    gameSetupSelector.setRequired(true)
    add(gameSetupSelector)

    val fbp = new FeedbackPanel("feedback")
    fbp.setFilter( new ContainerFeedbackMessageFilter(AnonLaunchForm.this) );
    fbp.setOutputMarkupId(true)
    add(fbp)
    var nameReference: Model[String] = new Model[String]()
    var field: TextField[String] = new TextField[String]("player_name", nameReference)

    field.add(new AjaxFormValidatingBehavior(AnonLaunchForm.this, "onblur") {
      override def onError(target: AjaxRequestTarget) {
        super.onError(target)
        log.debug("beh error")
        //error("bupp")
        //target.add(fbp)
        button.setEnabled(true)
        target.add(button)
      }


      override def onSubmit(target: AjaxRequestTarget) {
        super.onSubmit(target)
        //var resp: Response = resp
        //RequestCycle.get().setResponse(resp)
        log.debug("submitting behav")
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
        log.debug("field.isValid" + field.isValid.toString)
        button.setEnabled(field.isValid)
        target.add(button)
      }
    })

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

      override def onSubmit() {
        log.debug("submitting button, player_name = " + nameReference.getObject)
        //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
        var parameters: PageParameters = new PageParameters()
        parameters.add("player_name", nameReference.getObject)
        parameters.add("lobby_id", selectionModel.getObject)

        log.debug("***  selectionModel.getObject " +  selectionModel.getObject)
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

  @SpringBean
  var competitorDao:CompetitorDao = _



  @LoggedInOnly
  class LoggedInOnlyForm(id:String) extends Form[String](id) {

    var user: User = WiaSession.get().getUser
    val competitors = competitorDao.findByUser(user)
    import scala.collection.JavaConversions.seqAsJavaList

    var selectionModel = new Model[Competitor](null)
    var compSel = new DropDownChoice[Competitor]("competitorSelect", selectionModel, competitors, new IChoiceRenderer[Competitor] {
      def getDisplayValue(p1: Competitor) = p1 match {
        case t:Team => t.name + " (Team)"
        case u:User => u.username
      }

      def getIdValue(p1:Competitor, p2: Int) = p1.id.toString
    })
    compSel.setRequired(true)
    add(compSel)
    val button = new Button("launch_button")
    add(button)

    override def onSubmit() {
      //println("submitting button, player_name = " + nameReference.getObject)
      //override def onSubmit(target: AjaxRequestTarget, form: Form[_]) {
      var parameters: PageParameters = new PageParameters()
      parameters.add("competitor_id", selectionModel.getObject.id)
      parameters.add("user_id", user.id)
      RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.lobbyResource, parameters))
    }
  }

  add(new LoggedInOnlyForm("loggedInForm"))

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


  //add(new LoggedInOnlyButton("logged_in_launch_btn"))






  //add(new LadderFormPanel("ladderform"))


  @LoggedInOnly
  class FFChPanel(id:String) extends PlayerOpenLobbiesPanel(id) {
    override def isVisible = super.isVisible && table.getItemCount > 0
  }

  /*class ChallangePanel(id:String) extends WebMarkupContainer(id) {
    var user: User = WiaSession.get().getUser
    var challanges:List[GameOccassion] =
      gameReservationService.findUnplayedGamesForCompetitor(user)


    import scala.collection.JavaConversions.seqAsJavaList

    add(new ListView[GameOccassion]("challanges",challanges) {
      def populateItem(listItem: ListItem[GameOccassion]) {
        var go = listItem.getModelObject
        var parameters: PageParameters = new PageParameters()
        //parameters.add("competitor_id", selectionModel.getObject.id)
        parameters.add("user_id", user.id)
        parameters.add("game_occassion_id", go.id)

        val ref = new ResourceReference("bupp") {
          def getResource = WicketApplication.get.gameResource
        }
        listItem.add(new ResourceLink[String]("play", ref, parameters) {
        })

      }
    })
  }*/

  class PPCompetitionListPanel(id:String,  m:IModel[Option[CompetitionState]]) extends CompetitionListPanel(id,m) {
    override def isVisible = super.isVisible && table.getItemCount > 0
  }

  add(new PPCompetitionListPanel("openCompetitions", new Model(Some(CompetitionState.SIGNUP))))
  add(new FFChPanel("challangePanel"))


  import scala.collection.JavaConversions.seqAsJavaList
  var all  = new ListModel[GameResult](gameResultDao.findAll)
  import scala.collection.JavaConversions.asScalaBuffer
  import scala.collection.JavaConversions.mapAsJavaMap

  add(new GameResultList("lastGames", all))



  add(new WebMarkupContainer("latestAnonScores") {
    override def onComponentTagBody(markupStream: MarkupStream, openTag: ComponentTag) {
      super.onComponentTagBody(markupStream, openTag)
      val response = getRequestCycle().getResponse();
      val read = gameLogService.read()
      val res = read.split("<br class=\"score-separator\">").map( i => """<table class="table table-striped">""" + i +  "</table>").mkString("<br>")
      response.write(res)
    }
  })
}
