package com.jonashr.monsters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PeersDialog extends Activity {

    public static final String REQUEST_CLOSE = "REQUEST_CLOSE";
    private Button btn_dismiss;

    private ListView peerList;
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ServiceScanner serviceScanner;

//    private ArrayList<WifiP2pDevice> peers;

//    private final HashMap<String, WifiP2pDevice> peers = new HashMap<>();

//    private final ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private final Map<String, WifiP2pDevice> peers = new HashMap<>();

    private TextView tv_noResults;
    private TextView tv_searching;
    private ProgressBar scanningBar;

    private TextView tv_scan;

    // Wifi
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_dialog);

        wifiManager = MainActivity.getWifiManager();
        wifiChannel = MainActivity.getWifiChannel();


        btn_dismiss = (Button) findViewById(R.id.btn_dismiss);
        tv_noResults = (TextView) findViewById(R.id.noresults);
        tv_searching = (TextView) findViewById(R.id.searching);
        tv_scan = (TextView) findViewById(R.id.scanText);
        scanningBar = (ProgressBar) findViewById(R.id.scanningBar);

        tv_scan.setBackgroundResource(R.drawable.tv_button);

        tv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_noResults.setVisibility(View.INVISIBLE);
                tv_searching.setVisibility(View.VISIBLE);
                new ScanPeers().execute();
            }
        });


        btn_dismiss.setBackgroundResource(R.drawable.cancel_bg_draw);

        btn_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list);

        peerList = (ListView) findViewById(R.id.peerListView);


        peerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final WifiP2pDevice device = peers.get(list.get(position));

                new AlertDialog.Builder(PeersDialog.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(device.deviceName)
                        .setMessage("Do you wish to battle " + device.deviceName + " ?")
                        .setPositiveButton("Battle!", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (peers != null) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra(MainActivity.PEER_SELECTED, device);
                                    if(serviceScanner != null) {
                                        serviceScanner.stopScanner();
                                        serviceScanner = null;
                                    }
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        peerList.setAdapter(adapter);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(REQUEST_CLOSE);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(receiver, mIntentFilter);

        startRegistration();
        discoverService();

        wifiManager.discoverServices(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        serviceScanner = new ServiceScanner();
        serviceScanner.start();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(serviceScanner != null) {
            serviceScanner.stopScanner();
            serviceScanner = null;
        }
        wifiManager.clearLocalServices(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Log.d("skainet_dk", "Local services cleared");
            }
        });

        wifiManager.clearServiceRequests(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Log.d("skainet_dk", "Service requests cleared");
            }
        });
    }

    private class ScanPeers extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            if(wifiManager != null && wifiChannel != null) {
                Log.d("skainet_dk", "Looking for peers");
                wifiManager.discoverServices(wifiChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("skainet_dk", "Discovering services succeeded");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("skainet_dk", "Discover Services = " + reason);
                    }
                });
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_peers_dialog, menu);
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

    private void startRegistration() {
        if(MainActivity.mDevice != null) {
            Log.d("skainet_dk", "Service registration initiated...");
            WifiP2pDevice me = MainActivity.mDevice;

            Map record = new HashMap();
            record.put("buddyname", me.deviceName);

            WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_monsters", "_presence._tcp", record);

            wifiManager.addLocalService(wifiChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d("skainet_dk", "Service registration completed");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("skainet_dk", "Add Local Service = " + reason);
                }
            });

        }
    }

    private void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.d("skainet_dk", "Found one! " + srcDevice.deviceName);
                peers.put(srcDevice.deviceName, srcDevice);
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.d("skainet_dk", "Peers available! = " + peers.size());

                tv_noResults.setVisibility(View.INVISIBLE);

                Set<String> keyset = peers.keySet();

                for(String name : keyset) {
                    if(!list.contains(name) && name != null && !name.isEmpty()) {
                        list.add(name);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };

        wifiManager.setDnsSdResponseListeners(wifiChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wifiManager.addServiceRequest(wifiChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Log.d("skainet_dk", "Service Request = " + reason);
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("skainet_dk", "Close request received checking extras");

            if(action.equals(PeersDialog.REQUEST_CLOSE)) {
                Log.d("skainet_dk", "Close request received");

                PeersDialog.this.finish();
            }
        }
    };

    private class ServiceScanner extends Thread {
        private boolean isRunning = true;

        public void stopScanner() { isRunning = false; }
        public void run() {
            while(isRunning) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wifiManager.discoverServices(wifiChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });
            }
        }
    }
}
