package unit.com.augurworks.engine.helper

import com.augurworks.engine.helper.TradingHours
import spock.lang.Specification

import java.text.DateFormat
import java.text.SimpleDateFormat

class TradingHoursSpec extends Specification {

    private DateFormat dayFormat = new SimpleDateFormat('MM/dd/yyyy')
    private DateFormat hourFormat = new SimpleDateFormat('MM/dd/yyyy HH:mm')

    void "test regular days"() {
        given:
        Date date = dayFormat.parse(day)

        when:
        Boolean isMarketOpen = TradingHours.isMarketOpen(date)

        then:
        isMarketOpen == expected

        where:
        day          | expected
        '01/09/2017' | true
        '01/10/2017' | true
        '01/11/2017' | true
        '01/12/2017' | true
        '01/13/2017' | true
        '01/14/2017' | false
        '01/15/2017' | false
    }

    void "test holiday days"() {
        given:
        Date date = dayFormat.parse(day)

        when:
        Boolean isMarketOpen = TradingHours.isMarketOpen(date)

        then:
        isMarketOpen == expected

        where:
        day          | expected
        '01/02/2017' | false
        '01/16/2017' | false
        '12/25/2017' | false
    }

    void "test regular trading hours"() {
        given:
        Date date = hourFormat.parse(time)

        when:
        Boolean isMarketOpen = TradingHours.isMarketOpen(date)

        then:
        isMarketOpen == expected

        where:
        time               | expected
        '01/09/2017 05:00' | false
        '01/09/2017 09:30' | true
        '01/09/2017 12:00' | true
        '01/09/2017 16:00' | false
        '01/09/2017 16:30' | false
    }

    void "test holiday trading hours"() {
        given:
        Date date = hourFormat.parse(time)

        when:
        Boolean isMarketOpen = TradingHours.isMarketOpen(date)

        then:
        isMarketOpen == expected

        where:
        time               | expected
        '07/03/2017 05:00' | false
        '07/03/2017 09:30' | true
        '07/03/2017 12:00' | true
        '07/03/2017 14:00' | false
        '07/03/2017 16:00' | false
        '07/03/2017 16:30' | false
    }

    void "test add trading minutes"() {
        given:
        Date date = hourFormat.parse(time)

        when:
        Date future = TradingHours.addPositiveTradingMinutes(date, tradingMinutes)

        then:
        future == hourFormat.parse(expected)

        where:
        time               | tradingMinutes | expected
        '01/09/2017 10:00' | 60             | '01/09/2017 11:00'
        '01/09/2017 10:00' | 120            | '01/09/2017 12:00'
        '01/09/2017 15:00' | 90             | '01/10/2017 10:15'
        '01/09/2017 15:00' | 600            | '01/11/2017 12:30'
        '01/06/2017 15:00' | 600            | '01/10/2017 12:30'
        '01/13/2017 15:00' | 600            | '01/18/2017 12:30'
        '06/30/2017 15:00' | 600            | '07/05/2017 15:15'
    }

    void "test subtract trading minutes"() {
        given:
        Date date = hourFormat.parse(time)

        when:
        Date past = TradingHours.addNegativeTradingMinutes(date, tradingMinutes)

        then:
        past == hourFormat.parse(expected)

        where:
        time               | tradingMinutes | expected
        '01/09/2017 10:00' | 30             | '01/09/2017 09:30'
        '01/10/2017 10:00' | 60             | '01/09/2017 15:15'
        '01/11/2017 10:00' | 600            | '01/09/2017 12:30'
        '01/10/2017 10:00' | 600            | '01/06/2017 12:30'
        '01/18/2017 10:00' | 600            | '01/13/2017 12:30'
        '07/05/2017 15:00' | 600            | '06/30/2017 14:45'
    }

    void "test add trading days"() {
        given:
        Date date = dayFormat.parse(time)

        when:
        Date future = TradingHours.addTradingDays(date, tradingDays)

        then:
        future == dayFormat.parse(expected)

        where:
        time         | tradingDays | expected
        '01/09/2017' | 0           | '01/09/2017'
        '01/09/2017' | 1           | '01/10/2017'
        '01/10/2017' | -1          | '01/09/2017'
        '01/09/2017' | 5           | '01/17/2017'
        '01/17/2017' | -5          | '01/09/2017'
        '01/23/2017' | 5           | '01/30/2017'
        '01/30/2017' | -5          | '01/23/2017'
        '06/26/2017' | 5           | '07/03/2017'
        '07/03/2017' | -5          | '06/26/2017'
    }

    void "test floor period"() {
        given:
        Date date = hourFormat.parse(time)

        when:
        Date floor = TradingHours.floorPeriod(date, period)

        then:
        floor == hourFormat.parse(expected)

        where:
        time               | period | expected
        '01/09/2017 00:00' | 1440   | '01/09/2017 00:00'
        '01/08/2017 00:00' | 1440   | '01/06/2017 00:00'
        '01/09/2017 12:34' | 15     | '01/09/2017 12:30'
        '01/09/2017 12:34' | 30     | '01/09/2017 12:30'
        '01/09/2017 12:34' | 60     | '01/09/2017 12:00'
        '01/09/2017 12:34' | 1440   | '01/09/2017 00:00'
        '01/10/2017 08:34' | 15     | '01/09/2017 15:45'
        '01/10/2017 08:34' | 30     | '01/09/2017 15:30'
        '01/10/2017 08:34' | 60     | '01/09/2017 15:00'
        '01/10/2017 08:34' | 1440   | '01/09/2017 00:00'
        '01/08/2017 12:34' | 15     | '01/06/2017 15:45'
        '01/08/2017 12:34' | 30     | '01/06/2017 15:30'
        '01/08/2017 12:34' | 60     | '01/06/2017 15:00'
        '01/08/2017 12:34' | 1440   | '01/06/2017 00:00'
        '07/04/2017 12:34' | 15     | '07/03/2017 13:00'
        '07/04/2017 12:34' | 30     | '07/03/2017 13:00'
        '07/04/2017 12:34' | 60     | '07/03/2017 13:00'
        '07/04/2017 12:34' | 1440   | '07/03/2017 00:00'
        '07/03/2017 14:34' | 15     | '07/03/2017 13:00'
        '07/03/2017 14:34' | 30     | '07/03/2017 13:00'
        '07/03/2017 14:34' | 60     | '07/03/2017 13:00'
    }

    void "test trading minutes between"() {
        given:
        Date date1 = hourFormat.parse(time1)
        Date date2 = hourFormat.parse(time2)

        when:
        Integer between = TradingHours.tradingMinutesBetween(date1, date2)

        then:
        between == minutes

        where:
        time1              | time2              | minutes
        '01/09/2017 10:00' | '01/09/2017 10:00' | 0
        '01/09/2017 10:00' | '01/09/2017 11:00' | 60
        '01/09/2017 10:00' | '01/10/2017 10:00' | 375
        '01/09/2017 10:00' | '01/11/2017 10:00' | 750
        '01/13/2017 10:00' | '01/17/2017 10:00' | 375
    }
}
