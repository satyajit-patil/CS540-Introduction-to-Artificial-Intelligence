/*
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 * 
 * Nannon is copyrighted (2004) by Jordan Pollack.  He has given permission for use by Jude Shavlik in his U-Wisconsin courses.
 */

import java.util.ArrayList;
import java.util.List;

public class NannonGameBoard {
	
	// Two key methods for writing players are
	//
	//   getBoardConfiguration()     // This returns 1D array that reports the CURRENT state of the board (see the method body for a description of the fields in this array).
	//
	//   getNextBoardConfiguration(int[] currentBoardConfig, List<Integer> move)
	//                               // This takes 1D array containing the current world state and returns the world state produced by applying this move.
	//                               // NOTE: This code does not check if this is a legal move, so you should only use moves produced by getLegalMoves().
	//
	// Moves are list with three integer values (assuming the standard board that is 6 'cells' wide):
	//    the first is the FROM (either 1 to cellsOnBoard or the constant 'movingFromHOME')                      - move.get(0) will access this
	//    the second is the TO  (either 1 to cellsOnBoard or the constant 'movingToSAFE')                        - move.get(1) will access this
	//    the third indicates the EFFECT of the move, see the constants in ManageMoveEffects.java  - move.get(2) will access this

	

	// The constructor.
	public NannonGameBoard() {
		board = new int[cellsOnBoard];
	}
		
	private int gamesPlayed  = 0; // A counter of games played on this instance of Nannon.
	public int getGamesPlayed() { return gamesPlayed; }
	
	// TODO - all public variables (that are not 'final') should be made private and accessors used (so during the class tourney we can prevent changes to these vars.)
	private static int piecesPerPlayer = 3; // Ideally your code should handle this being 4 or 5.
	public  static void setPiecesPerPlayer(int pieces) { piecesPerPlayer = pieces; }
	public  static int  getPiecesPerPlayer()           { return piecesPerPlayer;	  }

	private static int cellsOnBoard = 2 * piecesPerPlayer; // And this being between 6 and 12.
	public  static void setCellsOnBoard(int cells) { cellsOnBoard = cells; }
	public  static int  getCellsOnBoard()          { return cellsOnBoard;  }

	public final static int sidesOnDice = 6;
	
	public boolean printProgress = true; // If false, the code will run much faster.
	public void    setPrintProgress(boolean value) { printProgress = value; }
	
	public boolean showNextStatesAsWell = true; // If reporting moves, show the NEXT state that results from each legal move.
	
	public final static int movingFromHOME = -999; // Indicates that a piece is moving FROM home.
	public final static int movingToSAFETY =  999; // Indicates that a piece is more TO safety.
	
	// These three int's can be set to anything BUT ALL THREE MUST HAVE DIFFERENT VALUES!
	public final static int empty   = 0; // Used to indicate a cell is EMPTY (and also used to say no one has yet one the game and it is neither player's turn).
	public final static int playerX = 1; // This player starts on the LEFT and moves RIGHT.  
	public final static int playerO = 2; // This player does the opposite.
	
	// You should use the accessors below or getBoardConfiguration() to access these - see the body of that method to see how to 'packages' these into a 1D array.
	private int whoseTurn          = empty; // One of {empty, playerX, playerO}.  Shouldn't be 'empty' except before initializing.
	private int homePieces_playerX = piecesPerPlayer; // These are counts for pieces ('pips') in the HOME position.
	private int homePieces_playerO = piecesPerPlayer;
	private int safePieces_playerX = 0; // These are counts for pieces ('pips') that have reached SAFETY (ie, a player win once all his/her/its pieces are SAFE).
	private int safePieces_playerO = 0;
	private int die_playerX        = 3; // Value on this player's die (i. e., the singular form of 'dice').
	private int die_playerO        = 3;

