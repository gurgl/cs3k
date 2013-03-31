package se.bupp.cs3k.server.web.component.generic

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.wicket.markup.ComponentTag
import org.apache.wicket.markup.MarkupStream
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.IModel
import org.apache.wicket.util.convert.IConverter
import org.joda.time.{Instant, DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import se.bupp.cs3k.server.web.generic.datetime.{DateConverter, StyleDateConverter}

/**
 * A label that is mapped to a <code>java.util.Date</code> object and that uses Joda time to format
 * values.
 * <p>
 * You can provide a date pattern in two of the constructors. When not provided,
 * {@link DateTimeFormat#shortDate()} will be used.
 * </p>
 * <p>
 * A special option is applyTimeZoneDifference which is an option that says whether to correct for
 * the difference between the client's time zone and server's time zone. This is true by default.
 * </p>
 *
 * @see DateTime
 * @see DateTimeFormat
 * @see DateTimeZone
 *
 * @author eelcohillenius
 */
object DateLabel {
  /**
   * Creates a new DateLabel defaulting to using a short date pattern
   *
   * @param id
     * The id of the text field
   * @param model
     * The model
   * @param datePattern
     * The pattern to use. Must be not null. See { @link SimpleDateFormat} for available
   *                                                     patterns.
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  /*def forDatePattern(id: Nothing, model: IModel[Date], datePattern: Nothing): DateLabel = {
    return new DateLabel(id, model, new PatternDateConverter(datePattern, true))
  }*/

  /**
   * Creates a new DateLabel defaulting to using a short date pattern
   *
   * @param id
     * The id of the text field
   * @param datePattern
     * The pattern to use. Must be not null. See { @link SimpleDateFormat} for available
   *                                                     patterns.
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  /*def forDatePattern(id: Nothing, datePattern: Nothing): DateLabel = {
    return forDatePattern(id, null, datePattern)
  }*/

  /**
   * Creates a new DateLabel defaulting to using a short date pattern
   *
   * @param id
     * The id of the text field
   * @param model
     * The model
   * @param dateStyle
     * style to use in case no pattern is provided. Must be two characters from the set
   *   {"S", "M", "L", "F", "-"}. Must be not null. See
   *   { @link DateTimeFormat#forStyle(String)} for options.
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  def forDateStyle(id: String, model: IModel[Instant], dateStyle: String): DateLabel = {
    return new DateLabel(id, model, new StyleDateConverter(dateStyle, true))
  }

  /**
   * Creates a new DateLabel defaulting to using a short date pattern
   *
   * @param id
     * The id of the text field
   * @param dateStyle
     * style to use in case no pattern is provided. Must be two characters from the set
   *   {"S", "M", "L", "F", "-"}. Must be not null. See
   *   { @link DateTimeFormat#forStyle(String)} for options.
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  def forDateStyle(id: String, dateStyle: String): DateLabel = {
    forDateStyle(id, null, dateStyle)
  }

  /**
   * Creates a new DateLabel defaulting to using a short date pattern
   *
   * @param id
     * The id of the text field
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  def forShortStyle(id: String): DateLabel = {
    forShortStyle(id, null)
  }

  /**
   * Creates a new DateLabel defaulting to using a short date pattern
   *
   * @param id
     * The id of the text field
   * @param model
     * The model
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  def forShortStyle(id: String, model: IModel[Instant]): DateLabel = {
    return new DateLabel(id, model, new StyleDateConverter(true))
  }

  /**
   * Creates a new DateLabel using the provided converter.
   *
   * @param id
     * The id of the text field
   * @param converter
     * the date converter
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  def withConverter(id: String, converter: DateConverter): DateLabel = {
    withConverter(id, null, converter)
  }

  /**
   * Creates a new DateLabel using the provided converter.
   *
   * @param id
     * The id of the text field
   * @param model
     * The model
   * @param converter
     * the date converter
   * @return new instance
   *
   * @see org.apache.wicket.markup.html.form.TextField
   */
  def withConverter(id: String, model: IModel[Instant], converter: DateConverter): DateLabel = {
    return new DateLabel(id, model, converter)
  }

  private final val serialVersionUID: Long = 1L
}

class DateLabel(id: String, model: IModel[Instant], var converter: DateConverter) extends Label(id , model ) {


  /**
   * @return after append to label or null
   */
  def getAfter: String = {
    return after
  }

  /**
   * @return before prepend to label or null
   */
  def getBefore: String = {
    return before
  }

  /**
   * Returns the specialized converter.
   */
  override def getConverter[C](clazz: Class[C]): IConverter[C] = {
    if (classOf[Instant].isAssignableFrom(clazz)) {
      @SuppressWarnings(Array("unchecked"))
      val result: IConverter[C] = converter.asInstanceOf[IConverter[C]]
      return result
    }
    else {
      return super.getConverter(clazz)
    }
  }

  /**
   * @param after
     * append to label
   */
  def setAfter(after: Nothing) {
    this.after = after
  }

  /**
   * @param before
     * prepend to label
   */
  def setBefore(before: Nothing) {
    this.before = before
  }

  /**
   * {@inheritDoc}
   */
  @Override override def onComponentTagBody(markupStream: MarkupStream, openTag: ComponentTag) {
    var s: String = getDefaultModelObjectAsString
    if (before != null) {
      s = before + s
    }
    if (after != null) {
      s = s + after
    }
    replaceComponentTagBody(markupStream, openTag, s)
  }

  /** optionally prepend to label. */
  private var after: String = null
  /** optionally append to label. */
  private var before: String = null
  /**
   * The converter for the Label
   */

}