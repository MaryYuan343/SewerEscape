package entities;

import static utils.Constants.EnemyConstants.*;
import static utils.HelpMethods.IsFloor;
import static utils.Constants.Dialogue.*;

import gameStates.Playing;
import gameStates.Tutorial;

public class Crabby extends Enemy {

    public Crabby(float x, float y) {
        super(x, y, CRABBY_WIDTH, CRABBY_HEIGHT, CRABBY);
        initHitbox(22, 19);
        initAttackBox(82, 19, 30);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
        updateAttackBox();
    }

    public void update(int[][] lvlData, Tutorial tutorial) {
        updateBehavior(lvlData, tutorial);
        updateAnimationTick();
        updateAttackBox();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir) {
            inAirChecks(lvlData, playing);
        } else {
            switch (state) {
                case IDLE:
                    if (IsFloor(hitbox, lvlData))
                        newState(RUNNING);
                    else
                        inAir = true;
                    break;
                case RUNNING:
                    if (canSeePlayer(lvlData, playing.getPlayer())) {
                        turnTowardsPlayer(playing.getPlayer());
                        if (isPlayerCloseForAttack(playing.getPlayer()))
                            newState(ATTACK);
                    }
                    move(lvlData);

                    if (inAir)
                        playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);

                    break;
                case ATTACK:
                    if (aniIndex == 0)
                        attackChecked = false;
                    if (aniIndex == 3 && !attackChecked)
                        checkPlayerHit(attackBox, playing.getPlayer());
                    break;
                case HIT:
                    if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
                        pushBack(pushBackDir, lvlData, 2f);
                    updatePushBackDrawOffset();
                    break;
            }
        }
    }

    private void updateBehavior(int[][] lvlData, Tutorial tutorial) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir) {
            inAirChecks(lvlData, tutorial);
        } else {
            switch (state) {
                case IDLE:
                    if (IsFloor(hitbox, lvlData))
                        newState(RUNNING);
                    else
                        inAir = true;
                    break;
                case RUNNING:
                    if (canSeePlayer(lvlData, tutorial.getPlayer())) {
                        turnTowardsPlayer(tutorial.getPlayer());
                        if (isPlayerCloseForAttack(tutorial.getPlayer()))
                            newState(ATTACK);
                    }
                    move(lvlData);

                    if (inAir)
                        tutorial.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);

                    break;
                case ATTACK:
                    if (aniIndex == 0)
                        attackChecked = false;
                    if (aniIndex == 3 && !attackChecked)
                        checkPlayerHit(attackBox, tutorial.getPlayer());
                    break;
                case HIT:
                    if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
                        pushBack(pushBackDir, lvlData, 2f);
                    updatePushBackDrawOffset();
                    break;
            }
        }
    }

}