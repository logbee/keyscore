package filter

import java.text.SimpleDateFormat
import java.util.{Calendar, Locale}

object FilterUtils {

  def getCurrentTimeFormatted: String = {
    val now = Calendar.getInstance().getTime
    val timeFormat = new SimpleDateFormat("MMMM dd yyyy, HH:mm:ss.SSS",Locale.US)
    val currentTimeString = timeFormat.format(now)
    currentTimeString
  }
}
