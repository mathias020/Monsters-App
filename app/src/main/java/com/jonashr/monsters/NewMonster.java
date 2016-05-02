package com.jonashr.monsters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class NewMonster extends Activity {

    public static final String EXTRA_MONSTER = "EXTRA_MONSTER";

    private RelativeLayout monster_frame;
    private ImageView monster_icon;
    private ImageView monster_element;

    private TextView monster_name;
    private TextView monster_attack_1;
    private TextView monster_attack_2;
    private TextView monster_attack_3;

    private TextView monster_atk;
    private TextView monster_def;
    private TextView monster_hp;

    private TextView btn_catchMonster;
    private TextView btn_letGo;

    private Monster monster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_monster);

        monster_frame = (RelativeLayout) findViewById(R.id.newMonster_frame);
        monster_icon = (ImageView) findViewById(R.id.newMonster_icon);
        monster_element = (ImageView) findViewById(R.id.newMonster_element);

        monster_name = (TextView) findViewById(R.id.newMonster_name);

        monster_attack_1 = (TextView) findViewById(R.id.newMonster_attack_1);
        monster_attack_2 = (TextView) findViewById(R.id.newMonster_attack_2);
        monster_attack_3 = (TextView) findViewById(R.id.newMonster_attack_3);

        monster_atk = (TextView) findViewById(R.id.newMonster_atk);
        monster_hp = (TextView) findViewById(R.id.newMonster_hp);
        monster_def = (TextView) findViewById(R.id.newMonster_def);

        btn_catchMonster = (TextView) findViewById(R.id.btn_catchMonster);
        btn_letGo = (TextView) findViewById(R.id.btn_letGo);

        Bundle extras = getIntent().getExtras();

        if(extras.containsKey(EXTRA_MONSTER)) {
            monster = (Monster) extras.get(EXTRA_MONSTER);

            monster_frame.setBackgroundResource(Monster.getFrameIcon(monster.getType()));
            monster_icon.setBackgroundResource(monster.getIcon());
            monster_element.setBackgroundResource(Monster.getElementIcon(monster.element()));

            monster_name.setText(monster.getName());
            monster_attack_1.setText(monster.getAttacks()[0].getName());
            monster_attack_2.setText(monster.getAttacks()[1].getName());
            monster_attack_3.setText(monster.getAttacks()[2].getName());

            monster_atk.setText(monster.getAttackPower(monster.element()) + " ATK");
            monster_hp.setText(monster.getMaxHealth() + " HP");
            monster_def.setText(monster.getDefense() + " DEF");

            btn_catchMonster.setOnClickListener(catchMonster);
            btn_letGo.setOnClickListener(letGo);

        }
    }

    private View.OnClickListener catchMonster = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            (new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    MainActivity.saveMonster(NewMonster.this, monster);

                    if(monster.getType().equals(Monster.TYPE_BASIC))
                        Statistics.addToStat(NewMonster.this, Statistics.BASICS, 1);
                    else  if(monster.getType().equals(Monster.TYPE_RARE))
                        Statistics.addToStat(NewMonster.this, Statistics.RARES, 1);
                    else  if(monster.getType().equals(Monster.TYPE_LEGENDARY))
                        Statistics.addToStat(NewMonster.this, Statistics.LEGENDARIES, 1);
                    else  if(monster.getType().equals(Monster.TYPE_MYTHIC))
                        Statistics.addToStat(NewMonster.this, Statistics.MYTHICS, 1);

                    return null;
                }

            }).execute();

            finish();
        }
    };

    private View.OnClickListener letGo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(NewMonster.this)
                    .setTitle("Let Go")
                    .setMessage("Are you sure?")
                    .setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            return;
                        }
                    })
                    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            NewMonster.this.finish();
                        }
                    }).create().show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_monster, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
