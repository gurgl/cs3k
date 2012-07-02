/**
 *
 */
package com.fsdev.scw

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.markup.html.panel.FeedbackPanel
import org.slf4j.LoggerFactory
import org.apache.wicket.markup.html.form.Form
import org.slf4j.Logger

/**
 * @author kjozsa
 */
class TestModel extends Serializable {
  var label: String = ""

  override def toString = label
}

class FormTestPage extends WebPage  {
  val testModel = new TestModel

  val form = new Form("form") {
    override def onSubmit() {
      info("submitted model: {}", testModel)
    }
  }
  add(form)

  //form.add(new TextField("name", testModel.label))

  add(new FeedbackPanel("feedback"))
}
