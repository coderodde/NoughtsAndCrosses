package net.coderodde.game.crosses;

import java.util.Objects;

/**
 * This class implements the field of the Tic Tac Toe game.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 */
public class TicTacToeGrid {

    /**
     * The minimum allowed number of rows in the grid.
     */
    private static final int MINIMUM_ROWS = 3;

    /**
     * The minimum allowed number of columns in the grid.
     */
    private static final int MINIMUM_COLUMNS = 3;

    /**
     * The actual grid holding the cells.
     */
    private final Mark[][] grid;

    private final int winningLength;

    public TicTacToeGrid(int rows, int columns, int winningLength) {
        checkNumberOfRows(rows);
        checkNumberOfColumns(columns);
        checkWinningLength(winningLength, rows, columns);
        this.grid = new Mark[rows][columns];
        this.winningLength = winningLength;
    }

    public TicTacToeGrid(TicTacToeGrid grid) {
        this.grid = new Mark[grid.getHeight()][grid.getWidth()];
        this.winningLength = grid.winningLength;

        for (int y = 0; y < grid.getHeight(); ++y) {
            for (int x = 0; x < grid.getWidth(); ++x) {
                this.grid[y][x] = grid.read(x, y);
            }
        }
    }

    public boolean isFull() {
        for (int y = 0; y < getHeight(); ++y) {
            for (int x = 0; x < getWidth(); ++x) {
                if (grid[y][x] == null) {
                    return false;
                }
            }
        }

        return true;
    }

    public int getWinningLength() {
        return winningLength;
    }

    public int getHeight() {
        return grid.length;
    }

    public int getWidth() {
        return grid[0].length;
    }

    public void mark(int x, int y, Mark player) {
        Objects.requireNonNull(player, "The input player is null.");
        checkXCoordinate(x);
        checkYCoordinate(y);

        if (grid[y][x] != null) {
            throw new IllegalArgumentException(
                    "The cell at (x = " + x + ", y = " + y + ") is occupied.");
        }

        grid[y][x] = player;
    }

    public Mark read(int x, int y) {
        checkXCoordinate(x);
        checkYCoordinate(y);
        return grid[y][x];
    }

    public Mark getWinner() {
        int width = getWidth();
        int height = getHeight();
        int patternLength = getWinningLength();
        
        // Check diagonal patterns from top-left to bottom-right.
        for (int y = 0; y <= height - patternLength; ++y) {
            label1:
            for (int x = 0; x <= width - patternLength; ++x) {
                Mark mark = read(x, y);
                
                if (mark == null) {
                    continue;
                }
                
                for (int i = 1; i < patternLength; ++i) {
                    if (read(x + i, y + i) != mark) {
                        continue label1;
                    }
                }
                
                return mark;
            }
        }
        
        // Check diagonal patterns from top-right to bottom-left.
        for (int y = 0; y <= height - patternLength; ++y) {
            label2:
            for (int x = patternLength - 1; x < width; ++x) {
                Mark mark = read(x, y);
                
                if (mark == null) {
                    continue;
                }
                
                for (int i = 1; i < patternLength; ++i) {
                    if (read(x - i, y + i) != mark) {
                        continue label2;
                    }
                }
                
                return mark;
            }
        }
        
        // Check vertical patterns.
        for (int y = 0; y <= height - patternLength; ++y) {
            label3:
            for (int x = 0; x < width; ++x) {
                Mark mark = read(x, y);
                
                if (mark == null) {
                    continue;
                }
                
                for (int i = 1; i < patternLength; ++i) {
                    if (read(x, y + i) != mark) {
                        continue label3;
                    }
                }
                
                return mark;
            }
        }
        
        // Check horizontal patterns.
        for (int y = 0; y < height; ++y) {
            label4:
            for (int x = 0; x <= width - patternLength; ++x) {
                Mark mark = read(x, y);
                
                if (mark == null) {
                    continue;
                }
                
                for (int i = 1; i < patternLength; ++i) {
                    if (read(x + i, y) != mark) {
                        continue label4;
                    }
                }
                
                return mark;
            }
        }
        
        // No winner yet.
        return null;
    }
    
    public void set(TicTacToeGrid other) {
        if (getWidth() != other.getWidth()) {
            throw new IllegalArgumentException(
                    "Width mismatch: copying " + other.getWidth() + 
                    " columns to " + this.getWidth());
        }
        
        if (getHeight() != other.getHeight()) {
            throw new IllegalArgumentException(
                    "Height mismatch: copying " + other.getHeight() +
                    " rows to " + this.getHeight());
        }
        
        for (int y = 0; y < getHeight(); ++y) {
            for (int x = 0; x < getWidth(); ++x) {
                this.grid[y][x] = other.grid[y][x];
            }
        }
    }
    
    @Override
    public String toString() {
        int width = grid[0].length;
        int height = grid.length;

        StringBuilder sb = new StringBuilder((width + 1) * height);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (grid[y][x] == null) {
                    sb.append('.');
                } else if (grid[y][x].equals(Mark.X)) {
                    sb.append('X');
                } else if (grid[y][x].equals(Mark.O)) {
                    sb.append('O');
                } else {
                    throw new IllegalStateException(
                            "Unknown enumeration: " + grid[y][x].toString());
                }

                sb.append(' ');
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    private void checkNumberOfRows(int rows) {
        if (rows < MINIMUM_ROWS) {
            throw new IllegalArgumentException(
                    "Too few rows requested: " + rows + ". Must be at " +
                    "least " + MINIMUM_ROWS + ".");
        }
    }

    private void checkNumberOfColumns(int columns) {
        if (columns < MINIMUM_COLUMNS) {
            throw new IllegalArgumentException(
                    "Too few columns requested: " + columns + ". Must be at " +
                    "least " + MINIMUM_COLUMNS + ".");
        }
    }

    private void checkWinningLength(int winningLength, int rows, int columns) {
        int maxAllowedWinningLength = Math.min(rows, columns);

        if (winningLength > maxAllowedWinningLength) {
            throw new IllegalArgumentException(
                    "The grid cannot accommodate a winning patter of length " +
                    winningLength + ". Maximum allowed length is " +
                    maxAllowedWinningLength);
        }
    }

    private void checkXCoordinate(int x) {
        if (x <  0) {
            throw new IndexOutOfBoundsException(
                    "The X-coordinate is negative: " + x + ".");
        }

        if (x >= grid[0].length) {
            throw new IndexOutOfBoundsException(
                    "The X-coordinate is too large: " + x + ". Must be at " +
                    "most " + (grid[0].length - 1));
        }
    }

    private void checkYCoordinate(int y) {
        if (y <  0) {
            throw new IndexOutOfBoundsException(
                    "The Y-coordinate is negative: " + y + ".");
        }

        if (y >= grid.length) {
            throw new IndexOutOfBoundsException(
                    "The Y-coordinate is too large: " + y + ". Must be at " +
                    "most " + (grid.length - 1));
        }
    }
}
