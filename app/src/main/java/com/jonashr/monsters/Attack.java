package com.jonashr.monsters;

import java.io.Serializable;

/**
 * Created by Mathiashr on 06-11-2015.
 */
public class Attack implements Serializable
{
    private String name;
    private int attackPower;
    private String effect;
    private int id;

    public Attack(String name, int attackPower, String effect, int id)
    {
        this.name=name;
        this.attackPower=attackPower;
        if(effect == null)
            this.effect = "";
        else
            this.effect=effect;
        this.id=id;
    }

    public String getName()
    {
        return name;
    }

    public int getAttackPower()
    {
        return attackPower;
    }

    public String getEffect()
    {
        return effect;
    }

    public int getId()
    {
        return id;
    }

}

