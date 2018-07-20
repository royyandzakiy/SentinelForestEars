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

import ca.uol.aig.fftpack.RealDoubleFFT;

public class SoundRecorderController {
    private MediaRecorder mRecorder;
    private SoundRecorderActivity activity;
    private Thread recordingThread;

    public SoundRecorderController(SoundRecorderActivity activity) {
        this.activity = activity;
    }

    public synchronized void startRecording() {

    }

    public synchronized void stopRecording() {

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
}
