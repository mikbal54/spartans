package com.spartans.levels;

import java.util.ArrayList;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.engine.*;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.engine.Input.TouchUpEvent;
import com.spartans.CameraManager;
import com.spartans.Spartans;
import com.spartans.Spartans.GameState;
import com.spartans.collectibles.Coin;
import com.spartans.collectibles.Collectible;
import com.spartans.collectibles.SpeedBoost;
import com.spartans.enemies.*;
import com.spartans.meta.GameStats;
import com.spartans.meta.LevelData;
import com.spartans.player.Player;

public class Gameplay extends Level implements TouchUpEvent
{

    public enum State
    {
        Ingame,
        Pause,
        EndLevel
    }

    public class CurrentLevelStats
    {

        public float timeSinceLevelStart;
        public int currentPoints;
        public int collectedCoins;
        public int lives;

        public CurrentLevelStats()
        {
            lives = 3;
        }

        public void Reset()
        {
            timeSinceLevelStart = 0;
            currentPoints = 0;
            lives = 3;
            collectedCoins = 0;
        }


        public void AddPoints(Gameplay gameplay, int amount)
        {
            currentPoints += amount;
            gameplay.SetNewScore(currentPoints);
        }
    }


    public class GroundSounds
    {
        public Sound leftFoot;
        public Sound rightFoot;
        public Sound landing;
        public Sound slide;
    }

    public State state;

    public CurrentLevelStats currentLevelStats;

    public ArrayList<BallTarget> ballTargets;

    public ArrayList<GroundSounds> groundSounds;

    public ArrayList<Enemy> enemies;
    public ArrayList<Collectible> collectibles;

    public GoalKeeper goalKeeper;

    public LevelInfo info;

    public Spartans main;
    public Player player;
    LevelData levelData;

    String startingText;
    String endingText;
    String failedText;

    public float goalLine;
    public float timeToComplete;
    public int maxPoints;
    public float groundHeight;
    boolean gameStarted;
    boolean finishedEarly;
    boolean bonusesEnded;

    public int fansGained1, fansGained2, fansGained3;
    public int moneyGained1, moneyGained2, moneyGained3;

    Image pauseButton;

    Image playButton;
    Image restartButton;
    Image exitButton;

    public Label scoreText;

    TextureRegion heartEmptyTextureRegion;
    TextureRegion heartTextureRegion;
    public Image[] heartImages;

    // bolum basindaki play tusu
    Table startLevelTable;
    Image startLevelBackground;


    public ScoreTable scoreTable;

    float timeSinceScoreTableStarted;
    float timeTillBonusesEnd;
    float scoreTableTimer;
    int starsShown, starsEarned;
    int moneyEarned;
    int fansEarned;

    public class ScoreTable
    {
        // score table
        TweenManager tweenManager;
        Image heartImage;
        Image foulImage;
        Image timeBonusImage;
        Image stars[];
        Image coachBackground;
        Label goalText;
        Image coachImage;
        Label finalScore;
        Image replayImage;
        Image fansIcon;
        Label fansAmountLabel;
        Label coisAmountLabel;
        Image coinsIcon;
        Image nextLevelButton;
        Image homeButton;

        ScoreTable()
        {
            tweenManager = new TweenManager();
        }

        void Update(float timePassed)
        {
            tweenManager.update(timePassed);
            scoreTable.replayImage.rotate(-timePassed * 200);
        }

        void ResetStarRotations()
        {
            for (int i = 0; i < 3; ++i)
            {
                stars[i].setRotation(0);
                stars[i].setScale(1);
            }
        }

        void Reset()
        {
            tweenManager.killAll();
            ResetStarRotations();
            for (int i = 0; i < 3; ++i)
            {
                ((TextureRegionDrawable) stars[i].getDrawable()).setRegion((commonResources.GetTextureRegion("data/common/common.atlas", "star_result_empty")));
                stars[i].setOrigin(stars[i].getWidth() / 2, stars[i].getHeight() / 2);
                Timeline.createSequence().push(Tween.to(stars[i], TweenAccessors.ImageAccessor.SCALE, 0.5f).ease(Quad.INOUT).target(1.1f)).push(Tween.to(stars[i], TweenAccessors.ImageAccessor.ROTATION, 0.1f).ease(Quad.INOUT).target(-6)).push(Tween.to(stars[i], TweenAccessors.ImageAccessor.ROTATION, 0.2f).ease(Quad.INOUT).target(6)).repeatYoyo(-1, 0).start(tweenManager);
            }
            timeBonusImage.setPosition(coachBackground.getX() + coachBackground.getWidth() / 2 - timeBonusImage.getWidth() / 2, stars[0].getY() + stars[0].getHeight() + 5);
        }

        void UpdateFanAndCoinAmounts(int fansAmount, int coinsAmount)
        {
            fansAmountLabel.setText(String.valueOf(fansAmount));
            coisAmountLabel.setText(String.valueOf(coinsAmount));

            fansAmountLabel.setPosition(fansIcon.getX() - fansAmountLabel.getTextBounds().width - 5, fansIcon.getY() - fansAmountLabel.getTextBounds().height / 4);
            coisAmountLabel.setPosition(coinsIcon.getX() - coisAmountLabel.getWidth() - 10, coinsIcon.getY() - coisAmountLabel.getTextBounds().height / 4);
        }

        void SetSuccess()
        {
            goalText.setText(endingText);
            coachImage.setDrawable(new TextureRegionDrawable(uiAtlas.findRegion("coach_victory")));
        }

        void SetFailed()
        {
            goalText.setText(failedText);
            coachImage.setDrawable(new TextureRegionDrawable(uiAtlas.findRegion("coach_lose")));
        }

