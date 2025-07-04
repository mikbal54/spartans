package com.spartans.levels;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.engine.*;

// bu level xml dosyasini okur, hafizada tutar. daha sonce level.Load ve level.Create fonksiyonlarinda kullanilir
public class LevelInfo
{

    public class StaticDecor extends Transform
    {
        public String atlasPath;
        public String image;
        public int zIndex;
    }

    public class AnimatedDecor extends Transform
    {
        public String animFilePath;
        public int zIndex;
    }

    public class Enemy extends Transform
    {
        public String enemyType;
        public int pointsGained;
        public int zIndex;
    }

    public class Collectible extends Transform
    {
        public String type;
        public int zIndex;
    }

    public class Ground
    {
        public String atlasPath;
        public String imagePath;
        public float width;
        public float height;
        public float widthMeters;
        public float heightMeters;
        public int repeat;
    }

    public class Background
    {
        public LinkedList<StaticDecor> staticDecors;
        public LinkedList<AnimatedDecor> animatedDecors;

        String backgroundAtlasName;
        String backgroundImageName;
        float backgroundImageX, backgroundImageY, widthMeters, heightMeters;

        public Background()
        {
            staticDecors = new LinkedList<LevelInfo.StaticDecor>();
            animatedDecors = new LinkedList<LevelInfo.AnimatedDecor>();
        }
    }

    public String resourceGroupName;
    public String startingText;
    public String endingText;
    public String failedText;

    public float startX;
    public float startY;
    public float kalePosX;
    public int maxPoints;
    public float timeToComplete;
    public int fansGained1, fansGained2, fansGained3;
    public int moneyGained1, moneyGained2, moneyGained3;
    public boolean hasGoalKeeper;

    public LinkedList<StaticDecor> staticDecors;
    public LinkedList<AnimatedDecor> animatedDecors;
    public LinkedList<Enemy> enemies;
    public LinkedList<Collectible> collectibles;

    public Ground ground;
    public Background background;

    public LevelInfo()
    {
        background = new Background();
        ground = new Ground();
        staticDecors = new LinkedList<LevelInfo.StaticDecor>();
        animatedDecors = new LinkedList<LevelInfo.AnimatedDecor>();
        enemies = new LinkedList<LevelInfo.Enemy>();
        collectibles = new LinkedList<Collectible>();

    }

