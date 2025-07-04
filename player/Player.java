package com.spartans.player;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.engine.*;
import com.esotericsoftware.spine.*;
import com.spartans.CameraManager;
import com.spartans.ball.Ball;
import com.spartans.enemies.BallTarget;
import com.spartans.enemies.GoalKeeper;
import com.spartans.levels.Gameplay;
import com.spartans.meta.GameStats;

public class Player extends Transform
{

    public enum State
    {
        GameStopped,
        Running, // sadece kosuyor
        ShotToGoalWaiting,
        Ascending, // ziplamis, yukseliyor
        Decending, // ziplamis, alcaliyor
        Sliding, // yerden kayiyor
        OnDirt,
        EndLevelFailed,
        EndLevelGoingDown,
        EndLevelSad,
        EndLevelHappy
    }

    public SkeletonEntity entity;

    public Ball ball;
    public EffectParticle feverParticle;
    public EffectParticle smokeTrailParticle;
    public EffectParticle slideParticle;
    public EffectParticle clickAlertParticle;

    Sound kickSound;

    // Renderer renderer;
    CameraManager cameraManager;

    EffectManager effectManager;

    //references
    Gameplay currentGameplay;

    GameStats gameStats;

    // oyuncu istatistikleri
    public PlayerStats stats;
    // oyuncunun hareketlerini algilar. ekrana basti, elini surtu gibi. Algilarsa gerekli fonksiyonu cagirir
    public PlayerActions actions;

    float groundHeight;
    float shootToGoalDistance;

    // kac saniyedir kayma modunda
    float slidingFor;

    //
    float feverModeSpeedUp;

    // bolum baslangic ekrani sirasinda bu false olur
    public boolean isMoving;
    public boolean isFeverMode;
    boolean canShootToGoal;
    boolean failNextFrame;

    boolean turnBackToPlayer;

    float timeLeftSinceInvunerableEnds;
    boolean isInvulnerable;

    public State state;

    public Player(Gameplay currentGameplay, GameStats gameStats, EffectManager effectManager, ResourceGroup resourceGroup, SkeletonRenderer skeletonRenderer, TweenManager tweenManager, CameraManager cameraManager, Input input)
    {
        this.effectManager = effectManager;
        this.currentGameplay = currentGameplay;
        this.cameraManager = cameraManager;
        this.gameStats = gameStats;

        stats = new PlayerStats();
        actions = new PlayerActions(this, input);

        turnBackToPlayer = false;

        timeLeftSinceInvunerableEnds = stats.invunerableDuration;
        isInvulnerable = false;

        isMoving = true;
        canShootToGoal = false;

        state = State.Running;

        shootToGoalDistance = Renderer.gameWorldSizeX - 2;
        widthMeters = 1;
        heightMeters = 2;

        feverModeSpeedUp = 1.3f;

        groundHeight = currentGameplay.GetGroundHeight();


        kickSound = resourceGroup.GetSound("data/sounds/common/kick.wav");

        SkeletonData skeletonData = resourceGroup.GetSkeletonData("data/player/player.json");
        entity = new SkeletonEntity(skeletonRenderer, skeletonData);
        entity.autoUpdate = false;
        entity.SetCurrentAnimation("LevelStart");
        /*
        Array<Animation.Timeline> timelines = skeletonData.findAnimation("Run").getTimelines();
        Animation.EventTimeline animTimeline = (Animation.EventTimeline) timelines.get(timelines.size - 1);
        rightLegStepped = animTimeline.getFrames()[0];
        leftLegStepped = animTimeline.getFrames()[1];
*/

        y = groundHeight;
        width = 32;
        entity.widthMeters = 1;
        entity.heightMeters = 2;
        currentGameplay.Add(entity, 8);

        ball = new Ball(x + 3, 2, stats.shootSpeed, currentGameplay.goalLine, 5f, currentGameplay.commonResources, tweenManager, currentGameplay);

        entity.x = x;
        entity.y = y;
    }

    public void Restart()
    {
        turnBackToPlayer = false;
        failNextFrame = false;
        EndFever();
        entity.SetCurrentAnimation("LevelStart");
        entity.isLooping = true;

        isInvulnerable = false;
        timeLeftSinceInvunerableEnds = stats.invunerableDuration;

        x = 0;
        y = groundHeight;
        entity.x = x;
        entity.y = y;

        actions.Restart();
        ball.Restart(x + 3, 2);

        slidingFor = 0;
        isMoving = true;
        canShootToGoal = false;
        state = State.Running;
    }

