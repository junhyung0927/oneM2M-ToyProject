package kr.re.keti.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.airbnb.lottie.LottieAnimationView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.re.keti.model.AE;
import kr.re.keti.model.ApplicationEntityObject;
import kr.re.keti.model.CSEBase;
import kr.re.keti.model.ContentInstanceObject;
import kr.re.keti.model.ContentSubscribeObject;
import kr.re.keti.service.mqtt.MqttClientRequest;
import kr.re.keti.util.MqttClientRequestParser;
import kr.re.keti.util.ParseElementXml;


public class MainActivity extends AppCompatActivity implements Button.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public Button btnRetrieve;

    public LottieAnimationView lottie_air_animation;
    public LottieAnimationView lottie_light_animation;
    public LottieAnimationView lottie_air_control_animation;
    public LottieAnimationView lottie_loading_animation;

    public ToggleButton btnControl_Led;
    public Switch Switch_MQTT;
    public TextView textView_airConditioner_data;
    public TextView textView_led_data;
    public TextView textView_preview;
    public TextView textView_air_control;
    public TextView textView_led_loading;
    public Button button_airConditioner_off;
    public Button button_airConditioner_on;

    public Handler handler;
    private static CSEBase csebase = new CSEBase();
    private static AE ae = new AE();
    private static String TAG = "MainActivity";
    private String MQTTPort = "1883";
    private String ServiceAEName = "daewon_demo";
    private String MQTT_Req_Topic = "";
    private String MQTT_Resp_Topic = "";
    private MqttAndroidClient mqttClient = null;


    // Main
    public MainActivity() {
        handler = new Handler();
    }

    /* onCreate */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        bind(R.layout.activity_main);
        setContentView(R.layout.activity_main);
