package me.yluo.androidble;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class DeviceControlActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private List<MyDataItem> listDatas = new ArrayList<>();
    private Switch ledSwitch;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private MyServiceAdaper gattServiceAdapter;

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
                listDatas.clear();
                gattServiceAdapter.notifyDataSetChanged();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                gattServiceAdapter.setDatas(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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
        findViewById(R.id.btn_set_date).setOnClickListener(this);
        findViewById(R.id.btn_rgb_advance).setOnClickListener(this);
        ledSwitch = (Switch) findViewById(R.id.device_led_switch);
        ListView mGattServicesList = (ListView) findViewById(R.id.gatt_services_list);
        mConnectionState = (TextView) findViewById(R.id.device_connect_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        ledSwitch.setOnCheckedChangeListener(this);

        gattServiceAdapter = new MyServiceAdaper();
        mGattServicesList.setOnItemClickListener(gattServiceAdapter);
        mGattServicesList.setAdapter(gattServiceAdapter);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private void updateConnectionState(final boolean isConnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText("连接状态: " + (isConnected ? "connected" : "disconnect"));
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
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
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_time:
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, null, 10, 0, true);
                timePickerDialog.show();
                break;
            case R.id.btn_set_date:
                final Calendar mCalendar = Calendar.getInstance();
                //sunday 1
                int week = mCalendar.get(Calendar.DAY_OF_WEEK);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, null,
                        mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.btn_rgb_advance:
                startActivityForResult(new Intent(this, ColorPickerActivity.class), ColorPickerActivity.COLOR_REQUEST);
                break;
        }
    }

    //led 开关变化
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mBluetoothLeService.setRgbState(isChecked);
    }

    private class MyDataItem {
        boolean isHeader;
        String name;
        String uuid;
        int property;

        public MyDataItem(boolean isHeader, String name, String uuid, int property) {
            this.isHeader = isHeader;
            this.name = name;
            this.uuid = uuid;
            this.property = property;
        }
    }

    private class MyServiceAdaper extends BaseAdapter implements AdapterView.OnItemClickListener {
        private List<BluetoothGattService> gattServices;
        private LayoutInflater mInflator;

        MyServiceAdaper() {
            mInflator = DeviceControlActivity.this.getLayoutInflater();
        }

        void setDatas(List<BluetoothGattService> gattServices) {
            this.gattServices = gattServices;
            if (gattServices == null) return;
            String uuid = null;
            String unknownServiceString = getResources().getString(R.string.unknown_service);
            String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
            listDatas.clear();
            for (BluetoothGattService gattService : gattServices) {
                uuid = gattService.getUuid().toString();
                MyDataItem item = new MyDataItem(true, GattUuids.lookup(uuid, unknownServiceString), uuid, -1);
                listDatas.add(item);
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    uuid = gattCharacteristic.getUuid().toString();
                    MyDataItem itemChild = new MyDataItem(false, GattUuids.lookup(uuid, unknownCharaString), uuid, gattCharacteristic.getProperties());
                    listDatas.add(itemChild);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return listDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return listDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            MyDataItem dataItem = listDatas.get(position);
            if (dataItem.isHeader) {
                view = mInflator.inflate(R.layout.item_service_header, null);
            } else {
                view = mInflator.inflate(R.layout.item_service, null);
                TextView t = (TextView) view.findViewById(R.id.service_property);
                if (dataItem.property < 0) {
                    t.setVisibility(View.INVISIBLE);
                } else {
                    t.setVisibility(View.VISIBLE);
                    String property = "";
                    if ((dataItem.property | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        property += " R";
                    }

                    if ((dataItem.property | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        property += " W";
                    }

                    if ((dataItem.property | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                        property += " WN";
                    }

                    if ((dataItem.property | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        property += " N";
                    }

                    t.setText(property);
                }
            }
            ((TextView) view.findViewById(R.id.service_name)).setText(dataItem.name);
            ((TextView) view.findViewById(R.id.device_uuid)).setText(dataItem.uuid);
            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (gattServices == null || listDatas.size() < position || listDatas.get(position).isHeader)
                return;
            String uuid = listDatas.get(position).uuid;
            for (BluetoothGattService s : gattServices) {
                BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(uuid));
                if (c != null) {
                    final int propertys = c.getProperties();
                    if ((propertys | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBluetoothLeService.readCharacteristic(c);
                    }

                    //if ((propertys | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    //    mNotifyCharacteristic = characteristic;
                    //    mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    //}
                    break;
                }
            }
        }
    }

}
