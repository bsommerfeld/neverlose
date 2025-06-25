package de.bsommerfeld.neverlose.logger;

import org.slf4j.Logger;

public class LogFacadeImpl implements LogFacade {

    private final Logger slf4jLogger;

    LogFacadeImpl(Logger slf4jLogger) {
        if (slf4jLogger == null) {
            throw new IllegalArgumentException("Underlying SLF4J Logger must not be null.");
        }
        this.slf4jLogger = slf4jLogger;
    }

    @Override
    public void trace(String msg) {
        slf4jLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        slf4jLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        slf4jLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        slf4jLogger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        slf4jLogger.trace(msg, t);
    }

    @Override
    public void debug(String msg) {
        slf4jLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        slf4jLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        slf4jLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        slf4jLogger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        slf4jLogger.debug(msg, t);
    }

    @Override
    public void info(String msg) {
        slf4jLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        slf4jLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        slf4jLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        slf4jLogger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        slf4jLogger.info(msg, t);
    }

    @Override
    public void warn(String msg) {
        slf4jLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        slf4jLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        slf4jLogger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        slf4jLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        slf4jLogger.warn(msg, t);
    }

    @Override
    public void error(String msg) {
        slf4jLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        slf4jLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        slf4jLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        slf4jLogger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        slf4jLogger.error(msg, t);
    }
}
