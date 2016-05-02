package com.jonashr.monsters;

import java.util.ArrayList;

/**
 * Created by Mathiashr on 11-11-2015.
 */
public class HardcodedAttacks
{
    private ArrayList<Attack> attacks;

    public HardcodedAttacks()
    {
        attacks= new ArrayList<Attack>();
        int i=1;
        attacks.add(new Attack("paralyzing fist", 80, "paralyze",i)); // 1
        i++;
        attacks.add(new Attack("punch", 90,null,i)); // 2
        i++;
        attacks.add(new Attack("heal", 0, "heal",i)); // 3
        i++;
        attacks.add(new Attack("drain energy", 100, "healAttack",i)); // 4
        i++;
        attacks.add(new Attack("dragon breath", 100, null,i)); // 5
        i++;
        attacks.add(new Attack("fury slash", 60, "multi",i)); // 6
        i++;
        attacks.add(new Attack("force choke", 80, "paralyze",i)); // 7
        i++;
        attacks.add(new Attack("psychic", 100, null,i)); // 8
        i++;
        attacks.add(new Attack("magic impulse", 100,null,i)); // 9
        i++;
        attacks.add(new Attack("ultra beam", 120, null, i)); // 10
        i++;
        attacks.add(new Attack("defense ball", 80, "buffDef",i)); // 11
        i++;
        attacks.add(new Attack("flex", 80, "buffAttack",i)); // 12
        i++;
        attacks.add(new Attack("afterimage", 0, "decreaseDef",i)); // 13
        i++;
        attacks.add(new Attack("intimidate", 0 , "decreaseAttack",i)); // 14
        i++;
        attacks.add(new Attack("warriors armor", 0, "buffDef",i)); // 15
        i++;
        attacks.add(new Attack("warriors sword", 80, "buffAttack",i)); // 16
        i++;
        attacks.add(new Attack("dragons skin", 80, "buffDef",i)); // 17
        i++;
        attacks.add(new Attack("dragon roar", 80, "buffAttack",i)); // 18
    }

    public Attack getAttack(int id)
    {
        return attacks.get(id-1);
    }


}