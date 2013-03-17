package se.bupp.cs3k.server.web.component

import generic.FodelPropertyColumn
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitionDao, TeamMemberDao, TeamDao}
import org.apache.wicket.extensions.markup.html.repeater.data.table.{DataTable, ISortableDataProvider, PropertyColumn, IColumn}
import se.bupp.cs3k.server.model.{Competition, User, TeamMember}
import org.apache.wicket.model.{IModel, LoadableDetachableModel, Model}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-17
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
class CompetitionParticipationList(id:String, mod:IModel[User]) extends Panel(id) {

  @SpringBean
  var competitionDao:CompetitionDao = _

  @SpringBean
  var teamMemberDao:TeamMemberDao = _

  import scala.collection.JavaConversions.seqAsJavaList

  val columns = List[IColumn[Competition,String]] (
    /*new AbstractColumn[GameOccassion,String](new Model(""))
    {
      def populateItem(cellItem:Item[ICellPopulator[GameOccassion]], componentId:String, model:IModel[GameOccassion])
      {

        cellItem.add(new ResourceLinkComp(componentId,ref,parameters))
      }
    },*/
    new PropertyColumn(new Model("Setup"),"gameSetup.name")
    ,
    new FodelPropertyColumn(new Model("Form"),
      ((a:Competition) => a.getClass.getSimpleName,
        (a:Competition,v:String) => ()
        )),
    new PropertyColumn(new Model("Competitor Type"),"competitorType"),
    new PropertyColumn(new Model("State"),"state")

  )

  var challanges = new ISortableDataProvider[Competition,String] {
    def detach() {}
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = competitionDao.findUserCompetitions(mod.getObject,p1,p2).toIterator

    def size() = competitionDao.findUserCompetitionsCount(mod.getObject)

    def model(p1: Competition) = new LoadableDetachableModel[Competition]() {
      def load() = competitionDao.find(p1.id).get
    }

    def getSortState = null
  }

  var table = new DataTable("table", columns, challanges, 8)
  add(table)


}
