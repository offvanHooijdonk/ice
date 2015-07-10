package by.ingman.ice.retailerrequest.v2.helpers;

import android.os.Environment;

import org.apache.log4j.Level;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by Yahor_Fralou on 7/10/2015.
 */
public class ConfigureLog4J {
    public static final String LOG_FILE_PATH = "/icev2/main.log";
    public static final long LOG_FILE_MAX_SIZE = 50 * 1024;

    public static void configure() {
        final LogConfigurator logConfigurator = new LogConfigurator();

        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + LOG_FILE_PATH);
        logConfigurator.setRootLevel(Level.WARN);
        logConfigurator.setUseLogCatAppender(false);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setMaxFileSize(LOG_FILE_MAX_SIZE);
        // Set log level of a specific logger
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.configure();
    }
}
