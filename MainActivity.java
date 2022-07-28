package org.techtown.client;

import android.content.Intent;
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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.Buffer;
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
    Socket sock;

    String memberID;
    String name;

    Handler handler = new Handler();

    public static final int REQUEST_CODE_MENU = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.qrcode);
        loca = getLocalIpAddress();
        Log.d("My Ip Address is ", loca);
        text = loca + "/" + store_num + "/" + kiosk_num;

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            iv.setImageBitmap(bitmap);
        } catch (Exception e) {
        }

        mHandler = new Handler();
        Log.w("connect","연결 하는중");
// 받아오는거
        Thread checkUpdate = new Thread() {
            public void run() {
                synchronized (this) {
                    startServer();
                    Log.d("DDDDDD", "안아ㅗㅆ음");
// ip받기
//                String newip = ip;
//
//// 서버 접속

//                try {
//                    socket = new Socket(loca, port);
//                    Log.w("서버 접속됨", "서버 접속됨");
//                } catch (IOException e1) {
//                    Log.w("서버접속못함", "서버접속못함");
//                    e1.printStackTrace();
//                }
//
//                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");
//
//
//                Log.w("버퍼","버퍼생성 잘됨");
//


// 서버에서 계속 받아옴 - 한번은 문자, 한번은 숫자를 읽음. 순서 맞춰줘야 함.

                    try {
                        //BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
                        DataInputStream is = new DataInputStream(sock.getInputStream());
                        //String value = in.readLine();
                        String value = is.readUTF().toString();
                        Log.w("JSON_READ_TAG", "서버에서 받아온 값 :" + value);
                        json = new JSONObject(value);
                        Log.w("변환한 값 ", " :" + json.toString());

                        memberID = json.getString("userid");
                        name = json.getString("name");

                        Log.w("멤버 ID : ", "" + memberID);
                        Log.w("이름 : ", "" + name);

                        // 화면 전환해서 사용자 이름 출력시키기
                        Intent intent = new Intent(getApplicationContext(), WhoUser.class);
                        intent.putExtra("name", name);
                        startActivityForResult(intent, 1);

                    } catch (EOFException e) {
                        Log.d("JSON_READ_TAG", "EOF 예외: " + e.getMessage());
                    }
                    catch (Exception e) {
                        Log.w("서버에서 받아온 값 ", "뭐애");
                        e.printStackTrace();
                    }

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

    public void startServer() {
        try {
            int portNumber = 5555;

            ServerSocket server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(portNumber));
            printServerLog("서버 시작함 : " + portNumber);

            while (true) {
                sock = server.accept();
                InetAddress clientHost = sock.getLocalAddress();
                int clientPort = sock.getPort();
                printServerLog("클라이언트 연결됨 : " + clientHost + " : " + clientPort);

                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                //DataInputStream instream = new DataInputStream(sock.getInputStream());
                //String obj = instream.readLine().toString();
                String obj = in.readLine();
                printServerLog("데이터 받음 : " + obj.toString());

                DataOutputStream outstream = new DataOutputStream(sock.getOutputStream());
                outstream.writeUTF(obj.toString() + " from Server.");
                outstream.flush();
                printServerLog("데이터 보냄.");

                //sock.close();
            }
        } catch(BindException e) {
            Log.d("BIND_LOG", "바인드 예오: " + e.getMessage());
        } catch(StreamCorruptedException e) {
            Log.d("SCE_LOG", "SCE 예외: " + e.getMessage());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void printServerLog(final String data) {
        Log.d("MainActivity", data);

        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.w("받은 값",""+data);
            }
        });
    }
}



