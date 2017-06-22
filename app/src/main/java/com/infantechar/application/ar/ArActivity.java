package com.infantechar.application.ar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.infantechar.application.ApplicationCallService;
import com.infantechar.application.BitmapEnum;
import com.infantechar.application.LoginActivity;
import com.infantechar.application.MapsActivity;
import com.infantechar.application.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class ArActivity extends Activity {
    private OverlayView arContent;

    static String DOMAIN = "http://34.209.105.118";
    static String URL = DOMAIN + "/capturepromoitem/";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.ar_main);
        
        FrameLayout arViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);
        
        ArDisplayView arDisplay = new ArDisplayView(getApplicationContext(), this);
        arViewPane.addView(arDisplay);

        arContent = new OverlayView(getApplicationContext());
        addViewListener();
        arViewPane.addView(arContent);
   }

   public void addViewListener(){
       arContent.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               List<JSONObject> responseList = arContent.getResponseList();
               Location lastLocation = arContent.getLastLocation();

               StringBuffer sb = new StringBuffer();

               for(JSONObject obj : responseList) {
                   String name = "";
                   try {
                       double latitude = (Double) obj.get("latitude");
                       double longitude = (Double) obj.get("longitude");
                       Integer promotionItemId = (Integer) obj.get("promotionItemId");
                       name = ((String) obj.get("keyword"));
                       name += ":" + ((String) obj.get("name"));
                       Location itemLocation = new Location("manual");
                       itemLocation.setLatitude(latitude);
                       itemLocation.setLongitude(longitude);
                       Integer userId = (Integer) LoginActivity.getUserId();

                       float dist = lastLocation.distanceTo(itemLocation);
                       if(dist<=100.0f){
                           //sb.append(keyword + ":" + name + ", distance: " + dist + "\n");
                           showYesNoDialog(name, promotionItemId, userId);
                           return false;
                       }
                   } catch (Exception e){
                       e.printStackTrace();
                   }
               }

               //Toast.makeText(ArActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
               return false;
           }
       });
   }

	@Override
	protected void onPause() {
		arContent.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		arContent.onResume();
        arContent.hasStarted = false;
	}

    public void showOkDialog(String dialog){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ArActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ArActivity.this);
        }

        builder.setTitle("Capture Promo Item")
                .setMessage(dialog)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void showYesNoDialog(String name, final long promotionItemId, final long userId){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ArActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ArActivity.this);
        }

        builder.setTitle("Capture Promo Item")
                .setMessage(String.format("Capture the item %s and place in your wallet?\nDetails\nName: %s\nPromo Id: %d\nUser Id: %d", name, name, promotionItemId, userId))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        List<Header> headers = new ArrayList<>();
                        headers.add(new BasicHeader("Accept", "application/json"));
                        ApplicationCallService.get(ArActivity.this, URL + "/" + promotionItemId + "," + userId, headers.toArray(new Header[headers.size()]), null, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {

                                    int code = (Integer) response.get("statusCode");
                                    String message = (String) response.get("statusMessage");

                                    if(code==0){
                                        showOkDialog("Item successfully placed in wallet");
                                        OverlayView.hasStarted = false; // reset the contents of OverlayView
                                    } else {
                                        showOkDialog("Error encountered adding item to wallet");
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(ArActivity.this, "JSONException encountered", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(ArActivity.this, "Exception encountered: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Toast.makeText(ArActivity.this, "Failed to call the service: " + errorResponse.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

}