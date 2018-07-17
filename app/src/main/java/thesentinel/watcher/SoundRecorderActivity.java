package thesentinel.watcher;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaRecorder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.UUID;

import static java.lang.Math.round;

public class SoundRecorderActivity extends AppCompatActivity {

    /* Sound Recorder */
    private SoundRecorderController soundRecorderController;
    private TextView amplitudeValue;
    private ConstraintLayout constraintLayout;
    private MediaRecorder mRecorder;
    private static double MAX_AMPLITUDE_THRESHOLD = 80.0;

    /* Bluetooth */
    private BluetoothController bluetoothController;
    private Button btnLightSwitch, btnDisconnect, btnSendMsg;
    private EditText msg;
    private boolean lightStatusOn = false;
    private ProgressDialog progress;
    String address = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recorder);

        /* Toolbar */
        initializeToolbar();

        /* Sound Recorder */
        initializeSoundRecorder();

        /* Bluetooth */
        initializeBlutooth();
    }

    /** SOUND RECORDER **/
    @Override
    protected void onStart() {
        super.onStart();
        soundRecorderController.record();
    }

    public void updateAmplitude(double amplitudeDb) {
        if (amplitudeDb > 0 && amplitudeDb < 1000) {
            amplitudeValue.setText(String.valueOf(round(amplitudeDb)) + " dB");

            boolean triggered = amplitudeDb > MAX_AMPLITUDE_THRESHOLD;

            if (triggered) {
                actionTriggered();
            } else {
                constraintLayout.setBackground(getResources().getDrawable(R.drawable.bg));
            }
        }
    }

    private void sendMsg() {
        String s = msg.getText().toString();
        //bluetoothController.sendMsg(s);
    }

    private void actionTriggered() {
        // do something when actionTriggered
        //bluetoothController.turnOnLed();
    }

    public void setProgress(ProgressDialog progress) {
        this.progress = progress;
    }

    public ProgressDialog getProgress() {
        return progress;
    }

    /** INITIALIZATION **/
    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),DeviceListActivity.class));
                finish();
            }
        });
    }

    private void initializeBlutooth() {
        msg = (EditText) findViewById(R.id.msg);
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceListActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device
        bluetoothController = new BluetoothController(address, this);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                bluetoothController.Disconnect();
                finish();
            }
        });

        btnLightSwitch = (Button) findViewById(R.id.btnLightSwitch);
        btnLightSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (lightStatusOn) {
                    bluetoothController.turnOnLed();
                } else {
                    bluetoothController.turnOffLed();
                }
            }
        });

        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
        btnSendMsg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendMsg();
            }
        });

        bluetoothController.ConncetBT(); //Call the class to connect
    }

    private void initializeSoundRecorder(){
        amplitudeValue = (TextView) findViewById(R.id.amplitudeValue);
        soundRecorderController = new SoundRecorderController(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