	// Here are accessors for pulling fields of out the 1D array that summarizes a board configuration.  NOTICE YOU DO NOT NEED TO KNOW IF YOU ARE X OR O, SINCE THIS IS IN boardConfiguration[0].
	public static int getWhoseTurn(        int[] boardConfiguration) { return                    boardConfiguration[0]; }  // These are static since they use no stored information.
	public static int getHomePieces_me(    int[] boardConfiguration) { return boardConfiguration[boardConfiguration[0] == playerX ? 1 : 2]; }
	public static int getHomePieces_them(  int[] boardConfiguration) { return boardConfiguration[boardConfiguration[0] == playerX ? 2 : 1]; }
	public static int getSafePieces_me(    int[] boardConfiguration) { return boardConfiguration[boardConfiguration[0] == playerX ? 3 : 4]; }
	public static int getSafePieces_them(  int[] boardConfiguration) { return boardConfiguration[boardConfiguration[0] == playerX ? 4 : 3]; }
	public static int getDie_player_me(    int[] boardConfiguration) { return boardConfiguration[boardConfiguration[0] == playerX ? 5 : 6]; }
	public static int getDie_player_them(  int[] boardConfiguration) { return boardConfiguration[boardConfiguration[0] == playerX ? 6 : 5]; }
	public static boolean amIatThisLocationCountingFromOne(  int[] boardConfiguration, int locationNumberCountingFromOne) { return boardConfiguration[6 + locationNumberCountingFromOne] == boardConfiguration[0]; } 
	public static boolean isThisLocationCountingFromOneEmpty(int[] boardConfiguration, int locationNumberCountingFromOne) { return boardConfiguration[6 + locationNumberCountingFromOne] == empty; } 
	public static boolean isOppAtThisLocationCountingFromOne(int[] boardConfiguration, int locationNumberCountingFromOne) { return boardConfiguration[6 + locationNumberCountingFromOne] != empty
		                                                                                                                        && boardConfiguration[6 + locationNumberCountingFromOne] != boardConfiguration[0]; } 
	// Accessor functions.  But you probably will instead want to get these packaged in a 1D array by calling getBoardConfiguration().
	public int getWhoseTurn()          { return whoseTurn;	}  // One of {empty, playerX, playerO}.  Shouldn't be 'empty' except before initializing.
	public int getHomePieces_playerX() { return homePieces_playerX;	}
	public int getHomePieces_playerO() { return homePieces_playerO;	}
	public int getSafePieces_playerX() { return safePieces_playerX;	}
	public int getSafePieces_playerO() { return safePieces_playerO;	}
	public int getDie_playerX()        { return die_playerX;	}
	public int getDie_playerO()        { return die_playerO;	}
	public int getWhatAtThisBoardLocation(int indexFromOne) { return board[indexFromOne - 1]; } // NOTE: it is to the caller the make sure this is a board location. 
	
	// This method returns a FRESH array (since it is saved for use until AFTER a game completes).
	public int[] getBoardConfiguration() {
		int[] boardConfiguration = new int[7 + cellsOnBoard];
		
		// Note: could also use the accessors above for these variables directly.  But putting them all in one place might be helpful.
		boardConfiguration[0] = whoseTurn;  // One of {empty, playerX, playerO}.  Shouldn't be 'empty' except before initializing.
		boardConfiguration[1] = homePieces_playerX;
		boardConfiguration[2] = homePieces_playerO;
		boardConfiguration[3] = safePieces_playerX;
		boardConfiguration[4] = safePieces_playerO;
		boardConfiguration[5] = die_playerX; // These probably aren't needed since the legal moves are produced, but provide them in case someone wants to use them somehow,
		boardConfiguration[6] = die_playerO;
		
		for (int i = 0; i < cellsOnBoard; i++) { // Usually there are 6 cells, but this code should handle larger board widths (cellsOnBoard <= 4 will lead to bugs).
			boardConfiguration[7 + i] = board[i];  // Remember:  One of {empty, playerX, playerO}.
		}
		
		return boardConfiguration;
	}
	
	public void reportBoardAsVector(int[] boardConfiguration, boolean reportTurnAndShake) {
		if (reportTurnAndShake) {
			Utils.println("  ");
			if (boardConfiguration[0] == playerX) { Utils.print(" turn=X, die=" + boardConfiguration[5]); } else { Utils.print("turn=O, die=" + boardConfiguration[6]); } 
			Utils.println("  ");
		}
		Utils.print(" Safe(O)=" + boardConfiguration[4]);  
		Utils.print(" Home(X)=" + boardConfiguration[1] + "  "); 
		for (int i = 0; i < cellsOnBoard; i++) {
			if      (boardConfiguration[7 + i] == empty)   { Utils.print("_"); }
			else if (boardConfiguration[7 + i] == playerX) { Utils.print("X"); }
			else if (boardConfiguration[7 + i] == playerO) { Utils.print("O"); }
		}
		Utils.print( "  Home(O)=" + boardConfiguration[2]); 
		Utils.print(  " Safe(X)=" + boardConfiguration[3] + " "); 
	}
	
