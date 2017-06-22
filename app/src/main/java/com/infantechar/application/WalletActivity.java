package com.infantechar.application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.infantechar.application.ar.OverlayView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class WalletActivity extends AppCompatActivity {

    List<String> titles = new ArrayList<String>();

    String DOMAIN = "http://34.209.105.118";
    String URL = DOMAIN + "/getcapturedpromoitemsbyuser";
    String[] resource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        OverlayView.hasStarted = false;


        //connect to ws
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));

        ApplicationCallService.get(this, URL + "/" + LoginActivity.getUserId(), headers.toArray(new Header[headers.size()]), null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("onsuccess1",response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONArray responseString) {
                super.onSuccess(statusCode, headers, responseString);
                Log.d("onsuccess2",responseString.toString());

                try{
                    for(int i=0; i<responseString.length(); i++){
                        JSONObject obj = (JSONObject) responseString.get(i);
                        String title = (String) obj.get("keyword");
                        title += ":" + (String) obj.get("name");
                        titles.add(title);
                        Log.d("title " + i, title);
                    }
                    resource = titles.toArray(new String[titles.size()]);

                    CustomList customAdapter = new CustomList(WalletActivity.this, resource);
                    ListView listView = (ListView) findViewById(R.id.myList);
                    listView.setAdapter(customAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(WalletActivity.this, "selected " + resource[position], Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("myString", "REDEEM CODE: 10293844");//todo: put object
                            intent.putExtras(bundle);
                            intent.setClass(WalletActivity.this, CouponActivity.class);
                            startActivity(intent);
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("fail",errorResponse.toString());
            }
        });

        //listView.setOnItemClickListener();
    }
}
