package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.repeater.data.{IDataProvider, DataView}
import se.bupp.cs3k.server.model.{Team, Competitor, Ladder, TotalAwaredPointsAndScore}
import org.apache.wicket.markup.repeater.Item
import com.inmethod.grid.datagrid.{DefaultDataGrid, DataGrid}
import com.inmethod.grid.{DataProviderAdapter, IDataSource, IGridColumn}
import java.util
import org.apache.wicket.model.{LoadableDetachableModel, IModel, Model}
import com.inmethod.grid.column.PropertyColumn
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitorDao, LadderDao}
import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal
import se.bupp.cs3k.server.web.component.LadderRankingView.{MyScoreScheme, MyCompetitorTotal}
import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal.Render
import se.bupp.cs3k.api.score.ScoreScheme

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-15
 * Time: 00:44
 * To change this template use File | Settings | File Templates.
 */

object LadderRankingView {

  class MyScoreScheme extends ScoreScheme {
    def competitorTotalColHeaders() = Array("Kills", "Diff Kills", "Trophys")
  }

  class MyCompetitorTotal(val kills:Int, val diffKills:Int, val trophys:Int) extends CompetitorTotal {
    def getRenderer = new Render {

      def render() = Array(kills.toString,diffKills.toString, trophys.toString)
    }
  }




}

class LadderRankingView(id:String) extends Panel(id) {



  @SpringBean
  var ladderDao:CompetitorDao = _

  val a = List[TotalAwaredPointsAndScore](
    new TotalAwaredPointsAndScore(new Team("Tjena"), 10, new MyCompetitorTotal(10,5,2)),
    new TotalAwaredPointsAndScore(new Team("Fena"), 4, new MyCompetitorTotal(6,2,2)),
    new TotalAwaredPointsAndScore(new Team("Mitt"), 3, new MyCompetitorTotal(2,0,2)),
    new TotalAwaredPointsAndScore(new Team("Bena"), 0, new MyCompetitorTotal(1,-8,2))
  )



  val provider = new IDataProvider[TotalAwaredPointsAndScore]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = a.toIterator

    def size() = a.size

    def model(p1: TotalAwaredPointsAndScore) = new Model(p1)

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


  val sc = new MyScoreScheme

  //import scala.collection.JavaConversions.

  val gameSpecificCols:List[IGridColumn[IDataSource[TotalAwaredPointsAndScore], TotalAwaredPointsAndScore, String]] =
    sc.competitorTotalColHeaders.toSeq.zipWithIndex.toList.map {
      case (h,i) => new PropertyColumn[IDataSource[TotalAwaredPointsAndScore], TotalAwaredPointsAndScore, String, String](new Model(h), "elements."+ i +"")
    }

  val cols = List[IGridColumn[IDataSource[TotalAwaredPointsAndScore], TotalAwaredPointsAndScore, String]](
    new PropertyColumn(new Model("Team"), "competitor.name"),
    new PropertyColumn(new Model("Score"), "points")
  ).++(gameSpecificCols)


  cols.map( p => p.asInstanceOf[PropertyColumn[_,_,_,_]].getPropertyExpression).foreach(println)



  import scala.collection.JavaConversions.seqAsJavaList
  val grid = new DefaultDataGrid[IDataSource[TotalAwaredPointsAndScore], TotalAwaredPointsAndScore, String]("grid", new DataProviderAdapter[TotalAwaredPointsAndScore,String](listDataProvider), seqAsJavaList(cols)) {

    override def onItemSelectionChanged(item:IModel[TotalAwaredPointsAndScore] , newValue:Boolean)
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
  def selectedItemsAsString( grid:DataGrid[IDataSource[TotalAwaredPointsAndScore], TotalAwaredPointsAndScore, String]) =
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
      /*if (contact != null) {
        res.append(contact.name);
        res.append(" ");
        res.append(contact.id);
      }*/
    })
    res.toString();
  }
  selectionLabel = new Label("currentSelection", selectedItemsModel)
  add(selectionLabel);
  selectionLabel.setOutputMarkupId(true);


}
