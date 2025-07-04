package com.spartans.enemies;

import com.engine.*;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.spartans.ball.Ball;
import com.spartans.levels.LevelInfo;
import com.spartans.player.Player;

public class EnemySliding extends Enemy implements BallTarget
{
    public SkeletonEntity entity;

    // player a ne kadar yaklastiginda kaymaya basliyacagi
    float attackDistance;
    // oyuncu ne mesafede kosmaya baslamasi
    float runTowardsDistance;
    // ne kader hizli kaydigi
    float slidingSpeed;
    // ne kadar hizli oyuncuya kosacagi
    float runSpeed;

    //Topla birlikte kayma uzakligi
    float kacisUzakligi = 7;

    boolean ballComingTowards;

    enum State
    {
        Inactive, RunningToPlayer, Waiting, Sliding, Kidnapping
    }

    TypeOfEnemies typeOfEnemies;

    State state;

    EffectManager effectManager;

    public EnemySliding(SkeletonRenderer renderer, ResourceGroup resourceGroup, EffectManager effectManager, LevelInfo.Enemy enemyInfo, float groundHeight)
    {
        super(enemyInfo);

        state = State.Waiting;

        ballComingTowards = false;
        runTowardsDistance = 15;
        attackDistance = 5;
        slidingSpeed = 2;
        runSpeed = 4;

        this.effectManager = effectManager;

        entity = new SkeletonEntity(renderer, resourceGroup.GetSkeletonData("data/sliding/sliding.json"));
        entity.SetTransform(enemyInfo);
        entity.width = enemyInfo.width;
        entity.height = enemyInfo.height;
        entity.y = groundHeight;

        entity.AdjustSize();
        entity.SetCurrentAnimation("idle");

        typeOfEnemies = TypeOfEnemies.EnemySliding;

    }

    public void Destroy()
    {
        // TODO: destroy skeletonEntity
        entity = null;
    }

    @Override
    public void Hit()
    {
        // burda sliding ise vuramamasi lazim
        if (state == State.RunningToPlayer || state == State.Waiting)
        {
            state = State.Inactive;

            entity.SetCurrentAnimation("gothit");
            entity.isLooping = false;
            entity.AddAnimationToQueue("hurtloop", true);

            isActive = false;

            EffectParticle ballhitEffect = (EffectParticle) effectManager.NewParticleEffect("data/effects/yildizlar.p", null);
            ballhitEffect.lifeTime = 1;
            ballhitEffect.SetPos(entity.x, entity.y + 1);
        }
    }

    @Override
    public boolean IsUnder(float x, float y, float radius)
    {
        if (!isActive || state == State.Sliding)
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

    void Slide()
    {
        if (freezeState)
            return;

        state = State.Sliding;
        entity.SetCurrentAnimation("slide");
        entity.isLooping = false;
        entity.AddAnimationToQueue("slideloop", true);
    }

    void StartRunning(Player player)
    {
        if (freezeState)
            return;
        state = State.RunningToPlayer;
        entity.SetCurrentAnimation("runstart");
        entity.isLooping = false;
        entity.AddAnimationToQueue("runloop", true);
    }

    @Override
    public float BallTargetX()
    {
        return entity.x + (entity.widthMeters / 2);
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
                entity.x -= slidingSpeed * timePassed;
                break;
            case RunningToPlayer:
                entity.x -= runSpeed * timePassed;
                if (!ballComingTowards && (entity.x - player.x) < attackDistance)
                    Slide();
                break;
            case Sliding:
                entity.x -= slidingSpeed * timePassed;
                if (player.state != Player.State.Ascending && player.state != Player.State.Decending) // oyuncu yukseliyorsa vuramaz // oyuncu alcaliyorsa vuramaz
                {
                    if (entity.x < (player.x + player.widthMeters))
                    {
                        player.GetHit();
                        state = State.Inactive;
                        player.ball.velX = -slidingSpeed * (float) Utility.RandomDouble(2, 4);
                        player.ball.velY = (float) Utility.RandomDouble(3, 7);
                        player.ball.state = Ball.State.Stray;
                    }
                } // oyuncu yukseliyor yada alcaliyor
                else
                {
                    if (state != State.Inactive)
                    {
                        float diff = player.x - entity.x;
                        if (diff > -0.2 && diff < 0.2) // 20 cm ile -20cm arasinda uzakliktaysa oyuncuya
                        {
                            int percentage = player.GetJumpHeightPercentage();
                            if (percentage > 70)
                            {
                                player.AddPoints(pointsGained);
                            }
                            else if (percentage > 40)
                            {
                                player.AddPoints((int) (pointsGained * 0.5f));
                            }
                            else
                            {
                                player.AddPoints((int) (pointsGained * 0.25f));
                            }

                            state = State.Inactive;
                        }
                    }
                }
                break;
            case Waiting:
                if (!ballComingTowards && (entity.x - player.x) < runTowardsDistance)
                    StartRunning(player);
                break;
            case Kidnapping:
                player.ball.state = Ball.State.Nothing;
                entity.x -= slidingSpeed * timePassed;
                player.ball.animatedEntity.x = entity.x;
                player.ball.animatedEntity.y = entity.y;
                if (entity.x < player.x - kacisUzakligi)
                {
                    player.GetHit();
                    state = State.Inactive;
                    break;
                }
            default:
                break;

        }
    }

    public void Restart()
    {
        super.Restart();
        entity.SetCurrentAnimation("idle");
        entity.isLooping = true;
        ballComingTowards = false;
        entity.currentAnimationTime = 0f;

        state = State.Waiting;
        entity.SetTransform(enemyInfo);
    }

    @Override
    public TypeOfEnemies GetType()
    {
        return typeOfEnemies;
    }

    @Override
    public float GetVelocityX()
    {

        if (state == State.RunningToPlayer)
            return runSpeed;
        else if (state == State.Sliding)
            return slidingSpeed;

        return 0;
    }

    @Override
    public float GetVelocityY()
    {
        return 0;
    }

}
