package filter

import java.text.SimpleDateFormat
import java.util.Calendar

object FilterUtils {

  def getCurrentTimeFormatted: String = {
    val now = Calendar.getInstance().getTime()
    val timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val currentTimeString = timeFormat.format(now)
    return currentTimeString
  }
}
