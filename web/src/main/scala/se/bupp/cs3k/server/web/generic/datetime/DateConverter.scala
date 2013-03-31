package se.bupp.cs3k.server.web.generic.datetime

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

import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.apache.wicket.Session
import org.apache.wicket.core.request.ClientInfo
import org.apache.wicket.protocol.http.request.WebClientInfo
import org.apache.wicket.util.convert.ConversionException
import org.apache.wicket.util.convert.IConverter
import org.apache.wicket.util.string.Strings
import org.joda.time.{Instant, DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormatter

/**
 * Base class for Joda Time based date converters. It contains the logic to parse and format,
 * optionally taking the time zone difference between clients and the server into account.
 * <p>
 * Converters of this class are best suited for per-component use.
 * </p>
 *
 * @author eelcohillenius
 */
object DateConverter {
  private final val serialVersionUID: Long = 1L
}

abstract class DateConverter(val applyTimeZoneDifference: Boolean)  extends IConverter[Instant] {

  def convertToObject(value: String, locale: Locale): Instant = {
    if (Strings.isEmpty(value)) {
      return null
    }
    var format: DateTimeFormatter = getFormat(locale)
    if (format == null) {
      throw new IllegalStateException("format must be not null")
    }
    if (applyTimeZoneDifference) {
      val zone: TimeZone = getClientTimeZone
      var dateTime: DateTime = null
      format = format.withZone(getTimeZone)
      try {
        dateTime = format.parseDateTime(value)
      }
      catch {
        case e: RuntimeException => {
          throw newConversionException(e, locale)
        }
      }
      if (zone != null) {
        dateTime = dateTime.withZoneRetainFields(DateTimeZone.forTimeZone(zone))
      }
      return dateTime.toInstant
    }
    else {
      try {
        val date: DateTime = format.parseDateTime(value)
        return date.toInstant
      }
      catch {
        case e: RuntimeException => {
          throw newConversionException(e, locale)
        }
      }
    }
  }

  /**
   * Creates a ConversionException and sets additional context information to it.
   *
   * @param cause
     * - { @link RuntimeException} cause
   * @param locale
     * - { @link Locale} used to set 'format' variable with localized pattern
   * @return { @link ConversionException}
   */
  private def newConversionException(cause: RuntimeException, locale: Locale): ConversionException = {
    return new ConversionException(cause).setVariable("format", getDatePattern(locale))
  }

  /**
   * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
   *      java.util.Locale)
   */
  def convertToString(value: Instant, locale: Locale): String = {
    val dt: DateTime = new DateTime(value.getMillis, getTimeZone)
    var format: DateTimeFormatter = getFormat(locale)
    if (applyTimeZoneDifference) {
      val zone: TimeZone = getClientTimeZone
      if (zone != null) {
        format = format.withZone(DateTimeZone.forTimeZone(zone))
      }
    }
    return format.print(dt)
  }

  /**
   * Gets whether to apply the time zone difference when interpreting dates.
   *
   * </p> When true, the current time is applied on the parsed date, and the date will be
   * corrected for the time zone difference between the server and the client. For instance, if
   * I'm in Seattle and the server I'm working on is in Amsterdam, the server is 9 hours ahead.
   * So, if I'm inputting say 12/24 at a couple of hours before midnight, at the server it is
   * already 12/25. If this boolean is true, it will be transformed to 12/25, while the client
   * sees 12/24. </p>
   *
   * @return whether to apply the difference in time zones between client and server
   */
  final def getApplyTimeZoneDifference: Boolean = {
    return applyTimeZoneDifference
  }

  /**
   * @param locale
     * The locale used to convert the value
   * @return Gets the pattern that is used for printing and parsing
   */
  def getDatePattern(locale: Locale): String

  /**
   * Gets the client's time zone.
   *
   * @return The client's time zone or null
   */
  protected def getClientTimeZone: TimeZone = {
    val info: ClientInfo = Session.get.getClientInfo
    if (info.isInstanceOf[WebClientInfo]) {
      return (info.asInstanceOf[WebClientInfo]).getProperties.getTimeZone
    }
    return null
  }

  /**
   * @param locale
     * The locale used to convert the value
   *
   * @return formatter The formatter for the current conversion
   */
  protected def getFormat(locale: Locale): DateTimeFormatter

  /**
   * Gets the server time zone. Override this method if you want to fix to a certain time zone,
   * regardless of what actual time zone the server is in.
   *
   * @return The server time zone
   */
  protected def getTimeZone: DateTimeZone = {
    return DateTimeZone.getDefault
  }

  /**
   * Whether to apply the time zone difference when interpreting dates.
   */
 //private final val applyTimeZoneDifference: Boolean = false
}