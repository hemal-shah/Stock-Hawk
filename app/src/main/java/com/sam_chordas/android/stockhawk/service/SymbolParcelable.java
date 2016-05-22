package com.sam_chordas.android.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hemal on 22/5/16.
 */
public class SymbolParcelable implements Parcelable{

    public String company_name, date;
    public double close, high, low, open;
    public int volume;

    public SymbolParcelable(String company_name, String date, double close, double high, double low, double open, int volume) {
        this.company_name = company_name;
        this.date = date;
        this.close = close;
        this.high = high;
        this.low = low;
        this.open = open;
        this.volume = volume;
    }

    protected SymbolParcelable(Parcel in) {
        company_name = in.readString();
        date = in.readString();
        close = in.readDouble();
        high = in.readDouble();
        low = in.readDouble();
        open = in.readDouble();
        volume = in.readInt();
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
        dest.writeString(company_name);
        dest.writeString(date);
        dest.writeDouble(close);
        dest.writeDouble(high);
        dest.writeDouble(low);
        dest.writeDouble(open);
        dest.writeInt(volume);
    }
}
