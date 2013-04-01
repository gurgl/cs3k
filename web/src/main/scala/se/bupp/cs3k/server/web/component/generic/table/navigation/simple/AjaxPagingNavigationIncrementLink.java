package se.bupp.cs3k.server.web.component.generic.table.navigation.simple;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-04-02
 * Time: 00:19
 * To change this template use File | Settings | File Templates.
 */
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


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;

/**
 * An incremental Ajaxian link to a page of a PageableListView. Assuming your list view navigation
 * looks like
 *
 * <pre>
 *
 *              	 [first / &lt;&lt; / &lt;] 1 | 2 | 3 [&gt; / &gt;&gt; /last]
 *
 * </pre>
 *
 * <p>
 * and "&lt;" meaning the previous and "&lt;&lt;" goto the "current page - 5", than it is this kind
 * of incremental page links which can easily be created.
 *
 * This link will update the pageable and itself or the navigator the link is part of using Ajax
 * techniques, or perform a full refresh when ajax is not available.
 *
 * @since 1.2
 *
 * @author Martijn Dashorst
 */
public class AjaxPagingNavigationIncrementLink extends PagingNavigationIncrementLink<Void>
        implements
        IAjaxLink
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param id
     *            See Component
     * @param pageable
     *            The pageable component the page links are referring to
     * @param increment
     *            increment by
     */
    public AjaxPagingNavigationIncrementLink(final String id, final IPageable pageable,
                                             final int increment)
    {
        super(id, pageable, increment);

        setOutputMarkupId(true);
    }

    @Override
    protected void onInitialize()
    {
        super.onInitialize();
        add(newAjaxPagingNavigationBehavior(pageable, "click"));
    }

    /**
     * @param pageable
     *            The pageable component the page links are referring to
     * @param event
     *            the name of the default event on which this link will listen to
     * @return the ajax behavior which will be executed when the user clicks the link
     */
    protected AjaxPagingNavigationBehavior newAjaxPagingNavigationBehavior(IPageable pageable,
                                                                           String event)
    {
        return new AjaxPagingNavigationBehavior(this, pageable, event)
        {
            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
            {
                super.updateAjaxAttributes(attributes);
                AjaxPagingNavigationIncrementLink.this.updateAjaxAttributes(attributes);
            }
        };
    }

    protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
    {
    }

    /**
     * Returns the javascript event handler for this component. This function is used to decorate
     * the generated javascript handler.
     * <p>
     * NOTE: It is recommended that you only prepend additional javascript to the default handler
     * because the default handler uses the return func() format so any appended javascript will not
     * be evaluated by default.
     *
     * @param defaultHandler
     *            default javascript event handler generated by this link
     * @return javascript event handler for this link
     * @deprecated This method is not used since Wicket 6.0.0
     */
    @Deprecated
    protected String getEventHandler(String defaultHandler)
    {
        return defaultHandler;
    }


    /**
     * Fallback event listener, will redisplay the current page.
     *
     * @see org.apache.wicket.markup.html.link.Link#onClick()
     */
    @Override
    public void onClick()
    {
        onClick(null);
    }

    /**
     * Performs the actual action of this component, performing a non-ajax fallback when there was
     * no AjaxRequestTarget available.
     *
     * @param target
     *            the request target, when <code>null</code>, a full page refresh will be generated
     */
    @Override
    public void onClick(AjaxRequestTarget target)
    {
        // Tell the PageableListView which page to print next
        pageable.setCurrentPage(getPageNumber());
    }
}