    // oyuncunun suanki durumuna gore genisligini dondurur
    public float GetCurrentWidth()
    {
        if (state == State.Sliding)
            return 2;
        return 1;
    }

    public boolean IsFeverAvailable()
    {
        if (isFeverMode)
            return false;
        return gameStats.GetFeverTimeLeft() > 0;
    }

    // oyuncunun suan ki durumuna gore yuksekligini dondurur
    public float GetCurrentHeight()
    {
        if (state == State.Sliding)
            return 1;
        return 2;
    }


    public void DestroyParticles()
    {
        effectManager.DestroyEffect(slideParticle);
        slideParticle = null;

        effectManager.DestroyEffect(feverParticle);
        feverParticle = null;

        effectManager.DestroyEffect(smokeTrailParticle);
        smokeTrailParticle = null;

        effectManager.DestroyEffect(clickAlertParticle);
        clickAlertParticle = null;
    }


    public void CreateParticleEffects()
    {
        CreateFeverParticle();
        CreateSmokeTrail();
        CreateSlideEffect();
        CreateClickAlertEffect();
    }

    public void CreateSlideEffect()
    {
        slideParticle = (EffectParticle) effectManager.NewParticleEffect("data/effects/slide_effect.p", cameraManager.camera);
        slideParticle.lifeTime = -1;
        slideParticle.SetDrawType(Effect.DrawType.GameWorld);
        slideParticle.SetPos(x + entity.widthMeters / 2, y);
        slideParticle.isDrawable = false;
    }

    public void CreateFeverParticle()
    {
        feverParticle = (EffectParticle) effectManager.NewParticleEffect("data/effects/fever_effect.p", cameraManager.camera);
        feverParticle.lifeTime = -1;
        feverParticle.SetDrawType(Effect.DrawType.Screen);
        feverParticle.SetPos(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2);
        feverParticle.isDrawable = false;
    }

    public float GetScreenX()
    {
        Vector3 pos = Engine.vector3Pool.obtain();
        pos.x = x * Renderer.PixelPerMeter;
        pos.y = (y + 1) * Renderer.PixelPerMeter;
        pos.z = 0;
        cameraManager.camera.project(pos);
        float x = pos.x;
        Engine.vector3Pool.free(pos);
        return x;
    }

    public float GetScreenY()
    {
        Vector3 pos = Engine.vector3Pool.obtain();
        pos.x = x * Renderer.PixelPerMeter;
        pos.y = (y + 1) * Renderer.PixelPerMeter;
        pos.z = 0;
        cameraManager.camera.project(pos);
        float y = pos.y;
        y = Gdx.graphics.getHeight() - y;
        Engine.vector3Pool.free(pos);
        return y;
    }

    public void CreateSmokeTrail()
    {
        smokeTrailParticle = (EffectParticle) effectManager.NewParticleEffect("data/effects/smoke_trail.p", cameraManager.camera);
        smokeTrailParticle.lifeTime = -1;
        smokeTrailParticle.SetDrawType(Effect.DrawType.GameWorld);
        smokeTrailParticle.SetPos(x + entity.widthMeters / 2, y);
        smokeTrailParticle.isDrawable = false;
    }

    public void CreateClickAlertEffect()
    {
        clickAlertParticle = (EffectParticle) effectManager.NewParticleEffect("data/effects/click_alert.p", cameraManager.camera);
        clickAlertParticle.lifeTime = -1f;
        clickAlertParticle.SetDrawType(Effect.DrawType.GameWorld);
        clickAlertParticle.isDrawable = false;
    }

    public void LevelStartedCallback()
    {
        entity.ClearAnimationQueue();
        entity.SetCurrentAnimation("Run");
        entity.isLooping = true;
    }

    public void SetMoving(boolean moving)
    {
        isMoving = moving;
    }

    public void StartFever()
    {
        feverParticle.particleEffect.start();
        smokeTrailParticle.particleEffect.start();

        isFeverMode = true;
        feverParticle.isDrawable = true;
        smokeTrailParticle.isDrawable = true;
        cameraManager.state = CameraManager.State.FollowingBehind;
    }

    public void EndFever()
    {
        feverParticle.particleEffect.allowCompletion();
        smokeTrailParticle.particleEffect.allowCompletion();

        isFeverMode = false;
        cameraManager.state = CameraManager.State.FollowingAhead;
    }

    public void ToggleFever()
    {
        if (isFeverMode)
            EndFever();
        else
            StartFever();
    }

