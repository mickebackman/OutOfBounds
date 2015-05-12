package se.mikaelbackman.outofbounds;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.ARELInterpreterAndroidJava;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import java.io.File;
import java.util.concurrent.locks.Lock;


public class ARHandlerActivity extends com.metaio.sdk.ARViewActivity implements SensorsComponentAndroid.Callback, SwingFragment.OnFragmentInteractionListener {


    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;
    private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;

    public double ball_lat, ball_long, flag_lat, flag_long;
    private LLACoordinate ball,flag;
    private IGeometry mFlagGeo, mBallGeo;
    private IRadar mRadar;
    public TextView distancetext;
    public Button swingbutton;
    public  FragmentTransaction fragmentTransaction;
    public FragmentManager fragmentManager;
    public SwingFragment swingFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arhandler);



        Intent intent = getIntent();
        ball_lat = intent.getDoubleExtra("balllat", 0);
        ball_long = intent.getDoubleExtra("balllong", 0);
        flag_lat = intent.getDoubleExtra("flaglat", 0);
        flag_long = intent.getDoubleExtra("flaglong", 0);
           Log.i("GPS_ARHand_rec", ("Ball lat: " + ball_lat + " long: " + ball_long
           + "\n Flag lat: " + flag_lat + " long: " + flag_long));

        // Set GPS tracking configuration
        boolean result = metaioSDK.setTrackingConfiguration("GPS");
        MetaioDebug.log("Tracking data loaded: " + result);

      //  distancetext = (TextView) findViewById(R.id.holedistance);

        swingbutton = (Button) findViewById(R.id.swingbutton);
        swingbutton.setEnabled(false);
        swingbutton.setClickable(false);



        if (savedInstanceState == null) {
            Log.i("FRAGMENT_CREATE", "Create fragment");
            swingFragment = new SwingFragment();
            swingFragment.setArguments(getIntent().getExtras());
            fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, swingFragment, "swingFragment");

        //    fragmentTransaction.hide(swingFragment);
            fragmentTransaction.commit();
        }


    }


    @Override
    protected void onDestroy()
    {
        // Break circular reference of Java objects
        if (mAnnotatedGeometriesGroup != null)
        {
            mAnnotatedGeometriesGroup.registerCallback(null);
        }

        if (mAnnotatedGeometriesGroupCallback != null)
        {
            mAnnotatedGeometriesGroupCallback.delete();
            mAnnotatedGeometriesGroupCallback = null;
        }

        super.onDestroy();
    }

    public void onCancelClick(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    public void swingFragment(View view){
        swingFragment.setPositions(ball_lat, ball_long, flag_lat, flag_long);
        fragmentTransaction.show(swingFragment);
        Log.i("FRAGMENT_SHOW", "Fragment trying to show");
        fragmentTransaction.commit();
    }

    @Override
    public void onDrawFrame()
    {
        if (metaioSDK != null && mSensors != null)
        {
            SensorValues sensorValues = mSensors.getSensorValues();
            mSensors.getLocation();

           /* if (flag != null) {
                Double doubledist = MetaioCloudUtils.getDistanceBetweenTwoCoordinates(flag, mSensors.getLocation());
                int dist = (int)(doubledist + 0.5d);
               //TODO får inte sätta texten för avstånd till hål så här. måste hitta något annat sätt.

              //  distancetext = (TextView) findViewById(R.id.holedistance);
             //  if (distancetext != null) {
               //    distancetext.setText("Distance to hole is: " + dist);
               //}
            }*/


            // Gör koll om distance < 5 och isåfall aktivera knappen
          /*  if (ball != null) {

                double tempdistance = MetaioCloudUtils.getDistanceBetweenTwoCoordinates(ball, mSensors.getLocation());
                if (tempdistance < 5.0d) {
                    if (swingbutton != null) {
                        swingbutton.setEnabled(true);
                        swingbutton.setClickable(true);
                    }
                }
                else {
                    if (swingbutton != null) {
                        Log.i("BUTTON_DEACTIVATE", "Button deactivated");
                        swingbutton.setEnabled(false);
                        swingbutton.setClickable(false);
                    }
                }
            }*/

            float heading = 0.0f;
            if (sensorValues.hasAttitude())
            {
                float m[] = new float[9];
                sensorValues.getAttitude().getRotationMatrix(m);

                Vector3d v = new Vector3d(m[6], m[7], m[8]);
                v.normalize();

                heading = (float)(-Math.atan2(v.getY(), v.getX()) - Math.PI / 2.0);
            }

            // Geometrices för locations. Kanske inte sätter värdena här ens.
            IGeometry geos[] = new IGeometry[] {mFlagGeo,mBallGeo};
            Rotation rot = new Rotation((float)(Math.PI / 2.0), 0.0f, -heading);
            for (IGeometry geo : geos)
            {
                if (geo != null)
                {
                    geo.setRotation(rot);
                }
            }
        }


        super.onDrawFrame();
    }


    @Override
    protected int getGUILayout() {

        return R.layout.activity_arhandler;

    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    // tells which tracking(s) should be used
    protected void loadContents() {

        mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
        mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
        mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);

        // Clamp geometries' Z position to range [5000;200000] no matter how close or far they are
        // away.
        // This influences minimum and maximum scaling of the geometries (easier for development).
        metaioSDK.setLLAObjectRenderingLimits(5, 200);

        // Set render frustum accordingly
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);



        ball = new LLACoordinate(ball_lat, ball_long, 0,0);
        Log.i("GPS_coordinates_ball", ball.toString());

        flag = new LLACoordinate(flag_lat, flag_long, 0,0);
        Log.i("GPS_coordinates_flag", flag.toString());



        //Lägger till POIS och 3dmodellerna som vi ska ha som objekt.
        mBallGeo = createPOIGeometry(ball);
        mAnnotatedGeometriesGroup.addGeometry(mBallGeo, "Ball");

        mFlagGeo = createFlagGeometry(flag);
        mAnnotatedGeometriesGroup.addGeometry(mFlagGeo, "Flag");


        // create radar
        mRadar = metaioSDK.createRadar();
        File backgroundtexture = AssetsManager.getAssetPathAsFile(getApplicationContext(), "radar.png");
        File defaulttexture = AssetsManager.getAssetPathAsFile(getApplicationContext(), "yellow.png");
        if (backgroundtexture != null) {
            mRadar.setBackgroundTexture(backgroundtexture);
            if (defaulttexture != null) {
                mRadar.setObjectsDefaultTexture(defaulttexture);
            }
            else {
                Log.i("MyTag", "Får null på filhämtning till objecttexture i radarcreation");
            }
            mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

            // lägger till object till radarn
            mRadar.add(mBallGeo);
            mRadar.add(mFlagGeo);
        } else {
            Log.i("MyTag", "Får null på filhämtning i background i radarcreation");
        }


        mAnnotatedGeometriesGroup.setBottomPadding(250);


    }




    @Override
    protected void onGeometryTouched(final IGeometry geometry) {
        MetaioDebug.log("Geometry selected: " + geometry);



        mSurfaceView.queueEvent(new Runnable()
        {

            @Override
            public void run()
            {
                mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "yellow.png"));
                mRadar.setObjectTexture(geometry, AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "red.png"));
                mAnnotatedGeometriesGroup.setSelectedGeometry(geometry);
            }
        });
    }



    // Här väljer vi vilken 3dmodell vi ska ha för att markera objekt
    private IGeometry createPOIGeometry(LLACoordinate lla)
    {
        final File path =
                AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "ExamplePOI.obj");
        if (path != null)
        {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(50);
            return geo;
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
            return null;
        }
    }

    private IGeometry createFlagGeometry(LLACoordinate lla) {
        final File path =
                AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "Flag.obj");
        if (path != null)
        {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(5000);
            return geo;
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Missing files for flag geometry");
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_arhandler, menu);
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




    @Override
  protected void onPause() {
        super.onPause();

        // remove callback
        if (mSensors != null) {
            mSensors.registerCallback(null);
            // mSensorsManager.pause();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register callback to receive sensor updates
        if (mSensors != null) {
            mSensors.registerCallback(this);
            // mSensorsManager.resume();
        }

    }

    @Override
    public void onGravitySensorChanged(float[] floats) {

    }

    @Override
    public void onHeadingSensorChanged(float[] floats) {

    }

    @Override
    public void onLocationSensorChanged(LLACoordinate llaCoordinate) {
        Log.i("GPS_LOC_CHANGE", "location sensor changed" + llaCoordinate.toString());
        if (mAnnotatedGeometriesGroup != null && mBallGeo != null && mFlagGeo != null) {
            mAnnotatedGeometriesGroup.triggerAnnotationUpdate(mBallGeo);
            mAnnotatedGeometriesGroup.triggerAnnotationUpdate(mFlagGeo);
            mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);
        }

    }

    @Override
    public void onFragmentInteraction(Double ball_lat, Double ball_long) {

    }


    final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback
    {
        Bitmap mAnnotationBackground, mEmptyStarImage, mFullStarImage;
        int mAnnotationBackgroundIndex;
        ImageStruct texture;
        String[] textureHash = new String[1];
        TextPaint mPaint;
        Lock geometryLock;


        Bitmap inOutCachedBitmaps[] = new Bitmap[] {mAnnotationBackground, mEmptyStarImage, mFullStarImage};
        int inOutCachedAnnotationBackgroundIndex[] = new int[] {mAnnotationBackgroundIndex};

        public MyAnnotatedGeometriesGroupCallback()
        {
            mPaint = new TextPaint();
            mPaint.setFilterBitmap(true); // enable dithering
            mPaint.setAntiAlias(true); // enable anti-aliasing
        }

        @Override
        public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData, IGeometry existingAnnotation)
        {
            Log.i("GPS_loadup_enter", "Går in i load updatedannotation med userdata " + (String) userData + " existingannotation " + existingAnnotation);
            if (userData == null)
            {
                return null;
            }

            /*if (existingAnnotation != null)
            {
                // We don't update the annotation if e.g. distance has changed
                return existingAnnotation;
            }
            */

            String title = (String)userData; // as passed to addGeometry
            LLACoordinate location = geometry.getTranslationLLA();
            float distance = (float) MetaioCloudUtils.getDistanceBetweenTwoCoordinates(location, mSensors.getLocation());
            Log.i("GPS_loadup_dist", "Distance mellan location och mSensor.getlocaion " + distance+ " location: " + location.toString() + ", " + mSensors.getLocation().toString());
            Bitmap thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            try
            {
                texture =
                        ARELInterpreterAndroidJava.getAnnotationImageForPOI(title, title, distance, "0", thumbnail,
                                null,
                                metaioSDK.getRenderSize(), ARHandlerActivity.this,
                                mPaint, inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex, textureHash);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (thumbnail != null)
                    thumbnail.recycle();
                thumbnail = null;
            }

            mAnnotationBackground = inOutCachedBitmaps[0];
            mEmptyStarImage = inOutCachedBitmaps[1];
            mFullStarImage = inOutCachedBitmaps[2];
            mAnnotationBackgroundIndex = inOutCachedAnnotationBackgroundIndex[0];

            IGeometry resultGeometry = null;

            if (texture != null)
            {
                if (geometryLock != null)
                {
                    geometryLock.lock();
                }

                try
                {
                    // Use texture "hash" to ensure that SDK loads new texture if texture changed
                    resultGeometry = metaioSDK.createGeometryFromImage(textureHash[0], texture, true, false);
                }
                finally
                {
                    if (geometryLock != null)
                    {
                        geometryLock.unlock();
                    }
                }
            }

            Log.i("GPS_loadup_exit", "Går ut i load updatedannotation med userdata " + (String) userData + " existingannotation " + existingAnnotation);
            return resultGeometry;
        }

        @Override
        public void onFocusStateChanged(IGeometry geometry, Object userData, EGEOMETRY_FOCUS_STATE oldState,
                                        EGEOMETRY_FOCUS_STATE newState)
        {
            MetaioDebug.log("onFocusStateChanged for " + (String)userData + ", " + oldState + "->" + newState);
        }
    }
}
