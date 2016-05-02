package com.jonashr.monsters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonas on 13-11-2015.
 */
public class ServerConnection extends BroadcastReceiver implements Connection {
    private boolean connectionGoing;

    public static final int SERVER_PORT = 8888;

    private Queue<ByteBuffer> packetQueue;

    private Socket opponent;

    private Context context;

    private ServerSocket serverSocket;

    private void sendBroadcast(String action) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);

        context.sendBroadcast(broadcastIntent);
    }

    public ServerConnection(Context context) {
        this.context = context;
        packetQueue = new LinkedBlockingQueue<>();
        connectionGoing = true;
    }

    public void stopConnection() {
        connectionGoing = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    private class ClientCommunicationThread implements Runnable {

        private DataInputStream inFromClient;

        public ClientCommunicationThread(Socket socket) {
            try {
                inFromClient = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Thread clientReceiver = new Thread() {
                public void run() {
                    while(connectionGoing) {
                        try {
                            int packetId = inFromClient.readInt();
                            int dataLength = inFromClient.readInt();

                            Log.d("skainet_dk", "Packet ID = " + packetId);
                            Log.d("skainet_dk", "Data Length = " + dataLength);

                            byte[] dataReceived = new byte[dataLength];

                            inFromClient.read(dataReceived, 0, dataLength);


                            if(packetId == 1) {
                                Log.d("skainet_dk", "Parsing data...");
                                Object[] monsters = (Object[]) PacketUtilities.packetToObject(dataReceived);


                                Log.d("skainet_dk", "Adding enemy monsters...");
                                MainActivity.enemyMonsters.add((Monster) monsters[0]);
                                MainActivity.enemyMonsters.add((Monster) monsters[1]);
                                MainActivity.enemyMonsters.add((Monster) monsters[2]);

                                Log.d("skainet_dk", "Monsters added succesfully = " + MainActivity.enemyMonsters.toString());

                                sendBroadcast(BattleScreen.WAITING_FOR_OPPONENT);
                                sendBroadcast(BattleScreen.MY_TURN);
                            }

                            else if(packetId == 2) {
                                Object[] attackInfo = (Object[]) PacketUtilities.packetToObject(dataReceived);

                                Log.d("skainet_dk", "Monster attacked!");

                                int target = (int) attackInfo[0];
                                int origin = (int) attackInfo[1];
                                int attack = (int) attackInfo[2];
                                int multiAmount = (int) attackInfo[3];
                                int paralyzeRand = (int) attackInfo[4];


                                int oldHealth = MainActivity.myMonsters.get(target).health();
                                int oldHealthOrigin = MainActivity.enemyMonsters.get(origin).health();
                                MainActivity.enemyMonsters.get(origin).attackMonster(attack, MainActivity.myMonsters.get(target), multiAmount, paralyzeRand);
                                int damageDone = oldHealth - MainActivity.myMonsters.get(target).health();
                                int healingDone = oldHealthOrigin - MainActivity.enemyMonsters.get(origin).health();

                                Intent attackIntent = new Intent();
                                attackIntent.setAction(BattleScreen.MONSTER_ATTACKED);
                                attackIntent.putExtra(BattleScreen.MONSTER_TARGET, target);
                                attackIntent.putExtra(BattleScreen.MONSTER_ORIGIN, origin);
                                attackIntent.putExtra(BattleScreen.MONSTER_WAS_HEALED, multiAmount);
                                attackIntent.putExtra(BattleScreen.MONSTER_DAMAGE_DONE, damageDone);
                                attackIntent.putExtra(BattleScreen.MONSTER_HEALING_DONE, healingDone);

                                if(MainActivity.enemyMonsters.get(origin).getAttacks()[attack].getEffect().equals("paralyze"))
                                    attackIntent.putExtra(BattleScreen.MONSTER_DAMAGE_TYPE, BattleScreen.ATK_TYPE_PARALYZE);
                                else
                                    attackIntent.putExtra(BattleScreen.MONSTER_DAMAGE_TYPE, BattleScreen.ATK_TYPE_DAMAGE);


                                context.sendBroadcast(attackIntent);
                            }
                            else if(packetId == 3) {
                                sendBroadcast(BattleScreen.MY_TURN);
                            }
                            else if(packetId == 4) {
                                BattleScreen.hasBattleEnded = true;
                                BattleScreen.hasWon = false;
                                sendBroadcast(BattleScreen.YOU_LOST);
                            }
                            else if(packetId == 5) {
                                Object[] attackInfo = (Object[]) PacketUtilities.packetToObject(dataReceived);

                                int who = (int) attackInfo[0];

                                Intent paralyzed = new Intent();
                                paralyzed.setAction(BattleScreen.MONSTER_PARALYZED);
                                paralyzed.putExtra(BattleScreen.MONSTER_ORIGIN, who);

                                context.sendBroadcast(paralyzed);
                            }

                        } catch (Exception e) {
                            break;
                        }
                    }
                }
            };

            clientReceiver.start();
        }
    }

    @Override
    public void run() {
        Thread receiverThread = new Thread() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(SERVER_PORT);

                    opponent = serverSocket.accept();


                    Log.d("skainet_dk", "Client connected");
                    (new Thread(new ClientCommunicationThread(opponent))).start();
                    Log.d("skainet_dk", "Client request sent to handler");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread senderThread = new Thread() {
            public void run() {
                try {
                    while (connectionGoing) {
                        for (int i = 0; i < packetQueue.size(); i++) {
                            ByteBuffer packet = packetQueue.poll();

                            if (packet != null) {
                                DataOutputStream dos = new DataOutputStream(opponent.getOutputStream());

                                byte[] dataToSend = packet.array();

                                dos.write(dataToSend, 0, dataToSend.length);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        receiverThread.start();
        senderThread.start();
    }

    public void enqueuePacket(int packetId, Object... data) {
        byte[] objectBytes = PacketUtilities.objectToBytes(data);

        ByteBuffer packetToSend = ByteBuffer.allocate(8 + objectBytes.length);
        packetToSend.putInt(packetId);
        packetToSend.putInt(objectBytes.length); // Data length
        packetToSend.put(objectBytes);

        packetQueue.add(packetToSend);

    }
}
