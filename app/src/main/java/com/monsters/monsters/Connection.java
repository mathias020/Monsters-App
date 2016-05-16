package com.jonashr.monsters;

/**
 * Created by Jonas on 13-11-2015.
 */
public interface Connection extends Runnable {
    void stopConnection();
    void enqueuePacket(int packetId, Object... data);
}
