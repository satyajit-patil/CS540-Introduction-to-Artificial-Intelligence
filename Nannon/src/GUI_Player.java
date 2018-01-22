import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


/*
 * NOTES: recall interaction is only called when there is a CHOICE of moves, which is a bit odd for humans playing, so a 'delay' is added in the game controller.
 * 
 * Because boards and moves are drawn BEFORE players are called, I need to skip the first post burn-in game since I want all the code changes to be local to this class.
 * Bad side effect: the number of wins or losses will be off by one.  But players probably wont run until the last game anyway and will instead abort.
 * 
 * TODO - devise a way to use some player as an ADVISOR to see what that player RECOMMENDS
 */

public class GUI_Player extends NannonPlayer {

	// Note: even if playThisManyPostBurninGamesBeforeVisualizing = 0, 1 post-burning game will be played (see comment above).
	private static boolean automaticallyPlayAnotherGameUntilQuitPressed = true; // See if there should be a button to allow continuing to the next game.
	
	private int     countOfPostBurnInGames   = 0;
	
	private boolean simplyChooseRandomMoves  = false; // Allow watching a player against the Random player.  NO LONGER MAKES SENSE TO SET TO TRUE?
	private int     sleepTimeBeforeMove      = 2000; // Units are milliseconds.  Only uses if simplyChooseRandomMoves = true;
	private boolean waitForMouseClick        = true;
	
	private boolean burnInPhaseOver          = false;
	private boolean haveSeenGamesToBeIgnored = false;
	private Boolean playingTheXs             = null;
	
	private int     wins                      = 0;
	private int     losses                    = 0;
	
	@Override
	public String getPlayerName() {
		return "Human-Powered GUI Player";
	}
	
	public GUI_Player() {
		initialize();
	}
	public GUI_Player(NannonGameBoard gameBoard) {
		super(gameBoard);
		initialize();
	}
	
	private void initialize() {

//		Nannon.setPlayThisManyPostBurninGamesBeforeVisualizing(1); // Make sure GUI turned on after first game.
		if (inBufferedReader == null) { inBufferedReader = new BufferedReader(new InputStreamReader(System.in)); }
	}
	
