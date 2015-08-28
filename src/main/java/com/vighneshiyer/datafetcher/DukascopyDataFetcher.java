package com.vighneshiyer.datafetcher;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Fetches historical data from dukascopy.com/datafeed
 * Some caveats: tick data is only available going back to 2007 or so, before that only minute data is available
 * This class will make a best attempt to get the tick data as far back as possible.
 * The resolution can be specified, but if it is too low for the fetcher, then a higher resolution will be used.
 */
public class DukascopyDataFetcher implements DataFetcher {

    // This is an initial attempt to download and decompress tick data from Dukascopy
    // This is a implementation using parallel streams (w/ global FPJ), which will be modified later to use Akka
    public Future<Void> preloadHistoricalTicksForSymbol(final String symbol, final ZonedDateTime startTime, final ZonedDateTime endTime) {
        // Create the directory that all the downloaded files will be stored in
        final String homeDirectoryPath = System.getProperty("user.home");
        final File dataFetcherDirectory = new File(homeDirectoryPath +
                File.separator + "HistoricalPriceDataFetcher" +
                File.separator + "DukascopyDataFetcher"
        );
        if (!dataFetcherDirectory.exists()) {
            dataFetcherDirectory.mkdirs();
        }

        final ZonedDateTime startTimeUTC = roundToNearestHourDown(startTime.withZoneSameInstant(ZoneOffset.UTC));
        final ZonedDateTime endTimeUTC = roundToNearestHourUp(endTime.withZoneSameInstant(ZoneOffset.UTC));

        // Generate the range of dates for which to fetch tick data
        final List<ZonedDateTime> datesToFetch = new ArrayList<>();
        ZonedDateTime fetchTime = startTimeUTC;
        while (fetchTime.isBefore(endTimeUTC) || fetchTime.equals(endTimeUTC)) {
            datesToFetch.add(fetchTime);
            fetchTime = fetchTime.plusHours(1);
        }

        datesToFetch.parallelStream().forEach(new Consumer<ZonedDateTime>() {
            @Override
            public void accept(ZonedDateTime fetchDateTime) {
                // Create the directory for this tick file
                final File tickDataDirectory = new File(dataFetcherDirectory.getAbsolutePath() +
                        File.separator + symbol +
                        File.separator + fetchDateTime.getYear() +
                        File.separator + fetchDateTime.getMonthValue() +
                        File.separator + fetchDateTime.getDayOfMonth());
                tickDataDirectory.mkdirs();
                final File tickFile = new File(tickDataDirectory.getAbsolutePath() +
                        File.separator + String.format("%02d", fetchDateTime.getHour()) + "h_ticks.bi5");
                if (tickFile.exists()) {
                    return; // Don't redownload files that already exist
                }
                // Generate the URL and copy the tick data to the FS
                URL tickURL = generateTickAPIURL(symbol, Year.of(fetchDateTime.getYear()),
                        MonthDay.of(fetchDateTime.getMonth(), fetchDateTime.getDayOfMonth()), fetchDateTime.getHour());
                if (tickURL == null) {
                    // Log some error here, add log4j handler
                    // This is really a significant error that indicates a bug in the code
                    return;
                }
                try {
                    System.out.println(tickURL);
                    FileUtils.copyURLToFile(tickURL, tickFile, 3000, 3000);
                } catch (IOException e) {
                    // Catch specifically the FileNotFoundException (404) and implement a missing data flag
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

    private URL generateTickAPIURL(String symbol, Year year, MonthDay monthDay, int hour) {
        final String tickAPITemplateURL = "https://www.dukascopy.com/datafeed/%s/%d/%02d/%02d/%02dh_ticks.bi5";
        try {
            return new URL(
                    String.format(tickAPITemplateURL, symbol, year.getValue(),
                            monthDay.getMonth().getValue() - 1, monthDay.getDayOfMonth(), hour));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ZonedDateTime roundToNearestHourDown(ZonedDateTime time) {
        return time.withNano(0).withSecond(0).withMinute(0);
    }

    private ZonedDateTime roundToNearestHourUp(ZonedDateTime time) {
        ZonedDateTime stripSecondPrecision = time.withNano(0).withSecond(0);
        if (stripSecondPrecision.getMinute() > 0) { // If minute is greater than 0, move up one hour
            if (stripSecondPrecision.getHour() == 23) { // Move up one day if at last hour of day
                return stripSecondPrecision.withMinute(0).withHour(0).withDayOfMonth(stripSecondPrecision.getDayOfMonth() + 1);
            }
            return stripSecondPrecision.withMinute(0).withHour(stripSecondPrecision.getHour() + 1);
        }
        return stripSecondPrecision.withMinute(0).withHour(stripSecondPrecision.getHour());
    }

    /**
     *
     * @param bi5CompressedFile
     */
    private void decompressDukascopyBi5(File bi5CompressedFile) {

    }

    //private TickSeries parseBinaryTickData(File binaryTickFile) {
    //
    //}
}
