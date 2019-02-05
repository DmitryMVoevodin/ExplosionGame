package com.vdm.game.interconnection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Disposable;
import com.vdm.game.ExplosionGame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class Client implements Disposable {

    public int NoP_index;

    private boolean Existence;
    private NodeServer nodeServer;
    private LinkedBlockingQueue<String> MessagesQueue;
    private Socket socketClient;
    private ClientThread clientThread; //Переменная класса ClientThread, как отдельного потока, из которого будет работать клиент

    public Client(String ip, int port, int NoP_index) throws IOException {
        this.NoP_index = NoP_index;
        Existence = true;
        clientThread = new ClientThread(ip, port); //Создание клиентского потока внутри класса и передача значения ip и порта для соединения
        clientThread.start(); //Запуск потока
    }

    public void KillExistence() { //"Убить" существование потока
        Existence = false;
    }

    public boolean isExistence() { //Получить информацию о статусе "существования" потока
        return Existence;
    }

    @Override
    public void dispose() {
        try {
            clientThread.join();
        } catch (InterruptedException e) {}
        CloseAll();
    }

    //Метод закрытия всего - сокета и потоков
    public void CloseAll() {
        if(nodeServer.socketClient != null) {
            nodeServer.CloseInOut();
            nodeServer.socketClient.dispose();
            nodeServer.socketClient = null;
        }
        if(socketClient != null) {
            socketClient.dispose();
        }
    }

    private class ClientThread extends Thread {

        private String ip; //Переменная ip-адреса
        private int port; //Переменная порта соединения

        public ClientThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            SocketHints hintsC = new SocketHints();
            hintsC.connectTimeout = 0;//20000;
            socketClient = Gdx.net.newClientSocket(Protocol.TCP, ip, port, hintsC);
            MessagesQueue = new LinkedBlockingQueue<String>();
            nodeServer = new NodeServer(socketClient);

            //Создание потока по обработке сообщений от сервера (вынимаем из очереди скопившиеся сообщения)
            Thread WorkingWithQueueOfMessages_Thread = new Thread() {
                @Override
                public void run() {
                    String msg;
                    byte bt;
                    while(Existence) {
                        try { //Обработка очередного сообщения
                            msg = MessagesQueue.take();
                            if(msg != null){
                                bt = msg.getBytes()[0];
                                if((bt > 63) ||
                                        (bt >= 7 && bt <= 10) ||
                                        (bt >= 23 && bt <= 26) ||
                                        (bt >= 39 && bt <= 42) ||
                                        (bt >= 55 && bt <= 58)) {
                                    Multiplayer.ByteTranslateToAction(bt);
                                } else {
                                    ExplosionGame.MessagesQueue2.offer(msg);
                                }
                            }
                        } catch (InterruptedException e) {}
                    }
                }
            };
            //WorkingWithQueueOfMessages_Thread.setDaemon(true);
            WorkingWithQueueOfMessages_Thread.start();

        }

    }


    private class NodeServer {

        Socket socketClient;
        InputStream in;
        OutputStream out;
        String msg;

        public NodeServer(Socket socketClient) {
            this.socketClient = socketClient;
            in = socketClient.getInputStream();
            out = socketClient.getOutputStream();

            Thread ReceiveMessage_Thread = new Thread(){
                public void run(){
                    byte[] byte_in = new byte[1];
                    while(Existence){
                        try{
                            in.read(byte_in);
                            msg = new String(byte_in);
                            MessagesQueue.put(msg);
                        } catch(IOException IOe){} catch (InterruptedException IEe) {}
                    }
                }
            };
            //ReceiveMessage_Thread.setDaemon(true);
            ReceiveMessage_Thread.start();
        }

        //Отправка сообщения этому (this) конкретному узлу-клиенту
        public void SendMessage(byte[] message) {
            try{
                out.write(message);

            } catch(IOException IOe) {}
        }

        //Закрытие in и out
        public void CloseInOut() {
            if(in != null) {
                try { in.close(); } catch (IOException e) {}
            }
            if(out != null) {
                try { out.close(); } catch (IOException e) {}
            }
        }

    }

    //Метод отправки сообщения серверу
    public void SendMessage(byte[] message) {
        nodeServer.SendMessage(message);
    }

}
