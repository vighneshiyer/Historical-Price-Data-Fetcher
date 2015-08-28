package com.vighneshiyer.datafetcher;

import com.vighneshiyer.OHLC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.Future;

public interface DataFetcher {

    //public Future<Void> preloadHistoricalTicksForSymbol(String symbol);
    public Future<Void> preloadHistoricalTicksForSymbol(String symbol, ZonedDateTime startTime, ZonedDateTime endTime);

    /*public Future<Void> preloadHistoricalCandlesForSymbol(String symbol, int lowerResolutionBound);

    public Future<TimeSeries> getHistoricalCandlesForSymbol(String symbol, int resolution);
    public Future<TickSeries> getHistoricalTicksForSymbol(String symbol, int resolution);

    public int lowestLoadedResolutionForSymbol(String symbol);
    public TimeSeriesRange loadedTimeBoundsForSymbol(String symbol);
    public TimeSeriesRange datasourceTimeBoundsForSymbol(String symbol);
    public TimeSeriesRange datasourceTickBoundsForSymbol(String symbol);*/
}
