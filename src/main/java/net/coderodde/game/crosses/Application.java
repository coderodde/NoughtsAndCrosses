package net.coderodde.game.crosses;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This class implements the actual Tic Tac Toe game.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 */
public class Application {

    /**
     * The entry point into the program.

     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new ConfigurationFrame();
            }
        });
    }
    
    public static void centerFrame(JFrame frame) {
        Dimension screenResolution = Toolkit.getDefaultToolkit()
                                            .getScreenSize();
        
        frame.setLocation((screenResolution.width - frame.getWidth())   / 2,
                          (screenResolution.height - frame.getHeight()) / 2);
    } 
}