	private static BufferedReader inBufferedReader = null;
	@Override
	public List<Integer> chooseMove(int[] boardConfiguration,	List<List<Integer>> legalMoves) {
		int choice = -1;
		String readThis = null;

		if (!burnInPhaseOver) {	
			burnInPhaseOver = true;	
//			Utils.println("\nFor some technical reasons, one game has to be played manually before moves can be chosen via the GUI.\n");
		}
		
		if (!haveSeenGamesToBeIgnored) return Utils.chooseRandomElementFromThisList(legalMoves);  // We need to skip the first game in order for some variables to be properly set.

		Nannon.getGUI().currentPlayerIsX = playingTheXs;
		Nannon.getGUI().drawBoard(boardConfiguration, legalMoves, getMoveNumber(), wins + losses + 1, 100.0 * wins / (wins + losses + 0.0000001));
		Utils.print("\nYour turn #" + Utils.comma(getMoveNumber()) + " in Game #" + Utils.comma(wins + losses + 1) + ".");
			
		if (waitForMouseClick) {
			// I don't recall the best way to wait for a mouse click, so I will busy wait (probably need to create a thread that sleeps until awakened ...)
			while (Nannon.getGUI().stillWaitingForUser()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
			int from = Nannon.getGUI().getMoveFrom(); 
			int to   = Nannon.getGUI().getMoveTo();
			for (List<Integer> move : legalMoves) {
				if (move.get(0) == from) {
					if (move.get(1) == to) {
//						Nannon.getGUI().animateMove(boardConfiguration, from, to);
						return move;
					}
					Utils.error("Mismatch: TO should be " + move.get(1) + " but found " + to);
				}
			}
			Utils.println("\nCurrent possible moves:");
			for (List<Integer> move : legalMoves) { Utils.println("  " + move.get(0) + " -> " + move.get(1)); }
			Utils.waitForEnter("  No match for move: " + from + " -> " + to);
		}
		if (simplyChooseRandomMoves) {
			try {
				Thread.sleep(sleepTimeBeforeMove);
			} catch (InterruptedException e) {	}
			return Utils.chooseRandomElementFromThisList(legalMoves);
		}
		
		// Should not reach here?
		while (choice < 1 || choice > legalMoves.size()) {
			Utils.print(" Choose one of the possible moves by typing one of {1");
			for (int i = 2; i < legalMoves.size(); i++) Utils.print(", " + i);
			Utils.print(" or " + legalMoves.size());
			Utils.print("} and then hitting Enter.  ");
			try {
	        	readThis = inBufferedReader.readLine();
	        	if (readThis != null) {
	        		choice = Integer.parseInt(readThis);
	        	}
	        	if (choice < 1 || choice > legalMoves.size()) { Utils.println("  Your choice of '" + choice + "' is invalid.  Please try again."); }
	        } catch (Exception e) {
	        	Utils.println("  Your entered something other than an integer.  Please try again.");
	        	inBufferedReader = new BufferedReader(new InputStreamReader(System.in));;  // If something went wrong, reset the reader. 
	        };			
		}				
		
		if (choice < 1 || choice > legalMoves.size()) { Utils.chooseRandomElementFromThisList(legalMoves); } // Include this in case anything weird happens.
		return legalMoves.get(choice - 1);
	}

	@Override
	public void updateStatistics(boolean didIwinThisGame,
							     List<int[]> allBoardConfigurationsThisGameForPlayer,
							     List<Integer> allCountsOfPossibleMovesForPlayer,
							     List<List<Integer>> allMovesThisGameForPlayer) {
		
		if (allBoardConfigurationsThisGameForPlayer == null) { return; }
						
		if (burnInPhaseOver) {
			countOfPostBurnInGames++;
			if (Nannon.getPlayThisManyPostBurninGamesBeforeVisualizing() > 0) return; // This is decremented, so compare to ZERO rather than countOfPostBurnInGames.
		}
		
		if (!burnInPhaseOver || !haveSeenGamesToBeIgnored) {
			
			if (burnInPhaseOver) {
				Utils.println("The GUI will be active for the next game now that the burn-in period has completed.");
				haveSeenGamesToBeIgnored = true;
				if (playingTheXs == null) { // Determine which player this instance is.
					for (int i = 0; i < allMovesThisGameForPlayer.size(); i++) {
						List<Integer> move = allMovesThisGameForPlayer.get(i);
						if (move != null && move.get(0) != NannonGameBoard.movingFromHOME  && move.get(1) != NannonGameBoard.movingToSAFETY) {
							playingTheXs = (move.get(0) < move.get(1));
							NannonGUI.setHumanIsPlayerX(playingTheXs);
//							Utils.println("Looks like you are playing the " + (playingTheXs ? "RED" : "WHITE") + "'s since " + move.get(0) + " -> " + move.get(1) + " is a possible move.");
							break;
						}
					}
					if (playingTheXs == null)  { Utils.error("Have not been able tell if I am Player X or Player O."); }
					Nannon.getGUI().reportGameCounter(1); // Reset so we can count wins and losses AFTER the game is over.
					Nannon.getGUI().setWins(0, 0);
					Nannon.getGUI().repaint();
				}
			}
			return;
		}
		
		// No machine learning.  A human is playing.
		if (didIwinThisGame) { wins++; } else { losses++; }
		Utils.println("\n***** You " + (didIwinThisGame ? "won" : "lost") + " this game.  Overall, " + Utils.comma(wins) + " wins and " + Utils.comma(losses) + " losses (" 
					  + Utils.truncate(100.0 * wins / (wins + losses), 2) + "% wins).");
				
		if (automaticallyPlayAnotherGameUntilQuitPressed) { return; }
		if (waitForMouseClick) {
			Nannon.getGUI().seeIfUserWantsToPlayAnotherGame();
			// I don't recall the best way to wait for a mouse click, so I will busy wait (probably need to create a thread that sleeps until awakened ...)
			while (!Nannon.getGUI().readyToPlayAgain()) {
				try { Thread.sleep(10);	} catch (InterruptedException e) {	}
			}
		} else if (simplyChooseRandomMoves) { return; }
		else {
			try {
				String readThis = inBufferedReader.readLine();
				if ("q".equals(readThis)) { Utils.println("\nThanks for playing!\n"); System.exit(0); }
			} catch (IOException e) {
				inBufferedReader = new BufferedReader(new InputStreamReader(System.in));
			}
		}
		
	}

	@Override
	public void reportLearnedModel() {
		Utils.println("\nIt's all in your head. Not mine (" + getPlayerName() + ").");

	}

}
