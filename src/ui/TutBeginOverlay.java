package ui;

import gameStates.Tutorial;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import gameStates.Gamestate;
import gameStates.Playing;
import main.Game;
import utils.LoadSave;
import static utils.Constants.UI.URMButtons.*;

public class TutBeginOverlay {

    private Tutorial tutorial;
    private BufferedImage backgroundImg;
    private int bgX, bgY, bgW, bgH;
    private UrmButton menuB, unpauseB;

    public TutBeginOverlay(Tutorial tutorial) {
        this.tutorial = tutorial;
        loadBackground();
        createUrmButtons();
    }

    private void createUrmButtons() {
        int menuX = (int) (340 * Game.SCALE);
        int unpauseX = (int) (435 * Game.SCALE);
        int bY = (int) (325 * Game.SCALE);

        menuB = new UrmButton(menuX, bY, URM_SIZE, URM_SIZE, 2);
        unpauseB = new UrmButton(unpauseX, bY, URM_SIZE, URM_SIZE, 0);
    }

    private void loadBackground() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.TUT_START);
        bgW = (int) (backgroundImg.getWidth() * Game.SCALE);
        bgH = (int) (backgroundImg.getHeight() * Game.SCALE);
        bgX = Game.GAME_WIDTH / 2 - bgW / 2;
        bgY = (int) (25 * Game.SCALE);
    }

    public void update() {

        menuB.update();
        unpauseB.update();

    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, bgX, bgY, bgW, bgH, null);

        // UrmButtons
        menuB.draw(g);
        unpauseB.draw(g);

    }

    public void mouseDragged(MouseEvent e) {
        // not used
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(e, menuB))
            menuB.setMousePressed(true);
        else if (isIn(e, unpauseB))
            unpauseB.setMousePressed(true);
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(e, menuB)) {
            if (menuB.isMousePressed()) {
                tutorial.resetAll();
                tutorial.setGamestate(Gamestate.MENU);
                tutorial.notStart();
            }
        } else if (isIn(e, unpauseB)) {
            if (unpauseB.isMousePressed())
                tutorial.notStart();
        }

        menuB.resetBools();
        unpauseB.resetBools();

    }

    public void mouseMoved(MouseEvent e) {
        menuB.setMouseOver(false);
        unpauseB.setMouseOver(false);

        if (isIn(e, menuB))
            menuB.setMouseOver(true);
        else if (isIn(e, unpauseB))
            unpauseB.setMouseOver(true);
    }

    private boolean isIn(MouseEvent e, PauseButton b) {
        return b.getBounds().contains(e.getX(), e.getY());
    }

}