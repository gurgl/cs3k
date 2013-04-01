package se.bupp.cs3k.server.web.generic.datetime

import java.util.{TimeZone, Locale}
import org.joda.time._
import org.joda.time.format.DateTimeFormatter
import concurrent.duration.Duration
import scala.Predef.format

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-31
 * Time: 03:28
 * To change this template use File | Settings | File Templates.
 */

object RelativeDateConverter {
  val r0to59seconds = Seconds.seconds(59).toStandardDuration
  val r1to59minutes = Minutes.minutes(59).toStandardDuration
  val r1to23hours = Hours.hours(23).toStandardDuration
  val r1to7days = Days.days(7).toStandardDuration
  val r1to5weeks = Weeks.weeks(5).toStandardDuration

}

class RelativeDateConverter(applyTimeZoneDifference:Boolean) extends StyleDateConverter(applyTimeZoneDifference) {
  import RelativeDateConverter._


  override def convertToString(then: Instant, locale: Locale): String = {
    val interval = new Interval(then, new Instant())

    interval.toDuration match {
      case i if r0to59seconds.isLongerThan(i) => i.getStandardSeconds + " seconds ago"
      case i if r1to59minutes.isLongerThan(i) => i.getStandardMinutes + " minutes ago"
      case i if r1to23hours.isLongerThan(i) => i.getStandardHours + " hours ago"
      case i if DateMidnight.now().minusDays(1).isBefore(interval.getStart) => "yesterday"
      case i if r1to7days.isLongerThan(i) => i.getStandardDays  + " days ago"
      case i if r1to5weeks.isLongerThan(i) => i.toPeriodFrom(then).getWeeks  + " weeks ago"
      case i =>
        val dt: DateTime = new DateTime(then.getMillis, getTimeZone)
        var format: DateTimeFormatter = getFormat(locale)
        if (applyTimeZoneDifference) {
          val zone: TimeZone = getClientTimeZone
          if (zone != null) {
            format = format.withZone(DateTimeZone.forTimeZone(zone))
          }
        }
        format.print(dt)

    }



  }
}
