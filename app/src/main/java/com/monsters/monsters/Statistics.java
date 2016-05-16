package com.jonashr.monsters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;
import java.util.Set;

public class Statistics extends Activity {

    public static final String PREFS_STATISTICS = "myStats_monsters";

    // Constants for statistic keys
    public static final String TOTAL_MATCHES = "TOTAL_MATCHES";
    public static final String WINS = "WINS";
    public static final String LOSSES = "LOSSES";

    public static final String TOTAL_MONSTERS = "TOTAL_MONSTERS";
    public static final String BASICS = "BASICS";
    public static final String RARES = "RARES";
    public static final String LEGENDARIES = "LEGENDARIES";
    public static final String MYTHICS = "MYTHICS";

    // Scroll view
    private ScrollView mScrollView;

    // Fields
    private TextView tv_totalMatches;
    private TextView tv_wins;
    private TextView tv_losses;
    private TextView tv_quits;

    private TextView tv_totalMonsters;
    private TextView tv_basics;
    private TextView tv_rares;
    private TextView tv_legendaries;
    private TextView tv_mythics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_statistics);

        mScrollView = (ScrollView) findViewById(R.id.statistics_box);
        mScrollView.setFadingEdgeLength(150);

        tv_totalMatches = (TextView) findViewById(R.id.numberOfMatches);
        tv_totalMatches.setText(loadStat(this, TOTAL_MATCHES));

        tv_wins = (TextView) findViewById(R.id.numberOfWins);
        tv_wins.setText(loadStat(this, WINS));

        tv_losses = (TextView) findViewById(R.id.numberOfLosses);
        tv_losses.setText(loadStat(this, LOSSES));

        tv_quits = (TextView) findViewById(R.id.quitPercentage);
        tv_quits.setText(calcQuits() + " %");

        tv_totalMonsters = (TextView) findViewById(R.id.numberOfMonsters);
        tv_totalMonsters.setText(String.valueOf(calcMonsters()));

        tv_basics = (TextView) findViewById(R.id.numberOfBasics);
        tv_basics.setText(loadStat(this, BASICS));

        tv_rares = (TextView) findViewById(R.id.numberOfRares);
        tv_rares.setText(loadStat(this, RARES));

        tv_legendaries = (TextView) findViewById(R.id.numberOfLegendaries);
        tv_legendaries.setText(loadStat(this, LEGENDARIES));

        tv_mythics = (TextView) findViewById(R.id.numberOfMythics);
        tv_mythics.setText(loadStat(this, MYTHICS));
    }

    private int calcMonsters() {
        int basics = Integer.parseInt(loadStat(this, BASICS));
        int rares = Integer.parseInt(loadStat(this, RARES));
        int legendaries = Integer.parseInt(loadStat(this, LEGENDARIES));
        int mythics = Integer.parseInt(loadStat(this, MYTHICS));

        return basics + rares + legendaries + mythics;
    }

    private int calcQuits() {
        int totalMatches = Integer.parseInt(loadStat(this, TOTAL_MATCHES));
        int wins = Integer.parseInt(loadStat(this, WINS));
        int losses = Integer.parseInt(loadStat(this, LOSSES));

        int quits = totalMatches - wins - losses;

        if(totalMatches == 0)
            return 0;
        else {
            float quitP = ((float)quits / totalMatches) * 100;
            return Math.round(quitP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
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

    public static String loadStat(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_STATISTICS, MODE_PRIVATE);

        return String.valueOf(sharedPref.getInt(key, 0));
    }

    public static void saveStat(Context context, String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_STATISTICS, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();

        edit.putInt(key, value);
        edit.commit();
    }

    public static void saveStat(Context context, Map<String, Integer> prefs) {
        Set<String> keys = prefs.keySet();

        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_STATISTICS, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();

        for(String key : keys) {
            edit.putInt(key, prefs.get(key));
        }

        edit.commit();
    }

    public static void addToStat(Context context, String key, int amount) {
        int stat = Integer.parseInt(loadStat(context, key));

        saveStat(context, key, stat+amount);
    }
}
