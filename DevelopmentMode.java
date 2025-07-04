package com.spartans;

import com.engine.Log;
import com.engine.Renderer;
import com.spartans.player.Player;

public class DevelopmentMode
{
    
    public enum State
    {
        Inactive,
        LevelEditing
    }

    public State state;

    public float lastPlayerX, lastPlayerY;
    
    public float lastCameraManagerX, lastCameraManagerY;

    public DevelopmentMode()
    {
        state = State.Inactive;
    }
    
    public void StorePlayerStatus(Player player)
    {
        lastPlayerX = player.x;
        lastPlayerY = player.y;
    }
    
    public void RestorePlayerStatus(Player player)
    {
        player.x = lastPlayerX;
        player.y = lastPlayerY;
    }
    
    public void StoreCameraManager(CameraManager cameraManager)
    {
        lastCameraManagerX = cameraManager.x;
        lastCameraManagerY = cameraManager.y;
    }
    
    public void RestoreCameraManager(CameraManager cameraManager)
    {
        cameraManager.SetX(lastCameraManagerX);
        cameraManager.SetY(lastCameraManagerY);

    }
    
}
