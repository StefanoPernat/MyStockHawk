package com.udacity.stockhawk.data;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by stefanopernat on 08/03/17.
 */

// This class is an utility class for stocks

public class StockUtils {
    public static ArrayList<Entry> fromStockHistoryToPoint(String symbolHistory, float currentValue){
        ArrayList<Entry> points = new ArrayList<>();

        String[] values = symbolHistory.split("\n");

        // Add also the current value to the entries
        points.add(
                new Entry(
                        Float.valueOf(String.valueOf(System.currentTimeMillis())),
                        currentValue
                )
        );

        for (String value: values){
            String[] singleValue = value.split(", ");


            points.add(
                    new Entry(
                        Float.valueOf(singleValue[0]),
                        Float.valueOf(singleValue[1])
                    )
            );
        }

        Collections.reverse(points);
        return points;
    }
}
