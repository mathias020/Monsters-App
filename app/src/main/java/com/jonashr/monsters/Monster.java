package com.jonashr.monsters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mathiashr on 06-11-2015.
 */
public class Monster implements Serializable
{
    private String name;
    private String monsterType;
    private String element;
    private int health;
    private int attackPower;
    private int defense;
    private Attack[] attacks;
    private int icon;
    private String effectOn;
    private int maxHealth;

    public static final String TYPE_BASIC = "Basic";
    public static final String TYPE_RARE = "Rare";
    public static final String TYPE_LEGENDARY = "Legendary";
    public static final String TYPE_MYTHIC = "Mythic";

    public static final String ELEMENT_FIRE = "Fire";
    public static final String ELEMENT_WATER = "Water";
    public static final String ELEMENT_EARTH = "Earth";

    public static final int ATTACK_HEAL = -100;

    public Monster(String name, String monsterType, String element, int health, int attackPower, int defense, Attack attack1, Attack attack2, Attack attack3, int icon, int maxHealth) {
        this.name = name;
        this.health = health;
        this.monsterType = monsterType;
        this.element = element;
        this.attackPower = attackPower;
        this.defense = defense;
        attacks = new Attack[3];
        attacks[0] = attack1;
        attacks[1] = attack2;
        attacks[2] = attack3;
        this.icon = icon;
        effectOn = "";
        this.maxHealth = maxHealth;
    }

    public Monster(String name, int health, int attackPower, int defense, Attack attack1, Attack attack2, Attack attack3, int icon, int maxHealth)
    {
        this.name=name;
        this.health=health;
        this.attackPower=attackPower;
        this.defense=defense;
        attacks=new Attack[3];
        attacks[0]=attack1;
        attacks[1]=attack2;
        attacks[2]=attack3;
        this.icon=icon;
        effectOn="";
        this.maxHealth=maxHealth;

        //monster Type
        Random randomGenerator = new Random();

        int cardType = randomGenerator.nextInt(100) + 1;
        int cardElement = randomGenerator.nextInt(100) + 1;

        //check card type
        if( cardType <= 70)
            this.monsterType = TYPE_BASIC;
        else
        if (cardType <= 85)
        {
            this.monsterType = TYPE_RARE;
            this.health += (this.health * 5)/100;
            this.attackPower += (this.attackPower * 5)/100;
            this.defense += (this.defense * 5)/100;
        }
        else
        if (cardType <= 95)
        {
            this.monsterType = TYPE_LEGENDARY;
            this.health += (this.health * 7)/100;
            this.attackPower += (this.attackPower * 7)/100;
            this.defense += (this.defense * 7)/100;
        }
        else
        {
            this.monsterType = TYPE_MYTHIC;
            this.health += (this.health * 10)/100;
            this.attackPower += (this.attackPower * 10)/100;
            this.defense += (this.defense * 10)/100;
        }

        this.maxHealth = this.health;

        if ( cardElement <= 33 )
            this.element = ELEMENT_FIRE;
        else
        if ( cardElement <= 66 )
            this.element = ELEMENT_WATER;
        else
            this.element = ELEMENT_EARTH;
    }

    public String getName()
    {
        return name;
    }

    public Attack[] getAttacks() {
        return attacks;
    }

    public String getType()
    {
        return monsterType;
    }

    public String element()
    {
        return element;
    }

    public int health()
    {
        return health;
    }

    public int getAttackPower(String oppElement)
    {
        if (element.equals(ELEMENT_FIRE) && oppElement.equals(ELEMENT_EARTH))
            return attackPower + (attackPower * 2)/10;
        if (element.equals(ELEMENT_WATER) && oppElement.equals(ELEMENT_FIRE))
            return attackPower + (attackPower * 2)/10;
        if (element.equals(ELEMENT_EARTH) && oppElement.equals(ELEMENT_WATER))
            return attackPower + (attackPower * 2)/10;

        return attackPower;
    }

    public int getDefense()
    {
        return defense;
    }

    public int getMaxHealth() { return maxHealth; }

    public void setHealth(int newHealth) { health = newHealth; }

    public Monster copy()
    {
        return new Monster(name, monsterType,element,health,attackPower,defense,attacks[0],attacks[1], attacks[2], icon, maxHealth);
    }

    public int getIcon() {
        return icon;
    }

