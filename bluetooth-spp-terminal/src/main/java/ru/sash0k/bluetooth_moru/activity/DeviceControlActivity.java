package ru.sash0k.bluetooth_moru.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import ru.sash0k.bluetooth_moru.DeviceData;
import ru.sash0k.bluetooth_moru.R;
import ru.sash0k.bluetooth_moru.Utils;
import ru.sash0k.bluetooth_moru.bluetooth.DeviceConnector;
import ru.sash0k.bluetooth_moru.bluetooth.DeviceListActivity;


public final class DeviceControlActivity extends BaseActivity {

    int servo = 90;
    int max=180;
    int min=0;
    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String LOG = "LOG";

    // Подсветка crc
    private static final String CRC_OK = "#FFFF00";
    private static final String CRC_BAD = "#FF0000";

    private static final SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss.SSS");

    private static String MSG_NOT_CONNECTED;
    private static String MSG_CONNECTING;
    private static String MSG_CONNECTED;

    private static DeviceConnector connector;
    private static BluetoothResponseHandler mHandler;

    private StringBuilder logHtml;
    private TextView logTextView;
    private EditText commandEditText;

    // Настройки приложения
    private boolean hexMode, checkSum, needClean;
    private boolean show_timings, show_direction;
    private String command_ending;
    private String deviceName;
    int counter = 0; // 0 정지 1 출발 2 수동
    timeThread t = new timeThread();
    Switch boldSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   PreferenceManager.setDefaultValues(this, R.xml.settings_activity, false);

        if (mHandler == null) mHandler = new BluetoothResponseHandler(this);
        else mHandler.setTarget(this);

        MSG_NOT_CONNECTED = getString(R.string.msg_not_connected);
        MSG_CONNECTING = getString(R.string.msg_connecting);
        MSG_CONNECTED = getString(R.string.msg_connected);

        setContentView(R.layout.activity_terminal_2);
        if (isConnected() && (savedInstanceState != null)) {
            setDeviceName(savedInstanceState.getString(DEVICE_NAME));
        } else getActionBar().setSubtitle(MSG_NOT_CONNECTED);

        //this.logHtml = new StringBuilder();
       // if (savedInstanceState != null) this.logHtml.append(savedInstanceState.getString(LOG));
        boldSwitch = (Switch) findViewById(R.id.switch1);
        boldSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b==true){
                    counter=2;
                    String commandString = "E1\n";
                    // Дополнение команд в hex
                    byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                    if (command_ending != null)
                        command = Utils.concat(command, command_ending.getBytes());
                    if (isConnected()) {
                        connector.write(command);
                    }


                }

                else{
                    counter=0;
                    String commandString = "B1\n";
                    // Дополнение команд в hex
                    byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                    if (command_ending != null)
                        command = Utils.concat(command, command_ending.getBytes());
                    if (isConnected()) {
                        connector.write(command);
                    }


                }

            }
        });
        t.start();

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setButtonDirection(-1);
        joystickRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i("  test n ", String.format("------------x%03d:y%03d", joystickRight.getNormalizedX(), joystickRight.getNormalizedY()));
                int action = motionEvent.getAction();
                servo = joystickRight.getNormalizedX();
                if (action == MotionEvent.ACTION_DOWN) {

                } else if (action == MotionEvent.ACTION_MOVE) {
                    counter =1;
                } else if (action == MotionEvent.ACTION_UP) {
                    counter =0;

                }
                return false;
            }
        });

    }
    // ==========================================================================
