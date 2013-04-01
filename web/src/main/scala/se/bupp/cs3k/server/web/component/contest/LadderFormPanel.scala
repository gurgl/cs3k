package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.markup.html.form._
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}
import se.bupp.cs3k.server.model.{Tournament, Competition, GameSetupType, Ladder}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{GameSetupTypeDao, LadderDao}
import se.bupp.cs3k.server.service.gameserver.GameServerRepository
import org.apache.wicket.model.util.{MapModel, ListModel}
import org.apache.wicket.validation.{IValidatable, IValidator}
import org.apache.wicket.validation.validator.StringValidator
import org.apache.wicket.model.Model
import se.bupp.cs3k.server.service.CompetitionService
import se.bupp.cs3k.server.web.component.generic.GenericFormPanel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-05
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */

class LadderFormPanel(id:String, label:String, ladder:java.util.Map[String,AnyRef]) extends GenericFormPanel[java.util.Map[String,AnyRef]](id, label, ladder) {

  def this(id:String, label:String) = this(id, label, null)


  @SpringBean
  var competitionService:CompetitionService = _


  @SpringBean
  var ladderDao:LadderDao = _

  @SpringBean
  var gameSetupDao:GameSetupTypeDao = _

  /*def this(id:String, label:String,dao:GameSetupTypeDao,i:Int) = {
    this(id, label, null)

  }*/

  def createNew() = {

    /*val ((gtId,gstId),_) = GameServerRepository.gameServerSetups.head
    val setupType = gameSetupDao.findGameSetupType(gtId, gstId).get

    val ladder = new Ladder("",CompetitorType.TEAM, setupType,CompetitionState.SIGNUP_CLOSED)
      */


    //import scala.collection.JavaConversions.mapAsJavaMap

    var map = new java.util.HashMap[String, AnyRef]()
    Map("name" -> null, "gameSetupType" -> null, "competitionState" -> null, "competitorType" -> null, "competitionForm" -> null).foreach {
      case (k,v) => map.put(k,v)
    }

    map

  }

  var setupsModel:ListModel[GameSetupType] = _
  setupsModel.ensuring(_ != null)
  //setupsModel.setObject(java.util.Arrays.asList({new GameSetupType('Bupp,"asdf","","")}))


  val buppa = new Model[java.util.ArrayList[GameSetupType]](null)


  override def onConfigure() {
    super.onConfigure()
    val all: List[GameSetupType] = gameSetupDao.findAll
    import scala.collection.JavaConversions.seqAsJavaList
    all.ensuring(_ != null)

    setupsModel.setObject(all)
    //setupsModel.getObject.ensuring(_ != null)

  }


  def populateFields(f: Form[java.util.Map[String,AnyRef]]) {

    import scala.collection.JavaConversions.seqAsJavaList

    setupsModel = new ListModel[GameSetupType]()

    val gameSetupChoice: DropDownChoice[GameSetupType] = new DropDownChoice[GameSetupType]("gameSetupType",setupsModel, new ChoiceRenderer[GameSetupType]() {
      override def getDisplayValue(obj: GameSetupType) = obj.name + " (" + obj.gameType.name + ")"

      override def getIdValue(obj: GameSetupType, index: Int) = obj.id.toString
    })

    val competitionForm: DropDownChoice[String] = new DropDownChoice[String]("formOfCompetition", java.util.Arrays.asList(List("Tournament", "Ladder"):_*))
    val competitorTypeChoice: DropDownChoice[CompetitorType] = new DropDownChoice[CompetitorType]("competitorType", java.util.Arrays.asList(CompetitorType.values():_*))

    f.add(competitionForm.setRequired(true))
    f.add(competitorTypeChoice.setRequired(true))
    f.add(gameSetupChoice.setRequired(true))
    f.add(new TextField("name").add(new StringValidator(2,20)).setRequired(true))
  }

  def save(s: java.util.Map[String,AnyRef]) {
    import scala.collection.JavaConversions.mapAsScalaMap
    val (a,b,c,d) = (s("name").toString, CompetitorType.valueOf(s("competitorType").toString), s("gameSetupType").asInstanceOf[GameSetupType], CompetitionState.SIGNUP)

    val comp = s("formOfCompetition") match {
      case "Tournament" => new Tournament(a,b,c,d)
      case "Ladder" => new Ladder(a,b,c,d)
    }
    competitionService.storeCompetition(comp)
  }
}
