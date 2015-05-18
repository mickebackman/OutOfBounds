package se.mikaelbackman.outofbounds;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.metaio.sdk.SensorsComponentAndroid;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng markerLocation = null;
    private LatLng gpsLocation = null;

    private TextView gpsLocationText = null;
    private TextView markerLocationText = null;
    Button playbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        playbutton = (Button) findViewById(R.id.playbutton);
        playbutton.setEnabled(false);
        playbutton.setClickable(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void playGolf(View view){

        if ((gpsLocation != null) && (markerLocation != null)) {
            playbutton.setEnabled(false);
            playbutton.setClickable(false);
            Intent intent = new Intent(this, Play.class);
            intent.putExtra("ball_latitude", gpsLocation.latitude);
            intent.putExtra("ball_longitude", gpsLocation.longitude);
            intent.putExtra("flag_latitude", markerLocation.latitude);
            intent.putExtra("flag_longitude", markerLocation.longitude);
            startActivity(intent);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
          //  gpsLocationText = (TextView) findViewById(R.id.gpsView);
          //  gpsLocationText.setText("GPS coord: " + loc.latitude + " , " + loc.longitude);
            gpsLocation = loc;
            if(mMap != null){
                float zoom = mMap.getCameraPosition().zoom;
                if(zoom < 10.0f) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                }
            }
        }
    };
    private GoogleMap.OnMapLongClickListener myLongClickListener = new GoogleMap.OnMapLongClickListener(){
        @Override
        public void onMapLongClick(LatLng latLng) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            markerLocation = latLng;
            playbutton.setEnabled(true);
            playbutton.setClickable(true);
          //  markerLocationText = (TextView) findViewById(R.id.markerView);
          //  markerLocationText.setText("Marker coord: " + latLng.latitude + " , " + latLng.longitude);
           // RoutePlanner routePlanner = new RoutePlanner(gpsLocation, markerLocation, RoutePlanner.MODE_WALKING);
           // drawRoute(routePlanner);
        }
    };


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnMapLongClickListener(myLongClickListener);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
