package com.spartans.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.engine.*;
import com.spartans.CameraManager;

import java.util.ArrayList;

public class Level
{

    protected ArrayList<ArrayList<StaticEntity>> staticEntities = new ArrayList<ArrayList<StaticEntity>>(10);
    protected ArrayList<ArrayList<AnimatedEntity>> animatedEntities = new ArrayList<ArrayList<AnimatedEntity>>(10);
    protected ArrayList<ArrayList<SkeletonEntity>> skeletonEntities = new ArrayList<ArrayList<SkeletonEntity>>(10);

    protected ArrayList<ArrayList<StaticEntity>> backgroundStaticEntities = new ArrayList<ArrayList<StaticEntity>>(10);
    protected ArrayList<ArrayList<AnimatedEntity>> backgroundAnimatedEntities = new ArrayList<ArrayList<AnimatedEntity>>(10);

    public StaticEntity background;

    public ResourceGroup resources;
    public ResourceGroup commonResources;

    public Stage ui;
    Skin uiSkin;
    TextureAtlas uiAtlas;

    public Level()
    {
        backgroundStaticEntities.ensureCapacity(10);
        for (int i = 0; i < 10; ++i)
            backgroundStaticEntities.add(new ArrayList<StaticEntity>());

        backgroundAnimatedEntities.ensureCapacity(10);
        for (int i = 0; i < 10; ++i)
            backgroundAnimatedEntities.add(new ArrayList<AnimatedEntity>());

        staticEntities.ensureCapacity(10);
        for (int i = 0; i < 10; ++i)
            staticEntities.add(new ArrayList<StaticEntity>());

        animatedEntities.ensureCapacity(10);
        for (int i = 0; i < 10; ++i)
            animatedEntities.add(new ArrayList<AnimatedEntity>());

        skeletonEntities.ensureCapacity(10);
        for (int i = 0; i < 10; ++i)
            skeletonEntities.add(new ArrayList<SkeletonEntity>());
    }

    public void DrawEntities(Renderer renderer, CameraManager cameraManager)
    {
        SpriteBatch batch = renderer.batch;

        background.Draw(batch);

        for (int i = 9; i >= 0; --i)
        {

            ArrayList<StaticEntity> bse = backgroundStaticEntities.get(i);
            int size = bse.size();
            for (int j = 0; j < size; ++j)
                if (Math.abs(bse.get(j).x - cameraManager.x) < 500)
                {
                    bse.get(j).Draw(batch);
                }

            ArrayList<AnimatedEntity> ase = backgroundAnimatedEntities.get(i);
            size = ase.size();
            for (int j = 0; j < size; ++j)
                ase.get(j).Draw(batch);

        }

        for (int i = 0; i < 10; ++i)
        {

            ArrayList<StaticEntity> se = staticEntities.get(i);
            int size = staticEntities.get(i).size();
            for (int j = 0; j < size; ++j)
                if (Math.abs(se.get(j).x - cameraManager.x) < 500)
                {
                    se.get(j).Draw(batch);
                }

            ArrayList<AnimatedEntity> ae = animatedEntities.get(i);
            size = animatedEntities.get(i).size();
            for (int j = 0; j < size; ++j)
                if (Math.abs(ae.get(j).x - cameraManager.x) < 500)
                {
                    ae.get(j).Draw(batch);
                }

            ArrayList<SkeletonEntity> ske = skeletonEntities.get(i);
            size = skeletonEntities.get(i).size();
            for (int j = 0; j < size; ++j)
                ske.get(j).Draw(batch);

        }

    }

    public void DrawUI(Renderer renderer, CameraManager cameraManager)
    {
        ui.getCamera().update();
        cameraManager.camera.update();
        renderer.batch.setProjectionMatrix(ui.getCamera().combined);
        ui.getRoot().draw(renderer.batch, 1);
        renderer.batch.setProjectionMatrix(cameraManager.camera.combined);
    }

    public void DrawEffects(Renderer renderer)
    {
        renderer.effectManager.Draw(renderer.batch);
    }


    public void Add(StaticEntity staticEntity, int zIndex)
    {
        staticEntities.get(zIndex).add(staticEntity);
    }

    public void Add(AnimatedEntity animatedEntity, int zIndex)
    {
        animatedEntities.get(zIndex).add(animatedEntity);
    }

    public void Add(SkeletonEntity skeletonEntity, int zIndex)
    {
        skeletonEntities.get(zIndex).add(skeletonEntity);
    }

    public void AddBackground(StaticEntity staticEntity, int layer)
    {
        backgroundStaticEntities.get(layer).add(staticEntity);
    }

    public void AddBackground(AnimatedEntity animatedEntity, int layer)
    {
        backgroundAnimatedEntities.get(layer).add(animatedEntity);
    }

    public void Update(float timePassed, CameraManager cameraManager)
    {
        // background obje leri cameraManager update ediyor.
        cameraManager.Update(timePassed, backgroundStaticEntities, backgroundAnimatedEntities);

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<AnimatedEntity> ae = animatedEntities.get(i);
            int size = ae.size();
            for (int j = 0; j < size; ++j)
                ae.get(j).Update(timePassed);
        }

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<SkeletonEntity> ske = skeletonEntities.get(i);
            int size = ske.size();
            for (int j = 0; j < size; ++j)
                ske.get(j).Update(timePassed, false, null);
        }
    }

}
