package se.bupp.cs3k.server.web.application;

import java.util.Locale;


import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import se.bupp.cs3k.server.model.User;

public class WiaSession extends WebSession {

  public static WiaSession get() {
    return (WiaSession) Session.get();
  }

  private User user;

  public WiaSession(Request request) {
    super(request);
    setLocale(Locale.ENGLISH);
  }

  public synchronized User getUser() {
    return user;
  }

  public synchronized boolean isAuthenticated() {
    return (user != null);
  }

  public synchronized void setUser(User user) {
    this.user = user;
    dirty();
  }
}
