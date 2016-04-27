package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hemal on 21/4/16.
 */
public class StockDetails extends AppCompatActivity {

    @Bind(R.id.lineChart_activity_line_graph)
    LineChart lineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        ButterKnife.bind(this);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xvalues = new ArrayList<>();

        for(int i = 0; i < 10; i++){

            int yValue = (i%2==0)? (i*10) : (i*3);

            xvalues.add("Month " + (i+1));
            entries.add(new Entry(yValue*1f, i));
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
        LineData lineData = new LineData(xvalues ,dataSet);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setData(lineData);
        lineChart.setDescription("Hello this is some description.!");
        lineChart.animateXY(3000, 3000);
    }
}
