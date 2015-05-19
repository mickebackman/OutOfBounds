package se.mikaelbackman.outofbounds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class Win extends Activity{
     //private SoundPool soundPool;
     //private boolean loaded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        Intent recintent = getIntent();
        String wintext = recintent.getStringExtra("wintext");
        TextView text = (TextView) findViewById(R.id.wintext);
        text.setText(wintext);


       /*  soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
       soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
       int intheholeID = soundPool.load(this, R.raw.inthehole, 1);
       int winID = soundPool.load(this, R.raw.win, 1);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if(loaded) {
            soundPool.play(intheholeID, 1, 1, 1, 0, 1f);
            soundPool.play(winID, 1, 1, 1, 0, 1f);
        }
*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_win, menu);
        return true;
    }


    public void finish(View view){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

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
