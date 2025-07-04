package com.spartans.ui;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.engine.*;
import com.spartans.CameraManager;
import com.spartans.Spartans;
import com.spartans.levels.Level;
import com.spartans.meta.GameStats;
import com.spartans.meta.LevelData;

public class MainMenu extends Level implements InputProcessor
{
    class Arrow implements TweenAccessor<Arrow>
    {
        boolean isVisible;
        Sprite image;

        @Override
        public int getValues(Arrow target, int tweenType, float[] returnValues)
        {
            returnValues[0] = image.getX();
            return 1;
        }

        @Override
        public void setValues(Arrow target, int tweenType, float[] newValues)
        {
            target.image.setX(newValues[0]);
        }

        Arrow()
        {
            isVisible = true;
        }

        void StartTween(float startX, float endX, float duration)
        {
            tweenManager.killTarget(this);
            Tween.to(this, 1, duration).target(startX, endX).ease(Expo.OUT).repeatYoyo(-1, 0).start(tweenManager);
        }

        void EndTween()
        {
            tweenManager.killTarget(this);
        }

        void Update(float timePassed)
        {
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;
            image.draw(batch);
        }

    }

    enum ScrollerState
    {
        None,
        ScrollingLeft,
        ScrollingRight
    }

    class Scroller
    {

        float scrollingSpeed;
        float distanceLeft;
        float targetX;
        CameraManager cameraManager;
        ScrollerState state;

        Scroller()
        {
            scrollingSpeed = 30;
            state = ScrollerState.None;
        }

        void HideUI()
        {
            fansDisplay.isVisible = false;
            coinsDisplay.isVisible = false;
            leftArrow.isVisible = false;
            rightArrow.isVisible = false;
        }

        void ShowUI()
        {
            fansDisplay.isVisible = true;
            coinsDisplay.isVisible = true;
            leftArrow.isVisible = true;
            rightArrow.isVisible = true;
        }

        void ScrollEnded()
        {
            ShowUI();
            state = ScrollerState.None;
            distanceLeft = 0;
            UpdateArrowStatus();
            UpdateArrowPosition();
            cameraManager.x = targetX;
        }

        void Update(float timePassed)
        {
            switch (state)
            {
                case None:
                    break;
                case ScrollingLeft:
                    if (distanceLeft > 0)
                    {
                        cameraManager.x -= timePassed * scrollingSpeed;
                        distanceLeft -= timePassed * scrollingSpeed;
                    }
                    else
                        ScrollEnded();

                    break;
                case ScrollingRight:
                    if (distanceLeft > 0)
                    {
                        cameraManager.x += timePassed * scrollingSpeed;
                        distanceLeft -= timePassed * scrollingSpeed;
                    }
                    else
                        ScrollEnded();
                    break;
            }
        }
    }

    class LevelBuyConfirmationAlert extends ConfirmationAlert
    {
        int levelId;

        LevelBuyConfirmationAlert(BitmapFont font)
        {
            super(font);
        }
    }

    class ConfirmationAlert
    {
        BitmapFont font;
        NinePatch background;
        String text;
        Sprite cancelButton;
        Sprite acceptButton;
        float width, height, x, y;
        float textWidth;
        boolean isVisible;

