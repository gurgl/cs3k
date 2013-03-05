package se.bupp.cs3k.server.web.page

import se.bupp.cs3k.server.web.auth.{AnonymousOnly, LoggedInOnly}
import org.apache.wicket.markup.html.link.BookmarkablePageLink

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-11
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
class SessionPage extends AbstractBasePage {

  @LoggedInOnly class LogoutLink extends BookmarkablePageLink("logout", classOf[SignOutPage])
  @AnonymousOnly class LoginLink extends BookmarkablePageLink("login", classOf[SigninPage])
  @AnonymousOnly class RegisterLink extends BookmarkablePageLink("register", classOf[RegisterPage])
  add(new LogoutLink)
  add(new LoginLink)
  add(new RegisterLink)

  @LoggedInOnly
  class ProfileLink extends BookmarkablePageLink("profile", classOf[ProfilePage])

  add(new ProfileLink)

}
