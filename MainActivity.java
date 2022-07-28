package org.techtown.client;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity {
    // 소켓통신에 필요한것
    private String html = "";
    private Handler mHandler;
    JSONObject json = null;
    private Socket socket;

    private String loca;

    private String ip="172.29.91.231"; // 서버의 ip주소는 고정
    private int port = 8080; // port 번호도 고정
    private int kiosk_num = 10;
    private int store_num = 100;

    private ImageView iv;
    private String text;

    String memberID;
    String name;

    public static final int REQUEST_CODE_MENU = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        Log.w("connect","연결 하는중");
// 받아오는거
        Thread checkUpdate = new Thread() {
            public void run() {
// ip받기
                String newip = ip;

// 서버 접속
                try {
                    socket = new Socket(newip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");

//                try {
//
//                    dos.writeObject("안드로이드에서 서버로 연결요청");
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.w("버퍼", "버퍼생성 잘못됨");
//
//                }
                Log.w("버퍼","버퍼생성 잘됨");

                iv = (ImageView)findViewById(R.id.qrcode);

                loca=getLocalIpAddress();
                Log.d("My Ip Address is ", loca);
                text =  loca+"/"+store_num+"/"+kiosk_num;

                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try{
                    BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    iv.setImageBitmap(bitmap);
                }catch (Exception e){}

// 서버에서 계속 받아옴 - 한번은 문자, 한번은 숫자를 읽음. 순서 맞춰줘야 함.

                try {

                    //ObjectOutputStream dos = new ObjectOutputStream(socket.getOutputStream()); // output에 보낼꺼 넣음
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    //DataInputStream dis = new DataInputStream(socket.getInputStream()); // input에 받을꺼 넣어짐
                    String value=in.readLine();
                    Log.w("서버에서 받아온 값 "," :"+value);
                    json = new JSONObject(value);
                    Log.w("변환한 값 "," :"+json);

                    memberID = json.getString("memberID");
                    name = json.getString("name");

                    Log.w("멤버 ID : ",""+memberID);
                    Log.w("이름 : ",""+name);

                    //println(""+name+"님 안녕하세요");
                    // 화면 전환해서 사용자 이름 출력시키기

                }catch (Exception e){
                    Log.w("서버에서 받아온 값 ","뭐애");
                    e.printStackTrace();
                }

            }
        };
// 소켓 접속 시도, 버퍼생성
        checkUpdate.start();

    }


    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public void println(String data) {
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
    }
}

