/**
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jude Shavlik
 * 
 * Nannon is copyrighted (2004) by Jordan Pollack.  He has given permission for use by Jude Shavlik in his U-Wisconsin courses.
 * 
 * CS 540 students need not read this file and should instead read PlayNannon.java.
 * 
 * This HW involves using probabilistic reasoning to play a simplified version of the 
 * dice-based game backgammon (http://en.wikipedia.org/wiki/Backgammon).  
 * 
 * We will be using something a friend of mine (Jordan Pollack of Brandeis University) created, called Nannon.  
 * The game is available for free for iPhones and iPads (http://nannon.com/play.html) 
 * and can be played for free on the web (http://www.nannon.com/cgi-bin/nannon.cgi).  
 * There is also a way to get a free copy that runs in Windows (http://nannon.com/play.html). 
 * 
 * Here are the rules: http://nannon.com/rules.html. 
 *
 */
public class Nannon {
	private static boolean useGUItoWatch      = false;  // After the burn-in games and possibly some additional learning, watch the two players.
	private static boolean waitBeforeEachMove = true;   // If true, will wait for user to click a button before progressing.
	// The number below says how much post burn-in training should take place silently.
	private static int playThisManyPostBurninGamesBeforeVisualizing = 1000000;
	private static int secondsToSleepAtStart                        =      -1; // If positive, allows some setup time before even the burn-in phase starts (in case it is very short and want to nake a videotape).

	private static boolean hidePlayerNames     = false;      // Set to 'true' if we want to use the names below.
	private static String  hiddenNameToUseForX = "Player X"; // We might want to make these anonymous for, say, YouTube videos.
	private static String  hiddenNameToUseForO = "Player O";
	
	
	// If any of these are more than 2 billion, need to use long's instead of int's.
	// TODO - make these PRIVATE and provide accessors, so in the class tourney we can prevent players from changing these.
    private static int numberOfGamesInBurnInPhase =     100000;  // Do random moves initially to get some idea of which moves are good or bad (and DO NOT count wins/losses in this phase).
												                 // I (Jude) experimented with this setting and this is a good value, so let's all use it.
	private static int gamesToPlay                =    1000000;  // On my desktop, this code takes 5 seconds to play 1 million games IF printProgress=false;  otherwise 1000 games can be played in about 5 seconds.

	public final static int maxMovesInGame        =        250;  // This shouldn't matter unless the game board is very wide (if set to at least, say, 250), but include it for safety.
	private static int progressReportingInterval  =     500000;
	
	private static boolean printProgress     = false;  // If true, will print boards as the games are played (but will run 1000x slower).
	// Also allow the code to stop between moves and/or games.
	private static boolean waitAfterEachMove = isPrintProgress() && true; // Silly to stop after each game if printProgress = false, so "AND" that in.
	private static boolean waitAfterEachGame = isPrintProgress() && true;
	private static boolean reportDistributionOfNumberOfPossibleMoves = false; // Report
	private static boolean reportLearnedModels = true; // After the games are played, ask the players to report what they learned. 
	
	private static NannonGUI gui = null;
	