        void SetupNextLevelButton()
        {
            final int nextLevelId = main.gameStats.GetNextLevel(levelData.id);
            if (nextLevelId == -1)
                scoreTable.nextLevelButton.setVisible(false);
            else
            {
                scoreTable.nextLevelButton.addListener(new ClickListener()
                {
                    final int levelId = nextLevelId;
                    public void clicked(InputEvent event, float x, float y)
                    {
                        main.ChangeGameState(GameState.InGame, main.gameStats.sortedLevelDatas.get(levelId));
                    }
                });

            }
        }

        void SetVisible(boolean status)
        {
            stars[0].setVisible(status);
            stars[1].setVisible(status);
            stars[2].setVisible(status);
            heartImage.setVisible(status);

            timeBonusImage.setVisible(status);
            foulImage.setVisible(status);
            nextLevelButton.setVisible(status);
            homeButton.setVisible(status);
            fansAmountLabel.setVisible(status);
            coisAmountLabel.setVisible(status);
            coinsIcon.setVisible(status);
            fansIcon.setVisible(status);
            coachImage.setVisible(status);
            replayImage.setVisible(status);
            coachBackground.setVisible(status);
            goalText.setVisible(status);
        }

        void Show(boolean success)
        {
            StringBuilder skor = new StringBuilder(10);
            skor.append(currentLevelStats.currentPoints);
            skor.append("/");
            skor.append(maxPoints);
            finalScore.setText(skor.toString());

            SetVisible(true);

            if (success)
            {
                foulImage.setVisible(false);
                for (int i = 0; i < 3; ++i)
                    stars[i].setVisible(true);
            }
            else
            {
                foulImage.setVisible(true);
                foulImage.setRotation(-10);
                Tween.to(foulImage, TweenAccessors.ImageAccessor.ROTATION, 1).target(0).start(tweenManager);
                Tween.to(foulImage, TweenAccessors.ImageAccessor.SCALE, 0.5f).target(1.05f).repeatYoyo(-1, 0).start(tweenManager);
                for (int i = 0; i < 3; ++i)
                    stars[i].setVisible(false);
            }

            timeBonusImage.setVisible(false);
            heartImage.setVisible(false);

            coachBackground.getColor().a = 0;
            coachImage.getColor().a = 0;
            Tween.to(coachImage, TweenAccessors.ImageAccessor.ALPHA, 0.5f).target(1).start(tweenManager);
            Tween.to(coachBackground, TweenAccessors.ImageAccessor.ALPHA, 0.3f).target(1).start(tweenManager);
            Gdx.input.setInputProcessor(ui);
        }

        void Hide(InputProcessor inputProcessor)
        {
            ResetStarRotations();
            tweenManager.killAll();
            SetVisible(false);
            Gdx.input.setInputProcessor(inputProcessor);
        }

        void Create()
        {

            NinePatch cb = new NinePatch(uiAtlas.findRegion("back"), 32, 32, 32, 32);
            coachBackground = new Image(cb);
            coachBackground.setWidth(ui.getWidth() - 70 - 70);
            coachBackground.setHeight(160);
            coachBackground.setPosition((ui.getWidth() - coachBackground.getWidth()) / 2, ui.getHeight() * 0.05f);
            ui.addActor(coachBackground);

            coinsIcon = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "coins"));
            coinsIcon.setPosition(ui.getWidth() - coinsIcon.getWidth(), ui.getHeight() - coinsIcon.getHeight());
            ui.addActor(coinsIcon);
            coisAmountLabel = new Label("0", uiSkin, "medium");
            coisAmountLabel.setAlignment(Align.right, Align.top);
            coisAmountLabel.setWidth(100);
            ui.addActor(coisAmountLabel);

