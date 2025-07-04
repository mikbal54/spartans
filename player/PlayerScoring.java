package com.spartans.player;

// oyuncunun hangi durumlarda ne kadar puan alacagini filan hesapliyor
public class PlayerScoring
{
    public static enum ScoreType
    {
        JumpOverSlidingEnemy,
        SlideUnderHeadButtingEnemy
    }

    public static int GetPoints(ScoreType type, float val)
    {
        switch (type)
        {
        case JumpOverSlidingEnemy:
            if (val > 80)
                return 100;
            return (int) val + 10;
        case SlideUnderHeadButtingEnemy:
            if (val > 80)
                return 100;
            return (int) val + 10;
        default:
            break;
        }
        return 0;
    }
}