	static public int[] playGames(String playerXid, String playerOid) {
		int winsForX = 0, burnin_winsForX = 0; // The 'burn in' wins should be about the same.
		int winsForO = 0, burnin_winsForO = 0;
		int draws    = 0; // If the board is wider than 11, it is possible to get this: "XXXXXXOOOOOO" and no moves are possible (currently this is only detected because the maxMovesInGame limit is reached).

		int total_movesInTheseGames = 0; // DO NOT COUNT these during burn-in games.
		int total_forcedMovesX      = 0;
		int total_forcedMovesO      = 0;
		int total_noMovesX          = 0;
		int total_noMovesO          = 0;
		
		boolean inBurnInPhase = true;
		
		NannonGameBoard gb = new NannonGameBoard(); // Create a game board.
		gb.printProgress = isPrintProgress() && !inBurnInPhase;  // If set to 'false' then the NannonGameBoard won't waste CPU cycles printing out the board state, the legal moves, etc.
		
		NannonPlayer playerX = getPlayerBasedOnString(gb, playerXid); // Tell the players about the game board being used.
		NannonPlayer playerO = getPlayerBasedOnString(gb, playerOid);
		playerX.setPrintProgress(isPrintProgress() && !inBurnInPhase); 
		playerO.setPrintProgress(isPrintProgress() && !inBurnInPhase);
		
		// This is used to keep track of the distribution of the number of legal moves.
		long[] distMovesX = new long[NannonGameBoard.getPiecesPerPlayer() + 1];  for (int i = 0; i <= NannonGameBoard.getPiecesPerPlayer(); i++) { distMovesX[i] = 0; }
		long[] distMovesO = new long[NannonGameBoard.getPiecesPerPlayer() + 1];  for (int i = 0; i <= NannonGameBoard.getPiecesPerPlayer(); i++) { distMovesO[i] = 0; }
		
		playerX.setPrintProgress(isPrintProgress() && !inBurnInPhase); 
		playerO.setPrintProgress(isPrintProgress() && !inBurnInPhase);
		
		long startTimeInMilliseconds = System.currentTimeMillis(); // Record start time.
		
		setGUI((!isUseGUItoWatch() ? null : new NannonGUI(NannonGameBoard.getCellsOnBoard(), NannonGameBoard.getPiecesPerPlayer(), null, isWaitBeforeEachMove(), null)));
		if (getGUI() != null) { 
			if (isHidePlayerNames()) {
				getGUI().setPlayersNames(getHiddenNameToUseForX(), getHiddenNameToUseForO());
			} else {
				getGUI().setPlayersNames(playerX.getPlayerName() + " (" + playerXid + ")", playerO.getPlayerName() + " (" + playerOid + ")");
			}
			getGUI().repaint(); // Finish displaying before starting the burn-in games.
			getGUI().reportGameCounter(1); // TODO - clean up duplicated code.
			getGUI().setBurnin(getNumberOfGamesInBurnInPhase(), getPlayThisManyPostBurninGamesBeforeVisualizing());
			getGUI().setWins(0, 0); // This will cause the "Waiting for" to be put in the title bar.
			for (int s = 0; s < 2 * getSecondsToSleepAtStart(); s++) try { Thread.sleep(500); getGUI().repaint(); } catch (InterruptedException e) { 	} // Used to allow time to start a videtaping.
		}
		
		for (int gameNumber = 1; gameNumber <= getGamesToPlay(); gameNumber++) {
			int movesInThisGames = 0;
			int forcedMovesX     = 0;
			int forcedMovesO     = 0;
			int noMovesX         = 0;
			int noMovesO         = 0;
			int movesForPlayerX  = 0;
			int movesForPlayerO  = 0;
			
			if (inBurnInPhase && gameNumber > getNumberOfGamesInBurnInPhase()) {
				if (getGUI() != null) getGUI().burninPhaseOver(getPlayThisManyPostBurninGamesBeforeVisualizing());
				inBurnInPhase = false;
				playerX.printProgress = isPrintProgress();
				playerO.printProgress = isPrintProgress();
				gb.printProgress      = isPrintProgress();
			}
			
		//  Use some variant of the following to start watching games after the burn-in phase.
		//	if (gameNumber > numberOfGamesInBurnInPhase) { playerX.setPrintProgress(true); playerO.setPrintProgress(true); gb.setPrintProgress(true); printProgress  = true; waitAfterEachMove = false; waitAfterEachGame = true; }
			
			if (!inBurnInPhase && (gameNumber - getNumberOfGamesInBurnInPhase()) % getProgressReportingInterval() == 0) { 
				Utils.println(Utils.comma(gameNumber - getNumberOfGamesInBurnInPhase()) + " post burn-in games played so far.  Took " 
							   + Utils.convertMillisecondsToTimeSpan(System.currentTimeMillis() - startTimeInMilliseconds)  + "."
							   +     "\n  Player X (" + playerX.getPlayerName() + ") won " + Utils.comma(winsForX) + " games (" + Utils.truncate(100.0 * winsForX / (winsForX + winsForO), 2) + "%)"
							   + " and\n  Player O (" + playerO.getPlayerName() + ") won " + Utils.comma(winsForO) + " games (" + Utils.truncate(100.0 * winsForO / (winsForX + winsForO), 2) + "%)"
							   + (NannonGameBoard.getPiecesPerPlayer() != 3 ? ",  getPiecesPerPlayer() = " + NannonGameBoard.getPiecesPerPlayer() : "")
							   + (NannonGameBoard.getCellsOnBoard()    != 6 ? ",  getCellsOnBoard() = "    + NannonGameBoard.getCellsOnBoard()    : "")
							   + ".");
			}
			
			// These are to be used to update statistics after finding out who won this game.			
			List<int[]>         allBoardConfigurationsThisGameForPlayerX = new ArrayList<int[]>(5);
			List<int[]>         allBoardConfigurationsThisGameForPlayerO = new ArrayList<int[]>(5);
			List<Integer>       allCountsOfLegalMovesForPlayerX          = new ArrayList<Integer>(5);
			List<Integer>       allCountsOfLegalMovesForPlayerO          = new ArrayList<Integer>(5);
			List<List<Integer>> allMovesThisGameForPlayerX               = new ArrayList<List<Integer>>(5);
			List<List<Integer>> allMovesThisGameForPlayerO               = new ArrayList<List<Integer>>(5);
			
			gb.initializeGame(); // Create an initial state for a game, including the die throws by each player to see who goes first.
			
			while (!gb.gameDone() && movesInThisGames++ <= maxMovesInGame) {	
				if (gb.getGamesPlayed() > getNumberOfGamesInBurnInPhase()) total_movesInTheseGames++;
				
				if (isPrintProgress() && !inBurnInPhase) { Utils.print("\nGame #" + Utils.comma(gameNumber) + ", Move #" + Utils.comma(movesInThisGames) + "\n-------------------------\n"); }
				if (isPrintProgress() && !inBurnInPhase) { gb.drawBoardInASCII(); } // Report the state of the game (nothing will print if gb.printProgress=false).
				
				int[]       boardConfiguration = gb.getBoardConfiguration(); // See the definition of getBoardConfiguration() to see what is stored here.
				List<List<Integer>> legalMoves = gb.getLegalMoves();	
				int          countOfLegalMoves = (legalMoves == null ? 0 : legalMoves.size());
				
				if (gb.getWhoseTurn() == NannonGameBoard.playerX) {
					movesForPlayerX++;
					if (gb.getGamesPlayed() > getNumberOfGamesInBurnInPhase()) if (countOfLegalMoves == 0) { noMovesX++; total_noMovesX++; } else if (countOfLegalMoves == 1) { forcedMovesX++; total_forcedMovesX++; }
					playerX.setMoveNumber(movesForPlayerX);
					if (countOfLegalMoves <= NannonGameBoard.getPiecesPerPlayer())  { distMovesX[countOfLegalMoves]++; } else { Utils.error("Too many possible moves?  " + legalMoves); }
				} else {
					movesForPlayerO++;
					if (gb.getGamesPlayed() > getNumberOfGamesInBurnInPhase()) if (countOfLegalMoves == 0) { noMovesO++; total_noMovesO++; } else if (countOfLegalMoves == 1) { forcedMovesO++; total_forcedMovesO++; }
					playerO.setMoveNumber(movesForPlayerO);
					if (countOfLegalMoves <= NannonGameBoard.getPiecesPerPlayer())  { distMovesO[countOfLegalMoves]++; } else { Utils.error("Too many possible moves?  " + legalMoves); }
				}
				
				List<Integer> chosenMove = null;	
				if (getGUI() != null && gb.getGamesPlayed() > getNumberOfGamesInBurnInPhase() && getPlayThisManyPostBurninGamesBeforeVisualizing() <= 0) {
					boolean XisPlaying = (gb.getWhoseTurn() == NannonGameBoard.playerX);

					getGUI().currentPlayerIsX = XisPlaying;
					getGUI().setWins(winsForX, winsForO);
					getGUI().drawBoard(boardConfiguration, legalMoves, (XisPlaying ? playerX.getMoveNumber() : playerO.getMoveNumber()), 
									winsForX + winsForO + 1, 
									100.0 * (XisPlaying? winsForX : winsForO) / (winsForX + winsForO + 0.0000001));
				}
				if (countOfLegalMoves > 0) { // See if there are any legal moves.  If not, the current player loses his/her/its turn.
					// Use the above two variables (boardConfiguration and legalMoves) to decide on a move.
					if (countOfLegalMoves == 1) {  // Forced move, no need to reason about which to choose.
						chosenMove = legalMoves.get(0);
					} else if (gb.getGamesPlayed() <= getNumberOfGamesInBurnInPhase()) { // For the first N games, simply chose actions randomly and gather statistics (called a "burn in" or "exploratory" phase).
						chosenMove = Utils.chooseRandomElementFromThisList(legalMoves);
					} else {
						chosenMove = (gb.getWhoseTurn() == NannonGameBoard.playerX
												  ? playerX.chooseMove(boardConfiguration, legalMoves) 
							                      : playerO.chooseMove(boardConfiguration, legalMoves));
					}
					
					if (chosenMove == null) { Utils.error("Why is chosenMove == null?"); }
					int from = chosenMove.get(0); // Pull out the fields of the move.  (The third argument in a move is not needed; its role is to show which moves cause a piece of the opponent to be sent back HOME (without its supper, even).
					int to   = chosenMove.get(1);

					
					if (getGUI() != null && gb.getGamesPlayed() > getNumberOfGamesInBurnInPhase() && getPlayThisManyPostBurninGamesBeforeVisualizing() <= 0) {
						boolean XisPlaying = (gb.getWhoseTurn() == NannonGameBoard.playerX);

						getGUI().animateMove(boardConfiguration, from, to);
					}
					
					
					gb.makeMove(from, to); // Make the move.
					if (isWaitAfterEachMove()  && !inBurnInPhase) { Utils.waitForEnter("Waiting after a move."); }
				} else { // If there are no legal moves, a player must pass (we could automatically do this, but are requiring it to be explicitly called so nothing 'magical' happens 'behind the scenes.'
					if (getGUI() != null && gb.getGamesPlayed() > getNumberOfGamesInBurnInPhase() && getPlayThisManyPostBurninGamesBeforeVisualizing() <= 0) {
						try { Thread.sleep(1500); } catch (InterruptedException e) {	} // Sleep a little so user can see the no-moves case.
					}
					gb.passTurn();
				//	if (waitAfterEachMove) { Utils.waitForEnter(); } // Uncomment this line if you want to wait even when there is no possible move.  Or add yet another boolean.
				}
				
				// Remember what happened so when game is over can update the proper statistics.
				if (gb.getWhoseTurn() == NannonGameBoard.playerX) {
					allBoardConfigurationsThisGameForPlayerX.add(boardConfiguration); // NOTE: getBoardConfiguration() produces NEW memory cells, so we can hold on to these w/o copying.
					allCountsOfLegalMovesForPlayerX.add(         countOfLegalMoves);
					allMovesThisGameForPlayerX.add(              chosenMove);
				} else {
					allBoardConfigurationsThisGameForPlayerO.add(boardConfiguration);
					allCountsOfLegalMovesForPlayerO.add(         countOfLegalMoves);
					allMovesThisGameForPlayerO.add(              chosenMove);
				}
				
				gb.changeTurns();
			}
			
			// Game is over.
			if (isPrintProgress() && !inBurnInPhase) { Utils.println("\nGAME OVER!  The winner of Game #" + Utils.comma(gameNumber) + " is " + gb.getWinnersName() + ".\n-------------------------------------------------------\n"); }
			if (inBurnInPhase) {
				if      (gb.didPlayerXwin()) { burnin_winsForX++; } 
				else if (gb.didPlayerOwin()) { burnin_winsForO++; }  
			//	if (gui != null && ((burnin_winsForX + burnin_winsForO) % 10000) == 0) gui.setWins(burnin_winsForX, burnin_winsForO); 
			}
			if (getGUI() != null && !inBurnInPhase && getPlayThisManyPostBurninGamesBeforeVisualizing() > 0) {
				if (((winsForX + winsForO) % NannonGUI.reportingPeriodForGames) == 0)  { // Every N games report the score.
					getGUI().setWins(winsForX, winsForO);
					getGUI().recordWinningPercentageDuringSilentRunning(winsForX, winsForO, gameNumber - getNumberOfGamesInBurnInPhase());
				}
			}
			if (!inBurnInPhase) { // Don't count wins during burn-in phase.
				setPlayThisManyPostBurninGamesBeforeVisualizing(getPlayThisManyPostBurninGamesBeforeVisualizing() - 1); // When this reaches 0, we'll start visualizing moves.
				if      (gb.didPlayerXwin()) { winsForX++; } 
				else if (gb.didPlayerOwin()) { winsForO++; } 
				else {
					draws++;
					if (NannonGameBoard.getCellsOnBoard() < 12) { Utils.println("Neither player won game #" + Utils.comma(gameNumber) + " of " + Utils.comma(draws+ winsForX + winsForO) + " games."); }
					if (NannonGameBoard.getCellsOnBoard() < 12) { Utils.waitForEnter("This should not happen unless maxMovesInGame is set too low."); }
				}
			}
			// Draws (i.e., ties) are rare, and lets lump them with losses (after all, our goal is to predict the probability of winning).
		 	playerX.updateStatistics(gb.didPlayerXwin(), allBoardConfigurationsThisGameForPlayerX, allCountsOfLegalMovesForPlayerX, allMovesThisGameForPlayerX);
		 	playerO.updateStatistics(gb.didPlayerOwin(), allBoardConfigurationsThisGameForPlayerO, allCountsOfLegalMovesForPlayerO, allMovesThisGameForPlayerO);
			if (isWaitAfterEachGame() && !inBurnInPhase) { Utils.waitForEnter("Waiting after a game."); }
		}

		long currentTimeInMilliseconds = System.currentTimeMillis(); // See how we have been playing the game.
		
		if (isReportDistributionOfNumberOfPossibleMoves()) {
			Utils.println("\n Moves   Occurrences\n");
			for (int i = 0 ; i <= NannonGameBoard.getPiecesPerPlayer(); i++) { Utils.println("  "  + Utils.padLeft(i, 2) 
																						+       Utils.padLeft(distMovesX[i], 10)  + " for Player X\n"
															                            + " " + Utils.padLeft(distMovesO[i], 10)  + " for Player O\n"); }
		}
		
		Utils.println(   "\nPlayer X (" + playerX.getPlayerName() + ") won " + Utils.comma(winsForX) + " games (" + Utils.truncate(100.0 * winsForX / (draws+ winsForX + winsForO), 2) + "%)" +
				      " and Player O (" + playerO.getPlayerName() + ") won " + Utils.comma(winsForO) + " games (" + Utils.truncate(100.0 * winsForO / (draws+ winsForX + winsForO), 2) + "%).");
		if (draws > 0) { Utils.println("There were " + Utils.comma(draws) + " draws."); }
		Utils.print(  "It took " +Utils.convertMillisecondsToTimeSpan(currentTimeInMilliseconds - startTimeInMilliseconds) + " to play " + Utils.comma(getGamesToPlay()) + " games.");
		Utils.println(" The numberOfGamesInBurnInPhase = " + Utils.comma(getNumberOfGamesInBurnInPhase()) + (NannonGameBoard.getPiecesPerPlayer() != 3 ? ",  getPiecesPerPlayer() = " + NannonGameBoard.getPiecesPerPlayer() : "") + ".");
		if (getGamesToPlay() > getNumberOfGamesInBurnInPhase()) { // Allow this to be turned off in tournaments to save load in Condor.
			Utils.println("    The mean number of turns per game:                    " + Utils.padLeft(Utils.truncate(0.5 + total_movesInTheseGames / (2.0 * (getGamesToPlay() - getNumberOfGamesInBurnInPhase())), 2), 6) + "  // These stats are for post burn-in games only."); // Add the 0.5 since half the games end with the first player winning.
			Utils.println("    The mean number of no-possible-moves per game for X:  " + Utils.padLeft(Utils.truncate(total_noMovesX     / (double) (getGamesToPlay() - getNumberOfGamesInBurnInPhase()),           2), 6));
			Utils.println("    The mean number of no-possible-moves per game for O:  " + Utils.padLeft(Utils.truncate(total_noMovesO     / (double) (getGamesToPlay() - getNumberOfGamesInBurnInPhase()),           2), 6));
			Utils.println("    The mean number of forced moves per game for X:       " + Utils.padLeft(Utils.truncate(total_forcedMovesX / (double) (getGamesToPlay() - getNumberOfGamesInBurnInPhase()),           2), 6));
			Utils.println("    The mean number of forced moves per game for O:       " + Utils.padLeft(Utils.truncate(total_forcedMovesO / (double) (getGamesToPlay() - getNumberOfGamesInBurnInPhase()),           2), 6));
		}
		
		if (isReportLearnedModels()) {
			playerX.reportLearnedModel();
			playerO.reportLearnedModel();
		}
		
		int[] results = new int[3];
		results[0] = winsForX;
		results[1] = winsForO;
		results[2] = draws;
		
		return results;
	}