    // maksimum yuksekligin yuzde kaci yuksekliginde suan oyuncu, bu puan belirlerken kullaniliyor
    public int GetJumpHeightPercentage()
    {
        int result = (int) ((y / stats.jumpHeight) * 100);
        if (result > 100) // float cok hassas olmadigi icin 100 un ustune cikabiliyor.
            result = 100;
        return result;
    }

    public void AddToCurrentCoints(int amount)
    {
        currentGameplay.currentLevelStats.collectedCoins += amount;
    }

    public void AddPoints(int amount)
    {
        currentGameplay.currentLevelStats.AddPoints(currentGameplay, amount);

        if (amount == 10)
        {
            EffectParticle ten = (EffectParticle) effectManager.NewParticleEffect("data/effects/10.p", cameraManager.camera);
            ten.lifeTime = 3;
            ten.SetPos(x, y + 2);
        }
        else if (amount == 50)
        {
            EffectParticle ten = (EffectParticle) effectManager.NewParticleEffect("data/effects/50.p", cameraManager.camera);
            ten.lifeTime = 3;
            ten.SetPos(x, y + 2);
        }
        else if (amount == 100)
        {
            EffectParticle ten = (EffectParticle) effectManager.NewParticleEffect("data/effects/100.p", cameraManager.camera);
            ten.lifeTime = 3;
            ten.SetPos(x, y + 2);
        }
        else
            Log.Print("Error: no particle effect for " + amount + " points");
    }

    public void EndGameplay(boolean success)
    {
        EndFever();
        if (state == State.Running)
        {
            if (success)
            {
                entity.SetCurrentAnimation("Goal");
                entity.isLooping = true;
                state = State.EndLevelHappy;
            }
            else
            {
                entity.ClearAnimationQueue();
                entity.SetCurrentAnimation("FallRun");
                entity.isLooping = false;
                state = State.EndLevelSad;
            }
        }
        else if (state == State.Sliding)
        {
            if (success)
            {
                entity.SetCurrentAnimation("Goal");
                entity.isLooping = true;
                state = State.EndLevelHappy;
            }
            else
            {
                entity.ClearAnimationQueue();
                entity.SetCurrentAnimation("FallRun");
                entity.isLooping = false;
                state = State.EndLevelSad;
            }
        }
        else if (state == State.Ascending || state == State.Decending)
        {
            if (!success)
            {
                entity.SetCurrentAnimation("FallRun");
                entity.isLooping = false;
                state = State.EndLevelGoingDown;
            }
            else
            {
                entity.SetCurrentAnimation("Goal");
                entity.isLooping = true;
                state = State.EndLevelHappy;
            }
        }

        ball.state = Ball.State.Stray;
        cameraManager.state = CameraManager.State.Fixed;
        slideParticle.particleEffect.allowCompletion();
    }

    public void Destroy()
    {

        DestroyParticles();

        actions.Destroy();
        // ball entity yi current levele ekledigim icin, burda destroy cagirmiyoruz
        ball = null;

        entity = null;
        // entity yi current levele ekledigim icin, burda destroy cagirmiyoruz
    }

    Array<Event> t = new Array<Event>(10);

    public void Update(float timePassed)
    {
        entity.Update(timePassed, true, t);
        if (state == State.Running)
            if (t.size > 0)
            {
                if (t.get(0).getInt() == 1)
                    currentGameplay.GetLeftFootstepSound().play();
                else
                    currentGameplay.GetRightFootstepSound().play();
                t.clear();
            }
        Act(timePassed);
        ball.Update(timePassed, this, groundHeight);
    }

    public void Move(float timePassed)
    {
        if (isMoving)
        {
            x += GetRunSpeed() * timePassed;
            entity.x = x;
            entity.y = y;
        }
    }

    public void MoveSlow(float timePassed)
    {
        if (isMoving)
        {
            x += timePassed * stats.runSpeed * 0.2f;
            entity.x = x;
            entity.y = y;
        }
    }


    public void UpdateCamera(float timePassed)
    {
        switch (cameraManager.state)
        {
            case Fixed:
                break;
            case FollowingBehind:
                if (cameraManager.currentShiftAmount < cameraManager.cameraShiftAhead)
                    cameraManager.currentShiftAmount += 1.4f * timePassed;
                break;
            case FollowingAhead:
                if (cameraManager.currentShiftAmount > cameraManager.cameraShiftBehind)
                    cameraManager.currentShiftAmount -= 1.4f * timePassed;
                break;
        }

        if (cameraManager.state != CameraManager.State.Fixed)
            cameraManager.SetX(x + cameraManager.currentShiftAmount);
        if ((Renderer.gameWorldSizeX / 2 + cameraManager.x) >= (currentGameplay.goalLine + 2))
            cameraManager.state = CameraManager.State.Fixed;

    }

