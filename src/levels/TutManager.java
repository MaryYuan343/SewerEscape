package levels;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import main.Game;
import utils.LoadSave;

public class TutManager {

    private Game game;
    private BufferedImage[] levelSprite;
    private BufferedImage[] waterSprite;
    private TutLevel tutorial;
    private int lvlIndex = 0, aniTick, aniIndex;

    public TutManager(Game game) {
        this.game = game;
        importOutsideSprites();
        createWater();
        buildTutorial();
    }

    private void buildTutorial() {
        BufferedImage tut = LoadSave.GetSpriteAtlas(LoadSave.TUTORIAL);
        tutorial = new TutLevel(tut);
    }

    private void createWater() {
        waterSprite = new BufferedImage[5];
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.WATER_TOP);
        for (int i = 0; i < 4; i++)
            waterSprite[i] = img.getSubimage(i * 32, 0, 32, 32);
        waterSprite[4] = LoadSave.GetSpriteAtlas(LoadSave.WATER_BOTTOM);
    }

    private void importOutsideSprites() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
        levelSprite = new BufferedImage[48];
        for (int j = 0; j < 4; j++)
            for (int i = 0; i < 12; i++) {
                int index = j * 12 + i;
                levelSprite[index] = img.getSubimage(i * 32, j * 32, 32, 32);
            }
    }

    public void drawTutorial(Graphics g, int lvlOffset) {
        for (int j = 0; j < Game.TILES_IN_HEIGHT; j++)
            for (int i = 0; i < tutorial.getLevelData()[0].length; i++) {
                int index = tutorial.getSpriteIndex(i, j);
                int x = Game.TILES_SIZE * i - lvlOffset;
                int y = Game.TILES_SIZE * j;
                if (index == 48)
                    g.drawImage(waterSprite[aniIndex], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
                else if (index == 49)
                    g.drawImage(waterSprite[4], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
                else
                    g.drawImage(levelSprite[index], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
            }
    }

    public void update() {
        updateWaterAnimation();
    }

    private void updateWaterAnimation() {
        aniTick++;
        if (aniTick >= 40) {
            aniTick = 0;
            aniIndex++;

            if (aniIndex >= 4)
                aniIndex = 0;
        }
    }

    public TutLevel getTutorial() {
        return tutorial;
    }

}
