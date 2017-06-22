package com.infantechar.application.ar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.infantechar.application.ApplicationCallService;
import com.infantechar.application.BitmapEnum;
import com.infantechar.application.MapsActivity;
import com.infantechar.application.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class OverlayView extends View implements SensorEventListener,
        LocationListener {

    public static final String DEBUG_TAG = "OverlayView Log";

    private final Context context;
    private Handler handler;

    private final static Location targetLocation = new Location("manual");

    static String DOMAIN = "http://34.209.105.118";
    static String URL = DOMAIN + "/getavailablepromoitemswithinarea/";
    static float PROXIMITY = 100.0f;    // maximum distance of objects from current location to be displayed

    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";

    private LocationManager locationManager = null;
    private SensorManager sensors = null;

    private Location lastLocation;
    private float[] lastAccelerometer;
    private float[] lastCompass;

    private float verticalFOV;
    private float horizontalFOV;

    private boolean isAccelAvailable;
    private boolean isCompassAvailable;
    private boolean isGyroAvailable;
    private Sensor accelSensor;
    private Sensor compassSensor;
    private Sensor gyroSensor;

    private TextPaint contentPaint;

    private Paint targetPaint;

    Map<String, Bitmap> bmpStore = new HashMap<String, Bitmap>();

    //int filterCtr = 0;
    float currX = 0;
    float currY = 0;
    float nextY = 0;

    public static boolean hasStarted = false;
    boolean ctrStarted = false;
    int filterCtr;

    List<JSONObject> responseList = new ArrayList<JSONObject>();

    public OverlayView(Context context) {
        super(context);
        this.context = context;
        this.handler = new Handler();
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        sensors = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        startSensors();
        startGPS();
        initializeImages();

        // get some camera parameters
        Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        verticalFOV = params.getVerticalViewAngle();
        horizontalFOV = params.getHorizontalViewAngle();
        camera.release();

        // paint for text
        contentPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextAlign(Align.LEFT);
        contentPaint.setTextSize(40);
        contentPaint.setColor(Color.YELLOW);

    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){

        }
    }

    private void startSensors() {
        isAccelAvailable = sensors.registerListener(this, accelSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isCompassAvailable = sensors.registerListener(this, compassSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isGyroAvailable = sensors.registerListener(this, gyroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void startGPS() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "Best provider: " + best);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(best, 50, 0, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        filterCtr = 0;
        float curBearingToTarget = 0.0f;
        float rotation[] = new float[9];
        float identity[] = new float[9];

        StringBuilder text = new StringBuilder(accelData).append("\n");

        if (lastLocation != null) {
            text.append(
                    String.format("GPS = (%.8f, %.8f) @ (%.2f meters up)",
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude(),
                            lastLocation.getAltitude())).append("\n");

            if(responseList.size()>0){
                for(JSONObject obj : responseList){
                    String keyword = "";
                    try {
                        double latitude = (Double) obj.get("latitude");
                        double longitude = (Double) obj.get("longitude");
                        keyword = ((String) obj.get("keyword")).toLowerCase();
                        keyword = BitmapEnum.getEnum(keyword).getKeyword();
                        targetLocation.setLatitude(latitude);
                        targetLocation.setLongitude(longitude);
                        curBearingToTarget = lastLocation.bearingTo(targetLocation);

                        if (lastAccelerometer != null && lastCompass != null) {
                            boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                                    identity, lastAccelerometer, lastCompass);

                            if (gotRotation) {
                                processARPromotion(canvas, rotation, curBearingToTarget, targetLocation, text, keyword, keyword.toLowerCase() + "_high");
                            }
                        }

                    } catch (JSONException e) {
                        Toast.makeText(context, "JSONException encountered", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Exception encountered on : " + keyword + "\nError: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                text.append(String.format("No promotion items found within your area (radius of %.2f meters)", PROXIMITY));
            }
        } else {
            text.append("Waiting for GPS signal and server resposne to complete");
        }


        canvas.save();
        canvas.translate(15.0f, 15.0f);
        StaticLayout textBox = new StaticLayout(text.toString(), contentPaint,
                560, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        textBox.draw(canvas);
        canvas.restore();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(DEBUG_TAG, "onAccuracyChanged");

    }

    public void onSensorChanged(SensorEvent event) {
        StringBuilder msg = new StringBuilder(event.sensor.getName())
                .append(" ");
        for (float value : event.values) {
            msg.append("[").append(String.format("%.3f", value)).append("]");
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values.clone();
                accelData = msg.toString();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastCompass = event.values.clone();
                compassData = msg.toString();
                break;
        }
        if(null!=lastLocation && !hasStarted) {
            getPromoItemsWithinProximity(lastLocation);
        }
        this.invalidate();
    }

    public void onLocationChanged(Location location) {
        // store it off for use when we need it
        lastLocation = location;
        if(null!=lastLocation && !hasStarted) {
            getPromoItemsWithinProximity(lastLocation);
        }
    }

    public void onProviderDisabled(String provider) {
        // ...
    }

    public void onProviderEnabled(String provider) {
        // ...
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ...
    }

    // this is not an override
    public void onPause() {
        locationManager.removeUpdates(this);
        sensors.unregisterListener(this);
    }

    // this is not an override
    public void onResume() {
        startSensors();
        startGPS();
    }

    public void initializeImages(){
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.adidas);
        bmpStore.put("adidas", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.adidas_high);
        bmpStore.put("adidas_high", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nike);
        bmpStore.put("nike", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.nike_high);
        bmpStore.put("nike_high", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.jollibee);
        bmpStore.put("jollibee", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.jollibee_high);
        bmpStore.put("jollibee_high", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.mcdonalds);
        bmpStore.put("mcdonalds", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.mcdonalds_high);
        bmpStore.put("mcdonalds_high", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bpi);
        bmpStore.put("bpi", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bpi_high);
        bmpStore.put("bpi_high", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.citi);
        bmpStore.put("citi", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.citi_high);
        bmpStore.put("citi_high", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.infantechar);
        bmpStore.put("default", bmp);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.infantechar_high);
        bmpStore.put("default_high", bmp);
    }

    public void processARPromotion(Canvas canvas, float[] rotation, float bearingFromCurrLoc, Location location, StringBuilder text, String promoName, String bitmapToUse){
        float cameraRotation[] = new float[9];
        SensorManager.remapCoordinateSystem(rotation,
                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                cameraRotation);
        float orientation[] = new float[3];
        SensorManager.getOrientation(cameraRotation, orientation);

        float distance = lastLocation.distanceTo(location);
        text.append(
                String.format("Distance to %s: %.3f meters", promoName, distance))
                .append("\n");

        canvas.save();
        canvas.rotate((float)(0.0f- Math.toDegrees(orientation[2])));

        float dx = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - bearingFromCurrLoc));
        float dy = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

        /*if(hasStarted = false) {
            currX = dx;
            currY = dy;
            hasStarted = true;
        } else {
            currX = (currX + dx) /2;
            currY = (currY + dy) /2;
        }*/

        if(filterCtr%5==0){
            if(ctrStarted = false){
                ctrStarted = true;
                currY = dy;
            } else {
                nextY /= 5;
                currY = nextY;
                nextY = 0;
            }
        }

        nextY+=dy;
        filterCtr++;


        //canvas.translate(0.0f, 0.0f-dy);
        canvas.translate(0.0f, 0.0f-currY);
        canvas.translate(0.0f-dx, 0.0f);

        Bitmap bmp = bmpStore.get(bitmapToUse);
        canvas.drawBitmap(bmp, (canvas.getWidth()-bmp.getWidth())/2, (canvas.getHeight()-bmp.getWidth())/2, null);
        canvas.restore();
    }

    public List<JSONObject> getResponseList() {
        return responseList;
    }

    public void setResponseList(List<JSONObject> responseList) {
        this.responseList = responseList;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    private void getPromoItemsWithinProximity(Location location){
        String url = URL + location.getLatitude() + "," + location.getLongitude() + "," + PROXIMITY;

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept", "application/json"));
        ApplicationCallService.get(context, url, headers.toArray(new Header[headers.size()]), null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                responseList.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject obj = response.getJSONObject(i);
                        responseList.add(obj);
                    } catch (JSONException e) {
                        Toast.makeText(context, "JSONException encountered", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Exception encountered: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                hasStarted = true;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, "Failed to call the service: " + errorResponse.toString(), Toast.LENGTH_SHORT).show();
            }


        });
    }

}