//        binding.setLifecycleOwner(this);


        lottie_air_animation = (LottieAnimationView) findViewById(R.id.lottie_air_animation);
        lottie_light_animation = (LottieAnimationView) findViewById(R.id.lottie_light_animation);
        lottie_air_control_animation = (LottieAnimationView) findViewById(R.id.lottie_air_control_animation);
        lottie_loading_animation = (LottieAnimationView) findViewById(R.id.lottie_loading_animation);

        Switch_MQTT = (Switch) findViewById(R.id.switch_mqtt);
        btnControl_Led = (ToggleButton) findViewById(R.id.btnControl_Led);
        button_airConditioner_off = (Button) findViewById(R.id.button_airConditioner_off);
        button_airConditioner_on = (Button) findViewById(R.id.button_airConditioner_on);
        textView_airConditioner_data = (TextView) findViewById(R.id.textView_airConditioner_data);
        textView_led_data = (TextView) findViewById(R.id.textView_led_data);
        textView_preview = (TextView) findViewById(R.id.textView_preview);
        textView_air_control = (TextView) findViewById(R.id.textView_air_control);

        button_airConditioner_on.setOnClickListener(this);
        button_airConditioner_off.setOnClickListener(this);
        Switch_MQTT.setOnCheckedChangeListener(this);
        btnControl_Led.setOnClickListener(this);

        // Create AE and Get AEID
        GetAEInfo();
    }

    /* AE Create for android AE */
    public void GetAEInfo() {

        csebase.setInfo("192.168.10.238", "7579", "Mobius", "1883");
        // AE Create for Android AE
        ae.setAppName("wisoftApp");
        aeCreateRequest aeCreate = new aeCreateRequest();

        aeCreate.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "** AE Create ResponseCode[" + msg + "]");
                        if (Integer.parseInt(msg) == 201) {
//                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/" + ae.getAEid() + "_sub" + "/#";
                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/Sdaewon_demo/json";
                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/" + ae.getAEid() + "_sub" + "/json";
                            Log.d(TAG, "ReqTopic[" + MQTT_Req_Topic + "]");
                            Log.d(TAG, "ResTopic[" + MQTT_Resp_Topic + "]");
                        } else { // If AE is Exist , GET AEID
                            aeRetrieveRequest aeRetrive = new aeRetrieveRequest();
                            aeRetrive.setReceiver(new IReceived() {
                                public void getResponseBody(final String resmsg) {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, "** AE Retrive ResponseCode[" + resmsg + "]");
//                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/" + ae.getAEid() + "_sub" + "/#";
                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/Sdaewon_demo/json";
                                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/" + ae.getAEid() + "_sub" + "/json";
                                            Log.d(TAG, "ReqTopic[" + MQTT_Req_Topic + "]");
                                            Log.d(TAG, "ResTopic[" + MQTT_Resp_Topic + "]");
                                        }
                                    });
                                }
                            });
                            aeRetrive.start();
                        }
                    }
                });
            }
        });
        aeCreate.start();
    }

    /* Switch - Get Co2 Data With MQTT */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Log.d(TAG, "MQTT Create");
            button_airConditioner_off.setVisibility(View.VISIBLE);
            button_airConditioner_on.setVisibility(View.VISIBLE);
            textView_preview.setVisibility(View.GONE);
            textView_airConditioner_data.setVisibility(View.VISIBLE);

            MQTT_Create(true);
        } else {
            lottie_loading_animation.setVisibility(View.GONE);
            button_airConditioner_off.setVisibility(View.GONE);
            button_airConditioner_on.setVisibility(View.GONE);
            lottie_air_animation.setVisibility(View.GONE);
            textView_airConditioner_data.setVisibility(View.GONE);
            textView_preview.setVisibility(View.VISIBLE);
            textView_air_control.setVisibility(View.GONE);

            Log.d(TAG, "MQTT Close");
            MQTT_Create(false);
        }
    }

    /* MQTT Subscription */
    public void MQTT_Create(boolean mtqqStart) {
        if (mtqqStart && mqttClient == null) {
            lottie_loading_animation.setVisibility(View.VISIBLE);

            /* Subscription Resource Create to Yellow Turtle */
            SubscribeResource subcribeResource = new SubscribeResource();
            subcribeResource.setReceiver(new IReceived() {
                @Override
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            System.out.println("subscription 완료");
                        }
                    });
                }
            });
            subcribeResource.start();

            /* MQTT Subscribe */
            mqttClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + csebase.getHost() + ":" + csebase.getMQTTPort(), MqttClient.generateClientId());
            //http://192.168.10.75:7579/Mobius/IYAHN_DEMO/co2

            mqttClient.setCallback(mainMqttCallback);
            try {
                IMqttToken token = mqttClient.connect();
                token.setActionCallback(mainIMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            /* MQTT unSubscribe or Client Close */
            mqttClient.setCallback(null);
            mqttClient.close();
            mqttClient = null;
//            MQTT_Create(true);
        }
    }

    /* MQTT Listener */
    private IMqttActionListener mainIMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "onSuccess");
            String payload = "";
            int mqttQos = 1; /* 0: NO QoS, 1: No Check , 2: Each Check */

            MqttMessage message = new MqttMessage(payload.getBytes());

            System.out.println(MQTT_Req_Topic);

            try {
                if(mqttClient != null){
                    mqttClient.subscribe(MQTT_Req_Topic, mqttQos);
                }
            } catch (MqttException e) {
                System.out.println("섭 에러");
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "onFailure");
        }
    };
    /* MQTT Broker Message Received */
    private MqttCallback mainMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            lottie_loading_animation.setVisibility(View.GONE);
            lottie_air_animation.resumeAnimation();
            lottie_air_animation.setProgress(0.1f);
            lottie_air_animation.setVisibility(View.VISIBLE);
            Log.d(TAG, "messageArrived");

            JSONObject jsonObject = new JSONObject(String.valueOf(message));
            JSONObject pc = new JSONObject(jsonObject.get("pc").toString());
            JSONObject m2m_sgn = new JSONObject(pc.get("m2m:sgn").toString());
            JSONObject nev = new JSONObject(m2m_sgn.get("nev").toString());
            JSONObject rep = new JSONObject(nev.get("rep").toString());
            JSONObject m2m_cin = new JSONObject(rep.get("m2m:cin").toString());

            String tem = m2m_sgn.get("sur").toString();
            String temParse = tem.split("/")[2];

            if(temParse.equals("temperature")){
                textView_airConditioner_data.setText("");
                textView_airConditioner_data.setText("연구실 에어컨 온도 \r\n\r\n" + m2m_cin.get("con"));
            }

            Log.d(TAG, "Notify ResMessage:" + message.toString());

            /* Json Type Response Parsing */
            String retrqi = MqttClientRequestParser.notificationJsonParse(message.toString());
            Log.d(TAG, "RQI[" + retrqi + "]");

            String responseMessage = MqttClientRequest.notificationResponse(retrqi);
            Log.d(TAG, "Receive OK ResMessage [" + responseMessage + "]");

            /* Make json for MQTT Response Message */
            MqttMessage res_message = new MqttMessage(responseMessage.getBytes());

            try {
                mqttClient.publish(MQTT_Resp_Topic, res_message);
            } catch (MqttException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }
    };

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_airConditioner_off: {

                lottie_loading_animation.setVisibility(View.GONE);
                lottie_air_control_animation.resumeAnimation();
                lottie_air_control_animation.setVisibility(View.VISIBLE);

                lottie_air_animation.setVisibility(View.GONE);
                textView_airConditioner_data.setVisibility(View.GONE);

                textView_air_control.setVisibility(View.VISIBLE);

                AirOffControlRequest req = new AirOffControlRequest("off");
                try {
                    mqttClient.unsubscribe(MQTT_Req_Topic);
                    System.out.println("unsub");
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                //에어컨 제어 완료시 나올 텍스트 뷰...
                            }
                        });
                    }
                });
                req.start();
                break;
            }

            case R.id.button_airConditioner_on: {
                lottie_air_control_animation.setVisibility(View.GONE);
                textView_air_control.setVisibility(View.GONE);

                textView_airConditioner_data.setVisibility(View.VISIBLE);
                lottie_loading_animation.setVisibility(View.VISIBLE);
                AirOnControlRequest req = new AirOnControlRequest("on");

                mqttClient = null;
                mqttClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + csebase.getHost() + ":" + csebase.getMQTTPort(), MqttClient.generateClientId());
                //http://192.168.10.75:7579/Mobius/IYAHN_DEMO/co2

                mqttClient.setCallback(mainMqttCallback);
                try {
                    IMqttToken token = mqttClient.connect();
                    token.setActionCallback(mainIMqttActionListener);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                textView_airConditioner_data.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                req.start();
                break;
            }

            case R.id.btnControl_Led: {
                if (((ToggleButton) v).isChecked()) {
                    textView_airConditioner_data.setVisibility(View.GONE);
                    textView_preview.setVisibility(View.VISIBLE);
                    ControlRequest req = new ControlRequest("on");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        textView_preview.setVisibility(View.GONE);
                                        lottie_light_animation.resumeAnimation();
                                        lottie_light_animation.setVisibility(View.VISIBLE);

                                        JSONObject jsonObject = new JSONObject(msg);
                                        JSONObject jsonObject1 = new JSONObject(jsonObject.get("m2m:cin").toString());
                                        textView_led_data.setVisibility(View.VISIBLE);
                                        textView_led_data.setText("LED 센서 \r\n\r\n" + jsonObject1.get("con"));
                                    } catch (JSONException e) {
//                                        textView_led_loading.setVisibility(View.VISIBLE);
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    req.start();
                } else {
//                    textView_led_loading.setVisibility(View.GONE);
                    lottie_light_animation.setVisibility(View.GONE);
                    ControlRequest req = new ControlRequest("off");
                    textView_preview.setVisibility(View.VISIBLE);
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        JSONObject jsonObject = new JSONObject(msg);
                                        JSONObject jsonObject1 = new JSONObject(jsonObject.get("m2m:cin").toString());
                                        textView_led_data.setText("LED 센서 \r\n\r\n" + jsonObject1.get("con"));
                                        textView_led_data.setVisibility(View.GONE);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    req.start();
                }
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    /* Response callback Interface */
    public interface IReceived {
        void getResponseBody(String msg);
    }

    /* Retrieve airConditioner Sensor */
    class RetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());
        private IReceived receiver;
        private String ContainerName = "temperature";

        public RetrieveRequest(String containerName) {
            this.ContainerName = containerName;
        }

        public RetrieveRequest() {
        }

        public void setReceiver(IReceived handler) {
            this.receiver = handler;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + ContainerName + "/" + "latest";
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                StringBuilder strResp = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String strLine = "";
                while ((strLine = in.readLine()) != null) {
                    strResp.append(strLine);
                }

                if (strResp.toString() != "") {
                    receiver.getResponseBody(strResp.toString());
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }

    /* AirOff Control*/
    class AirOffControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;
        private String container_name = "aircon";

        public ContentInstanceObject contentinstance;

        //led con 값
        public AirOffControlRequest(String comm) {
            contentinstance = new ContentInstanceObject();
            contentinstance.setContent(comm);
        }

        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqContent = contentinstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder resp = new StringBuilder();
                String strLine = "";
                while ((strLine = in.readLine()) != null) {
                    resp.append(strLine);
                }
                if (resp.toString() != "") {
                    receiver.getResponseBody(resp.toString());
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    /* AirOff Control*/
    class AirOnControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;
        private String container_name = "aircon";

        public ContentInstanceObject contentinstance;

        //led con 값
        public AirOnControlRequest(String comm) {
            contentinstance = new ContentInstanceObject();
            contentinstance.setContent(comm);
        }

        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqContent = contentinstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder resp = new StringBuilder();
                String strLine = "";
                while ((strLine = in.readLine()) != null) {
                    resp.append(strLine);
                }
                if (resp.toString() != "") {
                    receiver.getResponseBody(resp.toString());
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    /* Request Control LED */
    class ControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;
        private String container_name = "led";

        public ContentInstanceObject contentinstance;

        //led con 값
        public ControlRequest(String comm) {
            contentinstance = new ContentInstanceObject();
            contentinstance.setContent(comm);
        }

        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqContent = contentinstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder resp = new StringBuilder();
                String strLine = "";
                while ((strLine = in.readLine()) != null) {
                    resp.append(strLine);
                }
                if (resp.toString() != "") {
                    System.out.println("HTTP 과부하");
                    receiver.getResponseBody(resp.toString());
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    /* Request AE Creation */
    static class aeCreateRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        String TAG = aeCreateRequest.class.getName();
        private IReceived receiver;
        int responseCode = 0;
        public ApplicationEntityObject applicationEntity;

        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        public aeCreateRequest() {
            applicationEntity = new ApplicationEntityObject();
            applicationEntity.setResourceName(ae.getappName());
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-Origin", "S" + ae.getappName());
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-NM", ae.getappName());
                System.out.println();
                String reqXml = applicationEntity.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqXml.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqXml.getBytes());
                dos.flush();
                dos.close();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 201) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder resp = new StringBuilder();
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp.append(strLine);
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp.toString(), "aei");
                    ae.setAEid(aei);
                    Log.d(TAG, "Create Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody(Integer.toString(responseCode));
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }

        }
    }

    /* Retrieve AE-ID */
    class aeRetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        private IReceived receiver;
        int responseCode = 0;

        public aeRetrieveRequest() {
        }

        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ae.getappName();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "Sandoroid");
                conn.setRequestProperty("nmtype", "short");
                conn.connect();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 200) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder resp = new StringBuilder();
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp.append(strLine);
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp.toString(), "aei");
                    ae.setAEid(aei);
                    //Log.d(TAG, "Retrieve Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody(Integer.toString(responseCode));
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    /* Subscribe air conditional Content Resource */
    class SubscribeResource extends Thread {
        private final Logger LOG = Logger.getLogger(SubscribeResource.class.getName());
        private IReceived receiver;
        private String container_name = "temperature"; //change to control container name

        public ContentSubscribeObject subscribeInstance;

        public SubscribeResource() {
            subscribeInstance = new ContentSubscribeObject();
            subscribeInstance.setUrl(csebase.getHost());
            subscribeInstance.setResourceName(ae.getAEid() + "_rn3");
            subscribeInstance.setPath(ae.getAEid() + "_sub");
            subscribeInstance.setOrigin_id(ae.getAEid());
        }

        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml; ty=23");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqmqttContent = subscribeInstance.makeXML();

                conn.setRequestProperty("Content-Length", String.valueOf(reqmqttContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqmqttContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine = "";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }

                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                //이미 sub을 생성했기에, resource name이 겹쳐서 생기는 오류
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
}