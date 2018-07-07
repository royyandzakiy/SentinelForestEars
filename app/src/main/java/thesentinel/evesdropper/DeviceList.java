package thesentinel.evesdropper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity {

    //widgets
    Button btnPaired;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    /** NOISE ALERT **/
    /* constants */
    private static final String LOG_TAG = "NoiseAlert";
    private static final int POLL_INTERVAL = 300;
    private static final int NO_NUM_DIALOG_ID=1;
    private static final String[] REMOTE_CMDS = {"start", "stop", "panic"};

    /* running state */
    private boolean mAutoResume = false;
    private boolean mRunning = false;
    private boolean mTestMode = false;
    private int mTickCount = 0;
    private int mHitCount =0;

    /* config state */
    private int mThreshold;
    private int mPollDelay;
    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mStatusView;
    private ImageView mActivityLed;
    private SoundLevelView mDisplay;

    /* data source */
    private SoundMeter mSensor;

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            start();
        }
    };
    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            if (mTestMode) updateDisplay("testing...", amp);
            else           updateDisplay("monitoring...", amp);

            if ((amp > mThreshold) && !mTestMode) {
                mHitCount++;
                if (mHitCount > 5){
                    // callForHelp();
                    // do nothing...
                    return;
                }
            }

            mTickCount++;
            setActivityLed(mTickCount% 2 == 0);

            if ((mTestMode || mPollDelay > 0) && mTickCount > 100) {
                if (mTestMode) {
                    stop();
                } else {
                    sleep();
                }
            } else {
                mHandler.postDelayed(mPollTask, POLL_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("DEBUG","DeviceList::onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //Calling widgets
        btnPaired = (Button)findViewById(R.id.button);
        devicelist = (ListView)findViewById(R.id.listView);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });

        //Noise Alert
        mStatusView = (TextView) findViewById(R.id.status);
        mActivityLed = (ImageView) findViewById(R.id.activity_led);

        mSensor = new SoundMeter();
        mDisplay = (SoundLevelView) findViewById(R.id.volume);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
    }


    private void updateDisplay(String status, double signalEMA) {
        mStatusView.setText(status);

        mDisplay.setLevel((int)signalEMA, mThreshold);
    }

    private void setActivityLed(boolean on) {
        mActivityLed.setVisibility( on ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    private void start() {
        Log.d("DEBUG","DeviceList::onStart()");
        mSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    private void stop() {
        Log.d("DEBUG","DeviceList::onStop()");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mSensor.stop();
        mDisplay.setLevel(0,0);
    }

    private void sleep() {
        Log.d("DEBUG","DeviceList::onSleep()");
        mSensor.stop();
    }

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(DeviceList.this, ledControl.class);

            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
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
