package com.vighneshiyer;

import com.vighneshiyer.datafetcher.DataFetcher;
import com.vighneshiyer.datafetcher.DukascopyDataFetcher;
import com.vighneshiyer.datafetcher.TradingviewDataFetcher;

import java.time.*;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        dukascopyExample();
    }

    private static void dukascopyExample() {
        DukascopyDataFetcher dataFetcher = new DukascopyDataFetcher();
        dataFetcher.preloadHistoricalTicksForSymbol(
                "EURUSD",
                LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0).atZone(ZoneId.systemDefault()),
                LocalDateTime.now().atZone(ZoneId.systemDefault())
        );
    }

    private static void tradingViewExample() {
        //DataFetcher dataFetcher = new TradingviewDataFetcher();
        //Map<Instant, OHLC> goldData = dataFetcher.getAllHistoricalDataForSymbol("FX:XAUUSD", 60);
    }
}
