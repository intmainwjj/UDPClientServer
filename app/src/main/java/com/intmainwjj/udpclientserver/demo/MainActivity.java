package com.intmainwjj.udpclientserver.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.intmainwjj.udpclientserver.lib.ByteUtils;
import com.intmainwjj.udpclientserver.lib.UDPClientServer;
import com.intmainwjj.udpclientserver.lib.UDPReceiveCallback;
import com.intmainwjj.udpclientserver.lib.UDPResult;
import com.intmainwjj.udpclientserver.lib.UDPSendCallback;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    UDPClientServer udpClientServer;

    private static final String TAG = MainActivity.class.getSimpleName();

    EditText targetIpInput;
    EditText targetPortInput;
    EditText receivePortInput;
    EditText sendDataInput;
    TextView receiveDataText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        udpClientServer = UDPClientServer.newInstance(MainApplication.getInstance());

        targetIpInput = findViewById(R.id.et_target_ip);
        targetPortInput = findViewById(R.id.et_target_port);
        receivePortInput = findViewById(R.id.et_receive_port);
        sendDataInput = findViewById(R.id.et_send_data);
        receiveDataText = findViewById(R.id.tv_receive_data);

        findViewById(R.id.btn_send_data).setOnClickListener(v -> {
            if (TextUtils.isEmpty(sendDataInput.getText().toString())) {
                Toast.makeText(this, "请输入发送数据", Toast.LENGTH_SHORT).show();
                return;
            }

            int targetPort = 8000;
            int receivePort = 8001;
            try {
                targetPort = Integer.parseInt(targetPortInput.getText().toString());
                receivePort = Integer.parseInt(receivePortInput.getText().toString());
            } catch (Exception e) {

            }

            String targetIp = targetIpInput.getText().toString();
            if (TextUtils.isEmpty(targetIp)) {
                targetIp = "255.255.255.255";
            }

            udpClientServer
                    .setTargetIp(targetIp)
                    .setTargetPort(targetPort)
                    .setReceivePort(receivePort)
                    .setReceiveTimeOut(Integer.MAX_VALUE)
                    .setSendCallback(new UDPSendCallback() {
                        @Override
                        public void onSendInstructionFailed(Throwable throwable) {
                            Log.e(TAG, "onSendInstructionFailed:" + throwable.getMessage());
                        }

                        @Override
                        public void onSendInstructionSuccess(String instruction) {
                            Log.d(TAG, "onSendInstructionSuccess:" + instruction + " success");
                        }
                    })
                    .setReceiveCallback(new UDPReceiveCallback() {
                        @Override
                        public void onReceiveDataSuccess(UDPResult result) {
                            Log.d(TAG, "onReceiveDataSuccess:" + ByteUtils.bytesToHexString(result.getResultData()));
                        }

                        @Override
                        public void onReceiveDataFailed(Throwable throwable) {
                            Log.e(TAG, "onReceiveDataFailed:" + throwable.getMessage());
                        }
                    })
                    .sendBroadcast(sendDataInput.getText().toString().getBytes());
        });
    }
}