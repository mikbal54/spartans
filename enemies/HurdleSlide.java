package com.spartans.enemies;


import com.engine.EffectManager;
import com.engine.ResourceGroup;
import com.engine.StaticEntity;
import com.spartans.levels.Gameplay;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class HurdleSlide extends Enemy
{

    enum State
    {
        Waiting,
        Done
    }

    State state;
    EffectManager effectManager;
    StaticEntity entity;
    boolean playerWasSlidingUnder;

    public HurdleSlide(EffectManager effectManager, ResourceGroup resourceGroup, Gameplay currentGameplay, LevelInfo.Enemy enemyInfo)
    {
        super(enemyInfo);
        playerWasSlidingUnder = false;
        state = State.Waiting;
        this.effectManager = effectManager;

        entity = new StaticEntity(resourceGroup.GetTextureRegion("data/urban_green/urban_green.atlas", "hurdle_slide"), enemyInfo.widthMeters, enemyInfo.heightMeters);
        entity.SetTransform(enemyInfo);
        currentGameplay.Add(entity, enemyInfo.zIndex);
    }

    public void Restart()
    {
        super.Restart();
        state = State.Waiting;
        playerWasSlidingUnder = false;
    }

    public float DistanceToPlayer(Player player)
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
            case Done:
                break;
            case Waiting:
                if (DistanceToPlayer(player) < (entity.widthMeters * 0.5f))
                {
                    if (player.state != Player.State.Sliding)
                    {
                        if (!playerWasSlidingUnder)
                        {
                            player.GetHit();
                            state = State.Done;
                        }
                    }
                    else
                        playerWasSlidingUnder = true;
                }
                else
                {
                    // oyuncu engelin saginda
                    if (player.x > entity.x)
                    {
                        player.AddPoints(100);
                        state = State.Done;
                    }
                }
                break;

        }
    }
}
