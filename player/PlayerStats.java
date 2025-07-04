package com.spartans.player;

// oyununun ozellikleri. bunlar itemlarla filan degisebilir
public class PlayerStats
{
    public float jumpHeight;
    public float jumpSpeed;
    public float runSpeed;

    public float shootSpeed;

    public float slideGetUpDuration;
    public float slidingDuration;
    public float dribbleVelocityX;

    public float invunerableDuration;

    PlayerStats()
    {
        // kosma hizi
        runSpeed = 7.1f;

        invunerableDuration = 1f;

        // ziplama yuksekligi
        jumpHeight = 3;

        // ziplama hizi
        jumpSpeed = 8;

        // kayma suresi, saniye
        slidingDuration = 0.6f;
        // kayma basladiktan kalkma animasyonuna kadar olan zaman
        slideGetUpDuration = 0.5f;

        // topu firlatma hizi, metre/saniye
        shootSpeed = runSpeed + 14;

        dribbleVelocityX = 5f;
    }

}
