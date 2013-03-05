package se.bupp.cs3k.server.web.component.generic.table

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator
import org.apache.wicket.markup.html.navigation.paging.IPageable

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-20
 * Time: 01:18
 * To change this template use File | Settings | File Templates.
 */
class MyAjaxPagingNavigator(id:String, pagable:IPageable) extends AjaxPagingNavigator(id,pagable) {

}
