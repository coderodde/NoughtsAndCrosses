package net.coderodde.game.crosses;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * This thread is responsible for running the AI.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 7, 2015)
 */
class AIWorker extends SwingWorker<TicTacToeGrid, Void> {

    private static final double LARGE = 1e10;

    private final ConfigurationFrame configurationFrame;
    private final GameFrame gameFrame;
    private final JProgressBar progressBar;
    private final TicTacToeGrid grid;
    private final TicTacToePanel canvas;
    private final MoveGenerator moveGenerator;
    private final HeuristicFunction heuristicFunction;
    private final int maximumDepth;

    AIWorker(ConfigurationFrame configurationFrame,
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
    protected TicTacToeGrid doInBackground() throws Exception {
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
            return null;
        }

        progressBar.setMaximum(nextStateList.size());

        List<WorkerCallable> callableList = 
                new ArrayList<>(nextStateList.size());
        
        for (TicTacToeGrid grid : nextStateList) {
            callableList.add(new WorkerCallable(grid,
                                                moveGenerator,
                                                heuristicFunction,
                                                maximumDepth,
                                                progressBar));
        }
        
        TicTacToeGrid bestState = null;
        
        try {
            long startTime = System.currentTimeMillis();
            List<Future<WorkerCallableResult>> resultList = 
                    Executors.newCachedThreadPool().invokeAll(callableList);
            long endTime = System.currentTimeMillis();
            System.out.println("Computation took " + (endTime - startTime) +
                               " milliseconds.");

            bestState        = resultList.get(0).get().bestState;
            double bestValue = resultList.get(0).get().bestValue;

            for (Future<WorkerCallableResult> result : resultList) {
                double currentValue = result.get().bestValue;
                TicTacToeGrid currentState = result.get().bestState;

                if (bestValue > currentValue) {
                    bestValue = currentValue;
                    bestState = currentState;
                }
            }
        }
        catch (InterruptedException | ExecutionException ex) {
            return null;
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

        return null;
    }
    
    @Override
    protected void done() {
        Dimension dimension = new Dimension();
        gameFrame.getSize(dimension);
        dimension.height -= progressBar.getHeight();
        gameFrame.setSize(dimension);
        progressBar.setVisible(false);
        canvas.repaint();
        canvas.unlock();
    }
    
    private static class WorkerCallableResult {
        TicTacToeGrid bestState;
        double bestValue;
    }

    private static final class WorkerCallable 
    implements Callable<WorkerCallableResult> {

        private final TicTacToeGrid       state;
        private final MoveGenerator       moveGenerator;
        private final HeuristicFunction   heuristicFunction;
        private final int                 maximumDepth;
        private final JProgressBar        progressBar;

        WorkerCallable(TicTacToeGrid state,
                       MoveGenerator moveGenerator,
                       HeuristicFunction heuristicFunction,
                       int maximumDepth,
                       JProgressBar progressBar) {
            this.state = state;
            this.moveGenerator = moveGenerator;
            this.heuristicFunction = heuristicFunction;
            this.maximumDepth = maximumDepth;
            this.progressBar = progressBar;
        }

        @Override
        public WorkerCallableResult call() {
            WorkerCallableResult result = new WorkerCallableResult();
            result.bestValue = alphabeta(state, maximumDepth);
            result.bestState = state;
            progressBar.setValue(progressBar.getValue() + 1);
            return result;
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

            private final Map<TicTacToeGrid, Double> heuristicMap 
                    = new HashMap<>();

            ChildComparator(HeuristicFunction heuristicFunction,
                            List<TicTacToeGrid> grids) {
                for (TicTacToeGrid grid : grids) {
                    heuristicMap.put(grid, 
                                     heuristicFunction.estimate(grid));
                }
            }

            @Override
            public int compare(TicTacToeGrid o1, TicTacToeGrid o2) {
                return Double.compare(heuristicMap.get(o1), 
                                      heuristicMap.get(o2));
            }
        }
    }
}
