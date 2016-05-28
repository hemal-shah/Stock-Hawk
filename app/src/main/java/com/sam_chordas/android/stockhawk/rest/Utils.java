package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static boolean showPercent = true;

    private static final String QUERY = "query";
    private static final String COUNT = "count";
    private static final String RESULTS = "results";
    private static final String QUOTE = "quote";
    private static final String CHANGE = "Change";
    private static final String BID = "Bid";
    private static final String SYMBOL = "symbol";
    private static final String CHANGE_IN_PERCENT = "ChangeinPercent";
    private static final String NULL = "null";

    public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(String JSON) throws JSONException {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        ContentProviderOperation cpo = null;

        jsonObject = new JSONObject(JSON);
        if (jsonObject != null && jsonObject.length() != 0) {
            jsonObject = jsonObject.getJSONObject(QUERY);
            int count = Integer.parseInt(jsonObject.getString(COUNT));
            if (count == 1) {
                jsonObject = jsonObject.getJSONObject(RESULTS)
                        .getJSONObject(QUOTE);
                cpo = buildBatchOperation(jsonObject);
                if (cpo != null) {
                    batchOperations.add(cpo);
                }

            } else {
                resultsArray = jsonObject.getJSONObject(RESULTS).getJSONArray(QUOTE);

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        cpo = buildBatchOperation(jsonObject);
                        if (cpo != null) {
                            batchOperations.add(cpo);
                        }

                    }
                }
            }
        }

        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }


    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws JSONException {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);

        if (!jsonObject.getString(CHANGE).equals(NULL) && !jsonObject.getString(BID).equals(NULL)) {
            String change = jsonObject.getString(CHANGE);
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(SYMBOL));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(BID)));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString(CHANGE_IN_PERCENT), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } else {
            return null;
        }
        return builder.build();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo availableNetwork = cm.getActiveNetworkInfo();
        return availableNetwork != null && availableNetwork.isConnectedOrConnecting();
    }



    public static String convertDate(String inputDate){
        StringBuilder outputFormattedDate = new StringBuilder();
        outputFormattedDate.append(inputDate.substring(6))
                .append("/")
                .append(inputDate.substring(4,6))
                .append("/")
                .append(inputDate.substring(2, 4));
        return outputFormattedDate.toString();
    }

}
