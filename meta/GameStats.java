package com.spartans.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.engine.*;
import com.spartans.levels.Gameplay;

public class GameStats
{
    public ArrayList<LevelData> sortedLevelDatas;
    public HashMap<String, LevelData> levelDatas;

    public ResourceGroup common;

    private float feverTimeLeft;

    private int fans;
    private int money;

    public boolean music;
    public boolean sound;

    private TextureRegion defaultRegion;
    private Sound defaultSound;

    public GameStats()
    {
        music = true;
        sound = true;
        levelDatas = new HashMap<String, LevelData>();
        sortedLevelDatas = new ArrayList<LevelData>();

        Texture defaultTexture = new Texture(Gdx.files.internal("data/default.png"));
        defaultSound = Gdx.audio.newSound(Gdx.files.internal("data/default.wav"));
        defaultRegion = new TextureRegion(defaultTexture, defaultTexture.getWidth(), defaultTexture.getHeight());
    }

    public TextureRegion GetDefaultTextureRegion()
    {
        return defaultRegion;
    }

    public Sound GetDefaultSound()
    {
        return  defaultSound;
    }

    public void Read(String pathToLevelsXml)
    {
        XmlReader reader = new XmlReader();
        levelDatas.clear();
        sortedLevelDatas.clear();

        Element root;
        try
        {
            root = reader.parse(Gdx.files.internal(pathToLevelsXml));
        } catch (IOException e)
        {
            Log.Print("Could not parse: " + pathToLevelsXml);
            return;
        }

        int rootChildCount = root.getChildCount();
        for (int i = 0; i < rootChildCount; ++i)
        {
            Element rootChild = root.getChild(i);

            if (rootChild.getName().equals("levels"))
            {
                int numOfLevels = rootChild.getChildCount();
                for (int j = 0; j < numOfLevels; ++j)
                {
                    Element level = rootChild.getChild(j);
                    LevelData newLevelData = new LevelData();
                    newLevelData.filePath = level.getAttribute("path");
                    newLevelData.landscape = level.getAttribute("landscape");
                    newLevelData.name = level.getAttribute("name");
                    newLevelData.moneyNeeded = (int) level.getFloatAttribute("dolar");
                    newLevelData.fansNeeded = (int) level.getFloatAttribute("fans");
                    levelDatas.put(newLevelData.name, newLevelData);
                    sortedLevelDatas.add(newLevelData);
                    newLevelData.id = sortedLevelDatas.size() - 1;
                }
            }
        }

        LoadGameSettings();
        LoadAllFromSaved();
        UpdateLevelDatas();
    }

    public void LoadLevelDatas(EffectManager effectManager)
    {
        Read("data/levels/levels.xml");
    }

    // Bi sonraki bolumun numarasini dondurur.
    // Eger yoksa -1 dondurur
    public int GetNextLevel(int currentLevelIndex)
    {
        currentLevelIndex++;

        if (currentLevelIndex >= sortedLevelDatas.size())
            return -1;

        int size = sortedLevelDatas.size();
        for (; currentLevelIndex < size; ++currentLevelIndex)
        {
            if (sortedLevelDatas.get(currentLevelIndex).isUnlocked)
                return currentLevelIndex;
        }

        return -1;
    }

    public void NewLevelStarted()
    {
        feverTimeLeft = 0;
    }

    public boolean IsLevelUnlocked(String name)
    {
        LevelData ldata = levelDatas.get(name);
        if (ldata == null)
            return false;
        return ldata.isUnlocked;
    }

    public int GetFans()
    {
        return fans;
    }

    public int GetMoney()
    {
        return money;
    }

    public void ResetAllSaves()
    {
        fans = 0;
        money = 0;

        Preferences levelPrefs = Gdx.app.getPreferences("SS.GameSettings");

        levelPrefs.putBoolean("music", true);
        levelPrefs.putBoolean("sound", true);
        levelPrefs.putInteger("fans", 0);
        levelPrefs.putInteger("money", 0);

        for (LevelData levelData : levelDatas.values())
        {
            levelData.isUnlocked = false;
            levelData.stars = 0;

            if (levelData.fansNeeded == 0 && levelData.moneyNeeded == 0)
                levelData.isUnlocked = true;

            SaveToDisk(levelData);
        }
    }

    public void AddFans(int amount)
    {
        if (amount == 0)
            return;

        fans += amount;
        SaveGameSettings();
    }

    public void AddMoney(int amount)
    {
        if (amount == 0)
            return;

        money += amount;
        SaveGameSettings();
    }

    // eger yeni bolum aculdiysa true dondurur
    public boolean UpdateLevelDatas()
    {
        boolean isAnyUnlocked = false;
        for (LevelData ldata : sortedLevelDatas)
        {

            if (!ldata.isUnlocked)
            {
                if (ldata.fansNeeded <= fans)
                {
                    if (!ldata.hasEnoughFans)
                        isAnyUnlocked = true;
                    ldata.hasEnoughFans = true;

                    if (ldata.moneyNeeded == 0)
                        ldata.isUnlocked = true;
                }
            }
        }

        return isAnyUnlocked;
    }

    public void LoadFromSaved(LevelData levelData)
    {

        Preferences levelPrefs = Gdx.app.getPreferences("SS.Level." + levelData.name);
        if (levelPrefs.contains("stars"))
        {
            levelData.stars = levelPrefs.getInteger("stars");
            levelData.isUnlocked = levelPrefs.getBoolean("isUnlocked");
        }
    }

    public void LoadAllFromSaved()
    {
        for (LevelData ldata : sortedLevelDatas)
        {
            LoadFromSaved(ldata);
        }
    }

    public void LoadGameSettings()
    {
        Preferences levelPrefs = Gdx.app.getPreferences("SS.GameSettings");
        if (!levelPrefs.contains("music"))
            return;

        music = levelPrefs.getBoolean("music");
        sound = levelPrefs.getBoolean("sound");
        fans = levelPrefs.getInteger("fans");
        money = levelPrefs.getInteger("money");
    }

    public void SaveGameSettings()
    {
        Preferences levelPrefs = Gdx.app.getPreferences("SS.GameSettings");
        levelPrefs.putBoolean("music", music);
        levelPrefs.putBoolean("sound", sound);
        levelPrefs.putInteger("fans", fans);
        levelPrefs.putInteger("money", money);
        levelPrefs.flush();
    }

    public void UnlockLevel(LevelData levelData)
    {
        levelData.isUnlocked = true;
        SaveToDisk(levelData);
    }

    public void SaveToDisk(LevelData data)
    {
        Preferences levelPrefs = Gdx.app.getPreferences("SS.Level." + data.name);

        levelPrefs.putInteger("stars", data.stars);
        levelPrefs.putBoolean("isUnlocked", data.isUnlocked);

        levelPrefs.flush();
    }

    public void SaveAllToDisk()
    {
        for (LevelData data : levelDatas.values())
        {
            SaveToDisk(data);
        }
    }

    public float GetFeverTimeLeft()
    {
        return feverTimeLeft;
    }

    public void AddFeverTime(float amount)
    {
        feverTimeLeft += amount;

        if (feverTimeLeft < 0)
            feverTimeLeft = 0f;
    }
}