	private static NannonPlayer getPlayerBasedOnString(NannonGameBoard gb, String id) {
		// Tell the players about the game board being used.
		
		if        (id.equalsIgnoreCase("random")             || id.equalsIgnoreCase("rand")) { // Allow some aliases.
			return new RandomNannonPlayer(gb);
		} else if (id.equalsIgnoreCase("manual")) {
			return new ManualPlayer(gb);
		} else if (id.equalsIgnoreCase("GUI")) {
			return new GUI_Player(gb);
		// My various solutions are not being released since it is too easy to "reverse compile" them and see the Java code.
		// I (Jude) am leaving this code here so I can uncomment the cases I wish to try out on my computer.
//		} else if (id.equalsIgnoreCase("jshavlik_easy")      || id.equalsIgnoreCase("jshavlik_weak")) {	
//			JWS_NaiveBayes_Player player = new JWS_NaiveBayes_Player(0.0001, gb); // A good setting for this fraction depends on the number of burn-in games.
//			player.setPlayerName("JWS's weak Naive Bayes Player"); 
//			player.useExtraFeatures(false);  // Ignores 99.99% of games and also ignores some features.
//			return player;
//		} else if (id.equalsIgnoreCase("jshavlik_med")       || id.equalsIgnoreCase("jshavlik_medium") || id.equalsIgnoreCase("jshavlik_naiveBayes") || id.equalsIgnoreCase("jws_naiveBayes")) {
//			return new JWS_NaiveBayes_Player(1.0, gb);
//		} else if (id.equalsIgnoreCase("jshavlik_smart")     || id.equalsIgnoreCase("jshavlik_strong") || id.equalsIgnoreCase("jshavlik_bayesNet")   || id.equalsIgnoreCase("jws_bayesNet")) {
//			return new JWS_BayesNet_Player(1.0, gb);
//		} else if (id.equalsIgnoreCase("jshavlik_smart13")     || id.equalsIgnoreCase("jshavlik_strong13") || id.equalsIgnoreCase("jshavlik_bayesNet13")   || id.equalsIgnoreCase("jws_bayesNet13")) {
//			return new JWS_BayesNet_PlayerSpring13(1.0, gb);
//			return new JWS_FullJointDistribution_PlayerSpring13(gb); 
//		} else if (id.equalsIgnoreCase("jshavlik_explorer1") || id.equalsIgnoreCase("jshavlik_explorer")) { // An experimental player that continues to explore even after the burnin-phase has completed.
//			JWS_BayesNet_Player player = new JWS_BayesNet_Player(1.0, gb);
//			player.setExplorationProb(0.01); // Even after the burn-in period, take some second best choices sometimes.
//			return player;
//		} else if (id.equalsIgnoreCase("jshavlik_explorer3")) { // An experimental player that continues to explore even after the burnin-phase has completed.
//			JWS_BayesNet_Player player = new JWS_BayesNet_Player(1.0, gb);
//			player.setExplorationProb(0.03); // Even after the burn-in period, take some second best choices sometimes.
//			return player;
//		} else if (id.equalsIgnoreCase("jshavlik_explorer5")) { // An experimental player that continues to explore even after the burnin-phase has completed.
//			JWS_BayesNet_Player player = new JWS_BayesNet_Player(1.0, gb);
//			player.setExplorationProb(0.05); // Even after the burn-in period, take some second best choices sometimes.
//			return player;
//		} else if (id.equalsIgnoreCase("jshavlik_explorer10")) { // An experimental player that continues to explore even after the burnin-phase has completed.
//			JWS_BayesNet_Player player = new JWS_BayesNet_Player(1.0, gb);
//			player.setExplorationProb(0.10); // Even after the burn-in period, take some second best choices sometimes.
//			return player;
//		} else if (id.equalsIgnoreCase("jshavlik_explorer15")) { // An experimental player that continues to explore even after the burnin-phase has completed.
//			JWS_BayesNet_Player player = new JWS_BayesNet_Player(1.0, gb);
//			player.setExplorationProb(0.15); // Even after the burn-in period, take some second best choices sometimes.
//			return player;
//		} else if (id.equalsIgnoreCase("perceptron")) { // A simple neural network written by JWS.
//			return new PerceptronNannonPlayer(gb);	
//		} else if (id.equalsIgnoreCase("jws_GA")             || id.equalsIgnoreCase("jws_genetic") ) { // This is an experimental player not released to the full cs540 class.
//			return new JWS_GeneticAlgorithmPlayer(gb);
//		} else if (id.equalsIgnoreCase("jws_fullJoint_MOVES_ONLY")) { // This is an experimental player not released to the full cs540 class.
//			return new JWS_FullJointDistribution_MOVES_ONLY_Player(gb); // Can handle 3 pieces per side and 6 board cells, as well as 4 and 6, but runs out of RAM with 5 and 10 (and that is on a machine with 50GB RAM).
//	    } else if (id.equalsIgnoreCase("jws_fullJoint_EFFECTS_ONLY")) { // This is an experimental player not released to the full cs540 class.
//			return new JWS_FullJointDistribution_EFFECTS_ONLY_Player(gb); // Can handle 3 pieces per side and 6 board cells, as well as 4 and 6, but runs out of RAM with 5 and 10 (and that is on a machine with 50GB RAM).
//	    } else if (id.equalsIgnoreCase("jws_fullJoint") || id.startsWith("jws_full") ) { // PUT THAT *AFTER* THE ABOVE TWO DUE TO THE "startsWith." This an experimental player not released to the full cs540 class.
//		 	return new JWS_FullJointDistribution_Player(gb); // Can handle 3 pieces per side and 6 board cells, as well as 4 and 6, but runs out of RAM with 5 and 10 (and that is on a machine with 50GB RAM).
//        } else if (id.equalsIgnoreCase("jws_fullJoint13") || id.startsWith("jws_full13") ) { // PUT THAT *AFTER* THE ABOVE TWO DUE TO THE "startsWith." This an experimental player not released to the full cs540 class.
//		 	return new JWS_FullJointDistribution_PlayerSpring13(gb); // Can handle 3 pieces per side and 6 board cells, as well as 4 and 6, but runs out of RAM with 5 and 10 (and that is on a machine with 50GB RAM).
//        } else if (id.equalsIgnoreCase("ann")) {
//			return new JWS_OneHiddenLayerNannonPlayer(gb);	
//		} else if (id.equalsIgnoreCase("perceptron")) {
//			return new JWS_PerceptronNannonPlayer(gb);	
		} else if (id.equalsIgnoreCase("greedyHandCoded")    || id.equalsIgnoreCase("greedy") ) {
			return new HandCodedPlayer_spatil5(gb);	
		} else if (id.startsWith("HandCodedPlayer_") ) { // Note: nothing prevents this player from learning (if necessary, we could add a flag that prevents hand-coded players from getting samples of games).
			try {
				ClassLoader loader = ClassLoader.getSystemClassLoader();
				String   className = id;
				@SuppressWarnings("rawtypes")
				Class   playerClass = loader.loadClass(className);  // TODO - what should be done to avoid the need for this @SuppressWarnings("rawtypes") here?
				NannonPlayer player = (NannonPlayer) playerClass.newInstance();
				player.setGameBoard(gb);
				return player;
		    } catch (Exception e) {
		    	Utils.reportStackTrace(e);
		    	Utils.error("Problem with player spec = " + id + "\n" + e);
		    }
		} else if (id.startsWith("FullJointProbTablePlayer_") ) {
			try {
				ClassLoader loader = ClassLoader.getSystemClassLoader();
				String   className = id;
				Class<NannonPlayer>   playerClass = (Class<NannonPlayer>) loader.loadClass(className);
            //  Constructor<NannonPlayer> constructor = playerClass.getConstructor(new Class[]{NannonGameBoard.class});  TODO - use this next time.
            //  NannonPlayer player = (NannonPlayer) constructor.newInstance(gb);
				NannonPlayer player = (NannonPlayer) playerClass.newInstance();
				player.setGameBoard(gb);
				return player;
		    } catch (Exception e) {
		    	Utils.reportStackTrace(e);
		    	Utils.error("Problem with player spec = " + id + "\n" + e);
		    }
		} else if (id.startsWith("NaiveBayesNetPlayer_") ) {
			try {
			      ClassLoader loader = ClassLoader.getSystemClassLoader();
			      String   className = id;
			      Class<NannonPlayer>   playerClass = (Class<NannonPlayer>) loader.loadClass(className);
			//  Constructor<NannonPlayer> constructor = playerClass.getConstructor(new Class[]{NannonGameBoard.class});  TODO - use this next time.
			//  NannonPlayer player = (NannonPlayer) constructor.newInstance(gb);
			      NannonPlayer player = (NannonPlayer) playerClass.newInstance();
			      player.setGameBoard(gb);
			return player;
			   } catch (Exception e) {
			      Utils.reportStackTrace(e);
			      Utils.error("Problem with player spec = " + id + "\n" + e);
			   }
			}
		 else if (id.startsWith("BayesNetPlayer_") ) {
				try {
				      ClassLoader loader = ClassLoader.getSystemClassLoader();
				      String   className = id;
				      Class<NannonPlayer>   playerClass = (Class<NannonPlayer>) loader.loadClass(className);
				//  Constructor<NannonPlayer> constructor = playerClass.getConstructor(new Class[]{NannonGameBoard.class});  TODO - use this next time.
				//  NannonPlayer player = (NannonPlayer) constructor.newInstance(gb);
				      NannonPlayer player = (NannonPlayer) playerClass.newInstance();
				      player.setGameBoard(gb);
				return player;
				   } catch (Exception e) {
				      Utils.reportStackTrace(e);
				      Utils.error("Problem with player spec = " + id + "\n" + e);
				   }
				}
		else {
			try {
				ClassLoader loader = ClassLoader.getSystemClassLoader();
				String   className = (id.startsWith("BayesNetPlayer_") ? id : "BayesNetPlayer_" + id);
				Class<NannonPlayer> playerClass = (Class<NannonPlayer>) loader.loadClass(className);
	            //  Constructor<NannonPlayer> constructor = playerClass.getConstructor(new Class[]{NannonGameBoard.class});  TODO - use this next time.
	            //  NannonPlayer player = (NannonPlayer) constructor.newInstance(gb);
				NannonPlayer player = (NannonPlayer) playerClass.newInstance();
				player.setGameBoard(gb);
				return player;
		    } catch (Exception e) {
		    	Utils.reportStackTrace(e);
		    	Utils.error("Problem with player spec = " + id + "\n" + e);
		    }	
		}
		Utils.error("Something went wrong in getPlayerBasedOnString().   id = " + id);
		return null;
	}
	
