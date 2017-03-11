package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by stefanopernat on 10/03/17.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockViewsFactory(this, intent);
    }


}