        ConfirmationAlert(BitmapFont font)
        {
            this.font = font;
            cancelButton = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "cancel"));
            cancelButton.setScale(0.7f);
            acceptButton = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "confirm"));
            background = new NinePatch(commonResources.GetTextureRegion("data/common/common.atlas", "back"), 33, 33, 33, 33);
        }

        void SetText(String text)
        {
            this.text = text;
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;

            background.draw(batch, x, y, width, height);
            hugeFont.draw(batch, text, x + width / 2 - textWidth / 2, y + height / 2 + hugeFont.getCapHeight() / 2);
            cancelButton.setPosition(x + width / 4 - cancelButton.getWidth() / 2, y + height / 4 - cancelButton.getHeight() / 2);
            cancelButton.draw(batch);
            acceptButton.setPosition(x + width * 0.75f - acceptButton.getWidth() / 2, y + height / 4 - acceptButton.getHeight() / 2);
            acceptButton.draw(batch);
        }
    }

    class NotEnoughCoinsAlert extends NotEnoughFansAlert
    {

    }

    class NotEnoughFansAlert
    {
        String text;

        float x, y, width, height;
        float textWidth;
        NinePatch background;

        boolean isVisible;

        NotEnoughFansAlert()
        {
            isVisible = false;
            background = new NinePatch(commonResources.GetTextureRegion("data/common/common.atlas", "back"), 33, 33, 33, 33);
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;

            width = Renderer.gameWorldSizeX * Renderer.PixelPerMeter * 0.8f;
            height = Renderer.gameWorldSizeY * Renderer.PixelPerMeter * 0.8f;
            x = 0.2f / 2 * Renderer.gameWorldSizeX * Renderer.PixelPerMeter + Renderer.gameWorldSizeX * Renderer.PixelPerMeter * (currentIndex / 8);
            y = 0.2f / 2 * Renderer.gameWorldSizeY * Renderer.PixelPerMeter;

            background.draw(batch, x, y, width, height);
            hugeFont.draw(batch, text, x + width / 2 - textWidth / 2, y + height / 2 + hugeFont.getCapHeight() / 2);
        }
    }

    class FansDisplay
    {
        boolean isVisible;
        Sprite image;
        String text;

        FansDisplay()
        {
            isVisible = true;
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;

            image.setX(Renderer.gameWorldSizeX * Renderer.PixelPerMeter * (currentIndex / 8));
            image.draw(batch);
            aldrich32Font.draw(batch, text, image.getX() + image.getWidth(), image.getY() + image.getHeight() - aldrich32Font.getLineHeight() / 4);
        }
    }

    class CoinsDisplay
    {
        boolean isVisible;
        Sprite image;
        String text;
        float textWidth;
        float textHeight;

        CoinsDisplay()
        {
            isVisible = true;
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;

            image.setX((Renderer.gameWorldSizeX * Renderer.PixelPerMeter * (currentIndex / 8)) + (Renderer.gameWorldSizeX * Renderer.PixelPerMeter) - image.getWidth());
            image.draw(batch);
            aldrich32Font.draw(batch, text, image.getX() - textWidth, image.getY() + image.getHeight() - textHeight / 4);
        }
    }

    class LockFans
    {
        StaticEntity landscape;
        Sprite lockImage;
        Sprite fansImage;
        String text;
        float textWidth;
        boolean isVisible;

        LockFans()
        {
            isVisible = true;
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;

            float width = textWidth + fansImage.getWidth();
            lockImage.setPosition(landscape.x * Renderer.PixelPerMeter + landscape.widthMeters * 0.5f * Renderer.PixelPerMeter - lockImage.getWidth() / 2, landscape.y * Renderer.PixelPerMeter + landscape.heightMeters * Renderer.PixelPerMeter * 0.95f - lockImage.getHeight());
            fansImage.setPosition(landscape.x * Renderer.PixelPerMeter + landscape.widthMeters * Renderer.PixelPerMeter / 2 - width / 2, landscape.y * Renderer.PixelPerMeter + (landscape.heightMeters * Renderer.PixelPerMeter - lockImage.getHeight() - fansImage.getHeight()) / 2);
            fansImage.draw(batch);
            lockImage.draw(batch);
            bitmapFont.setColor(1, 0, 0, 1);
            bitmapFont.draw(batch, text, fansImage.getX() + fansImage.getWidth() * fansImage.getScaleX(), fansImage.getY() + fansImage.getHeight() / 2 + bitmapFont.getCapHeight() / 2);
            bitmapFont.setColor(1, 1, 1, 1);
        }
    }

    class LockCoins
    {
        StaticEntity landscape;
        Sprite lockImage;
        Sprite coinsImage;
        String text;
        float textWidth;
        boolean isVisible;

        LockCoins()
        {
            isVisible = false;
        }

        void Draw(SpriteBatch batch)
        {
            if (!isVisible)
                return;
            float width = textWidth + coinsImage.getWidth();
            lockImage.setPosition(landscape.x * Renderer.PixelPerMeter + landscape.widthMeters * 0.5f * Renderer.PixelPerMeter - lockImage.getWidth() / 2, landscape.y * Renderer.PixelPerMeter + landscape.heightMeters * Renderer.PixelPerMeter * 0.95f - lockImage.getHeight());
            coinsImage.setPosition(landscape.x * Renderer.PixelPerMeter + landscape.widthMeters * Renderer.PixelPerMeter / 2 - width / 2, landscape.y * Renderer.PixelPerMeter + ((landscape.heightMeters * Renderer.PixelPerMeter - lockImage.getHeight()) - coinsImage.getHeight()) / 2);
            coinsImage.draw(batch);
            lockImage.draw(batch);
            bitmapFont.setColor(1, 0, 0, 1);
            bitmapFont.draw(batch, text, coinsImage.getX() + coinsImage.getWidth() * coinsImage.getScaleX(), coinsImage.getY() + coinsImage.getHeight() / 2 + bitmapFont.getCapHeight() / 2);
            bitmapFont.setColor(1, 1, 1, 1);
        }
    }

    class LevelText
    {
        float x, y;
        String text;
        boolean isVisible;

        LevelText()
        {
            isVisible = true;
        }
    }

    class LevelSelectionBox
    {
        float x, y;
        float width, height;
        int currentStars;
        TextureRegion emptyStarRegion, starRegion;
        StaticEntity stars[];
        LevelText levelText;
        StaticEntity landscapePicture;
        LockFans lockFans;
        LockCoins lockCoins;
        LevelData levelData;

        LevelSelectionBox(LevelData levelData)
        {
            currentStars = 0;
            this.levelData = levelData;
            stars = new StaticEntity[3];
            if (!levelData.isUnlocked)
            {
                if (!levelData.hasEnoughFans)
                    lockFans = new LockFans();
                else
                    lockCoins = new LockCoins();
            }
        }

        void HideAnimation()
        {
            if (lockFans != null)
                lockFans.isVisible = false;
            if (lockCoins != null)
                lockCoins.isVisible = false;
            stars[0].isVisible = false;
            stars[1].isVisible = false;
            stars[2].isVisible = false;

            levelText.isVisible = false;
            Tween.to(landscapePicture, TweenAccessors.StaticEntityPositionAccessor.HEIGHT, (float) Utility.RandomDouble(0.3f, 0.5f)).
                    target(0).
                    ease(Cubic.OUT).
                    start(tweenManager);
        }

        void ShowAnimation()
        {
            if (lockFans != null)
                lockFans.isVisible = true;
            if (lockCoins != null)
                lockCoins.isVisible = true;
            stars[0].isVisible = true;
            stars[1].isVisible = true;
            stars[2].isVisible = true;
            levelText.isVisible = true;
            Tween.to(landscapePicture, TweenAccessors.StaticEntityPositionAccessor.HEIGHT, (float) Utility.RandomDouble(0.3f, 0.5f)).
                    target(height).
                    ease(Cubic.OUT).
                    start(tweenManager);
        }

        void UpdateLockStatus()
        {
            if (levelData.isUnlocked)
            {
                lockFans = null;
            }
        }

        void Create()
        {
            CreateLandscapePicture();
            CreateStars();
            CreateLevelText();
            CreateLockImages();
        }

        void CreateLandscapePicture()
        {
            landscapePicture = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", levelData.landscape), width, height);
            landscapePicture.x = x;
            landscapePicture.y = y;
            Add(landscapePicture, 1);
        }

        void CreateStars()
        {
            currentStars = levelData.stars;
            emptyStarRegion = commonResources.GetTextureRegion("data/common/common.atlas", "star_result_empty");
            starRegion = commonResources.GetTextureRegion("data/common/common.atlas", "star_result");
            if (levelData.stars == 0)
            {
                stars[0] = new StaticEntity(emptyStarRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[1] = new StaticEntity(emptyStarRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[2] = new StaticEntity(emptyStarRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
            }
            else if (levelData.stars == 1)
            {
                stars[0] = new StaticEntity(starRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[1] = new StaticEntity(emptyStarRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[2] = new StaticEntity(emptyStarRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
            }
            else if (levelData.stars == 2)
            {
                stars[0] = new StaticEntity(starRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[1] = new StaticEntity(starRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[2] = new StaticEntity(emptyStarRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
            }
            else
            {
                stars[0] = new StaticEntity(starRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[1] = new StaticEntity(starRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
                stars[2] = new StaticEntity(starRegion, landscapePicture.widthMeters / 4, landscapePicture.widthMeters / 4);
            }
            Add(stars[0], 1);
            Add(stars[1], 1);
            Add(stars[2], 1);
            stars[0].y = landscapePicture.y + landscapePicture.heightMeters;
            stars[1].y = landscapePicture.y + landscapePicture.heightMeters;
            stars[2].y = landscapePicture.y + landscapePicture.heightMeters;

            for (int i = 0; i < 3; ++i)
            {
                if (i == 0)
                {
                    stars[i].x = landscapePicture.x + landscapePicture.widthMeters * 0.30f - stars[i].widthMeters / 2;
                }
                else if (i == 1)
                {
                    stars[i].x = landscapePicture.x + landscapePicture.widthMeters * 0.50f - stars[i].widthMeters / 2;
                    stars[i].y += 0.1f;
                }
                else
                {
                    stars[i].x = landscapePicture.x + landscapePicture.widthMeters * 0.70f - stars[i].widthMeters / 2;
                }
            }
        }

        void CreateLevelText()
        {
            levelText = new LevelText();
            levelText.text = levelData.name;
            levelText.x = landscapePicture.x + landscapePicture.widthMeters / 2 - (bitmapFont.getBounds(levelText.text).width / Renderer.PixelPerMeter) / 2;
            levelText.y = landscapePicture.y - 0.1f;
        }

        void CreateLockImages()
        {
            if (lockFans != null)
            {
                lockFans.landscape = landscapePicture;
                lockFans.text = String.valueOf(levelData.fansNeeded);
                lockFans.lockImage = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "lock_menu"));
                lockFans.fansImage = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "fans_small"));
                lockFans.fansImage.setScale(0.75f);
                lockFans.textWidth = bitmapFont.getBounds(lockFans.text).width;
            }

            if (lockCoins != null)
            {
                lockCoins.landscape = landscapePicture;
                lockCoins.text = String.valueOf(levelData.moneyNeeded);
                lockCoins.lockImage = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "lock_menu"));
                lockCoins.coinsImage = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "coins_small_mainmenu"));
                lockCoins.coinsImage.setScale(0.75f);
                lockCoins.textWidth = bitmapFont.getBounds(lockCoins.text).width;
            }
        }

        void StartStarAnimations()
        {
            Tween.to(stars[0], TweenAccessors.StaticEntityPositionAccessor.POSITION_Y, 1).
                    targetRelative(0.05f).
                    repeatYoyo(-1, 0).
                    ease(Cubic.INOUT).start(tweenManager);

            Tween.to(stars[1], TweenAccessors.StaticEntityPositionAccessor.POSITION_Y, 1).
                    targetRelative(0.03f).
                    repeatYoyo(-1, 0).
                    ease(Cubic.INOUT).start(tweenManager);

            Tween.to(stars[2], TweenAccessors.StaticEntityPositionAccessor.POSITION_Y, 1).
                    targetRelative(0.05f).
                    repeatYoyo(-1, 0).
                    ease(Cubic.INOUT).start(tweenManager);
        }

        void Update()
        {
            if (currentStars < levelData.stars)
            {
                currentStars = levelData.stars;
                switch (currentStars)
                {
                    case 0:
                        stars[0].region = emptyStarRegion;
                        stars[1].region = emptyStarRegion;
                        stars[2].region = emptyStarRegion;
                        break;
                    case 1:
                        stars[0].region = starRegion;
                        stars[1].region = emptyStarRegion;
                        stars[2].region = emptyStarRegion;
                        break;
                    case 2:
                        stars[0].region = starRegion;
                        stars[1].region = starRegion;
                        stars[2].region = emptyStarRegion;
                        break;
                    case 3:
                        stars[0].region = starRegion;
                        stars[1].region = starRegion;
                        stars[2].region = starRegion;
                        break;
                }
            }
        }

        void Draw(SpriteBatch batch)
        {
            if (levelText.isVisible)
                bitmapFont.draw(batch, levelText.text, levelText.x * Renderer.PixelPerMeter, levelText.y * Renderer.PixelPerMeter);
            if (lockFans != null)
                lockFans.Draw(batch);
            if (lockCoins != null)
                lockCoins.Draw(batch);
        }
    }


    enum State
    {
        LevelSelection,
        NotEnoughFansAlert,
        NotEnoughCoinsAlert,
        BuyLevelAlert
    }

    Spartans main;
    TweenManager tweenManager;
    CameraManager cameraManager;
    Scroller scroller;
    BitmapFont bitmapFont;
    BitmapFont aldrich32Font;
    BitmapFont hugeFont;
    LevelText[] levelText;
    LevelSelectionBox[] levelSelectionBoxes;
    LevelData[] current8LevelDatas;
    StaticEntity[] ground;
    GameStats gameStats;
    Arrow leftArrow, rightArrow;
    FansDisplay fansDisplay;
    CoinsDisplay coinsDisplay;
    NotEnoughFansAlert notEnoughFansAlert;
    NotEnoughCoinsAlert notEnoughCoinsAlert;
    LevelBuyConfirmationAlert levelBuyAlert;
    State state;
    int currentIndex;

    public MainMenu(Spartans main, ResourceManager resourceManager, CameraManager cameraManager, GameStats gameStats, SpriteBatch batch)
    {

        this.main = main;
        this.tweenManager = new TweenManager();
        this.gameStats = gameStats;
        this.cameraManager = cameraManager;
        state = State.LevelSelection;
        scroller = new Scroller();
        scroller.cameraManager = cameraManager;
        levelText = new LevelText[8];
        current8LevelDatas = new LevelData[8];
        ground = new StaticEntity[50];
        levelSelectionBoxes = new LevelSelectionBox[gameStats.levelDatas.size()];
        fansDisplay = new FansDisplay();
        coinsDisplay = new CoinsDisplay();
        Load(resourceManager);
        Create(batch);
    }

    int GetFirstLevelId()
    {
        return currentIndex;
    }

    int GetLastLevelId()
    {
        if ((currentIndex + 8) > levelSelectionBoxes.length)
            return levelSelectionBoxes.length - 1;
        return currentIndex + 8 - 1;
    }

    public void Load(ResourceManager resourceManager)
    {
        commonResources = resourceManager.GetResourceGroup("common");
        if (Gdx.graphics.getHeight() >= 480)
            bitmapFont = new BitmapFont(Gdx.files.internal("data/ui/main_menu/font18.fnt"), false);
        else
            bitmapFont = new BitmapFont(Gdx.files.internal("data/ui/main_menu/font15.fnt"), false);

        aldrich32Font = new BitmapFont(Gdx.files.internal("data/ui/main_menu/aldrich-32.fnt"), false);
        hugeFont = new BitmapFont(Gdx.files.internal("data/ui/main_menu/aldrich-40.fnt"), false);
        bitmapFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Log.Print("Menu loaded!!");
    }

    public void CaptureInputProcessor()
    {
        Gdx.input.setInputProcessor(this);
    }

    public void UpdateLevelBoxes()
    {
        for (int i = 0; i < levelSelectionBoxes.length; ++i)
        {
            levelSelectionBoxes[i].Update();
        }
    }

    public void Reset()
    {
        currentIndex = 0;
        cameraManager.x = Renderer.gameWorldSizeX / 2;
        cameraManager.y = Renderer.gameWorldSizeY / 2;

        for (int i = 0; i < levelSelectionBoxes.length; ++i)
            levelSelectionBoxes[i].UpdateLockStatus();

        UpdateLevelBoxes();
        UpdateFansAndMoney();
        UpdateArrowStatus();
        UpdateArrowPosition();
    }

    public void CreateLevelSelectionBoxes()
    {
        float leftSpace = 1;
        float rightSpace = 1;
        float totalSpaceLeft = Renderer.gameWorldSizeX - leftSpace - rightSpace;
        float neededWidth = totalSpaceLeft / 5;

        levelSelectionBoxes = new LevelSelectionBox[gameStats.sortedLevelDatas.size()];

        float blockStartX = 0;
        for (int j = 0; j < levelSelectionBoxes.length; j += 8)
        {

            int nextPageEnd = j + 8;
            if (nextPageEnd > levelSelectionBoxes.length)
                nextPageEnd = levelSelectionBoxes.length;

            float lastX = blockStartX + 1;

            for (int i = j; i < nextPageEnd; ++i)
            {
                levelSelectionBoxes[i] = new LevelSelectionBox(gameStats.sortedLevelDatas.get(i));
                levelSelectionBoxes[i].width = neededWidth;
                levelSelectionBoxes[i].height = neededWidth / 2.0f;

                //x
                if (i % 8 == 4)
                {
                    lastX = blockStartX + 1;
                }
                else if (i % 8 == 0)
                {

                }
                else
                    lastX += neededWidth * 4 / 3;

                levelSelectionBoxes[i].x = lastX;


                //y
                if (i % 8 < 4)
                    levelSelectionBoxes[i].y = Renderer.gameWorldSizeY * 5 / 8;
                else
                    levelSelectionBoxes[i].y = Renderer.gameWorldSizeY * 2 / 8;


                levelSelectionBoxes[i].Create();

                levelSelectionBoxes[i].StartStarAnimations();
            }

            blockStartX += Renderer.gameWorldSizeX;
        }

        UpdateArrowStatus();
    }

    void UpdateFansAndMoney()
    {
        fansDisplay.text = String.valueOf(gameStats.GetFans());
        coinsDisplay.text = String.valueOf(gameStats.GetMoney());
        coinsDisplay.textWidth = aldrich32Font.getBounds(coinsDisplay.text).width;
        coinsDisplay.textHeight = aldrich32Font.getBounds(coinsDisplay.text).height;
    }

    void UpdateArrowStatus()
    {
        rightArrow.isVisible = IsRightLevelAvailable();
        leftArrow.isVisible = IsLeftLevelAvailable();
    }

    void UpdateArrowPosition()
    {
        int pageNumber = currentIndex / 8;
        float rightShift = pageNumber * Renderer.gameWorldSizeX * Renderer.PixelPerMeter;
        leftArrow.image.setPosition(rightShift + 2, ((Renderer.gameWorldSizeY / 2) * Renderer.PixelPerMeter) - leftArrow.image.getRegionHeight() / 2);
        leftArrow.StartTween(leftArrow.image.getX() + 4, rightArrow.image.getX() - 4, 1f);
        rightArrow.image.setPosition(rightShift + (Renderer.gameWorldSizeX * Renderer.PixelPerMeter) - rightArrow.image.getRegionWidth() - 3, ((Renderer.gameWorldSizeY / 2) * Renderer.PixelPerMeter) - rightArrow.image.getRegionHeight() / 2);
        rightArrow.StartTween(rightArrow.image.getX() - 4, rightArrow.image.getX() + 4, 1f);
    }

    void CreateNotEnoughFansScreen()
    {
        notEnoughFansAlert = new NotEnoughFansAlert();
        notEnoughFansAlert.text = "Not enough fans!";
        notEnoughFansAlert.textWidth = hugeFont.getBounds(notEnoughFansAlert.text).width;
    }

    void CreateNotEnoughCoinsScreen()
    {
        notEnoughCoinsAlert = new NotEnoughCoinsAlert();
        notEnoughCoinsAlert.text = "Not enough coins!";
        notEnoughCoinsAlert.textWidth = hugeFont.getBounds(notEnoughCoinsAlert.text).width;
    }

    void CreateGround()
    {
        int lastX = -1;
        TextureRegion reg = commonResources.GetTextureRegion("data/common/common.atlas", "ground");
        reg = commonResources.CreateNewTextureRegion("data/common/common.atlas", "groundGenerated", reg, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(), reg.getRegionHeight());
        //ground[i].region.setRegion(ground[i].region.getRegionX() + 1, ground[i].region.getRegionY(), ground[i].region.getRegionWidth() - 1, ground[i].region.getRegionHeight());
        for (int i = 0; i < ground.length; ++i)
        {
            ground[i] = new StaticEntity(reg, reg.getRegionWidth() / Renderer.PixelPerMeter, 1);
            ground[i].x = lastX - (2 / 32f);
            ground[i].y = 0;
            lastX += ground[i].widthMeters;
            Add(ground[i], 0);
        }
    }

    public void Create(SpriteBatch batch)
    {

        background = new StaticEntity(commonResources.GetTextureRegion("data/common/common.atlas", "start_back"), Renderer.gameWorldSizeX * 2, Renderer.gameWorldSizeY * 2);
        Vector3 pos = Engine.vector3Pool.obtain(Gdx.graphics.getWidth() / -2, Gdx.graphics.getHeight() * 3 / 2, 0);
        cameraManager.ScreenToWorldCoordinate(pos);
        Engine.vector3Pool.free(pos);
        background.x = pos.x;
        background.y = pos.y;

        fansDisplay.image = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "fans_menu"));
        fansDisplay.image.setY(Renderer.gameWorldSizeY * Renderer.PixelPerMeter - fansDisplay.image.getHeight());
        fansDisplay.text = String.valueOf(gameStats.GetFans());

        coinsDisplay.image = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "coins"));
        coinsDisplay.image.setY(Renderer.gameWorldSizeY * Renderer.PixelPerMeter - fansDisplay.image.getHeight());
        coinsDisplay.text = String.valueOf(gameStats.GetMoney());

        leftArrow = new Arrow();
        leftArrow.image = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "left_arrow"));
        rightArrow = new Arrow();
        rightArrow.image = new Sprite(commonResources.GetTextureRegion("data/common/common.atlas", "right_arrow"));

        UpdateArrowPosition();

        CreateGround();

        CreateNotEnoughFansScreen();
        CreateNotEnoughCoinsScreen();

        currentIndex = 0;
        Fill8LevelDatas(currentIndex);
        CreateLevelSelectionBoxes();

        cameraManager.x = Renderer.gameWorldSizeX / 2;

        levelBuyAlert = new LevelBuyConfirmationAlert(hugeFont);
        levelBuyAlert.width = Renderer.gameWorldSizeX * Renderer.PixelPerMeter * 0.8f;
        levelBuyAlert.height = Renderer.gameWorldSizeY * Renderer.PixelPerMeter * 0.8f;
        levelBuyAlert.x = 0.1f * Renderer.gameWorldSizeX * Renderer.PixelPerMeter;
        levelBuyAlert.y = 0.1f * Renderer.gameWorldSizeY * Renderer.PixelPerMeter;

        HideAllLevelBoxesExceptCurrent();
    }

    void StartLevel(int id)
    {
        Log.Print("box clicked: " + id);
        LevelData data = gameStats.sortedLevelDatas.get(id);
        End(main.engine.input);
        main.ChangeGameState(Spartans.GameState.InGame, data);
    }

    void Fill8LevelDatas(int staringIndex)
    {
        for (int i = 0; i < 8; ++i)
            current8LevelDatas[i] = gameStats.sortedLevelDatas.get(staringIndex + i);
    }

    public void End(InputProcessor inputProcessor)
    {
        Gdx.input.setInputProcessor(inputProcessor);
    }

    public void Destroy(InputProcessor inputProcessor)
    {
        End(inputProcessor);
        tweenManager.killAll();
        tweenManager = null;
    }

    public void Update(float timePassed)
    {
        super.Update(timePassed, cameraManager);

        tweenManager.update(timePassed);

        leftArrow.Update(timePassed);
        rightArrow.Update(timePassed);
        scroller.Update(timePassed);

        Vector3 pos = Engine.vector3Pool.obtain(Gdx.graphics.getWidth() / -2, Gdx.graphics.getHeight() * 3 / 2, 0);
        cameraManager.ScreenToWorldCoordinate(pos);
        Engine.vector3Pool.free(pos);
        background.x = pos.x;
        background.y = pos.y;
    }

    public void DrawText(SpriteBatch batch)
    {
        for (int i = 0; i < levelSelectionBoxes.length; i++)
        {
            levelSelectionBoxes[i].Draw(batch);
        }

        rightArrow.Draw(batch);
        leftArrow.Draw(batch);

        fansDisplay.Draw(batch);
        coinsDisplay.Draw(batch);
        notEnoughFansAlert.Draw(batch);
        notEnoughCoinsAlert.Draw(batch);
        levelBuyAlert.Draw(batch);
    }

    void HideAllLevelBoxesExceptCurrent()
    {
        int firstLevel = GetFirstLevelId();
        int lastLevel = GetLastLevelId();
        for (int i = 0; i < levelSelectionBoxes.length; ++i)
        {
            if (i > lastLevel || i < firstLevel)
                levelSelectionBoxes[i].HideAnimation();
        }
        ShowCurrentPageBoxes();
    }

    void HideCurrentPageLevelBoxes()
    {
        int last = GetLastLevelId();
        for (int i = GetFirstLevelId(); i <= last; ++i)
            levelSelectionBoxes[i].HideAnimation();

    }

    void ShowCurrentPageBoxes()
    {
        int last = GetLastLevelId();
        for (int i = GetFirstLevelId(); i <= last; ++i)
            levelSelectionBoxes[i].ShowAnimation();

    }

    void GotoLeftPage()
    {
        scroller.state = ScrollerState.ScrollingLeft;
        scroller.distanceLeft = Renderer.gameWorldSizeX;
        scroller.targetX = cameraManager.x - Renderer.gameWorldSizeX;
        scroller.HideUI();
        HideCurrentPageLevelBoxes();
        currentIndex -= 8;
        ShowCurrentPageBoxes();
    }

    void GotoRightPage()
    {
        scroller.state = ScrollerState.ScrollingRight;
        scroller.distanceLeft = Renderer.gameWorldSizeX;
        scroller.targetX = cameraManager.x + Renderer.gameWorldSizeX;
        scroller.HideUI();

        HideCurrentPageLevelBoxes();
        currentIndex += 8;
        ShowCurrentPageBoxes();
    }

    public boolean IsRightLevelAvailable()
    {
        // if any level is available high than current page index we can go right
        for (int i = currentIndex + 8; i < gameStats.sortedLevelDatas.size(); ++i)
        {
            if (gameStats.sortedLevelDatas.get(i).isUnlocked)
                return true;
            if (gameStats.sortedLevelDatas.get(i).hasEnoughFans)
                return true;
        }

        return false;
    }

    public boolean IsLeftLevelAvailable()
    {
        // if any level is available high than current page index we can go right
        for (int i = currentIndex - 1; i >= 0; --i)
        {
            if (gameStats.sortedLevelDatas.get(i).isUnlocked)
                return true;
            if (gameStats.sortedLevelDatas.get(i).hasEnoughFans)
                return true;
        }

        return false;
    }

    @Override
    public boolean keyDown(int keycode)
    {

        if (keycode == Input.Keys.BACK)
            main.gameState = Spartans.GameState.WaitingExit;

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
        Vector3 sec = Engine.vector3Pool.obtain(screenX, screenY, 0);
        cameraManager.ScreenToWorldCoordinate(sec);

        switch (state)
        {
            case LevelSelection:
            {
                boolean clicked = false;

                if (rightArrow.isVisible && Utility.RectIntersect(sec.x, sec.y, 0.1f, 0.1f, rightArrow.image.getX() / Renderer.PixelPerMeter, rightArrow.image.getY() / Renderer.PixelPerMeter, rightArrow.image.getWidth() / Renderer.PixelPerMeter, rightArrow.image.getHeight() / Renderer.PixelPerMeter))
                {
                    clicked = true;
                    GotoRightPage();
                    Log.Print("Right clicked!");
                }

                if (!clicked)
                {
                    if (leftArrow.isVisible && Utility.RectIntersect(sec.x, sec.y, 0.1f, 0.1f, leftArrow.image.getX() / Renderer.PixelPerMeter, leftArrow.image.getY() / Renderer.PixelPerMeter, leftArrow.image.getWidth() / Renderer.PixelPerMeter, leftArrow.image.getHeight() / Renderer.PixelPerMeter))
                    {
                        clicked = true;
                        GotoLeftPage();
                        Log.Print("Left clicked!");
                    }
                }

                if (!clicked)
                {
                    for (int i = 0; i < gameStats.sortedLevelDatas.size(); ++i)
                    {
                        if (levelSelectionBoxes[i] != null)
                        {
                            if (Utility.RectIntersect(sec.x, sec.y, 0.1f, 0.1f, levelSelectionBoxes[i].x, levelSelectionBoxes[i].y, levelSelectionBoxes[i].landscapePicture.widthMeters, levelSelectionBoxes[i].landscapePicture.heightMeters))
                            {
                                LevelData data = levelSelectionBoxes[i].levelData;
                                if (data.isUnlocked)
                                {
                                    StartLevel(i);
                                }
                                else
                                {
                                    if (!data.hasEnoughFans)
                                    {
                                        notEnoughFansAlert.isVisible = true;
                                        state = State.NotEnoughFansAlert;
                                    }
                                    else if (data.moneyNeeded > gameStats.GetMoney())
                                    {
                                        notEnoughCoinsAlert.isVisible = true;
                                        state = State.NotEnoughCoinsAlert;
                                    }
                                    else
                                    {
                                        state = State.BuyLevelAlert;
                                        levelBuyAlert.levelId = i;
                                        levelBuyAlert.isVisible = true;
                                        levelBuyAlert.text = "Travel costs: " + data.moneyNeeded;
                                        levelBuyAlert.textWidth = hugeFont.getBounds(levelBuyAlert.text).width;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

            }
            break;
            case NotEnoughFansAlert:
                notEnoughFansAlert.isVisible = false;
                state = State.LevelSelection;
                break;
            case NotEnoughCoinsAlert:
                notEnoughCoinsAlert.isVisible = false;
                state = State.LevelSelection;
                break;
            case BuyLevelAlert:
                if (Utility.RectIntersect(sec.x * Renderer.PixelPerMeter, sec.y * Renderer.PixelPerMeter, 0.1f, 0.1f, levelBuyAlert.cancelButton.getX(), levelBuyAlert.cancelButton.getY(), levelBuyAlert.cancelButton.getWidth(), levelBuyAlert.cancelButton.getHeight()))
                {
                    state = State.LevelSelection;
                    levelBuyAlert.isVisible = false;
                }
                else if (Utility.RectIntersect(sec.x * Renderer.PixelPerMeter, sec.y * Renderer.PixelPerMeter, 0.1f, 0.1f, levelBuyAlert.acceptButton.getX(), levelBuyAlert.acceptButton.getY(), levelBuyAlert.acceptButton.getWidth(), levelBuyAlert.acceptButton.getHeight()))
                {
                    LevelData data = levelSelectionBoxes[levelBuyAlert.levelId].levelData;
                    LevelSelectionBox box = levelSelectionBoxes[levelBuyAlert.levelId];
                    gameStats.UnlockLevel(data);
                    gameStats.AddMoney(-data.moneyNeeded);
                    box.lockCoins = null;
                    state = State.LevelSelection;
                    levelBuyAlert.isVisible = false;
                    UpdateFansAndMoney();
                }
                break;
        }

        Engine.vector3Pool.free(sec);
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
