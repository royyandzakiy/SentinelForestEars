package thesentinel.watcher;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.round;

public class SoundRecorderController {
    private MediaRecorder mRecorder;
    private SoundRecorderActivity activity;
    private Thread recordingThread;

    public SoundRecorderController(SoundRecorderActivity activity) {
        this.activity = activity;
    }

    public synchronized void startRecording() {
        Log.d("DEBUG","SoundRecorderController::startRecording");
        if (mRecorder != null) {
            return;
        }
        recordingThread =
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        record();
                    }
                });
        recordingThread.start();
    }

    public synchronized void stopRecording() {
        Log.d("DEBUG","SoundRecorderController::stopRecording");
        if (recordingThread == null) {
            return;
        }
        //cleanUp();
        recordingThread = null;
    }

    private void record() {
        try {
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

    public void cleanUp() {
        if(mRecorder != null) {
            try {
                mRecorder.stop();
            } finally {
                mRecorder.release();
                mRecorder = null;
            }
        }
    }

    public void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, 13);
        }
    }

    public boolean checkRecordingPermission()
    {
        int result  = activity.getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.RECORD_AUDIO);
        return (result == PackageManager.PERMISSION_GRANTED);
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