	// This methods makes a copy of the current board, then applies the given move.  BUT DOES NOT CHANGE WHOSE TURN IT IS (so the meaning of getHomePieces_me doesn't change).
	// getNextBoardConfiguration(...) DOES NOT CHECK IF THE MOVE IS LEGAL (that should have been done elsewhere, e.g., getLegalMoves()).
	private int[] getNextBoardConfiguration(List<Integer> move) {
		int[] nextBoardConfig = getBoardConfiguration(); // Gets A FRESH COPY of the current board.
		
		return getNextBoardConfiguration(nextBoardConfig, move, false);
	}

	public int[] getNextBoardConfiguration(int[] currentBoardConfig, List<Integer> move) {
		return getNextBoardConfiguration(currentBoardConfig, move, true); // Play it safe and create a fresh copy before manipulating it.
	}
	
	public int[] getNextBoardConfiguration(int[] currentBoardConfig, List<Integer> move, boolean needToCopyBoard) {
		
		int whoseTurnToPlay   = currentBoardConfig[0]; // Need to get this from the current board, since this might be a REPLAY of a game (i.e., during training).		
		int[] nextBoardConfig = (needToCopyBoard ? currentBoardConfig.clone() : currentBoardConfig); 
		
		int fromCountingFromOne = move.get(0);
		int toCountingFromOne   = move.get(1);
		boolean opponentHit     = ManageMoveEffects.isaHit(move.get(2));

		// Convert to zero-based numbering and also handle NannonGameBoard.movingFromHOME and NannonGameBoard.movingToSAFE.
		int from = (fromCountingFromOne == NannonGameBoard.movingFromHOME ? NannonGameBoard.movingFromHOME : fromCountingFromOne - 1);
		int to   = (toCountingFromOne   == NannonGameBoard.movingToSAFETY ? NannonGameBoard.movingToSAFETY : toCountingFromOne   - 1);
		
		if (from == NannonGameBoard.movingFromHOME) {			
			if (whoseTurnToPlay == playerX) { nextBoardConfig[1]--; } else { nextBoardConfig[2]--; }
		} else {
			nextBoardConfig[7 + from] = empty;
		}
		
		if (to  == NannonGameBoard.movingToSAFETY) {
			if (whoseTurnToPlay == playerX) { nextBoardConfig[3]++; } else { nextBoardConfig[4]++; }
		} else {
			nextBoardConfig[7 + to] = whoseTurnToPlay;
		}
		
		if (opponentHit) { // Send the opposing player that was hit back to its home.
			if (whoseTurnToPlay == playerX) { nextBoardConfig[2]++; } else { nextBoardConfig[1]++; }
		}
		for (int i = 1; i <= 4; i++) {
			if (nextBoardConfig[i] < 0 || nextBoardConfig[i] > NannonGameBoard.piecesPerPlayer) { reportBoardAsVector(currentBoardConfig, true); reportBoardAsVector(nextBoardConfig, true); Utils.error("Bad world state ... turn = " + (whoseTurnToPlay == playerX ? "X" : "O") + ", from = " + from + ", to = " + to + ", nextBoardConfig[" + i + "] = " + nextBoardConfig[i]); }
		}
		return nextBoardConfig;
	}
	
	
	// You probably will want to simply use Nannon.playGames() as it is (that method is what we will use during grading and the class-wide tournament).
	// But in case you want to call some of these methods, here is the rest of the API, the Applications Programmer Interface).
	//
	//    void                initializeGame()    // Setup for a new game.
	//    void                drawBoardInASCII()  // A simple "visualizer" for the game board.
	//    List<List<Integer>> getLegalMoves()
	//    void                makeMove(int fromCountingFromOne, int toCountingFromOne)
	//    void                passTurn()
	//    void                changeTurns()
	//    boolean             gameDone()
	//    boolean             didPlayerXwin()
	//    boolean             didPlayerOwin()
	//    String              getWinnersName()    // In case you want to print out which player won.
	//    
	// but you need not read the code for them.
	
	///////////////////////////////////////////////// CS 540 students should not need to read/understand the rest of the code in this file (unless they are trying to help track down a bug). 

	private int[] board; // What is filling each cell (i. e., location) on the board. One of {empty, playerX, playerO}.

