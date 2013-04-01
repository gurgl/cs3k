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

import se.bupp.cs3k.server.web.application.WicketApplication
import de.agilecoders.wicket.Bootstrap
import de.agilecoders.wicket.markup.html.themes.bootswatch.BootswatchTheme
import de.agilecoders.wicket.settings.DefaultThemeProvider

class AbstractBasePage( pars:PageParameters) extends WebPage(pars) {
  def this() = this(null)


  override def onBeforeRender() {
    WicketApplication.get.getMarkupSettings.setStripWicketTags(true)

    var settings = Bootstrap.getSettings(WicketApplication.get);
    var themes = settings.getThemeProvider().available();

    import scala.collection.JavaConversions.asScalaBuffer
    themes.foreach(println)
    //var settings: IBootstrapSettings = Bootstrap.getSettings(this)
    settings.getActiveThemeProvider().setActiveTheme(themes(0));

    super.onBeforeRender()
  }

  override def renderHead(response:IHeaderResponse )
  {

    Bootstrap.renderHead(response)
    WicketApplication.get.getMarkupSettings.setStripWicketTags(WicketApplication.get.mode)
  }

}
