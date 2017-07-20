package me.yluo.androidble;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

public class DeviceControlActivity extends Activity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private EditText input;

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(true);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(false);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_SERVICE_DISCOVERED.equals(action)) {
                pullData();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        ((TextView) findViewById(R.id.device_address)).setText("设备MAC: " + mDeviceAddress);
        ((TextView) findViewById(R.id.device_name)).setText("设备名称: " + mDeviceName);

        findViewById(R.id.btn_set_time).setOnClickListener(this);
        findViewById(R.id.btn_sync_time).setOnClickListener(this);
        findViewById(R.id.btn_rgb_advance).setOnClickListener(this);
        findViewById(R.id.send_btn).setOnClickListener(this);

        input = (EditText) findViewById(R.id.input);
        mConnectionState = (TextView) findViewById(R.id.device_connect_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        ((Switch) findViewById(R.id.device_led_switch)).setOnCheckedChangeListener(this);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    private void updateConnectionState(final boolean isConnected) {
        runOnUiThread(() -> {
            mConnectionState.setText("连接状态: " + (isConnected ? "connected" : "disconnect"));
        });
    }

    private void displayData(String data) {
        Log.d("BLE", "BLE GET DATA :" + data);
        if (data != null) {
            mDataField.setText(data);
        }
    }

    //从arduino端口获得数据
    private void pullData() {
        System.out.println("====pullData====");
        mBluetoothLeService.sendData(DataType.SHOW_RGB, DataType.SHOW_RGB.toString());
        mBluetoothLeService.sendData(DataType.SHOW_SENSOR, DataType.SHOW_SENSOR.toString());
        mBluetoothLeService.sendData(DataType.SHOW_TIME, DataType.SHOW_TIME.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_time:
            case R.id.btn_sync_time:
                final Calendar mCalendar = Calendar.getInstance();
                //sunday 1
                int year = mCalendar.get(Calendar.YEAR) - 2000;
                int month = mCalendar.get(Calendar.MONTH) + 1;
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                int week = mCalendar.get(Calendar.DAY_OF_WEEK);

                if (v.getId() == R.id.btn_set_time) {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                        int s = mCalendar.get(Calendar.SECOND);
                        mBluetoothLeService.setTime(year, month, day, week, hourOfDay, minute, s);
                    }, 10, 0, true);
                    timePickerDialog.show();
                } else {
                    int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                    int minute = mCalendar.get(Calendar.MINUTE);
                    int second = mCalendar.get(Calendar.SECOND);
                    mBluetoothLeService.setTime(year, month, day, week, hour, minute, second);
                }
                break;
            case R.id.btn_rgb_advance:
                startActivityForResult(new Intent(this, ColorPickerActivity.class), ColorPickerActivity.COLOR_REQUEST);
                break;
            case R.id.send_btn:
                String s = input.getText().toString().trim();
                if (!TextUtils.isEmpty(s)) {
                    mBluetoothLeService.sendData(DataType.CUSTOM, s);
                }
                input.setText(null);
                break;
        }
    }

    //led 开关变化
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //mBluetoothLeService.setRgbState(isChecked);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ColorPickerActivity.COLOR_RESULT) {
            int color = data.getIntExtra(ColorPickerActivity.COLOR_KEY, -1);
            if (color != -1) {
                mBluetoothLeService.setRgb(color);
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_SERVICE_DISCOVERED);
        return intentFilter;
    }
}
