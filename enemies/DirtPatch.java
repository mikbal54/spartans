package com.spartans.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.engine.EffectManager;
import com.engine.ResourceGroup;
import com.engine.StaticEntity;
import com.spartans.levels.Gameplay;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class DirtPatch extends Enemy
{

    public enum State
    {
        Waiting,
        OnPatch,
        Done
    }

    State state;
    public StaticEntity entity;
    public boolean playerStuckOnThis, gavePoints;
    EffectManager effectManager;

    public DirtPatch(ResourceGroup resourceGroup, EffectManager effectManager, Gameplay currentGameplay, LevelInfo.Enemy info)
    {
        super(info);

        this.effectManager = effectManager;
        state = State.Waiting;
        TextureRegion region = resourceGroup.GetTextureRegion("data/urban_green/urban_green.sprites", "/dirt_patch");
        if (region == null)
            region = resourceGroup.defaultRegion;
        entity = new StaticEntity(region, info.widthMeters, info.heightMeters);
        entity.SetTransform(info);
        currentGameplay.Add(entity, info.zIndex);
    }

    public void Restart()
    {
        super.Restart();
        gavePoints = false;
        gavePoints = false;
        state = State.Waiting;
    }

    public void GivePoints(Player player)
    {
        gavePoints = true;
        player.AddPoints(100);
    }

    public boolean IsUnderPlayer(Player player)
    {
        if ((player.x + (player.widthMeters * 0.5f)) > entity.x)
        {
            if (player.x < (entity.x + entity.widthMeters))
                return true;
        }

        return false;
    }

    public boolean PlayerPassedDirt(Player player)
    {
        if (player.x > (entity.x + entity.widthMeters))
            return true;
        return false;
    }

    @Override
    public void Update(float timePassed, Player player)
    {
        switch (state)
        {
            case Done:
                break;
            case OnPatch:
                if (!IsUnderPlayer(player))
                {
                    state = State.Done;
                    player.ExitDirtPatch();
                }
                break;
            case Waiting:
                if (player.state == Player.State.Running || player.state == Player.State.Sliding)
                {
                    if (IsUnderPlayer(player))
                    {
                        player.EnteredDirtPatch();
                        state = State.OnPatch;
                        playerStuckOnThis = true;
                    }
                }

                break;
        }

        if (!gavePoints && !playerStuckOnThis)
        {
            if (PlayerPassedDirt(player))
            {
                GivePoints(player);
            }
        }
    }
}
