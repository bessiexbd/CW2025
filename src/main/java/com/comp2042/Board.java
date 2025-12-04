package com.comp2042;

import com.comp2042.logic.bricks.Brick;

public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData getViewData();

    ViewData getNextBrickViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    Score getScore();

    void newGame();

    Brick getCurrentBrick();

    void swapWithHeldBrick(Brick brick);

}
