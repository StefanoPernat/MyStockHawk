package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.StockUtils;

import java.util.ArrayList;

import timber.log.Timber;

//  This is the activity that display

public class StockTrendActivity extends AppCompatActivity
                                implements LoaderManager.LoaderCallbacks<Cursor>{

    //  Id for the history loader
    private static final int QUOTE_HISTORY_LOADER_ID = 10;
    private static final String SYMBOL_EXTRA = "symbol";

    private String mSelectedSymbol;

    private FrameLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_trend);

        Intent intent = getIntent();

        if (intent != null && intent.getStringExtra(MainActivity.SYMBOL_SELECTED_EXTRA) != null){
            mSelectedSymbol = intent.getStringExtra(MainActivity.SYMBOL_SELECTED_EXTRA);
            getSupportActionBar().setTitle(getString(R.string.stock_trend_title, mSelectedSymbol));
            Bundle bundle = new Bundle();
            bundle.putString(SYMBOL_EXTRA, mSelectedSymbol);
            mLayout = (FrameLayout) findViewById(R.id.graph_container);
            getSupportLoaderManager().initLoader(QUOTE_HISTORY_LOADER_ID, bundle, this);
        }
    }

    //  RUBIC PART I
    //  Implement loader callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case QUOTE_HISTORY_LOADER_ID:
                return new CursorLoader(
                        StockTrendActivity.this,
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        Contract.Quote.COLUMN_SYMBOL + " = ?",
                        new String[]{args.getString(SYMBOL_EXTRA)},
                        null
                );
            default:
                throw new UnsupportedOperationException("Loader : " + id + " not implemented!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0){
            Timber.d("Data not available for displaying graph");
        } else {
            mLayout.addView(buildTrendGraph(data));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private LineChart buildTrendGraph(Cursor cursor){
        LineChart trendChart = new LineChart(StockTrendActivity.this);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
            float currentValue = cursor.getFloat(Contract.Quote.POSITION_PRICE);

            ArrayList<Entry> entries = StockUtils.fromStockHistoryToPoint(history, currentValue);

            LineDataSet lineDataSet = new LineDataSet(
                    entries, getString(R.string.stock_trend_label, mSelectedSymbol)
            );

            lineDataSet.setColor(
                    ContextCompat.getColor(StockTrendActivity.this,R.color.material_blue_500)
            );

            lineDataSet.setCircleColor(
                    ContextCompat.getColor(StockTrendActivity.this,R.color.material_blue_500)
            );

            lineDataSet.setCircleColorHole(
                    ContextCompat.getColor(StockTrendActivity.this,R.color.material_blue_500)
            );


            LineData lineData = new LineData(lineDataSet);
            lineData.setValueTextColor(
                    ContextCompat.getColor(
                            StockTrendActivity.this, R.color.secondaryText)
            );


            trendChart.setData(lineData);
            trendChart.setContentDescription(getString(R.string.stock_trend_content_description, mSelectedSymbol));
            trendChart.getXAxis().setDrawLabels(false);

            trendChart.getXAxis().setTextColor(
                    ContextCompat.getColor(
                            StockTrendActivity.this, R.color.secondaryText)
            );

            trendChart.getAxisLeft().setTextColor(
                    ContextCompat.getColor(
                            StockTrendActivity.this, R.color.secondaryText)
            );

            trendChart.getAxisRight().setTextColor(
                    ContextCompat.getColor(
                            StockTrendActivity.this, R.color.secondaryText)
            );

            Description description = new Description();
            description.setText("");

            trendChart.setDescription(description);
            trendChart.getLegend().setTextColor(
                    ContextCompat.getColor(
                            StockTrendActivity.this, R.color.secondaryText)
            );
            trendChart.invalidate();


        }

        return trendChart;
    }
}
