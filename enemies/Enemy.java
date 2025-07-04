package com.spartans.enemies;

import com.engine.Transform;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public abstract class Enemy
{

    // update cagirilip cagirilmayacagini anlamak icin
    public boolean isActive;

    // bu dusman puanini verdi
    public boolean gavePoints;

    public int pointsGained;
    public int pointsLost;

    public boolean freezeState;

    LevelInfo.Enemy enemyInfo;

    protected Enemy(LevelInfo.Enemy enemyInfo)
    {
        freezeState = false;
        this.enemyInfo = enemyInfo;

        if (enemyInfo != null)
        {
            pointsGained = enemyInfo.pointsGained;
        }
        gavePoints = false;
        isActive = true;
    }

    public abstract void Update(float timePassed, Player player);

    public void Restart()
    {
        if (enemyInfo != null)
        {
            pointsGained = enemyInfo.pointsGained;
        }
        freezeState = false;
        gavePoints = false;
        isActive = true;
    }
}