	// Get the legal moves for the player whose turn it is.  Depends on the current value of that player's die.
	// Each legal move has THREE ELEMENTS, all of which are integers: the FROM cell, the TO cell, and whether or not the TO cell contains an opponent.
	// Check the comments above for the documentation for the special 'cell' values, movingFromHOME and movingFromSAFE (ie, moving from or to off the board).
	public List<List<Integer>> getLegalMoves() {
		if (whoseTurn != playerX && whoseTurn != playerO) { Utils.error("Unexpected 'whoseTurn' value: " + whoseTurn); }
		return getLegalMoves(whoseTurn, whoseTurn == playerX ? die_playerX : die_playerO);
	}	

	// Have the player whose turn it is make this move.  If illegal, an error will result.
	// Note that counting is from one (internally the code counts from zero and a conversion is made below).
	// Check the comments above for the documentation for the special 'cell' values,  movingFromHOME and movingFromSAFE (ie, moving from or to OFF THE BOARD).
	public void makeMove(int fromCountingFromOne, int toCountingFromOne) {
		makeMove(whoseTurn, fromCountingFromOne, toCountingFromOne);
	}
	// If no turns to make, pass.  THE GAME'S RULES SAY ONE HAS TO MOVE IF ONE CAN, BUT THAT IS NOT ENFORCED IN THIS CODE.   TODO
	public void passTurn() {	
	}
	
	// Switch which player is to next move.
	public void changeTurns() {		
		if (!gameDone()) { // See if game is not yet done.
			if (whoseTurn == playerX) { whoseTurn = playerO; } else { whoseTurn = playerX; } // The other player's turn.
			rollDie();    // Have that player roll.
		}
	}
	private List<List<Integer>> getLegalMoves(int playerID, int dieShake) { // Pass both these in in case we want to IMAGINE moves.
		
		if (playerID != playerX && playerID != playerO) { Utils.error("Player IDs must be " + playerX + " or " + playerO + "; you provided " + playerID + "."); }
		if (dieShake < 1 || dieShake > sidesOnDice) { Utils.error("Die rolls must be 1-" + sidesOnDice + "; you provided "    + dieShake + "."); }
		
		List<List<Integer>> legalMoves = new ArrayList<List<Integer>>(0);
		
		if (playerID == playerX) { // Get legal moves for Player playerX, who is on the LEFT and moves toward the right.
			
			// See if there are any from-home moves and if so see if the next cell is available (ie, empty or containing a Player O pip).
			if (homePieces_playerX > 0) {
				int nextCell = dieShake - 1; // Counting is from zero.
				
				if (board[nextCell] != playerID && !isProtectedCell(nextCell)) {
					legalMoves.add(getMove(movingFromHOME, nextCell + 1, computeEffectOfMove(movingFromHOME, nextCell)));
				}
			}
			
			//  Now see if any current pips can be moved.
			for (int cell = 0; cell < cellsOnBoard; cell++) if (board[cell] == playerID) {
				int nextCell = cell + dieShake; // Counting is from zero.
				
				if (nextCell >= cellsOnBoard) { // Can go to HOME.
					legalMoves.add(getMove(cell + 1, movingToSAFETY, computeEffectOfMove(cell, nextCell)));
				} else if (board[nextCell] != playerID && !isProtectedCell(nextCell)) {
					legalMoves.add(getMove(cell + 1, nextCell + 1,   computeEffectOfMove(cell, nextCell)));
				}
				
			}
		
		}
		if (playerID == playerO) { // Get legal moves for Player playerO, who is on the RIGHT and moves toward the left.
			
			// See if there are any from-home moves and if so see if the next cell is available (ie, empty or containing a Player X pip).
			if (homePieces_playerO > 0) {
				int nextCell = cellsOnBoard - dieShake; // Counting is from zero.
				
				if (board[nextCell] != playerID && !isProtectedCell(nextCell)) {
					legalMoves.add(getMove(movingFromHOME, nextCell + 1, computeEffectOfMove(movingFromHOME, nextCell))); // Still add 1 here since we are converting to 1-based counting.
				}
			}
			
			//  Now see if any current pips can be moved.
			for (int cell = cellsOnBoard - 1; cell >= 0; cell--) if (board[cell] == playerID) { // Count backwards so Player O treated as mirror image of Player X.
				int nextCell = cell - dieShake; // Counting is from zero.
				
				if (nextCell < 0) { // Can go to HOME.
					legalMoves.add(getMove(cell + 1, movingToSAFETY, computeEffectOfMove(cell, nextCell)));                                // Still add 1 here since we are converting to 1-based counting.
				} else if (board[nextCell] != playerID && !isProtectedCell(nextCell)) {
					legalMoves.add(getMove(cell + 1, nextCell + 1, computeEffectOfMove(cell, nextCell))); // Still add 1 here since we are converting to 1-based counting.
				}
				
			}
		
		}
		
		if (printProgress) { Utils.print("\nLegal moves for Player " + (playerID == playerX ? "X" : "O") + " with die roll of " + dieShake + " are:"); }
		if (printProgress) { printLegalMoves(legalMoves); }
		return legalMoves;
	}
	