	public static void main(String[] argsRaw) {
		// This main is for Jude and TAs to do tests, debug, etc.
		// Students should use PlayNannon.
		
		if (!"shavlik".equalsIgnoreCase(Utils.getUserName()) && !"jshavlik".equalsIgnoreCase(Utils.getUserName()) && 
			!"nbridle".equalsIgnoreCase(Utils.getUserName()) && !"bridle".equalsIgnoreCase(  Utils.getUserName())) {
			Utils.error("This main is for Jude and Nick to do tests, debug, etc.   Students should use PlayNannon's main().  UserName = " + Utils.getUserName());
			return;
		}		
		
		if (argsRaw.length > 2) { Utils.error("Too many arguments: " +  Utils.converStringListToString(argsRaw) + "\n" + PlayNannon.argsSpecString + "\n"); }
		
		String[] args = Utils.chopCommentFromArgs(argsRaw);
		String   arg1 = "random";
		String   arg2 = "jws_fullJoint";

	//	hidePlayerNames     = true; // For use when making a YouTube video (don't want to release student names).
	//	hiddenNameToUseForX = "Hand-Coded Player";
	//	hiddenNameToUseForO = "Bayesian Network";
		
		if      (args.length >= 1) { arg1 = args[0]; }
		else if (args.length >= 2) { arg2 = args[1]; }
		
		setUseGUItoWatch(true);
		setWaitBeforeEachMove(false);   // If true, will wait for user to click a button before progressing.
		setPlayThisManyPostBurninGamesBeforeVisualizing(1000 * 1000);
		setSecondsToSleepAtStart(3);   // To allow time to get ready for making a video.
		int animationSpeed                           =       50; // Slow down so that when we speedup the video, these games don't zoom by too fast.  TODO Comment out: Thread.sleep(xx); // Used to resize screen before 'videotaping.'
		NannonGUI.setAnimationSpeed(animationSpeed); // Set this to 0-100 to vary speed of animation (if negative, than no waiting, i.e. will draw is as fast as possible).
				
		setNumberOfGamesInBurnInPhase(100 * 1000);
		setGamesToPlay(100 + getNumberOfGamesInBurnInPhase() + getPlayThisManyPostBurninGamesBeforeVisualizing());
		
		NannonGameBoard.setPiecesPerPlayer(3);  // 3; // If too large (6 or more), boards can get into a deadlock state, though the maxMoves counter will kill such games (such drawed games can be counted as a loss for both sides - thus the Bayes Nets need only worry about prob(win)).
		NannonGameBoard.setCellsOnBoard(   6); // 2 * NannonGameBoard.getPiecesPerPlayer(); // TODO - some bugs if this is less than sidesOnDice.
//		NannonGameBoard.sidesOnDice     =  6; // If this is too small, boards can get into a deadlock state.
		
		NannonGUI.reportingPeriodForGames = Math.min(NannonGUI.reportingPeriodForGames, getPlayThisManyPostBurninGamesBeforeVisualizing() / 100); // Report at least 100 times.
		
		// Printing won't start until after the burn-in phase.
		setPrintProgress(false); // Set to 'true' to watch the games being played.
		setWaitAfterEachMove(isPrintProgress() && false); // Replace 'false' with 'true' to pause after each move.
		setWaitAfterEachGame(isPrintProgress() && false);
		
	    // If you print a LOT, comment this out (Utils.java will also stop 'dribbling' if more than 10M characters are printed).
		Utils.createDribbleFile("dribbleFiles/nannonMain_" + Utils.getUserName() + "_X=" + arg1 + "_Y=" + arg2
				+ "_" + ((Nannon.getGamesToPlay() - Nannon.getNumberOfGamesInBurnInPhase()) / 1000000) + "M_games"
				+ "_" + (Nannon.getNumberOfGamesInBurnInPhase() / 1000) + "K_burnInGames"
				+ (NannonGameBoard.getPiecesPerPlayer()     != 3                                 ? "_" + NannonGameBoard.getPiecesPerPlayer() + "_getPiecesPerPlayer()" : "")
				+ (NannonGameBoard.getPiecesPerPlayer() * 2 != NannonGameBoard.getCellsOnBoard() ? "_" + NannonGameBoard.getCellsOnBoard()    + "_boardSize"       : "")
//				+ (NannonGameBoard.sidesOnDice              != 6                                 ? "_" + NannonGameBoard.sidesOnDice          + "_sidedDice"       : "")
				+ ".txt");
		
		playGames(arg1, arg2);
	}

