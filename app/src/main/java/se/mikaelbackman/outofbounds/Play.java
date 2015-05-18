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


public class Play extends ActionBarActivity {

    /**
     * Task that will extract all the assets
     */
    private AssetsExtracter mTask;
    private Double ball_latitude, ball_longitude, flag_latitude, flag_longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Intent intent = getIntent();
        ball_latitude = intent.getDoubleExtra("ball_latitude", 0);
        ball_longitude = intent.getDoubleExtra("ball_longitude", 0);
        flag_latitude = intent.getDoubleExtra("flag_latitude", 0);
        flag_longitude = intent.getDoubleExtra("flag_longitude", 0);


        // Enable metaio SDK debug log messages based on build configuration
        MetaioDebug.enableLogging(BuildConfig.DEBUG);

        // extract all the assets
        mTask = new AssetsExtracter();
        mTask.execute(0);

        // start a new activity that handles AR content
        int numberOfStrokes = 0;
        int totalLength = 0;
        Intent intentsend = new Intent(this, ARHandlerActivity.class);
        intentsend.putExtra("balllat", ball_latitude);
        intentsend.putExtra("balllong", ball_longitude);
        intentsend.putExtra("flaglat", flag_latitude);
        intentsend.putExtra("flaglong", flag_longitude);
        intentsend.putExtra("strokes", numberOfStrokes);
        intentsend.putExtra("totalLength", totalLength);

        startActivity(intentsend);
    }




    /**
     * This task extracts all the application assets to an external or internal location
     * to make them accessible to Metaio SDK
     */

    protected void onResume() {
        super.onResume();
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
           try
            {
             //   Extract all assets and overwrite existing files if debug build
           AssetsManager.extractAllAssets(getApplicationContext(), BuildConfig.DEBUG);
            }
           catch (IOException e)
            {
             MetaioDebug.log(Log.ERROR, "Error extracting assets: "+e.getMessage());
              MetaioDebug.printStackTrace(Log.ERROR, e);
                return false;
           }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (result)
            {
                // Start AR Activity on success
                Intent intent = new Intent(getApplicationContext(), ARHandlerActivity.class);
                intent.putExtra("balllat", ball_latitude);
                intent.putExtra("balllong", ball_longitude);
                intent.putExtra("flaglat", flag_latitude);
                intent.putExtra("flaglong", flag_longitude);
                startActivity(intent);
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

}
