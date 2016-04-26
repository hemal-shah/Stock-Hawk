package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.sam_chordas.android.stockhawk.R;

import org.hogel.android.linechartview.LineChartView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemal on 21/4/16.
 */
public class StockDetails extends AppCompatActivity {

    private static final double MAX_Y = 100;
    private LineChartView chartView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);


        chartView  = (LineChartView) findViewById(R.id.linechart);
        chartView.setManualMinY(0);

        List<LineChartView.Point> points = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int y = (int) (Math.random() * MAX_Y);
            points.add(new LineChartView.Point(i, y));
        }
        chartView.setPoints(points);
    }
}
