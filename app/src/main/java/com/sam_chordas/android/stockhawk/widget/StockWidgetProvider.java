package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by hemal on 25/5/16.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        for(int i = 0; i< appWidgetIds.length; i++){
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);
            remoteViews.setRemoteAdapter(appWidgetIds[i], intent);

            remoteViews.setEmptyView(R.id.lv_stock_widget_layout, R.id.emptyView_acitivity_my_stocks);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
