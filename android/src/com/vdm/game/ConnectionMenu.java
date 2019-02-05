package com.vdm.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class ConnectionMenu extends Activity {

    static String   ClientIP;
    static String   ServerIP;
    static boolean  isServer;
    static int      NoP;
    static int      TypeOfTexture;

    Spinner     spinner_DomestIP;
    Spinner     spinnerPlayerType;
    EditText    editTextServerIP;
    Spinner     spinnerNumberOfPlayers;

    ArrayList<String> ip_string; ArrayAdapter<String> ip_SpinnerAdapter;
    ArrayList<String> plType_string; ArrayAdapter<String> plType_SpinnerAdapter;
    ArrayList<String> NoP_int; ArrayAdapter<String> NoP_SpinnerAdapter;

    ImageView ch1;
    ImageView ch2;
    ImageView ch3;
    ImageView ch4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_menu);

        ch1 = (ImageView)findViewById(R.id.imageView1);
        ch2 = (ImageView)findViewById(R.id.imageView2);
        ch3 = (ImageView)findViewById(R.id.imageView3);
        ch4 = (ImageView)findViewById(R.id.imageView4);
        ch1.setClickable(true);
        ch2.setClickable(true);
        ch3.setClickable(true);
        ch4.setClickable(true);
        ch1.setAlpha(1.0f); ch2.setAlpha(0.5f); ch3.setAlpha(0.5f); ch4.setAlpha(0.5f);
        TypeOfTexture = 1;


        spinner_DomestIP = (Spinner)findViewById(R.id.spinnerDomesticIP);
        spinnerPlayerType =(Spinner)findViewById(R.id.spinnerPlayerType);
        editTextServerIP = (EditText)findViewById(R.id.editTextServerIP);
        spinnerNumberOfPlayers = (Spinner)findViewById(R.id.spinnerNumberOfPlayers);

        CreateSpinnerPlayerType();
        plType_SpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, plType_string);
        spinnerPlayerType.setAdapter(plType_SpinnerAdapter);
        spinnerPlayerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinnerPlayerType.getSelectedItem().toString().equals("Server")) {
                    editTextServerIP.setText(spinner_DomestIP.getSelectedItem().toString());
                    editTextServerIP.setClickable(false);
                    editTextServerIP.setFocusable(false);
                } else {
                    editTextServerIP.setClickable(true);
                    editTextServerIP.setFocusable(true);
                    editTextServerIP.setFocusableInTouchMode(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        CreateSpinnerNoP();
        NoP_SpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, NoP_int);
        spinnerNumberOfPlayers.setAdapter(NoP_SpinnerAdapter);

        CreateSpinnerDomesticIP();
        ip_SpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ip_string);
        spinner_DomestIP.setAdapter(ip_SpinnerAdapter);
        spinner_DomestIP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinnerPlayerType.getSelectedItem().toString().equals("Server")) {
                    editTextServerIP.setText(spinner_DomestIP.getSelectedItem().toString());
                    editTextServerIP.setClickable(false);
                    editTextServerIP.setFocusable(false);
                } else {
                    editTextServerIP.setClickable(true);
                    editTextServerIP.setFocusable(true);
                    editTextServerIP.setFocusableInTouchMode(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    //Выход из программы
    public void onButtonClick_Exit(View v) {
        System.exit(0);
    }

    //Запуск игры, переход на новую Activity
    public void onButtonClick_StartGame(View v) {
        ClientIP = spinner_DomestIP.getSelectedItem().toString();
        ServerIP = editTextServerIP.getText().toString();
        String s = spinnerNumberOfPlayers.getSelectedItem().toString();
        NoP = s.equals("2 players") ? 2 : (s.equals("3 players") ? 3 : 4);
        isServer = spinnerPlayerType.getSelectedItem().toString().equals("Server") ? true : false;
        Intent intent = new Intent(this, AndroidLauncher.class);
        startActivity(intent);
        finish();//Уничтожение Activity для сетевых настроек
    }

    //Нажатие на рисунок
    public void onClickImage(View v) {
        switch(v.getId()) {
            case R.id.imageView1:
                ch1.setAlpha(1.0f); ch2.setAlpha(0.5f); ch3.setAlpha(0.5f); ch4.setAlpha(0.5f);
                TypeOfTexture = 1;
                break;
            case R.id.imageView2:
                ch1.setAlpha(0.5f); ch2.setAlpha(1.0f); ch3.setAlpha(0.5f); ch4.setAlpha(0.5f);
                TypeOfTexture = 2;
                break;
            case R.id.imageView3:
                ch1.setAlpha(0.5f); ch2.setAlpha(0.5f); ch3.setAlpha(1.0f); ch4.setAlpha(0.5f);
                TypeOfTexture = 3;
                break;
            case R.id.imageView4:
                ch1.setAlpha(0.5f); ch2.setAlpha(0.5f); ch3.setAlpha(0.5f); ch4.setAlpha(1.0f);
                TypeOfTexture = 4;
                break;
        }
    }

    //Заполнение спиннера локальных ip-адресов
    private void CreateSpinnerDomesticIP() {
        ip_string = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip_string.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {}
    }

    //Заполнение спиннера типа игрока
    private void CreateSpinnerPlayerType() {
        plType_string = new ArrayList<>();
        plType_string.add("Server");
        plType_string.add("Client");
    }

    //Заполнение спиннера количества игроков
    private void CreateSpinnerNoP() {
        NoP_int = new ArrayList<>();
        for(int i = 1; i < 4; ++i) {
            NoP_int.add((i + 1) + " players");
        }
    }

}
