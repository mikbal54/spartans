package com.spartans.collectibles;

import com.badlogic.gdx.audio.Sound;
import com.engine.*;
import com.spartans.levels.Gameplay;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class SpeedBoost implements Collectible
{
    public StaticEntity entity;

    public enum State
    {
        Waiting,
        Shrinking,
        Done
    }

    public State state;
    public float startX, startY, startWidth, startHeight;

    public EffectManager effectManager;

    Sound collectSound;

    public static float feverGained = 0.4f;

    public SpeedBoost(ResourceGroup resourceGroup, EffectManager effectManager, Gameplay currentGameplay, LevelInfo.Collectible info)
    {
        this.effectManager = effectManager;

        entity = new StaticEntity(resourceGroup.GetTextureRegion("data/common/common.atlas", "boost"), info.widthMeters, info.heightMeters);
        entity.SetTransform(info);
        entity.originX = entity.width / 2;
        entity.originY = entity.height / 2;
        currentGameplay.Add(entity, info.zIndex);

        collectSound = resourceGroup.GetSound("data/sounds/common/boost.wav");
        state = State.Waiting;

        startX = info.x;
        startY = info.y;
        startWidth = info.widthMeters;
        startHeight = info.heightMeters;

    }

    public void Restart()
    {

        entity.x = startX;
        entity.y = startY;
        entity.widthMeters = startWidth;
        entity.heightMeters = startHeight;

        state = State.Waiting;
        entity.isVisible = true;
    }

    boolean CheckCollision(Player player)
    {
        return Utility.RectIntersect(player.x, player.y, player.GetCurrentWidth(), player.GetCurrentHeight(), entity.x, entity.y, .4f, .5f);
    }

    @Override
    public void Update(float timePassed, Player player)
    {
        switch (state)
        {
            case Waiting:
                if (CheckCollision(player))
                {
                    player.AddFeverTime(feverGained);
                    state = State.Shrinking;
                    collectSound.play();
                    //bubbleEffect.particleEffect.allowCompletion();
                }
                if ((player.x - entity.x) > 2)
                    state = State.Done;
                break;
            case Shrinking:
                entity.widthMeters -= timePassed * 2;
                entity.heightMeters -= timePassed * 2;
                if (entity.widthMeters < 0.05f)
                {
                    state = State.Done;
                    entity.isVisible = false;
                }
                break;
            case Done:
                break;
        }
    }

}
