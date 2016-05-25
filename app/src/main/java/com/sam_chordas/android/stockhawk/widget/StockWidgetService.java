package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by hemal on 25/5/16.
 */
public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return null;
    }


    class StockRVFactory implements RemoteViewsFactory{

        private final String TAG = StockRVFactory.class.getSimpleName();

        private int appWidgetId;
        private Context context;
        private Cursor cursor;


        public StockRVFactory(Context context, Intent intent){
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        @Override
        public void onCreate() {
            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);

            if(cursor.moveToPosition(position)){
                remoteViews.setTextViewText(R.id.stock_symbol, cursor.getString(1));
                remoteViews.setTextViewText(R.id.bid_price, cursor.getString(2));
                remoteViews.setTextViewText(R.id.change, cursor.getString(4));
            } else{
                Log.e(TAG, "getViewAt: error!");
            }

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
