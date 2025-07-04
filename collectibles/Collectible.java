package com.spartans.collectibles;

import com.spartans.player.Player;

public interface Collectible
{
    public void Update(float timePassed, Player player);

    public void Restart();

}
