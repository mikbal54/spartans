package com.spartans.ball;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.math.Vector2;
import com.engine.*;
import com.spartans.enemies.BallTarget;
import com.spartans.levels.Gameplay;
import com.spartans.player.Player;

public class Ball
{
    public AnimatedEntity animatedEntity;

    public enum State
    {
        IdleForward,
        IdleBackward,
        UpAirPlayerFoot,
        GoingBackToFoot,
        GoingToTarget,
        GoingToGoal,
        Stray, // top oyuncudan uzakta. mesela kaleci topa kaydigi zaman
        Nothing
    }

    public State state;

    public boolean isFailed;
    public float ballShotSpeed;
    public float goalLine, goalHeight;

    public BallTarget currentTarget;

    EffectParticle trail;
    boolean isBallTrailDrawing;
    public float distanceToPlayer;
    float dribbleSpeed;

    // hiz ve yer cekimi eger Stray state te ise yarar
    public float velX, velY, xFriction, yFriction;
    float targetX, targetY;
    public float speedToTarget;
    public boolean arrivedToTarget;

    TweenManager tweenManager; // topa animasyon vermek icin gerekli


    public Ball(float startX, float startY, float shotSpeed, float goalLine, float goalHeight, ResourceGroup resourceGroup, TweenManager tweenManager, Gameplay currentGameplay)
    {
        xFriction = 1;
        yFriction = 3;
        dribbleSpeed = 3f;
        isBallTrailDrawing = false;
        this.goalLine = goalLine;
        this.goalHeight = goalHeight;
        state = State.IdleForward;
        ballShotSpeed = shotSpeed;
        this.tweenManager = tweenManager;
        // TODO: cache animated entity infos
        AnimatedEntityInfo info = resourceGroup.GetAnimatedEntityInfo("data/common/ball.anim");
        animatedEntity = new AnimatedEntity(resourceGroup.GetAtlas("data/common/common.atlas"), resourceGroup.defaultRegion, info, 64, 64, 0.5f, 0.5f);

        currentGameplay.Add(animatedEntity, 1);

        animatedEntity.x = startX;
        animatedEntity.y = startY;
        speedToTarget = 2;
    }

    public void GoalHappenned()
    {
        CompleteTrail();
    }

    public void SetState(State newState)
    {
        if (state == State.IdleForward || state == State.IdleBackward)
        {
            distanceToPlayer = 0;
        }
        else if (state == State.UpAirPlayerFoot)
        {
            distanceToPlayer = 0;
        }

        state = newState;
    }

    public void SetTarget(float x, float y, float speed)
    {
        targetX = x;
        targetY = y;
        speedToTarget = speed;
        arrivedToTarget = false;
    }

    public void EnableTrail()
    {
        isBallTrailDrawing = true;
        trail.isDrawable = true;
        trail.particleEffect.start();
    }

    public void CompleteTrail()
    {
        trail.particleEffect.allowCompletion();
    }

    public void CreateTrail(EffectManager effectManager)
    {
        trail = (EffectParticle) effectManager.NewParticleEffect("data/effects/trail_red.p", null);
        trail.lifeTime = -1;
        trail.SetDrawType(Effect.DrawType.GameWorld);
        trail.SetPos(animatedEntity.x, animatedEntity.y);
        trail.isDrawable = false;
    }

    public void GoToTarget(float timePassed)
    {
        if (arrivedToTarget)
            return;

        Vector2 distance = Engine.vector2Pool.obtain(targetX - (animatedEntity.x + animatedEntity.widthMeters / 2), targetY - (animatedEntity.y + animatedEntity.heightMeters / 2));

        float len = distance.len();

        float resolution = speedToTarget * timePassed;

        if (len <= resolution)
        {
            arrivedToTarget = true;
            return;
        }

        distance.x /= len;
        distance.y /= len;

        distance.nor();
        distance.x *= speedToTarget;
        distance.y *= speedToTarget;

        animatedEntity.x += distance.x * timePassed;
        animatedEntity.y += distance.y * timePassed;

        Engine.vector2Pool.free(distance);
    }

    public void Restart(float startX, float startY)
    {
        CompleteTrail();
        isFailed = false;
        velX = 0;
        velY = 0;
        targetX = 0;
        targetY = 0;
        speedToTarget = 0;
        arrivedToTarget = false;
        isBallTrailDrawing = false;
        trail.isDrawable = true;
        state = State.IdleForward;
        animatedEntity.x = startX;
        animatedEntity.y = startY;
        distanceToPlayer = 0f;
    }