	// Various 'accessor' functions follow.
	
	public static void setGamesToPlay(int gamesToPlay) {
		Nannon.gamesToPlay = gamesToPlay;
	}

	public static int getGamesToPlay() {
		return gamesToPlay;
	}

	public static void setProgressReportingInterval(int progressReportingInterval) {
		Nannon.progressReportingInterval = progressReportingInterval;
	}

	public static int getProgressReportingInterval() {
		return progressReportingInterval;
	}

	public static void setNumberOfGamesInBurnInPhase(int numberOfGamesInBurnInPhase) {
		Nannon.numberOfGamesInBurnInPhase = numberOfGamesInBurnInPhase;
	}

	public static int getNumberOfGamesInBurnInPhase() {
		return numberOfGamesInBurnInPhase;
	}

	public static void setPlayThisManyPostBurninGamesBeforeVisualizing(int playThisManyPostBurninGamesBeforeVisualizing) {
		Nannon.playThisManyPostBurninGamesBeforeVisualizing = playThisManyPostBurninGamesBeforeVisualizing;
	}

	public static int getPlayThisManyPostBurninGamesBeforeVisualizing() {
		return playThisManyPostBurninGamesBeforeVisualizing;
	}

	public static void setSecondsToSleepAtStart(int secondsToSleepAtStart) {
		Nannon.secondsToSleepAtStart = secondsToSleepAtStart;
	}

