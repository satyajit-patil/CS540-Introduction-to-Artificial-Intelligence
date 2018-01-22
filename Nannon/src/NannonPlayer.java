/**
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */
/*
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */


import java.util.List;

public abstract class NannonPlayer {
	public NannonGameBoard gameBoard;
	
	boolean printProgress = true; // If false, turn off printing.

	private int movesCountForPlayer;

	// All players must implement the 'abstract' methods.	
	abstract public String getPlayerName();

	// The first argument is the board configuration;  see NannonGameBoard.java
	abstract public List<Integer> chooseMove(int[] boardConfiguration, List<List<Integer>> legalMoves);

	// The first argument is whether or not this player won this game.  (Assume only win or lose, i. e., that there are no draws.)
	// The corresponding elements in these three lists (all of which are the same length) report the plays during that game FOR THIS PLAYER. 
	//		(1) current board state, 
	//		(2) the number of legal moves for that board state, and 
	//		(3) the chosen move (the move = null if no legal move was possible). 
	// See PlayNannon.java for more explanation of these, especially for the meaning of the components of the first and third of these.
	abstract public void updateStatistics(boolean didIwinThisGame, List<int[]> allBoardConfigurationsThisGameForPlayer, List<Integer> allCountsOfPossibleMovesForPlayer, List<List<Integer>> allMovesThisGameForPlayer);

	abstract public void reportLearnedModel();
		
	// Setter and Getters.
	public NannonGameBoard getGameBoard()                          { return gameBoard; }
	public void            setGameBoard(NannonGameBoard gameBoard) { this.gameBoard = gameBoard; }
	
	public boolean         isPrintProgress()                       { return printProgress; }
	public void            setPrintProgress(boolean printProgress) { this.printProgress = printProgress; }
	
	// The Constructors.
	public NannonPlayer() { 
	}
	public NannonPlayer(NannonGameBoard gameBoard) { 
		this.gameBoard = gameBoard; 
	}

	public void setMoveNumber(int movesCountForPlayer) {
		this.movesCountForPlayer = movesCountForPlayer;		
	}

	public int getMoveNumber() {
		return movesCountForPlayer;		
	}
	
	// Players might want to use this to convert the number for a cell to handle HOME (H) and SAFE (S), assuming the board cells are converted to zero-based counting and the extra array location is used to represent HOME or SAFE (moves cannot be from SAFE nor be to HOME, so no need to worry about 'collisions' by over using one memory cell).
	String convertFrom(int i) {
		if (i >= NannonGameBoard.getCellsOnBoard()) { return "H"; }
		return "" + (i + 1);
	}
	
	String convertTo(int i) {
		if (i >=  NannonGameBoard.getCellsOnBoard()) { return "S"; }
		return "" + (i + 1);
	}	
	
}
