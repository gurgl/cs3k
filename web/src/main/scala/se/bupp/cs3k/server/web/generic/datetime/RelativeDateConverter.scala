package se.bupp.cs3k.server.web.generic.datetime

import java.util.Locale

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-31
 * Time: 03:28
 * To change this template use File | Settings | File Templates.
 */
class RelativeDateConverter(applyTimeZoneDifference:Boolean) extends DateConverter(applyTimeZoneDifference) {
  /**
   * @param locale
     * The locale used to convert the value
   * @return Gets the pattern that is used for printing and parsing
   */
  def getDatePattern(locale: Locale) = ???

  /**
   * @param locale
     * The locale used to convert the value
   *
   * @return formatter The formatter for the current conversion
   */
  protected def getFormat(locale: Locale) = ???
}