	private boolean onTheBoardAndOccuppiedByThisPlayer(int cell, int player) {
		if (cell < 0 || cell >= cellsOnBoard) { return false; }
		return board[cell] == player;
	}
	
	private int computeEffectOfMove(int oldCell, int newCell) {
		boolean hit              = (newCell >= 0 && newCell < cellsOnBoard && board[newCell] != empty);
		boolean breaksPrime      = ifMovedBreaksPrime(oldCell);
		int     hold             = oldCell == movingFromHOME ? -1 : board[oldCell]; // "Lift" the pip being moved while computing impact.
		if (hold >= 0) { board[oldCell] = empty; }
		boolean protectedLeft    = onTheBoardAndOccuppiedByThisPlayer(newCell - 1, whoseTurn) && isProtectedCell(newCell - 1); // See if will be adding to an existing prime OF THIS PLAYER (the use of 'hold' makes sure this is not due to the cell being moved out of).
		boolean protectedRight   = onTheBoardAndOccuppiedByThisPlayer(newCell + 1, whoseTurn) && isProtectedCell(newCell + 1);
		boolean extendsPrime     = protectedLeft || protectedRight;
		boolean createsPrime     =   newCell >= 0 && newCell < cellsOnBoard && 
		                            (newCell == 0 ?                               board[newCell + 1] == whoseTurn
                                                  : newCell == cellsOnBoard - 1 ? board[newCell - 1] == whoseTurn 
           		                                                                : board[newCell - 1] == whoseTurn || board[newCell + 1] == whoseTurn);
		/* Used for debugging.
		if (printProgress) {
			Utils.println();
			Utils.println("  breaksPrime = " + breaksPrime);
			Utils.println("oldCell = " + (1 + oldCell) + "  newCell = " + (1 + newCell) + "  whoseTurn = " + whoseTurn);
			Utils.println("  onTheBoardAndOccuppiedByThisPlayer(newCell - 1, whoseTurn) = " + onTheBoardAndOccuppiedByThisPlayer(newCell - 1, whoseTurn));
			Utils.println("  onTheBoardAndOccuppiedByThisPlayer(newCell + 1, whoseTurn) = " + onTheBoardAndOccuppiedByThisPlayer(newCell + 1, whoseTurn));
			Utils.println();
		}
		*/
		
		if (hold >= 0) { board[oldCell] = hold; }
		
		if ( hit &&  breaksPrime &&  extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT; }
		if ( hit &&  breaksPrime &&  extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT; } // Same as above since extend subsumes creating.
		if ( hit &&  breaksPrime && !extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT; }
		if ( hit &&  breaksPrime && !extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_BREAKS_PRIME_AND_HITS_OPPONENT; }
		if ( hit && !breaksPrime &&  extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT; }
		if ( hit && !breaksPrime &&  extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT; } // Same as above since extend subsumes creating.
		if ( hit && !breaksPrime && !extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_CREATES_PRIME_AND_HITS_OPPONENT; }
		if ( hit && !breaksPrime && !extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_HITS_OPPONENT; }
		if (!hit &&  breaksPrime &&  extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_BREAKS_EXTENDS_PRIME; }
		if (!hit &&  breaksPrime &&  extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_BREAKS_EXTENDS_PRIME; } // Same as above since extend subsumes creating.
		if (!hit &&  breaksPrime && !extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_BREAKS_CREATES_PRIME; }
		if (!hit &&  breaksPrime && !extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_BREAKS_PRIME; }
		if (!hit && !breaksPrime &&  extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_EXTENDS_PRIME; }
		if (!hit && !breaksPrime &&  extendsPrime && !createsPrime) { return ManageMoveEffects.MOVE_EXTENDS_PRIME; } // Same as above since extend subsumes creating.
		if (!hit && !breaksPrime && !extendsPrime &&  createsPrime) { return ManageMoveEffects.MOVE_CREATES_PRIME; }
		if (!hit && !breaksPrime && !extendsPrime && !createsPrime) { return ManageMoveEffects.NON_DESCRIPT_MOVE; }
		Utils.waitForEnter("Should not happen in computeEffectOfMove.");
		return -1;
	}
		
