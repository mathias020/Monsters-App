package com.jonashr.monsters;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Selection;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SelectionActivity extends Activity {

    private ArrayList<Monster> myMonsters;
    private ScrollView scrollView;
    private LinearLayout listOfMonsters;

    private TextView selectMonstersTitle;

    private ArrayList<Monster> selected = new ArrayList<>();

    public static final String MONSTER_RETURN = "MONSTER_RETURN";


    // Receiver values
    public static final String DESTROY_REQUEST = "DESTROY_REQUEST";
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_selection);


        scrollView = (ScrollView) findViewById(R.id.scrollViewSelection);
        listOfMonsters = (LinearLayout) findViewById(R.id.listOfMonsters);
        selectMonstersTitle = (TextView) findViewById(R.id.selectMonstersTitle);
        scrollView.setFadingEdgeLength(150);


        myMonsters = readFromBinaryFile();

        if(myMonsters != null) {
            Toast.makeText(this, "Found monsters! = " + myMonsters.size(), Toast.LENGTH_SHORT).show();
            int i = 0;
            for(Monster monster : myMonsters) {
                addMonsterToList(i, monster);
                i++;
            }
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(DESTROY_REQUEST);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(receiver, mIntentFilter);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    public void addMonsterToList(int position, Monster monster) {
        if(monster != null && scrollView != null) {
            // Making initial linear-layout wrapper
            LinearLayout itemWrapper = new LinearLayout(this);
            LinearLayout.LayoutParams itemWrapperParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            itemWrapperParams.bottomMargin = getDP(5);

            itemWrapper.setOrientation(LinearLayout.HORIZONTAL);
            itemWrapper.setBackgroundColor(0x90000000);
            itemWrapper.setPadding(5, 5, 5, 5);
            itemWrapper.setLayoutParams(itemWrapperParams);

            // Monster frame build-up
            RelativeLayout monsterFrame = new RelativeLayout(this);
            RelativeLayout.LayoutParams monsterFrameParams = new RelativeLayout.LayoutParams(getDP(80), getDP(80));

            monsterFrameParams.rightMargin = getDP(5);

            monsterFrame.setBackgroundResource(Monster.getFrameIcon(monster.getType()));
            monsterFrame.setLayoutParams(monsterFrameParams);

            // Monster element icon
            ImageView monsterElement = new ImageView(this);
            RelativeLayout.LayoutParams monsterElementParams = new RelativeLayout.LayoutParams(getDP(20), getDP(20));

            monsterElement.setBackgroundResource(Monster.getElementIcon(monster.element()));
            monsterElementParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            monsterElementParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            monsterElement.setLayoutParams(monsterElementParams);

            // Monster icon

            ImageView monsterIcon = new ImageView(this);
            ViewGroup.LayoutParams monsterIconParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            monsterIcon.setBackgroundResource(monster.getIcon());
            monsterIcon.setLayoutParams(monsterIconParams);

            // Add image-view inside MonsterFrame linear layout
            monsterFrame.addView(monsterIcon);
            monsterFrame.addView(monsterElement);

            // Add Monster-frame to the outer wrapper
            itemWrapper.addView(monsterFrame);

            // Text fields
            LinearLayout textContainer = new LinearLayout(this);
            LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            textContainer.setOrientation(LinearLayout.VERTICAL);
            textContainer.setLayoutParams(textContainerParams);


            // Name of monster
            TextView tv_monsterName = new TextView(this);
            ViewGroup.LayoutParams tv_monsterNameParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            tv_monsterName.setTextColor(0xFFFFFFFF);
            tv_monsterName.setTypeface(null, Typeface.BOLD);
            tv_monsterName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tv_monsterName.setPadding(getDP(10), getDP(10), getDP(10), getDP(10));
            tv_monsterName.setText(monster.getName());


            tv_monsterName.setLayoutParams(tv_monsterNameParams);
            textContainer.addView(tv_monsterName);


            // Description of monster
            RelativeLayout monsterDesc = new RelativeLayout(this);
            RelativeLayout.LayoutParams monsterDescParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            monsterDesc.setLayoutParams(monsterDescParams);

            // Monster health
            TextView monsterHealth = new TextView(this);
            RelativeLayout.LayoutParams monsterHealthParams = new RelativeLayout.LayoutParams(getDP(80), RelativeLayout.LayoutParams.WRAP_CONTENT);

            int healthId = View.generateViewId();
            monsterHealth.setId(healthId);
            monsterHealth.setTextColor(0xff05d800);
            monsterHealth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            monsterHealth.setPadding(getDP(10), 0, 0, 0);
            monsterHealth.setTypeface(null, Typeface.BOLD);
            monsterHealth.setText(monster.health() + " HP");
            monsterHealth.setLayoutParams(monsterHealthParams);
            monsterDesc.addView(monsterHealth);

            TextView monsterAttack = new TextView(this);
            RelativeLayout.LayoutParams monsterAttackParams = new RelativeLayout.LayoutParams(getDP(80), RelativeLayout.LayoutParams.WRAP_CONTENT);

            monsterAttackParams.addRule(RelativeLayout.RIGHT_OF, healthId);
            monsterAttackParams.leftMargin = getDP(5);
            int attackId = View.generateViewId();
            monsterAttack.setId(attackId);
            monsterAttack.setTextColor(0xffff0000);
            monsterAttack.setTypeface(null, Typeface.BOLD);
            monsterAttack.setPadding(getDP(10), 0, 0, 0);
            monsterAttack.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            monsterAttack.setLayoutParams(monsterAttackParams);

            monsterAttack.setText(monster.getAttackPower(monster.element()) + " ATK");

            monsterDesc.addView(monsterAttack);

            TextView monsterDef = new TextView(this);
            RelativeLayout.LayoutParams monsterDefParams = new RelativeLayout.LayoutParams(getDP(80), RelativeLayout.LayoutParams.WRAP_CONTENT);

            monsterDefParams.addRule(RelativeLayout.RIGHT_OF, attackId);
            monsterDefParams.leftMargin = getDP(5);

            monsterDef.setTextColor(0xff1bc7db);
            monsterDef.setPadding(getDP(10), 0, 0, 0);
            monsterDef.setTypeface(null, Typeface.BOLD);
            monsterDef.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            monsterDef.setLayoutParams(monsterDefParams);

            monsterDef.setText(monster.getDefense() + " DEF");

            monsterDesc.addView(monsterDef);

            textContainer.addView(monsterDesc);

            itemWrapper.setTag(position);
            itemWrapper.setOnClickListener(monsterSelectorListener);
            itemWrapper.addView(textContainer);

            listOfMonsters.addView(itemWrapper);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selection, menu);
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

    public ArrayList<Monster> getMyCollection() {
        try {
            FileInputStream fin = openFileInput("monsters");
            ObjectInputStream oin = new ObjectInputStream(fin);

            ArrayList<Monster> monsters = new ArrayList<>();

            Object in;
            try {
                while ((in = oin.readObject()) != null) {
                    monsters.add((Monster) in);
                }
            } catch (EOFException er) {
                ;
            }

            oin.close();
            fin.close();

            return monsters;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private View.OnClickListener monsterSelectorListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();

            Monster monster = myMonsters.get(tag);
            LinearLayout view = (LinearLayout) v;

            if(!selected.contains(monster) && selected.size() < 3) {
                selected.add(monster);
                view.setBackgroundColor(0xafa30300);

                if(selected.size() == 3) {
                    selectMonstersTitle.setBackgroundColor(0xafa30300);
                    selectMonstersTitle.setText("CLICK TO BEGIN");
                    selectMonstersTitle.setOnClickListener(readyListener);
                } else {
                    selectMonstersTitle.setText("Select your monsters (" + selected.size() + "/3)");
                    selectMonstersTitle.setBackgroundColor(0x60000000);
                    selectMonstersTitle.setOnClickListener(null);
                }
            } else if(selected.contains(monster)) {
                selected.remove(monster);
                view.setBackgroundColor(0x60000000);
                selectMonstersTitle.setText("Select your monsters (" + selected.size() + "/3)");
                selectMonstersTitle.setBackgroundColor(0x60000000);
                selectMonstersTitle.setOnClickListener(null);
            } else if(selected.size() == 3) {
                Toast.makeText(SelectionActivity.this, "You may not choose more than 3 monsters", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener readyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(selected.size() == 3) {
                Monster[] monsters = selected.toArray(new Monster[selected.size()]);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(MONSTER_RETURN, monsters);
                setResult(RESULT_OK, resultIntent);
                SelectionActivity.this.finish();
            }
        }
    };

    public int getDP(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Quit Battle")
            .setMessage("Are you sure you wish to quit ?")
            .setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    return;
                }
            })
            .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    if (MainActivity.getWifiManager() != null &&
                            MainActivity.getWifiChannel() != null) {
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
                    SelectionActivity.super.onBackPressed();
                }
            }).create().show();

        return;
    }

    public ArrayList<Monster> readFromBinaryFile (){
        File file = new File (getFilesDir(), MainActivity.MY_COLLECTION_FILE);
        ArrayList<Monster> monsters = new ArrayList<>();

        if (file.exists ()){
            ObjectInputStream ois = null;
            try{
                ois = new ObjectInputStream (new FileInputStream(file));
                while (true){
                    Monster s = (Monster)ois.readObject ();
                    monsters.add(s);
                }
            }catch (EOFException e){

            }catch (Exception e){
                e.printStackTrace ();
            }finally{
                try{
                    if (ois != null) ois.close();
                }catch (IOException e){
                    e.printStackTrace ();
                }
            }
        }

        return monsters;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("skainet_dk", "Close request received checking extras");

            if(action.equals(DESTROY_REQUEST)) {
                Log.d("skainet_dk", "Close request received");

                SelectionActivity.this.finish();
            }
        }
    };
}
