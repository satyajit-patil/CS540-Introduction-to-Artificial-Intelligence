/**
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

/*
 * A player that simply random chooses from among the possible moves.
 * 
 * NOTE: I (Jude) recommend you COPY THIS TO YOUR PLAYER (NannonPlayer_yourLoginName.java) AND THEN EDIT IN THAT FILE.
 * 
 *       Be sure to change "Random-Move Player" in getPlayerName() to something unique to you!
 */

import java.util.List;

public class RandomNannonPlayer extends NannonPlayer {
	
	/*
	 * A good way to create your players is to edit these methods.  See PlayNannon.java for more details.
	 */

	@Override
	public String getPlayerName() { return "Random-Move Player"; } // <------------------ choose a name for your player here in your (ranamed) copy of this class (ok to simply use your normal name or your initials, but also consider including your login name so unique).
	
	// Constructors.
	public RandomNannonPlayer() {
		initialize();
		
	}
	public RandomNannonPlayer(NannonGameBoard gameBoard) {
		super(gameBoard);
		initialize();
	}
	
	private void initialize() {
		// Put things here needed for instance creation.
	}

	@SuppressWarnings("unused") // This prevents a warning from the "if (false)" below.
	@Override
	public List<Integer> chooseMove(int[] boardConfiguration, List<List<Integer>> legalMoves) {
		
		// Below is some code you might want to use in your solution.
		//      (a) converts to zero-based counting for the cell locations
		//      (b) converts NannonGameBoard.movingFromHOME and NannonGameBoard.movingToSAFE to NannonGameBoard.cellsOnBoard,
		//          (so you could then make arrays with dimension NannonGameBoard.cellsOnBoard+1)
		//      (c) gets the current and next board configurations.
		
		if (false && legalMoves != null) for (List<Integer> move : legalMoves) { // <----- be sure to drop the "false &&" !
			
			int fromCountingFromOne    = move.get(0);  // Convert below to an internal count-from-zero system.
			int   toCountingFromOne    = move.get(1);			
			int                 effect = move.get(2);  // See ManageMoveEffects.java for the possible values that can appear here.	
			
			// Note we use 0 for both 'from' and 'to' because one can never move FROM SAFETY or TO HOME, so we save a memory cell.
			int from = (fromCountingFromOne == NannonGameBoard.movingFromHOME ? 0 : fromCountingFromOne);
			int to   = (toCountingFromOne   == NannonGameBoard.movingToSAFETY ? 0 : toCountingFromOne);
			
			// The 'effect' of move is encoded in these four booleans:
		    boolean        hitOpponent = ManageMoveEffects.isaHit(      effect);  // Did this move 'land' on an opponent (sending it back to HOME)?
		    boolean       brokeMyPrime = ManageMoveEffects.breaksPrime( effect);  // A 'prime' is when two pieces from the same player are adjacent on the board;
		                                                                          // an opponent can NOT land on pieces that are 'prime' - so breaking up a prime of 
		                                                                          // might be a bad idea.
		    boolean extendsPrimeOfMine = ManageMoveEffects.extendsPrime(effect);  // Did this move lengthen (i.e., extend) an existing prime?
		    boolean createsPrimeOfMine = ManageMoveEffects.createsPrime(effect);  // Did this move CREATE a NEW prime? (A move cannot both extend and create a prime.)
		    
		    // Note that you can compute other effects than the four above (but you need to do it from the info in boardConfiguration, resultingBoard, and move).
		    
			// See comments in updateStatistics() regarding how to use these.
			int[] resultingBoard = gameBoard.getNextBoardConfiguration(boardConfiguration, move);  // You might choose NOT to use this - see updateStatistics().
			
			/* Here is what is in a board configuration vector.  There are also accessor functions in NannonGameBoard.java (starts at or around line 60).
			 
			   	boardConfiguration[0] = whoseTurn;        // Ignore, since it is OUR TURN when we play, by definition. (But needed to compute getNextBoardConfiguration.)
        		boardConfiguration[1] = homePieces_playerX; 
        		boardConfiguration[2] = homePieces_playerO;
        		boardConfiguration[3] = safePieces_playerX;
        		boardConfiguration[4] = safePieces_playerO;
        		boardConfiguration[5] = die_playerX;      // I added these early on, but never used them.
        		boardConfiguration[6] = die_playerO;      // Probably can be ignored since get the number of legal moves, which is more meaningful.
       
        		cells 7 to (6 + NannonGameBoard.cellsOnBoard) record what is on the board at each 'cell' (ie, board location).
        					- one of NannonGameBoard.playerX, NannonGameBoard.playerO, or NannonGameBoard.empty.
        		
			 */
			
			// DO SOMETHING HERE.             <-------------------------------------------------
		}
		
		return Utils.chooseRandomElementFromThisList(legalMoves); // In you own code you should of course get rid of this line.
	}

