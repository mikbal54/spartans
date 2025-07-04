package com.spartans.enemies;

import com.engine.EffectManager;
import com.engine.ResourceGroup;
import com.engine.StaticEntity;
import com.spartans.levels.Gameplay;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class HurdleJump extends Enemy
{

    enum State
    {
        Waiting,
        Done
    }

    State state;
    StaticEntity entity;
    EffectManager effectManager;
    int pointsGained;

    public HurdleJump(EffectManager effectManager, Gameplay currentGameplay, ResourceGroup resourceGroup, LevelInfo.Enemy enemyInfo)
    {
        super(enemyInfo);

        pointsGained = enemyInfo.pointsGained;
        state = State.Waiting;
        this.effectManager = effectManager;

        entity = new StaticEntity(resourceGroup.GetTextureRegion("data/urban_green/urban_green.atlas", "hurdle_jump"), enemyInfo.widthMeters, enemyInfo.heightMeters);
        entity.SetTransform(enemyInfo);

        currentGameplay.Add(entity, enemyInfo.zIndex);
    }

    public void Restart()
    {
        state = State.Waiting;
    }

    float GetDistanceToPlayer(Player player)
    {
        if (player.x > entity.x)
            return player.x - entity.x;
        else
            return entity.x - player.x;
    }

    @Override
    public void Update(float timePassed, Player player)
    {
        switch (state)
        {
            case Waiting:
                if (GetDistanceToPlayer(player) < (entity.widthMeters * 0.5))
                {
                    if (player.state == Player.State.Running || player.state == Player.State.Sliding)
                    {
                        player.GetHit();
                        state = State.Done;
                    }
                }
                else
                {
                    // oyuncu engeling saginda
                    if (player.x > entity.x)
                    {
                        player.AddPoints(pointsGained);
                        state = State.Done;
                    }
                }
                break;
            case Done:
                break;
        }

    }
}
