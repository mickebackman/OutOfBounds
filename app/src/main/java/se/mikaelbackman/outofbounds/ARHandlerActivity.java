package se.mikaelbackman.outofbounds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.metaio.sdk.jni.ISensorsComponent;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import java.io.File;
import java.util.concurrent.locks.Lock;


public class ARHandlerActivity extends com.metaio.sdk.ARViewActivity implements  SensorsComponentAndroid.Callback,LocationListener {


    //LLACoordinate lund = new LLACoordinate(55.7047, 13.191, 0,0);
    //LLACoordinate micke = new LLACoordinate(55.7112, 13.1871,0,0);
    //LLACoordinate backis = new LLACoordinate(55.7104, 13.1686,0,0);
    protected LocationManager locationManager;
    String provider;
    private LocationListener locationListener;
    private double longitude, latitude;

    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;

    private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;
    public SensorsComponentAndroid sensors;
    public double ball_lat, ball_long, flag_lat,flag_long;
    private LLACoordinate ball,flag;


    /**
     * Geometries
     */
    private IGeometry mFlagGeo, mBallGeo, mLundGeo, mBackisGeo, mMickeGeo, mIkdcGeo;

    private IRadar mRadar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_arhandler);
       /*
        if (savedInstanceState != null) {*/

            Intent intent = getIntent();
           // if (intent.hasExtra("balllat")) {
           ball_lat = intent.getDoubleExtra("balllat", 0);
           ball_long = intent.getDoubleExtra("balllong", 0);
           flag_lat = intent.getDoubleExtra("flaglat", 0);
           flag_long = intent.getDoubleExtra("flaglong", 0);
                Log.i("GPS_ARHand_rec", ("Ball lat: " + ball_lat + " long: " + ball_long
                        + "\n Flag lat: " + flag_lat + " long: " + flag_long));
          //  }
     //   }

        // Set GPS tracking configuration
        boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
        MetaioDebug.log("Tracking data loaded: " + result);

        //nytt
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);
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

    @Override
    public void onDrawFrame()
    {
        if (metaioSDK != null && mSensors != null)
        {
            SensorValues sensorValues = mSensors.getSensorValues();


            //Nytt
            ISensorsComponent aSensors = metaioSDK.getRegisteredSensorsComponent();
            aSensors.start(ISensorsComponent.SENSOR_LOCATION);


            float heading = 0.0f;
            if (sensorValues.hasAttitude())
            {
                float m[] = new float[9];
                sensorValues.getAttitude().getRotationMatrix(m);

                Vector3d v = new Vector3d(m[6], m[7], m[8]);
                v.normalize();

                heading = (float)(-Math.atan2(v.getY(), v.getX()) - Math.PI / 2.0);
            }

            // Geos för locations. Kanske inte sätter värdena här ens.
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

    public void onButtonClick(View v)
    {
        finish();
    }

    @Override
    protected int getGUILayout() {

        // TODO: Måste göra layout för detta
        //return R.layout.activity_arhandler;
        return 0;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    // tells which tracking(s) should be used
    // Logiken för trackingen
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


      /*  LLACoordinate lund = new LLACoordinate(55.7047, 13.191, 0,0);
        LLACoordinate micke = new LLACoordinate(55.7112, 13.1871,0,0);
        LLACoordinate backis = new LLACoordinate(55.7104, 13.1686,0,0);
        LLACoordinate ikdc = new LLACoordinate(55.7151, 13.2113,0,0);
*/
        ball = new LLACoordinate(ball_lat, ball_long, 0,0);
        Log.i("GPS_coordinates_ball", ball.toString());

        flag = new LLACoordinate(flag_lat, flag_long, 0,0);
        Log.i("GPS_coordinates_flag", flag.toString());



        //Lägger till POIS och 3dmodellerna som vi ska ha som objekt.

     /*   mLundGeo = createPOIGeometry(lund);
        mAnnotatedGeometriesGroup.addGeometry(mLundGeo, "Lund");

        mMickeGeo = createPOIGeometry(micke);
        mAnnotatedGeometriesGroup.addGeometry(mMickeGeo, "Micke");

        mBackisGeo = createPOIGeometry(backis);
        mAnnotatedGeometriesGroup.addGeometry(mBackisGeo, "Bäckis");

        mIkdcGeo = createPOIGeometry(ikdc);
        mAnnotatedGeometriesGroup.addGeometry(mIkdcGeo, "IKDC");
        */

        mBallGeo = createPOIGeometry(ball);
        mAnnotatedGeometriesGroup.addGeometry(mBallGeo, "Ball");

        mFlagGeo = createPOIGeometry(flag);
        mAnnotatedGeometriesGroup.addGeometry(mFlagGeo, "Flag");

       // Lägger till en golfboll på ikdc's location
      //  File golfball = AssetsManager.getAssetPathAsFile(getApplicationContext(), "golfball_lowpoly.obj");

      //  if (golfball != null)
        //{
          //  mIkdcGeo = metaioSDK.createGeometry(golfball);
       //     mMickeGeo = metaioSDK.createGeometry(golfball);
         //   if (mIkdcGeo != null || mMickeGeo != null)
         //   {
       //         mIkdcGeo.setTranslationLLA(ikdc);
           //     mIkdcGeo.setLLALimitsEnabled(true);
             //   mIkdcGeo.setScale(500);
            //    mMickeGeo.setTranslationLLA(ikdc);
              //  mMickeGeo.setLLALimitsEnabled(true);
               // mMickeGeo.setScale(500);
           // }
            //else
            //{
          //      MetaioDebug.log(Log.ERROR, "Error loading geometry: " + golfball );
         //   }
      //  }




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
                Log.i("MyTag", "Får null på filhämtning till objecttexture");
            }
            mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

            // lägger till object till radarn
           /* mRadar.add(mIkdcGeo);
            mRadar.add(mBackisGeo);
            mRadar.add(mMickeGeo);
            mRadar.add(mLundGeo);
            */
            mRadar.add(mBallGeo);
            mRadar.add(mFlagGeo);
        } else {
            Log.i("MyTag", "Får null på filhämtning i background");
        }


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
            geo.setScale(100);
            return geo;
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
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
    public void onGravitySensorChanged(float[] floats) {

    }

    @Override
    public void onHeadingSensorChanged(float[] floats) {

    }

    @Override
    public void onLocationSensorChanged(LLACoordinate llaCoordinate) {

    }

    @Override

    //nytt
    public void onLocationChanged(Location location){
        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.i("GPS_LOCATION", "Lat: " + latitude +"Long: " + longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Latitude","status");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Latitude","enable");

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Latitude","disable");
            }
        };

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    //nytt plus resume
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
            if (userData == null)
            {
                return null;
            }

            if (existingAnnotation != null)
            {
                // We don't update the annotation if e.g. distance has changed
                return existingAnnotation;
            }

            String title = (String)userData; // as passed to addGeometry
            LLACoordinate location = geometry.getTranslationLLA();
            float distance = (float) MetaioCloudUtils.getDistanceBetweenTwoCoordinates(location, mSensors.getLocation());
            Bitmap thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            try
            {
                texture =
                        ARELInterpreterAndroidJava.getAnnotationImageForPOI(title, title, distance, "5", thumbnail,
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