	@SuppressWarnings("unused") // This prevents a warning from the "if (false)" below.
	@Override
	public void updateStatistics(boolean             didIwinThisGame, 
		                         List<int[]>         allBoardConfigurationsThisGameForPlayer,
			                     List<Integer>       allCountsOfPossibleMovesForPlayer,
			                     List<List<Integer>> allMovesThisGameForPlayer) {
		
		// Do nothing with these in the random player (but hints are here for use in your players).	
		
		// However, here are the beginnings of what you might want to do in your solution (see comments in 'chooseMove' as well).
		if (false) { // <------------ Be sure to remove this 'false' *********************************************************************
			int numberOfMyMovesThisGame = allBoardConfigurationsThisGameForPlayer.size();	
			
			for (int myMove = 0; myMove < numberOfMyMovesThisGame; myMove++) {
				int[]         currentBoard        = allBoardConfigurationsThisGameForPlayer.get(myMove);
				int           numberPossibleMoves = allCountsOfPossibleMovesForPlayer.get(myMove);
				List<Integer> moveChosen          = allMovesThisGameForPlayer.get(myMove);
				int[]         resultingBoard      = (numberPossibleMoves < 1 ? currentBoard // No move possible, so board is unchanged.
						                                                     : gameBoard.getNextBoardConfiguration(currentBoard, moveChosen));
				
				// You should compute the statistics needed for a Bayes Net for any of these problem formulations:
				//
				//     prob(win | currentBoard and chosenMove and chosenMove's Effects)  <--- this is what I (Jude) did, but mainly because at that point I had not yet written getNextBoardConfiguration()
				//     prob(win | resultingBoard and chosenMove's Effects)               <--- condition on the board produced and also on the important changes from the prev board
				//
				//     prob(win | currentBoard and chosenMove)                           <--- if we ignore 'chosenMove's Effects' we would be more in the spirit of a State Board Evaluator (SBE)
				//     prob(win | resultingBoard)                                        <--- but it seems helpful to know something about the impact of the chosen move (ie, in the first two options)
				//
				//     prob(win | currentBoard)                                          <--- if you estimate this, be sure when CHOOSING moves you apply to the NEXT boards (since when choosing moves, one needs to score each legal move).
				
				if (numberPossibleMoves < 1) { continue; } // If NO moves possible, nothing to learn from (it is up to you if you want to learn for cases where there is a FORCED move, ie only one possible move).
	
				// Convert to our internal count-from-zero system.
				// A move is a list of three integers.  Their meanings should be clear from the variable names below.
				int fromCountingFromOne = moveChosen.get(0);  // Convert below to an internal count-from-zero system.
				int   toCountingFromOne = moveChosen.get(1);
				int              effect = moveChosen.get(2);  // See ManageMoveEffects.java for the possible values that can appear here. Also see the four booleans below.

				// Note we use 0 for both 'from' and 'to' because one can never move FROM SAFETY or TO HOME, so we save a memory cell.
				int from = (fromCountingFromOne == NannonGameBoard.movingFromHOME ? 0 : fromCountingFromOne);
				int to   = (toCountingFromOne   == NannonGameBoard.movingToSAFETY ? 0 : toCountingFromOne);
				
				// The 'effect' of move is encoded in these four booleans:
			    boolean        hitOpponent = ManageMoveEffects.isaHit(      effect); // Explained in chooseMove() above.
			    boolean       brokeMyPrime = ManageMoveEffects.breaksPrime( effect);
			    boolean extendsPrimeOfMine = ManageMoveEffects.extendsPrime(effect);
			    boolean createsPrimeOfMine = ManageMoveEffects.createsPrime(effect);
				
				// DO SOMETHING HERE.  See chooseMove() for an explanation of what is stored in currentBoard and resultingBoard.
			}
		}
	}
	
	@Override
	public void reportLearnedModel() { // You can add some code here that reports what was learned, eg the most important feature for WIN and for LOSS.  And/or all the weights on your features.
		Utils.println("\n-------------------------------------------------");
		Utils.println("\nSorry, I (" + getPlayerName() + ") wasn't paying attention and didn't learn a thing ...");		
		Utils.println("\n-------------------------------------------------");
	}
}
