import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import org.slello.logger.LogPattern
import org.slello.logger.Loggers

scan()
jmxConfigurator()

Loggers.values().each { log ->
    def appenderName = "ap_" + log.getLoggerName()
    def loggerConfig = log.getLoggerConfiguration()

    def appenders = new ArrayList<String>()
    appenders.add(appenderName + "_async")
    createAppender(appenderName, log.getLoggerName(), loggerConfig.getPattern().getPattern())

    logger(log.getLoggerName(), loggerConfig.getLevel(), appenders, false)
}

def createAppender(final String appenderName, final String fileName, final String logPattern) {

    final String LOG_PATH = "log"
    final String DEFAULT_MAX_FILE_SIZE = "200MB"
    final Integer DEFAULT_MAX_HISTORY = 7
    final Integer DEFAULT_ASYNC_QUEUE_SIZE = 100

    appender(appenderName, RollingFileAppender) {
        file = "${LOG_PATH}/${fileName}.log"
        rollingPolicy(SizeAndTimeBasedRollingPolicy) {
            fileNamePattern = "${LOG_PATH}/${fileName}.%d{yyyy-MM-dd}.%i.log.gz"
            maxFileSize = DEFAULT_MAX_FILE_SIZE
            maxHistory = DEFAULT_MAX_HISTORY
        }

        encoder(PatternLayoutEncoder) {
            pattern = logPattern
        }
    }

    appender(appenderName + "_async", AsyncAppender) {
        queueSize = DEFAULT_ASYNC_QUEUE_SIZE
        discardingThreshold = 0
        includeCallerData = true
        appenderRef(appenderName)
    }
}

createAppender("STDOUT", "stdout", LogPattern.DEFAULT.pattern)
createAppender("STDERR", "stderr", LogPattern.DEFAULT.pattern)
createAppender("ap_spring", "spring", LogPattern.DEFAULT.pattern)
createAppender("ap_mongo_template", "mongodb", LogPattern.DEFAULT.pattern)
logger("org.springframework", INFO, ["ap_spring"], false)
logger("org.springframework.data.mongodb.core.MongoTemplate", DEBUG, ["ap_mongo_template"], false)

root(INFO, ["STDOUT"])