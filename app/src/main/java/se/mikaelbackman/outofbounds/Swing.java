package se.mikaelbackman.outofbounds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.SoundPool.OnLoadCompleteListener;

import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.jni.LLACoordinate;

public class Swing extends Activity implements SensorEventListener {
    private static final int PERFECT_HIT=0;
    private static final int SLICE =1;
    private static final int HOOK =2;
    private static final int MISS =3;
    private static final float WEDGE =0.2f;
    private static final float IRON =0.6f;
    private static final float DRIVER =1.1f;
    private static final float MAX_DISTANCE = 250f;
    private SensorManager manager;
    private Sensor accelerometer;
    private Sensor gravitymeter;
    private float accelerometerData;
    private float[] gravityData = new float[2];
    private float maxG;
    private float closestGX;
    private float closestGY;
    private float stanceGX;
    private float stanceGY;
    private float speed;
    private int impact;
    private float club;
    private float maxSpeed;
    private float swingType;
    private boolean buttonPressed = false;
    private boolean swingStarted = false;
    private boolean hit = false;
    private float differenceGX;
    private float differenceGY;
    private float XAbs =100;
    private float YAbs = 100;
    private Location ballPosition;
    private Location flagPosition;
    private SoundPool soundPool;
    private int driverID;
    private int wedgeID;
    private int ironID;
    private int applauseID, winID, intheholeID, sliceID, hookID;
    private int missID;
    private boolean loaded;
    private float distanceToFlag;
    private double balllat, balllong, flaglat, flaglong;
    private int numberOfStrokes;
    private int totalLength;
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swing);
        ballPosition = new Location("GPS_PROVIDER");
        flagPosition = new Location("GPS_PROVIDER");


         v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Ta emot intent från AR-activity
        Intent intent = getIntent();
        balllat = intent.getDoubleExtra("ball_latitude", 0);
        balllong = intent.getDoubleExtra("ball_longitude", 0);
        flaglat = intent.getDoubleExtra("flag_latitude", 0);
        flaglong = intent.getDoubleExtra("flag_longitude", 0);
        numberOfStrokes = intent.getIntExtra("strokes", 0);
        totalLength = intent.getIntExtra("totalLength", totalLength);


        ballPosition.setLatitude(balllat);
        ballPosition.setLongitude(balllong);
        flagPosition.setLatitude(flaglat);
        flagPosition.setLongitude(flaglong);
        distanceToFlag = intent.getFloatExtra("distance", 0);
        int distanceInt = (int)(distanceToFlag + 0.5f);

        Button readybutton = (Button) findViewById(R.id.readytoswing_button);
        readybutton.setVisibility(View.GONE);
        TextView swingInfo = (TextView) findViewById(R.id.swingInfo);
        swingInfo.setVisibility(View.GONE);
        Button swingbutton= (Button) findViewById(R.id.ready_button);
        swingbutton.setEnabled(false);
        swingbutton.setClickable(false);

        TextView distancetext = (TextView) findViewById(R.id.distancetext);
        distancetext.setText(distanceInt + "m");

        if (numberOfStrokes == 0){
            LLACoordinate ball = new LLACoordinate(balllat,balllong,0,0);
            LLACoordinate flag = new LLACoordinate(flaglat, flaglong,0,0);

            double doubledist = MetaioCloudUtils.getDistanceBetweenTwoCoordinates(ball, flag);
            distanceInt = (int) (doubledist + 0.5d);
            totalLength = distanceInt;
        }

        TextView swingText = (TextView) findViewById(R.id.swingText);
        swingText.setVisibility(View.GONE);

        Button okbutton = (Button) findViewById(R.id.ok_button);
        okbutton.setVisibility(View.GONE);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitymeter = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        maxSpeed = accelerometer.getMaximumRange();


        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        driverID = soundPool.load(this, R.raw.driver, 1);
        ironID = soundPool.load(this, R.raw.iron, 1);
        wedgeID = soundPool.load(this, R.raw.wedge, 1);
        applauseID = soundPool.load(this, R.raw.applause, 1);
        missID = soundPool.load(this, R.raw.miss, 1);
        intheholeID = soundPool.load(this, R.raw.inthehole, 1);
        winID = soundPool.load(this, R.raw.win, 1);
        sliceID = soundPool.load(this, R.raw.slice, 1);
        hookID = soundPool.load(this, R.raw.hook, 1);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_swing, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        manager.registerListener(this, gravitymeter, SensorManager.SENSOR_DELAY_FASTEST);
    }
    public void ironButton(View view){
        if (v!=null) v.vibrate(50);
        Button ironbutton = (Button) findViewById(R.id.ironbutton);
        ironbutton.setFocusable(true);
        ironbutton.setFocusableInTouchMode(true);


        ironbutton.requestFocus();

        club = IRON;
        Button swingbutton= (Button) findViewById(R.id.ready_button);
        swingbutton.setEnabled(true);
        swingbutton.setClickable(true);

    }
    public void wedgeButton(View view){
        if (v!=null) v.vibrate(50);
        Button wedgebutton = (Button) findViewById(R.id.wedgebutton);
        wedgebutton.setFocusable(true);
        wedgebutton.setFocusableInTouchMode(true);
        wedgebutton.requestFocus();
        club = WEDGE;
        Button swingbutton= (Button) findViewById(R.id.ready_button);
        swingbutton.setEnabled(true);
        swingbutton.setClickable(true);
    }
    public void driverButton(View view){
        if (v!=null) v.vibrate(50);

        Button driverbutton = (Button) findViewById(R.id.driverbutton);
        driverbutton.setFocusable(true);
        driverbutton.setFocusableInTouchMode(true);
        driverbutton.requestFocus();
        club = DRIVER;
        Button swingbutton= (Button) findViewById(R.id.ready_button);
        swingbutton.setEnabled(true);
        swingbutton.setClickable(true);

    }



    public void getData(View view){
        Button readybutton = (Button) findViewById(R.id.readytoswing_button);
        readybutton.setVisibility(View.GONE);
        readybutton.setEnabled(false);
        readybutton.setClickable(false);
        if (v!=null) v.vibrate(50);

        TextView swingInfo = (TextView) findViewById(R.id.swingInfo);
        swingInfo.setVisibility(View.GONE);

        stanceGX = gravityData[0];
        stanceGY =gravityData[1];
        findViewById(R.id.ready_button).setVisibility(View.GONE);
        buttonPressed = true;




        TextView swingText = (TextView) findViewById(R.id.swingText);
        swingText.setVisibility(View.VISIBLE);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == gravitymeter){
            gravityData[0] = sensorEvent.values[0];
            gravityData[1] = sensorEvent.values[1];
        }
        if (sensorEvent.sensor == accelerometer) {
            accelerometerData = sensorEvent.values[1];
        }
        checkSensorValues();
    }
    private void checkSensorValues(){
        differenceGX = Math.abs(stanceGX-gravityData[0]);
        differenceGY = Math.abs(stanceGY-gravityData[1]);
        if(Math.abs(gravityData[1]-stanceGY) > maxG){
            maxG = Math.abs(gravityData[1]-stanceGY);
        }
        if (differenceGY > 2 && buttonPressed) {
            swingStarted = true;
            buttonPressed = false;
        }
        if (swingStarted && differenceGY<2 && differenceGX<1.5) {
            if(!hit){
                speed = accelerometerData;
            }
            hit = true;
            if (differenceGX < XAbs) {
                XAbs = differenceGX;
                closestGX = gravityData[0];
            }
            if(differenceGY<YAbs) {
                YAbs = differenceGY;
                closestGY = gravityData[1];
            }
            if (stanceGY-closestGY < 0.5 && stanceGY-closestGY>-0.5 &&  stanceGX-closestGX < 0.1 && stanceGX-closestGX>-0.1 ){
                impact = PERFECT_HIT;
            }else if(stanceGY-closestGY<-0.8 || stanceGY-closestGY> 0.8 ){
                impact = MISS;
            }else if (stanceGX-closestGX < -0.1) {
                impact = SLICE;
            }else if (stanceGX-closestGX > 0.1) {
                impact = HOOK;
            }
        }




        TextView swingView = (TextView) findViewById(R.id.swingView);

        String tempclub = "";
        float impactPenalty = 1f;
        if(differenceGY>1.5 && hit) {


            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);

            if (club == DRIVER) tempclub = "Driver";
            else if (club == IRON) tempclub = "Iron";
            else tempclub = "Wedge";


            swingView.setText(tempclub);

            switch (impact) {
                case SLICE:
                    swingView.append("\nSlice!");
                    impactPenalty = 0.7f;
                    break;
                case HOOK:
                    swingView.append("\n" +
                            "Hook!");
                    impactPenalty = 0.7f;
                    break;
                case PERFECT_HIT:
                    swingView.append("\n" +
                            "Perfect shot!");
                    break;
                case MISS:
                    swingView.append("\n" +
                            "Miss!");
                    break;
                default:
                    break;
            }

            swingStarted = false;
            hit=false;
            if (maxG < 10) {
                swingType = 0.3f;
            } else if (maxG < 15) {
                swingType = 0.6f;
            } else {
                swingType = 1f;
            }

            TextView swingText = (TextView) findViewById(R.id.swingText);
            swingText.setVisibility(View.GONE);




            Button okbutton = (Button) findViewById(R.id.ok_button);
            okbutton.setEnabled(true);
            okbutton.setClickable(true);
            okbutton.setVisibility(View.VISIBLE);

            // Lägger till ett slag
            numberOfStrokes++;
            int distanceInt;
            LLACoordinate newcord = null;
            if(impact!=MISS) {
                float trueSpeed = Math.abs(speed / maxSpeed);
               double distance = club * swingType * trueSpeed * impactPenalty * MAX_DISTANCE;
                distanceInt = (int) (distance + 0.5d);


                newcord= calculateNewPosition(distance, impact);
                balllat = newcord.getLatitude();
                balllong = newcord.getLongitude();
            }else{
                distanceInt=0;
            }
            swingView.append("\n" + distanceInt + "m");

            LLACoordinate flag = new LLACoordinate(flaglat, flaglong, 0, 0);

            //Nytt ljud
            boolean shallplay = true;
            if (newcord!= null){
                shallplay = (MetaioCloudUtils.getDistanceBetweenTwoCoordinates(newcord, flag)> 10d);
            }
            if(impact!=MISS) {
                if (club == DRIVER && loaded) {
                    soundPool.play(driverID, 1, 1, 1, 0, 1f);
                } else if (club == IRON && loaded) {
                    soundPool.play(ironID, 1,1, 1, 0, 1f);
                } else {
                    soundPool.play(wedgeID, 1, 1, 1, 0, 1f);
                }
            } else if (shallplay){
                soundPool.play(missID, 1, 1, 1, 0, 1f);
            }
            if(impact==PERFECT_HIT && shallplay) {
                soundPool.play(applauseID, 1, 1, 1, 0, 1f);
            }
            if(impact==SLICE && shallplay){
                soundPool.play(sliceID, 1, 1, 1, 0, 1f);
            }
            if(impact==HOOK && shallplay){
                soundPool.play(hookID, 1, 1, 1, 0, 1f);
            }

            if (newcord != null) {

                double doubledist = MetaioCloudUtils.getDistanceBetweenTwoCoordinates(newcord, flag);

                if (doubledist < 10d && flag != null){

                    if (loaded){
                    soundPool.play(winID, 1, 1, 1, 0, 1f);
                    soundPool.play(intheholeID, 1, 1, 1, 0, 1f);
                    }

                    Intent winintent = new Intent(this, Win.class);
                    winintent.putExtra("wintext","In the hole!\nHole Length "+ totalLength+"m\nStrokes "+numberOfStrokes);
                    startActivity(winintent);
                }
            }

        }
    }
    private LLACoordinate calculateNewPosition(double distance, int impact){
        double bearing = ballPosition.bearingTo(flagPosition);
        if(impact==SLICE){
            bearing = (bearing+20f*club)%360;
        }
        if(impact==HOOK){
            bearing = (bearing-20f*club)%360;
        }
        bearing = Math.toRadians(bearing);
        double ballLat = Math.toRadians(ballPosition.getLatitude());
        double ballLong = Math.toRadians(ballPosition.getLongitude());
        double flagLat = Math.toRadians(flagPosition.getLatitude());
        double dist= (distance/1000.0)/6371;
        double newLat = Math.asin( Math.sin(ballLat)*Math.cos(dist) + Math.cos(ballLat)*Math.sin(dist)*Math.cos(bearing) );
        double a = Math.atan2(Math.sin(bearing)*Math.sin(dist)*Math.cos(ballLat), Math.cos(dist)-Math.sin(ballLat)*Math.sin(flagLat));
        double newLong = ballLong + a;
        newLong = (newLong+ 3*Math.PI) % (2*Math.PI) - Math.PI;
        return new LLACoordinate(Math.toDegrees(newLat),Math.toDegrees(newLong),0d,0d);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    public void changeButton(View view) {
        if (v!=null) v.vibrate(50);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.beforestrokeview);
        relativeLayout.setVisibility(View.GONE);

        Button swingbutton = (Button) findViewById(R.id.ready_button);
        swingbutton.setVisibility(View.GONE);
        swingbutton.setEnabled(false);
        swingbutton.setClickable(false);

        TextView swingInfo = (TextView) findViewById(R.id.swingInfo);
        swingInfo.setVisibility(View.VISIBLE);

        Button readybutton = (Button) findViewById(R.id.readytoswing_button);
        readybutton.setVisibility(View.VISIBLE);
        readybutton.setEnabled(true);
        readybutton.setClickable(true);



    }
    public void sendBallPosition(View view){
       // Göra en ny intent och skicka till arhandler
        if (v!=null) v.vibrate(50);
        Intent intent = new Intent(this, ARHandlerActivity.class);
        intent.putExtra("balllat", balllat);
        intent.putExtra("balllong", balllong);
        intent.putExtra("flaglat", flaglat );
        intent.putExtra("flaglong", flaglong);
        intent.putExtra("strokes", numberOfStrokes);
        intent.putExtra("totalLength", totalLength);

        Button okbutton = (Button) findViewById(R.id.ok_button);
        okbutton.setEnabled(false);
        okbutton.setClickable(false);
        startActivity(intent);
    }
}