    public int attacked(Attack attack, Monster attacker, int multiAmount)
    {
        if(!attack.getEffect().equals(""))
        {
            int randomNumber = -1;
            if(attack.getEffect().equals("paralyze")) {
                double damage = (attacker.getAttackPower(element) * (attack.getAttackPower() / 100.0)) - defense;
                if(damage > 0) {
                    decHealth((int)damage);
                    effectOn = "paralyzed";
                }
            }

            if(attack.getEffect().equals("heal"))
            {
                attacker.heal(40);
                return ATTACK_HEAL;
            }

            if(attack.getEffect().equals("multi"))
            {
                if(multiAmount == -1)
                    randomNumber= new Random().nextInt(4)+1;
                else
                    randomNumber = multiAmount;

                double damage = (attacker.getAttackPower(element) * (attack.getAttackPower() / 100.0)) - defense;

                if(damage > 0) {
                    for (int i = 0; i <= randomNumber; i++) {
                        decHealth((int)damage);
                    }
                }

            }
            if(attack.getEffect().equals("healAttack"))
            {
                //heals the amount of damage done on the opponent
                double damage = (attacker.getAttackPower(element) * (attack.getAttackPower() / 100.0)) - defense;
                if(damage > 0) {
                    decHealth((int)damage);
                    attacker.heal((int) damage);
                }
            }

            if(attack.getEffect().equals("decreaseDefense"))
            {
                defense = defense - 20;
            }

            if(attack.getEffect().equals("decreaseAttack")) {
                attackPower = attackPower - 20;
            }

            if(attack.getEffect().equals("buffAttack")) {
                double damage = (attacker.getAttackPower(element) * (attack.getAttackPower() / 100.0)) - defense;

                if(damage > 0) {
                    decHealth((int) damage);
                }

                attacker.increaseAttack();
            }

            if(attack.getEffect().equals("buffDef")) {
                double damage = (attacker.getAttackPower(element) * (attack.getAttackPower() / 100.0)) - defense;

                if(damage > 0) {
                    decHealth((int) damage);
                }

                attacker.increaseDef();
            }

            return randomNumber;
        }
        else {
            double damage = (attacker.getAttackPower(element) * (attack.getAttackPower() / 100.0)) - defense;

            if(damage > 0)
                decHealth((int)damage);

            return -1;
        }

    }

    private void decHealth(int damage) {
        if( (health-damage) < 0)
            health = 0;
        else
            health -= damage;
    }

    public void heal(int amount)
    {
        if(maxHealth>=health+amount)
            health=health+amount;
        else
            health=maxHealth;
    }

    public int[] attackMonster(int attackIndex, Monster target, int multiAmount, int paralyzeRand)
    {
        if(!effectOn.equals("paralyzed"))
            return new int[] {target.attacked(attacks[attackIndex],this, multiAmount), -1};
        else
        {
            int temp;
            if(paralyzeRand == -1) {
                temp = new Random().nextInt(100);
            } else {
                temp = paralyzeRand;
            }

            if(temp <= 80)  //1 means paralyze effect does not affect the monster, an attack is allowed - 80% chance of attack when paralyzed
                return new int[] {target.attacked(attacks[attackIndex],this, multiAmount), temp};
            else
                return new int[] {-2, temp};
        }
    }

    public void increaseAttack()
    {
        attackPower+=15;
    }

    public void increaseDef()
    {
        defense+=15;
    }



    public static int getFrameIcon(String monsterType) {
        if(monsterType == null)
            return -1;

        if(monsterType.equals(TYPE_BASIC))
            return R.drawable.frame_basic;

        if(monsterType.equals(TYPE_RARE))
            return R.drawable.frame_rare;

        if(monsterType.equals(TYPE_LEGENDARY))
            return R.drawable.frame_legendary;

        if(monsterType.equals(TYPE_MYTHIC))
            return R.drawable.frame_mythic;

        return -1;
    }

    public static int getElementIcon(String element) {
        if(element == null)
            return -1;

        if(element.equals(ELEMENT_EARTH))
            return R.drawable.earth;

        if(element.equals(ELEMENT_FIRE))
            return R.drawable.fire;

        if(element.equals(ELEMENT_WATER))
            return R.drawable.water;

        return -1;
    }

    public String toString() {
        return name + " / " + monsterType;
    }

}