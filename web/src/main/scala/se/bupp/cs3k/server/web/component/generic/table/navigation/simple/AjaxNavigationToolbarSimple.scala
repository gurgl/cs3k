package se.bupp.cs3k.server.web.component.generic.table.navigation.simple

import org.apache.wicket.extensions.markup.html.repeater.data.table.{AbstractToolbar, DataTable, NavigationToolbar}
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.AttributeModifier
import org.apache.wicket.model.AbstractReadOnlyModel
import se.bupp.cs3k.server.web.component.generic.table.MyAjaxPagingNavigatorSimple

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-20
 * Time: 00:44
 * To change this template use File | Settings | File Templates.
 */
class AjaxNavigationToolbarSimple(val table:DataTable[_, _]) extends AbstractToolbar(table)
{
  val serialVersionUID = 1L


  val span = new WebMarkupContainer("span");
  add(span);
  span.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel[String]()
  {

    @Override
    def getObject() =
    {
      String.valueOf(table.getColumns().size());
    }
  }));

  span.add(new MyAjaxPagingNavigatorSimple("navigator", table) {
    /** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
    override def onAjaxEvent(target: AjaxRequestTarget) {
      //super.onAjaxEvent(target)
      target.add(table);
    }
  });
  //span.add(newNavigatorLabel("navigatorLabel", table));


  /**
   * Factory method used to create the paging navigator that will be used by the datatable.
   *
   * @param navigatorId
	 *            component id the navigator should be created with
   * @param table
	 *            dataview used by datatable
   * @return paging navigator that will be used to navigate the data table
   */

  /*def newPagingNavigator(navigatorId:String ,  table:DataTable[_, _]) : MyAjaxPagingNavigatorSimple =
  {
    new MyAjaxPagingNavigatorSimple(navigatorId, table)
    {
       val serialVersionUID = 1L



      /**
       * Implement our own ajax event handling in order to update the datatable itself, as the
       * default implementation doesn't support DataViews.
       *
       * @see AjaxPagingNavigator#onAjaxEvent(org.apache.wicket.ajax.AjaxRequestTarget)
       */

      override def onAjaxEvent(target:AjaxRequestTarget )
      {
        super.onAjaxEvent(target)
        target.add(table);
      }
    }

  }
   */
  override def onConfigure()
  {
    super.onConfigure();
    setVisible(getTable().getPageCount() > 1);
  }
}
