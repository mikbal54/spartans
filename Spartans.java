package com.spartans;

import com.badlogic.gdx.graphics.Texture;
import com.engine.*;
import com.engine.Input.KeyDownEvent;
import com.spartans.levels.Gameplay;
import com.spartans.meta.GameStats;
import com.spartans.meta.LevelData;
import com.spartans.player.Player;
import com.spartans.ui.GameStartScreen;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;
import com.spartans.ui.MainMenu;

public class Spartans implements ApplicationListener, KeyDownEvent
{
    private MultipleVirtualViewportBuilder multipleVirtualViewportBuilder;

    public DevelopmentMode developmentMode;

    public Gameplay currentGameplay;
    public Player player;
    public Engine engine;
    public GameStats gameStats;
    public CameraManager cameraManager;

    //public MainMenu mainMenu;
    public MainMenu mainMenu2;

    public GameStartScreen gameStartScreen;

    public enum GameState
    {
        JustStarted, // oyun yeni acildi
        GameStartScreen,
        GameStartScreenPressed,
        MainMenuQueued,
        MainMenuStarted,
        MainMenu, // oyun ana menusunde
        Restart, // bolumu yeniden baslatilacagi zaman
        HardRestart, // bolumu yeniden baslatir, dosyalari bi daha yukser. shift+s ye basilinca olur
        InGame, // oyun devam ediyor
        LevelLoadQueued,
        LevelLoadStarted,
        Exit, // bir sonraki framede oyun sona erecek
        WaitingExit
    }

    // oyun durunca timePassed cok yuksek olursa oyun bozulur. o yuzden durup/devam ettigi zamam zamani dogru ayarlamak icin kullaniyorum
    boolean justResumed;
    public boolean isPaused;

    public GameState gameState = GameState.JustStarted;

    public LevelData currentLevelData;

    public ResourceGroup lastLoadedResourceGroup;

    LevelData queuedLevelData;

    public void create()
    {
        isPaused = false;
        gameStats = new GameStats();
        developmentMode = new DevelopmentMode();
        justResumed = true;
        engine = new Engine();

        long time = System.nanoTime();
        engine.resourceManager.LoadResourceGroupList("data/resource_groups.json", gameStats.GetDefaultTextureRegion(), gameStats.GetDefaultSound());
        gameStats.common = engine.resourceManager.LoadResourceGroup(engine.renderer.effectManager, "common");
        gameStats.common.SetTextureFilterForAll(Texture.TextureFilter.Linear);
        Log.Print("Common group loaded: " + ((System.nanoTime() - time) / 1000000) + "ms");

        gameStats.LoadLevelDatas(engine.renderer.effectManager);

        multipleVirtualViewportBuilder = new MultipleVirtualViewportBuilder(568, 320, 480, 320);
        //multipleVirtualViewportBuilder = new MultipleVirtualViewportBuilder(800, 480, 854, 600);
        // debugRenderer = new SkeletonRendererDebug();
        VirtualViewport virtualViewport = multipleVirtualViewportBuilder.getVirtualViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraManager = new CameraManager(engine.renderer, new OrthographicCameraWithVirtualViewport(virtualViewport));

        engine.input.AddKeyDownEvent(this);


        ChangeGameState(GameState.GameStartScreen, null);

        gameStartScreen = new GameStartScreen(this, gameStats.common);
    }


    public void TogglePause()
    {
        if (isPaused)
            justResumed = true;

        isPaused = !isPaused;

        currentGameplay.state = Gameplay.State.Ingame;
        currentGameplay.SetPauseUI(isPaused);
    }

    public void ChangeGameState(GameState newState, LevelData levelData)
    {
        if (newState == GameState.HardRestart)
        {
            gameState = newState;
            return;
        }

        if (gameState == GameState.InGame)
        {
            DestroyLevel();
        }
        else if (gameState == GameState.MainMenu)
        {
            if (newState == gameState)
                return;

            //mainMenu2.Destroy(engine.input);
            //mainMenu2 = null;
            //mainMenu.Destroy(engine.input);
            //mainMenu = null;
            //Runtime.getRuntime().gc();
        }

        gameState = newState;
        if (newState == GameState.InGame)
        {
            gameState = GameState.LevelLoadQueued;
            queuedLevelData = levelData;
        }
        else if (newState == GameState.MainMenu)
        {
            gameState = GameState.MainMenuQueued;
        }

    }

