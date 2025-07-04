package com.spartans.player;

import com.badlogic.gdx.math.Vector3;
import com.engine.Engine;
import com.engine.Input;
import com.engine.Input.TouchUpEvent;
import com.engine.Input.TouchDownEvent;
import com.engine.Utility;
import com.spartans.ball.Ball;
import com.spartans.enemies.BallTarget;

public class PlayerActions implements TouchDownEvent, TouchUpEvent, Input.KeyUpEvent
{
    Player player;

    Input input;

    float dragStartX;
    float dragStartY;
    float touchDistance;
    long dragStartTime;
    public boolean ignoreNextAction;

    Player.State stateBeforeGameStopped;

    PlayerActions(Player player, Input input)
    {
        stateBeforeGameStopped = Player.State.GameStopped;
        ignoreNextAction = false;
        this.player = player;
        this.input = input;

        dragStartX = -1f;
        dragStartY = -1f;

        touchDistance = 2;

        dragStartTime = 0;

        input.AddTouchDownEvent(this);
        input.AddTouchUpEvent(this);
        input.AddKeyUpEvent(this);
    }

    public void Restart()
    {
        stateBeforeGameStopped = Player.State.GameStopped;
        dragStartTime = 0;
        dragStartX = -1f;
        dragStartY = -1f;
        ignoreNextAction = false;
    }

    public void Destroy()
    {
        input.RemoveTouchDownEvent(this);
        input.RemoveTouchUpEvent(this);
        input.RemoveKeyUpEvent(this);
    }

    public BallTarget TryToFindBallTarget(int screenX, int screenY)
    {
        Vector3 loc = Engine.vector3Pool.obtain();
        loc.z = 0;
        loc.x = screenX;
        loc.y = screenY;
        player.cameraManager.ScreenToWorldCoordinate(loc);

        // hedef oyuncunun en az 2 metre ilerisinde olmali
        if ((loc.x - player.x) > 2)
        {
            // hedeflerden mouse altinda olani bulmaya calis
            int size = player.currentGameplay.ballTargets.size();
            for (int i = 0; i < size; ++i)
            {
                if (player.currentGameplay.ballTargets.get(i).IsUnder(loc.x, loc.y, touchDistance))
                {
                    if (!player.currentGameplay.main.isPaused)
                    {
                        player.ShootToTarget(player.currentGameplay.ballTargets.get(i));

                        Engine.vector3Pool.free(loc);
                        return player.currentGameplay.ballTargets.get(i);
                    }
                    break;
                }
            }
        }

        Engine.vector3Pool.free(loc);

        return null;
    }

    @Override
    public boolean TouchDown(int screenX, int screenY, int pointer, int button)
    {
        dragStartX = screenX;
        dragStartY = screenY;

        dragStartTime = System.currentTimeMillis();

        return false;
    }

    @Override
    public boolean TouchUp(int screenX, int screenY, int pointer, int button)
    {
        if (ignoreNextAction)
        {
            ignoreNextAction = false;
            return false;
        }
        float xDiff = screenX - dragStartX;
        float yDiff = screenY - dragStartY;

        // en az 40 birim oteye gitmis olmali
        if (Math.sqrt(xDiff * xDiff + yDiff * yDiff) > 40)
        {
            float degree = (float) Math.toDegrees(Math.atan2(yDiff, xDiff) + 3);

            // acilar: 
            //
            //              90
            //          45      135
            //      0 -------------- 180
            //          315     225
            //              270
            //

            // yukari
            if (degree > 45 && degree < 135)
            {
                player.Jump();
            }
            else if (degree < 315 && degree > 225)
            {
                player.Slide();
            }
        }
        else
        {
            if (player.ball.state == Ball.State.IdleForward || player.ball.state == Ball.State.IdleBackward)
            {
                Vector3 loc = Engine.vector3Pool.obtain();
                loc.z = 0;
                loc.x = screenX;
                loc.y = screenY;
                player.cameraManager.ScreenToWorldCoordinate(loc);

                // kaleye mi tiklandi onu kontrol et
                if (player.canShootToGoal)
                {
                    if (Utility.RectIntersect(loc.x, loc.y, 2, 2, player.currentGameplay.goalLine + 2, 1, 10, 10))
                        player.ShootToGoal();
                }
                else
                {
                    BallTarget target = TryToFindBallTarget(screenX, screenY);
                    if (target != null)
                    {
                        player.clickAlertParticle.particleEffect.start();
                        player.clickAlertParticle.isDrawable = true;
                        player.clickAlertParticle.SetPos(target.BallTargetX(), target.BallTargetY());
                        player.clickAlertParticle.particleEffect.allowCompletion();
                        player.ball.EnableTrail();
                    }
                }

                Engine.vector3Pool.free(loc);
            }
        }

        dragStartX = -1f;
        dragStartY = -1f;

        return false;
    }

    @Override
    public boolean KeyUp(int keycode)
    {

        if (keycode == com.badlogic.gdx.Input.Keys.F1)
        {
            if (player.state == Player.State.GameStopped)
            {
                player.state = stateBeforeGameStopped;
                stateBeforeGameStopped = Player.State.GameStopped;
            }
            else
            {
                stateBeforeGameStopped = player.state;
                player.state = Player.State.GameStopped;
            }
        }

        return false;
    }
}
