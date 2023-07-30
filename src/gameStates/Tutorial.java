package gameStates;

import static utils.Constants.Dialogue.DIALOGUE_HEIGHT;
import static utils.Constants.Dialogue.DIALOGUE_WIDTH;
import static utils.Constants.Dialogue.EXCLAMATION;
import static utils.Constants.Dialogue.QUESTION;
import static utils.Constants.Environment.BIG_CLOUD_HEIGHT;
import static utils.Constants.Environment.BIG_CLOUD_WIDTH;
import static utils.Constants.Environment.SMALL_CLOUD_HEIGHT;
import static utils.Constants.Environment.SMALL_CLOUD_WIDTH;

import effects.DialogueEffect;
import entities.EnemyManagerTutorial;
import entities.Player;
import entities.PlayerTutorial;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import levels.LevelManager;
import main.Game;
import objects.Key;
import objects.ObjectManager;
import objects.ObjectManagerTutorial;
import ui.GameCompletedOverlay;
import ui.GameCompletedOverlayTutorial;
import ui.GameOverOverlayTutorial;
import ui.LevelCompletedOverlay;
import ui.LevelCompletedOverlayTutorial;
import ui.PausedOverlay;
import ui.PausedOverlayTutorial;
import utils.LoadSave;

public class Tutorial extends State implements StateMethods {

    private PlayerTutorial player;
    private LevelManager levelManager;
    private EnemyManagerTutorial enemyManagerT;
    private ObjectManagerTutorial objectManagerT;
    private PausedOverlayTutorial pauseOverlayT;
    private GameOverOverlayTutorial gameOverOverlayT;
    private GameCompletedOverlayTutorial gameCompletedOverlayT;
    private LevelCompletedOverlayTutorial levelCompletedOverlayT;

    private boolean paused = true;

    private int xLvlOffset;
    private int leftBorder = (int) (0.25 * Game.GAME_WIDTH);
    private int rightBorder = (int) (0.75 * Game.GAME_WIDTH);
    private int maxLvlOffsetX;

    private BufferedImage backgroundImg, bigCloud, smallCloud;
    private BufferedImage[] questionImgs, exclamationImgs;
    private ArrayList<DialogueEffect> dialogEffects = new ArrayList<>();

    private boolean gameOver;
    private boolean lvlCompleted;
    private boolean gameCompleted;
    private boolean playerDying;

    private int[] smallCloudsPos;
    private Random rnd = new Random();

    public Tutorial(Game game) {
        super(game);
        initClasses();

        initClouds();
        loadDialogue();
        calcLvlOffset();
        // TODO: change it to load tutorial
        loadTutorial();
    }

    private void loadTutorial() {
        // TODO: write this method
        enemyManagerT.loadEnemies(levelManager.getTutorial());
        objectManagerT.loadObjects(levelManager.getTutorial());
    }

    private void initClasses() {
        levelManager = new LevelManager(game);
        // TODO: idk how to fix this tbh
        enemyManagerT = new EnemyManagerTutorial(this);
        objectManagerT = new ObjectManagerTutorial(this);

        player = new PlayerTutorial(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE), this);
        player.loadLvlData(levelManager.getTutorial().getLevelData());
        player.setSpawn(levelManager.getTutorial().getPlayerSpawn());

