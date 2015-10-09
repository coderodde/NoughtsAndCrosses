package net.coderodde.game.crosses;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * This class implements the panel for playing Tic Tac Toe.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 5, 2015)
 */
public class TicTacToePanel extends JPanel {

    private static final int  DEFAULT_PADDING      = 10;
    private static final int  DEFAULT_BORDER_WIDTH = 3;
    private static final int  MINIMUM_CELL_LENGTH  = 60;
    private static final Font DEFAULT_FONT = new Font("Times New Roman", 
                                                      Font.BOLD, 
                                                      50);

    private final MoveGenerator      moveGenerator;
    private final HeuristicFunction  heuristicFunction;
    private final int                maximumDepth;
    private final ConfigurationFrame configurationFrame;
    private final GameFrame          gameFrame;

    private TicTacToeGrid currentGrid;
    private int           padding;
    private int           borderWidth;
    private Color         backgroundColor;
    private Color         borderColor;
    private Color         foregroundColor; 
    private Color         highlightOccupiedBorderColor;
    private Color         highlightOccupiedCellColor;
    private Color         highlightFreeBorderColor;
    private Color         highlightFreeCellColor;

    private int highlightCellX;
    private int highlightCellY;

    private int previousHighlightCellX;
    private int previousHighlightCellY;

    private int lastValidCellX = -1;
    private int lastValidCellY = -1;

    /**
     * Specifies whether the AI is currently computing its next move. If the
     * lock is on, the user's input will be ignored. The default value is 
     * {@code false}.
     */
    private volatile boolean lock;

    public TicTacToePanel(JProgressBar progressBar,
                          MoveGenerator moveGenerator,
                          HeuristicFunction heuristicFunction,
                          int maximumDepth,
                          ConfigurationFrame configurationFrame,
                          GameFrame gameFrame) {
        this.moveGenerator = moveGenerator;
        this.heuristicFunction = heuristicFunction;
        this.maximumDepth = maximumDepth;
        this.configurationFrame = configurationFrame;
        this.gameFrame = gameFrame;

        this.padding = DEFAULT_PADDING;
        this.borderWidth = DEFAULT_BORDER_WIDTH;
        this.backgroundColor = Color.WHITE;
        this.borderColor = Color.GRAY;
        this.foregroundColor = Color.BLACK;
        this.highlightFreeBorderColor = new Color(100, 200, 100);
        this.highlightFreeCellColor = Color.GREEN;
        this.highlightOccupiedBorderColor = Color.PINK;
        this.highlightOccupiedCellColor = Color.RED;

        setFont(DEFAULT_FONT);

        this.setMinimumSize(new Dimension(100, 100));
        CanvasMouseListener mouseListener = new CanvasMouseListener();
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        CanvasKeyListener keyListener = new CanvasKeyListener();
        this.addKeyListener(keyListener);
        this.setFocusable(true);
        this.requestFocus();
    }

    public void lock() {
        lock = true;
    }

    public void unlock() {
        lock = false;
    }

