package se.bupp.cs3k.server.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class ProtectedPage extends WebPage {

  public ProtectedPage() {
  }

  public ProtectedPage(IModel model) {
    super(model);
  }


}
