package com.spartans.enemies;

import com.engine.ResourceGroup;
import com.engine.SkeletonEntity;
import com.engine.Utility;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.spartans.ball.Ball;
import com.spartans.player.Player;

public class GoalKeeper extends Enemy
{

    public SkeletonEntity entity;

    public enum State
    {
        Waiting,
        JumpingUp, // yukari dogru atliyor, topu tutmak icin filan
        JumpingDown
    }

    public State state;

    public float slideDuration;
    public float slidingFor;
    public float jumpDistance;
    public float jumpSpeed;

    float initialX, initialY;

    public GoalKeeper(float posX, float posY, SkeletonRenderer renderer, ResourceGroup resourceGroup)
    {
        super(null);

        initialX = posX;
        initialY = posY;
        state = State.Waiting;

        slideDuration = 1;
        slidingFor = 0f;
        jumpSpeed = 5;
        jumpDistance = 5;

        entity = new SkeletonEntity(renderer, resourceGroup.GetSkeletonData("data/goalkeeper/goalkeeper.json"));
        entity.width = 64;
        entity.height = 256;
        entity.widthMeters = 0.5f;
        entity.heightMeters = 2.0f;
        entity.y = posY;
        entity.x = posX;

        entity.AdjustSize();
        entity.SetCurrentAnimation("idle");

    }

    public void Restart()
    {
        slidingFor = 0f;
        entity.SetCurrentAnimation("idle");
        state = State.Waiting;
        entity.x = initialX;
        entity.y = initialY;
    }


    public void JumpUp()
    {
        entity.ClearAnimationQueue();
        entity.SetCurrentAnimation("jump_up");
        entity.isLooping = false;
    }


    public void JumpDown()
    {
        state = State.JumpingDown;
        entity.SetCurrentAnimation("jumpdown");
        entity.isLooping = false;
        entity.AddAnimationToQueue("jumploop", true);
    }

    void Destroy()
    {
        entity = null;
    }

    void CheckBallCollision(Player player)
    {
        if (player.ball.GetY() < 1.5f)
        {
            if (Math.abs((entity.x - 1) - (player.ball.GetX())) < 1)
            {
                player.ball.SetState(Ball.State.Stray);
                player.ball.velX -= 5;
                player.ball.velY = (float) Utility.RandomDouble(3, 10);
                player.GetHit();
                player.ball.isFailed = true;
            }
        }
    }

    @Override
    public void Update(float timePassed, Player player)
    {
        switch (state)
        {
            case Waiting:
                break;
            case JumpingDown:
                entity.x -= jumpSpeed * timePassed;
                if (!player.ball.isFailed)
                    CheckBallCollision(player);
                slidingFor += timePassed;
                if (slidingFor > slideDuration)
                {
                    slidingFor = 0;
                    state = State.Waiting;
                }
                break;
            case JumpingUp:
                break;
            default:
                break;
        }

    }

}
