package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.StockDetails;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class HistoricalData {

    private static final String TAG = HistoricalData.class.getSimpleName();
    HistoricalDataCallback callback;
    ArrayList<SymbolParcelable> symbolParcelables;

    Context context;

    //to form the url for any symbol
    final String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
    final String END_URL = "/chartdata;type=quote;range=1y/json";


    //to parse the json data..
    private static final String JSON_SERIES = "series";
    private static final String JSON_DATE = "Date";
    private static final String JSON_CLOSE = "close";


    //to indicate errors incurred during parsing.
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_OK, STATUS_ERROR_JSON, STATUS_ERROR_NO_NETWORK, STATUS_ERROR_PARSE
            , STATUS_ERROR_SERVER, STATUS_ERROR_UNKNOWN})
    public @interface HistoricalDataStatuses {
    }

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR_JSON = 1;
    public static final int STATUS_ERROR_SERVER = 2;
    public static final int STATUS_ERROR_PARSE = 3;
    public static final int STATUS_ERROR_NO_NETWORK = 4;
    public static final int STATUS_ERROR_UNKNOWN = 5;


    public HistoricalData(Context context, StockDetails object) {
        this.context = context;
        this.callback = object;
    }

    /**
     * Helper method to retrieve data from yahoo's api
     * and parse it into appropriate data format.
     *
     * @param symbol Symbol for which historical data is to be retrieved and parsed.
     */
    public void getHistoricalData(String symbol) {

        String URL = BASE_URL + symbol + END_URL;

        final StringRequest request = new StringRequest(
                Request.Method.GET,
                URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            symbolParcelables = new ArrayList<>();
                            try {

                                /**
                                 * The retrieved data is in JSONP format.
                                 * so we need to strip off the outer function,
                                 * and then parse the json data inside that.
                                 */

                                String json = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
                                JSONObject mainObject = new JSONObject(json);
                                JSONArray series_data = mainObject.getJSONArray(JSON_SERIES);
                                for (int i = 0; i < series_data.length(); i += 10) {
                                    JSONObject singleObject = series_data.getJSONObject(i);
                                    String date = singleObject.getString(JSON_DATE);
                                    double close = singleObject.getDouble(JSON_CLOSE);
                                    symbolParcelables.add(new SymbolParcelable(date, close));
                                }
                                if (callback != null) {
                                    setHistoricalDataStatus(STATUS_OK);
                                    callback.onSuccess(symbolParcelables);
                                }
                            } catch (JSONException e) {
                                setHistoricalDataStatus(STATUS_ERROR_JSON);
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            setHistoricalDataStatus(STATUS_ERROR_NO_NETWORK);
                        } else if (error instanceof ServerError) {
                            setHistoricalDataStatus(STATUS_ERROR_SERVER);
                        } else if (error instanceof NetworkError) {
                            setHistoricalDataStatus( STATUS_ERROR_UNKNOWN);
                        } else if (error instanceof ParseError) {
                            setHistoricalDataStatus(STATUS_ERROR_PARSE);
                        }

                        if (callback != null) {
                            callback.onFailure();
                        }
                    }
                }
        );
        AppController.getInstance().addToRequestQueue(request, TAG);
    }


    public void setHistoricalDataStatus(@HistoricalDataStatuses int status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.historicalDataStatus), status);
        editor.apply();
    }

    /**
     * Interface to interact with the callee class to notify regarding success, or errors if any.
     */
    public interface HistoricalDataCallback {
        void onSuccess(ArrayList<SymbolParcelable> list);
        void onFailure();
    }
}