	public static int getSecondsToSleepAtStart() {
		return secondsToSleepAtStart;
	}

	public static void setUseGUItoWatch(boolean useGUItoWatch) {
		Nannon.useGUItoWatch = useGUItoWatch;
	}

	public static boolean isUseGUItoWatch() {
		return useGUItoWatch;
	}

	public static void setWaitBeforeEachMove(boolean waitBeforeEachMove) {
		Nannon.waitBeforeEachMove = waitBeforeEachMove;
	}

	public static boolean isWaitBeforeEachMove() {
		return waitBeforeEachMove;
	}

	public static void setHidePlayerNames(boolean hidePlayerNames) {
		Nannon.hidePlayerNames = hidePlayerNames;
	}

	public static boolean isHidePlayerNames() {
		return hidePlayerNames;
	}

	public static void setHiddenNameToUseForX(String hiddenNameToUseForX) {
		Nannon.hiddenNameToUseForX = hiddenNameToUseForX;
	}

	public static String getHiddenNameToUseForX() {
		return hiddenNameToUseForX;
	}

	public static void setHiddenNameToUseForO(String hiddenNameToUseForO) {
		Nannon.hiddenNameToUseForO = hiddenNameToUseForO;
	}

	public static String getHiddenNameToUseForO() {
		return hiddenNameToUseForO;
	}

