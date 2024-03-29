package com.augurworks.engine.helper

import groovy.time.TimeCategory

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit

class TradingHours {

    private static DateFormat getDayFormat() {
        return new SimpleDateFormat('MM/dd/yyyy')
    }

    private static final List<String> HOLIDAYS = [
            '01/02/2017',
            '01/16/2017',
            '02/20/2017',
            '04/14/2017',
            '05/29/2017',
            '07/04/2017',
            '09/04/2017',
            '11/23/2017',
            '12/25/2017',
            '01/01/2018',
            '01/15/2018',
            '02/19/2018',
            '03/30/2018',
            '05/28/2018',
            '07/04/2018',
            '09/03/2018',
            '11/22/2018',
            '12/05/2018',
            '12/25/2018',
            '01/01/2019',
            '01/21/2019',
            '02/18/2019',
            '04/19/2019',
            '05/27/2019',
            '07/04/2019',
            '09/02/2019',
            '11/28/2019',
            '12/25/2019'
    ]

    private static final List<String> HALF_DAYS = [
            '07/03/2017',
            '11/24/2017',
            '07/03/2018',
            '11/23/2018',
            '12/24/2018',
            '12/31/2018',
            '07/03/2019',
            '11/29/2019',
            '12/24/2019',
            '12/31/2019'
    ]

    private static final Integer DAY_OPEN_MINUTES = 9 * 60 + 30
    private static final Integer HALF_DAY_CLOSE_MINUTES = 13 * 60
    private static final Integer DAY_CLOSE_MINUTES = 16 * 60 - 15

    static Boolean isMarketOpen(Date date) {
        return isTradingDay(date) && (isDayDate(date) || isTradingHours(date))
    }

    static Boolean isTradingDay(Date date) {
        return isWeekday(date) && !isHoliday(date)
    }

    private static Boolean isWeekday(Date date) {
        Integer day = date[Calendar.DAY_OF_WEEK]
        return day != Calendar.SATURDAY && day != Calendar.SUNDAY
    }

    private static Boolean isHoliday(Date date) {
        return HOLIDAYS.contains(getDayFormat().format(date))
    }

    private static Boolean isHalfDay(Date date) {
        return HALF_DAYS.contains(getDayFormat().format(date))
    }

    private static Boolean isDayDate(Date date) {
        Integer minutesOfDay = date[Calendar.HOUR_OF_DAY] * 60 + date[Calendar.MINUTE]
        return minutesOfDay == 0
    }

    private static Boolean isTradingHours(Date date) {
        Integer minutesOfDay = date[Calendar.HOUR_OF_DAY] * 60 + date[Calendar.MINUTE]
        return minutesOfDay >= DAY_OPEN_MINUTES && (minutesOfDay <= HALF_DAY_CLOSE_MINUTES || (!HALF_DAYS.contains(getDayFormat().format(date)) && minutesOfDay <= DAY_CLOSE_MINUTES))
    }

    static Date addTradingMinutes(Date date, Integer minutes) {
        return minutes > 0 ? addPositiveTradingMinutes(date, minutes) : addNegativeTradingMinutes(date, Math.abs(minutes))
    }

