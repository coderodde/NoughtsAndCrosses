package net.coderodde.game.crosses;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generates next states from a given state for a particular player.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 4, 2015)
 */
public class MoveGenerator {

    public List<TicTacToeGrid> generateMoves(TicTacToeGrid state, Mark player) {
        int width = state.getWidth();
        int height = state.getHeight();

        int minX = width;
        int maxX = 0;

        int minY = height;
        int maxY = 0;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Mark mark = state.read(x, y);

                if (mark == null) {
                    continue;
                }

                if (minX > x) {
                    minX = x;
                } 

                if (maxX < x) {
                    maxX = x;
                }

                if (minY > y) {
                    minY = y;
                }

                if (maxY < y) {
                    maxY = y;
                }
            }
        }

        minX = Math.max(0, minX - 2);
        maxX = Math.min(width - 1, maxX + 2);
        minY = Math.max(0, minY - 2);
        maxY = Math.min(height - 1, maxY + 2);

        List<TicTacToeGrid> next = new ArrayList<>((maxX - minX + 1) *
                                                   (maxY - minY + 1));

        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                Mark mark = state.read(x, y);

                if (mark == null) {
                    TicTacToeGrid grid = new TicTacToeGrid(state);
                    grid.mark(x, y, player);
                    next.add(grid);
                }
            }
        }

        return next;
    }
}
