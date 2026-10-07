package com.package1.shopcook

import com.package1.shopcook.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun testGetUpcomingWeekRangesDefaultDate() {
        val ranges = DateUtils.getUpcomingWeekRanges()
        assertEquals(4, ranges.size)
        ranges.forEach { range ->
            assertNotNull(range)
            assertTrue("Range should not be empty", range.isNotBlank())
            assertTrue("Range should contain en-dash", range.contains("–"))
        }
    }

    @Test
    fun testGetUpcomingWeekRangesWithFixedDate() {
        val fixedDate = LocalDate.of(2026, 3, 16) // Monday March 16, 2026
        val ranges = DateUtils.getUpcomingWeekRanges(fixedDate)

        assertEquals(4, ranges.size)
        assertEquals("Mar 16–22", ranges[0])
        assertEquals("Mar 23–29", ranges[1])
        assertEquals("Mar 30–Apr 5", ranges[2])
        assertEquals("Apr 6–12", ranges[3])
    }

    @Test
    fun testGetWeekOptionsWithFixedDate() {
        val fixedDate = LocalDate.of(2026, 3, 16)
        val options = DateUtils.getWeekOptions(fixedDate)

        assertEquals(4, options.size)
        assertEquals("This week", options[0])
        assertEquals("Mar 23–29", options[1])
        assertEquals("Mar 30–Apr 5", options[2])
        assertEquals("Apr 6–12", options[3])
    }

    @Test
    fun testGetDateBadgeForDaysOfWeek() {
        val fixedDate = LocalDate.of(2026, 3, 16)

        assertEquals("MON 16", DateUtils.getDateBadge("MON", "This week", fixedDate))
        assertEquals("TUE 17", DateUtils.getDateBadge("TUE", "This week", fixedDate))
        assertEquals("WED 18", DateUtils.getDateBadge("WED", "This week", fixedDate))
        assertEquals("THU 19", DateUtils.getDateBadge("THU", "This week", fixedDate))
        assertEquals("FRI 20", DateUtils.getDateBadge("FRI", "This week", fixedDate))
        assertEquals("SAT 21", DateUtils.getDateBadge("SAT", "This week", fixedDate))
        assertEquals("SUN 22", DateUtils.getDateBadge("SUN", "This week", fixedDate))

        // Next week
        assertEquals("MON 23", DateUtils.getDateBadge("MON", "Mar 23–29", fixedDate))
        assertEquals("SUN 29", DateUtils.getDateBadge("SUN", "Mar 23–29", fixedDate))

        // Month boundary week
        assertEquals("MON 30", DateUtils.getDateBadge("MON", "Mar 30–Apr 5", fixedDate))
        assertEquals("SUN 5", DateUtils.getDateBadge("SUN", "Mar 30–Apr 5", fixedDate))
    }
}
