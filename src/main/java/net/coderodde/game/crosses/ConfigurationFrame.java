package net.coderodde.game.crosses;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import static net.coderodde.game.crosses.Application.centerFrame;

/**
 * This class implements a configuration frame.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 8, 2015)
 */
public class ConfigurationFrame extends JFrame {

    private final JTextField heightField;
    private final JTextField widthField;
    private final JTextField patternLengthField;
    private final JTextField depthField;

    private final JLabel heightLabel;
    private final JLabel widthLabel;
    private final JLabel patternLengthLabel;
    private final JLabel depthLabel;

    private final JButton startGameButton;

    private final GameFrame gameFrame;

    public ConfigurationFrame() {
        this(5, 5, 4, 5);
    }

    public ConfigurationFrame(int height, 
                              int width, 
                              int patternLength, 
                              int depth) {
        super("Configure your game");
        this.heightField        = new JTextField("" + height);
        this.widthField         = new JTextField("" + width);
        this.patternLengthField = new JTextField("" + patternLength);
        this.depthField         = new JTextField("" + depth);
        this.heightLabel        = new JLabel("Field height");
        this.widthLabel         = new JLabel("Field width");
        this.patternLengthLabel = new JLabel("Winning pattern length");
        this.depthLabel         = new JLabel("AI depth");
        this.startGameButton    = new JButton("Start");

        this.gameFrame = new GameFrame(this);

        Border labelBorder = new EmptyBorder(0, 10, 0, 10);

        heightLabel       .setBorder(labelBorder);
        widthLabel        .setBorder(labelBorder);
        patternLengthLabel.setBorder(labelBorder);
        depthLabel.        setBorder(labelBorder);

        Border panelBorder = BorderFactory.createLineBorder(Color.RED);

        JPanel heightPanel        = new JPanel();
        JPanel widthPanel         = new JPanel();
        JPanel patternLengthPanel = new JPanel();
        JPanel depthPanel         = new JPanel();

        heightPanel        .setBorder(panelBorder);
        widthPanel         .setBorder(panelBorder);
        patternLengthPanel .setBorder(panelBorder);
        depthPanel         .setBorder(panelBorder);

        heightPanel        .setLayout(new GridLayout(1, 2));
        widthPanel         .setLayout(new GridLayout(1, 2));
        patternLengthPanel .setLayout(new GridLayout(1, 2));
        depthPanel         .setLayout(new GridLayout(1, 2));

        heightPanel.add(heightLabel);
        heightPanel.add(heightField);

        widthPanel.add(widthLabel);
        widthPanel.add(widthField);

        patternLengthPanel.add(patternLengthLabel);
        patternLengthPanel.add(patternLengthField);

        depthPanel.add(depthLabel);
        depthPanel.add(depthField);

        getContentPane().setLayout(new GridLayout(5, 1, 20, 10));

        getContentPane().add(heightPanel);
        getContentPane().add(widthPanel);
        getContentPane().add(patternLengthPanel);
        getContentPane().add(depthPanel);
        getContentPane().add(startGameButton);

        StartButtonActionListener startButtonActionListener = 
                new StartButtonActionListener(heightField,
                                              widthField,
                                              patternLengthField,
                                              depthField);

        startGameButton.addActionListener(startButtonActionListener);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        centerFrame(this);
        setResizable(false);
        startGameButton.requestFocus();
        setVisible(true);
    }

    public void setHeight(int height) {
        this.heightField.setText("" + height);
    }

    public void setWidth(int width) {
        this.widthField.setText("" + width);
    }

    public void setPatternLength(int patternLength) {
        this.patternLengthField.setText("" + patternLength);
    }

    public void setDepth(int depth) {
        this.depthField.setText("" + depth);
    }

    private class StartButtonActionListener implements ActionListener {

        private final JTextField heightField;
        private final JTextField widthField;
        private final JTextField patternLengthField;
        private final JTextField depthField;

        private TicTacToeGrid resultGrid;

        StartButtonActionListener(JTextField heightField,
                                  JTextField widthField,
                                  JTextField patternLengthField,
                                  JTextField depthField) {
            this.heightField        = heightField;
            this.widthField         = widthField;
            this.patternLengthField = patternLengthField;
            this.depthField         = depthField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            resultGrid = null;

            String stmp = heightField.getText().trim();

            if (stmp.isEmpty()) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this,
                        "Please input the field height.",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int height;

            try {
                height = Integer.parseInt(stmp);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this, 
                        "Bad height: " + stmp, 
                        "Input error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            stmp = widthField.getText().trim();

            if (stmp.isEmpty()) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this,
                        "Please input the field width.",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int width;

            try {
                width = Integer.parseInt(stmp);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this, 
                        "Bad width: " + stmp, 
                        "Input error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            stmp = patternLengthField.getText().trim();

            if (stmp.isEmpty()) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this,
                        "Please input the winning pattern length.",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int patternLength;

            try {
                patternLength = Integer.parseInt(stmp);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this, 
                        "Bad pattern length: " + stmp, 
                        "Input error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            stmp = depthField.getText().trim();

            if (stmp.isEmpty()) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this,
                        "Please input the AI depth.",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int depth;

            try {
                depth = Integer.parseInt(stmp);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this, 
                        "Bad depth: " + stmp, 
                        "Input error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
              resultGrid = new TicTacToeGrid(height, width, patternLength); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        ConfigurationFrame.this,
                        "Bad configuration: " + ex.getMessage(),
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ConfigurationFrame.this.setVisible(false);
            gameFrame.startGame(resultGrid, depth);
            gameFrame.setVisible(true);
        }
    }
}
