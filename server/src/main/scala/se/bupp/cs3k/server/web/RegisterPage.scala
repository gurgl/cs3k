package se.bupp.cs3k.server.web

import org.apache.wicket.model.{Model, CompoundPropertyModel}
import org.apache.wicket.markup.html.form.{Form, PasswordTextField, TextField}
import org.apache.wicket.markup.html.WebPage
import se.bupp.cs3k.server.ApiPlayer
import org.apache.wicket.markup.html.panel.FeedbackPanel
import org.apache.wicket.spring.injection.annot.SpringBean

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-12
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
class RegisterPage extends AbstractBasePage {

  @SpringBean(name = "mySBean") var beanan: MyBean = _
  add(new RegisterForm("registerForm"))

  add(new FeedbackPanel("feedback"))

  var confirm:String = ""

  class RegisterForm(id:String) extends Form[ApiPlayer](id) {

    var user = new ApiPlayer()


    var confirmModel = new Model[String](confirm)
    setModel(new CompoundPropertyModel(user))

    add(new TextField("username"))
    add(new TextField("email"))
    add(new PasswordTextField("password"))
    add(new PasswordTextField("wiaPasswordConfirm"))

    override def onSubmit() {
      super.onSubmit()
      println("confirm " + confirm  + " user.password " + user.password + ", " + user.wiaPasswordConfirm)
      if (user.wiaPasswordConfirm == user.password) {

        beanan.insert(user)
        setResponsePage(getApplication.getHomePage)
      }
    }
  }

}