    public void Load(String xmlFile)
    {
        XmlReader reader = new XmlReader();

        Element root;
        try
        {
            root = reader.parse(Gdx.files.internal(xmlFile));
        } catch (Exception e)
        {
            Log.Print("ERROR: unable to parse xml file: " + xmlFile);
            return;
        }

        int rootChildCount = root.getChildCount();
        for (int i = 0; i < rootChildCount; ++i)
        {
            Element rootChild = root.getChild(i);
            String elementName = rootChild.getName();

            if (elementName.equals("settings"))
            {
                resourceGroupName = rootChild.getAttribute("resourceGroup");
                startingText = rootChild.getAttribute("startingText");
                endingText = rootChild.getAttribute("endingText");
                failedText = rootChild.getAttribute("failedText");
                startX = rootChild.getFloatAttribute("startX");
                startY = rootChild.getFloatAttribute("startY");
                kalePosX = rootChild.getFloatAttribute("kalePosX");
                maxPoints = rootChild.getIntAttribute("maxPoints");
                timeToComplete = rootChild.getFloatAttribute("timeToComplete");
                fansGained1 = rootChild.getIntAttribute("fansGained1");
                fansGained2 = rootChild.getIntAttribute("fansGained2");
                fansGained3 = rootChild.getIntAttribute("fansGained3");
                moneyGained1 = rootChild.getIntAttribute("moneyGained1");
                moneyGained2 = rootChild.getIntAttribute("moneyGained2");
                moneyGained3 = rootChild.getIntAttribute("moneyGained3");
                hasGoalKeeper = rootChild.getBooleanAttribute("hasGoalKeeper");
            }
            else if (elementName.equals("ground"))
            {
                ground.atlasPath = rootChild.getAttribute("atlas");
                ground.imagePath = rootChild.getAttribute("image");
                ground.repeat = rootChild.getIntAttribute("repeat");
                ground.width = rootChild.getFloatAttribute("width");
                ground.height = rootChild.getFloatAttribute("height");
                ground.widthMeters = rootChild.getFloatAttribute("widthMeters");
                ground.heightMeters = rootChild.getFloatAttribute("heightMeters");
            }
            else if (elementName.equals("bimage"))
            {
                background.backgroundAtlasName = rootChild.getAttribute("atlas");
                background.backgroundImageName = rootChild.getAttribute("image");
                background.backgroundImageX = rootChild.getFloatAttribute("x");
                background.backgroundImageY = rootChild.getFloatAttribute("y");
                background.widthMeters = rootChild.getFloatAttribute("widthMeters");
                background.heightMeters = rootChild.getFloatAttribute("heightMeters");
            }
            else if (elementName.equals("background"))
            {
                int backgroundChildrenCount = rootChild.getChildCount();
                for (int j = 0; j < backgroundChildrenCount; ++j)
                {
                    Element decor = rootChild.getChild(j);
                    if (decor.getName().equals("static"))
                    {
                        StaticDecor staticDecor = new StaticDecor();
                        staticDecor.atlasPath = decor.getAttribute("atlas");
                        staticDecor.image = decor.getAttribute("image");
                        staticDecor.x = decor.getFloat("x");
                        staticDecor.y = decor.getFloat("y");
                        staticDecor.zIndex = decor.getInt("z");
                        staticDecor.angle = decor.getFloat("angle");
                        staticDecor.widthMeters = decor.getFloat("widthMeters");
                        staticDecor.heightMeters = decor.getFloat("heightMeters");
                        background.staticDecors.add(staticDecor);
                    }
                    else if (decor.getName().equals("animated"))
                    {
                        AnimatedDecor animatedDecor = new AnimatedDecor();
                        animatedDecor.animFilePath = decor.getAttribute("anim");
                        animatedDecor.x = decor.getFloat("x");
                        animatedDecor.y = decor.getFloat("y");
                        animatedDecor.zIndex = decor.getInt("z");
                        animatedDecor.angle = decor.getFloat("angle");
                        animatedDecor.width = decor.getFloat("width");
                        animatedDecor.height = decor.getFloat("height");
                        animatedDecor.widthMeters = decor.getFloat("widthMeters");
                        animatedDecor.heightMeters = decor.getFloat("heightMeters");
                        background.animatedDecors.add(animatedDecor);
                    }
                }
            }
            else if (elementName.equals("decors"))
            {
                int decorsChild = rootChild.getChildCount();
                for (int j = 0; j < decorsChild; ++j)
                {
                    Element decor = rootChild.getChild(j);
                    String decorType = decor.getName();
                    if (decorType.equals("static"))
                    {
                        StaticDecor staticDecor = new StaticDecor();
                        staticDecor.atlasPath = decor.getAttribute("atlas");
                        staticDecor.image = decor.getAttribute("image");
                        staticDecor.x = decor.getFloat("x");
                        staticDecor.y = decor.getFloat("y");
                        staticDecor.zIndex = decor.getInt("z");
                        staticDecor.angle = decor.getFloat("angle");
                        staticDecor.widthMeters = decor.getFloat("widthMeters");
                        staticDecor.heightMeters = decor.getFloat("heightMeters");
                        staticDecors.add(staticDecor);
                    }
                    else if (decorType.equals("animated"))
                    {
                        AnimatedDecor animatedDecor = new AnimatedDecor();
                        animatedDecor.animFilePath = decor.getAttribute("anim");
                        animatedDecor.x = decor.getFloat("x");
                        animatedDecor.y = decor.getFloat("y");
                        animatedDecor.zIndex = decor.getInt("z");
                        animatedDecor.angle = decor.getFloat("angle");
                        animatedDecor.width = decor.getFloat("width");
                        animatedDecor.height = decor.getFloat("height");
                        animatedDecor.widthMeters = decor.getFloat("widthMeters");
                        animatedDecor.heightMeters = decor.getFloat("heightMeters");
                        animatedDecors.add(animatedDecor);
                    }
                }
            }
            else if (elementName.equals("enemies"))
            {
                int enemiesChildrenCount = rootChild.getChildCount();
                for (int j = 0; j < enemiesChildrenCount; ++j)
                {
                    Element enemyElement = rootChild.getChild(j);
                    Enemy enemy = new Enemy();
                    enemy.pointsGained = enemyElement.getInt("pointsGained");
                    enemy.x = enemyElement.getFloat("x");
                    enemy.y = enemyElement.getFloat("y");
                    enemy.zIndex = enemyElement.getInt("z");
                    enemy.angle = enemyElement.getFloat("angle");
                    enemy.width = enemyElement.getFloat("width");
                    enemy.height = enemyElement.getFloat("height");
                    enemy.widthMeters = enemyElement.getFloat("widthMeters");
                    enemy.heightMeters = enemyElement.getFloat("heightMeters");
                    enemy.enemyType = enemyElement.getAttribute("type");
                    enemies.add(enemy);
                }
            }
            else if (elementName.equals("collectibles"))
            {
                int collectiblesChildrenCount = rootChild.getChildCount();
                for (int j = 0; j < collectiblesChildrenCount; ++j)
                {
                    Element collElement = rootChild.getChild(j);
                    Collectible coll = new Collectible();
                    coll.type = collElement.getAttribute("type");
                    coll.x = collElement.getFloat("x");
                    coll.y = collElement.getFloat("y");
                    coll.zIndex = collElement.getInt("z");
                    coll.angle = collElement.getFloat("angle");
                    coll.width = collElement.getFloat("width");
                    coll.height = collElement.getFloat("height");
                    coll.widthMeters = collElement.getFloat("widthMeters");
                    coll.heightMeters = collElement.getFloat("heightMeters");
                    collectibles.add(coll);
                }
            }

        }
    }
}
