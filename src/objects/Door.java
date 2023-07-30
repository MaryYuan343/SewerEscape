package objects;

import main.Game;

public class Door extends GameObject{

    public Door(int x, int y, int objType) {
        super(x, y, objType);

        // TODO: get correct values
        initHitbox(32, 32);
        xDrawOffset = 0;
        yDrawOffset = 0;

        hitbox.y += yDrawOffset + (int) (Game.SCALE * 2);
        hitbox.x += xDrawOffset / 2;
    }

    public void update(){

    }
}
