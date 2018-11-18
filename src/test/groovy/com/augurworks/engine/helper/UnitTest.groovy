package com.augurworks.engine.helper


import spock.lang.Specification

class UnitTest extends Specification {

    void "test hour offset"() {
        given:
        Date startDate = Date.parse('MM/dd/yyyy HH:mm', '11/18/2018 12:13')
        Date expectedOffsetDate = Date.parse('MM/dd/yyyy HH:mm', '11/06/2018 13:45')

        when:
        Date offsetDate = Unit.HOUR.calculateOffset.apply(startDate, -52)

        then:
        expectedOffsetDate == offsetDate
    }

    void "test half hour offset"() {
        given:
        Date startDate = Date.parse('MM/dd/yyyy HH:mm', '11/18/2018 12:13')
        Date expectedOffsetDate = Date.parse('MM/dd/yyyy HH:mm', '11/06/2018 13:45')

        when:
        Date offsetDate = Unit.HALF_HOUR.calculateOffset.apply(startDate, -104)

        then:
        expectedOffsetDate == offsetDate
    }
}