    void ApplyGravity(float timePassed, float groundHeight)
    {
        if (animatedEntity.y <= groundHeight)
            return;

        velY += -9.8f * timePassed;
    }


    void ApplyFriction(float timePassed)
    {
        if (velX > 0)
            velX -= xFriction * timePassed;
        else
            velX += xFriction * timePassed;
        if (Math.abs(velX) < xFriction * timePassed)
            velX = 0;


        if (velY > 0)
            velY -= yFriction * timePassed;
        else
            velY += yFriction * timePassed;
        if (Math.abs(velY) < yFriction * timePassed)
            velY = 0;
    }

    void ApplyVelocity(float timePassed)
    {
        if (velX != 0)
            animatedEntity.x += velX * timePassed;
        if (velY != 0)
            animatedEntity.y += velY * timePassed;
    }

    void CheckCollision(float timePassed, float groundHeight)
    {
        // yer ile capismayi kontrol et
        if (animatedEntity.y < groundHeight)
        {
            animatedEntity.y = groundHeight;
            if (velY < 0)
                velY *= -0.6f;
        }

        // kale ile carpismayi kontrol et
        if (animatedEntity.x > goalLine)
        {
            if ((animatedEntity.y + animatedEntity.heightMeters) > goalHeight)
            {
                animatedEntity.y = goalHeight - animatedEntity.heightMeters;
                velY *= -1;
            }

            if ((animatedEntity.x + animatedEntity.widthMeters) > (goalLine + 2))
            {
                animatedEntity.x = goalLine + 2 - animatedEntity.widthMeters;
                velX *= -0.4f;
            }
        }
    }

    public void Update(float timePassed, Player player, float groundHeight)
    {
        switch (state)
        {
            case Stray:
                ApplyGravity(timePassed, groundHeight);
                ApplyVelocity(timePassed);
                ApplyFriction(timePassed);
                CheckCollision(timePassed, groundHeight);
                break;
            case IdleForward:
                if (distanceToPlayer < 0.5f)
                    distanceToPlayer += timePassed * player.GetRunSpeed() * 0.5f;
                else if (distanceToPlayer < 0.8f)
                    distanceToPlayer += timePassed * player.GetRunSpeed() * 0.3f;
                else if (distanceToPlayer < 1.2f)
                    distanceToPlayer += timePassed * player.GetRunSpeed() * 0.2f;
                else if (distanceToPlayer > 1.2f)
                    state = State.IdleBackward;
                animatedEntity.x = player.x + distanceToPlayer + player.entity.widthMeters / 2;
                animatedEntity.y = player.y;
                break;
            case GoingBackToFoot:
                SetTarget(player.x + distanceToPlayer + player.entity.widthMeters, player.y, player.GetRunSpeed() + 3);
                GoToTarget(timePassed);
                if (arrivedToTarget)
                {
                    SetState(State.IdleForward);
                }
                break;
            case IdleBackward:
                distanceToPlayer -= timePassed * player.GetRunSpeed() * 0.3f;
                if (distanceToPlayer < 0)
                    state = State.IdleForward;
                animatedEntity.x = player.x + distanceToPlayer + player.entity.widthMeters / 2;
                animatedEntity.y = player.y;
                break;
            case GoingToTarget:
                GoToTarget(timePassed);
                if (arrivedToTarget && currentTarget != null)
                {
                    currentTarget.Hit();
                    state = State.GoingBackToFoot;
                    CompleteTrail();
                }
                break;
            case GoingToGoal:
                GoToTarget(timePassed);
                if (animatedEntity.x > goalLine)
                {
                    SetState(State.Stray);
                    velX += 10;
                    player.EndGameGoal();
                    player.state = Player.State.EndLevelHappy;
                    player.entity.ClearAnimationQueue();
                    player.entity.SetCurrentAnimation("Goal");
                    player.entity.isLooping = true;
                    GoalHappenned();
                }
                break;
            case UpAirPlayerFoot:
                SetTarget(player.x + distanceToPlayer + player.entity.widthMeters + 0.5f, player.y + player.heightMeters / 2, 10);
                GoToTarget(timePassed);
                break;
            default:
                break;
        }

        if (isBallTrailDrawing)
            trail.SetPos(animatedEntity.x + animatedEntity.widthMeters / 2, animatedEntity.y + animatedEntity.heightMeters / 2);

    }

    public float GetX()
    {
        return animatedEntity.x;
    }

    public float GetY()
    {
        return animatedEntity.y;
    }

}
