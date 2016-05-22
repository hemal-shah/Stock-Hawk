package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.HistoricalData;
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
public class StockDetails extends AppCompatActivity implements HistoricalData.HistoricalDataCallback{

    private static final String TAG = StockDetails.class.getSimpleName();

    HistoricalData historicalData;

    @Bind(R.id.lineChart_activity_line_graph)
    LineChart lineChart;

    @Bind(R.id.toolbar_activity_line_graph)
    Toolbar toolbar;

    @Bind(R.id.ll_activity_line_graph)
    LinearLayout linearLayout;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        historicalData = new HistoricalData(this, this);
        historicalData.getHistoricalData("fb");

    }


    public void loadDataIntoGraph(){
}


    @Override
    public void onSuccess(ArrayList<SymbolParcelable> symbolParcelables) {

        Log.i(TAG, "loadDataIntoGraph: " + symbolParcelables.toString());

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xvalues = new ArrayList<>();

        for (int i = 0; i < symbolParcelables.size(); i++) {

            SymbolParcelable symbolParcelable = symbolParcelables.get(i);
            double yValue = symbolParcelable.close;

            xvalues.add(symbolParcelable.date);
            entries.add(new Entry((float)yValue, i));
        }


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelsToSkip(2);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        YAxis left = lineChart.getAxisLeft();
        left.setEnabled(true);
        left.setLabelCount(5, true);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.getLegend().setTextSize(16f);

        LineDataSet dataSet = new LineDataSet(entries, "Stock Name");
        LineData lineData = new LineData(xvalues, dataSet);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setData(lineData);
        lineChart.setDescription("Hello this is some description.!");
//        lineChart.animateXY(3000, 3000);

    }

    @Override
    public void onFailure(String error) {
        //TODO add options to reload the data.!
        Snackbar.make(linearLayout, "Sorry! Couldn't fetch data!" + error, Snackbar.LENGTH_INDEFINITE).show();
    }
}
