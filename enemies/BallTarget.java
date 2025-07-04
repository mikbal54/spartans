package com.spartans.enemies;

import com.spartans.player.Player;

public interface BallTarget
{
    public boolean IsUnder(float x, float y, float radius);

    public void BallLaunched();

    public void Hit();

    public float BallTargetX();

    public float BallTargetY();
    
    public float GetVelocityX();
    
    public float GetVelocityY();

    void Update(float timePassed, Player player);

    public enum TypeOfEnemies
    {
        EnemyHawk, TeamMate, EnemySliding, EnemyHeadButting
    }

    public TypeOfEnemies GetType();
}
