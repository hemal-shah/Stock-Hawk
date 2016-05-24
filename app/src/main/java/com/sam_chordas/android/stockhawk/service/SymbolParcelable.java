package com.sam_chordas.android.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hemal on 22/5/16.
 * Now we don't want the entire data returned from the api.
 * We need to only use the date and close factors.
 */
public class SymbolParcelable implements Parcelable{

    public String date;
    public double close;

    protected SymbolParcelable(Parcel in) {
        date = in.readString();
        close = in.readDouble();
    }

    public SymbolParcelable(String date, double close) {
        this.date = date;
        this.close = close;
    }

    public static final Creator<SymbolParcelable> CREATOR = new Creator<SymbolParcelable>() {
        @Override
        public SymbolParcelable createFromParcel(Parcel in) {
            return new SymbolParcelable(in);
        }

        @Override
        public SymbolParcelable[] newArray(int size) {
            return new SymbolParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeDouble(close);
    }
}
