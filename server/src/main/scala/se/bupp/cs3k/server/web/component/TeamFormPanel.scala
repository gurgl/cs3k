package se.bupp.cs3k.server.web.component

import generic.GenericFormPanel
import org.apache.wicket.markup.html.form.{TextField, Form}
import se.bupp.cs3k.server.model.Team
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.TeamDao

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-05
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */

class TeamFormPanel(id:String, label:String, ladder:Team) extends GenericFormPanel[Team](id, label, ladder) {

  def this(id:String, label:String) = this(id, label,null)

  @SpringBean
  var teamDao:TeamDao = _

  def createNew() = new Team()

  def populateFields(f: Form[Team]) {

    f.add(new TextField("name"))
  }

  def save(s: Team) { teamDao.insert(s) }
}
