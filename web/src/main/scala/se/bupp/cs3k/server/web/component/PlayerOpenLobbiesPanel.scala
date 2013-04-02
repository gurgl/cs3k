package se.bupp.cs3k.server.web.component

import contest.Events
import generic.{ResourceLinkComp, FodelPropertyColumn, AjaxLinkLabel}
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.server.model.{Competition, GameOccassion, User}
import org.apache.wicket.model.util.ListModel
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.request.resource.ResourceReference
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.GameReservationService
import org.apache.wicket.markup.repeater.data.{ListDataProvider, IDataProvider, DataView}
import org.apache.wicket.extensions.markup.html.repeater.data.table._

import org.apache.wicket.model.{PropertyModel, IModel, Model}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.event.Broadcast
import Events.CompetitionSelectedEvent
import se.bupp.cs3k.server.model.User
import org.apache.wicket.extensions.markup.html.repeater.util.{SortParam, SingleSortState}
import org.apache.wicket.extensions.markup.html.repeater.data.sort.{SortOrder, ISortState}
import se.bupp.cs3k.server.model.User
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar
import se.bupp.cs3k.server.web.application.{WicketApplication, WiaSession}
import org.apache.wicket.markup.html.basic.Label

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-10
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
class PlayerOpenLobbiesPanel(id:String,modl:IModel[User]) extends Panel(id) {


  @SpringBean
  var gameReservationService:GameReservationService = _

  var usr: User = WiaSession.get().getUser

  import scala.collection.JavaConversions.seqAsJavaList


  val isOwningUser = usr == modl.getObject


  val columns = List[IColumn[GameOccassion,String]] (
    new AbstractColumn[GameOccassion,String](new Model(""))
    {
      def populateItem(cellItem:Item[ICellPopulator[GameOccassion]], componentId:String, model:IModel[GameOccassion])
      {
        val go = model.getObject



        if (isOwningUser) {
          val parameters: PageParameters = new PageParameters()
          //parameters.add("competitor_id", selectionModel.getObject.id)
          parameters.add("user_id",  modl.getObject.id)
          parameters.add("game_occassion_id", go.id)
          val ref = new ResourceReference("bupp") {
            def getResource = WicketApplication.get.gameResource
          }


          cellItem.add(new ResourceLinkComp(componentId,ref,parameters))
        } else {
          cellItem.add(new EmptyPanel(componentId))
        }
      }
    },
    new PropertyColumn(new Model("Setup"),"game.name"),
    new PropertyColumn(new Model("Opp Type"),"competitorType"),
    new PropertyColumn(new Model("Competition"),"competitionGame.competition.name")

    //new PropertyColumn(new Model("Competitor Type"),"competitorType"),
    //new PropertyColumn(new Model("State"),"state"),

    //new PropertyColumn(new Model("Left"),"name")
  )

  /*trait SortableDataProviderTrait[T,S] extends ISortableDataProvider {
    var state:SingleSortState
    @Override
    def  getSortState() : ISortState[S] = state

    def getSort() :SortParam[S] = state.getSort()

    def setSort(param:SortParam[S]) {
      state.setSort(param);
    }

    def setSort(property:S , order:SortOrder )
    {
      state.setPropertySortOrder(property, order);
    }
    def detach() {}
  }
  */

  var challanges: ISortableDataProvider[GameOccassion,String] = new ListDataProvider(
    {
      gameReservationService.findUnplayedGamesForCompetitor( modl.getObject)
    }
    ) with ISortableDataProvider[GameOccassion,String] {
    var state:SingleSortState[String] = new SingleSortState[String]

    override def  getSortState() : ISortState[String] = state

    def getSort() :SortParam[String] = state.getSort()

    def setSort(param:SortParam[String]) {
      state.setSort(param);
    }

    def setSort(property:String , order:SortOrder )
    {
      state.setPropertySortOrder(property, order);
    }
    override def detach() {}
  }

  var table = new DataTable("table", columns, challanges, 8)
  table.addTopToolbar(new AjaxFallbackHeadersToolbar(table,challanges))
  add(table)

}
