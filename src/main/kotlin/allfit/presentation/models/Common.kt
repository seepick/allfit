package allfit.presentation.models

import allfit.service.Clock
import allfit.service.formatStartAndEnd
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

typealias Rating = Int

data class DateRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) : Comparable<DateRange> {

    fun toPrettyString(clock: Clock) =
        formatStartAndEnd(showYear = start.year != clock.now().year)

    val durationInMinutes = ChronoUnit.MINUTES.between(start, end)

    init {
        require(start.isEqual(end) || start.isBefore(end)) { "START $start must be before-equal END $end." }
    }

    override fun compareTo(other: DateRange): Int {
        val startDiff = start.compareTo(other.start)
        if (startDiff != 0) return startDiff
        return end.compareTo(other.end)
    }
}
