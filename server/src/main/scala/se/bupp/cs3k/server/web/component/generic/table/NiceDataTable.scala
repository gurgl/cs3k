package se.bupp.cs3k.server.web.component.generic.table

import org.apache.wicket.extensions.markup.html.repeater.data.table.{NoRecordsToolbar, DataTable, ISortableDataProvider, IColumn}
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.{AjaxFallbackHeadersToolbar}
import org.apache.wicket.model.IModel
import org.apache.wicket.markup.repeater.{OddEvenItem, Item}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-20
 * Time: 00:35
 * To change this template use File | Settings | File Templates.
 */
import scala.collection.JavaConversions.seqAsJavaList
class NiceDataTable[T, S](id:String ,  columns:List[_ <: IColumn[T, S]],  dataProvider:ISortableDataProvider[T, S], rowsPerPage:Int)
  extends DataTable(id, columns, dataProvider, rowsPerPage) {

  setOutputMarkupId(true);
  setVersioned(false);
  addTopToolbar(new AjaxNavigationToolbar(this));
  addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider));
  addBottomToolbar(new NoRecordsToolbar(this));


  override def newRowItem(id:String, index:Int,  model:IModel[T]) : Item[T] = {
    new OddEvenItem[T](id, index, model)
  }


}
