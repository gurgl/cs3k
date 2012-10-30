package se.bupp.cs3k.server.web;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import org.apache.wicket.spring.injection.annot.SpringBean;
import se.bupp.cs3k.server.User;


public class SigninPage extends AbstractBasePage {




    private static class SignInForm extends StatelessForm {

        @SpringBean(name="mySBean")
        MyBean beanan;

        private String wiaPassword;

        private String wiaUsername;

        public SignInForm(final String id) {
          super(id);
          setModel(new CompoundPropertyModel(this));
          add(new TextField("wiaUsername"));
          add(new PasswordTextField("wiaPassword"));
        }

        public String getWiaPassword() {
          return wiaPassword;
        }

        public String getWiaUsername() {
          return wiaUsername;
        }

        @Override
        public final void onSubmit() {
          if (signIn(wiaUsername, wiaPassword)) {
            //if (!continueToOriginalDestination()) {
              setResponsePage(getApplication().getHomePage());
            //}
          } else {
            error("Unknown username/ password");
          }
        }

        public void setWiaPassword(String password) {
          this.wiaPassword = password;
        }

        public void setWiaUsername(String username) {
          this.wiaUsername = username;
        }

        private boolean signIn(String username, String password) {
          if (username != null && password != null) {
            User user = beanan.findUser(username);

              if(user == null) {
                User apiPlayer = new User("admin");
                  apiPlayer.password_$eq("admin");
                beanan.insert(apiPlayer);
                  user = apiPlayer;
              }
            if (user != null) {
              if (user.password().equals(password)) {
                WiaSession.get().setUser(user);
                return true;
              }
            }
          }
          return false;
        }
      }

  public SigninPage() {

    add(new SignInForm("signInForm"));
    add(new FeedbackPanel("feedback"));
  }
}
