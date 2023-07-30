package objects;

import entities.PlayerTutorial;
import gameStates.Tutorial;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Enemy;
import entities.Player;
import gameStates.Playing;
import levels.Level;
import levels.TutLevel;
import main.Game;
import utils.LoadSave;
import static utils.Constants.ObjectConstants.*;
import static utils.HelpMethods.CanCannonSeePlayer;
import static utils.HelpMethods.IsProjectileHittingLevel;
import static utils.Constants.Projectiles.*;

public class ObjectManagerTutorial {

    private Tutorial tutorial;
    private BufferedImage[][] potionImgs, containerImgs;
    private BufferedImage[] cannonImgs, grassImgs;
    private BufferedImage[][] treeImgs;
    private BufferedImage spikeImg, cannonBallImg, keyImg, doorClose, doorOpen;
    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers;
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    private TutLevel tutLevel;

    public ObjectManagerTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
        tutLevel = tutorial.getLevelManager().getTutorial();
        loadImgs();
    }

    public void checkSpikesTouched(PlayerTutorial p) {
        for (Spike s : tutLevel.getSpikes())
            if (s.getHitbox().intersects(p.getHitbox()))
                p.kill();
    }

    public void checkSpikesTouched(Enemy e) {
        for (Spike s : tutLevel.getSpikes())
            if (s.getHitbox().intersects(e.getHitbox()))
                e.hurt(200);
    }

    public void checkKeyTouched(PlayerTutorial p, int xLvlOffset){
        if (tutLevel.getKey().getHitbox().intersects(p.getHitbox())
                && !tutLevel.getKey().isFollowPlayer())
            tutLevel.getKey().followPlayer(p, xLvlOffset);
        else if (tutLevel.getKey().isFollowPlayer())
            tutLevel.getKey().followPlayer(p, xLvlOffset);
    }

    public void checkDoorTouched(){
        if (tutLevel.getDoor().getHitbox().intersects(tutLevel.getKey().getHitbox())){
            tutLevel.getDoor().setActive(false);
            tutLevel.getKey().setActive(false);

            tutorial.setLevelCompleted(true);
        }
    }

    public void checkObjectTouched(Rectangle2D.Float hitbox) {
        for (Potion p : potions)
            if (p.isActive()) {
                if (hitbox.intersects(p.getHitbox())) {
                    p.setActive(false);
                    applyEffectToPlayer(p);
                }
            }
    }

    public void applyEffectToPlayer(Potion p) {
        if (p.getObjType() == RED_POTION)
            tutorial.getPlayer().changeHealth(RED_POTION_VALUE);
        else
            tutorial.getPlayer().changePower(BLUE_POTION_VALUE);
    }

    public void checkObjectHit(Rectangle2D.Float attackbox) {
        for (GameContainer gc : containers)
            if (gc.isActive() && !gc.doAnimation) {
                if (gc.getHitbox().intersects(attackbox)) {
                    gc.setAnimation(true);
                    int type = 0;
                    if (gc.getObjType() == BARREL)
                        type = 1;
                    potions.add(new Potion((int) (gc.getHitbox().x + gc.getHitbox().width / 2),
                            (int) (gc.getHitbox().y - gc.getHitbox().height / 2) + 10, type));
                    return;
                }
            }
    }

    public void loadObjects(TutLevel newLevel) {
        tutLevel = newLevel;
        potions = new ArrayList<>(newLevel.getPotions());
        containers = new ArrayList<>(newLevel.getContainers());
        projectiles.clear();
    }

    private void loadImgs() {
        BufferedImage potionSprite = LoadSave.GetSpriteAtlas(LoadSave.POTION_ATLAS);
        potionImgs = new BufferedImage[2][7];

        for (int j = 0; j < potionImgs.length; j++)
            for (int i = 0; i < potionImgs[j].length; i++)
                potionImgs[j][i] = potionSprite.getSubimage(12 * i, 16 * j, 12, 16);

        BufferedImage containerSprite = LoadSave.GetSpriteAtlas(LoadSave.CONTAINER_ATLAS);
        containerImgs = new BufferedImage[2][8];

        for (int j = 0; j < containerImgs.length; j++)
            for (int i = 0; i < containerImgs[j].length; i++)
                containerImgs[j][i] = containerSprite.getSubimage(40 * i, 30 * j, 40, 30);

        spikeImg = LoadSave.GetSpriteAtlas(LoadSave.TRAP_ATLAS);

        keyImg = LoadSave.GetSpriteAtlas(LoadSave.KEY);

        doorClose = LoadSave.GetSpriteAtlas(LoadSave.DOOR_CLOSE);

        doorOpen = LoadSave.GetSpriteAtlas(LoadSave.DOOR_OPEN);

        cannonImgs = new BufferedImage[7];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CANNON_ATLAS);

        for (int i = 0; i < cannonImgs.length; i++)
            cannonImgs[i] = temp.getSubimage(i * 40, 0, 40, 26);

        cannonBallImg = LoadSave.GetSpriteAtlas(LoadSave.CANNON_BALL);
        treeImgs = new BufferedImage[2][4];
        BufferedImage treeOneImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_ONE_ATLAS);
        for (int i = 0; i < 4; i++)
            treeImgs[0][i] = treeOneImg.getSubimage(i * 39, 0, 39, 92);

        BufferedImage treeTwoImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_TWO_ATLAS);
        for (int i = 0; i < 4; i++)
            treeImgs[1][i] = treeTwoImg.getSubimage(i * 62, 0, 62, 54);

        BufferedImage grassTemp = LoadSave.GetSpriteAtlas(LoadSave.GRASS_ATLAS);
        grassImgs = new BufferedImage[2];
        for (int i = 0; i < grassImgs.length; i++)
            grassImgs[i] = grassTemp.getSubimage(32 * i, 0, 32, 32);
    }

    public void update(int[][] lvlData, PlayerTutorial player) {
        updateBackgroundTrees();
        for (Potion p : potions)
            if (p.isActive())
                p.update();

        for (GameContainer gc : containers)
            if (gc.isActive())
                gc.update();


        tutLevel.getKey().update();

        updateCannons(lvlData, player);
        updateProjectiles(lvlData, player);

    }

    private void updateBackgroundTrees() {
        for (BackgroundTree bt : tutLevel.getTrees())
            bt.update();
    }

    private void updateProjectiles(int[][] lvlData, PlayerTutorial player) {
        for (Projectile p : projectiles)
            if (p.isActive()) {
                p.updatePos();
                if (p.getHitbox().intersects(player.getHitbox())) {
                    // TODO: change cannon hit after testing
                    player.changeHealth(PROJECTILE_DMG, p);
                    p.setActive(false);
                } else if (IsProjectileHittingLevel(p, lvlData))
                    p.setActive(false);
            }
    }

    private boolean isPlayerInRange(Cannon c, PlayerTutorial player) {
        int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
        return absValue <= Game.TILES_SIZE * 5;
    }

    private boolean isPlayerInfrontOfCannon(Cannon c, PlayerTutorial player) {
        if (c.getObjType() == CANNON_LEFT) {
            if (c.getHitbox().x > player.getHitbox().x)
                return true;

        } else if (c.getHitbox().x < player.getHitbox().x)
            return true;
        return false;
    }

    private void updateCannons(int[][] lvlData, PlayerTutorial player) {
        for (Cannon c : tutLevel.getCannons()) {
            if (!c.doAnimation)
                if (c.getTileY() == player.getTileY())
                    if (isPlayerInRange(c, player))
                        if (isPlayerInfrontOfCannon(c, player))
                            if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY()))
                                c.setAnimation(true);

            c.update();
            if (c.getAniIndex() == 4 && c.getAniTick() == 0)
                shootCannon(c);
        }
    }

    private void shootCannon(Cannon c) {
        int dir = 1;
        if (c.getObjType() == CANNON_LEFT)
            dir = -1;

        projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawPotions(g, xLvlOffset);
        drawContainers(g, xLvlOffset);
        drawTraps(g, xLvlOffset);
        drawKey(g, xLvlOffset);
        drawDoor(g, xLvlOffset);
        drawCannons(g, xLvlOffset);
        drawProjectiles(g, xLvlOffset);
        drawGrass(g, xLvlOffset);
    }

    private void drawDoor(Graphics g, int xLvlOffset) {
        BufferedImage temp = doorClose;
        if(!tutLevel.getDoor().isActive())
            temp = doorOpen;

        g.drawImage(temp, (int) (tutLevel.getDoor().getHitbox().x - xLvlOffset),
                (int) (tutLevel.getDoor().getHitbox().y - tutLevel.getDoor().getyDrawOffset() - 1),
                DOOR_WIDTH, DOOR_HEIGHT, null);

        //tutLevel.getDoor().drawHitbox(g, xLvlOffset);
    }

    private void drawGrass(Graphics g, int xLvlOffset) {
        for (Grass grass : tutLevel.getGrass())
            g.drawImage(grassImgs[grass.getType()], grass.getX() - xLvlOffset, grass.getY(), (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
    }

    public void drawBackgroundTrees(Graphics g, int xLvlOffset) {
        for (BackgroundTree bt : tutLevel.getTrees()) {

            int type = bt.getType();
            if (type == 9)
                type = 8;
            g.drawImage(treeImgs[type - 7][bt.getAniIndex()], bt.getX() - xLvlOffset + GetTreeOffsetX(bt.getType()), (int) (bt.getY() + GetTreeOffsetY(bt.getType())), GetTreeWidth(bt.getType()),
                    GetTreeHeight(bt.getType()), null);
        }
    }

    private void drawProjectiles(Graphics g, int xLvlOffset) {
        for (Projectile p : projectiles)
            if (p.isActive())
                g.drawImage(cannonBallImg, (int) (p.getHitbox().x - xLvlOffset), (int) (p.getHitbox().y), CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT, null);
    }

    private void drawCannons(Graphics g, int xLvlOffset) {
        for (Cannon c : tutLevel.getCannons()) {
            int x = (int) (c.getHitbox().x - xLvlOffset);
            int width = CANNON_WIDTH;

            if (c.getObjType() == CANNON_RIGHT) {
                x += width;
                width *= -1;
            }
            g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y), width, CANNON_HEIGHT, null);
        }
    }

    private void drawTraps(Graphics g, int xLvlOffset) {
        for (Spike s : tutLevel.getSpikes())
            g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset()), SPIKE_WIDTH, SPIKE_HEIGHT, null);

    }

    private void drawKey(Graphics g, int xLvlOffset){
        if (tutLevel.getKey().isActive()){
            g.drawImage(keyImg, (int) (tutLevel.getKey().getHitbox().x - xLvlOffset),
                    (int) (tutLevel.getKey().getHitbox().y - tutLevel.getKey()
                            .getyDrawOffset()),
                    KEY_WIDTH, KEY_HEIGHT, null);
        }

    }

    private void drawContainers(Graphics g, int xLvlOffset) {
        for (GameContainer gc : containers)
            if (gc.isActive()) {
                int type = 0;
                if (gc.getObjType() == BARREL)
                    type = 1;
                g.drawImage(containerImgs[type][gc.getAniIndex()], (int) (gc.getHitbox().x - gc.getxDrawOffset() - xLvlOffset), (int) (gc.getHitbox().y - gc.getyDrawOffset()), CONTAINER_WIDTH,
                        CONTAINER_HEIGHT, null);
            }
    }

    private void drawPotions(Graphics g, int xLvlOffset) {
        for (Potion p : potions)
            if (p.isActive()) {
                int type = 0;
                if (p.getObjType() == RED_POTION)
                    type = 1;
                g.drawImage(potionImgs[type][p.getAniIndex()], (int) (p.getHitbox().x - p.getxDrawOffset() - xLvlOffset), (int) (p.getHitbox().y - p.getyDrawOffset()), POTION_WIDTH, POTION_HEIGHT,
                        null);
            }
    }

    public void resetAllObjects() {
        loadObjects(tutorial.getLevelManager().getTutorial());
        for (Potion p : potions)
            p.reset();
        for (GameContainer gc : containers)
            gc.reset();
        for (Cannon c : tutLevel.getCannons())
            c.reset();
        tutLevel.getKey().reset();
        tutLevel.getDoor().reset();
    }
}