package com.comp2042;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBoardTest {

    private SimpleBoard board;

    @BeforeEach
    void setUp() {
        board = new SimpleBoard(10, 20); // standard Tetris board size
    }

    @Test
    void testCreateNewBrick() {
        boolean conflict = board.createNewBrick();
        assertNotNull(board.getCurrentBrick());
        assertEquals(new Point(4, 0), board.getCurrentBrickPosition());
        assertFalse(conflict);
    }

    @Test
    void testMoveBrickDown() {
        board.createNewBrick();
        Point initial = board.getCurrentBrickPosition();
        boolean moved = board.moveBrickDown();
        Point after = board.getCurrentBrickPosition();
        assertTrue(moved);
        assertEquals(initial.x, after.x);
        assertEquals(initial.y + 1, after.y);
    }

    @Test
    void testMoveBrickLeft() {
        board.createNewBrick();
        Point initial = board.getCurrentBrickPosition();
        boolean moved = board.moveBrickLeft();
        Point after = board.getCurrentBrickPosition();
        assertTrue(moved);
        assertEquals(initial.x - 1, after.x);
        assertEquals(initial.y, after.y);
    }

    @Test
    void testMoveBrickRight() {
        board.createNewBrick();
        Point initial = board.getCurrentBrickPosition();
        boolean moved = board.moveBrickRight();
        Point after = board.getCurrentBrickPosition();
        assertTrue(moved);
        assertEquals(initial.x + 1, after.x);
        assertEquals(initial.y, after.y);
    }

    @Test
    void testRotateLeftBrick() {
        board.createNewBrick();
        int[][] oldShape = board.getCurrentBrick().getShapeMatrix().get(0);
        boolean rotated = board.rotateLeftBrick();
        int[][] newShape = board.getCurrentBrick().getShapeMatrix().get(0);
        assertTrue(rotated);
        assertNotEquals(oldShape, newShape); // shape should change
    }

    @Test
    void testMergeBrickToBackground() {
        board.createNewBrick();
        int[][] before = board.getBoardMatrix();
        board.mergeBrickToBackground();
        int[][] after = board.getBoardMatrix();
        assertNotEquals(before, after);
    }

    @Test
    void testClearRows() {
        int[][] matrix = board.getBoardMatrix();
        int width = matrix[0].length; // number of columns
        for (int col = 0; col < width; col++) {
            matrix[0][col] = 1;
        }

        ClearRow clearRow = board.clearRows();
        assertEquals(1, clearRow.getLinesRemoved());
        assertEquals(50, clearRow.getScoreBonus());
    }


    @Test
    void testNewGameResetsBoard() {
        board.createNewBrick();
        board.mergeBrickToBackground();
        board.newGame();
        int[][] matrix = board.getBoardMatrix();
        for (int[] row : matrix) {
            for (int cell : row) {
                assertEquals(0, cell);
            }
        }
        assertEquals(0, board.getScore().getScore());
    }
}
