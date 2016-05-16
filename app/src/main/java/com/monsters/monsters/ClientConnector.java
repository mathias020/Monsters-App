package com.jonashr.monsters;

import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonas on 13-11-2015.
 */
public class ClientConnector implements Connection {

    private Socket socket;

    private String host;
    private int port;

    private DataOutputStream outToServer;
    private DataInputStream inFromServer;

    private Queue<ByteBuffer> packetsToSend;
    private boolean connectionGoing;

    private Context context;

    public ClientConnector(String host, int port, Context context) {
        this.host = host;
        this.port = port;
        this.context = context;
        packetsToSend = new LinkedBlockingQueue<>();
    }

    private void sendBroadcast(String action) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);

        context.sendBroadcast(broadcastIntent);
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);

            outToServer = new DataOutputStream(socket.getOutputStream());
            inFromServer = new DataInputStream(socket.getInputStream());

            connectionGoing = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread senderThread = new Thread() {
            public void run() {
                while(connectionGoing) {
                    for(int i = 0; i < packetsToSend.size(); i++) {
                        try {
                            ByteBuffer packet = packetsToSend.poll();

                            if(packet != null) {
                                byte[] dataToSend = packet.array();

                                outToServer.write(dataToSend, 0, dataToSend.length);
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                }
            }
        };

        Thread receiverThread = new Thread() {
            public void run() {
                while(connectionGoing) {
                    try {
                        int packetId = inFromServer.readInt();
                        int dataLength = inFromServer.readInt();

                        Log.d("skainet_dk", "Packet ID = " + packetId);
                        Log.d("skainet_dk", "Data Length = " + dataLength);

                        byte[] dataReceived = new byte[dataLength];


                        Log.d("skainet_dk", "Reading data...");
                        inFromServer.read(dataReceived, 0, dataLength);


                        if(packetId == 1) {
                            Log.d("skainet_dk", "Parsing data...");
                            Object[] monsters = (Object[]) PacketUtilities.packetToObject(dataReceived);


                            Log.d("skainet_dk", "Adding enemy monsters...");
                            MainActivity.enemyMonsters.add((Monster) monsters[0]);
                            MainActivity.enemyMonsters.add((Monster) monsters[1]);
                            MainActivity.enemyMonsters.add((Monster) monsters[2]);

                            Log.d("skainet_dk", "Monsters added succesfully = " + MainActivity.enemyMonsters.toString());

                            sendBroadcast(BattleScreen.WAITING_FOR_OPPONENT);
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
                        } else if(packetId == 3) {
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

        senderThread.start();
        receiverThread.start();
    }

    public void stopConnection() {
        connectionGoing = false;
    }

    public void enqueuePacket(int packetId, Object... data) {
        byte[] objectBytes = PacketUtilities.objectToBytes(data);

        ByteBuffer packetToSend = ByteBuffer.allocate(8 + objectBytes.length);
        packetToSend.putInt(packetId);
        packetToSend.putInt(objectBytes.length); // Data length
        packetToSend.put(objectBytes);

        packetsToSend.add(packetToSend);

    }
}
