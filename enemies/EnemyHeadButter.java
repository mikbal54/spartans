package com.spartans.enemies;


import com.engine.*;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class EnemyHeadButter extends Enemy implements BallTarget
{
    public SkeletonEntity entity;

    // player a ne kadar yaklastiginda kaymaya basliyacagi
    float attackDistance;
    // ne kader hizli kaydigi
    float jumpingSpeed;

    float timeSinceJumpStarted;

    float jumpDuration;

    float groundHeight;

    boolean playerPassedUnder;

    boolean ballComingTowards;

    enum State
    {
        Inactive,
        Waiting,
        Jumping, GoingDown
    }

    TypeOfEnemies typeOfEnemies;

    State state;

    EffectManager effectManager;

    public EnemyHeadButter(EffectManager effectManager, SkeletonRenderer renderer, ResourceGroup resourceGroup, LevelInfo.Enemy enemyInfo, float groundHeight)
    {
        super(enemyInfo);

        this.groundHeight = groundHeight;
        this.effectManager = effectManager;
        state = State.Waiting;

        attackDistance = 8;
        jumpingSpeed = 2;

        jumpDuration = 1;

        entity = new SkeletonEntity(renderer, resourceGroup.GetSkeletonData("data/headbutter/headbutter.json"));
        entity.SetTransform(enemyInfo);
        entity.width = enemyInfo.width;
        entity.height = enemyInfo.height;

        entity.AdjustSize();
        entity.SetCurrentAnimation("idle");

        typeOfEnemies = TypeOfEnemies.EnemySliding;
    }

    public void Destroy()
    {
        // TODO: destroy skeletonEntity
        entity = null;
    }

    public void EndJump()
    {
        entity.SetCurrentAnimation("jumpend");
        entity.isLooping = false;
        state = State.GoingDown;
    }

    @Override
    public void Hit()
    {
        if (state == State.Waiting)
        {
            entity.SetCurrentAnimation("hit");
            entity.isLooping = false;
            entity.AddAnimationToQueue("idle", true);
        }
    }

    @Override
    public boolean IsUnder(float x, float y, float radius)
    {
        if (!isActive || state == State.Jumping)
            return false;

        float x2 = x - entity.x;
        x2 *= x2;
        float y2 = y - entity.y;
        y2 *= y2;

        if (Math.sqrt(x2 + y2) < radius)
            return true;

        return false;
    }

    @Override
    public void BallLaunched()
    {
        ballComingTowards = true;
    }

    void Jump()
    {
        state = State.Jumping;
        entity.SetCurrentAnimation("jumpstart");
        entity.isLooping = false;
        entity.AddAnimationToQueue("jumploop", true);
    }

    public void Restart()
    {
        super.Restart();
        ballComingTowards = false;
        playerPassedUnder = false;
        entity.SetCurrentAnimation("idle");
        entity.isLooping = true;
        entity.ClearAnimationQueue();
        state = State.Waiting;
        entity.SetTransform(enemyInfo);
        timeSinceJumpStarted = 0f;
    }

    public boolean IsPlayerUnder(Player player)
    {
        return (Math.abs(entity.x - 1 - player.x) < 1);
    }


    @Override
    public float BallTargetX()
    {
        return entity.x + (entity.widthMeters / 2) + 1;
    }

    @Override
    public float BallTargetY()
    {
        return entity.y + (entity.heightMeters / 2);
    }

    @Override
    public void Update(float timePassed, Player player)
    {
        switch (state)
        {
            case Inactive:
                entity.x -= jumpingSpeed * timePassed;
                break;
            case Jumping:
                timeSinceJumpStarted += timePassed;
                entity.x -= jumpingSpeed * timePassed;

                if (player.state == Player.State.Sliding) // oyuncu kayiyorsa vuramaz
                {
                    if (IsPlayerUnder(player))
                    {
                        playerPassedUnder = true;
                    }
                } // oyuncu yukseliyor yada alcaliyor
                else
                {
                    if (Math.abs(entity.x - 1 - player.x) < 1) //state != State.Inactive)
                    {
                        player.GetHit();
                        state = State.Inactive;
                    }
                }

                if (timeSinceJumpStarted > jumpDuration)
                {
                    if (playerPassedUnder)
                    {
                        if (!gavePoints)
                        {
                            player.AddPoints(pointsGained);
                            gavePoints = true;
                        }
                    }
                    EndJump();
                }
                break;
            case Waiting:
                if ((entity.x - player.x) < attackDistance)
                    Jump();
                break;
            case GoingDown:
                entity.x -= jumpingSpeed * timePassed;
                if (player.x - entity.x > 10) // oyuncudan 10 den fazla uzaktaysa inaktif yap
                {
                    state = State.Inactive;
                    isActive = false; // bu updatin bi daha cagirilmayacagini soler
                }
                break;
            default:
                break;

        }
    }

    @Override
    public TypeOfEnemies GetType()
    {
        return typeOfEnemies;
    }

    @Override
    public float GetVelocityX()
    {
        return jumpingSpeed;
    }

    @Override
    public float GetVelocityY()
    {
        return 0;
    }

}
