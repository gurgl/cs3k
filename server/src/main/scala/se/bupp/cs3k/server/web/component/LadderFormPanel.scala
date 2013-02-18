package se.bupp.cs3k.server.web.component

import generic.GenericFormPanel
import org.apache.wicket.markup.html.form.{DropDownChoice, TextField, Form}
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}
import se.bupp.cs3k.server.model.{Ladder}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{GameSetupTypeDao, LadderDao}
import java.util
import se.bupp.cs3k.server.service.gameserver.GameServerRepository

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-05
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */

class LadderFormPanel(id:String, label:String, ladder:Ladder) extends GenericFormPanel[Ladder](id, label, ladder) {

  def this(id:String, label:String) = this(id, label, null)

  @SpringBean
  var ladderDao:LadderDao = _

  @SpringBean
  var gameSetupDao:GameSetupTypeDao = _


  def createNew() = {
    var ladder = new Ladder()
    var ((gtId,gstId),_) = GameServerRepository.gameServerSetups.head
    ladder.gameSetup = gameSetupDao.findGameSetupType(gtId,gstId).get

    ladder.state = CompetitionState.ANNOUNCED
    ladder
  }

  def populateFields(f: Form[Ladder]) {

    import scala.collection.JavaConversions.seqAsJavaList
    var choice: DropDownChoice[CompetitorType] = new DropDownChoice[CompetitorType]("competitorType", util.Arrays.asList(CompetitorType.values():_*))
    f.add(choice.setRequired(true))
    f.add(new TextField("name"))
  }

  def save(s: Ladder) { ladderDao.insert(s) }
}
