package net.coderodde.game.crosses;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JProgressBar;
import static net.coderodde.game.crosses.Application.centerFrame;

/**
 * This class implements a frame showing the game grid.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 8, 2015)
 */
public class GameFrame extends JFrame implements AIProgressListener {

    private final JProgressBar progressBar;
    private final ConfigurationFrame configurationFrame;
    private TicTacToePanel gamePanel;
    
    public GameFrame(ConfigurationFrame configurationFrame) {
        this.progressBar = new JProgressBar();
        this.configurationFrame = configurationFrame;
    }

    public void startGame(TicTacToeGrid grid, int depth) {
        getContentPane().removeAll();
        progressBar.setVisible(false);
        centerFrame(this);

        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        MoveGenerator moveGenerator = new MoveGenerator();
        HeuristicFunction heuristicFunction = new HeuristicFunction();

        gamePanel = new TicTacToePanel(progressBar,
                                                  moveGenerator,
                                                  heuristicFunction,
                                                  depth,
                                                  configurationFrame,
                                                  this);
        gamePanel.setCurrentGrid(grid);
        gamePanel.unlock();
        gamePanel.repaint();

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        getContentPane().add(gamePanel, c);

        c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        getContentPane().add(progressBar, c);

        setMinimumSize(gamePanel.getMinimumSize());

        centerFrame(this);
        pack();
    }

    @Override
    public void start(int totalProgressTokens) {
        progressBar.setMaximum(totalProgressTokens);
        progressBar.setValue(0);
        
        Dimension dimension = getSize();
        dimension.height += progressBar.getHeight();
        setSize(dimension);
        
        progressBar.setVisible(true);
    }

    @Override
    public void increment() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    @Override
    public void done() {
        Dimension dimension = getSize();
        dimension.height -= progressBar.getHeight();
        setSize(dimension);
        
        progressBar.setVisible(false);
    }
}
