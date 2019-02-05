package com.vdm.game.interconnection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Disposable;
import com.vdm.game.ExplosionGame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Disposable {

    private boolean Existence;
    private Multiplayer.Flag RegistrationFlag;

    private ArrayList<ClientNode> ClientNodeArrayList; //Список с узлами-связями(это отдельный класс - см. ниже) по клиентам
    private LinkedBlockingQueue<String> MessagesQueue; //Очередь с объектами-сообщений от всех клиентов
    private ServerSocket serverSocket; //Переменная сокета для сервера
    private ServerThread serverThread; //Переменная класса ServerThread, как отдельного потока, из которого будет работать сервер

    public Server(String ip, int port, int NoP) { //Инициализация класса Server
        Existence = true;
        RegistrationFlag = new Multiplayer.Flag();
        serverThread = new ServerThread(ip, port, NoP); //Создание серверного потока внутри класса и передача значения определенного порта для соединения
        serverThread.start(); //Запуск потока
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
            serverThread.join();
        } catch (InterruptedException e) {}
        CloseAll();
    }

    //Закрытие in, out, serverSocket
    public void CloseAll() {
        //Закрываем сервер-сокет
        if(serverSocket != null) {
            serverSocket.dispose();
            serverSocket = null;
        }
        //Закрываем клиент-сокет
        for(ClientNode clientNode : ClientNodeArrayList) {
            if(clientNode.socketClient != null) {
                clientNode.CloseInOut();
                clientNode.socketClient.dispose();
                clientNode.socketClient = null;
            }
        }
    }

    //Класс-поток для работы сервера
    private class ServerThread extends Thread {

        private String ip; //Переменная ip-адреса
        private int port; //Переменная порта соединения
        private int NoP; //Переменная количества подключаемых клиентов
        private int NoP_index; //Переменная-индекс для игроков

        public ServerThread(String ip, int port, int NoP) { //Инициализация класса ServerThread
            this.ip = ip;
            this.port = port;
            this.NoP = NoP;
            this.NoP_index = 0;
        }

        @Override
        public void run() {

            //Инициализация списка клиентов и очереди сообщений от них
            ClientNodeArrayList = new ArrayList<ClientNode>(); //Инициализация списка с узлами-связями по клиентам
            MessagesQueue = new LinkedBlockingQueue<String>(); //Инициализация очереди объектов-сообщений

            //Инициализация пременнной серверного сокета
            ServerSocketHints hintsS = new ServerSocketHints(); hintsS.acceptTimeout = 0;//20000;
            serverSocket = Gdx.net.newServerSocket(Protocol.TCP, ip, port, hintsS);

            //Создание потока по установлению соединения с клиентами (заполнения ArrayList новыми узлами-связями добавившихся игроков)
            Thread ClientRegistration_Thread = new Thread() {
                @Override
                public void run() {
                    while(NoP_index < NoP) { //Проводим регистрацию, пока количество подключений не будет равно заявленному количеству игроков
                        try {
                            //"Воспроизводим" сокет клиента на стороне сервера
                            SocketHints hintsC = new SocketHints(); //Нужно для дополнительных настроек подключения
                            hintsC.connectTimeout = 0; //Вот и дополнительные настройки: время ожидания подключения бесконечно
                            Socket socketClient = serverSocket.accept(hintsC); //Ждем нового подключения-регистрации
                            //Если мы уже здесь, то подключение осуществлено успешно, осталось его зарегестрировать, внеся в список
                            //Добавлям в регистрационный список нового игрока с параметром этого сокета в виде объекта класса ClientNode
                            NoP_index++; //Увеличиваем номер игрока (каждый новый зарегестрированный игрок имеет более высокий номер)
                            RegistrationFlag.setValue((NoP_index == NoP)); //Флаг <Регистрация> = true, как только ВСЕ игроки будут зарегестрированы (чтобы отослать сообщение о смене окон).
                            ClientNodeArrayList.add(new ClientNode(socketClient, NoP_index)); //Зарегестрировать нового клиента, занеся его в список с его номером
                        } catch (IOException IOe) {}
                    }
                }
            };
            //ClientRegistration_Thread.setDaemon(true); //Теперь поток регистрации клиентов представляет собой daemon-процесс
            ClientRegistration_Thread.start(); //Запускаем поток регистрации клиентов

            //Создание потока по обработке сообщений от клиентов (вынимаем из очереди скопившиеся сообщения)
            Thread WorkingWithQueueOfMessages_Thread = new Thread() {
                @Override
                public void run() {
                    String msg;
                    while(Existence) {
                        try { //Обработка очередного сообщения
                            msg = MessagesQueue.take(); //Вытаскиваем сообщение из очереди
                            SendMessageToAllClients(msg.getBytes()); //Рассылка этого сообщения всем игрокам
                        } catch (InterruptedException e) {}
                    }
                }
            };
            //WorkingWithQueueOfMessages_Thread.setDaemon(true); //Теперь поток регистрации клиентов представляет собой daemon-процесс
            WorkingWithQueueOfMessages_Thread.start(); //Запускаем поток регистрации клиентов

        }

    }

    //Класс узла-связи, т.е. игрока, в регистрационном списке клиентов.
    //Позволяет класть сообщения в очередь от игроков и рассылать им ответные сообщения.
    private class ClientNode {

        int NoP_index;
        Socket socketClient;
        InputStream in;
        OutputStream out;
        String msg;

        public ClientNode(final Socket socketClient, int NoP_index) throws IOException {
            this.NoP_index = NoP_index;
            this.socketClient = socketClient;
            in = socketClient.getInputStream();
            out = socketClient.getOutputStream();

            //Поток по получению сообщений - добавления их в очередь
            Thread ReceiveMessage_Thread = new Thread(){
                @Override
                public void run() {
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

            //Первичная отправка информации о номере игрока этому игроку (без задействования системы очереди сообщений на стороне сервера)
            byte[] byte_out_special = new byte[1]; //Создание массива байт для отправки клиентам
            byte_out_special[0] = Multiplayer.ByteFormation(ExplosionGame.NoP + 26); //Заполнение массива информационным байтом с числом игроков
            SendMessage(byte_out_special); //Отправить сообщение о числе игроков, чтобы клиент добавил эту информацию к себе
            byte_out_special[0] = Multiplayer.ByteFormation(NoP_index - 1); //Заполнение массива информационным байтом с порядковым номером игрока
            SendMessage(byte_out_special); //Отправить сообщение о номере игрока, чтобы клиент добавил эту информацию к себе
            //Если был зарегистрирован последний клиент(определяется по флагу <Регистрация>) и ему было отправлено сообщение о его номере,
            //тогда отправляем всем сообщение о смене игрового окна GW_MainMenu на GW_Play (без системы серверной очереди).
            if(RegistrationFlag.Trigger()) {
                byte_out_special[0] = Multiplayer.ByteFormation(4); //Номер закодированного сообщения смены окон
                SendMessageToAllClients(byte_out_special); //Рассылаем это сообщение по всем подключениям, для всех игроков (кроме последнего)
                SendMessage(byte_out_special); //Для последнего игрока отдельно нужно сообщение присылать, т.к. сейчас мы ещё в его конструкторе, поэтому его еще нет в общем списке
            }
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

    //Метод отправки сообщения одному игроку сконкретным индексом
    public void SendMessageToOneClient(int i, byte[] message) {
        try {
            ClientNodeArrayList.get(i).SendMessage(message);
        } catch (IndexOutOfBoundsException e) {}
    }

    //Метод отправки сообщения всем игрокам
    public void SendMessageToAllClients(byte[] message) {
        for(ClientNode clientNode : ClientNodeArrayList) {
            clientNode.SendMessage(message);
        }
    }


}
