package net.coderodde.game.crosses;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * This thread is responsible for running the AI.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 7, 2015)
 */
class AIThread extends Thread {

    private static final double LARGE = 1e10;

    private final ConfigurationFrame configurationFrame;
    private final GameFrame gameFrame;
    private final JProgressBar progressBar;
    private final TicTacToeGrid grid;
    private final TicTacToePanel canvas;
    private final MoveGenerator moveGenerator;
    private final HeuristicFunction heuristicFunction;
    private final int maximumDepth;

    AIThread(ConfigurationFrame configurationFrame,
             GameFrame gameFrame,
             TicTacToeGrid grid, 
             TicTacToePanel canvas,
             JProgressBar progressBar,
             MoveGenerator moveGenerator,
             HeuristicFunction heuristicFunction,
             int maximumDepth) {
        this.configurationFrame = configurationFrame;
        this.gameFrame = gameFrame;
        this.grid = grid;
        this.canvas = canvas;
        this.progressBar = progressBar; 
        this.moveGenerator = moveGenerator;
        this.heuristicFunction = heuristicFunction;
        this.maximumDepth = maximumDepth;
    }

    @Override
    public void run() {
        canvas.lock(); // Make sure that the user's clicks do not modify the 
                       // grid.
        progressBar.setValue(0);
        Dimension dimension = gameFrame.getSize();
        dimension.height += progressBar.getHeight();
        gameFrame.setSize(dimension);
        progressBar.setVisible(true);

        List<TicTacToeGrid> nextStateList = moveGenerator.generateMoves(grid, 
                                                                        Mark.O);
        if (nextStateList.isEmpty()) {
            return;
        }

        progressBar.setMaximum(nextStateList.size());

        int cores = Runtime.getRuntime().availableProcessors();
        List<List<TicTacToeGrid>> workItemLists = new ArrayList<>(cores);
        int workItemListCapacity = nextStateList.size() / cores + 1;

        for (int i = 0; i < cores; ++i) {
            workItemLists.add(new ArrayList<TicTacToeGrid>
                             (workItemListCapacity));
        }

        // Distribute the work items.
        for (int i = 0; i < nextStateList.size(); ++i) {
            workItemLists.get(i % cores).add(nextStateList.get(i));
        }

        long startTime = System.currentTimeMillis();

        WorkerThread[] threads = new WorkerThread[cores];

        for (int i = 0; i < cores; ++i) {
            threads[i] = new WorkerThread(workItemLists.get(i),
                                          moveGenerator,
                                          heuristicFunction,
                                          maximumDepth,
                                          progressBar);
        }

        for (int i = 0; i < cores; ++i) {
            threads[i].start();
        }

        for (int i = 0; i < cores; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {

            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Computation took " + (endTime - startTime) +
                           " milliseconds.");

        TicTacToeGrid bestState = threads[0].getBestState();
        double bestValue = threads[0].getBestValue();

        for (WorkerThread wt : threads) {
            if (bestValue > wt.getBestValue()) {
                bestValue = wt.getBestValue();
                bestState = wt.getBestState();
            }
        }

        grid.set(bestState);

        Mark winner = grid.getWinner();
        String message = null;

        if (winner != null) {
            message = winner.equals(Mark.X) ? "You won!" : "You lost.";
        } else if (grid.isFull()) {
            message = "It's a tie.";
        }

        if (message != null) {
            JOptionPane.showMessageDialog(canvas,
                                          message,
                                          "Game over", 
                                          JOptionPane.INFORMATION_MESSAGE);
            gameFrame.setVisible(false);
            configurationFrame.setVisible(true);
        } else {
            canvas.unlock();
        }

        gameFrame.getSize(dimension);
        dimension.height -= progressBar.getHeight();
        gameFrame.setSize(dimension);
        progressBar.setVisible(false);
        canvas.repaint();
    }

    private static final class WorkerThread extends Thread {

        private final List<TicTacToeGrid> workItemList;
        private final MoveGenerator       moveGenerator;
        private final HeuristicFunction   heuristicFunction;
        private final int                 maximumDepth;
        private final JProgressBar        progressBar;
        private TicTacToeGrid             bestState;
        private double                    bestValue;

        WorkerThread(List<TicTacToeGrid> workItemList,
                     MoveGenerator moveGenerator,
                     HeuristicFunction heuristicFunction,
                     int maximumDepth,
                     JProgressBar progressBar) {
            this.workItemList = workItemList;
            this.moveGenerator = moveGenerator;
            this.heuristicFunction = heuristicFunction;
            this.maximumDepth = maximumDepth;
            this.progressBar = progressBar;
        }

        @Override
        public void run() {
            bestValue = Double.POSITIVE_INFINITY;

            for (TicTacToeGrid s : workItemList) {
                double value = alphabeta(s, maximumDepth);

                if (bestValue > value) {
                    bestValue = value;
                    bestState = s;
                }

                progressBar.setValue(progressBar.getValue() + 1);
            }
        }

        double getBestValue() {
            return bestValue;
        }

        TicTacToeGrid getBestState() {
            return bestState;
        }

        private double alphabeta(TicTacToeGrid node, 
                                 int depth, 
                                 double alpha, 
                                 double beta, 
                                 Mark player) {
            double estimate = heuristicFunction.estimate(node);

            if (Double.isInfinite(estimate)) {
                // Once here, the game is over.

                if (estimate > 0.0) {
                    // The human player won.
                    return LARGE + depth;
                } else {
                    // The AI bot won.
                    return -LARGE - depth;
                }
            }

            if (depth == 0) {
                return estimate;
            }

            List<TicTacToeGrid> children = moveGenerator.generateMoves(node, 
                                                                       player);
            Comparator<TicTacToeGrid> comparator = 
                    new ChildComparator(heuristicFunction,children);

            Collections.<TicTacToeGrid>sort(children, comparator);

            if (player.equals(Mark.X)) {
                Collections.<TicTacToeGrid>reverse(children);

                for (TicTacToeGrid child : children) {
                    alpha = Math.max(alpha, alphabeta(child, 
                                                      depth - 1, 
                                                      alpha, 
                                                      beta, 
                                                      Mark.O));

                    if (beta <= alpha) {
                        return alpha;
                    }
                }

                return alpha;
            }

            for (TicTacToeGrid child : children) {
                beta = Math.min(beta, alphabeta(child,
                                                depth - 1,
                                                alpha,
                                                beta,
                                                Mark.X));

                if (beta <= alpha) {
                    return alpha;
                }
            }

            return beta;
        }

        private double alphabeta(TicTacToeGrid state, int depth) {
            return alphabeta(state, 
                             depth, 
                             -Double.MAX_VALUE, 
                             Double.MAX_VALUE, 
                             Mark.X);
        }

        private final class ChildComparator 
        implements Comparator<TicTacToeGrid> {

            private final Map<TicTacToeGrid, Double> heuristicMap = new HashMap<>();

            ChildComparator(HeuristicFunction heuristicFunction,
                            List<TicTacToeGrid> grids) {
                for (TicTacToeGrid grid : grids) {
                    heuristicMap.put(grid, 
                                     heuristicFunction.estimate(grid));
                }
            }

            @Override
            public int compare(TicTacToeGrid o1, TicTacToeGrid o2) {
                return Double.compare(heuristicMap.get(o1), heuristicMap.get(o2));
            }
        }
    }
}