    protected static Date addPositiveTradingMinutes(Date date, Integer minutes) {
        if (!isTradingHours(date)) {
            throw new RuntimeException('Bad starting date')
        }
        Date finalDate = date.clone()
        Integer remainingMinutes = minutes
        while (remainingMinutes > 0) {
            if (!isWeekday(finalDate)) {
                finalDate = addMinutes(finalDate, 48 * 60)
                continue
            }
            if (isHoliday(finalDate)) {
                finalDate = addMinutes(finalDate, 24 * 60)
                continue
            }
            Integer endOfTradingDay = (HALF_DAYS.contains(getDayFormat().format(finalDate)) ? HALF_DAY_CLOSE_MINUTES : DAY_CLOSE_MINUTES)
            Integer timeUntilEndOfTradingDay = endOfTradingDay - (finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE])
            if (remainingMinutes < timeUntilEndOfTradingDay) {
                finalDate = addMinutes(finalDate, remainingMinutes)
                break
            } else {
                finalDate = addMinutes(finalDate, timeUntilEndOfTradingDay + (24 * 60 - (HALF_DAYS.contains(getDayFormat().format(finalDate)) ? HALF_DAY_CLOSE_MINUTES : DAY_CLOSE_MINUTES) + DAY_OPEN_MINUTES))
                remainingMinutes -= timeUntilEndOfTradingDay
            }
        }
        return finalDate
    }

    protected static Date addNegativeTradingMinutes(Date date, Integer minutes) {
        if (!isTradingHours(date)) {
            throw new RuntimeException('Bad starting date')
        }
        Date finalDate = date.clone()
        Integer remainingMinutes = minutes
        while (remainingMinutes > 0) {
            if ((!isBeginningOfDay(finalDate) && !isWeekday(finalDate)) || (isBeginningOfDay(finalDate) && !isWeekday(addMinutes(finalDate, -24 * 60)))) {
                finalDate = addMinutes(finalDate, -48 * 60)
                continue
            }
            if ((!isBeginningOfDay(finalDate) && isHoliday(finalDate)) || (isBeginningOfDay(finalDate) && isHoliday(addMinutes(finalDate, -24 * 60)))) {
                finalDate = addMinutes(finalDate, -24 * 60)
                continue
            }
            if (isBeginningOfDay(finalDate)) {
                finalDate = addMinutes(finalDate, -(24 * 60 - (HALF_DAYS.contains(getDayFormat().format(addMinutes(finalDate, -24 * 60))) ? HALF_DAY_CLOSE_MINUTES : DAY_CLOSE_MINUTES) + DAY_OPEN_MINUTES))
                continue
            }
            Integer timeUntilBeginningOfTradingDay = (finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE]) - DAY_OPEN_MINUTES
            if (remainingMinutes <= timeUntilBeginningOfTradingDay) {
                finalDate = addMinutes(finalDate, -remainingMinutes)
                break
            } else {
                finalDate = addMinutes(finalDate, -timeUntilBeginningOfTradingDay)
                remainingMinutes -= timeUntilBeginningOfTradingDay
            }
        }
        return finalDate
    }

    static Date addTradingDays(Date date, Integer tradingDays) {
        Date finalDate = date.clearTime()
        Integer remainingDays = Math.abs(tradingDays)
        while (remainingDays > 0) {
            finalDate = addDays(finalDate, tradingDays > 0 ? 1 : -1)
            if (isMarketOpen(finalDate)) {
                remainingDays--
            }
        }
        return finalDate
    }

    static Date floorPeriod(Date date, Integer periodMinutes) {
        Date finalDate = date.clone()
        Integer currentMinutes = finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE]
        if (currentMinutes < DAY_OPEN_MINUTES && !(periodMinutes == 24 * 60 && currentMinutes == 0)) {
            finalDate = addMinutes(finalDate.clearTime(), DAY_CLOSE_MINUTES - 24 * 60)
        }
        while (!isWeekday(finalDate) || isHoliday(finalDate)) {
            finalDate = addMinutes(addDays(finalDate, -1).clearTime(), DAY_CLOSE_MINUTES)
        }
        if (finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE] > DAY_CLOSE_MINUTES) {
            finalDate = addMinutes(finalDate.clearTime(), DAY_CLOSE_MINUTES)
        }
        if (HALF_DAYS.contains(getDayFormat().format(finalDate)) && finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE] > HALF_DAY_CLOSE_MINUTES) {
            finalDate = addMinutes(finalDate.clearTime(), HALF_DAY_CLOSE_MINUTES)
        }
        Integer minutesOffset = ((finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE]) / periodMinutes).intValue() * periodMinutes
        if (minutesOffset < DAY_OPEN_MINUTES && periodMinutes < 1440) {
            return addNegativeTradingMinutes(addMinutes(finalDate.clearTime(), DAY_OPEN_MINUTES), DAY_OPEN_MINUTES - minutesOffset)
        }
        return addMinutes(finalDate.clearTime(), minutesOffset)
    }

    static Integer tradingMinutesBetween(Date date1, Date date2) {
        Date end = floorPeriod(new Date(Math.max(date1.getTime(), date2.getTime())), 1)
        Date now = floorPeriod(new Date(Math.min(date1.getTime(), date2.getTime())), 1)
        Integer between = 0
        while (true) {
            if (Math.floor(now.getTime() / (24 * 3600 * 1000)) == Math.floor(end.getTime() / (24 * 3600 * 1000))) {
                return between + (end.getTime() - now.getTime()) / (60 * 1000)
            }
            if (isHalfDay(now)) {
                between += HALF_DAY_CLOSE_MINUTES - DAY_OPEN_MINUTES
            } else if (isWeekday(now) && !isHoliday(now)) {
                between += DAY_CLOSE_MINUTES - DAY_OPEN_MINUTES
            }
            now = addDays(now, 1)
        }
    }

    static Date floorAnyPeriod(Date date, Integer periodMinutes) {
        if (periodMinutes == 1440) {
            return date.clearTime()
        }
        date.set(minute: Math.floor(date[Calendar.MINUTE] / periodMinutes).toInteger() * periodMinutes, second: 0)
        return Date.from(date.toInstant().truncatedTo(ChronoUnit.SECONDS))
    }

    private static Boolean isBeginningOfDay(Date date) {
        return date[Calendar.HOUR_OF_DAY] * 60 + date[Calendar.MINUTE] == DAY_OPEN_MINUTES
    }

    private static Date addMinutes(Date date, Integer minutes) {
        use(TimeCategory) {
            Date finalDate = date + minutes.minutes
            if (TimeZone.getDefault().inDaylightTime(date) && !TimeZone.getDefault().inDaylightTime(finalDate)) {
                return finalDate + 60.minutes
            }
            if (!TimeZone.getDefault().inDaylightTime(date) && TimeZone.getDefault().inDaylightTime(finalDate)) {
                return finalDate - 60.minutes
            }
            return finalDate
        }
    }

    private static Date addDays(Date date, Integer days) {
        use(TimeCategory) {
            Date finalDate = date + days.days
            if (TimeZone.getDefault().inDaylightTime(date) && !TimeZone.getDefault().inDaylightTime(finalDate)) {
                return finalDate + 60.minutes
            }
            if (!TimeZone.getDefault().inDaylightTime(date) && TimeZone.getDefault().inDaylightTime(finalDate)) {
                return finalDate - 60.minutes
            }
            return finalDate
        }
    }
}