            fansIcon = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "fans_menu"));
            fansIcon.setPosition(ui.getWidth() - fansIcon.getWidth(), coinsIcon.getY() - fansIcon.getHeight());
            ui.addActor(fansIcon);
            fansAmountLabel = new Label("0", uiSkin, "medium");
            fansAmountLabel.setAlignment(Align.left, Align.top);
            fansAmountLabel.setWidth(100);
            ui.addActor(fansAmountLabel);

            coachImage = new Image(uiAtlas.findRegion("coach_lose"));
            coachImage.setPosition(coachBackground.getX() + 32, coachBackground.getY() - (coachImage.getHeight() - coachBackground.getHeight()) / 2);
            ui.addActor(coachImage);

            goalText = new Label("LONG LONG LONG LONG LONG LONG LONG", uiSkin, "medium");
            goalText.setWrap(true);
            goalText.setWidth(coachBackground.getWidth() - coachImage.getHeight() - 32);
            goalText.setAlignment(Align.left, Align.center);
            goalText.setPosition(coachImage.getX() + coachImage.getWidth(), coachImage.getY() + coachImage.getHeight() / 2 - goalText.getHeight() / 2);
            ui.addActor(goalText);


            finalScore = new Label("000/000", uiSkin, "medium");

            replayImage = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "replay"));
            replayImage.addListener(new ClickListener()
            {
                public void clicked(InputEvent event, float x, float y)
                {
                    player.Restart();
                    Restart();
                    Gdx.input.setInputProcessor(main.engine.input);
                }
            });
            replayImage.setOrigin(replayImage.getWidth() / 2, replayImage.getHeight() / 2);
            replayImage.setPosition(2, coachBackground.getY());
            ui.addActor(replayImage);

            nextLevelButton = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "next_level"));
            nextLevelButton.setPosition(ui.getWidth() - nextLevelButton.getWidth() - 2, coachBackground.getY());
            ui.addActor(nextLevelButton);

            homeButton = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "home"));
            homeButton.setPosition(ui.getWidth() - homeButton.getWidth() - 2, nextLevelButton.getY() + nextLevelButton.getHeight() + 10);
            homeButton.addListener(new ClickListener()
            {
                public void clicked(InputEvent event, float x, float y)
                {
                    main.ChangeGameState(GameState.MainMenu, null);
                }
            });
            ui.addActor(homeButton);


            foulImage = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "foul"));
            foulImage.setPosition(coachBackground.getX() + coachBackground.getWidth() / 2 - foulImage.getWidth() / 2, coachBackground.getY() + coachBackground.getHeight() + 5);
            foulImage.setVisible(false);
            foulImage.setOrigin(foulImage.getWidth() / 2, foulImage.getHeight() / 2);
            ui.addActor(foulImage);

            stars = new Image[3];

            for (int i = 0; i < 3; ++i)
                stars[i] = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "star_result_empty"));

            stars[1].setPosition(coachBackground.getX() + coachBackground.getWidth() / 2 - stars[1].getWidth() / 2, coachBackground.getY() + coachBackground.getHeight() + 10);
            stars[0].setPosition(stars[1].getX() - stars[1].getWidth(), stars[1].getY() - 10);
            stars[2].setPosition(stars[1].getX() + stars[1].getWidth(), stars[1].getY() - 10);

            ui.addActor(stars[0]);
            ui.addActor(stars[1]);
            ui.addActor(stars[2]);


            timeBonusImage = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "time_bonus"));
            timeBonusImage.setPosition(coachBackground.getX() + coachBackground.getWidth() / 2 - timeBonusImage.getWidth() / 2, stars[0].getY() + stars[0].getHeight() + 5);
            timeBonusImage.setVisible(false);
            timeBonusImage.setOrigin(timeBonusImage.getWidth() / 2, timeBonusImage.getHeight() / 2);
            ui.addActor(timeBonusImage);

            heartImage = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "heart_large"));
            heartImage.setOrigin(heartImage.getWidth() / 2, heartImage.getHeight() / 2);
            heartImage.setPosition( ui.getWidth() / 2 - heartImage.getWidth() / 2, ui.getHeight() / 2 - heartImage.getHeight() / 2 );
            heartImage.setVisible(false);
            ui.addActor(heartImage);

            fansAmountLabel.setPosition(fansIcon.getX() + fansIcon.getWidth(), fansIcon.getY());
            coisAmountLabel.setPosition(coinsIcon.getX() - coisAmountLabel.getWidth(), coinsIcon.getY());

            Hide(main.engine.input);
        }

        void Destroy()
        {

        }
    }


    public Gameplay(Spartans main, ResourceGroup common)
    {
        super();
        bonusesEnded = true;
        timeTillBonusesEnd = 0f;
        groundSounds = new ArrayList<GroundSounds>();
        currentLevelStats = new CurrentLevelStats();
        scoreTable = new ScoreTable();
        commonResources = common;
        state = State.Pause;
        gameStarted = false;
        finishedEarly = false;
        this.main = main;
        heartImages = new Image[3];
        ballTargets = new ArrayList<BallTarget>();
        enemies = new ArrayList<Enemy>();
        collectibles = new ArrayList<Collectible>();
    }

    public void FillGroundSounds()
    {
        GroundSounds sounds = new GroundSounds();
        sounds.leftFoot = commonResources.GetSound("data/sounds/common/grass_leftfoot.wav");
        sounds.rightFoot = commonResources.GetSound("data/sounds/common/grass_rightfoot.wav");
        sounds.landing = commonResources.GetSound("data/sounds/common/grass_landing.wav");
        sounds.slide = commonResources.GetSound("data/sounds/common/grass_slide.wav");
        groundSounds.add(sounds);
    }

    public int GetGroundType()
    {
        //TODO: find real ground type
        return 0;
    }

    public Sound GetLeftFootstepSound()
    {
        return groundSounds.get(GetGroundType()).leftFoot;
    }

    public Sound GetRightFootstepSound()
    {
        return groundSounds.get(GetGroundType()).rightFoot;
    }

    public Sound GetLandingSound()
    {
        return groundSounds.get(GetGroundType()).landing;
    }

    public Sound GetSlideSound()
    {
        return groundSounds.get(GetGroundType()).slide;
    }

    // obje nin zindex ini degistirir. Basarirsa bi onceki zindex i donurur,
    // basaramazsa -1 dondurur.
    // DIKKAT! bu baya yavas bi fonksiyon, sikca kullanma
    public int ChangeZIndex(StaticEntity entity, int newZIndex)
    {
        int currentZIndex = -1;

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<StaticEntity> arr = staticEntities.get(i);
            for (int j = 0; j < 10; ++j)
            {
                if (arr.get(j) == entity)
                {
                    // found it, remove it!
                    arr.remove(j);
                    currentZIndex = i;
                    i = 10;
                    break;
                }
            }
        }

        if (currentZIndex != -1)
        {
            staticEntities.get(newZIndex).add(entity);
        }

        return currentZIndex;
    }

    // obje nin zindex ini degistirir. Basarirsa bi onceki zindex i donurur,
    // basaramazsa -1 dondurur.
    // DIKKAT! bu baya yavas bi fonksiyon, sikca kullanma
    public int ChangeZIndex(AnimatedEntity entity, int newZIndex)
    {
        int currentZIndex = -1;

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<AnimatedEntity> arr = animatedEntities.get(i);
            for (int j = 0; j < 10; ++j)
            {
                if (arr.get(j) == entity)
                {
                    // found it, remove it!
                    arr.remove(j);
                    currentZIndex = i;
                    i = 10;
                    break;
                }
            }
        }

        if (currentZIndex != -1)
        {
            animatedEntities.get(newZIndex).add(entity);
        }

        return currentZIndex;
    }

    public float GetGroundHeight()
    {
        return groundHeight;
    }

    public void SetPauseUI(boolean isPaused)
    {

        scoreText.setVisible(!isPaused);
        pauseButton.setVisible(!isPaused);
        playButton.setVisible(isPaused);
        restartButton.setVisible(isPaused);
        exitButton.setVisible(isPaused);
        for (int i = 0; i < 3; ++i)
            heartImages[i].setVisible(!isPaused);
    }

    public void CompleteLevel(GameStats gameStats, boolean scoredGoal, boolean success)
    {
        CreateScoreTableUI(gameStats, scoredGoal, success);
        gameStats.SaveToDisk(levelData);
    }

    public void SetNewScore(int newScore)
    {
        String s = String.valueOf(newScore);
        scoreText.setText("SCORE: " + s);
    }

    void CreateGoalPost(SkeletonRenderer renderer)
    {
        goalLine = info.kalePosX;

        if (info.hasGoalKeeper)
        {
            goalKeeper = new GoalKeeper(info.kalePosX - 1, 0.9f, renderer, resources);
            enemies.add(goalKeeper);
            Add(goalKeeper.entity, 8);
        }

        StaticEntity staticEntity = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "kale"), 2, 4);
        staticEntity.x = info.kalePosX;
        staticEntity.y = 0.9f;
        staticEntity.paddingX = 0;
        staticEntity.paddingY = 0;
        Add(staticEntity, 8); // over ground
    }

    public void LoseAHeart()
    {
        for (int i = heartImages.length - 1; i >= 0; --i)
        {
            if (((TextureRegionDrawable) heartImages[i].getDrawable()).getRegion() == heartTextureRegion)
            {
                ((TextureRegionDrawable) heartImages[i].getDrawable()).setRegion(heartEmptyTextureRegion);
                return;
            }
        }
    }

    void CreateEnemies(SkeletonRenderer renderer, EffectManager effectManager)
    {
        for (LevelInfo.Enemy enemy : info.enemies)
        {

            if (enemy.enemyType.equals("sliding"))
            {
                // EnemySlidings kendisini updater.ballTargets in icine alir
                EnemySliding newEnemy = new EnemySliding(renderer, resources, effectManager, enemy, GetGroundHeight());
                ballTargets.add(newEnemy);
                enemies.add(newEnemy);

                Add(newEnemy.entity, enemy.zIndex);
            }
            else if (enemy.enemyType.equals("headbutter"))
            {
                // EnemySlidings kendisini updater.ballTargets in icine alir
                EnemyHeadButter newEnemy = new EnemyHeadButter(effectManager, renderer, resources, enemy, GetGroundHeight());

                ballTargets.add(newEnemy);
                enemies.add(newEnemy);

                Add(newEnemy.entity, enemy.zIndex);
            }
            else if (enemy.enemyType.equals("dirtpatch"))
            {
                DirtPatch dirtPatch = new DirtPatch(resources, effectManager, this, enemy);
                enemies.add(dirtPatch);
            }
            else if (enemy.enemyType.equals("hurdlejump"))
            {
                HurdleJump hurdleJump = new HurdleJump(effectManager, this, resources, enemy);
                enemies.add(hurdleJump);
            }
            else if (enemy.enemyType.equals("hurdleslide"))
            {
                HurdleSlide slide = new HurdleSlide(effectManager, resources, this, enemy);
                enemies.add(slide);
            }
            else
            {
                Log.Print("Error: there is no type of enemy called: " + enemy.enemyType);
            }

        }
    }

    void CreateBackground()
    {
        background = new StaticEntity(resources.GetTextureRegion(info.background.backgroundAtlasName, info.background.backgroundImageName), info.background.widthMeters, info.background.heightMeters);
        background.x = info.background.backgroundImageX;
        background.y = info.background.backgroundImageY;

        for (LevelInfo.StaticDecor decor : info.background.staticDecors)
        {
            StaticEntity staticEntity = new StaticEntity(resources.GetTextureRegion(decor.atlasPath, decor.image), decor.widthMeters, decor.heightMeters);
            staticEntity.SetTransform(decor);
            staticEntity.x = 0;
            staticEntity.y = 0;
            staticEntity.paddingX = decor.x;
            staticEntity.paddingY = decor.y;
            AddBackground(staticEntity, decor.zIndex); // over ground
        }

        for (LevelInfo.AnimatedDecor decor : info.background.animatedDecors)
        {
            // this xml over and over again
            AnimatedEntityInfo animInfo = resources.GetAnimatedEntityInfo(decor.animFilePath);
            AnimatedEntity animatedEntity = new AnimatedEntity(resources.GetAtlas(animInfo.atlasName), resources.defaultRegion, animInfo, decor.width, decor.height, decor.widthMeters, decor.heightMeters);
            animatedEntity.SetTransform(decor);
            animatedEntity.x = 0;
            animatedEntity.y = 0;
            animatedEntity.paddingX = decor.x;
            animatedEntity.paddingY = decor.y;
            AddBackground(animatedEntity, decor.zIndex);
        }
    }

    // bolum zeminin olusturur
    void CreateGround()
    {
        TextureRegion region = resources.GetTextureRegion(info.ground.atlasPath, info.ground.imagePath);
        for (int i = -10; i < info.ground.repeat; ++i)
        {
            StaticEntity ground = new StaticEntity(region, info.ground.widthMeters, info.ground.heightMeters);
            ground.height = info.ground.height;
            ground.width = info.ground.width;
            ground.x = i * (ground.width) / Renderer.PixelPerMeter;
            Add(ground, 9);
        }
    }

    // animasyonlu olmayan decorlari olusturur
    void CreateStaticDecors()
    {
        for (LevelInfo.StaticDecor decor : info.staticDecors)
        {
            //StaticEntity staticEntity = new StaticEntity(textureLoader, decor.atlasPath, decor.image, decor.widthMeters, decor.heightMeters);
            StaticEntity staticEntity = new StaticEntity(resources.GetTextureRegion(decor.atlasPath, decor.image), decor.widthMeters, decor.heightMeters);
            staticEntity.SetTransform(decor);
            Add(staticEntity, decor.zIndex); // over ground
        }
    }

    void CreateAnimatedDecors()
    {
        for (LevelInfo.AnimatedDecor decor : info.animatedDecors)
        {
            // this xml over and over again
            AnimatedEntityInfo animInfo = resources.GetAnimatedEntityInfo(decor.animFilePath);

            // check common resources too
            if (animInfo == null)
                animInfo = commonResources.GetAnimatedEntityInfo(decor.animFilePath);

            AnimatedEntity animatedEntity = new AnimatedEntity(resources.GetAtlas(animInfo.atlasName), resources.defaultRegion, animInfo, decor.width, decor.height, decor.widthMeters, decor.heightMeters);
            animatedEntity.SetTransform(decor);
            animatedEntity.SetAnimation("default");
            Add(animatedEntity, decor.zIndex); // over ground
        }
    }

    void CreateStartScreen()
    {
        NinePatch np = new NinePatch(uiAtlas.findRegion("back"), 33, 33, 33, 33);
        startLevelBackground = new Image(np);
        startLevelBackground.setWidth(ui.getWidth() * 0.9f);
        startLevelBackground.setHeight(ui.getHeight() * 0.9f);
        startLevelBackground.setPosition(ui.getWidth() * 0.05f, ui.getHeight() * 0.05f);
        ui.addActor(startLevelBackground);

        startLevelTable = new Table();

        Image coachPicture = new Image(uiAtlas.findRegion("coach"));
        startLevelTable.add(coachPicture).width(140).height(140).center();

        Label levelIntroductionTest = new Label(startingText, uiSkin, "medium");
        levelIntroductionTest.setWrap(true);
        levelIntroductionTest.setBounds(0, 0, startLevelBackground.getWidth() - 10 - 64, 140);
        startLevelTable.add(levelIntroductionTest).width(startLevelBackground.getWidth() - 10 - 140).center();
        startLevelTable.row();

        startLevelTable.add().colspan(2).height(100);
        startLevelTable.row();

        Label startLevelText = new Label("Click to Start!", uiSkin, "medium");
        startLevelText.setColor(0.8f, 0, 0, 1);
        startLevelTable.add(startLevelText).colspan(2).center();
        startLevelTable.row();

        ui.addActor(startLevelTable);
        startLevelTable.setPosition(ui.getWidth() / 2, ui.getHeight() / 2);
    }

    void CreateUI(SpriteBatch batch)
    {
        ui = new Stage(800, 480, true, batch);

        scoreText = new Label("SCORE: 0", uiSkin, "medium");
        scoreText.setAlignment(Align.left);
        scoreText.setWidth(400);
        scoreText.setVisible(false);
        ui.addActor(scoreText);
        scoreText.setPosition(0, ui.getHeight() - scoreText.getHeight() + 3); // 3 font olusturulurken ustune biraz bosluk birakiliyo o yuzden

        heartTextureRegion = commonResources.GetTextureRegion("data/common/common.atlas", "heart");
        heartEmptyTextureRegion = commonResources.GetTextureRegion("data/common/common.atlas", "heart_empty");
        heartImages[0] = new Image(heartTextureRegion);
        heartImages[0].setPosition(2, scoreText.getY() - heartImages[0].getHeight() - 2);
        heartImages[0].setVisible(false);
        ui.addActor(heartImages[0]);

        heartImages[1] = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "heart"));
        heartImages[1].setPosition(heartImages[0].getX() + heartImages[0].getWidth() + 2, scoreText.getY() - heartImages[0].getHeight() - 2);
        heartImages[1].setVisible(false);
        ui.addActor(heartImages[1]);

        heartImages[2] = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "heart"));
        heartImages[2].setPosition(heartImages[1].getX() + heartImages[0].getWidth() + 2, scoreText.getY() - heartImages[0].getHeight() - 2);
        heartImages[2].setVisible(false);
        ui.addActor(heartImages[2]);

        pauseButton = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "pause"));
        ui.addActor(pauseButton);
        pauseButton.setPosition(ui.getWidth() - pauseButton.getWidth() - 3, ui.getHeight() - pauseButton.getHeight() - 3);
        pauseButton.setVisible(false);

        playButton = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "play"));
        playButton.setWidth(128);
        playButton.setHeight(128);
        ui.addActor(playButton);
        playButton.setPosition(ui.getWidth() / 2 - (playButton.getWidth() / 2), ui.getHeight() / 2 - (playButton.getHeight() / 2));
        playButton.setVisible(false);


        restartButton = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "replay"));
        restartButton.setWidth(64);
        restartButton.setHeight(64);
        ui.addActor(restartButton);
        restartButton.setPosition(ui.getWidth() / 2 - (restartButton.getWidth() / 2) + 200, ui.getHeight() / 2 - (restartButton.getHeight() / 2));
        restartButton.setVisible(false);

        exitButton = new Image(commonResources.GetTextureRegion("data/common/common.atlas", "exit"));
        exitButton.setWidth(64);
        exitButton.setHeight(64);
        ui.addActor(exitButton);
        exitButton.setPosition(ui.getWidth() / 2 - (playButton.getWidth() / 2) - 200, ui.getHeight() / 2 - (exitButton.getHeight() / 2));
        exitButton.setVisible(false);

        CreateStartScreen();
    }

    public void SetIngameUIStatus(boolean status)
    {
        scoreText.setVisible(status);
        for (int i = 0; i < 3; ++i)
            heartImages[i].setVisible(status);
        pauseButton.setVisible(status);
    }

    public int GetNeededPointsToNext(int currentStar)
    {
        switch (currentStar)
        {
            case 0:
                return (int) (maxPoints * 0.2f);
            case 1:
                return (int) (maxPoints * 0.3f);
            case 2:
                return (int) (maxPoints * 0.5f);
            case 3:
                return maxPoints;
            default:
                return 0;
        }
    }

    public int GetMinimumRequiredPoints(int star)
    {
        switch (star)
        {
            case 0:
                return 0;
            case 1:
                return (int) (maxPoints * 0.2f);
            case 2:
                return (int) (maxPoints * 0.5f);
            case 3:
                return maxPoints;
            default:
                return 0;
        }
    }


    public void CreateScoreTableUI(GameStats gameStats, boolean scoredGoal, boolean success)
    {
        pauseButton.setVisible(false);


        Log.Print("[End] timeSinceLevelStart: " + currentLevelStats.timeSinceLevelStart);
        if (currentLevelStats.timeSinceLevelStart <= timeToComplete)
        {
            timeTillBonusesEnd += 0.5f;
            finishedEarly = true;
        }

        timeTillBonusesEnd += currentLevelStats.lives * 0.5f;
        if (timeTillBonusesEnd > 0)
            bonusesEnded = false;

        state = State.EndLevel;

        int previousStars = levelData.stars;
        int numOfStars;
        if (currentLevelStats.currentPoints >= GetMinimumRequiredPoints(3))
            numOfStars = 3;
        else if (currentLevelStats.currentPoints >= GetMinimumRequiredPoints(2))
            numOfStars = 2;
        else if (currentLevelStats.currentPoints >= GetMinimumRequiredPoints(1))
            numOfStars = 1;
        else
            numOfStars = 0;


        int newlyGainedStars = numOfStars - previousStars;
        if (success && newlyGainedStars > 0)
            levelData.stars = numOfStars;

        int newFans = 0;
        if (newlyGainedStars == 1)
            newFans = fansGained1;
        else if (newlyGainedStars == 2)
            newFans = fansGained1 + fansGained2;
        else if (newlyGainedStars == 3)
            newFans = fansGained1 + fansGained2 + fansGained3;

        if (!success)
            newFans = 0;

        fansEarned = newFans;

        gameStats.UpdateLevelDatas();

        if (!success)
            numOfStars = 0;
        starsEarned = numOfStars;

        int newMoney = 0;
        if (starsEarned == 1)
            newMoney = moneyGained1;
        else if (starsEarned == 2)
            newMoney = moneyGained1 + moneyGained2;
        else if (starsEarned == 3)
            newMoney = moneyGained1 + moneyGained2 + moneyGained3;

        if (!success)
            newMoney = 0;

        moneyEarned = newMoney;

        state = State.EndLevel;

        if (success)
            scoreTable.SetSuccess();
        else
            scoreTable.SetFailed();

        scoreTable.Reset();
        scoreTable.Show(success);

        gameStats.AddFans(newFans);
        gameStats.AddMoney(newMoney);
        if (success)
            gameStats.AddMoney(currentLevelStats.collectedCoins);
        main.gameStats.UpdateLevelDatas();

        scoreTable.SetupNextLevelButton();
        scoreTable.UpdateFanAndCoinAmounts(main.gameStats.GetFans(), main.gameStats.GetMoney());
        // bu bolum puanini sifirlar
        gameStats.NewLevelStarted();

    }

    public ResourceGroup LoadFiles(String levelName, TextureRegion defaultRegion, EffectManager effectManager)
    {
        long startTime = System.nanoTime();

        levelData = main.gameStats.levelDatas.get(levelName);
        uiAtlas = new TextureAtlas(Gdx.files.internal("data/ui/level/level.pack"));
        uiAtlas.getTextures().iterator().next().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        uiSkin = new Skin(Gdx.files.internal("data/ui/level/skin.json"));

        info = new LevelInfo();

        long parseStart = System.nanoTime();
        info.Load(levelData.filePath);
        Log.Print("Parse Time: " + (System.nanoTime() - parseStart) / 1000000 + "ms");

        resources = main.engine.resourceManager.GetResourceGroup(info.resourceGroupName);

        // son yuklenen bolum bu bolumle ayni degilse, onceki leveli kaldir
        if (resources != main.lastLoadedResourceGroup)
        {
            if (main.lastLoadedResourceGroup != null)
                main.lastLoadedResourceGroup.Destroy();
        }

        startingText = info.startingText;
        endingText = info.endingText;
        failedText = info.failedText;

        if (!resources.isLoaded)
        {
            resources = main.engine.resourceManager.LoadResourceGroup(effectManager, "urban_green");
            main.lastLoadedResourceGroup = resources;
            resources.SetTextureFilterForAll(TextureFilter.Linear);
        }

        long endTime = System.nanoTime();
        if (Log.isLogging)
        {
            long dur = endTime - startTime;
            Log.Print("Level Assets Loaded (" + dur / 1000000 + "ms) ");
        }

        FillGroundSounds();

        groundHeight = info.startY;
        maxPoints = info.maxPoints;
        timeToComplete = info.timeToComplete;
        fansGained1 = info.fansGained1;
        fansGained2 = info.fansGained2;
        fansGained3 = info.fansGained3;
        moneyGained1 = info.moneyGained1;
        moneyGained2 = info.moneyGained2;
        moneyGained3 = info.moneyGained3;

        return resources;
    }

    public void CreateCollectibles(EffectManager effectManager, Gameplay currentGameplay)
    {
        int size = info.collectibles.size();
        collectibles.ensureCapacity(size);
        for (int i = 0; i < size; ++i)
        {
            LevelInfo.Collectible coll = info.collectibles.get(i);

            if (coll.type.equals("drink"))
            {
                SpeedBoost drink = new SpeedBoost(commonResources, effectManager, currentGameplay, coll);
                collectibles.add(drink);
            }
            else if (coll.type.equals("coin"))
            {
                Coin coin = new Coin(commonResources, player, currentGameplay, effectManager, coll);
                collectibles.add(coin);
            }

        }
    }


    public void Create(SpriteBatch batch, SkeletonRenderer skeletonRenderer, EffectManager effectManager)
    {
        CreateBackground();
        CreateGround();
        CreateStaticDecors();
        CreateAnimatedDecors();
        CreateEnemies(skeletonRenderer, effectManager);
        CreateCollectibles(effectManager, this);
        CreateGoalPost(skeletonRenderer);
        CreateUI(batch);
        scoreTable.Create();
        // nulify, so GC collects it
        info = null;

        main.engine.input.AddTouchUpEvent(this);
    }

    public void Restart()
    {
        scoreTable.Hide(main.engine.input);
        main.gameStats.NewLevelStarted();
        RestartUI();
        for (Enemy enemy : enemies)
        {
            enemy.Restart();
        }

        int size = collectibles.size();
        for (int i = 0; i < size; ++i)
            collectibles.get(i).Restart();

        for (int i = 0; i < heartImages.length; ++i)
            ((TextureRegionDrawable) heartImages[i].getDrawable()).setRegion(heartTextureRegion);

        if (goalKeeper != null)
            goalKeeper.Restart();


        timeSinceScoreTableStarted = 0;
        timeTillBonusesEnd = 0f;
        scoreTableTimer = 0f;
        finishedEarly = false;
        bonusesEnded = true;
        starsShown = 0;
        starsEarned = 0;
        moneyEarned = 0;
        fansEarned = 0;
        player.Restart();
        player.LevelStartedCallback();
        main.cameraManager.Reset();
        state = State.Ingame;
        currentLevelStats.Reset();
    }

    public void RestartUI()
    {
        SetIngameUIStatus(true);
        SetNewScore(0);
    }

    public void Destroy(EffectManager effectManager)
    {
        main.engine.input.RemoveTouchUpEvent(this);

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<StaticEntity> e = backgroundStaticEntities.get(i);
            int size = e.size();
            for (int j = 0; j < size; ++j)
            {
                e.get(j).Destroy();
            }
        }
        backgroundStaticEntities.clear();

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<AnimatedEntity> e = backgroundAnimatedEntities.get(i);
            int size = e.size();
            for (int j = 0; j < size; ++j)
            {
                e.get(j).Destroy();
            }
        }
        backgroundAnimatedEntities.clear();

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<StaticEntity> e = staticEntities.get(i);
            int size = e.size();
            for (int j = 0; j < size; ++j)
            {
                e.get(j).Destroy();
            }
        }
        staticEntities.clear();

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<AnimatedEntity> e = animatedEntities.get(i);
            int size = e.size();
            for (int j = 0; j < size; ++j)
            {
                e.get(j).Destroy();
            }
        }
        animatedEntities.clear();

        ballTargets.clear();
        enemies.clear();

        effectManager.ClearEffects();

        if (ui != null)
            ui.dispose();
        if (uiAtlas != null)
            uiAtlas.dispose();
        if (uiSkin != null)
            uiSkin.dispose();

        resources = null;
        info = null;
    }


    // her frame de caliriliyor. her dusmanin Update fonksiyonunu cagirir
    public void Update(float timePassed, CameraManager cameraManager, Player player)
    {

        Update(timePassed, cameraManager);

        scoreTable.Update(timePassed);

        int size = enemies.size();
        for (int i = 0; i < size; ++i)
        {
            if (enemies.get(i).isActive)
            {
                enemies.get(i).Update(timePassed, player);
            }
        }

        size = collectibles.size();
        for (int i = 0; i < size; ++i)
        {
            collectibles.get(i).Update(timePassed, player);
        }

        switch (state)
        {
            case Ingame:
                if (!main.isPaused)
                {
                    currentLevelStats.timeSinceLevelStart += timePassed;
                }
                break;
            case EndLevel:
                timeSinceScoreTableStarted += timePassed;
                scoreTableTimer += timePassed;
                if (bonusesEnded)
                {

                    if (scoreTableTimer > 0.5f)
                    {
                        if (starsShown < starsEarned)
                        {
                            Vector2 v = Engine.vector2Pool.obtain(scoreTable.stars[starsShown].getX() + scoreTable.stars[starsShown].getWidth() / 2, scoreTable.stars[starsShown].getY() + scoreTable.stars[starsShown].getHeight() / 2);
                            ui.stageToScreenCoordinates(v);
                            EffectParticle ten = (EffectParticle) main.engine.renderer.effectManager.NewParticleEffect("data/effects/yellow_explosion.p", main.cameraManager.camera);
                            ten.lifeTime = 3;
                            ten.SetDrawType(Effect.DrawType.Screen);
                            ten.SetPos(v.x, v.y);
                            ((TextureRegionDrawable) scoreTable.stars[starsShown].getDrawable()).setRegion(commonResources.GetTextureRegion("data/common/common.atlas", "star_result"));
                            Engine.vector2Pool.free(v);

                            commonResources.GetSound("data/sounds/common/fireworks.wav").play();

                            if (starsShown == 2)
                                commonResources.GetSound("data/sounds/common/applause.wav").play();

                            currentLevelStats.currentPoints -= GetNeededPointsToNext(starsShown);
                            scoreText.setText("SCORE: " + currentLevelStats.currentPoints);
                        }

                        starsShown++;
                        scoreTableTimer = 0f;
                    }
                }
                else
                {
                    if (scoreTableTimer > 0.5f)
                    {
                        if (finishedEarly)
                        {
                            finishedEarly = false;
                            currentLevelStats.currentPoints += maxPoints * 0.25f;
                            scoreText.setText("SCORE: " + currentLevelStats.currentPoints);

                            scoreTable.timeBonusImage.setVisible(true);
                            scoreTable.timeBonusImage.setScale(0.5f);
                            Tween.to(scoreTable.timeBonusImage, TweenAccessors.ImageAccessor.POSITION_Y, 1f).target(scoreTable.timeBonusImage.getY() + 150).start(scoreTable.tweenManager);
                            Timeline.createSequence().
                                    push(Tween.to(scoreTable.timeBonusImage, TweenAccessors.ImageAccessor.SCALE, 0.7f).target(1.3f)).
                                    push(Tween.to(scoreTable.timeBonusImage, TweenAccessors.ImageAccessor.SCALE, 0.2f).target(0)).start(scoreTable.tweenManager);

                            commonResources.GetSound("data/sounds/common/level_bonus.wav").play();

                        }
                        else if (currentLevelStats.lives > 0)
                        {
                            currentLevelStats.currentPoints += maxPoints * 0.1f;
                            scoreText.setText("SCORE: " + currentLevelStats.currentPoints);
                            currentLevelStats.lives--;

                            if(currentLevelStats.lives == 2)
                            {
                                ((TextureRegionDrawable) heartImages[2].getDrawable()).setRegion( heartEmptyTextureRegion );
                            }
                            else if(currentLevelStats.lives == 1)
                            {
                                ((TextureRegionDrawable) heartImages[2].getDrawable()).setRegion( heartEmptyTextureRegion );
                                ((TextureRegionDrawable) heartImages[1].getDrawable()).setRegion( heartEmptyTextureRegion );
                            }
                            else
                            {
                                ((TextureRegionDrawable) heartImages[2].getDrawable()).setRegion( heartEmptyTextureRegion );
                                ((TextureRegionDrawable) heartImages[1].getDrawable()).setRegion( heartEmptyTextureRegion );
                                ((TextureRegionDrawable) heartImages[0].getDrawable()).setRegion( heartEmptyTextureRegion );
                            }

                            scoreTable.heartImage.setVisible(true);
                            scoreTable.heartImage.setScale(1f);
                            scoreTable.heartImage.setPosition( ui.getWidth() / 2 - scoreTable.heartImage.getWidth() / 2, ui.getHeight() / 2 - scoreTable.heartImage.getHeight() / 2 );
                            Timeline.createSequence().
                                    push(Tween.to(scoreTable.heartImage, TweenAccessors.ImageAccessor.POSITION_Y, 0.35f).target(scoreTable.heartImage.getY() + 100)).
                                    push(Tween.to(scoreTable.heartImage, TweenAccessors.ImageAccessor.SCALE, 0.15f).target(0))
                                    .start(scoreTable.tweenManager);

                            commonResources.GetSound("data/sounds/common/level_bonus.wav").play();
                        }
                        else
                            bonusesEnded = true;

                        scoreTableTimer = 0f;
                    }

                }

                break;
        }
    }

    @Override
    public boolean TouchUp(int screenX, int screenY, int pointer, int button)
    {
        if (!gameStarted)
        {
            gameStarted = true;
            player.SetMoving(true);
            startLevelTable.setVisible(false);
            startLevelBackground.setVisible(false);

            SetPauseUI(false);

            state = State.Ingame;
            player.LevelStartedCallback();
        }
        else
        {

            float widthPercentage = screenX / (float) Gdx.graphics.getWidth();
            float heightPercentage = screenY / (float) Gdx.graphics.getHeight();

            if (!main.isPaused)
            {
                // ekranin sag ustune basinca pause oluyor
                if (widthPercentage > 0.9f && heightPercentage < 0.1f)
                {
                    main.TogglePause();
                    state = State.Pause;
                }
            }
            else
            {
                Vector2 coord = ui.screenToStageCoordinates(new Vector2(screenX, screenY));

                if (coord.x > playButton.getX() && coord.x < (playButton.getX() + playButton.getWidth()) && coord.y > playButton.getY() && coord.y < (playButton.getY() + playButton.getHeight()))
                {
                    player.actions.ignoreNextAction = true;
                    main.TogglePause();
                    state = State.Ingame;
                }
                else if (coord.x > restartButton.getX() && coord.x < (restartButton.getX() + restartButton.getWidth()) && coord.y > restartButton.getY() && coord.y < (restartButton.getY() + restartButton.getHeight()))
                {
                    player.actions.ignoreNextAction = true;
                    main.TogglePause();
                    Restart();
                }
                else if (coord.x > exitButton.getX() && coord.x < (exitButton.getX() + exitButton.getWidth()) && coord.y > exitButton.getY() && coord.y < (exitButton.getY() + exitButton.getHeight()))
                {
                    player.actions.ignoreNextAction = true;
                    main.TogglePause();
                    main.ChangeGameState(GameState.MainMenu, null);
                }
            }
        }

        return false;
    }
}
