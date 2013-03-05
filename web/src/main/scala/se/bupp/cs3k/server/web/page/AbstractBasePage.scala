package se.bupp.cs3k.server.web.page

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.bootstrap.Bootstrap
import se.bupp.cs3k.server.web.WicketApplication

class AbstractBasePage( pars:PageParameters) extends WebPage(pars) {
  def this() = this(null)


  override def onBeforeRender() {
    WicketApplication.get.getMarkupSettings.setStripWicketTags(true)
    super.onBeforeRender()
  }

  override def renderHead(response:IHeaderResponse )
  {
    Bootstrap.renderHeadResponsive(response)
    WicketApplication.get.getMarkupSettings.setStripWicketTags(WicketApplication.get.mode)
  }

}