void bt_on(){
    String commandString = "B1\n";
    // Дополнение команд в hex
    byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
    if (command_ending != null)
        command = Utils.concat(command, command_ending.getBytes());
    if (isConnected()) {
        connector.write(command);
    }

}
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEVICE_NAME, deviceName);
        if (logTextView != null) {
            outState.putString(LOG, logHtml.toString());
        }
    }
    // ============================================================================
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //tv_count.setText(msg.arg1 +"");
            if (counter == 0) {
                servo = 90;
                String commandString = "T";
                commandString = commandString + String.valueOf(servo) + "\n";

                byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                if (command_ending != null)
                    command = Utils.concat(command, command_ending.getBytes());
                if (isConnected()) {
                    connector.write(command);
                }
            } else if(counter == 1){
                String commandString = "S";
                commandString = commandString + String.valueOf(servo) + "\n";
                // Дополнение команд в hex
                byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                if (command_ending != null)
                    command = Utils.concat(command, command_ending.getBytes());
                if (isConnected()) {
                    connector.write(command);
                }
            }else if(counter == 2){
                String commandString = "E";
                commandString = commandString + String.valueOf(servo) + "\n";
                // Дополнение команд в hex
                byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                if (command_ending != null)
                    command = Utils.concat(command, command_ending.getBytes());
                if (isConnected()) {
                    connector.write(command);
                }
            }
        }
    };
    public class timeThread extends Thread{
        int i = 0;
        @Override
        public void run() {
            while(true){
                Message msg = new Message();
                msg.arg1 = i++;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    /**
     * Проверка готовности соединения
     */
    private boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }
    // ==========================================================================


    /**
     * Разорвать соединение
     */
    private void stopConnection() {
        if (connector != null) {
            connector.stop();
            connector = null;
            deviceName = null;
        }
    }
    // ==========================================================================


    /**
     * Список устройств для подключения
     */
    private void startDeviceListActivity() {
        stopConnection();
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    // ============================================================================


    /**
     * Обработка аппаратной кнопки "Поиск"
     *
     * @return
     */
    @Override
    public boolean onSearchRequested() {
        if (super.isAdapterReady()) startDeviceListActivity();
        return false;
    }
    // ==========================================================================


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control_activity, menu);
        final MenuItem bluetooth = menu.findItem(R.id.menu_search);
        if (bluetooth != null) bluetooth.setIcon(this.isConnected() ?
                R.drawable.ic_action_device_bluetooth_connected :
                R.drawable.ic_action_device_bluetooth);
        return true;
    }
    // ============================================================================

    AlertDialog alert;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_search:
                if (super.isAdapterReady()) {
                    if (isConnected()) stopConnection();
                    else startDeviceListActivity();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                return true;


            case R.id.menu_settings:
                createDialog();


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // ============================================================================
    private Dialog mDialog = null;


    private void createDialog() {
        final View innerView = getLayoutInflater().inflate(R.layout.alert_view, null);
Button rr,ll,ok,reset;
rr = (Button)innerView.findViewById(R.id.rr);
rr.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              String commandString = "p\n";
                                // Дополнение команд в hex
                                byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                                if (command_ending != null)
                                    command = Utils.concat(command, command_ending.getBytes());
                                if (isConnected()) {
                                    connector.write(command);
                                }
                          }
                      });

        ll = (Button)innerView.findViewById(R.id.ll);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commandString = "m\n";
                // Дополнение команд в hex
                byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                if (command_ending != null)
                    command = Utils.concat(command, command_ending.getBytes());
                if (isConnected()) {
                    connector.write(command);
                }
            }
        });
        ok = (Button)innerView.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.cancel();
            }
        });
        reset = (Button)innerView.findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commandString = "f\n";
                // Дополнение команд в hex
                byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
                if (command_ending != null)
                    command = Utils.concat(command, command_ending.getBytes());
                if (isConnected()) {
                    connector.write(command);
                }
            }
        });
        mDialog = new Dialog(this);
        mDialog.setTitle("Title");
        mDialog.setContentView(innerView);



        // Back키 눌렀을 경우 Dialog Cancle 여부 설정
        mDialog.setCancelable(true);

        // Dialog 생성시 배경화면 어둡게 하지 않기
        mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Dialog 밖을 터치 했을 경우 Dialog 사라지게 하기
//      mDialog.setCanceledOnTouchOutside(true);

        // Dialog 밖의 View를 터치할 수 있게 하기 (다른 View를 터치시 Dialog Dismiss)
        mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // Dialog 자체 배경을 투명하게 하기