    void Act(float timePassed)
    {
        switch (state)
        {
            case GameStopped:
                break;
            case ShotToGoalWaiting:
                break;
            case Ascending:
                if (y < stats.jumpHeight)
                    y += timePassed * stats.jumpSpeed;
                else
                    state = State.Decending;

                Move(timePassed);
                break;
            case Decending:
                if (entity.y > groundHeight)
                {
                    if ((y - timePassed * stats.jumpSpeed) > groundHeight)
                        y -= timePassed * stats.jumpSpeed;
                    else
                    {
                        y = groundHeight;
                        state = State.Running;
                        entity.SetCurrentAnimation("Run");
                        entity.isLooping = true;
                        ball.SetState(Ball.State.GoingBackToFoot);
                        currentGameplay.GetLandingSound().play();
                    }
                }
                else
                {
                    y = groundHeight;
                    state = State.Running;
                }

                Move(timePassed);

                break;
            case Sliding:

                slidingFor += timePassed;
                // kayma suresini doldurmus
                if (slidingFor >= stats.slidingDuration)
                {
                    state = State.Running;
                    slidingFor = 0f;

                    entity.SetCurrentAnimation("Run");
                    entity.isLooping = true;

                    slideParticle.particleEffect.allowCompletion();
                }
                else if (slidingFor >= stats.slideGetUpDuration)
                {
                    // sadece 1 kere atamak icin
                    if (entity.isLooping)
                    {
                        entity.SetCurrentAnimation("SlideEnd");
                        entity.isLooping = false;
                        currentGameplay.GetSlideSound().setLooping(slideLoopId, false);
                        currentGameplay.GetSlideSound().stop(slideLoopId);
                    }
                }

                Move(timePassed);
                break;
            case Running:
                Move(timePassed);
                break;
            case OnDirt:
                MoveSlow(timePassed);
                break;
            case EndLevelHappy:
            case EndLevelGoingDown:
            case EndLevelSad:
            case EndLevelFailed:
                // oyuncu oyun bitince havadaysa asagi iner
                if (entity.y > groundHeight)
                {
                    if ((y - timePassed * stats.jumpSpeed) > groundHeight)
                        y -= timePassed * stats.jumpSpeed;
                    else
                        y = groundHeight;
                }
                else
                    y = groundHeight;
                entity.x = x;
                entity.y = y;
                break;
            default:
                break;
        }

        switch (state)
        {
            case EndLevelFailed:
            case EndLevelGoingDown:
            case EndLevelSad:
            case EndLevelHappy:
                break;
            default:
                if (x > (currentGameplay.goalLine - shootToGoalDistance))
                {
                    canShootToGoal = true;
                    if (currentGameplay.goalKeeper != null)
                    {
                        if (currentGameplay.goalKeeper.state == GoalKeeper.State.Waiting && currentGameplay.goalKeeper.entity.x - x < currentGameplay.goalKeeper.jumpDistance)
                        {
                            currentGameplay.goalKeeper.JumpDown();
                        }
                    }

                    if (x + entity.widthMeters > currentGameplay.goalLine)
                    {
                        if (!ball.isFailed)
                        {
                            EndGameGoal();
                            state = State.EndLevelHappy;
                            entity.ClearAnimationQueue();
                            entity.SetCurrentAnimation("Goal");
                            entity.isLooping = true;
                            ball.SetState(Ball.State.Stray);
                            ball.velX += GetRunSpeed() * 0.5f;
                        }
                    }

                }
        }


        if (isInvulnerable)
        {
            timeLeftSinceInvunerableEnds -= timePassed;
            if (timeLeftSinceInvunerableEnds < 0)
            {
                entity.isVisible = true;
                isInvulnerable = false;
                timeLeftSinceInvunerableEnds = stats.invunerableDuration;
            }
            else if (timeLeftSinceInvunerableEnds < 0.1f)
            {
                entity.isVisible = false;
            }
            else if (timeLeftSinceInvunerableEnds < 0.2f)
            {
                entity.isVisible = true;
            }
            else if (timeLeftSinceInvunerableEnds < 0.3f)
            {
                entity.isVisible = false;
            }
            else if (timeLeftSinceInvunerableEnds < 0.4f)
            {
                entity.isVisible = true;
            }
            else if (timeLeftSinceInvunerableEnds < 0.6f)
            {
                entity.isVisible = false;
            }
            else if (timeLeftSinceInvunerableEnds < 0.8f)
            {
                entity.isVisible = true;
            }
            else
            {
                entity.isVisible = false;
            }

        }

        slideParticle.SetPos(x + widthMeters + 1, y + 0.2f);
        if (isFeverMode)
        {
            smokeTrailParticle.SetPos(x, y);
        }

        if (failNextFrame)
        {
            EndGameFail();
            failNextFrame = false;
        }

        if(IsFeverAvailable())
            StartFever();

        // fever mode taysa oyuncuyu hizlandir
        if (isFeverMode)
        {
            entity.Update(timePassed * (feverModeSpeedUp - 1), false, null);

            gameStats.AddFeverTime(-timePassed);

            if (gameStats.GetFeverTimeLeft() == 0)
                EndFever();
        }

        UpdateCamera(timePassed);
    }


