import static ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.more.appenders.AugurWorksDataFluentAppender

appender("FLUENTD", AugurWorksDataFluentAppender) {
    label = "logback"
    remoteHost = System.getProperty('FLUENTD_HOST') ?: System.getenv('FLUENTD_HOST')
    port = 24224
    maxQueueSize = 999
    additionalFields = [
        function: "ENG",
        env: System.getProperty('ENV') ?: (System.getenv('ENV') ?: 'LOCAL'),
        hostname: System.getProperty('HOSTNAME') ?: (System.getenv('HOSTNAME') ?: InetAddress.getLocalHost().getHostName())
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

if (System.getProperty('FLUENTD_HOST') ?: System.getenv('FLUENTD_HOST')) {
	appenders.push("FLUENTD")
}

logger("grails.app.controllers.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.services.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.conf.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.domain.com.augurworks.engine", DEBUG, appenders)
logger("grails.app.jobs.com.augurworks.engine", DEBUG, appenders)
logger("com.augurworks.engine", DEBUG, appenders)
