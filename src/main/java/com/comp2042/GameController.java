package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.logging.Level;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;
    private IntegerProperty lines = new SimpleIntegerProperty(0);
    //    make level an integer property for binding
    private IntegerProperty level = new SimpleIntegerProperty(1);

//hold feature
    private Brick holdBrick = null;
    private boolean hasUsedHold = false;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindLine(lines);
        viewGuiController.bindLevel(level);
//        update the next brick preview
        updateNextBrickPreview();
    }
    private void updateNextBrickPreview() {
        viewGuiController.updateNextBrick(board.getNextBrickViewData());
    }


    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                lines.setValue(lines.getValue() + clearRow.getLinesRemoved());
//update the value
                updateLevel(lines.getValue());
            }

            hasUsedHold = false;

            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
            updateNextBrickPreview();

        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }
        return new DownData(clearRow, board.getViewData(),0,event.getEventSource() == EventSource.USER);
    }

    @Override
    public DownData onHardDropEvent(MoveEvent event) {
        int dropCount = 0;
        while (board.moveBrickDown()) {
            dropCount++;
            board.getScore().add(2);
        }
        board.mergeBrickToBackground();
        ClearRow clearRow = board.clearRows();
        if (clearRow.getLinesRemoved() > 0) {
            board.getScore().add(clearRow.getScoreBonus());
            lines.setValue(lines.getValue() + clearRow.getLinesRemoved());
            updateLevel(lines.getValue());
        }
//        reset hold when new piece spawns
        hasUsedHold = false;

        if (board.createNewBrick()) {
            viewGuiController.gameOver();
        }
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        updateNextBrickPreview();
        return new DownData(clearRow, board.getViewData() ,dropCount,event.getEventSource() == EventSource.USER);
    }
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    @Override
    public ViewData getGhostPosition(ViewData currentBrick) {

        int originalY = currentBrick.getyPosition();
        int originalX = currentBrick.getxPosition();
        int[][] originalData = currentBrick.getBrickData();
        int[][] nextBrickData = currentBrick.getNextBrickData();
        int ghostY = originalY;
        int[][] boardMatrix = board.getBoardMatrix();

        while (canPlaceBrick(originalX, ghostY + 1, originalData, boardMatrix)) {
            ghostY++;
        }

        return new ViewData(
                originalData,      // brickData
                originalX,         // xPosition
                ghostY,            // yPosition
                nextBrickData      // next brick preview
        );
    }
    private boolean canPlaceBrick(int x, int y, int[][] brickData, int[][] boardMatrix) {
        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                if (brickData[i][j] != 0) {
                    int boardY = y + i;
                    int boardX = x + j;

                    // Check boundaries
                    if (boardY < 0 || boardY >= boardMatrix.length ||
                            boardX < 0 || boardX >= boardMatrix[0].length) {
                        return false;
                    }

                    // Check collision with existing blocks
                    if (boardMatrix[boardY][boardX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void updateLevel(int lines){
//        update the level according to the lines
        int newLevel = 1 + lines / 5;
//      only update if level actually changed
        if(newLevel != level.getValue() && newLevel != 12){
            level.setValue(newLevel);
            viewGuiController.updateGameSpeed(400 - (30 * newLevel-1));
            System.out.print(400 - (30 * newLevel-1));
        }
    }

    @Override
    public ViewData onHoldEvent() {
        // Prevent using hold multiple times for the same piece
        if (hasUsedHold) {
            return null;
        }

        Brick currentBrick = board.getCurrentBrick();

        if (holdBrick == null) {
            // First time holding - store current brick and spawn new one
            holdBrick = currentBrick;
            hasUsedHold = true;

            if (board.createNewBrick()) {
                viewGuiController.gameOver();
                return null;
            }

            // Update hold display with the brick we just stored
            viewGuiController.updateHoldBrick(new ViewData(
                    holdBrick.getShapeMatrix().get(0), 0, 0,
                    holdBrick.getShapeMatrix().get(0)
            ));
            updateNextBrickPreview();

        } else {
            // Swap current brick with held brick
            Brick temp = holdBrick;
            holdBrick = currentBrick;
            hasUsedHold = true;

            board.swapWithHeldBrick(temp);


            // Update hold display with the new brick we just stored
            viewGuiController.updateHoldBrick(new ViewData(
                    holdBrick.getShapeMatrix().get(0), 0, 0,
                    holdBrick.getShapeMatrix().get(0)
            ));
        }

        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();

//        reset hold feature
        holdBrick = null;
        hasUsedHold = false;
//        reset lines and level when starting a new game
        lines .setValue(0);
        level.setValue(1);

        viewGuiController.refreshGameBackground(board.getBoardMatrix());
//        update next brick for new game
        updateNextBrickPreview();
        viewGuiController.updateHoldBrick(null);
    }
}
