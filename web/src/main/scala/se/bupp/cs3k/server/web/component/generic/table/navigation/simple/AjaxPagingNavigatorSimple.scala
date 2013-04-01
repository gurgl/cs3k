package se.bupp.cs3k.server.web.component.generic.table.navigation.simple

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.wicket.{MarkupContainer, Component}
import org.apache.wicket.behavior.Behavior
import org.apache.wicket.markup.ComponentTag
import org.apache.wicket.markup.html.link.AbstractLink
import org.apache.wicket.markup.html.navigation.paging._
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.repeater.AbstractRepeater
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes
import org.apache.wicket.ajax.markup.html.navigation.paging

/**
 * A Wicket panel component to draw and maintain a complete page navigator, meant to be easily added
 * to any PageableListView. A navigation which contains links to the first and last page, the
 * current page +- some increment and which supports paged navigation bars (@see
 * PageableListViewNavigationWithMargin).
 *
 * @author Juergen Donnerstag
 */
object AjaxPagingNavigatorSimple {
  private final val serialVersionUID: Long = 1L


}

object Buppa {

}



class AjaxPagingNavigatorSimple(id: String, var pageable: IPageable, var labelProvider: IPagingLabelProvider) extends Panel(id) {
  setOutputMarkupId(true)
  /**
   * Constructor.
   *
   * @param id
     * See Component
   * @param pageable
     * The pageable component the page links are referring to.
   */
  def this(id: String, pageable: IPageable) =  {
    this(id, pageable, null)
  }

  /**
   * {@link IPageable} this navigator is linked with
   *
   * @return { @link IPageable} instance
   */
  final def getPageable: IPageable = {
    return pageable
  }

  protected override def onInitialize {
    super.onInitialize
    /*pagingNavigation = newNavigation("navigation", pageable, labelProvider)
    add(pagingNavigation)*/
    add(newPagingNavigationLink("first", pageable, 0).add(new TitleAppender("PagingNavigator.first")))
    add(newPagingNavigationIncrementLink("prev", pageable, -1).add(new TitleAppender("PagingNavigator.previous")))
    add(newPagingNavigationIncrementLink("next", pageable, 1).add(new TitleAppender("PagingNavigator.next")))
    add(newPagingNavigationLink("last", pageable, -1).add(new TitleAppender("PagingNavigator.last")))
  }

  /**
   * Create a new increment link. May be subclassed to make use of specialized links, e.g. Ajaxian
   * links.
   *
   * @param id
     * the link id
   * @param pageable
     * the pageable to control
   * @param increment
     * the increment
   * @return the increment link
   */
  protected def newPagingNavigationIncrementLink(id: String, pageable: IPageable, increment: Int): AbstractLink = {
    return new AjaxPagingNavigationIncrementLink(id, pageable, increment)
  }

  /**
   * Create a new pagenumber link. May be subclassed to make use of specialized links, e.g.
   * Ajaxian links.
   *
   * @param id
     * the link id
   * @param pageable
     * the pageable to control
   * @param pageNumber
     * the page to jump to
   * @return the pagenumber link
   */
  protected def newPagingNavigationLink(id: String, pageable: IPageable, pageNumber: Int): AbstractLink = {
    return new AjaxPagingNavigationLink(id, pageable, pageNumber)
  }


  /**
   * Gets the pageable navigation component for configuration purposes.
   *
   * @return the associated pageable navigation.
   */
  /*final def getPagingNavigation: PagingNavigation = {
    return pagingNavigation
  }*/

  /** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
  //private var pagingNavigation: PagingNavigation = null


  def onAjaxEvent( target:AjaxRequestTarget)
  {
    // update the container (parent) of the pageable, this assumes that
    // the pageable is a component, and that it is a child of a web
    // markup container.

    var container = (pageable.asInstanceOf[Component]);
    // no need for a nullcheck as there is bound to be a non-repeater
    // somewhere higher in the hierarchy
    while (container.isInstanceOf[AbstractRepeater])
    {
      container = container.getParent();
    }
    target.add(container);

    // in case the navigator is not contained by the container, we have
    // to add it to the response
    if ((container.asInstanceOf[MarkupContainer]).contains(this, true) == false)
    {
      target.add(this);
    }
  }

  private final object TitleAppender {
    private final val serialVersionUID: Long = 1L
  }

  class TitleAppender(val resourceKey: String) extends Behavior {

    /** {@inheritDoc} */
    override def onComponentTag(component: Component, tag: ComponentTag) {
      tag.put("title", AjaxPagingNavigatorSimple.this.getString(resourceKey))
    }
  }
}