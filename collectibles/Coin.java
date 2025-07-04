package com.spartans.collectibles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.engine.*;
import com.spartans.levels.Gameplay;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class Coin implements Collectible
{

    public enum State
    {
        Waiting,
        Shrinking,
        Done
    }

    public enum AnimationState
    {
        GoingUp,
        GoingDown,
        None
    }

    EffectManager effectManager;
    EffectParticle collectEffect;
    Sound coinSound;
    StaticEntity entity;
    float maxUpDistance;
    float animationSpeed;
    float startX, startY;
    float startWidth, startHeight;
    State state;
    AnimationState animationState;
    Player player;

    public Coin(ResourceGroup resourceGroup, Player player, Gameplay gameplay, EffectManager effectManager, LevelInfo.Collectible info)
    {
        this.effectManager = effectManager;
        this.player = player;
        maxUpDistance = 0.15f;
        animationSpeed = (float) Utility.RandomDouble(0.1, 0.2);
        state = State.Waiting;
        animationState = AnimationState.GoingUp;
        coinSound = resourceGroup.GetSound("data/sounds/common/coin_pickup.wav");
        entity = new StaticEntity(resourceGroup.GetTextureRegion("data/common/common.atlas", "coin"), info.widthMeters, info.heightMeters);
        entity.SetTransform(info);
        entity.originX = entity.width / 2;
        entity.originY = entity.height / 2;
        startX = info.x;
        startY = info.y;
        startWidth = info.widthMeters;
        startHeight = info.heightMeters;
        gameplay.Add(entity, info.zIndex);

        collectEffect = (EffectParticle) effectManager.NewParticleEffect("data/effects/coin_collect.p", null);
        collectEffect.SetPos(entity.x, entity.y);
        collectEffect.lifeTime = 3;
        collectEffect.isActive = false;
        collectEffect.isDrawable = false;
        collectEffect.isRemovedWhenComplete = false;
    }

    @Override
    public void Update(float timePassed, Player player)
    {
        switch (state)
        {
            case Waiting:
                if (Utility.RectIntersect(player.x, player.y, player.widthMeters, player.heightMeters, entity.x, entity.y, entity.widthMeters, entity.heightMeters))
                {
                    state = State.Shrinking;
                    collectEffect.isActive = true;
                    collectEffect.isDrawable = true;
                    coinSound.play();
                    player.AddToCurrentCoints(1);
                }
                break;
            case Shrinking:
                entity.widthMeters -= timePassed * 2;
                entity.heightMeters -= timePassed * 2;
                if (entity.widthMeters < 0.05)
                {
                    state = State.Done;
                    entity.isVisible = false;
                }
                break;
            case Done:
                break;
        }

        switch (animationState)
        {
            case GoingUp:
                entity.y += timePassed * animationSpeed;
                if(entity.y > startY + maxUpDistance)
                    animationState = AnimationState.GoingDown;
                break;
            case GoingDown:
                entity.y -= timePassed * animationSpeed;
                if(entity.y < startY)
                    animationState = AnimationState.GoingUp;
                break;
            case None:
                break;
        }
    }

    @Override
    public void Restart()
    {
        entity.isVisible = true;
        animationState = AnimationState.GoingUp;
        state = State.Waiting;
        entity.x = startX;
        entity.y = startY;
        entity.widthMeters = startWidth;
        entity.heightMeters = startHeight;
        collectEffect.particleEffect.reset();
        collectEffect.lifeTime = 3;
        collectEffect.isActive = false;
        collectEffect.isDrawable = false;
    }
}
