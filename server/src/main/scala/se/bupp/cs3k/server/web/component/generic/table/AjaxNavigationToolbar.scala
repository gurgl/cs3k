package se.bupp.cs3k.server.web.component.generic.table

import org.apache.wicket.extensions.markup.html.repeater.data.table.{DataTable, NavigationToolbar}
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator
import org.apache.wicket.ajax.AjaxRequestTarget

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-20
 * Time: 00:44
 * To change this template use File | Settings | File Templates.
 */
class AjaxNavigationToolbar(val table:DataTable[_, _]) extends NavigationToolbar(table)
{
  val serialVersionUID = 1L




  /**
   * Factory method used to create the paging navigator that will be used by the datatable.
   *
   * @param navigatorId
	 *            component id the navigator should be created with
   * @param table
	 *            dataview used by datatable
   * @return paging navigator that will be used to navigate the data table
   */

  override def newPagingNavigator(navigatorId:String ,  table:DataTable[_, _]) : PagingNavigator =
  {
    new MyAjaxPagingNavigator(navigatorId, table)
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
        target.add(table);
      }
    };
  }
}
