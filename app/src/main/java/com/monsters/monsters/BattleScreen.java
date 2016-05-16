package com.jonashr.monsters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class BattleScreen extends Activity {

    public static final String MONSTER_ATTACKED = "MONSTER_ATTACKED";
    public static final String MONSTER_TARGET = "MONSTER_TARGET";
    public static final String MONSTER_ATTK_ID = "MONSTER_ATTK_ID";
    public static final String MONSTER_NEW_HEALTH = "MONSTER_NEW_HEALTH";
    public static final String MONSTER_WAS_HEALED = "MONSTER_WAS_HEALED";
    public static final String MONSTER_ORIGIN = "MONSTER_ORIGIN";
    public static final String YOU_LOST = "YOU_LOST";
    public static final String MONSTER_DAMAGE_DONE = "MONSTER_DAMAGE_DONE";
    public static final String MONSTER_DAMAGE_TYPE = "MONSTER_DAMAGE_TYPE";
    public static final String MONSTER_HEALING_DONE = "MONSTER_HEALING_DONE";
    public static final String MONSTER_PARALYZED = "MONSTER_PARALYZED";
    private LinearLayout monster_bottom_second;


    // Damage types for floating text
    public static final String ATK_TYPE_DAMAGE = "ATK_TYPE_DAMAGE";
    public static final String ATK_TYPE_HEAL = "ATK_TYPE_HEAL";
    public static final String ATK_TYPE_PARALYZE = "ATK_TYPE_PARALYZE";

    private LinearLayout monster_top_third;

    private TranslateAnimation animation;

    // Animation specifics
    private static final int ANIMATION_OFFSET = 10;

    private int[] startPos;
    private int[] endPos;

    // My monsters
    private ImageView myMonster1_icon;
    private RelativeLayout myMonster1_frame;
    private TextView myMonster1_tag;
    private TextView myMonster1_health;
    private ImageView myMonster1_element;

    private ImageView myMonster2_icon;
    private RelativeLayout myMonster2_frame;
    private TextView myMonster2_tag;
    private TextView myMonster2_health;
    private ImageView myMonster2_element;


    private ImageView myMonster3_icon;
    private RelativeLayout myMonster3_frame;
    private TextView myMonster3_tag;
    private TextView myMonster3_health;
    private ImageView myMonster3_element;



    // Enemy monsters
    private ImageView enemyMonster1_icon;
    private RelativeLayout enemyMonster1_frame;
    private TextView enemyMonster1_tag;
    private TextView enemyMonster1_health;
    private ImageView enemyMonster1_element;

    private ImageView enemyMonster2_icon;
    private RelativeLayout enemyMonster2_frame;
    private TextView enemyMonster2_tag;
    private TextView enemyMonster2_health;
    private ImageView enemyMonster2_element;

    private ImageView enemyMonster3_icon;
    private RelativeLayout enemyMonster3_frame;
    private TextView enemyMonster3_tag;
    private TextView enemyMonster3_health;
    private ImageView enemyMonster3_element;

    // Text view
    private TextView waitingForOpponent;
    public static final String WAITING_FOR_OPPONENT = "WAITING_FOR_OPPONENT";
    public static final String SHUTDOWN_BATTLE_SCREEN = "SHUTDOWN_BATTLE_SCREEN";
    public static final String MY_TURN = "MY_TURN";

    private IntentFilter mIntentFilter;


    // Battle
    private RelativeLayout battleMenu;
    private RelativeLayout attackMenu;
    private TextView selectMonster;
    private TextView selectOpponent;

    private TextView attack1;
    private TextView attack2;
    private TextView attack3;

    private TextView goBack;

    private boolean myTurn = false;

    public static boolean hasBattleEnded = false;
    public static boolean hasWon = false;

    // End game screen
    private RelativeLayout endGameView;
    private TextView endGameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_battle_screen);

        // End game screen
        endGameView = (RelativeLayout) findViewById(R.id.endGameView);
        endGameText = (TextView) findViewById(R.id.gameEndText);

        waitingForOpponent = (TextView) findViewById(R.id.waitingForOpponent);

        // Monsters and frames
        enemyMonster1_frame = (RelativeLayout) findViewById(R.id.battle_top_first_monster);
        enemyMonster1_icon = (ImageView) findViewById(R.id.battle_top_first_monster_icon);
        enemyMonster1_tag = (TextView) findViewById(R.id.battle_top_first_monster_tag);
        enemyMonster1_health = (TextView) findViewById(R.id.battle_top_first_monster_health);
        enemyMonster1_element = (ImageView) findViewById(R.id.battle_top_first_monster_element);

        enemyMonster2_frame = (RelativeLayout) findViewById(R.id.battle_top_second_monster);
        enemyMonster2_icon = (ImageView) findViewById(R.id.battle_top_second_monster_icon);
        enemyMonster2_tag = (TextView) findViewById(R.id.battle_top_second_monster_tag);
        enemyMonster2_health = (TextView) findViewById(R.id.battle_top_second_monster_health);
        enemyMonster2_element = (ImageView) findViewById(R.id.battle_top_second_monster_element);

        enemyMonster3_frame = (RelativeLayout) findViewById(R.id.battle_top_third_monster);
        enemyMonster3_icon = (ImageView) findViewById(R.id.battle_top_third_monster_icon);
        enemyMonster3_tag = (TextView) findViewById(R.id.battle_top_third_monster_tag);
        enemyMonster3_health = (TextView) findViewById(R.id.battle_top_third_monster_health);
        enemyMonster3_element = (ImageView) findViewById(R.id.battle_top_third_monster_element);


        myMonster1_frame = (RelativeLayout) findViewById(R.id.battle_bottom_first_monster);
        myMonster1_icon = (ImageView) findViewById(R.id.battle_bottom_first_monster_icon);
        myMonster1_tag = (TextView) findViewById(R.id.battle_bottom_first_monster_tag);
        myMonster1_health = (TextView) findViewById(R.id.battle_bottom_first_monster_health);
        myMonster1_element = (ImageView) findViewById(R.id.battle_bottom_first_monster_element);

        myMonster2_frame = (RelativeLayout) findViewById(R.id.battle_bottom_second_monster);
        myMonster2_icon = (ImageView) findViewById(R.id.battle_bottom_second_monster_icon);
        myMonster2_tag = (TextView) findViewById(R.id.battle_bottom_second_monster_tag);
        myMonster2_health = (TextView) findViewById(R.id.battle_bottom_second_monster_health);
        myMonster2_element = (ImageView) findViewById(R.id.battle_bottom_second_monster_element);

        myMonster3_frame = (RelativeLayout) findViewById(R.id.battle_bottom_third_monster);
        myMonster3_icon = (ImageView) findViewById(R.id.battle_bottom_third_monster_icon);
        myMonster3_tag = (TextView) findViewById(R.id.battle_bottom_third_monster_tag);
        myMonster3_health = (TextView) findViewById(R.id.battle_bottom_third_monster_health);
        myMonster3_element = (ImageView) findViewById(R.id.battle_bottom_third_monster_element);

        // Battle design
        battleMenu = (RelativeLayout) findViewById(R.id.battleMenu);
        attackMenu = (RelativeLayout) findViewById(R.id.attackMenu);

        selectMonster = (TextView) findViewById(R.id.selectMonster);
        selectOpponent = (TextView) findViewById(R.id.selectOpponent);

        attack1 = (TextView) findViewById(R.id.monster_attack_1);
        attack2 = (TextView) findViewById(R.id.monster_attack_2);
        attack3 = (TextView) findViewById(R.id.monster_attack_3);

        goBack = (TextView) findViewById(R.id.monster_attack_back);

        setEnemyMonsters();
        setMyMonsters();

        // If I am the group owner and the enemy is already ready when I enter the battle screen -> start the fight
        if(MainActivity.serverConn instanceof ServerConnection) {
            if(MainActivity.enemyMonsters.size() == 3) {
                Animation menuUp = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.menu_up);
                battleMenu.startAnimation(menuUp);
                battleMenu.setVisibility(View.VISIBLE);

                setMyClickListeners();

                selectMonster.setVisibility(View.VISIBLE);
                selectOpponent.setVisibility(View.INVISIBLE);
                attackMenu.setVisibility(View.INVISIBLE);

                myTurn = true;
                selectedMonster = -1;
                selectedAttack = -1;
            }
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WAITING_FOR_OPPONENT);
        mIntentFilter.addAction(SHUTDOWN_BATTLE_SCREEN);
        mIntentFilter.addAction(MY_TURN);
        mIntentFilter.addAction(MONSTER_ATTACKED);
        mIntentFilter.addAction(YOU_LOST);
        mIntentFilter.addAction(MONSTER_PARALYZED);
    }
    public int getDP(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void simpleScaleAnimation(final View target, final int animationResource) {
        final ImageView animationObject = new ImageView(this);
        RelativeLayout.LayoutParams animationObjectParams = new RelativeLayout.LayoutParams(getDP(5), getDP(5));
        animationObjectParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        animationObjectParams.addRule(RelativeLayout.CENTER_VERTICAL);
        animationObject.setBackgroundResource(animationResource);
        animationObject.setLayoutParams(animationObjectParams);

        ((RelativeLayout)target).addView(animationObject);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(animationObject, "scaleX", 14.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(animationObject, "scaleY", 12.0f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.setDuration(700);
        animSet.start();

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationObject.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void simpleAnimation(final View target, final int animationResource) {
        final ImageView animationObject = new ImageView(this);
        RelativeLayout.LayoutParams animationObjectParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDP(20));

        animationObject.setBackgroundResource(animationResource);

        animationObject.setLayoutParams(animationObjectParams);

        ((RelativeLayout)target).addView(animationObject);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.effect_up);
        animationObject.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationObject.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void battleText(final View target, final int damage, final String attackType) {
        final TextView floatingText = new TextView(this);

        if(attackType.equals(ATK_TYPE_DAMAGE)) {
            floatingText.setTextColor(0xfff80000);
            floatingText.setText("-" + damage);
        } else if(attackType.equals(ATK_TYPE_HEAL)) {
            floatingText.setTextColor(0xff2aff00);
            floatingText.setText("+" + damage);
        } else if(attackType.equals(ATK_TYPE_PARALYZE)) {
            floatingText.setTextColor(0xffeef100);
            floatingText.setText("-" + damage);
        }

        floatingText.setTypeface(null, Typeface.BOLD);
        floatingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        ((RelativeLayout)target).addView(floatingText);

        Animation floatingtextAnim = AnimationUtils.loadAnimation(this, R.anim.floatingtext);
        floatingText.startAnimation(floatingtextAnim);

        floatingtextAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                floatingText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation(final View target) {
        Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

        target.startAnimation(animShake);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(receiver, mIntentFilter);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    private void attackAnimation(final View origin, final View target, final TextView originHealthBar, final TextView targetHealthBar, final int targetNewHealth, final int targetMaxHealth, final int originNewHealth, final int originMaxHealth, final int damage, final String attackType) {
        origin.bringToFront();
        ((View)origin.getParent()).bringToFront();

        int[] originStartPos = new int[2];
        origin.getLocationOnScreen(originStartPos);

        int[] targetPos = new int[2];
        target.getLocationOnScreen(targetPos);

        final int amountToMoveX = targetPos[0] - originStartPos[0];
        final int amountToMoveY = targetPos[1] - originStartPos[1];

        final AnimatorSet animScale = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(origin, "scaleX", 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(origin, "scaleY", 1.2f);

        animScale.playTogether(scaleX, scaleY);
        animScale.setInterpolator(new LinearInterpolator());
        animScale.setDuration(700);
        animScale.start();

        animScale.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                final AnimatorSet animSetXY = new AnimatorSet();

                ObjectAnimator y = ObjectAnimator.ofFloat(origin,
                        "translationY", 0, amountToMoveY);
                ObjectAnimator x = ObjectAnimator.ofFloat(origin,
                        "translationX", 0, amountToMoveX);

                animSetXY.playTogether(x, y);
                animSetXY.setInterpolator(new LinearInterpolator());
                animSetXY.setDuration(700);
                animSetXY.start();

                animSetXY.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        targetHealthBar.setText(targetNewHealth + " / " + targetMaxHealth);
                        originHealthBar.setText(originNewHealth + " / " + originMaxHealth);

                        battleText(target, damage, attackType);

                        animSetXY.setInterpolator(new ReverseInterpolator());
                        animSetXY.removeAllListeners();
                        animSetXY.start();


                        animScale.removeAllListeners();
                        animScale.setInterpolator(new ReverseInterpolator());
                        animScale.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float paramFloat) {
            return Math.abs(paramFloat -1f);
        }
    }


    public void setEnemyMonsters() {
        if(MainActivity.enemyMonsters.size() == 3) {
            waitingForOpponent.setVisibility(View.INVISIBLE);

            enemyMonster1_frame.setBackgroundResource(Monster.getFrameIcon(MainActivity.enemyMonsters.get(0).getType()));
            enemyMonster1_icon.setBackgroundResource(MainActivity.enemyMonsters.get(0).getIcon());
            enemyMonster1_tag.setText(MainActivity.enemyMonsters.get(0).getName());
            enemyMonster1_health.setText(MainActivity.enemyMonsters.get(0).health() + " / " + MainActivity.enemyMonsters.get(0).getMaxHealth());
            enemyMonster1_element.setBackgroundResource(Monster.getElementIcon(MainActivity.enemyMonsters.get(0).element()));

            enemyMonster2_frame.setBackgroundResource(Monster.getFrameIcon(MainActivity.enemyMonsters.get(1).getType()));
            enemyMonster2_icon.setBackgroundResource(MainActivity.enemyMonsters.get(1).getIcon());
            enemyMonster2_tag.setText(MainActivity.enemyMonsters.get(1).getName());
            enemyMonster2_health.setText(MainActivity.enemyMonsters.get(1).health() + " / " + MainActivity.enemyMonsters.get(1).getMaxHealth());
            enemyMonster2_element.setBackgroundResource(Monster.getElementIcon(MainActivity.enemyMonsters.get(1).element()));

            enemyMonster3_frame.setBackgroundResource(Monster.getFrameIcon(MainActivity.enemyMonsters.get(2).getType()));
            enemyMonster3_icon.setBackgroundResource(MainActivity.enemyMonsters.get(2).getIcon());
            enemyMonster3_tag.setText(MainActivity.enemyMonsters.get(2).getName());
            enemyMonster3_health.setText(MainActivity.enemyMonsters.get(2).health() + " / " + MainActivity.enemyMonsters.get(2).getMaxHealth());
            enemyMonster3_element.setBackgroundResource(Monster.getElementIcon(MainActivity.enemyMonsters.get(2).element()));
        }
    }

    public void setMyMonsters() {
        if(MainActivity.myMonsters.size() == 3) {
            myMonster1_frame.setBackgroundResource(Monster.getFrameIcon(MainActivity.myMonsters.get(0).getType()));
            myMonster1_icon.setBackgroundResource(MainActivity.myMonsters.get(0).getIcon());
            myMonster1_tag.setText(MainActivity.myMonsters.get(0).getName());
            myMonster1_health.setText(MainActivity.myMonsters.get(0).health() + " / " + MainActivity.myMonsters.get(0).getMaxHealth());
            myMonster1_element.setBackgroundResource(Monster.getElementIcon(MainActivity.myMonsters.get(0).element()));

            myMonster2_frame.setBackgroundResource(Monster.getFrameIcon(MainActivity.myMonsters.get(1).getType()));
            myMonster2_icon.setBackgroundResource(MainActivity.myMonsters.get(1).getIcon());
            myMonster2_tag.setText(MainActivity.myMonsters.get(1).getName());
            myMonster2_health.setText(MainActivity.myMonsters.get(1).health() + " / " + MainActivity.myMonsters.get(1).getMaxHealth());
            myMonster2_element.setBackgroundResource(Monster.getElementIcon(MainActivity.myMonsters.get(1).element()));

            myMonster3_frame.setBackgroundResource(Monster.getFrameIcon(MainActivity.myMonsters.get(2).getType()));
            myMonster3_icon.setBackgroundResource(MainActivity.myMonsters.get(2).getIcon());
            myMonster3_tag.setText(MainActivity.myMonsters.get(2).getName());
            myMonster3_health.setText(MainActivity.myMonsters.get(2).health() + " / " + MainActivity.myMonsters.get(2).getMaxHealth());
            myMonster3_element.setBackgroundResource(Monster.getElementIcon(MainActivity.myMonsters.get(2).element()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_battle_screen, menu);
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

    private int selectedMonster;
    private int selectedAttack;

    private void openEndGame() {
        if(hasWon) {
            endGameText.setText("! YOU WON !");
            endGameText.setTextColor(0xff32ff0c);

            endGameView.setVisibility(View.VISIBLE);
        }
        else
        {
            endGameText.setText("! YOU LOST !");
            endGameText.setTextColor(0xfffff7ee);

            endGameView.setVisibility(View.VISIBLE);
        }

        endGameView.setOnClickListener(endGameViewClick);
    }

    private View.OnClickListener endGameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hasBattleEnded = true;

            setResult(RESULT_OK);
            finish();
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(WAITING_FOR_OPPONENT)) {
                setEnemyMonsters();
                // Start game loop now
            }

            else if(action.equals(SHUTDOWN_BATTLE_SCREEN)) {
                BattleScreen.this.finish();
            }

            else if(action.equals(MY_TURN)) {
                Animation menuUp = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.menu_up);
                battleMenu.startAnimation(menuUp);
                battleMenu.setVisibility(View.VISIBLE);

                setMyClickListeners();

                selectMonster.setVisibility(View.VISIBLE);
                selectOpponent.setVisibility(View.INVISIBLE);
                attackMenu.setVisibility(View.INVISIBLE);

                myTurn = true;
                selectedMonster = -1;
                selectedAttack = -1;
            }

            else if(action.equals(MONSTER_ATTACKED)) {
                int origin = intent.getIntExtra(MONSTER_ORIGIN, -1);
                int target = intent.getIntExtra(MONSTER_TARGET, -1);
                int multiAmount = intent.getIntExtra(MONSTER_WAS_HEALED, -1);
                int damageDone = intent.getIntExtra(MONSTER_DAMAGE_DONE, 0);
                int healingDone = intent.getIntExtra(MONSTER_HEALING_DONE, 0);
                String damageType = intent.getStringExtra(MONSTER_DAMAGE_TYPE);

                if(target == 0) {
                    if (origin == 0) {
                        if(multiAmount != Monster.ATTACK_HEAL) {
                            attackAnimation(enemyMonster1_frame, myMonster1_frame, enemyMonster1_health, myMonster1_health, MainActivity.myMonsters.get(0).health(), MainActivity.myMonsters.get(0).getMaxHealth(), MainActivity.enemyMonsters.get(0).health(), MainActivity.enemyMonsters.get(0).getMaxHealth(), damageDone, damageType);
                       } else {
                            simpleAnimation(enemyMonster1_frame, R.drawable.healline);
                            battleText(enemyMonster1_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster1_health.setText(MainActivity.enemyMonsters.get(0).health() + " / " + MainActivity.enemyMonsters.get(0).getMaxHealth());
                        }
                    } else if (origin == 1) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster2_frame, myMonster1_frame, enemyMonster2_health, myMonster1_health, MainActivity.myMonsters.get(0).health(), MainActivity.myMonsters.get(0).getMaxHealth(), MainActivity.enemyMonsters.get(1).health(), MainActivity.enemyMonsters.get(1).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster2_frame, R.drawable.healline);
                            battleText(enemyMonster2_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster2_health.setText(MainActivity.enemyMonsters.get(1).health() + " / " + MainActivity.enemyMonsters.get(1).getMaxHealth());
                        }
                    } else if (origin == 2) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster3_frame, myMonster1_frame, enemyMonster3_health, myMonster1_health, MainActivity.myMonsters.get(0).health(), MainActivity.myMonsters.get(0).getMaxHealth(), MainActivity.enemyMonsters.get(2).health(), MainActivity.enemyMonsters.get(2).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster3_frame, R.drawable.healline);
                            battleText(enemyMonster3_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster3_health.setText(MainActivity.enemyMonsters.get(2).health() + " / " + MainActivity.enemyMonsters.get(2).getMaxHealth());
                        }
                    }
                }
                else if(target == 1) {
                    if(origin == 0) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster1_frame, myMonster2_frame, enemyMonster1_health, myMonster2_health, MainActivity.myMonsters.get(1).health(), MainActivity.myMonsters.get(1).getMaxHealth(), MainActivity.enemyMonsters.get(0).health(), MainActivity.enemyMonsters.get(0).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster1_frame, R.drawable.healline);
                            battleText(enemyMonster1_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster1_health.setText(MainActivity.enemyMonsters.get(0).health() + " / " + MainActivity.enemyMonsters.get(0).getMaxHealth());
                        }
                    }
                    else if(origin == 1) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster2_frame, myMonster2_frame, enemyMonster2_health, myMonster2_health, MainActivity.myMonsters.get(1).health(), MainActivity.myMonsters.get(1).getMaxHealth(), MainActivity.enemyMonsters.get(1).health(), MainActivity.enemyMonsters.get(1).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster2_frame, R.drawable.healline);
                            battleText(enemyMonster2_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster2_health.setText(MainActivity.enemyMonsters.get(1).health() + " / " + MainActivity.enemyMonsters.get(1).getMaxHealth());
                        }
                    }
                    else if(origin == 2) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster3_frame, myMonster2_frame, enemyMonster3_health, myMonster2_health, MainActivity.myMonsters.get(1).health(), MainActivity.myMonsters.get(1).getMaxHealth(), MainActivity.enemyMonsters.get(2).health(), MainActivity.enemyMonsters.get(2).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster3_frame, R.drawable.healline);
                            battleText(enemyMonster3_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster3_health.setText(MainActivity.enemyMonsters.get(2).health() + " / " + MainActivity.enemyMonsters.get(2).getMaxHealth());
                        }
                    }
                }
                else if(target == 2) {
                    if(origin == 0) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster1_frame, myMonster3_frame, enemyMonster1_health, myMonster3_health, MainActivity.myMonsters.get(2).health(), MainActivity.myMonsters.get(2).getMaxHealth(), MainActivity.enemyMonsters.get(0).health(), MainActivity.enemyMonsters.get(0).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster1_frame, R.drawable.healline);
                            battleText(enemyMonster1_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster1_health.setText(MainActivity.enemyMonsters.get(0).health() + " / " + MainActivity.enemyMonsters.get(0).getMaxHealth());
                        }
                    }
                    else if(origin == 1) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster2_frame, myMonster3_frame, enemyMonster2_health, myMonster3_health, MainActivity.myMonsters.get(2).health(), MainActivity.myMonsters.get(2).getMaxHealth(), MainActivity.enemyMonsters.get(1).health(), MainActivity.enemyMonsters.get(1).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster2_frame, R.drawable.healline);
                            battleText(enemyMonster2_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster2_health.setText(MainActivity.enemyMonsters.get(1).health() + " / " + MainActivity.enemyMonsters.get(1).getMaxHealth());
                        }
                    }
                    else if(origin == 2) {
                        if(multiAmount != Monster.ATTACK_HEAL)
                            attackAnimation(enemyMonster3_frame, myMonster3_frame, enemyMonster3_health, myMonster3_health, MainActivity.myMonsters.get(2).health(), MainActivity.myMonsters.get(2).getMaxHealth(), MainActivity.enemyMonsters.get(2).health(), MainActivity.enemyMonsters.get(2).getMaxHealth(), damageDone, damageType);
                        else {
                            simpleAnimation(enemyMonster3_frame, R.drawable.healline);
                            battleText(enemyMonster3_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                            enemyMonster3_health.setText(MainActivity.enemyMonsters.get(2).health() + " / " + MainActivity.enemyMonsters.get(2).getMaxHealth());
                        }
                    }
                }
            }

            else if(action.equals(YOU_LOST)) {
                hasBattleEnded = true;
                hasWon = false;

                openEndGame();
            }

            else if(action.equals(MONSTER_PARALYZED)) {
                int origin = intent.getIntExtra(MONSTER_ORIGIN, -1);

                if(origin == 0)
                    shakeAnimation(enemyMonster1_frame);
                else if(origin == 1)
                    shakeAnimation(enemyMonster2_frame);
                else if(origin == 2)
                    shakeAnimation(enemyMonster3_frame);
            }
        }
    };

    private View.OnClickListener clickMyMonster = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == myMonster1_frame) {
                if(MainActivity.myMonsters.get(0).health() > 0) {
                    selectedMonster = 0;
                    openAttackMenu();
                    removeMyClickListeners();
                }
            }

            if(v == myMonster2_frame) {
                if(MainActivity.myMonsters.get(1).health() > 0) {
                    selectedMonster = 1;
                    openAttackMenu();
                    removeMyClickListeners();
                }
            }

            if(v == myMonster3_frame) {
                if(MainActivity.myMonsters.get(2).health() > 0) {
                    selectedMonster = 2;
                    openAttackMenu();
                    removeMyClickListeners();
                }
            }
        }
    };

    private View.OnClickListener clickEnemyMonster = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == enemyMonster1_frame) {
                if(MainActivity.enemyMonsters.get(0).health() > 0) {
                    int oldHealth = MainActivity.enemyMonsters.get(0).health();
                    int[] multiAmount =MainActivity.myMonsters.get(selectedMonster).attackMonster(selectedAttack, MainActivity.enemyMonsters.get(0), -1, -1);
                    int damageDone = oldHealth - MainActivity.enemyMonsters.get(0).health();

                    String dmgType;
                    if(MainActivity.myMonsters.get(selectedMonster).getAttacks()[selectedAttack].getEffect().equals("paralyze"))
                        dmgType = ATK_TYPE_PARALYZE;
                    else
                        dmgType = ATK_TYPE_DAMAGE;

                    if(multiAmount[0] != -2) {
                        if (selectedMonster == 0)
                            attackAnimation(myMonster1_frame, enemyMonster1_frame, myMonster1_health, enemyMonster1_health, MainActivity.enemyMonsters.get(0).health(), MainActivity.enemyMonsters.get(0).getMaxHealth(), MainActivity.myMonsters.get(0).health(), MainActivity.myMonsters.get(0).getMaxHealth(), damageDone, dmgType);
                        else if (selectedMonster == 1)
                            attackAnimation(myMonster2_frame, enemyMonster1_frame, myMonster2_health, enemyMonster1_health, MainActivity.enemyMonsters.get(0).health(), MainActivity.enemyMonsters.get(0).getMaxHealth(), MainActivity.myMonsters.get(1).health(), MainActivity.myMonsters.get(1).getMaxHealth(), damageDone, dmgType);
                        else if (selectedMonster == 2)
                            attackAnimation(myMonster3_frame, enemyMonster1_frame, myMonster3_health, enemyMonster1_health, MainActivity.enemyMonsters.get(0).health(), MainActivity.enemyMonsters.get(0).getMaxHealth(), MainActivity.myMonsters.get(2).health(), MainActivity.myMonsters.get(2).getMaxHealth(), damageDone, dmgType);

                        attack(0, selectedMonster, selectedAttack, multiAmount[0], multiAmount[1]);
                    }
                    else {
                        if(selectedMonster == 0)
                            shakeAnimation(myMonster1_frame);
                        else if(selectedMonster == 1)
                            shakeAnimation(myMonster2_frame);
                        else if(selectedMonster == 2)
                            shakeAnimation(myMonster3_frame);

                        MainActivity.serverConn.enqueuePacket(5, selectedMonster);
                    }

                    endTurn();
                }
            }

            if(v == enemyMonster2_frame) {
                if(MainActivity.enemyMonsters.get(1).health() > 0) {
                    int oldHealth = MainActivity.enemyMonsters.get(1).health();
                    int[] multiAmount =MainActivity.myMonsters.get(selectedMonster).attackMonster(selectedAttack, MainActivity.enemyMonsters.get(1), -1, -1);
                    int damageDone = oldHealth - MainActivity.enemyMonsters.get(1).health();

                    String dmgType;
                    if(MainActivity.myMonsters.get(selectedMonster).getAttacks()[selectedAttack].getEffect().equals("paralyze"))
                        dmgType = ATK_TYPE_PARALYZE;
                    else
                        dmgType = ATK_TYPE_DAMAGE;

                    if(multiAmount[0] != -2) {
                        if (selectedMonster == 0)
                            attackAnimation(myMonster1_frame, enemyMonster2_frame, myMonster1_health, enemyMonster2_health, MainActivity.enemyMonsters.get(1).health(), MainActivity.enemyMonsters.get(1).getMaxHealth(), MainActivity.myMonsters.get(0).health(), MainActivity.myMonsters.get(0).getMaxHealth(), damageDone, dmgType);
                        else if (selectedMonster == 1)
                            attackAnimation(myMonster2_frame, enemyMonster2_frame, myMonster2_health, enemyMonster2_health, MainActivity.enemyMonsters.get(1).health(), MainActivity.enemyMonsters.get(1).getMaxHealth(), MainActivity.myMonsters.get(1).health(), MainActivity.myMonsters.get(1).getMaxHealth(), damageDone, dmgType);
                        else if (selectedMonster == 2)
                            attackAnimation(myMonster3_frame, enemyMonster2_frame, myMonster3_health, enemyMonster2_health, MainActivity.enemyMonsters.get(1).health(), MainActivity.enemyMonsters.get(1).getMaxHealth(), MainActivity.myMonsters.get(2).health(), MainActivity.myMonsters.get(2).getMaxHealth(), damageDone, dmgType);

                        attack(1, selectedMonster, selectedAttack, multiAmount[0], multiAmount[1]);
                    }
                    else {
                        if(selectedMonster == 0)
                            shakeAnimation(myMonster1_frame);
                        else if(selectedMonster == 1)
                            shakeAnimation(myMonster2_frame);
                        else if(selectedMonster == 2)
                            shakeAnimation(myMonster3_frame);

                        MainActivity.serverConn.enqueuePacket(5, selectedMonster);
                    }

                    endTurn();
                }
            }

            if(v == enemyMonster3_frame) {
                if(MainActivity.enemyMonsters.get(2).health() > 0) {
                    int oldHealth = MainActivity.enemyMonsters.get(2).health();
                    int[] multiAmount =MainActivity.myMonsters.get(selectedMonster).attackMonster(selectedAttack, MainActivity.enemyMonsters.get(2), -1, -1);
                    int damageDone = oldHealth - MainActivity.enemyMonsters.get(2).health();

                    String dmgType;
                    if(MainActivity.myMonsters.get(selectedMonster).getAttacks()[selectedAttack].getEffect().equals("paralyze"))
                        dmgType = ATK_TYPE_PARALYZE;
                    else
                        dmgType = ATK_TYPE_DAMAGE;

                    if(multiAmount[0] != -2) {
                        if (selectedMonster == 0)
                            attackAnimation(myMonster1_frame, enemyMonster3_frame, myMonster1_health, enemyMonster3_health, MainActivity.enemyMonsters.get(2).health(), MainActivity.enemyMonsters.get(2).getMaxHealth(), MainActivity.myMonsters.get(0).health(), MainActivity.myMonsters.get(0).getMaxHealth(), damageDone, dmgType);
                        else if (selectedMonster == 1)
                            attackAnimation(myMonster2_frame, enemyMonster3_frame, myMonster2_health, enemyMonster3_health, MainActivity.enemyMonsters.get(2).health(), MainActivity.enemyMonsters.get(2).getMaxHealth(), MainActivity.myMonsters.get(1).health(), MainActivity.myMonsters.get(1).getMaxHealth(), damageDone, dmgType);
                        else if (selectedMonster == 2)
                            attackAnimation(myMonster3_frame, enemyMonster3_frame, myMonster3_health, enemyMonster3_health, MainActivity.enemyMonsters.get(2).health(), MainActivity.enemyMonsters.get(2).getMaxHealth(), MainActivity.myMonsters.get(2).health(), MainActivity.myMonsters.get(2).getMaxHealth(), damageDone, dmgType);

                        attack(2, selectedMonster, selectedAttack, multiAmount[0], multiAmount[1]);
                    }
                    else {
                        if(selectedMonster == 0)
                            shakeAnimation(myMonster1_frame);
                        else if(selectedMonster == 1)
                            shakeAnimation(myMonster2_frame);
                        else if(selectedMonster == 2)
                            shakeAnimation(myMonster3_frame);

                        MainActivity.serverConn.enqueuePacket(5, selectedMonster);
                    }

                    endTurn();
                }
            }
        }
    };

    private void attack(int target, int origin, int attack, int multiAmount, int paralyzeRand) {
        MainActivity.serverConn.enqueuePacket(2, target, origin, attack, multiAmount, paralyzeRand);
    }

    private void endTurn() {
        battleMenu.setVisibility(View.INVISIBLE);
        selectOpponent.setVisibility(View.INVISIBLE);

        removeEnemyClickListeners();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.enemyMonsters.get(0).health() <= 0 &&
                        MainActivity.enemyMonsters.get(1).health() <= 0 &&
                        MainActivity.enemyMonsters.get(2).health() <= 0) {
                    MainActivity.serverConn.enqueuePacket(4, new Object[]{});
                    hasWon = true;
                    hasBattleEnded = true;
                    openEndGame();
                } else {
                    MainActivity.serverConn.enqueuePacket(3, new Object[]{});
                }
            }
        }, 2000);

    }

    private void removeMyClickListeners() {
        myMonster1_frame.setOnClickListener(null);
        myMonster2_frame.setOnClickListener(null);
        myMonster3_frame.setOnClickListener(null);
    }

    private void removeEnemyClickListeners() {
        enemyMonster1_frame.setOnClickListener(null);
        enemyMonster2_frame.setOnClickListener(null);
        enemyMonster3_frame.setOnClickListener(null);
    }

    private void setMyClickListeners() {
        myMonster1_frame.setOnClickListener(clickMyMonster);
        myMonster2_frame.setOnClickListener(clickMyMonster);
        myMonster3_frame.setOnClickListener(clickMyMonster);
    }

    private void setEnemyClickListeners() {
        enemyMonster1_frame.setOnClickListener(clickEnemyMonster);
        enemyMonster2_frame.setOnClickListener(clickEnemyMonster);
        enemyMonster3_frame.setOnClickListener(clickEnemyMonster);
    }

    private void openAttackMenu() {
        selectMonster.setVisibility(View.INVISIBLE);
        Attack[] attacks = MainActivity.myMonsters.get(selectedMonster).getAttacks();

        attack1.setText((attacks[0] != null ? attacks[0].getName() : ""));
        attack2.setText((attacks[1] != null ? attacks[1].getName() : ""));
        attack3.setText((attacks[2] != null ? attacks[2].getName() : ""));

        attack1.setOnClickListener(attackBtn);
        attack2.setOnClickListener(attackBtn);
        attack3.setOnClickListener(attackBtn);
        goBack.setOnClickListener(goBackBtn);

        attackMenu.setVisibility(View.VISIBLE);

        removeMyClickListeners();
    }

    private View.OnClickListener attackBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == attack1) {
                selectedAttack = 0;
                selectOpponent();
            }
            else if(v == attack2) {
                selectedAttack = 1;
                selectOpponent();
            }
            else if(v == attack3) {
                selectedAttack = 2;
                selectOpponent();
            }
        }
    };

    private void selectOpponent() {
        if(selectedAttack != -1 && selectedMonster != -1) {
            if(MainActivity.myMonsters.get(selectedMonster).getAttacks()[selectedAttack].getId() == 3) {
                int oldHealth = MainActivity.myMonsters.get(selectedMonster).health();
                int[] multiAmount = MainActivity.myMonsters.get(selectedMonster).attackMonster(selectedAttack, MainActivity.enemyMonsters.get(0), -1, -1);
                int healingDone = oldHealth - MainActivity.myMonsters.get(selectedMonster).health();

                if(selectedMonster == 0) {
                    simpleAnimation(myMonster1_frame, R.drawable.healline);
                    myMonster1_health.setText(MainActivity.myMonsters.get(0).health() + " / " + MainActivity.myMonsters.get(0).getMaxHealth());
                    battleText(myMonster1_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                }
                else if(selectedMonster == 1) {
                    simpleAnimation(myMonster2_frame, R.drawable.healline);
                    myMonster2_health.setText(MainActivity.myMonsters.get(1).health() + " / " + MainActivity.myMonsters.get(1).getMaxHealth());
                    battleText(myMonster2_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                } else if(selectedMonster == 2) {
                    simpleAnimation(myMonster3_frame, R.drawable.healline);
                    myMonster3_health.setText(MainActivity.myMonsters.get(2).health() + " / " + MainActivity.myMonsters.get(2).getMaxHealth());
                    battleText(myMonster3_frame, Math.abs(healingDone), ATK_TYPE_HEAL);
                }

                attack(0, selectedMonster, selectedAttack, multiAmount[0], multiAmount[1]);
                attackMenu.setVisibility(View.INVISIBLE);

                removeMyClickListeners();
                endTurn();
            } else {
                attackMenu.setVisibility(View.INVISIBLE);
                selectOpponent.setVisibility(View.VISIBLE);

                removeMyClickListeners();
                setEnemyClickListeners();
            }
        }
    }

    private View.OnClickListener goBackBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attackMenu.setVisibility(View.INVISIBLE);
            selectMonster.setVisibility(View.VISIBLE);

            setMyClickListeners();
        }
    };



    public void onBackPressed() {
        if(hasBattleEnded == true) {
            setResult(RESULT_OK);
            finish();
        } else {
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
                            BattleScreen.super.onBackPressed();
                        }
                    }).create().show();
        }

        return;
    }
}
