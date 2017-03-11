package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockTrendActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by stefanopernat on 11/03/17.
 */

public class StockViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mStockCursor;
    private Context mContext;
    private Intent mIntent;

    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    public StockViewsFactory(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    private void retriveCursorWithData(){

        if (mStockCursor != null) {
            mStockCursor.close();
            mStockCursor = null;
        }

        //  Clear the calling identity
        final long identityToken = Binder.clearCallingIdentity();

        mStockCursor =
            mContext.getContentResolver().query(
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL
            );

        //  Restore the calling identity
        Binder.restoreCallingIdentity(identityToken);
    }


    @Override
    public void onCreate() {
        retriveCursorWithData();
    }

    @Override
    public void onDataSetChanged() {
        retriveCursorWithData();
    }

    @Override
    public void onDestroy() {
        if (mStockCursor != null){
            mStockCursor.close();
            mStockCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mStockCursor == null ? 0 : mStockCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (mStockCursor == null || position == AdapterView.INVALID_POSITION
                || !mStockCursor.moveToPosition(position)) {

            return null;
        }

        //  Create the remoteViews
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_quote);

        // setting the values for all views
        views.setTextViewText(R.id.symbol, mStockCursor.getString(Contract.Quote.POSITION_SYMBOL));
        views.setContentDescription(R.id.symbol, mStockCursor.getString(Contract.Quote.POSITION_SYMBOL));


        float rawAbsoluteChange = mStockCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = mStockCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.change, change);
        } else {
            views.setTextViewText(R.id.change, percentage);
        }

        //  Create Intent to launch TrendActivity
        Intent trendActivityIntent = new Intent(mContext, StockTrendActivity.class);
        trendActivityIntent.putExtra(
                MainActivity.SYMBOL_SELECTED_EXTRA,
                mStockCursor.getString(Contract.Quote.POSITION_SYMBOL)
        );

        views.setOnClickFillInIntent(R.id.item_view, trendActivityIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mStockCursor.moveToPosition(position)){
            return mStockCursor.getLong(Contract.Quote.POSITION_ID);
        }

        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