    public void EnteredDirtPatch()
    {
        Log.Print("entered dirt patch");
        state = State.OnDirt;
        entity.SetCurrentAnimation("WalkOnDirt");
        entity.isLooping = true;
    }

    public void ExitDirtPatch()
    {
        Log.Print("exit dirt patch");
        state = State.Running;
        entity.SetCurrentAnimation("Run");
        entity.isLooping = true;
    }

    public void GetHit()
    {
        if (!isInvulnerable)
        {
            currentGameplay.currentLevelStats.lives -= 1;
            if (currentGameplay.currentLevelStats.lives < 0)
                SetGameToFail();
            else
            {
                isInvulnerable = true;
                currentGameplay.LoseAHeart();
            }

        }
    }

    private void SetGameToFail()
    {
        failNextFrame = true;
    }

    void EndGameFail()
    {
        EndGameplay(false);
        currentGameplay.CompleteLevel(gameStats, false, false);
    }

    public void EndGameGoal()
    {
        EndGameplay(true);
        currentGameplay.CompleteLevel(gameStats, true, true);
    }

    void Jump()
    {
        if (state == State.Running)
        {
            state = State.Ascending;
            entity.SetCurrentAnimation("Jump");
            entity.isLooping = false;
            entity.AddAnimationToQueue("JumpLoop", true);
            ball.SetState(Ball.State.UpAirPlayerFoot);
        }
    }

    public float GetRunSpeed()
    {
        if (isFeverMode)
            return stats.runSpeed * feverModeSpeedUp;
        else
            return stats.runSpeed;
    }


    long slideLoopId;
    void Slide()
    {
        if (state == State.Running)
        {
            state = State.Sliding;
            slidingFor = 0f;
            entity.SetCurrentAnimation("SlideStart");
            entity.isLooping = false;
            entity.AddAnimationToQueue("SlideLoop", true);
            slideParticle.isDrawable = true;
            slideParticle.particleEffect.start();
            slideLoopId = currentGameplay.GetSlideSound().play();
            currentGameplay.GetSlideSound().setLooping(slideLoopId, true);
        }
    }

    void ShootToTarget(BallTarget target)
    {
        ball.currentTarget = target;
        ball.SetState(Ball.State.GoingToTarget);

        float xSpeed = target.GetVelocityX();
        float targetX = 20 * (target.BallTargetX() - x) / (xSpeed + 20);
        ball.SetTarget(x + targetX, target.BallTargetY(), 20);
        target.BallLaunched();

        kickSound.play();

        if (state == State.Running)
        {
            entity.isLooping = false;
            entity.SetCurrentAnimation("Shoot");
            entity.AddAnimationToQueue("Run", true);
        }
    }

    void ShootToGoal()
    {
        ball.EnableTrail();
        ball.SetState(Ball.State.GoingToGoal);
        ball.SetTarget(ball.goalLine + 2, ball.goalHeight, 20);

        if (State.Running == state)
        {
            entity.isLooping = false;
            entity.SetCurrentAnimation("Shoot");
            entity.AddAnimationToQueue("Run", true);
        }
        kickSound.play();
        if (currentGameplay.goalKeeper != null)
            currentGameplay.goalKeeper.JumpUp();
        state = State.ShotToGoalWaiting;
    }

    public void AddFeverTime(float amount)
    {
        gameStats.AddFeverTime(amount);
    }
}
