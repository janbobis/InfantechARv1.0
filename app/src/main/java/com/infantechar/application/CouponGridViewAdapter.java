package com.infantechar.application;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eldon on 5/24/2017.
 */

public class CouponGridViewAdapter extends ArrayAdapter {

    private Context context;
    private int resource;
    private List data = new ArrayList();

    public CouponGridViewAdapter(@NonNull Context context, @LayoutRes int resource, List data) {
        super(context, resource, data);
        this.context = context;
        this.resource = resource;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        CouponViewHolder holder = null;

        if(null == row){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.coupon_item_layout, parent, false);
            holder = new CouponViewHolder();
            holder.coupon = (ImageView) row.findViewById(R.id.image_coupon);
            holder.redeemCode = (TextView) row.findViewById(R.id.code_coupon);
            holder.qrCode = (ImageView) row.findViewById(R.id.qr_coupon);
            holder.barcode = (ImageView) row.findViewById(R.id.barcode_coupon);
            holder.nfcButton = (ImageView) row.findViewById(R.id.nfc_button);
            row.setTag(holder);
        }else {
            holder =(CouponViewHolder) row.getTag();
        }

        holder.coupon.setImageResource(R.drawable.bogof);
        holder.redeemCode.setText("Redeem code: 123");
        holder.qrCode.setImageResource(R.drawable.qrcode);
        holder.barcode.setImageResource(R.drawable.barcode);
        holder.nfcButton.setImageResource(R.drawable.nfc);


        return row;
    }

    static class CouponViewHolder {
        ImageView coupon;
        TextView redeemCode;
        ImageView qrCode;
        ImageView barcode;
        ImageView nfcButton;
    }
}
