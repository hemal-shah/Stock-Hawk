package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.squareup.okhttp.internal.Util;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = MyStocksActivity.class.getSimpleName();

    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;
    @Bind(R.id.cdl_activity_my_stocks)
    CoordinatorLayout cdl;
    @Bind(R.id.rv_activity_my_stocks)
    RecyclerView recyclerView;
    @Bind(R.id.fab_activity_my_stocks)
    FloatingActionButton fab;
    @Bind(R.id.toolbar_activity_my_stocks)
    Toolbar toolbar;
    @Bind(R.id.emptyView_acitivity_my_stocks)
    TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_my_stocks);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        isConnected = Utils.isNetworkAvailable(this);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                noNetworkSnack();
            }
        }


        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //Initializing the loader from here.
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        //TODO : do something on item click
                        startActivity(new Intent(mContext, StockDetails.class));
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);
        emptyViewBehavior();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }

    private void noStockFoundSnack() {
        Snackbar.make(cdl, "Stock Not Found!", Snackbar.LENGTH_SHORT).show();
    }


    /**
     * Method to show error message when the data is not available.
     */
    public void emptyViewBehavior() {

        if (mCursorAdapter.getItemCount() <= 0) {
            //The data is not available


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            @StockTaskService.StockStatuses int stockStatus = sp.getInt(getString(R.string.stockStatus), -1);

            String message = getString(R.string.data_not_available);

            switch (stockStatus) {
                case StockTaskService.STATUS_OK:
                    message += getString(R.string.string_status_ok);
                    break;

                case StockTaskService.STATUS_NO_NETWORK:
                    message += getString(R.string.string_status_no_network);
                    break;

                case StockTaskService.STATUS_ERROR_JSON:
                    message += getString(R.string.string_error_json);
                    break;

                case StockTaskService.STATUS_SERVER_DOWN:
                    message += getString(R.string.string_server_down);
                    break;

                case StockTaskService.STATUS_SERVER_ERROR:
                    message += getString(R.string.string_error_server);
                    break;

                case StockTaskService.STATUS_UNKNOWN:
                    message += getString(R.string.string_status_unknown);
                    break;
                default:
                    break;

            }

            emptyText.setText(message);


            recyclerView.setVisibility(View.INVISIBLE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            //the data is available
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        emptyViewBehavior();
    }

    public void noNetworkSnack() {
        StockTaskService.setStockStatus(mContext, StockTaskService.STATUS_NO_NETWORK);
        Snackbar.make(cdl, R.string.network_snack, Snackbar.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_change_units:
                Utils.showPercent = !Utils.showPercent;
                this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this,
                QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        emptyViewBehavior();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
        emptyViewBehavior();
    }


    @OnClick(R.id.fab_activity_my_stocks)
    public void fabClicked() {
        if (isConnected) {
            new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                    .content(R.string.content_test)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            // On FAB click, receive user input. Make sure the stock doesn't already exist
                            // in the DB and proceed accordingly
                            Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                    new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                    new String[]{input.toString()}, null);
                            if ( c != null &&  c.getCount() != 0) {
                                Snackbar.make(cdl, R.string.already_saved, Snackbar.LENGTH_SHORT).show();
                                return;
                            } else {
                                // Add the stock to DB
                                mServiceIntent.putExtra("tag", "add");
                                mServiceIntent.putExtra("symbol", input.toString());
                                startService(mServiceIntent);
                            }
                        }
                    })
                    .show();
        } else {
            noNetworkSnack();
        }
    }

}
