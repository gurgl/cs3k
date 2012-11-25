package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.repeater.data.{IDataProvider, DataView}
import se.bupp.cs3k.server.model.{Team, Competitor, Ladder, TotalAwaredPointsAndScore}
import org.apache.wicket.markup.repeater.Item
import com.inmethod.grid.datagrid.{DefaultDataGrid, DataGrid}
import com.inmethod.grid.{DataProviderAdapter, IDataSource, IGridColumn}
import java.{lang, util}
import org.apache.wicket.model.{LoadableDetachableModel, IModel, Model}
import com.inmethod.grid.column.PropertyColumn
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitorDao, LadderDao}
import se.bupp.cs3k.example.ExampleScoreScheme._

import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal
import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal.Render
import se.bupp.cs3k.api.score.{CompetitorScore, ContestScore, ScoreScheme}
import com.fasterxml.jackson.annotation.JsonTypeInfo
import reflect.BeanProperty




class LadderRankingView(id:String) extends Panel(id) {


  @SpringBean
  var ladderDao:CompetitorDao = _

  val a = List[TotalAwaredPointsAndScore](
    new TotalAwaredPointsAndScore(new Team("Tjena"), 10, new ExCompetitorTotal(10,5,2)),
    new TotalAwaredPointsAndScore(new Team("Fena"), 4, new ExCompetitorTotal(6,2,2)),
    new TotalAwaredPointsAndScore(new Team("Mitt"), 3, new ExCompetitorTotal(2,0,2)),
    new TotalAwaredPointsAndScore(new Team("Bena"), 0, new ExCompetitorTotal(1,-8,2))
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
    def populateItem(attributes: Item[Ladder]) {
      attributes.add(new Label("item",attributes.getModelObject.name))

    }
  })*/

  var selectionLabel:Label = _
  val listDataProvider = provider


  val sc = ExScoreScheme

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
