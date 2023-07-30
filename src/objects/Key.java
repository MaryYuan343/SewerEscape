package objects;

import entities.Player;
import entities.PlayerTutorial;
import main.Game;
import static utils.Constants.ObjectConstants.*;

public class Key extends GameObject{

    private float hoverOffset;
    private int maxHoverOffset, hoverDir = 1;
    private boolean followPlayer;
    public Key(int x, int y, int objType) {
        super(x, y, objType);

        initHitbox(25, 25);

        xDrawOffset = (int) (9 * Game.SCALE);
        yDrawOffset = 0;

        maxHoverOffset = (int) (10 * Game.SCALE);
    }

    public void update() {
        updateHover();
    }

    private void updateHover() {
        hoverOffset += (0.075f * Game.SCALE * hoverDir);

        if (hoverOffset >= maxHoverOffset)
            hoverDir = -1;
        else if (hoverOffset < 0)
            hoverDir = 1;

        hitbox.y = y + hoverOffset + KEY_HEIGHT;
    }

    private void hover() {
        hoverOffset += (0.075f * Game.SCALE * hoverDir);

        if (hoverOffset >= maxHoverOffset)
            hoverDir = -1;
        else if (hoverOffset < 0)
            hoverDir = 1;

        hitbox.y += hoverOffset;
    }

    public void followPlayer(Player p, int xLvlOffset){
        followPlayer = true;
        hitbox.x = (int)p.getHitbox().x - KEY_TRAIL_X;
        hitbox.y = (int)p.getHitbox().y;
        hover();
    }

    public void followPlayer(PlayerTutorial p, int xLvlOffset){
        followPlayer = true;
        hitbox.x = (int)p.getHitbox().x - KEY_TRAIL_X;
        hitbox.y = (int)p.getHitbox().y;
        hover();
    }

    public boolean isFollowPlayer() {
        return followPlayer;
    }

    public void reset(){
        super.reset();
        hitbox.x = x;
        hitbox.y = y;
        followPlayer = false;
    }
}
