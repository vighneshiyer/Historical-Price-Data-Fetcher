package com.vighneshiyer;

public class OHLC {
    private final Double open;
    private final Double high;
    private final Double low;
    private final Double close;

    @Override
    public String toString() {
        return "OHLC{" +
                "open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                '}';
    }

    public Double getOpen() {
        return open;
    }

    public OHLC(Double open, Double high, Double low, Double close) {

        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}