        pauseOverlayT = new PausedOverlayTutorial(this);
        gameOverOverlayT = new GameOverOverlayTutorial(this);
        // TODO: write levelcompleted
        levelCompletedOverlayT = new LevelCompletedOverlayTutorial(this);
        gameCompletedOverlayT = new GameCompletedOverlayTutorial(this);

    }

    private void initClouds() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
        bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
        smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
        smallCloudsPos = new int[8];
        for (int i = 0; i < smallCloudsPos.length; i++)
            smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));
    }

    private void loadDialogue() {
        loadDialogueImgs();

        // Load dialogue array with premade objects, that gets activated when needed.
        // This is a simple
        // way of avoiding ConcurrentModificationException error. (Adding to a list that
        // is being looped through.

        for (int i = 0; i < 10; i++)
            dialogEffects.add(new DialogueEffect(0, 0, EXCLAMATION));
        for (int i = 0; i < 10; i++)
            dialogEffects.add(new DialogueEffect(0, 0, QUESTION));

        for (DialogueEffect de : dialogEffects)
            de.deactive();
    }

    private void loadDialogueImgs() {
        BufferedImage qtemp = LoadSave.GetSpriteAtlas(LoadSave.QUESTION_ATLAS);
        questionImgs = new BufferedImage[5];
        for (int i = 0; i < questionImgs.length; i++)
            questionImgs[i] = qtemp.getSubimage(i * 14, 0, 14, 12);

        BufferedImage etemp = LoadSave.GetSpriteAtlas(LoadSave.EXCLAMATION_ATLAS);
        exclamationImgs = new BufferedImage[5];
        for (int i = 0; i < exclamationImgs.length; i++)
            exclamationImgs[i] = etemp.getSubimage(i * 14, 0, 14, 12);
    }

    private void calcLvlOffset() {
        // TODO: write another method in level manager that gets the tutorial
        maxLvlOffsetX = levelManager.getTutorial().getLvlOffset();
    }

    @Override
    public void update() {
        if (paused)
            pauseOverlayT.update();
        else if (lvlCompleted)
            levelCompletedOverlayT.update();
        else if (gameCompleted)
            gameCompletedOverlayT.update();
        else if (gameOver)
            gameOverOverlayT.update();
        else if (playerDying) {
            player.update();
        }
        else {
            updateDialogue();
            levelManager.update();
            objectManagerT.update(levelManager.getTutorial().getLevelData(), player);
            player.update();
            enemyManagerT.update(levelManager.getTutorial().getLevelData());
            checkCloseToBorder();

        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        drawClouds(g);

        levelManager.drawTutorial(g, xLvlOffset);
        enemyManagerT.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);
        objectManagerT.draw(g, xLvlOffset);
        objectManagerT.drawBackgroundTrees(g, xLvlOffset);
        drawDialogue(g, xLvlOffset);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            pauseOverlayT.draw(g);
        } else if (gameOver)
            gameOverOverlayT.draw(g);
        else if (lvlCompleted)
            levelCompletedOverlayT.draw(g);
        else if (gameCompleted)
            gameCompletedOverlayT.draw(g);

    }

    private void drawClouds(Graphics g) {
        for (int i = 0; i < 4; i++)
            g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

        for (int i = 0; i < smallCloudsPos.length; i++)
            g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
    }

    private void updateDialogue() {
        for (DialogueEffect de : dialogEffects)
            if (de.isActive())
                de.update();
    }

    private void drawDialogue(Graphics g, int xLvlOffset) {
        for (DialogueEffect de : dialogEffects)
            if (de.isActive()) {
                if (de.getType() == QUESTION)
                    g.drawImage(questionImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
                else
                    g.drawImage(exclamationImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
            }
    }

    public void addDialogue(int x, int y, int type) {
        // Not adding a new one, we are recycling. #ThinkGreen lol
        dialogEffects.add(new DialogueEffect(x, y - (int) (Game.SCALE * 15), type));
        for (DialogueEffect de : dialogEffects)
            if (!de.isActive())
                if (de.getType() == type) {
                    de.reset(x, -(int) (Game.SCALE * 15));
                    return;
                }
    }

    private void checkCloseToBorder() {
        int playerX = (int) player.getHitbox().x;
        int diff = playerX - xLvlOffset;

        if (diff > rightBorder)
            xLvlOffset += diff - rightBorder;
        else if (diff < leftBorder)
            xLvlOffset += diff - leftBorder;

        xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
    }

    public void resetAll() {
        gameOver = false;
        lvlCompleted = false;
        paused = false;

        playerDying = false;

        player.resetAll();

        enemyManagerT.resetAllEnemies();
        objectManagerT.resetAllObjects();
        dialogEffects.clear();
    }

    public void resetGameCompleted() {
        gameCompleted = false;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void checkObjectHit(Rectangle2D.Float attackBox) {
        objectManagerT.checkObjectHit(attackBox);
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        enemyManagerT.checkEnemyHit(attackBox);
    }

    public void checkPotionTouched(Rectangle2D.Float hitbox) {
        objectManagerT.checkObjectTouched(hitbox);
    }

    public void checkSpikesTouched(PlayerTutorial p) {
        objectManagerT.checkSpikesTouched(p);
    }

    public void checkKeyTouched(PlayerTutorial p){
        objectManagerT.checkKeyTouched(p, xLvlOffset);
    }

    public void checkDoorTouched(){
        objectManagerT.checkDoorTouched();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver && !gameCompleted)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    player.setLeft(true);
                    break;
                case KeyEvent.VK_D:

                    player.setRight(true);
                    break;
                case KeyEvent.VK_SPACE:
                    player.setJump(true);
                    break;
                case KeyEvent.VK_ESCAPE:
                    paused = !paused;
            }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!gameOver && !gameCompleted)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    player.setLeft(false);
                    break;
                case KeyEvent.VK_D:
                    player.setRight(false);
                    break;
                case KeyEvent.VK_SPACE:
                    player.setJump(false);
                    break;
            }
    }

    public void mouseDragged(MouseEvent e) {
        if (!gameOver && !gameCompleted)
            if (paused)
                pauseOverlayT.mouseDragged(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameOver) {
            if (e.getButton() == MouseEvent.BUTTON1)
                player.setAttacking(true);
            else if (e.getButton() == MouseEvent.BUTTON3)
                player.powerAttack();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver)
            gameOverOverlayT.mousePressed(e);
        else if (paused)
            pauseOverlayT.mousePressed(e);
        else if (lvlCompleted)
            levelCompletedOverlayT.mousePressed(e);
        else if (gameCompleted)
            gameCompletedOverlayT.mousePressed(e);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameOver)
            gameOverOverlayT.mouseReleased(e);
        else if (paused)
            pauseOverlayT.mouseReleased(e);
        else if (lvlCompleted)
            levelCompletedOverlayT.mouseReleased(e);
        else if (gameCompleted)
            gameCompletedOverlayT.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameOver)
            gameOverOverlayT.mouseMoved(e);
        else if (paused)
            pauseOverlayT.mouseMoved(e);
        else if (lvlCompleted)
            levelCompletedOverlayT.mouseMoved(e);
        else if (gameCompleted)
            gameCompletedOverlayT.mouseMoved(e);
    }

    public void setMaxLvlOffset(int lvlOffset) {
        this.maxLvlOffsetX = lvlOffset;
    }

    public void unpauseGame() {
        paused = false;
    }

    public void windowFocusLost() {
        player.resetDirBooleans();
    }

    public void setPlayerDying(boolean playerDying) {
        this.playerDying = playerDying;
    }

    public PlayerTutorial getPlayer() {
        return player;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public void setGameCompleted(boolean gameCompleted){
        this.gameCompleted = gameCompleted;
    }

    public ObjectManagerTutorial getObjectManagerT() {
        return objectManagerT;
    }

    public void setLevelCompleted(boolean levelCompleted) {
        game.getAudioPlayer().lvlCompleted();

        this.lvlCompleted = levelCompleted;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public boolean isLvlCompleted() {
        return lvlCompleted;
    }
}
