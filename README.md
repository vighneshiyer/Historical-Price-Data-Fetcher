Tasks for Java library to fetch historical forex/CFDs/equities/futures(maybe) data

1. Ability to download from various sources using whatever strategies are best (Selenium webscraping, threaded downloaders over HTTP, fully concurrent design)
    a. Dukascopy (http://eareview.net/tick-data/dukascopy-php-scripts https://www.dukascopy.com/datafeed/EURUSD/2010/08/20/01h_ticks.bi5)
    b. Gain Capital (http://eareview.net/tick-data/downloads)
    b. Yahoo Finance (http://www.quantshare.com/sa-426-6-ways-to-download-free-intraday-and-tick-data-for-the-us-stock-market)
    c. Google Finance
    d. Tradingview (selenium scraping needed)
    e. Schwab
2. Ability to aggregate all the data from various sources and store it in flat files or a database with a given schema