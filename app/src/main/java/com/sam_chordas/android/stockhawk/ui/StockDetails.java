package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.HistoricalData;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.service.SymbolParcelable;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hemal on 21/4/16.
 */
public class StockDetails extends AppCompatActivity implements HistoricalData.HistoricalDataCallback {

    private static final String TAG = StockDetails.class.getSimpleName();

    HistoricalData historicalData;

    ArrayList<SymbolParcelable> symbolParcelables = null;

    @Bind(R.id.lineChart_activity_line_graph)
    LineChart lineChart;

    @Bind(R.id.toolbar_activity_line_graph)
    Toolbar toolbar;

    @Bind(R.id.ll_activity_line_graph)
    LinearLayout linearLayout;

    String symbol = "";
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        ButterKnife.bind(this);

        symbol = getIntent().getStringExtra("symbol_name");

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        historicalData = new HistoricalData(this, this);
        historicalData.getHistoricalData(symbol);

    }


    @Override
    public void onSuccess(ArrayList<SymbolParcelable> sp) {

        this.symbolParcelables = sp;

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xvalues = new ArrayList<>();

        for (int i = 0; i < this.symbolParcelables.size(); i++) {

            SymbolParcelable symbolParcelable = this.symbolParcelables.get(i);
            double yValue = symbolParcelable.close;

            xvalues.add(Utils.convertDate(symbolParcelable.date));
            entries.add(new Entry((float) yValue, i));
        }


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelsToSkip(4);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        YAxis left = lineChart.getAxisLeft();
        left.setEnabled(true);
        left.setLabelCount(5, true);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.getLegend().setTextSize(16f);

        LineDataSet dataSet = new LineDataSet(entries, symbol);
        LineData lineData = new LineData(xvalues, dataSet);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setData(lineData);
    }

    @Override
    public void onFailure() {
        String errorMessage = "";

        @HistoricalData.HistoricalDataStatuses
        int status = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.historicalDataStatus), -1);

        switch (status) {
            case -1:
                //In most cases this would not be called!
                errorMessage += "No errors!";
                break;
            case HistoricalData.STATUS_ERROR_JSON:
                errorMessage += "Looks like it's our fault!";
                break;
            case HistoricalData.STATUS_ERROR_NO_NETWORK:
                errorMessage += "Internet Connection is required!";
                break;
            case HistoricalData.STATUS_ERROR_PARSE:
                errorMessage += "Unprecedented errors have happened!";
                break;
            case HistoricalData.STATUS_ERROR_UNKNOWN:
                errorMessage += "Can't identify the problem here!";
                break;
            case HistoricalData.STATUS_ERROR_SERVER:
                errorMessage += "The Yahoo! Server is not responding properly!";
                break;
            case HistoricalData.STATUS_OK:
                errorMessage += "No error has occurred!";
                break;
            default:
                break;
        }

//        Snackbar.make(linearLayout, R.string.no_data_show + errorMessage, Snackbar.LENGTH_INDEFINITE).show();


        final Snackbar snackbar = Snackbar
                .make(linearLayout, getString(R.string.no_data_show) + errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historicalData.getHistoricalData(symbol);
                    }
                })
                .setActionTextColor(Color.GREEN);

        View subview = snackbar.getView();
        TextView tv = (TextView) subview.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.RED);
        snackbar.show();
    }
}
