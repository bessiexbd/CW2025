package com.comp2042;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatrixOperationsTest {

    @Test
    void testIntersect_NoIntersection() {
        int[][] matrix = {
                {0,0,0},
                {0,0,0},
                {0,0,0}
        };

        int[][] brick = {
                {1,1},
                {1,1}
        };

        // Place brick at (0,0) – should not collide
        assertFalse(MatrixOperations.intersect(matrix, brick, 0, 0));
    }

    @Test
    void testIntersect_IntersectionWithFilledCell() {
        int[][] matrix = {
                {0,1,0},
                {0,0,0},
                {0,0,0}
        };

        int[][] brick = {
                {1}
        };

        assertTrue(MatrixOperations.intersect(matrix, brick, 1, 0));
    }

    @Test
    void testIntersect_OutOfBounds() {
        int[][] matrix = {
                {0,0,0},
                {0,0,0},
                {0,0,0}
        };

        int[][] brick = {
                {1}
        };

        assertTrue(MatrixOperations.intersect(matrix, brick, 3, 0));
    }

    @Test
    void testCopy() {
        int[][] original = {
                {1,2},
                {3,4}
        };

        int[][] copy = MatrixOperations.copy(original);

        assertArrayEquals(original, copy);
        assertNotSame(original, copy);
        assertNotSame(original[0], copy[0]); // ensure deep copy
    }

    @Test
    void testMerge() {
        int[][] matrix = {
                {0,0,0},
                {0,0,0},
                {0,0,0}
        };

        int[][] brick = {
                {2,2},
                {2,2}
        };

        int[][] result = MatrixOperations.merge(matrix, brick, 1, 1);

        int[][] expected = {
                {0,0,0},
                {0,2,2},
                {0,2,2}
        };

        assertArrayEquals(expected, result);
    }

    @Test
    void testCheckRemoving_SingleLineClear() {
        int[][] matrix = {
                {1,1,1},
                {0,1,0},
                {1,0,1}
        };

        ClearRow result = MatrixOperations.checkRemoving(matrix);

        // Only row 0 should be cleared
        assertEquals(1, result.getCleared());

        assertEquals(50, result.getScoreBonus());

        // Remaining rows should shift down
        int[][] expected = {
                {0,0,0},
                {0,1,0},
                {1,0,1}
        };

        assertArrayEquals(expected, result.getMatrix());
    }

    @Test
    void testCheckRemoving_MultipleRowsClear() {
        int[][] matrix = {
                {1,1,1},
                {1,1,1},
                {0,1,0}
        };

        ClearRow result = MatrixOperations.checkRemoving(matrix);

        assertEquals(2, result.getCleared());
        assertEquals(200, result.getScoreBonus());
        int[][] expected = {
                {0,0,0},
                {0,0,0},
                {0,1,0}
        };

        assertArrayEquals(expected, result.getMatrix());
    }
}
