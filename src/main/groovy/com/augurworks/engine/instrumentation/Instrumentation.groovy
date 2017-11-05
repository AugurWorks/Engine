package com.augurworks.engine.instrumentation

import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.StatsDClient
import grails.util.Holders

class Instrumentation {

    public static final StatsDClient statsdClient =  new NonBlockingStatsDClient(
            Holders.config.statsd.prefix,
            Holders.config.statsd.host,
            Holders.config.statsd.port,
            Holders.config.statsd.type,
            Holders.config.statsd.element
    )
}
