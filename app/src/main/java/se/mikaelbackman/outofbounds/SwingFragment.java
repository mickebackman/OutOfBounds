package se.mikaelbackman.outofbounds;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SwingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SwingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwingFragment extends Fragment implements SensorEventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public Double balllat, balllong, flaglat, flaglong;

    private OnFragmentInteractionListener mListener;

    private static final int PERFECT_HIT=0;
    private static final int SLICE =1;
    private static final int HOOK =2;
    private static final int MISS =3;
    private static final float WEDGE =0.3f;
    private static final float IRON =0.6f;
    private static final float DRIVER =1f;
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
    private int applauseID;
    private int missID;
    private boolean loaded;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SwingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SwingFragment newInstance(String param1, String param2) {
        SwingFragment fragment = new SwingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SwingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        manager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitymeter = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        maxSpeed = accelerometer.getMaximumRange();
        ballPosition = new Location("GPS_PROVIDER");
        flagPosition = new Location("GPS_PROVIDER");

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override

            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                loaded = true;

            }

        });

        driverID = soundPool.load(getActivity(), R.raw.driver, 1);
        ironID = soundPool.load(getActivity(), R.raw.iron, 1);
        wedgeID = soundPool.load(getActivity(), R.raw.wedge, 1);
        applauseID = soundPool.load(getActivity(), R.raw.applause, 1);
        missID = soundPool.load(getActivity(), R.raw.miss, 1);

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        club = IRON;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swing, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
           // mListener.onFragmentInteraction(uri);
        }
    }

    public void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    public void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        manager.registerListener(this, gravitymeter, SensorManager.SENSOR_DELAY_FASTEST);
    }




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPositions(Double ball_lat, Double ball_long, Double flag_lat, Double flag_long){
       /* balllat = ball_lat;
        balllong = ball_long;
        flaglat = flag_lat;
        flaglong = flag_long;*/
        ballPosition.setLatitude(ball_lat);
        ballPosition.setLongitude(ball_long);
        flagPosition.setLatitude(flag_lat);
        flagPosition.setLongitude(flag_long);
    }

    public void getData(View view){
        stanceGX = gravityData[0];
        stanceGY =gravityData[1];
        getActivity().findViewById(R.id.ready_button).setVisibility(View.GONE);
        buttonPressed = true;
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

            if (stanceGY-closestGY < 0.5 && stanceGY-closestGY>-0.5 && stanceGX-closestGX < 0.1 && stanceGX-closestGX>-0.1 ){
                impact = PERFECT_HIT;
            }else if(stanceGY-closestGY<-0.8 || stanceGY-closestGY> 0.8 ){
                impact = MISS;
            }else if (stanceGX-closestGX < -0.1) {
                impact = SLICE;
            }else if (stanceGX-closestGX > 0.1) {
                impact = HOOK;
            }
        }

        TextView swingView = (TextView) getActivity().findViewById(R.id.data_View);
       // getActivity().findViewById(R.id.swing_text).setVisibility(View.GONE);
        float impactPenalty = 1f;
        if(differenceGY>1.5 && hit) {
            if(impact!=MISS ) {
                if (club == DRIVER && loaded) {
                    soundPool.play(driverID, 1, 1, 1, 0, 1f);
                } else if (club == IRON) {
                    soundPool.play(ironID, 1,1, 1, 0, 1f);
                } else {
                    soundPool.play(wedgeID, 1, 1, 1, 0, 1f);
                }
            } else {
                soundPool.play(missID, 1, 1, 1, 0, 1f);
            }

            if(impact==PERFECT_HIT) {
                soundPool.play(applauseID, 1, 1, 1, 0, 1f);
            }
            switch (impact) {
                case SLICE:
                    swingView.setText("Slice!");
                    impactPenalty = 0.7f;
                    break;
                case HOOK:
                    swingView.setText("Hook!");
                    impactPenalty = 0.7f;
                    break;
                case PERFECT_HIT:
                    swingView.setText("Perfect shot!");
                    break;
                case MISS:
                    swingView.setText("Miss!");
                    break;
                default:
                    break;
            }



            swingStarted = false;
            hit=false;
            if (maxG < 10) {
                swingType = 0.3f;
                swingView.append("Quarter swing!");
            } else if (maxG < 15) {
                swingType = 0.6f;
                swingView.append("Half swing!");
            } else {
                swingType = 1f;
                swingView.append("Full swing!");
            }



            float trueSpeed = Math.abs(speed/maxSpeed);

            double distance = club*swingType*trueSpeed*impactPenalty*MAX_DISTANCE;

            swingView.append("\n Distance: "+ distance);
            swingView.append("\n Speed: "+ trueSpeed);
            swingView.append(calculateNewPosition(distance, impact));


        }

    }

    private String calculateNewPosition(double distance, int impact){

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

        return "New ballPosition: "+ Math.toDegrees(newLat)+ " ; "+ Math.toDegrees(newLong);

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    /**
         * This interface must be implemented by activities that contain this
         * fragment to allow an interaction in this fragment to be communicated
         * to the activity and potentially other fragments contained in that
         * activity.
         * <p/>
         * See the Android Training lesson <a href=
         * "http://developer.android.com/training/basics/fragments/communicating.html"
         * >Communicating with Other Fragments</a> for more information.
         */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Double ball_lat, Double ball_long);
    }

}
