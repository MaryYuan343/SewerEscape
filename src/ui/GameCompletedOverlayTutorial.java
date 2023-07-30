package ui;

import gameStates.Tutorial;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import gameStates.Gamestate;
import gameStates.Playing;
import main.Game;
import utils.LoadSave;

public class GameCompletedOverlayTutorial {
    private Tutorial tutorial;
    private BufferedImage img;
    private MenuButton quit, credit;
    private int imgX, imgY, imgW, imgH;

    public GameCompletedOverlayTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
        createImg();
        createButtons();
    }

    private void createButtons() {
        quit = new MenuButton(Game.GAME_WIDTH / 2, (int) (270 * Game.SCALE), 2, Gamestate.MENU);
        credit = new MenuButton(Game.GAME_WIDTH / 2, (int) (200 * Game.SCALE), 3, Gamestate.CREDITS);
    }

    private void createImg() {
        img = LoadSave.GetSpriteAtlas(LoadSave.GAME_COMPLETED);
        imgW = (int) (img.getWidth() * Game.SCALE);
        imgH = (int) (img.getHeight() * Game.SCALE);
        imgX = Game.GAME_WIDTH / 2 - imgW / 2;
        imgY = (int) (100 * Game.SCALE);

    }

    public void draw(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        g.drawImage(img, imgX, imgY, imgW, imgH, null);

        credit.draw(g);
        quit.draw(g);
    }

    public void update() {
        credit.update();
        quit.update();
    }

    private boolean isIn(MenuButton b, MouseEvent e) {
        return b.getBounds().contains(e.getX(), e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        credit.setMouseOver(false);
        quit.setMouseOver(false);

        if (isIn(quit, e))
            quit.setMouseOver(true);
        else if (isIn(credit, e))
            credit.setMouseOver(true);
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(quit, e)) {
            if (quit.isMousePressed()) {
                tutorial.resetAll();
                tutorial.resetGameCompleted();
                tutorial.setGamestate(Gamestate.MENU);

            }
        } else if (isIn(credit, e))
            if (credit.isMousePressed()) {
                tutorial.resetAll();
                tutorial.resetGameCompleted();
                tutorial.setGamestate(Gamestate.CREDITS);
            }

        quit.resetBools();
        credit.resetBools();
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(quit, e))
            quit.setMousePressed(true);
        else if (isIn(credit, e))
            credit.setMousePressed(true);
    }
}