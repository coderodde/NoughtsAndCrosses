package net.coderodde.game.crosses;

/**
 * Implements a default heuristic function.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 4, 2015)
 */
public class HeuristicFunction {

    /**
     * Returns the heuristic estimate for {@code grid}. If the returned value is
     * positive infinity, the <tt>X</tt> player wins. If the returned values is
     * a negative infinity, the <tt>O</tt> player wins. If none wins, returns 
     * the heuristic estimate of {@code grid}, which will be finite.
     * 
     * @param grid the state to estimate.
     * @return a heuristic estimate.
     */
    public double estimate(TicTacToeGrid grid) {
        double sum = checkDiagonalLR(grid);

        if (Double.isInfinite(sum) && sum > 0.0) {
            return sum;
        }

        double sum2 = checkDiagonalRL(grid);

        if (Double.isInfinite(sum2) && sum2 > 0.0) {
            return sum2;
        }

        double sum3 = checkVertical(grid);

        if (Double.isInfinite(sum3) && sum3 > 0.0) {
            return sum3;
        }

        return sum + sum2 + sum3 + checkHorizontal(grid);
    }

    private double checkDiagonalLR(TicTacToeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int winLen = grid.getWinningLength();

        double value = 0.0;

        for (int y = 0; y < height - 1; ++y) {
            for (int x = 0; x < width - 1; ++x) {
                Mark mark = grid.read(x, y);

                if (mark == null) {
                    continue;
                }

                int scanned = 1;

                while (scanned < winLen
                        && y + scanned < height
                        && x + scanned < width) {
                    if (mark.equals(grid.read(x + scanned, y + scanned))) {
                        scanned++;
                    } else {
                        break;
                    }
                }

                if (scanned == winLen) {
                    return mark.equals(Mark.X) ? 
                            Double.POSITIVE_INFINITY : 
                            Double.NEGATIVE_INFINITY;
                } else if (scanned > 1) {
                    if (mark.equals(Mark.X)) {
                        value += scanned * scanned;
                    } else {
                        value += scanned * scanned;
                    }
                }
            }
        }

        return value;
    }

    private double checkDiagonalRL(TicTacToeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int winLen = grid.getWinningLength();

        double value = 0.0;

        for (int y = 0; y < height - 1; ++y) {
            for (int x = 1; x < width; ++x) {
                Mark mark = grid.read(x, y);

                if (mark == null) {
                    continue;
                }

                int scanned = 1;

                while (scanned < winLen 
                        && y + scanned < height 
                        && x - scanned >= 0) {
                    if (mark.equals(grid.read(x - scanned, y + scanned))) {
                        scanned++;
                    } else {
                        break;
                    }
                }

                if (scanned == winLen) {
                    return mark.equals(Mark.X) ? 
                            Double.POSITIVE_INFINITY : 
                            Double.NEGATIVE_INFINITY;
                } else if (scanned > 1) {
                    if (mark.equals(Mark.X)) {
                        value += scanned * scanned;
                    } else {
                        value -= scanned * scanned;
                    }
                }
            }
        }

        return value;
    }

    private double checkVertical(TicTacToeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int winLen = grid.getWinningLength();

        double value = 0.0;

        for (int y = 0; y < height - 1; ++y) {
            for (int x = 0; x < width; ++x) {
                Mark mark = grid.read(x, y);

                if (mark == null) {
                    continue;
                }

                int scanned = 1;

                while (scanned < winLen && y + scanned < height) {
                    if (mark.equals(grid.read(x, y + scanned))) {
                        scanned++;
                    } else {
                        break;
                    }
                }

                if (scanned == winLen) {
                    return mark.equals(Mark.X) ? 
                            Double.POSITIVE_INFINITY :
                            Double.NEGATIVE_INFINITY;
                } else if (scanned > 1) {
                    if (mark.equals(Mark.X)) {
                        value += scanned * scanned;
                    } else {
                        value -= scanned * scanned;
                    }
                }
            }
        }

        return value;
    }

    private double checkHorizontal(TicTacToeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int winLen = grid.getWinningLength();

        double value = 0.0;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width - 1; ++x) {
                Mark mark = grid.read(x, y);

                if (mark == null) {
                    continue;
                }

                int scanned = 1;

                while (scanned < winLen && x + scanned < width) {
                    if (mark.equals(grid.read(x + scanned, y))) {
                        scanned++;
                    } else {
                        break;
                    }
                }

                if (scanned == winLen) {
                    return mark.equals(Mark.X) ? 
                            Double.POSITIVE_INFINITY :
                            Double.NEGATIVE_INFINITY;
                } else if (scanned > 1) {
                    if (mark.equals(Mark.X)) {
                        value += scanned * scanned;
                    } else {
                        value -= scanned * scanned;
                    }
                }
            }
        }

        return value;
    }
}
