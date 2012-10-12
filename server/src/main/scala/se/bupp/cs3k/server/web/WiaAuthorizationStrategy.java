package se.bupp.cs3k.server.web;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import se.bupp.cs3k.server.ApiPlayer;
import se.bupp.cs3k.server.web.ProtectedPage;
import se.bupp.cs3k.server.web.SigninPage;
import se.bupp.cs3k.server.web.WiaSession;

public final class WiaAuthorizationStrategy implements
    IAuthorizationStrategy,
    IUnauthorizedComponentInstantiationListener {

  public boolean isActionAuthorized(Component component, Action action) {

    if (action.equals(Component.RENDER)) {
      Class<? extends Component> c = component.getClass();
      AdminOnly adminOnly = c.getAnnotation(AdminOnly.class);
      if (adminOnly != null) {
        ApiPlayer user = WiaSession.get().getUser();
        return (user != null && user.isAdmin());
      }
    }
    return true;
  }

  public boolean isInstantiationAuthorized(Class componentClass) {

    if (ProtectedPage.class.isAssignableFrom(componentClass)) {
      return WiaSession.get().isAuthenticated();
    }

    return true;
  }

  public void onUnauthorizedInstantiation(Component component) {
    throw new RestartResponseAtInterceptPageException(
        SigninPage.class);
  }
}
