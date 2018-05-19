import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import net._95point2.utils.AugurWorksLogDNAAppender

appender("LOGDNA", AugurWorksLogDNAAppender) {
    appName = "Engine"
    ingestKey = System.getProperty("LOGDNA_INGEST_KEY") ?: System.getenv('LOGDNA_INGEST_KEY')
    additionalFields = [
        function: "ENG",
        env: System.getProperty('ENV') ?: (System.getenv('ENV') ?: 'LOCAL')
    ]
}

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %logger{15}#%line %msg %n"
    }
}

appender("ROLLING", RollingFileAppender) {
	file = "logs/debug.log"
	encoder(PatternLayoutEncoder) {
		Pattern = "%d %level %mdc %logger - %m%n"
	}
	rollingPolicy(TimeBasedRollingPolicy) {
		FileNamePattern = "logs/debug-%d{yyyy-MM}.zip"
	}
}

Collection<String> appenders = ["STDOUT", "ROLLING"]

if (System.getProperty('LOGDNA_INGEST_KEY') ?: System.getenv('LOGDNA_INGEST_KEY')) {
    appenders.push("LOGDNA")
}

logger("grails.app.controllers.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.services.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.conf.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.domain.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.jobs.com.augurworks.engine", DEBUG, appenders)
logger("com.augurworks.engine", DEBUG, appenders)

if (System.getProperty('SQL_LOGGING') ?: System.getenv('SQL_LOGGING')) {
    logger("org.hibernate.SQL", DEBUG, appenders)
    logger("org.hibernate.type.descriptor.sql.BasicBinder", TRACE, appenders)
}
