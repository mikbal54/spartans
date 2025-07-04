package com.spartans.levels;

import com.badlogic.gdx.utils.XmlReader;

import java.util.LinkedList;

public class LevelResouces
{
    public class SkeletonPathData
    {
        String atlasPath;
        String skeletonPath;
    }

    public LinkedList<String> sprites;
    public LinkedList<String> anims;
    public LinkedList<String> textures;
    public LinkedList<SkeletonPathData> skeletons;
    public LinkedList<String> particles;

    public LevelResouces()
    {
        sprites = new LinkedList<String>();
        anims = new LinkedList<String>();
        textures = new LinkedList<String>();
        skeletons = new LinkedList<SkeletonPathData>();
        particles = new LinkedList<String>();
    }

    /**/
    public void FillResources(XmlReader.Element rootElement)
    {
        int assetsChildCount = rootElement.getChildCount();
        for (int j = 0; j < assetsChildCount; ++j)
        {
            XmlReader.Element asset = rootElement.getChild(j);
            String name = asset.getName();
            if (name.equals("sprites"))
                sprites.add(asset.getText());
            else if (name.equals("texture"))
                textures.add(asset.getText());
            else if (name.equals("anim"))
                anims.add(asset.getText());
            else if (name.equals("skeleton"))
            {
                SkeletonPathData pdata =  new SkeletonPathData();
                pdata.atlasPath = asset.getAttribute("atlas");
                pdata.skeletonPath = asset.getText();
                skeletons.add(pdata);
            }
            else if (name.equals("particle"))
                particles.add(asset.getText());
        }
    }

}