	public static void setPrintProgress(boolean printProgress) {
		Nannon.printProgress = printProgress;
	}

	public static boolean isPrintProgress() {
		return printProgress;
	}

	public static void setWaitAfterEachMove(boolean waitAfterEachMove) {
		Nannon.waitAfterEachMove = waitAfterEachMove;
	}

	public static boolean isWaitAfterEachMove() {
		return waitAfterEachMove;
	}

	public static void setWaitAfterEachGame(boolean waitAfterEachGame) {
		Nannon.waitAfterEachGame = waitAfterEachGame;
	}

	public static boolean isWaitAfterEachGame() {
		return waitAfterEachGame;
	}

	public static void setReportDistributionOfNumberOfPossibleMoves(boolean reportDistributionOfNumberOfPossibleMoves) {
		Nannon.reportDistributionOfNumberOfPossibleMoves = reportDistributionOfNumberOfPossibleMoves;
	}

	public static boolean isReportDistributionOfNumberOfPossibleMoves() {
		return reportDistributionOfNumberOfPossibleMoves;
	}

	public static void setReportLearnedModels(boolean reportLearnedModels) {
		Nannon.reportLearnedModels = reportLearnedModels;
	}

	public static boolean isReportLearnedModels() {
		return reportLearnedModels;
	}

	public static void setGUI(NannonGUI gui) {
		Nannon.gui = gui;
	}

	public static NannonGUI getGUI() {
		return gui;
	}
}
