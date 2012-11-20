package se.bupp.cs3k.server.web.component

import generic.GenericFormPanel
import org.apache.wicket.markup.html.form.{DropDownChoice, TextField, Form}
import se.bupp.cs3k.model.CompetitorType
import se.bupp.cs3k.server.model.{Ladder}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import java.util

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

  def createNew() = new Ladder()

  def populateFields(f: Form[Ladder]) {

    import scala.collection.JavaConversions.seqAsJavaList
    var choice: DropDownChoice[CompetitorType] = new DropDownChoice[CompetitorType]("competitorType", util.Arrays.asList(CompetitorType.values():_*))
    f.add(choice.setRequired(true))
    f.add(new TextField("name"))
  }

  def save(s: Ladder) { ladderDao.insert(s) }
}