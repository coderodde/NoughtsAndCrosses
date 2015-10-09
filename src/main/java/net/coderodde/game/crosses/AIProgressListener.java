package net.coderodde.game.crosses;

/**
 * This interface defines the API for listening for AI progress.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 9, 2015)
 */
public interface AIProgressListener {
    
    public void start(int totalProgressTokens);
    
    public void increment();
    
    public void done();
}
