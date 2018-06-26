package org.slello.logger

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory


enum class LogPattern(val pattern: String) {
    DEFAULT("%highlight([%-5level]) %magenta([%date{ISO8601}]) %yellow([%X{stamp}][%X{username}]) %magenta([%class{50}->%method]) | %msg%n"),
    EXCEPTION("%highlight([%-5level]) %magenta([%date{ISO8601}]) %yellow([%X{stamp}]) %magenta([%class->%method:%line]) [exception %msg - Thread Id %thread - Start]%n%xException{8}%n[exception %msg - End]%n")
}

data class LoggerConfiguration(val level: Level, val pattern: LogPattern = LogPattern.DEFAULT)

enum class Loggers(val loggerConfiguration: LoggerConfiguration) {
    EXCEPTIONS(LoggerConfiguration(level = Level.ERROR, pattern = LogPattern.EXCEPTION)),
    SYSTEM(LoggerConfiguration(level = Level.ALL)),
    REPOSITORY(LoggerConfiguration(level = Level.INFO));

    val loggerName: String by lazy {
        name.toLowerCase()
    }

    val logger: Logger by lazy {
        LoggerFactory.getLogger(loggerName)
    }
}