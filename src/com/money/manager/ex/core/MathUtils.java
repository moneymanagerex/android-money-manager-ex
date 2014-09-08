package com.money.manager.ex.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Alessandro Lazzari on 08/09/2014.
 */
public class MathUtils {

    public static Double Round(double value, int places) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
