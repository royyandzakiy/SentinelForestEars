package thesentinel.watcher;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.round;

public class SoundRecorderController {
    private MediaRecorder mRecorder;
    private SoundRecorderActivity activity;

    public SoundRecorderController(SoundRecorderActivity activity) {
        this.activity = activity;
        getPermission();
    }

    public void record() {
        if (mRecorder == null) {
            try {
                Log.d("DEBUG","mRecorder NULL");
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");

                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new RecorderTask(mRecorder), 0, 50);
                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("ERROR",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) activity.getApplicationContext(), new String[]{Manifest.permission.RECORD_AUDIO},
                    0);
        }
    }

    private class RecorderTask extends TimerTask {
        private MediaRecorder mRecorder;

        public RecorderTask(MediaRecorder mRecorder) {
            this.mRecorder = mRecorder;
        }

        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int amplitude = mRecorder.getMaxAmplitude();
                    double amplitudeDb = 20 * Math.log10((double) Math.abs(amplitude));
                    Log.d("DEBUG","amplitudeDb:" + amplitudeDb);
                    activity.updateAmplitude(round(amplitudeDb));
                }
            });
        }
    }

}
