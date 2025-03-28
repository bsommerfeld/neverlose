package de.sommerfeld.topspin.logger;

import org.slf4j.Logger;

public class LogFacade {

    private final Logger slf4jLogger;

    LogFacade(Logger slf4jLogger) {
        if (slf4jLogger == null) {
            throw new IllegalArgumentException("Underlying SLF4J Logger must not be null.");
        }
        this.slf4jLogger = slf4jLogger;
    }

    public void trace(String msg) {
        slf4jLogger.trace(msg);
    }

    public void trace(String format, Object arg) {
        slf4jLogger.trace(format, arg);
    }

    public void trace(String format, Object arg1, Object arg2) {
        slf4jLogger.trace(format, arg1, arg2);
    }

    public void trace(String format, Object... arguments) {
        slf4jLogger.trace(format, arguments);
    }

    public void trace(String msg, Throwable t) {
        slf4jLogger.trace(msg, t);
    }

    public void debug(String msg) {
        slf4jLogger.debug(msg);
    }

    public void debug(String format, Object arg) {
        slf4jLogger.debug(format, arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        slf4jLogger.debug(format, arg1, arg2);
    }

    public void debug(String format, Object... arguments) {
        slf4jLogger.debug(format, arguments);
    }

    public void debug(String msg, Throwable t) {
        slf4jLogger.debug(msg, t);
    }

    public void info(String msg) {
        slf4jLogger.info(msg);
    }

    public void info(String format, Object arg) {
        slf4jLogger.info(format, arg);
    }

    public void info(String format, Object arg1, Object arg2) {
        slf4jLogger.info(format, arg1, arg2);
    }

    public void info(String format, Object... arguments) {
        slf4jLogger.info(format, arguments);
    }

    public void info(String msg, Throwable t) {
        slf4jLogger.info(msg, t);
    }

    public void warn(String msg) {
        slf4jLogger.warn(msg);
    }

    public void warn(String format, Object arg) {
        slf4jLogger.warn(format, arg);
    }

    public void warn(String format, Object... arguments) {
        slf4jLogger.warn(format, arguments);
    }

    public void warn(String format, Object arg1, Object arg2) {
        slf4jLogger.warn(format, arg1, arg2);
    }

    public void warn(String msg, Throwable t) {
        slf4jLogger.warn(msg, t);
    }

    public void error(String msg) {
        slf4jLogger.error(msg);
    }

    public void error(String format, Object arg) {
        slf4jLogger.error(format, arg);
    }

    public void error(String format, Object arg1, Object arg2) {
        slf4jLogger.error(format, arg1, arg2);
    }

    public void error(String format, Object... arguments) {
        slf4jLogger.error(format, arguments);
    }

    public void error(String msg, Throwable t) {
        slf4jLogger.error(msg, t);
    }

    public boolean isTraceEnabled() {
        return slf4jLogger.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return slf4jLogger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return slf4jLogger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return slf4jLogger.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return slf4jLogger.isErrorEnabled();
    }
}