//      mDialog.getWindow().setBackgroundDrawable
//              (new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Dialog Cancle시 Event 받기
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(DeviceControlActivity.this, "cancle listener",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Dialog Show시 Event 받기
        mDialog.setOnShowListener(new  DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Toast.makeText(DeviceControlActivity.this, "show listener",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Dialog Dismiss시 Event 받기
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Toast.makeText(DeviceControlActivity.this, "dismiss listener",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mDialog.show();

    }

    private void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // hex mode
//        final String mode = Utils.getPrefence(this, getString(R.string.pref_commands_mode));
//        this.hexMode = "HEX".equals(mode);
//        if (hexMode) {
//            commandEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
//            commandEditText.setFilters(new InputFilter[]{new Utils.InputFilterHex()});
//        } else {
//            commandEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//            commandEditText.setFilters(new InputFilter[]{});
//        }
//
//        // checksum
//        final String checkSum = Utils.getPrefence(this, getString(R.string.pref_checksum_mode));
//        this.checkSum = "Modulo 256".equals(checkSum);

//        // Окончание строки
//        this.command_ending = getCommandEnding();
//
//        // Формат отображения лога команд
//        this.show_timings = Utils.getBooleanPrefence(this, getString(R.string.pref_log_timing));
//        this.show_direction = Utils.getBooleanPrefence(this, getString(R.string.pref_log_direction));
//        this.needClean = Utils.getBooleanPrefence(this, getString(R.string.pref_need_clean));
    }
    // ============================================================================





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    if (super.isAdapterReady() && (connector == null)) setupConnector(device);
                    bt_on();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                super.pendingRequestEnableBt = false;
                if (resultCode != Activity.RESULT_OK) {
                    Utils.log("BT not enabled");
                }
                break;
        }
    }
    // ==========================================================================


    /**
     * Установка соединения с устройством
     */
    private void setupConnector(BluetoothDevice connectedDevice) {
        stopConnection();
        try {
            String emptyName = getString(R.string.empty_device_name);
            DeviceData data = new DeviceData(connectedDevice, emptyName);
            connector = new DeviceConnector(data, mHandler);
            connector.connect();
        } catch (IllegalArgumentException e) {
            Utils.log("setupConnector failed: " + e.getMessage());
        }
    }
    // ==========================================================================


    /**
     * Отправка команды устройству
     */
    public void sendCommand(View view) {
        if (commandEditText != null) {
            String commandString = commandEditText.getText().toString();
            if (commandString.isEmpty()) return;

            // Дополнение команд в hex
            if (hexMode && (commandString.length() % 2 == 1)) {
                commandString = "0" + commandString;
                commandEditText.setText(commandString);
            }

            // checksum
            if (checkSum) {
                commandString += Utils.calcModulo256(commandString);
            }

            byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
            if (command_ending != null) command = Utils.concat(command, command_ending.getBytes());
            if (isConnected()) {
                connector.write(command);
                //appendLog(commandString, hexMode, true, needClean);
            }
        }
    }
    // ==========================================================================




    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        getActionBar().setSubtitle(deviceName);
    }
    // ==========================================================================

    /**
     * Обработчик приёма данных от bluetooth-потока
     */
    private static class BluetoothResponseHandler extends Handler {
        private WeakReference<DeviceControlActivity> mActivity;

        public BluetoothResponseHandler(DeviceControlActivity activity) {
            mActivity = new WeakReference<DeviceControlActivity>(activity);
        }

        public void setTarget(DeviceControlActivity target) {
            mActivity.clear();
            mActivity = new WeakReference<DeviceControlActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            DeviceControlActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:

                        Utils.log("MESSAGE_STATE_CHANGE: " + msg.arg1);
                        final ActionBar bar = activity.getActionBar();
                        switch (msg.arg1) {
                            case DeviceConnector.STATE_CONNECTED:
                                bar.setSubtitle(MSG_CONNECTED);

                                break;
                            case DeviceConnector.STATE_CONNECTING:
                                bar.setSubtitle(MSG_CONNECTING);
                                break;
                            case DeviceConnector.STATE_NONE:
                                bar.setSubtitle(MSG_NOT_CONNECTED);
                                break;
                        }
                        activity.invalidateOptionsMenu();
                        break;

                    case MESSAGE_READ:
                        final String readMessage = (String) msg.obj;
                        if (readMessage != null) {
                           // activity.appendLog(readMessage, false, false, activity.needClean);
                        }
                        break;

                    case MESSAGE_DEVICE_NAME:
                        activity.setDeviceName((String) msg.obj);
                        break;

                    case MESSAGE_WRITE:
                        // stub
                        break;

                    case MESSAGE_TOAST:
                        // stub
                        break;
                }
            }
        }
    }
    // ==========================================================================


    @Override
    public synchronized void onResume() {
        super.onResume();
        counter =0;
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        counter =0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        counter =0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        counter =0;
    }
}