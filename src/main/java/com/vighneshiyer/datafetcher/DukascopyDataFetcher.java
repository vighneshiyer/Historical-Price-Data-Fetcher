package com.vighneshiyer.datafetcher;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.concurrent.Future;

/**
 * Fetches historical data from dukascopy.com/datafeed
 * Some caveats: tick data is only available going back to 2007 or so, before that only minute data is available
 * This class will make a best attempt to get the tick data as far back as possible.
 * The resolution can be specified, but if it is too low for the fetcher, then a higher resolution will be used.
 */
public class DukascopyDataFetcher implements DataFetcher {
    private static final String tickAPITemplateURL = "https://www.dukascopy.com/datafeed/%s/%d/%02d/%02d/%02dh_ticks.bi5";

    public Future<Void> preloadHistoricalTicksForSymbol(final String symbol, final ZonedDateTime startTime, final ZonedDateTime endTime) {
        // This is a first pass attempt to download and decompress tick data from Dukascopy
        // This is a single threaded implementation, which will be modified later to use Akka or a simple threadpool executor

        // Create the directory that all the downloaded files will be stored in
        String homeDirectoryPath = System.getProperty("user.home");
        File dataFetcherDirectory = new File(homeDirectoryPath + "\\HistoricalPriceDataFetcher");
        if (!dataFetcherDirectory.exists()) {
            dataFetcherDirectory.mkdirs();
        }

        ZonedDateTime fetchTime = startTime.withZoneSameInstant(ZoneOffset.UTC);
        while (fetchTime.isBefore(endTime) || fetchTime.equals(endTime)) {
            int year = fetchTime.get(ChronoField.YEAR);
            int month = fetchTime.get(ChronoField.MONTH_OF_YEAR);
            int day = fetchTime.get(ChronoField.DAY_OF_MONTH);
            int hour = fetchTime.get(ChronoField.HOUR_OF_DAY);

            // Create the directory for this tick file
            File tickDataDirectory = new File(dataFetcherDirectory.getAbsolutePath() + "\\" + year + "\\" + month + "\\" + day);
            tickDataDirectory.mkdirs();
            File tickFile = new File(tickDataDirectory.getAbsolutePath() + "\\" + hour + "h_ticks.bi5");
            String tickURLString = new TickURLGenerator(symbol, year, month, day, hour).toString();
            System.out.println(tickURLString);
            URL tickURL = null;
            try {
                tickURL = new URL(tickURLString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (tickURL == null) {
                System.out.println("ERROR for URL" + tickURLString);
                continue;
            }
            try {
                FileUtils.copyURLToFile(tickURL, tickFile, 3000, 3000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fetchTime = fetchTime.plus(1, ChronoUnit.HOURS);
        }
        return null;
    }

    private class TickURLGenerator {
        private final String URL;
        TickURLGenerator(String symbol, int year, int month, int day, int hour) {
            URL = String.format(tickAPITemplateURL, symbol, year, month, day, hour);
        }

        @Override
        public String toString() {
            return URL;
        }
    }

}


