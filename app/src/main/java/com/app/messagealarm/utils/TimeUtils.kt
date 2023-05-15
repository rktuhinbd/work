package com.app.messagealarm.utils

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.app.messagealarm.BaseApplication
import es.dmoral.toasty.Toasty

import timber.log.Timber
import java.text.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


/**
 * This is a class that contains utils for the time
 *  @author Al Mujahid Khan
 * */
class TimeUtils private constructor() {

    companion object {
        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private const val DAY_MILLIS = 24 * HOUR_MILLIS

        /**
         * This method provides current system time in milliseconds
         *
         * @return [Long] current time
         * */
        fun currentTime(): Long {
            return System.currentTimeMillis()
        }

        fun getCurrentDayName(): String {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            return SimpleDateFormat("EE", Locale.ENGLISH).format(date.time)
        }

        private fun getCurrentDayNameBDTime(): String {
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
            val date = calendar.time
            return SimpleDateFormat("EE", Locale.ENGLISH).format(date.time)
        }

        private fun getCurrentHourBDTime(): Int {
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
            return calendar.get(Calendar.HOUR_OF_DAY)
        }

        /**
         * Office Time : 30 minutes
         * Not Office Time : 1 hours
         * Sleeping Time: 11 PM -> 8AM : 4 hours
         * Weekend Day Time: 2 hour
         * Note: All times are in BST time
         */
        fun getPossibleReplyTime(): String {
            var possibleTime: String = ""
            possibleTime = when {
                getCurrentHourBDTime() in 0..5 -> {
                    "Sleeping hours"
                }
                getCurrentHourBDTime() in 22..24 -> {
                    "Sleeping hours"
                }
                else -> {
                    "Instant replay within 10 min"
                }
            }
            return possibleTime
        }

        /**
         * This method provides the first day of the current year in milliseconds
         *
         * @return [Long] milliseconds
         */
        fun getFirstDayOfTheYear(): Long {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            calendar.clear()
            calendar.set(Calendar.YEAR, year)
            // 1st day of the year
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            return calendar.timeInMillis
        }

        /**
         * This method provides the last day of the current year in milliseconds
         *
         * @return [Date] milliseconds
         */
        fun getLastDayOfTheYear(): Long {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            calendar.clear()
            calendar.set(Calendar.YEAR, year)
            // Last day of the year
            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
            calendar.set(Calendar.DAY_OF_MONTH, 31)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            return calendar.timeInMillis
        }

        /**
         * This method returns a formatted date which uses the common date format all over the app
         *
         * @param year provided year
         * @param month provided month
         * @param day provided day
         */
        fun getFormattedDateString(year: Int, month: Int, day: Int): String {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            return SimpleDateFormat(
                Constants.Common.APP_COMMON_DATE_FORMAT,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a formatted date which uses the common date format all over the app
         *
         * @param timeInMillis time
         */
        fun getFormattedDateString(timeInMillis: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return SimpleDateFormat(
                Constants.Common.APP_COMMON_DATE_FORMAT,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a formatted time which uses the common time format all over the app
         *
         * @param timeInMillis time
         */
        fun getFormattedTimeString(timeInMillis: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return SimpleDateFormat(
                Constants.Common.APP_COMMON_TIME_FORMAT,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a formatted day which uses the common format all over the app
         *
         * @param timeInMillis time
         */
        fun getFormattedDayNameString(timeInMillis: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return SimpleDateFormat(
                Constants.Common.APP_COMMON_DAY_FORMAT,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a formatted date time string
         *
         * @param timeInMillis time
         * @param format date time format
         */
        fun getFormattedDateTime(timeInMillis: Long, format: String): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return SimpleDateFormat(
                format,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a formatted only date which uses the common format all over the app
         *
         * @param timeInMillis time
         */
        fun getFormattedOnlyDateString(timeInMillis: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return SimpleDateFormat(
                Constants.Common.APP_COMMON_ONLY_DATE_FORMAT,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a formatted month which uses the common format all over the app
         *
         * @param timeInMillis time
         */
        fun getFormattedMonthString(timeInMillis: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return SimpleDateFormat(
                Constants.Common.APP_COMMON_MONTH_FORMAT,
                Locale.ENGLISH
            ).format(calendar.time)
        }

        /**
         * This method returns a calendar object which is parsed from a date string using
         * common date format of the application
         *
         * @param date formatted date as string
         */
        fun getCalendarFromDate(date: String): Calendar {
            val calendar = Calendar.getInstance()
            try {
                calendar.time = SimpleDateFormat(
                    Constants.Common.APP_COMMON_DATE_FORMAT,
                    Locale.ENGLISH
                ).parse(date)!!
            } catch (e: ParseException) {
                Timber.e(e)
            }

            return calendar
        }

        /**
         * This method returns a calendar object which is parsed from a date string using
         * custom date time format
         *
         * @param date formatted date as string
         * @param dateTimeFormat format of the date time
         */
        fun getCalendarFromDate(date: String, dateTimeFormat: String): Calendar {
            val calendar = Calendar.getInstance()
            try {
                calendar.time = SimpleDateFormat(dateTimeFormat, Locale.ENGLISH).parse(date)!!
            } catch (e: ParseException) {
                Timber.e(e)
            }

            return calendar
        }

        /**
         * This method returns an integer representing which date is bigger than the other
         *
         * @param date1 first date
         * @param date2 second date
         * @return int state
         *
         * States:
         * less than 0 means date1 is less than date2
         * greater than 0 means date1 is greater than date2
         * exact 0 means date1 and date2 is equal
         */
        fun compareTwoDates(date1: String, date2: String): Int {
            return getCalendarFromDate(date1).time
                .compareTo(getCalendarFromDate(date2).time)
        }

        /**
         * This method provides difference between current time in milliseconds and provided time
         *
         * @param time time in milliseconds
         * @return [Long] time difference in milliseconds
         */
        fun differ(time: Long): Long {
            return currentTime() - time
        }

        /**
         * This method converts milliseconds to days
         *
         * @param time time in milliseconds
         * @return [Int] count of days
         */
        fun convertMillisecondsToDays(time: Long): Int {
            return ((time.toDouble()) / (24 * 60 * 60 * 1000).toDouble()).toInt()
        }

        /**
         * This method provides a time ago text depending on the time difference
         *
         * @param newTime new time
         * @param oldTime old time
         * @return [String] time ago text
         * */
        fun getTimeAgo(newTime: Long, oldTime: Long): String {
            val diff = newTime - oldTime

            return when {
                diff < MINUTE_MILLIS -> "Just now"
                diff < 2 * MINUTE_MILLIS -> "A minute ago"
                diff < 50 * MINUTE_MILLIS -> (diff / MINUTE_MILLIS).toString() + " minutes ago"
                diff < 90 * MINUTE_MILLIS -> "An hour ago"
                diff < 24 * HOUR_MILLIS -> (diff / HOUR_MILLIS).toString() + " hours ago"
                diff < 48 * HOUR_MILLIS -> "Yesterday"
                else -> (diff / DAY_MILLIS).toString() + " days ago"
            }
        }

        /**
         * This method provides the year based on the time stamp
         *
         * @param timeStamp timestamp
         * @return [Int] year
         * */
        fun getYear(timeStamp: Long): Int {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp
            return calendar.get(Calendar.YEAR)
        }

        /**
         * This method provides the month based on the time stamp
         *
         * @param timeStamp timestamp
         * @return [Int] month
         * */
        fun getMonth(timeStamp: Long): Int {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp
            return calendar.get(Calendar.MONTH) + 1
        }

        /**
         * This method provides the month name based on the numeric month
         *
         * @param month month
         * @return [String] month in text
         * */
        fun getMonth(month: Int): String {
            return DateFormatSymbols.getInstance().months[month - 1]
        }

        /**
         * This method provides the day of month based on the time stamp
         *
         * @param timeStamp timestamp
         * @return [Int] day of month
         * */
        fun getDayOfMonth(timeStamp: Long): Int {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp
            return calendar.get(Calendar.DAY_OF_MONTH)
        }

        @SuppressLint("SimpleDateFormat")
        fun isTimeConstrained(startTime: String, endTime: String): Boolean {
            return try {
                val date = Date()
                val dateFormatter = SimpleDateFormat("yyyy/MM/dd")
                val startDateWithTime = dateFormatter.format(date) + " " + startTime
                val endDateWithTime = dateFormatter.format(date) + " " + endTime
                val converterFormat = SimpleDateFormat("yyyy/MM/dd h a")
                converterFormat.parse(startDateWithTime)
                    .before(converterFormat.parse(endDateWithTime))
            } catch (ex: ParseException) {
                false
            }
        }

        fun convert12HrTo24Hr(time12Hr: String): Float {
            val dateFormat = SimpleDateFormat("h a", Locale.US)
            val parsedTime = dateFormat.parse(time12Hr)
            val calender = Calendar.getInstance()
            calender.time = parsedTime
            return calender.get(Calendar.HOUR_OF_DAY).toFloat()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun isTimeRangeValid(startHour: Int, endHour: Int): Boolean {
            // Convert the selected start and end hour to UTC time zone
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, startHour)
            val startTimeUTC = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, endHour)
            val endTimeUTC = calendar.timeInMillis

            // Convert the UTC time range to the user's time zone
            val userTimeZone = TimeZone.getDefault()
            val startTimeUser = startTimeUTC + userTimeZone.getOffset(startTimeUTC)
            val endTimeUser = endTimeUTC + userTimeZone.getOffset(endTimeUTC)

            // Check if the time range falls between 4 to 12
            val startHourUser = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startTimeUser),
                userTimeZone.toZoneId()
            ).hour
            val endHourUser = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(endTimeUser),
                userTimeZone.toZoneId()
            ).hour
            return startHourUser >= 4 && endHourUser <= 12
        }

        @SuppressLint("SimpleDateFormat")
        fun isConstrainedByTime(startTime: String, endTime: String): Boolean {
            return try {
                val date = Date()
                val dateFormatter = SimpleDateFormat("yyyy/MM/dd")
                val startDateWithTime = dateFormatter.format(date) + " " + startTime
                val endDateWithTime = dateFormatter.format(date) + " " + endTime
                val converterFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a")
                val currentTime = converterFormat.format(date)
                converterFormat.parse(currentTime)
                    .after(converterFormat.parse(startDateWithTime)) &&
                        converterFormat.parse(currentTime)
                            .before(converterFormat.parse(endDateWithTime))
            } catch (ex: ParseException) {
                false
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun getTimeWithAMOrPM(hr: Int, min: Int): String? {
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = hr
            cal[Calendar.MINUTE] = min
            val formatter: Format
            formatter = SimpleDateFormat("hh:mm a")
            return formatter.format(cal.time)
        }


    }
}