    public void LoadLevel(LevelData ldata)
    {

        gameStats.NewLevelStarted();
        currentLevelData = ldata;
        currentGameplay = new Gameplay(this, gameStats.common);
        currentGameplay.LoadFiles(ldata.name, gameStats.GetDefaultTextureRegion(), engine.renderer.effectManager);
        currentGameplay.Create(engine.renderer.batch, engine.renderer.skeletonRenderer, engine.renderer.effectManager);

        player = new Player(currentGameplay, gameStats, engine.renderer.effectManager, gameStats.common, engine.renderer.skeletonRenderer, engine.tweenManager, cameraManager, engine.input);
        engine.renderer.Resize(cameraManager, multipleVirtualViewportBuilder, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        currentGameplay.player = player;
        player.ball.CreateTrail(engine.renderer.effectManager);
        player.CreateParticleEffects();
        player.SetMoving(false);
        justResumed = true;
    }

    public void LoadLevel(String levelName)
    {
        LevelData ldata = gameStats.levelDatas.get(levelName);
        LoadLevel(ldata);
    }

    public void DestroyLevel()
    {
        if (player != null)
            player.Destroy();
        if (currentGameplay != null)
            currentGameplay.Destroy(engine.renderer.effectManager);
        player = null;
        currentGameplay = null;
        cameraManager.Reset();
        Runtime.getRuntime().gc();
    }

    public void DestroyMainMenu()
    {
        /*
        if (mainMenu != null)
        {
            mainMenu.Destroy(engine.input);
            mainMenu = null;
        }
        */
        if (mainMenu2 != null)
        {
            mainMenu2.Destroy(engine.input);
            mainMenu2 = null;
        }
    }

    public void RestartLevel()
    {
        long startTime = System.nanoTime();
        currentGameplay.Restart();
        player.Restart();
        cameraManager.Reset();
        Runtime.getRuntime().gc();
        justResumed = true;
        gameState = GameState.InGame;
        gameStats.NewLevelStarted();

        Runtime.getRuntime().gc();
        long endTime = System.nanoTime();
        if (Log.isLogging)
        {
            long dur = endTime - startTime;
            Log.Print("Level Reloaded (" + dur / 1000000 + "ms) ");
            Log.Print("Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        }
    }

    public void HardRestartLevel()
    {

        DestroyLevel();
        LoadLevel(currentLevelData.name);

        cameraManager.Reset();

        if (developmentMode.state == DevelopmentMode.State.LevelEditing)
        {
            player.stats.runSpeed = 0;
            developmentMode.RestorePlayerStatus(player);
            developmentMode.RestoreCameraManager(cameraManager);
        }

        Runtime.getRuntime().gc();
        Log.Print("Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        justResumed = true;
        gameState = GameState.InGame;
        gameStats.NewLevelStarted();
    }

    void GameLogic(float timePassed)
    {
        currentGameplay.Update(timePassed, cameraManager, player);
    }

    void DrawGameplay()
    {
        engine.renderer.batch.begin();
        currentGameplay.DrawEntities(engine.renderer, cameraManager);
        currentGameplay.DrawUI(engine.renderer, cameraManager);
        currentGameplay.DrawEffects(engine.renderer);
        engine.renderer.batch.end();
    }

    public void render()
    {

        float timePassed = Gdx.graphics.getDeltaTime();
        timePassed = Math.max(timePassed, 1 / 60f);
        // bu fonksiyon oyun sirasinda devamli cagirilir
        engine.Update(timePassed);
        switch (gameState)
        {
            case GameStartScreen:
            {
                gameStartScreen.Update(timePassed);
                gameStartScreen.Draw(engine.renderer, cameraManager, engine.renderer.batch);
            }
            break;
            case GameStartScreenPressed:
                ChangeGameState(GameState.MainMenu, null);
                break;
            case InGame:
            {
                if (isPaused)
                    timePassed = 0f;
                else
                    timePassed = Gdx.graphics.getDeltaTime();

                if (justResumed)
                {
                    timePassed = 0;
                    justResumed = false;
                }

                if (!isPaused)
                {
                    player.Update(timePassed);
                    GameLogic(timePassed);
                    DrawGameplay();
                }
                else
                {
                    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
                    currentGameplay.ui.draw();
                }
            }
            break;
            case HardRestart:
                HardRestartLevel();
                break;
            case Restart:
                RestartLevel();
                break;
            case MainMenuQueued:
                gameState = GameState.MainMenuStarted;
                break;
            case MainMenuStarted:
                DestroyLevel();
                // DestroyMainMenu();
                if (mainMenu2 == null)
                {
                    mainMenu2 = new MainMenu(this, engine.resourceManager, cameraManager, gameStats, engine.renderer.batch);
                }
                mainMenu2.Reset();
                mainMenu2.CaptureInputProcessor();
                gameState = GameState.MainMenu;
                break;
            case MainMenu:
            {
                Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
                mainMenu2.Update(timePassed);
                engine.renderer.batch.begin();
                mainMenu2.DrawEntities(engine.renderer, cameraManager);
                mainMenu2.DrawText(engine.renderer.batch);
                engine.renderer.batch.end();

                //mainMenu.Update(timePassed);
                //mainMenu.DrawGameplay();
            }
            break;
            case Exit:
                DestroyLevel();
                Gdx.app.exit();
                gameState = GameState.WaitingExit;
                break;
            case WaitingExit:
                gameState = GameState.Exit;
                break;
            case LevelLoadQueued:
                Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
                gameState = GameState.LevelLoadStarted;
                break;
            case LevelLoadStarted:
                DestroyLevel();
                LoadLevel(queuedLevelData);
                gameState = GameState.InGame;
                Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
                break;
            default:
                break;

        }



        /*
        // bunu yavas telefonlarda nasil olur onu test etmek icin kullaniyom
        try
        {
            Thread.sleep((long) (Math.random() * 100));
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        */

    }

    int currentWidth;
    int currentHeight;

    public void resize(int width, int height)
    {
        if (width == currentWidth && currentHeight == height)
            return;
        currentWidth = width;
        currentHeight = height;

        engine.renderer.Resize(cameraManager, multipleVirtualViewportBuilder, width, height);

        if (gameState == GameState.MainMenu)
        {
            //TODO: resize main menu
            //   mainMenu.Resize(width, height);
        }

        Log.Print("Device width: " + width);
        Log.Print("Device height: " + height);
    }

    @Override
    public void pause()
    {
        switch (gameState)
        {
            case InGame:
                if (currentGameplay.state == Gameplay.State.EndLevel)
                    return;

                if (!player.isMoving)
                    return;

                if (isPaused)
                    justResumed = true;

                isPaused = !isPaused;
                currentGameplay.SetPauseUI(true);

                break;
            default:
                break;
        }

        Log.Print("pause called");
    }

    @Override
    public void resume()
    {
        // TODO Auto-generated method stub
        Log.Print("resume called");
        justResumed = true;
    }

    @Override
    public void dispose()
    {
        if (currentGameplay != null)
            currentGameplay.Destroy(engine.renderer.effectManager);
        if (player != null)
            player.Destroy();

        currentGameplay = null;
        player = null;

        //TODO dispose default assets
        engine = null;

        Runtime.getRuntime().gc();
        Log.Print("dispose called");
    }

    @Override
    public boolean KeyDown(int keycode)
    {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && keycode == Input.Keys.S)
        {
            gameState = GameState.HardRestart;
        }
        else if (keycode == Input.Keys.BACK)
        {
            gameState = GameState.Exit;
        }
        else if (keycode == Input.Keys.F)
        {
            player.ToggleFever();
        }

        return false;
    }

}
