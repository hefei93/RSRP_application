package com.example.leesujin.ffffinal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.text.*;
import java.util.Date;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {


    TextView txtRsrp;
    String content;
    int asulevel;
    int cqi;
    int dbm;
    int level;
    int rsrp;
    int rsrq;
    int rssnr;
    int tadvanced;
    int state = 0;
    long start = System.currentTimeMillis();

    final static String foldername = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sujin/";
    //final static String foldername = "sdcard/TestLog";
    final static String filename = "data.txt";

    TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //버튼클릭
    public void mOnFileWrite(View v){
        state= 1;
        Log.i("button","on");
        Log.i("path",foldername);

        txtRsrp = (TextView)findViewById(R.id.txtRsrp); //xml파일에 추가하기

        //txt파일에 날짜 입력
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        WriteTextFile(foldername, filename, 0 ,now);

        //manager
        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkType()==TelephonyManager.NETWORK_TYPE_LTE){
            Log.i("process","telephony");

            //리스너 등록
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    public void mOffFileWrite(View v){
        Log.i("button","off");
        state = 0;
    }

    //리스너
    PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            //RSRP (Reference Signal Received Power) - 단위 dBm (절대크기). - 단말에 수신되는 Reference Signal의 Power
            //strSignal = signalStrength.toString();
            //Log.d("SignalStrength", strSignal);
            cellsignal();

            if (state==1) {
                Log.i("process","writeTextFile");
                Log.i("data",String.valueOf(rsrp));
                long end = System.currentTimeMillis();
                int time = (int) (end - start);
                WriteTextFile(foldername, filename, time, content);
                //start = System.currentTimeMillis();
            }
        }
    };

    public void cellsignal(){
        Log.i("Process", "cellsignal function");
        try {
            final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            for (final CellInfo info : tm.getAllCellInfo()) {
                Log.i("Process","for, getAllCellInfo");
                if (info instanceof CellInfoLte) {
                    final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                    // do what you need
                    asulevel = lte.getAsuLevel();
                    cqi = lte.getCqi();
                    dbm = lte.getDbm();
                    level = lte.getLevel();
                    rsrp = lte.getRsrp();
                    rsrq = lte.getRsrq();
                    rssnr = lte.getRssnr();
                    tadvanced = lte.getTimingAdvance();
                    Log.i("Process : asu ",String.valueOf(asulevel));
                    Log.i("Process : cqi ",String.valueOf(cqi));
                    Log.i("Process : dbm ",String.valueOf(dbm));
                    Log.i("Process : level ",String.valueOf(level));
                    Log.i("Process : rsrp ",String.valueOf(rsrp));
                    Log.i("Process : rsrq ", String.valueOf(rsrq));
                    Log.i("Process : rssnr ", String.valueOf(rssnr));
                    Log.i("Process : tadvanced ",String.valueOf(tadvanced));
                    content = String.valueOf(asulevel) + ","+String.valueOf(cqi) + ","+String.valueOf(dbm) + ","+String.valueOf(level) + ","+String.valueOf(rsrp) + ","+String.valueOf(rsrq)+","+String.valueOf(rssnr)+","+String.valueOf(tadvanced);
                } else {
                    throw new Exception("Unknown type of cell signal!");
                }
            }
        } catch (Exception e) {
            Log.e("process", "Unable to obtain cell signal information", e);
        }
    }

    //텍스트내용을 경로의 텍스트 파일에 쓰기
    public void WriteTextFile(String foldername, String filename, int wtime, String contents){
        try{
            File dir = new File (foldername);
            //디렉토리 폴더가 없으면 생성함
            if(!dir.exists()){
                Log.i("process","mkdir");
                dir.mkdirs();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(foldername+"/"+filename, true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(String.valueOf(wtime)+",");
            writer.write(contents);
            writer.newLine();
            writer.flush();
            writer.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        super.onDestroy();
    }
}
