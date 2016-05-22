package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.sam_chordas.android.stockhawk.ui.StockDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hemal on 22/5/16.
 */
public class HistoricalData {

    private static final String TAG = HistoricalData.class.getSimpleName();
    Context context;
    HistoricalDataCallback callback;
    ArrayList<SymbolParcelable> symbolParcelables;

    final String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
    final String END_URL = "/chartdata;type=quote;range=1y/json";

    public HistoricalData(Context context, StockDetails object){
        this.context = context;
        this.callback = object;
    }

    public void getHistoricalData(String symbol){

        String URL = BASE_URL + symbol + END_URL;

        final StringRequest request = new StringRequest(
                Request.Method.GET,
                URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response != null){
                            symbolParcelables = new ArrayList<>();
                            try {
                                Log.i(TAG, "onResponse: " + response);
                                String json = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
                                JSONObject mainObject = new JSONObject(json);
                                Log.i(TAG, "onResponse: json object formed is " + mainObject.toString());
                                JSONObject jsonObject = mainObject.getJSONObject("meta");
                                Log.i(TAG, "onResponse: meta info : " + jsonObject.toString());
                                String company_name = jsonObject.getString("Company-Name");
                                JSONArray series_data = mainObject.getJSONArray("series");
                                for(int i = 0; i< series_data.length(); i += 10){
                                    JSONObject singleObject = series_data.getJSONObject(i);
                                    String date = singleObject.getString("Date");
                                    double close = singleObject.getDouble("close");
                                    double high = singleObject.getDouble("high");
                                    double low = singleObject.getDouble("low");
                                    double open = singleObject.getDouble("open");
                                    int volume = singleObject.getInt("volume");
                                    symbolParcelables.add(new SymbolParcelable(company_name, date, close, high, low, open, volume));
                                }
                                if(callback != null){
                                    Log.i(TAG, "onResponse: data is " + symbolParcelables.toString());
                                    callback.onSuccess(symbolParcelables);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(callback != null){
                            Log.i(TAG, "onErrorResponse: failed to load data!");
                            callback.onFailure(error.toString());
                        }
                    }
                }
        );
        AppController.getInstance().addToRequestQueue(request, TAG);
    }


    public interface HistoricalDataCallback{
        void onSuccess(ArrayList<SymbolParcelable> list);
        void onFailure(String error);
    }
}
