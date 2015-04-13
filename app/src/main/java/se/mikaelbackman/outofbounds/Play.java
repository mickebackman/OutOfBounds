package se.mikaelbackman.outofbounds;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

import java.io.IOException;


public class Play extends ActionBarActivity implements SensorEventListener {

    /**
     * Task that will extract all the assets
     */
    private AssetsExtracter mTask;
    private SensorManager manager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] accelerometerData = new float[3];
    private float[] magnetometerData = new float[3];
    private boolean accSet = false;
    private boolean magSet = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        // Enable metaio SDK debug log messages based on build configuration
        MetaioDebug.enableLogging(BuildConfig.DEBUG);

        // extract all the assets
       // mTask = new AssetsExtracter();
       // mTask.execute(0);

        // start a new activity that handles AR content
        // TODO: måste göra något med onresume eller liknande, kanske onstop i main activity... får error om rätt layout laddas.
      //  Intent intent = new Intent(this, ARHandlerActivity.class);
       // startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == accelerometer) {
            accelerometerData[0] = sensorEvent.values[0];
            accelerometerData[1] = sensorEvent.values[1];
            accelerometerData[2] = sensorEvent.values[2];
            accSet = true;
        } else if (sensorEvent.sensor == magnetometer) {
            magnetometerData[0] = sensorEvent.values[0];
            magnetometerData[1] = sensorEvent.values[1];
            magnetometerData[2] = sensorEvent.values[2];
            magSet = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /**
     * This task extracts all the application assets to an external or internal location
     * to make them accessible to Metaio SDK
     */

    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }
    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
    {

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected Boolean doInBackground(Integer... params)
        {
        //    try
        //    {
                // Extract all assets and overwrite existing files if debug build
          //      AssetsManager.extractAllAssets(getApplicationContext(), BuildConfig.DEBUG);
          //  }
          //  catch (IOException e)
          //  {
           //     MetaioDebug.log(Log.ERROR, "Error extracting assets: "+e.getMessage());
           //     MetaioDebug.printStackTrace(Log.ERROR, e);
           //     return false;
           // }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (result)
            {
                // Start AR Activity on success
            //    Intent intent = new Intent(getApplicationContext(), ARHandlerActivity.class);
               // startActivity(intent);
            }
            else
            {
                // Show a toast with an error message
                Toast toast = Toast.makeText(getApplicationContext(), "Error extracting application assets!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }

            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getData(View view){
        if (accSet && magSet) {
            TextView text = (TextView) findViewById(R.id.data);
            text.setText("");
            float xda = accelerometerData[0];
            float yda = accelerometerData[1];
            float zda = accelerometerData[2];
            float xdm = magnetometerData[0];
            float ydm = magnetometerData[1];
            float zdm = magnetometerData[2];
            accSet = false;
            magSet = false;
            text.setText("Xa " + xda + "\n" + " Ya " + yda + "\n" + " Za " + zda + "\n" + " Xm " + xdm + "\n" + " Ym " + ydm + "\n" + " Zm " + zdm);
        }
    }
}