    public void setPadding(int padding) {
        this.padding = Math.max(1, padding);
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = Math.max(1, borderWidth);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    public void setForegroundColor(Color color) {
        this.foregroundColor = color;
    }

    public void setHighlightOccupiedBorderColor(Color color) {
        this.highlightOccupiedBorderColor = color;
    }

    public void setHighlightOccupiedCellColor(Color color) {
        this.highlightOccupiedCellColor = color;
    }

    public void setHighlightFreeBorderColor(Color color) {
        this.highlightFreeBorderColor = color;
    }

    public void setHighlightFreeCellColor(Color color) {
        this.highlightFreeCellColor = color;
    }

    public void setCurrentGrid(TicTacToeGrid grid) {
        if (this.currentGrid == null) {
            this.highlightCellX = grid.getWidth()  / 2;
            this.highlightCellY = grid.getHeight() / 2;

            this.lastValidCellX = this.highlightCellX;
            this.lastValidCellY = this.highlightCellY;

            this.previousHighlightCellX = this.highlightCellX;
            this.previousHighlightCellY = this.highlightCellY;
        }

        this.currentGrid = grid;
        repaint();
    }

    public TicTacToeGrid getCurrentGrid() {
        return currentGrid;
    }

    @Override
    public Dimension getMinimumSize() {
        int horizontalCells = currentGrid.getWidth();
        int verticalCells   = currentGrid.getHeight();

        return new Dimension(2 * padding + horizontalCells * 
                                (borderWidth + MINIMUM_CELL_LENGTH) + 
                                 borderWidth,
                            (2 * padding + verticalCells * 
                                (borderWidth + MINIMUM_CELL_LENGTH) + 
                                 borderWidth));
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public void update(Graphics g) {
        int availableWidth  = getWidth() - 2 * padding;
        int availableHeight = getHeight() - 2 * padding;

        int horizontalCells = currentGrid.getWidth();
        int verticalCells   = currentGrid.getHeight();

        int cellWidth  = (availableWidth - (horizontalCells + 1) * borderWidth) 
                         / horizontalCells;

        int cellHeight = (availableHeight - (verticalCells + 1) * borderWidth) 
                         / verticalCells;

        int cellLength = Math.min(cellWidth, cellHeight);

        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        int occupiedWidth = (horizontalCells * (cellLength + borderWidth)) + 
                             borderWidth;
        int occupiedHeight = (verticalCells * (cellLength + borderWidth)) +
                             borderWidth;

        int skipX = (getWidth()  - occupiedWidth) / 2;
        int skipY = (getHeight() - occupiedHeight) / 2;

        g.setColor(borderColor);

        // Draw horizontal borders.
        for (int y = 0; y <= verticalCells; ++y) {
            g.fillRect(skipX, 
                       skipY + y * (borderWidth + cellLength), 
                       horizontalCells * (borderWidth + cellLength) + borderWidth, 
                       borderWidth);
        }

        // Draw vertical borders.
        for (int x = 0; x <= horizontalCells; ++x) {
            g.fillRect(skipX + x * (borderWidth + cellLength),
                       skipY,
                       borderWidth,
                       verticalCells * (borderWidth + cellLength) + borderWidth);
        }

        if (highlightCellX >= 0
                && highlightCellX < horizontalCells
                && highlightCellY >= 0
                && highlightCellY < verticalCells) {
            boolean cellOccupied = currentGrid.read(highlightCellX,
                                                    highlightCellY) != null;
            if (cellOccupied) {
                g.setColor(this.highlightOccupiedBorderColor);
            } else {
                g.setColor(this.highlightFreeBorderColor);
            }

            // Draw the border.
            g.fillRect(skipX + highlightCellX * (borderWidth + cellLength),
                       skipY + highlightCellY * (borderWidth + cellLength), 
                       2 * borderWidth + cellLength,
                       2 * borderWidth + cellLength);

            if (cellOccupied) {
                g.setColor(this.highlightOccupiedCellColor);
            } else {
                g.setColor(this.highlightFreeCellColor);
            }

            // Draw the cell.
            g.fillRect(skipX + highlightCellX * (borderWidth + cellLength) + borderWidth,
                       skipY + highlightCellY * (borderWidth + cellLength) + borderWidth, 
                       cellLength ,
                       cellLength);

        }

        g.setColor(foregroundColor);
        g.setFont(getFont());

        if (currentGrid == null) {
            return;
        }

        int verticalSkip = 16;

        Font font = prepareFont(cellLength, verticalSkip, g);

        g.setFont(font);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g.getFontMetrics(font);

        int textHeight = fm.getAscent();
        int textWidth = fm.stringWidth("X");

        int dx = (cellLength - textWidth)  / 2;
        int dy = (cellLength - textHeight) / 2;

        // Draw the marks.
        for (int y = 0; y < currentGrid.getHeight(); ++y) {
            for (int x = 0; x < currentGrid.getWidth(); ++x) { 
                Mark mark = currentGrid.read(x, y);

                if (mark == null) {
                    continue;
                }

                g.drawString(mark.equals(Mark.X) ? "X" : "O", 
                             skipX + dx + borderWidth * (1 + x) + x * cellLength,
                             skipY - dy - 8 + borderWidth * (1 + y) + (1 + y) * cellLength);
            }
        }
    }

    private Font prepareFont(int cellLength, int verticalSkip, Graphics g) {
        Font currentFont = getFont();

        for (int fontSize = 1; ; ++fontSize) {
            Font f = new Font(currentFont.getFontName(), Font.BOLD, fontSize);
            FontMetrics fm = g.getFontMetrics(f);

            int height = fm.getAscent();

            if (height >= cellLength - verticalSkip) {
                return new Font(currentFont.getFontName(), 
                                Font.BOLD, 
                                fontSize - 1);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }

    private Point toCellCoordinates(int x, int y) {
        Point ret = new Point();

        int availableWidth  = getWidth() - 2 * padding;
        int availableHeight = getHeight() - 2 * padding;

        int horizontalCells = currentGrid.getWidth();
        int verticalCells   = currentGrid.getHeight();

        int cellWidth  = (availableWidth - (horizontalCells + 1) * borderWidth) 
                         / horizontalCells;

        int cellHeight = (availableHeight - (verticalCells + 1) * borderWidth) 
                         / verticalCells;

        int cellLength = Math.min(cellWidth, cellHeight);

        int occupiedWidth = (horizontalCells * (cellLength + borderWidth)) + 
                             borderWidth;
        int occupiedHeight = (verticalCells * (cellLength + borderWidth)) +
                             borderWidth;

        x -= (getWidth()  - occupiedWidth)  / 2;
        y -= (getHeight() - occupiedHeight) / 2;

        if (x % (cellLength + borderWidth) < borderWidth
                || y % (cellLength + borderWidth) < borderWidth) {
            ret.x = -1;
            return ret;
        }

        ret.x = x / (cellLength + borderWidth);
        ret.y = y / (cellLength + borderWidth);

        return ret;
    }

    private void tryClick(int x, int y) {
        Point p = toCellCoordinates(x, y);

        if (p.x >= 0 && p.x < currentGrid.getWidth() 
                && p.y >= 0 && p.y < currentGrid.getHeight()) {
            try {
                lock();
                currentGrid.mark(p.x, p.y, Mark.X);
                
                repaint();

                Mark winner = currentGrid.getWinner();
                String message = null;

                if (winner != null) {
                    message = winner.equals(Mark.X) ? 
                            "You won!" : 
                            "You lost.";

                } else if (currentGrid.isFull()) {
                    message = "It's a tie.";
                }

                if (message != null) {
                    JOptionPane.showMessageDialog(
                            gameFrame, 
                            message,
                            "Game over",
                            JOptionPane.INFORMATION_MESSAGE);

                    gameFrame.setVisible(false);
                    configurationFrame.setVisible(true);
                    return;
                }
                
                AIWorker ai = new AIWorker(configurationFrame,
                                           gameFrame,
                                           gameFrame,
                                           currentGrid,
                                           this,
                                           moveGenerator,
                                           heuristicFunction,
                                           maximumDepth);

                ai.execute();
            } catch (Exception ex) {
                
            }
        }
    }

    private void tryHighlight(int x, int y) {
        Point p = toCellCoordinates(x, y);

        // Check that the mouse is on top of a border.
        if (p.x < 0) {
            if (previousHighlightCellX != p.x) {
                highlightCellX = p.x;
                previousHighlightCellX = p.x;
                repaint();
            }

            return;
        }

        if (p.x >= 0 && p.x < currentGrid.getWidth()) {
            this.lastValidCellX = p.x;
            this.lastValidCellY = p.y;
        }

        this.highlightCellX = p.x;
        this.highlightCellY = p.y;

        if (highlightCellX != previousHighlightCellX 
                || highlightCellY != previousHighlightCellY) {
            previousHighlightCellX = highlightCellX;
            previousHighlightCellY = highlightCellY;
            repaint();
        }
    }

    private class CanvasKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            process(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            process(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }

        private void process(KeyEvent e) {
            int cellX = lastValidCellX;
            int cellY = lastValidCellY;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:

                    if (cellY > 0) {
                        previousHighlightCellY = lastValidCellY;
                        highlightCellY = --lastValidCellY;
                        repaint();
                    }

                    break;

                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:

                    if (cellX < currentGrid.getWidth() - 1) {
                        previousHighlightCellX = lastValidCellX;
                        highlightCellX = ++lastValidCellX;
                        repaint();
                    }

                    break;

                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:

                    if (cellY < currentGrid.getHeight() - 1) {
                        previousHighlightCellY = lastValidCellY;
                        highlightCellY = ++lastValidCellY;
                        repaint();
                    }

                    break;

                case KeyEvent.VK_LEFT: 
                case KeyEvent.VK_A:

                    if (cellX > 0) {
                        previousHighlightCellX = lastValidCellX;
                        highlightCellX = --lastValidCellX;
                        repaint();
                    }

                    break;

                case KeyEvent.VK_SPACE:
                    if (!lock) {
                        // Try mark.
                        try {
                            lock();
                            currentGrid.mark(lastValidCellX,
                                             lastValidCellY,
                                             Mark.X);

                            Mark winner = currentGrid.getWinner();

                            String message = null;

                            if (winner != null) {
                                message = winner.equals(Mark.X) ? 
                                        "You won!" : 
                                        "You lost.";

                            } else if (currentGrid.isFull()) {
                                message = "It's a tie.";
                            }

                            if (message != null) {
                                JOptionPane.showMessageDialog(
                                        gameFrame, 
                                        message,
                                        "Game over",
                                        JOptionPane.INFORMATION_MESSAGE);

                                gameFrame.setVisible(false);
                                configurationFrame.setVisible(true);
                                repaint();
                                return;
                            }
                            
                            repaint();
                            AIWorker ai = new AIWorker(configurationFrame,
                                                       gameFrame,
                                                       gameFrame,
                                                       currentGrid,
                                                       TicTacToePanel.this,
                                                       moveGenerator,
                                                       heuristicFunction,
                                                       maximumDepth);
                            ai.execute();
                        } catch (Exception ex) {
                            unlock();
                        }
                    }

                    break;
            }
        }
    }

    private class CanvasMouseListener implements MouseListener,
                                                 MouseMotionListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!lock) {
                tryClick(e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            tryHighlight(e.getX(), e.getY());
        }
    }
}
