package com.example.almax.nataloapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import android.widget.Switch;


public   class MainActivity extends Activity implements View.OnClickListener {
    private final static String TAG = ">==< ArduinoYun >==<";
    private final static String ARDUINO_IP_ADDRESS = "192.168.1.103";
    private final static int PORT = 6666;

    private Switch switch1, switch2;
    private ToggleButton button1, button2, button3, button4;

    MediaPlayer mp;
    ArrayList<Integer> playlist;
    ImageButton AudioP;
    Boolean MpPlaying = false;

    public int songId = 0;
    public void playSong()
    {
        //songId = getAllResourceIDs(R.raw.class, to_play_song);
        //mp1 = MediaPlayer.create(MainActivity.this,songId);


        mp = MediaPlayer.create(MainActivity.this,playlist.get(songId));


        try
        {
            mp.prepare();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
        Log.e ("PLAYING song: ", String.valueOf(songId));

        //Called when the song comp1letes.....



        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {




                if (playlist.size() == songId+1) {
                    //mp.stop();
                    songId=0;
                    playSong();
                }

                else {
                    if (playlist.size() > songId+1) {
                        songId = songId +1;

                        playSong();
                        Log.e("NEXTSONGID", String.valueOf(songId));
                    }
                }






            }
        });

    }

    //function which returns the unique resource ID.
    public static int getAllResourceIDs(Class c, String song) throws IllegalArgumentException
    {
        //System.out.println("inside HashMap"+ song);
        HashMap resmap = new HashMap();
        java.lang.reflect.Field[] fields = c.getFields();
        try
        {
            for(int i = 0; i < fields.length; i++)
            {
                if(song != null)
                    if(fields[i].getName().startsWith(song))
                        resmap.put(fields[i].getName(), fields[i].getInt(null));
                    else
                        resmap.put(fields[i].getName(), fields[i].getInt(null));
            }
        } catch (Exception e)
        {
            throw new IllegalArgumentException();
        }
        Integer one = (Integer) resmap.get(song);
        int songid = one.intValue();
        return songid;
    }







    @Override
    public void onDestroy() {
        if (mp.isPlaying())
            mp.stop();

        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Example of a call to a native method
        switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setOnClickListener(this); // calling onClick() method

        switch2 = (Switch) findViewById(R.id.switch2);
        switch2.setOnClickListener(this); // calling onClick() method

        button1 = (ToggleButton) findViewById(R.id.button1);
        button1.setOnClickListener(this); // calling onClick() method

        button2 = (ToggleButton) findViewById(R.id.button2);
        button2.setOnClickListener(this); // calling onClick() method

        button3 = (ToggleButton) findViewById(R.id.button3);
        button3.setOnClickListener(this); // calling onClick() method

        button4 = (ToggleButton) findViewById(R.id.button4);
        button4.setOnClickListener(this);
                                           // calling onClick() method


        AudioP=(ImageButton)findViewById(R.id.audioP);


        AudioP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                if (MpPlaying==false){
                    playSong();
                    AudioP.setBackgroundResource(R.drawable.stop);
                    MpPlaying=true;

                    Log.e("MP", "PLAYING");
                }

                else

                {
                    mp.stop();
                    mp.reset();
                    AudioP.setBackgroundResource(R.drawable.play);
                    MpPlaying=false;


                    Log.e("MP", "STOPPED");

                }




            }
        });



        final AudioManager audioManager;

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl = (SeekBar)findViewById(R.id.volbar);
        volControl.setMax(maxVolume);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });







//  AGGIUNGERE I FILE MP3 nella cartella RAW e inserirli in playlist
        playlist = new ArrayList<>();
        playlist.add(R.raw.m1);
        playlist.add(R.raw.m2);






    };




    @Override
    protected void onStart() {
        mStop.set(false);
        if(sNetworkThread == null){
            sNetworkThread = new Thread(mNetworkRunnable);
            sNetworkThread.start();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        mStop.set(true);
        mQueue.clear();
        mQueue.offer("END");
        if(sNetworkThread != null) sNetworkThread.interrupt();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private ArrayBlockingQueue<String> mQueue = new ArrayBlockingQueue<String>(100);
    private AtomicBoolean mStop = new AtomicBoolean(false);

    private OutputStream mOutputStream = null;

    private Socket mSocket = null;

    private static Thread sNetworkThread = null;
    private final Runnable mNetworkRunnable = new Runnable() {

        @Override
        public void run() {
            log("starting network thread");

            try {
                mSocket = new Socket(ARDUINO_IP_ADDRESS, PORT);
                mOutputStream = mSocket.getOutputStream();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                mStop.set(true);
            } catch (IOException e1) {
                e1.printStackTrace();
                mStop.set(true);
            }

            mQueue.clear(); // we only want new values

            try {
                while(!mStop.get()){
                    String val = mQueue.take();
                    Log.e("Queu ", val);
                    if(val != ""){
                        Log.e("sending value ", val);
                        mOutputStream.write((val+"\n").getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally{
                try {
                    mStop.set(true);
                    if(mOutputStream != null) mOutputStream.close();
                    if(mSocket != null) mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            log("returning from network thread");
            sNetworkThread = null;
        }
    };

    public void log(String s){
        Log.d(TAG, s);
    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View v) {


        switch (v.getId())

        {

        case R.id.switch1:

        if (switch1.isChecked()==true)
        {
        mQueue.offer("C01*0"); //scambio dritto
        }
        else{
        mQueue.offer("C01*1"); //scambio destro
        }
        break;


        case R.id.switch2:

        if (switch2.isChecked()==true)
        {
        mQueue.offer("C02*0"); //scambio dritto
        }
        else{
        mQueue.offer("C02*1"); //scambio sinistro
        }

        break;
        case R.id.button1:

        if (button1.isChecked()==true)
        {
        mQueue.offer("C03*0");
        }
        else{
        mQueue.offer("C03*1");
        }
        break;
        case R.id.button2:

        if (button2.isChecked()==true)
        {
        mQueue.offer("C04*0");
        }
        else{
        mQueue.offer("C04*1");
        }

        break;
        case R.id.button3:

        if (button3.isChecked()==true)
        {
        mQueue.offer("C05*1");
        }
        else{
        mQueue.offer("C05*0");
        }

        break;
        case R.id.button4:


        if (button4.isChecked()==true)
        {
        mQueue.offer("C06*1");
        }
        else{
        mQueue.offer("C06*0");
        }


        break;


        }







        }





}


