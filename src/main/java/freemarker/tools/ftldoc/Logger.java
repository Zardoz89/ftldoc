/*
 * Logger.java - ftldoc-maven-plugin
 * (C) 2021 DIGIBÍS S.L.U.
 * Todos los derechos reservados.
 */
package freemarker.tools.ftldoc;

import org.apache.maven.plugin.logging.Log;

/**
 * Wrapper aroubd Maven logger, that fallbacks to stdout/stderr if tehre isn't any maven logger
 */
public class Logger
{
    private Log log;

    public Logger()
    {
        this.log = null;
    }

    public Logger(Log log)
    {
        this.log = log;
    }

    public boolean isDebugEnabled()
    {
        return this.log == null || this.log.isDebugEnabled();
    }

    public void debug(CharSequence content)
    {
        if (this.log != null) {
            this.log.debug(content);
        } else {
            System.err.println("DEBUG: " + content);
        }
    }

    public void debug(CharSequence content, Throwable error)
    {
        if (this.log != null) {
            this.log.debug(content, error);
        } else {
            System.err.println("DEBUG: " + content);
            System.err.println(error);
        }
    }

    public void debug(Throwable error)
    {
        if (this.log != null) {
            this.log.debug(error);
        } else {
            System.err.println("DEBUG: ");
            System.err.println(error);
        }
    }

    public boolean isInfoEnabled()
    {
        return this.log == null || this.log.isInfoEnabled();
    }

    public void info(CharSequence content)
    {
        if (this.log != null) {
            this.log.info(content);
        } else {
            System.out.println("INFO: " + content);
        }
    }

    public void info(CharSequence content, Throwable error)
    {
        if (this.log != null) {
            this.log.info(content, error);
        } else {
            System.out.println("INFO: " + content);
            System.out.println(error);
        }
    }

    public void info(Throwable error)
    {
        if (this.log != null) {
            this.log.info(error);
        } else {
            System.out.println("INFO: ");
            System.out.println(error);
        }
    }

    public boolean isWarnEnabled()
    {
        return this.log == null || this.log.isWarnEnabled();
    }

    public void warn(CharSequence content)
    {
        if (this.log != null) {
            this.log.warn(content);
        } else {
            System.err.println("WARN: " + content);
        }
    }

    public void warn(CharSequence content, Throwable error)
    {
        if (this.log != null) {
            this.log.warn(content, error);
        } else {
            System.err.println("WARN: " + content);
            System.err.println(error);
        }
    }

    public void warn(Throwable error)
    {
        if (this.log != null) {
            this.log.warn(error);
        } else {
            System.err.println("WARN: ");
            System.err.println(error);
        }
    }

    public boolean isErrorEnabled()
    {
        return this.log == null || this.log.isErrorEnabled();
    }

    public void error(CharSequence content)
    {
        if (this.log != null) {
            this.log.error(content);
        } else {
            System.err.println("ERROR: " + content);
        }
    }

    public void error(CharSequence content, Throwable error)
    {
        if (this.log != null) {
            this.log.error(content, error);
        } else {
            System.err.println("ERROR: " + content);
            System.err.println(error);
        }
    }

    public void error(Throwable error)
    {
        if (this.log != null) {
            this.log.error(error);
        } else {
            System.err.println("ERROR: ");
            System.err.println(error);
        }
    }
}
