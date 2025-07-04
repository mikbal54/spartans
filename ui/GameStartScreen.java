package com.spartans.ui;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.engine.*;
import com.spartans.CameraManager;
import com.spartans.Spartans;
import com.spartans.levels.Level;

public class GameStartScreen extends Level implements InputProcessor
{
    Spartans main;

    TweenManager tweenManager;
    StaticEntity startText;
    StaticEntity title;
    TextureRegion ground;

    StaticEntity goal;
    StaticEntity sun;
    StaticEntity clouds[];

    public GameStartScreen(Spartans main, ResourceGroup commonGroup)
    {
        this.main = main;
        this.commonResources = commonGroup;

        tweenManager = new TweenManager();

        Gdx.input.setInputProcessor(this);
        clouds = new StaticEntity[30];

        background = new StaticEntity(commonGroup.GetTextureRegion("data/common/common.atlas", "start_back"), Renderer.gameWorldSizeX * 2, Renderer.gameWorldSizeY * 2);
        background.x = Renderer.gameWorldSizeX / 2 - background.widthMeters / 2;
        background.y = Renderer.gameWorldSizeY / 2 - background.heightMeters / 2;

        startText = new StaticEntity(commonGroup.GetTextureRegion("data/common/common.atlas", "press_start"), Renderer.gameWorldSizeX / 2, Renderer.gameWorldSizeX / 2 / 4);
        startText.x = Renderer.gameWorldSizeX / 2;
        startText.y = 2;
        startText.angle = -5;
        startText.originY = startText.height / 2;
        startText.originX = startText.width / 2;
        Add(startText, 1);

        Tween.to(startText, TweenAccessors.StaticEntityPositionAccessor.ANGLE, 0.5f).target(5).repeatYoyo(-1, 0).start(tweenManager);
        Tween.to(startText, TweenAccessors.StaticEntityPositionAccessor.WIDTH, 1).target(startText.widthMeters * 1.1f).repeatYoyo(-1, 0).start(tweenManager);

        title = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "game_title"), Renderer.gameWorldSizeX / 2, Renderer.gameWorldSizeX / 8);
        title.x = Renderer.gameWorldSizeX / 2;
        title.y = Renderer.gameWorldSizeY - 2;
        title.originX = title.width / 2;
        title.originY = title.height / 2;
        Add(title, 2);

        sun = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "sun"), 3, 3);
        sun.x = Renderer.gameWorldSizeX - sun.widthMeters / 2;
        sun.y = Renderer.gameWorldSizeY - sun.heightMeters / 2;
        sun.originX = sun.width / 2;
        sun.originY = sun.height / 2;
        Add(sun, 1);

        goal = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "kale"), 2.5f, 4);
        goal.x = Renderer.gameWorldSizeX - goal.widthMeters;
        goal.y = 0.8f;
        Add(goal, 1);

        for (int i = 0; i < clouds.length; ++i)
        {
            int randInt = Utility.RandomInt(0, 100);
            if (randInt > 50)
                clouds[i] = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "cloud2"), 1.61f, 1);
            else
                clouds[i] = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "cloud3"), 1.61f, 1);

            Add(clouds[i], 1);
        }

        ground = commonResources.GetTextureRegion("data/common/common.atlas", "ground");
        ground.setRegion(ground.getRegionX() + 1, ground.getRegionY(), ground.getRegionWidth() - 1, ground.getRegionHeight());

        for (int i = 0; i < clouds.length; ++i)
        {
            clouds[i].x = Renderer.gameWorldSizeX * i * 0.08f;
            clouds[i].y = (float) (Renderer.gameWorldSizeY * Utility.RandomDouble(0.4f, 0.9f));
        }

        main.cameraManager.x = Renderer.gameWorldSizeX / 2;
    }

    public void Destroy(InputProcessor input)
    {
        Gdx.input.setInputProcessor(input);
        tweenManager.killAll();
    }

    boolean gameTitleScalingDown = true;
    float gameTitleTimeToScale = 0f;

    public void ScaleGameTitle(float timePassed)
    {
        float scaleAmount = timePassed / 30;
        if (gameTitleScalingDown)
        {
            title.widthMeters = title.widthMeters - (title.widthMeters * scaleAmount);
            title.heightMeters = title.heightMeters - (title.heightMeters * scaleAmount);

        }
        else
        {
            title.widthMeters = title.widthMeters + (title.widthMeters * scaleAmount);
            title.heightMeters = title.heightMeters + (title.heightMeters * scaleAmount);
        }
        gameTitleTimeToScale += scaleAmount;

        if (gameTitleTimeToScale > 0.04f)
        {
            gameTitleTimeToScale = 0;
            gameTitleScalingDown = !gameTitleScalingDown;
        }
    }

    boolean sunScalingDown = true;
    float sunTimeToScale = 0f;

    void SunScaler(float timePassed)
    {
        float scaleAmount = timePassed / 30;
        if (gameTitleScalingDown)
        {
            sun.widthMeters = sun.widthMeters - (sun.widthMeters * scaleAmount);
            sun.heightMeters = sun.heightMeters - (sun.heightMeters * scaleAmount);

        }
        else
        {
            sun.widthMeters = sun.widthMeters + (sun.widthMeters * scaleAmount);
            sun.heightMeters = sun.heightMeters + (sun.heightMeters * scaleAmount);
        }
        sunTimeToScale += scaleAmount;

        if (sunTimeToScale > 0.04f)
        {
            sunTimeToScale = 0;
            sunScalingDown = !sunScalingDown;
        }
    }

    public void Update(float timePassed)
    {
        Update(timePassed, main.cameraManager);

        tweenManager.update(timePassed);
        // main.cameraManager.Update(timePassed, staticEntities, animatedEntities);
        ScaleGameTitle(timePassed);

        //RotateStartText(timePassed);
        //ScaleStartText(timePassed);

        SunScaler(timePassed);

        for (int i = 0; i < clouds.length; ++i)
        {
            clouds[i].x += ((float) (-timePassed * Utility.RandomDouble(0.05f, 0.08f)));
            clouds[i].y += ((float) (-timePassed * Utility.RandomDouble(-0.10f, 0.10f)));
        }
    }

    public void Draw(Renderer renderer, CameraManager cameraManager, SpriteBatch batch)
    {
        batch.begin();
        DrawEntities(renderer, cameraManager);
        float lastX = -1;
        for (int i = 0; i < 5; i++)
        {
            batch.draw(ground, lastX -2, 0);
            lastX += ground.getRegionWidth();
        }
        batch.end();
    }


    @Override
    public boolean keyDown(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        main.gameState = Spartans.GameState.GameStartScreenPressed;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return false;
    }

    @Override
    public boolean scrolled(int amount)
    {
        return false;
    }
}