	private void makeMove(int playerID, int fromCountingFromOne, int toCountingFromOne) {
		if (printProgress) { Utils.println("\n" + "Move Player " + (playerID == playerX ? "X" : "O") + " from " + (fromCountingFromOne == movingFromHOME ? "HOME" : fromCountingFromOne) + " to " + (toCountingFromOne == movingToSAFETY ? "SAFETY" : toCountingFromOne) + "."); }
		
		// Convert to our internal count-from-zero system.
		int from = (fromCountingFromOne == movingFromHOME ? movingFromHOME : fromCountingFromOne - 1);
		int to   = (toCountingFromOne   == movingToSAFETY ? movingToSAFETY : toCountingFromOne   - 1);
		
		if (from == movingFromHOME) {
			if (playerID == playerX && homePieces_playerX <= 0 || playerID == playerO && homePieces_playerO <= 0) { Utils.error("Cannot move from HOME since there are no pieces there for Player " + (playerID == playerX ? "X" : "O") + "."); }
			if (playerID == playerX) { homePieces_playerX--; } else { homePieces_playerO--; }
		} else if (from >= 0 && from < cellsOnBoard)  { // Count is from zero. 
			if (board[from] != playerID) { Utils.error("Cannot move from " + from + " because Player " + (playerID == playerX ? "X" : "O") + " is not at that board position."); }
			board[from] = empty; // Empty the FROM cell.			
		} else { Utils.error("Unexpected 'from' value: " + from); }
		
		if (to == movingToSAFETY) {
			if (playerID == playerX) {safePieces_playerX++; } else { safePieces_playerO++; }
		} else  if (to >= 0 && to < cellsOnBoard) {
			if (isProtectedCell(to))   { Utils.error("Cannot move to " + to + " because it is a protected location (i.e., a 'prime')."); }
			if (board[to] == playerID) { Utils.error("Cannot move to " + to + " because a piece of Player " + (playerID == playerX ? "X" : "O") + "  is already there."); }
			if (board[to] != empty) { // Knock the opponent back to HOME.
				if (playerID == playerX) { homePieces_playerO++; } else { homePieces_playerX++; }
			}
			board[to] = playerID;
		} else { Utils.error("Unexpected 'to' value: " + to); }
		
	}
	
	private boolean isProtectedCell(int cell) {
		if (cell < 0 || cell >= cellsOnBoard || board[cell] == empty) { return false; } // Cell is empty or off the board.
		
		if      (cell == 0)                {     return board[cell] == board[cell + 1]; } // See if NEXT cell has the same player in it.
		else if (cell == cellsOnBoard - 1) {     return board[cell] == board[cell - 1]; } // See if PREV cell has the same player in it.
		else { return board[cell] == board[cell + 1] || board[cell] == board[cell - 1]; } // See if NEXT or PREV cell has the same player in it.
	}
	
	private boolean ifMovedBreaksPrime(int cell) { // If this cell is cleared, will a prime be destroyed?
		if (cell < 0 || cell >= cellsOnBoard || board[cell] == empty || !isProtectedCell(cell)) { return false; } // Cell is empty or off the board or not in a prime.
		
		if (cellsOnBoard < 3) { Utils.error("This code assumes there are at least 3 locations on the game board."); }
		if      (cell == 0)                { return                                   board[cell] != board[cell + 2]; }  // See if NEXT NEXT cell has the same player in it (we know NEXT must since cell is prime).
		else if (cell == 1)                { return board[cell] != board[cell + 1] || board[cell] != board[cell + 2]; }  // See if NEXT and NEXT NEXT cell has the same player in it (but no need to look at PREV PREV).
		else if (cell == cellsOnBoard - 1) { return                                   board[cell] != board[cell - 2]; }  // See if PREV PREV cell has the same player in it (we know PREV must since the last cell is prime).
		else if (cell == cellsOnBoard - 2) { return board[cell] != board[cell - 1] || board[cell] != board[cell - 2]; }  // See if PREV and PREV PREV cell has the same player in it (but no need to look at NEXT NEXT).
		else {                               return(board[cell] != board[cell + 1] || board[cell] != board[cell + 2]) && // See if NEXT NEXT or PREV PREV cell has the same player in it.
			                                       (board[cell] != board[cell - 1] || board[cell] != board[cell - 2]); }
	}

