package se.bupp.cs3k.server.web.generic.datetime

import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import java.util.Locale

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-31
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */
class StyleDateConverter(val dateStyle:String,applyTimeZoneDifference :Boolean) extends DateConverter(applyTimeZoneDifference) {

  def this(b:Boolean) = this("S-",b)

  override def getDatePattern( locale:Locale) =
  {
   DateTimeFormat.patternForStyle(dateStyle, locale);
  }

  /**
   * @return formatter The formatter for the current conversion
   */
  @Override
  override def getFormat(locale:Locale ) : DateTimeFormatter =
  {
    DateTimeFormat.forPattern(getDatePattern(locale))
      .withLocale(locale)
      .withPivotYear(2000);
  }

}
