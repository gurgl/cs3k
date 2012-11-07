package se.bupp.cs3k.server.web

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.list.ListView
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.markup.repeater.data.{ListDataProvider, IDataProvider, DataView}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import org.apache.wicket.model.{IModel, LoadableDetachableModel, Model}
import org.apache.wicket.markup.html.basic.Label
import com.inmethod.grid.column.PropertyColumn
import com.inmethod.grid.datagrid.{DataGrid, DefaultDataGrid}
import com.inmethod.grid.{IGridColumn, IDataSource, DataProviderAdapter}
import org.apache.wicket.ajax.AjaxRequestTarget



/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class LadderPanel(id:String) extends Panel(id) {

  @SpringBean
  var ladderDao:LadderDao = _


  val provider = new IDataProvider[Ladder]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = ladderDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = ladderDao.selectRangeCount

    def model(p1: Ladder) = new LoadableDetachableModel[Ladder](p1) {
      def load() = ladderDao.find(p1.id).get

    }

    def detach() {}
  }
  /*
    add(new DataView[Ladder]("list",provider) {
    def populateItem(p1: Item[Ladder]) {
      p1.add(new Label("item",p1.getModelObject.name))

    }
  })*/

  var selectionLabel:Label = _
  val listDataProvider = provider

  val cols = List[IGridColumn[IDataSource[Ladder], Ladder, String]](
    new PropertyColumn(new Model("id"), "id"),
    new PropertyColumn(new Model("Name"), "name"))
  import scala.collection.JavaConversions.seqAsJavaList
  val grid = new DefaultDataGrid[IDataSource[Ladder], Ladder, String]("grid", new DataProviderAdapter[Ladder,String](listDataProvider), seqAsJavaList(cols)) {

    override def onItemSelectionChanged(item:IModel[Ladder] , newValue:Boolean)
    {
      super.onItemSelectionChanged(item, newValue);

      // when item selection changes the label showing selected items needs to be
      // refreshed
      val target = getRequestCycle().find(classOf[AjaxRequestTarget]);
      target.add(selectionLabel);
    }
  }
  grid.setCleanSelectionOnPageChange(false);
  grid.setClickRowToSelect(true);
  add(grid);



  // model for label that shows selected items
  val  selectedItemsModel = new Model[String]()
  {
    //final long serialVersionUID = 1L;

    @Override
    override def getObject() =
    {
      selectedItemsAsString(grid);
    }
  };
  import scala.collection.JavaConversions.collectionAsScalaIterable
  def selectedItemsAsString( grid:DataGrid[IDataSource[Ladder], Ladder, String]) =
  {
    var res = new java.lang.StringBuilder();
    val selected = grid.getSelectedItems();
    selected.foreach ( model =>
    {
      var contact = model.getObject();
      if (res.length > 0)
      {
        res.append(", ");
      }
      if (contact != null) {
      res.append(contact.name);
      res.append(" ");
      res.append(contact.id);
      }
    })
    res.toString();
  }
  selectionLabel = new Label("currentSelection", selectedItemsModel)
  add(selectionLabel);
  selectionLabel.setOutputMarkupId(true);

}