	private void printLegalMoves(List<List<Integer>> legalMoves) {
		if (!printProgress) { return; }
		if (legalMoves == null)  { Utils.println("NONE"); return; }
		
		int maxMoveDescriptionLength = 0;
		if (legalMoves.size() > 0 && showNextStatesAsWell) {
			for (List<Integer> move : legalMoves) {
				int descriptionLength = ManageMoveEffects.convertMoveEffectToShortString(move.get(2)).length();
				if (descriptionLength > maxMoveDescriptionLength) { maxMoveDescriptionLength = descriptionLength; }
			}
		}

		if (showNextStatesAsWell) {
			Utils.println();
	    	int spacesNeeded = 11 + maxMoveDescriptionLength; // 3 less since "Current" longer than "Next" and 1 more since '\n' is counted below.
	    	if (spacesNeeded > 0) for (int i = 0; i < spacesNeeded; i++) { Utils.print(" "); } // Get these to align.
	    	Utils.print(" Current state:"); 
			reportBoardAsVector(getBoardConfiguration(), false);
		}
		
		int moveCounter = 0;
		for (List<Integer> move : legalMoves) {
			moveCounter++;
			StringBuffer sb = new StringBuffer(showNextStatesAsWell ? 128 : 32);
			sb.append("\n [" + moveCounter + "] ");
			if (move.get(0) == movingFromHOME) { sb.append("H"); } else { sb.append("" + move.get(0)); }
			sb.append(" -> ");
			if (move.get(1) == movingToSAFETY) { sb.append("S"); } else { sb.append("" + move.get(1)); }
			sb.append(ManageMoveEffects.convertMoveEffectToShortString(move.get(2)));
			
		    if (showNextStatesAsWell) {
		    	int spacesNeeded = 15 + maxMoveDescriptionLength - sb.length();
		    	if (spacesNeeded > 0) for (int i = 0; i < spacesNeeded; i++) { sb.append(' '); } // Get these to align.
		    	sb.append(" Next state:"); 
		    	Utils.print(sb.toString()); 
		    	reportBoardAsVector(getNextBoardConfiguration(move), false);
		    } else {
		    	Utils.print(sb.toString());
		    }
		}
		Utils.println();
	}

	private List<Integer> getMove(int i, int j, int hitOrMiss) {
		List<Integer> list = new ArrayList<Integer>(3);
		list.add(i); list.add(j); list.add(hitOrMiss);
		return list;
	}
	
	public void drawBoardInASCII() {
		if (!printProgress) { return; }
		drawBoardInASCIIregardless();
	}
	private void drawBoardInASCIIregardless() {
		
		for (int rows = piecesPerPlayer; rows > 1; rows--) {
			Utils.print("\n ");
			if (safePieces_playerO >= rows) { Utils.print("O "); } else { Utils.print("  ");  } Utils.print(" ");
			if (homePieces_playerX >= rows) { Utils.print("X");  } else { Utils.print(" ");   } Utils.print(" |");
			for (int i = 0; i < cellsOnBoard; i++)                      { Utils.print("   "); } Utils.print("| ");
			if (homePieces_playerO >= rows) { Utils.print("O "); } else { Utils.print("  ");  }
			if (safePieces_playerX >= rows) { Utils.print("X "); }
		}
		
		Utils.print("\n ");
		if (safePieces_playerO >= 1) { Utils.print("O "); } else {  Utils.print("  "); } Utils.print(" ");
		if (homePieces_playerX >= 1) { Utils.print("X");  } else {  Utils.print(" ");  } Utils.print(" |");
		for (int i = 0; i < cellsOnBoard; i++) { 
			if (board[i] == empty)   { Utils.print(" - "); } else
			if (board[i] == playerX) { Utils.print(" X "); } else
			if (board[i] == playerO) { Utils.print(" O "); } 
		}
		Utils.print("| ");
		if (homePieces_playerO >= 1) { Utils.print("O ");  } else {  Utils.print("  ");  }
		if (safePieces_playerX >= 1) { Utils.print("X");   }
		
		if (whoseTurn == playerX) {
			Utils.print("\n die=" + die_playerX);
		} else { Utils.print("\n      "); }
		Utils.print(" ");
		for (int i = 1; i <= cellsOnBoard; i++) { Utils.print("   "); }
		Utils.print(" ");
		if (whoseTurn == playerO) { // Don't draw anything if no one's turn.
			Utils.print("die=" + die_playerO);
		}
		Utils.print("\n       ");
		for (int i = 1; i <= cellsOnBoard; i++)   { Utils.print((i < 10 ? " " + i : i) + " "); } Utils.print("");
		Utils.println("");
	}
	
