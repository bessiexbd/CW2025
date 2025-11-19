package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.logging.Level;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;
    private IntegerProperty lines = new SimpleIntegerProperty(0);
    //    make level an integer property for binding
    private IntegerProperty level = new SimpleIntegerProperty(1);

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindLine(lines);
        viewGuiController.bindLevel(level);
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
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix());

        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }
        return new DownData(clearRow, board.getViewData());
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
    public void createNewGame() {
        board.newGame();

//        reset lines and level when starting a new game
        lines .setValue(0);
        level.setValue(1);

        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
}
