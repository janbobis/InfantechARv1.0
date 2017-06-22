package com.infantechar.application;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CouponActivity extends AppCompatActivity {

    boolean isQRCodeFitToScreen;
    boolean isBarCodeFitToScreen;
    boolean isNFCFitToScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String message = bundle.getString("myString");

        Log.d("MEssage", message);

        ImageView coupon = (ImageView) findViewById(R.id.image_coupon);
        coupon.setImageResource(R.drawable.bogof);

        final TextView redeemCode = (TextView) findViewById(R.id.code_coupon);
        redeemCode.setText(message);
        redeemCode.setBackgroundColor(Color.BLUE);
//        redeemCode.

        final ImageView qrCode = (ImageView) findViewById(R.id.qr_coupon);
        final ImageView barCode = (ImageView) findViewById(R.id.barcode_coupon);
        final ImageView nfc = (ImageView) findViewById(R.id.nfc_button);

        //default small size
        final LinearLayout.LayoutParams defaultSmall = new LinearLayout.LayoutParams(300, 300);

        qrCode.setImageResource(R.drawable.qrcode);
        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isQRCodeFitToScreen){
                    isQRCodeFitToScreen = false;
                    qrCode.setLayoutParams(defaultSmall);

                    qrCode.setAdjustViewBounds(true);
                    barCode.setVisibility(View.VISIBLE);
                    nfc.setVisibility(View.VISIBLE);
                }else{
                    isQRCodeFitToScreen = true;

                    qrCode.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    qrCode.setScaleType(ImageView.ScaleType.FIT_XY);

                    barCode.setVisibility(View.GONE);
                    nfc.setVisibility(View.GONE);
                }
            }
        });



        barCode.setImageResource(R.drawable.barcode);
        barCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBarCodeFitToScreen){
                    isBarCodeFitToScreen = false;
                    barCode.setLayoutParams(defaultSmall);
                    barCode.setAdjustViewBounds(true);
                    qrCode.setVisibility(View.VISIBLE);
                    nfc.setVisibility(View.VISIBLE);

                }else{
                    isBarCodeFitToScreen = true;
                    barCode.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    barCode.setScaleType(ImageView.ScaleType.FIT_XY);

                    qrCode.setVisibility(View.GONE);
                    nfc.setVisibility(View.GONE);
                }
            }
        });


        nfc.setImageResource(R.drawable.nfc);
        nfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNFCFitToScreen){
                    isNFCFitToScreen = false;
                    nfc.setLayoutParams(defaultSmall);
                    nfc.setAdjustViewBounds(true);

                    qrCode.setVisibility(View.VISIBLE);
                    barCode.setVisibility(View.VISIBLE);
                }else{
                    isNFCFitToScreen = true;
                    nfc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    nfc.setScaleType(ImageView.ScaleType.FIT_XY);

                    qrCode.setVisibility(View.GONE);
                    barCode.setVisibility(View.GONE);
                }
            }
        });


//        GridView gridView = (GridView) findViewById(R.id.couponGridView);
//        CouponGridViewAdapter adapter = new CouponGridViewAdapter(this, R.layout.coupon_item_layout, new ArrayList());
//        gridView.setAdapter(adapter);


    }

}
