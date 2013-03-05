package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.{FeedbackPanel, Panel}
import se.bupp.cs3k.server.model._
import org.apache.wicket.ajax.markup.html.AjaxLink
import se.bupp.cs3k.server.service.dao.CompetitorDao
import se.bupp.cs3k.server.service.dao.TeamDao
import se.bupp.cs3k.server.service.dao.UserDao
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{GameReservationService, TeamService, CompetitionService}
import se.bupp.cs3k.server.web.WiaSession
import se.bupp.cs3k.server.web.{WiaSession, WicketApplication}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.{Model, IModel, LoadableDetachableModel, AbstractReadOnlyModel}
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.markup.repeater.data.{DataView, IDataProvider, ListDataProvider}
import org.apache.wicket.markup.repeater.Item
import org.slf4j.LoggerFactory
import se.bupp.cs3k.server.web.auth.LoggedInOnly

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 21:25
 * To change this template use File | Settings | File Templates.
 */





