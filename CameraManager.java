package com.spartans;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.engine.AnimatedEntity;
import com.engine.OrthographicCameraWithVirtualViewport;
import com.engine.Renderer;
import com.engine.StaticEntity;

public class CameraManager
{

    public enum State
    {
        FollowingBehind,
        FollowingAhead,
        Fixed
    }

    public State state;

    public float x, y;

    public float currentShiftAmount = 6;
    public float cameraShiftBehind;
    public float cameraShiftAhead;

    public OrthographicCameraWithVirtualViewport camera;
    public Renderer renderer;

    public float[] layerPositionX;
    public float[] layerPositionY;
    public float[] layerRatios;

    public boolean parallaxX;
    public boolean parallaxY;

    public CameraManager(Renderer renderer, OrthographicCameraWithVirtualViewport camera)
    {
        SetupLayerRatios();

        state = State.FollowingAhead;
        this.renderer = renderer;
        this.camera = camera;
        this.camera.position.x = 0;
        this.camera.position.y = 0;

        // camera yi oyuncunun 7 metre otesinden baslat.
        cameraShiftBehind = 6;
        currentShiftAmount = cameraShiftBehind;
        cameraShiftAhead = 7;

        x = 0;
        y = 0;

        parallaxX = true;
        parallaxY = true;
        camera.position.set(cameraShiftBehind, 0, 0f);

        Renderer.gameWorldSizeX = camera.virtualViewport.getWidth() / Renderer.PixelPerMeter;
        Renderer.gameWorldSizeY = camera.virtualViewport.getHeight() / Renderer.PixelPerMeter;
    }

    public void ChangeState(State newState)
    {
        state = newState;
    }

    void SetupLayerRatios()
    {
        layerPositionX = new float[10];
        layerPositionY = new float[10];
        layerRatios = new float[10];

        for (int i = 0; i < 10; ++i)
        {
            switch (i)
            {
                case 0:
                    layerRatios[0] = 0;
                    break;
                case 1:
                    layerRatios[1] = 0.50f;
                    break;
                case 2:
                    layerRatios[2] = 0.55f;
                    break;
                case 3:
                    layerRatios[3] = 0.65f;
                    break;
                case 4:
                    layerRatios[4] = 0.70f;
                    break;
                case 5:
                    layerRatios[5] = 0.75f;
                    break;
                case 6:
                    layerRatios[6] = 0.80f;
                    break;
                case 7:
                    layerRatios[7] = 0.85f;
                    break;
                case 8:
                    layerRatios[8] = 0.95f;
                    break;
                case 9:
                    layerRatios[9] = 1;
                    break;
            }
        }
    }

    // mouse pozisyonunu dunya posizyonuna cevirir. Metre cinsinden sonuc dondurur
    public Vector3 ScreenToWorldCoordinate(Vector3 screenCoords)
    {
        camera.unproject(screenCoords, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenCoords.x /= Renderer.PixelPerMeter;
        screenCoords.y /= Renderer.PixelPerMeter;
        return screenCoords;
    }

    // Oyun posizyonunu ekran posiyonuna cevirir
    public Vector3 WorldToScreenCoordinate(Vector3 worldCoord)
    {
        worldCoord.x *= Renderer.PixelPerMeter;
        worldCoord.y *= Renderer.PixelPerMeter;
        camera.project(worldCoord, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        worldCoord.y = Gdx.graphics.getHeight() - worldCoord.y;
        return worldCoord;
    }

    // bolum bastan baslayinca bunu cagir, bolece camera dogru poziyonda baslasin
    public void Reset()
    {
        state = State.FollowingAhead;
    }

    public void SetX(float newX)
    {
        for (int i = 0; i < 10; ++i)
            layerPositionX[i] = newX * layerRatios[i];

        this.x = newX;
    }

    public void SetY(float newY)
    {
        for (int i = 0; i < 10; ++i)
            layerPositionY[i] = (newY - Renderer.gameWorldSizeY / 2f) * layerRatios[i];

        this.y = newY;
    }

    public void Update(float timeSinceLastFrame, ArrayList<ArrayList<StaticEntity>> staticEntities, ArrayList<ArrayList<AnimatedEntity>> animatedEntities)
    {

        camera.position.x = x * Renderer.PixelPerMeter;
        camera.position.y = y * Renderer.PixelPerMeter;
        camera.update();
        renderer.batch.setProjectionMatrix(camera.combined);

        for (int i = 0; i < 10; ++i)
        {
            ArrayList<StaticEntity> se = staticEntities.get(i);
            int size = se.size();
            for (int j = 0; j < size; ++j)
            {
                StaticEntity staticEntity = se.get(j);
                if (parallaxX)
                    staticEntity.x = layerPositionX[i] + staticEntity.paddingX;
                else
                {
                    // do what?
                }

                if (parallaxY)
                    staticEntity.y = layerPositionY[i] + staticEntity.paddingY;
                else
                {
                    // do what?
                }

            }

            ArrayList<AnimatedEntity> ae = animatedEntities.get(i);
            size = ae.size();
            for (int j = 0; j < size; ++j)
            {
                AnimatedEntity animatedEntity = ae.get(j);
                if (parallaxX)
                    animatedEntity.x = layerPositionX[i] + animatedEntity.paddingX;
                else
                {
                    // do what?
                }

                if (parallaxY)
                    animatedEntity.y = layerPositionY[i] + animatedEntity.paddingY;
                else
                {
                    // do what?
                }

                animatedEntity.Update(timeSinceLastFrame);
            }
        }


    }
}
