package com.augurworks.engine.helper

import groovy.time.TimeCategory

import java.text.DateFormat
import java.text.SimpleDateFormat

class TradingHours {

    private static final DateFormat dayFormat = new SimpleDateFormat('MM/dd/yyyy')

    private static final List<String> HOLIDAYS = [
            '01/02/2017',
            '01/16/2017',
            '02/20/2017',
            '04/14/2017',
            '05/29/2017',
            '07/04/2017',
            '09/04/2017',
            '11/23/2017',
            '12/25/2017'
    ]

    private static final List<String> HALF_DAYS = [
            '07/03/2017',
            '11/24/2017'
    ]

    private static final Integer DAY_OPEN_MINUTES = 9 * 60 + 30
    private static final Integer HALF_DAY_CLOSE_MINUTES = 13 * 60
    private static final Integer DAY_CLOSE_MINUTES = 16 * 60 - 15

    static Boolean isMarketOpen(Date date) {
        return isWeekday(date) && !isHoliday(date) && (isDayDate(date) || isTradingHours(date))
    }

    private static Boolean isWeekday(Date date) {
        Integer day = date[Calendar.DAY_OF_WEEK]
        return day != Calendar.SATURDAY && day != Calendar.SUNDAY
    }

    private static Boolean isHoliday(Date date) {
        return HOLIDAYS.contains(dayFormat.format(date))
    }

    private static Boolean isDayDate(Date date) {
        Integer minutesOfDay = date[Calendar.HOUR_OF_DAY] * 60 + date[Calendar.MINUTE]
        return minutesOfDay == 0
    }

    private static Boolean isTradingHours(Date date) {
        Integer minutesOfDay = date[Calendar.HOUR_OF_DAY] * 60 + date[Calendar.MINUTE]
        return minutesOfDay >= DAY_OPEN_MINUTES && (minutesOfDay <= HALF_DAY_CLOSE_MINUTES || (!HALF_DAYS.contains(dayFormat.format(date)) && minutesOfDay <= DAY_CLOSE_MINUTES))
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
            Integer endOfTradingDay = (HALF_DAYS.contains(dayFormat.format(finalDate)) ? HALF_DAY_CLOSE_MINUTES : DAY_CLOSE_MINUTES)
            Integer timeUntilEndOfTradingDay = endOfTradingDay - (finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE])
            if (remainingMinutes < timeUntilEndOfTradingDay) {
                finalDate = addMinutes(finalDate, remainingMinutes)
                break
            } else {
                finalDate = addMinutes(finalDate, timeUntilEndOfTradingDay + (24 * 60 - (HALF_DAYS.contains(dayFormat.format(finalDate)) ? HALF_DAY_CLOSE_MINUTES : DAY_CLOSE_MINUTES) + DAY_OPEN_MINUTES))
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
                finalDate = addMinutes(finalDate, -(24 * 60 - (HALF_DAYS.contains(dayFormat.format(addMinutes(finalDate, -24 * 60))) ? HALF_DAY_CLOSE_MINUTES : DAY_CLOSE_MINUTES) + DAY_OPEN_MINUTES))
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
        if (HALF_DAYS.contains(dayFormat.format(finalDate)) && finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE] > HALF_DAY_CLOSE_MINUTES) {
            finalDate = addMinutes(finalDate.clearTime(), HALF_DAY_CLOSE_MINUTES)
        }
        Integer minutesOffset = ((finalDate[Calendar.HOUR_OF_DAY] * 60 + finalDate[Calendar.MINUTE]) / periodMinutes).intValue() * periodMinutes
        return addMinutes(finalDate.clearTime(), minutesOffset)
    }

    private static Boolean isBeginningOfDay(Date date) {
        return date[Calendar.HOUR_OF_DAY] * 60 + date[Calendar.MINUTE] == DAY_OPEN_MINUTES
    }

    private static Date addMinutes(Date date, Integer minutes) {
        use(TimeCategory) {
            return date + minutes.minutes
        }
    }

    private static Date addDays(Date date, Integer days) {
        use(TimeCategory) {
            return date + days.days
        }
    }
}
