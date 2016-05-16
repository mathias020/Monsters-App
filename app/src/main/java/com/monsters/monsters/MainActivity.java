package com.jonashr.monsters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    // NFC
    private NfcAdapter mNFCAdapter;

    /*
    // Matchmaking
    private ImageView mm_checkmark;
    private LinearLayout mm_checkarea;
    private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private TextView mm_text;
    */

    // Buttons
    private ImageView btn_findbattle;
    private ImageView btn_mycollection;
    private ImageView btn_statistics;

    // Wifi P2P
    private static WifiP2pManager wifiManager;
    private static WifiP2pManager.Channel wifiChannel;
    private BroadcastReceiver receiver;
    private IntentFilter mIntentFilter;
    public static WifiP2pDevice mDevice;


    // Constants
    public static final String MY_COLLECTION_FILE = "myCollection";
    public static final String PEERS_TO_DISPLAY = "PEERS_TO_DISPLAY";
    public static final int FIND_BATTLE_RETURN = 100;
    public static final int BATTLE_RETURN = 300;
    public static final String PEER_SELECTED = "PEER_SELECTED";

    public static final int SELECT_MONSTERS_RETURN = 200;
    private boolean isSelectScreenOpen = false;

    // All monsters
    public static ArrayList<Monster> monsters;
    public static  HardcodedAttacks attacks;


    // Monsters for battle
    public static ArrayList<Monster> myMonsters = new ArrayList<>();
    public static ArrayList<Monster> enemyMonsters = new ArrayList<>();

    private ProgressDialog dialog;


    // WiFi handling
    public static Connection serverConn;

    public static WifiP2pManager getWifiManager() {
        return wifiManager;
    }

    public static WifiP2pManager.Channel getWifiChannel() {
        return wifiChannel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        attacks = new HardcodedAttacks();

        new GetAllMonsters().execute("getAllMonsters"); // Synchronize app list of monsters with database

        /*
        // Initialize the needed variables for matchmaking-button
        mm_checkmark = (ImageView) findViewById(R.id.mm_checkmark);
        mm_checkarea = (LinearLayout) findViewById(R.id.mm_checkarea);
        // Add on-click listener to the view surrounding the matchmaking
        mm_checkarea.setOnClickListener(new EnableMatchmakingListener());

        // Text matchmaking
        mm_text = (TextView) findViewById(R.id.mm_text);
        */

        // Initialize buttons
        btn_findbattle = (ImageView) findViewById(R.id.btn_findbattle);
        btn_mycollection = (ImageView) findViewById(R.id.btn_mycollection);
        btn_statistics = (ImageView) findViewById(R.id.btn_statistics);





        // Initialize onClick listeners
        btn_findbattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findBattle = new Intent(MainActivity.this, PeersDialog.class);

                startActivityForResult(findBattle, FIND_BATTLE_RETURN);
            }
        });

        btn_mycollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mycollection = new Intent(MainActivity.this, MyCollectionActivity.class);

                startActivity(mycollection);
            }
        });

        btn_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myStatistics = new Intent(MainActivity.this, Statistics.class);

                startActivity(myStatistics);
            }
        });

        // Setting up wifi
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(wifiManager, wifiChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Set up NFC
        mNFCAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNFCAdapter != null && mNFCAdapter.isEnabled())
        {
 //           Toast.makeText(this, "NFC is Enabled", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (mNFCAdapter != null && mNFCAdapter.isEnabled() == false) {
//                Toast.makeText(this, "NFC not enabled, please enable", Toast.LENGTH_LONG).show();
            }

            else{
 //               Toast.makeText(this, "No NFC on device", Toast.LENGTH_LONG).show();
            }
        }


        // Set up receiver for WiFi P2P
        registerReceiver(receiver, mIntentFilter);
    }

    private void setUpCollectionFile() {
        File file = new File(getFilesDir(), MY_COLLECTION_FILE);

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //tag received
        if(intent.hasExtra(NfcAdapter.EXTRA_TAG))
        {
 //           Toast.makeText(this, "New NFC Intent received",Toast.LENGTH_LONG).show();
        }
        //getting the tag
        Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(parcelables != null && parcelables.length > 0)
        {
            readTextFromMessage((NdefMessage) parcelables[0]);
        }
        else
        {
 //           Toast.makeText(this, "No Message", Toast.LENGTH_LONG).show();
        }
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage)
    {
        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null)
            {
                Toast.makeText(this,"Tag is not ndef formatable!", Toast.LENGTH_LONG).show();
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
        }catch (Exception e){
            Log.e("formatTag", e.getMessage());
        }

    }

    //Read text from message
    private void readTextFromMessage(NdefMessage ndefMessage){
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null && ndefRecords.length > 0)
        {
            NdefRecord ndefRecord = ndefRecords[0];
            String tagContent = getTextFromNdefRecord(ndefRecord);
  //          Toast.makeText(this, tagContent, Toast.LENGTH_LONG).show();

            int monsterIdToGet = 0;

            Random randomGenerator = new Random();

            switch(tagContent) {
                case "number1":
                    monsterIdToGet = 0 + randomGenerator.nextInt(5);
                    break;
                case "number2":
                    monsterIdToGet = 5 + randomGenerator.nextInt(5);
                    break;
                case "number3":
                    monsterIdToGet = 10 + randomGenerator.nextInt(5);
                    break;
                case "number4":
                    monsterIdToGet = 15 + randomGenerator.nextInt(5);
                    break;
            }


            if(monsterIdToGet > 0) {
                new GetMonsterById().execute("getMonsterById", String.valueOf(monsterIdToGet));
            }


        }
        else
        {
            Toast.makeText(this, "No ndef records found", Toast.LENGTH_LONG).show();
        }
    }

    //Get the text from the NDef record
    public String getTextFromNdefRecord(NdefRecord ndefRecord)
    {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = "UTF-8";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FIND_BATTLE_RETURN) {
            if(resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();

                if(bundle.containsKey(PEER_SELECTED)) {
                    Bundle mBundle = data.getExtras();

                    final WifiP2pDevice selected = (WifiP2pDevice) mBundle.get(PEER_SELECTED);

                    Toast.makeText(this, "You chose: " + selected.deviceName, Toast.LENGTH_SHORT).show();

                    if(selected != null) {
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = selected.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;

                        dialog  = ProgressDialog.show(MainActivity.this, "",
                                "Waiting for response...", true);

                        wifiManager.connect(wifiChannel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int reason) {
                                dialog.dismiss();
                            }
                        });
                    }
                }
            }
        }

        else if(requestCode == SELECT_MONSTERS_RETURN) {
            if(resultCode == RESULT_OK) {
                isSelectScreenOpen = false;

                Bundle mBundle = data.getExtras();

                if(mBundle.containsKey(SelectionActivity.MONSTER_RETURN)) {
                    Object[] monstersChosen = (Object[]) mBundle.get(SelectionActivity.MONSTER_RETURN);

                    myMonsters.add((Monster)monstersChosen[0]);
                    myMonsters.add((Monster)monstersChosen[1]);
                    myMonsters.add((Monster)monstersChosen[2]);

                    serverConn.enqueuePacket(1, monstersChosen);

                    Statistics.addToStat(this, Statistics.TOTAL_MATCHES, 1);

                    Intent battleIntent = new Intent(MainActivity.this, BattleScreen.class);
                    startActivityForResult(battleIntent, BATTLE_RETURN);
                }

            } else {
                isSelectScreenOpen = false;
                if(serverConn != null)
                    serverConn.stopConnection();
            }
        }

        else if(requestCode == BATTLE_RETURN) {
            if(resultCode != RESULT_OK) {
                if(serverConn != null) {
                    serverConn.stopConnection();
                    serverConn = null;
                }
            } else if (resultCode == RESULT_OK) {
                if(MainActivity.getWifiManager() != null && MainActivity.getWifiChannel() != null) {
                    MainActivity.getWifiManager().requestGroupInfo(MainActivity.getWifiChannel(), new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(WifiP2pGroup group) {
                            if (group != null && MainActivity.getWifiManager() != null && MainActivity.getWifiChannel() != null) {
                                MainActivity.getWifiManager().removeGroup(MainActivity.getWifiChannel(), new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        MainActivity.deletePersistentGroups();
                                        Log.d("ConnC", "removeGroup onSuccess -");
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.d("ConnC", "removeGroup onFailure -" + reason);
                                    }
                                });
                            }
                        }
                    });
                }

                if(serverConn != null) {
                    serverConn.stopConnection();
                    serverConn = null;
                }

                if(BattleScreen.hasBattleEnded) {
                    if(BattleScreen.hasWon == true) {
                        Statistics.addToStat(this, Statistics.WINS, 1);
                    } else if(BattleScreen.hasWon == false) {
                        Statistics.addToStat(this, Statistics.LOSSES, 1);
                    }
                }
            }
        }
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, mIntentFilter);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};


        if(mNFCAdapter != null)
            mNFCAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }

    protected void onPause() {
        super.onPause();
//        unregisterReceiver(receiver);

        if(mNFCAdapter != null)
            mNFCAdapter.disableForegroundDispatch(this);
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.nfc_tech_filter.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // WiFi P2P Broadcast Receiver
    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        private WifiP2pManager wifiManager;
        private WifiP2pManager.Channel wifiChannel;
        private MainActivity mActivity;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
            super();
            this.wifiManager = manager;
            this.wifiChannel = channel;
            this.mActivity = activity;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                    Toast.makeText(mActivity, "WiFi Direct is Enabled!", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(mActivity, "WiFi Direct is DISABLED!!", Toast.LENGTH_LONG).show();
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Nothing
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if(wifiManager == null)
                    return;

                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    wifiManager.requestConnectionInfo(wifiChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            if(dialog != null)
                                dialog.dismiss();

                            if(info != null && info.groupOwnerAddress != null) {
                                Toast.makeText(MainActivity.this, "Host Addr: " + info.groupOwnerAddress.getHostAddress() + " | Group owner: " + info.isGroupOwner, Toast.LENGTH_LONG).show();

                                if(serverConn == null) {
                                    if (info.isGroupOwner) {
                                        serverConn = new ServerConnection(getApplicationContext());
                                        (new Thread(serverConn)).start();
                                    } else {
                                        serverConn = new ClientConnector(info.groupOwnerAddress.getHostAddress(), ServerConnection.SERVER_PORT, getApplicationContext());
                                        (new Thread(serverConn)).start();
                                    }
                                }

                                if (!isSelectScreenOpen && myMonsters.size() == 0) {
                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction(PeersDialog.REQUEST_CLOSE);

                                    sendBroadcast(broadcastIntent);

                                    Intent selectMonstersIntent = new Intent(MainActivity.this, SelectionActivity.class);
                                    isSelectScreenOpen = true;
                                    startActivityForResult(selectMonstersIntent, SELECT_MONSTERS_RETURN);
                                }
                            }
                        }
                    });
                } else {
                    deletePersistentGroups();

                    if(dialog != null)
                        dialog.dismiss();

                    if(serverConn != null) {
                        serverConn.stopConnection();
                        serverConn = null;
                    }

                    enemyMonsters.clear();
                    myMonsters.clear();

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(SelectionActivity.DESTROY_REQUEST);
                    sendBroadcast(broadcastIntent);


                    if(BattleScreen.hasBattleEnded == false) {
                        Intent broadcastIntent2 = new Intent();
                        broadcastIntent2.setAction(BattleScreen.SHUTDOWN_BATTLE_SCREEN);

                        sendBroadcast(broadcastIntent2);
                    }
                }

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                mDevice = device;
            }
        }
    }

    public void createTemporaryMonsters() {
        try {
            FileOutputStream fos = openFileOutput("monsters", Context.MODE_PRIVATE);
            ObjectOutputStream obs = new ObjectOutputStream(fos);

            Monster dragon = new Monster("Blue-Eyes", Monster.TYPE_MYTHIC, "Ice", 2000, 4000, 10000, attacks.getAttack(1), attacks.getAttack(2), attacks.getAttack(3), getIconResource("beu_dragon"), 2000);
            Monster dragon2 = new Monster("Blue-Eyes",  Monster.TYPE_RARE, "Ice", 2000, 4000, 10000, attacks.getAttack(1), attacks.getAttack(2), attacks.getAttack(3), R.drawable.beu_dragon, 2000);
            Monster dragon3 = new Monster("Black-Eyes",  Monster.TYPE_LEGENDARY, "YOMAMA", 50000, 40000, 50000, attacks.getAttack(1), attacks.getAttack(2), attacks.getAttack(3), R.drawable.beu_dragon, 50000);
            Monster dragon4 = new Monster("White-Eyes", Monster.TYPE_BASIC, "Derp", 100, 100, 100, attacks.getAttack(1), attacks.getAttack(2), attacks.getAttack(3), R.drawable.beu_dragon, 100);
            Monster dragon5 = new Monster("Derp-Eyes", Monster.TYPE_BASIC, "Derp", 100, 100, 100, attacks.getAttack(1), attacks.getAttack(2), attacks.getAttack(3), R.drawable.beu_dragon, 100);
            Monster dragon6 = new Monster("Shit-Eyes", Monster.TYPE_BASIC, "Derp", 100, 100, 100, attacks.getAttack(1), attacks.getAttack(2), attacks.getAttack(3), R.drawable.beu_dragon, 100);

            obs.writeObject(dragon);
            obs.writeObject(dragon2);
            obs.writeObject(dragon3);
            obs.writeObject(dragon4);
            obs.writeObject(dragon5);
            obs.writeObject(dragon6);

            fos.flush();
            obs.close();
            fos.close();

        } catch (IOException e) {
            Log.d("jhr", "File not found");
        }
    }


    // AsyncTask get all monsters
    private class GetAllMonsters extends AsyncTask<String, Void, ArrayList<Map<String, Object>>> {

        @Override
        protected ArrayList<Map<String, Object>>  doInBackground(String... params) {
            return MonstersDB.httpPost(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, Object>> data) {
            ArrayList<Monster> result = new ArrayList<Monster>();
            try {
                Log.d("skainet_dk", "Building local monsters array = " + data.size());

                for (int i = 0; i < data.size(); i++) {
                    String monsterName = (String) data.get(i).get("monster_name");
                    int attackPower = Integer.parseInt((String)data.get(i).get("attack"));
                    int defense = Integer.parseInt((String)data.get(i).get("defense"));
                    int health = Integer.parseInt((String)data.get(i).get("health"));
                    int attack1_id = Integer.parseInt((String)data.get(i).get("attack1_id"));
                    int attack2_id = Integer.parseInt((String)data.get(i).get("attack2_id"));
                    int attack3_id = Integer.parseInt((String)data.get(i).get("attack3_id"));
                    String icon = (String) data.get(i).get("icon_name");

                    Log.d("skainet_dk", "Added monster: " + monsterName);
                    Monster monster = new Monster(monsterName, health, attackPower, defense, attacks.getAttack(attack1_id), attacks.getAttack(attack2_id), attacks.getAttack(attack3_id), getIconResource(icon), health);
                    result.add(monster);

                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            MainActivity.monsters = result;
//            Toast.makeText(MainActivity.this, "Amount of monsters = " + monsters.size(), Toast.LENGTH_SHORT).show();
        }
    }

    private class GetMonsterById extends AsyncTask<String, Void, ArrayList<Map<String, Object>>> {

        @Override
        protected ArrayList<Map<String, Object>>  doInBackground(String... params) {
            Log.d("skainet_dk", "Getting monster ID = " + params[1]);
            return MonstersDB.httpPost(params[0], new AbstractMap.SimpleEntry<String, String>("id_to_get", params[1]));
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, Object>> data) {
            try {
                if(data.size() == 0) {
                    Log.d("skainet_dk", "Monster reading failed. Try again.");
                    Toast.makeText(MainActivity.this, "Collection requires internet.", Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < data.size(); i++) {
                        String monsterName = (String) data.get(i).get("monster_name");
                        int attackPower = Integer.parseInt((String) data.get(i).get("attack"));
                        int defense = Integer.parseInt((String) data.get(i).get("defense"));
                        int health = Integer.parseInt((String) data.get(i).get("health"));
                        int attack1_id = Integer.parseInt((String) data.get(i).get("attack1_id"));
                        int attack2_id = Integer.parseInt((String) data.get(i).get("attack2_id"));
                        int attack3_id = Integer.parseInt((String) data.get(i).get("attack3_id"));
                        String icon = (String) data.get(i).get("icon_name");

                        Log.d("skainet_dk", "New monster: " + monsterName);

                        Monster monster = new Monster(monsterName, health, attackPower, defense, attacks.getAttack(attack1_id), attacks.getAttack(attack2_id), attacks.getAttack(attack3_id), getIconResource(icon), health);

                        Intent wannaCatchIntent = new Intent(MainActivity.this, NewMonster.class);
                        wannaCatchIntent.putExtra(NewMonster.EXTRA_MONSTER, monster);

                        startActivity(wannaCatchIntent);

                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

//            Toast.makeText(MainActivity.this, "Amount of monsters = " + monsters.size(), Toast.LENGTH_SHORT).show();
        }
    }

    public int getIconResource(String icon) {
        return getResources().getIdentifier(icon, "drawable", getPackageName());
    }

    public static void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(MainActivity.getWifiManager(), MainActivity.getWifiChannel(), netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static class AppendingObjectOutputStream extends ObjectOutputStream {

        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            // do not write a header, but reset:
            // this line added after another question
            // showed a problem with the original
            reset();
        }

    }

    public static void saveMonster(Context context, Monster monster) {
        File file = new File(context.getFilesDir(), MY_COLLECTION_FILE);
        ObjectOutputStream out = null;

        try {
            if(!file.exists()) out = new ObjectOutputStream(new FileOutputStream(file));
            else out = new AppendingObjectOutputStream(new FileOutputStream(file, true));
            out.writeObject(monster);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace ();
        }finally{
            try{
                if (out != null) out.close ();
            }catch (Exception e){
                e.printStackTrace ();
            }
        }
    }
}