	private void rollForWhoGoesFirst() {
		die_playerX = Utils.randomInInterval(1, sidesOnDice);
		die_playerO = Utils.randomInInterval(1, sidesOnDice);
		if (die_playerX == die_playerO) { 
			if (printProgress) { Utils.println("Both players rolled a " + die_playerX + ", so roll again."); }
			rollForWhoGoesFirst(); 
		} else {
			if (printProgress) { Utils.println("Player X rolled a " + die_playerX + " and Player O rolled a " + die_playerO + "."); }
			if (die_playerX > die_playerO)  { die_playerX = die_playerX - die_playerO; die_playerO = -1; if (printProgress) { Utils.println("So Player X gets to move " + die_playerX + "."); }}
			else                            { die_playerO = die_playerO - die_playerX; die_playerX = -1; if (printProgress) { Utils.println("So Player O gets to move " + die_playerO + "."); }}
		}
		
	}

	public void initializeGame() {
		gamesPlayed++;  // This is really counting GAMES THAT HAVE BEEN INITIALIZED, but assume that is how we define a game.
		homePieces_playerX = piecesPerPlayer - 2; // That two are on the board to start is HARDWIRED.
		homePieces_playerO = piecesPerPlayer - 2;
		safePieces_playerX = 0;
		safePieces_playerO = 0;
		rollForWhoGoesFirst(); // Handles ties (by shaking again).
		if (die_playerX == die_playerO) { Utils.error("Bad dice state!"); }
		whoseTurn        = (die_playerX > die_playerO ? playerX : playerO); 
		
		for (int i = 0; i < cellsOnBoard; i++) { board[i] = empty; }
		board[0]                = playerX;
		board[1]                = playerX;
		board[cellsOnBoard - 2] = playerO; 
		board[cellsOnBoard - 1] = playerO;
	}

	public boolean gameDone() {
		if (safePieces_playerX >= piecesPerPlayer && safePieces_playerO >= piecesPerPlayer) { Utils.error("Cannot have BOTH players winning!"); }
		boolean result = safePieces_playerX == piecesPerPlayer || safePieces_playerO == piecesPerPlayer;
		if (result) { whoseTurn = empty; } // Use this to mark that the board needs to be reset.
		return result;
	}

	public boolean didPlayerXwin() {
		return getWinner() == playerX;
	}
	public boolean didPlayerOwin() {
		return getWinner() == playerO;
	}

	private int getWinner() {
		if (safePieces_playerX >= piecesPerPlayer && safePieces_playerO >= piecesPerPlayer) { Utils.error("Cannot have BOTH players winning!"); }
		if (safePieces_playerX >= piecesPerPlayer) { return playerX; }
		if (safePieces_playerO >= piecesPerPlayer) { return playerO; }
		return empty; // Overload this slightly and use it to mean "no winner."
	}
	
	public String getWinnersName() {
		if (safePieces_playerX >= piecesPerPlayer && safePieces_playerO >= piecesPerPlayer) { Utils.error("Cannot have BOTH players winning!"); }
		if (safePieces_playerX >= piecesPerPlayer) { return "Player X"; }
		if (safePieces_playerO >= piecesPerPlayer) { return "Player O"; }
		return "NEITHER";
	}


	private void rollDie() {
		if      (whoseTurn == playerX) { die_playerX = Utils.randomInInterval(1, sidesOnDice); }
		else if (whoseTurn == playerO) { die_playerO = Utils.randomInInterval(1, sidesOnDice); }
		else                           { Utils.error("ODD state - neither players turn!"); }
	}
	public static int getMySafeCount(int[] boardConfiguration) {
		// TODO Auto-generated method stub
		return 0;
	}
}
