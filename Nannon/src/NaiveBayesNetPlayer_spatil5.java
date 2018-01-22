/**
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Main Class File:  PlayNannon.java
// File:             NaiveBayesNetPlayer.java
// Semester:         CS540 Fall 2016
//
// Author:           Satyajit Patil / spatil5@wisc.edu
// CS Login:         jit
// Lecturer's Name:  Jude Shavlik
// Lab Section:      
//////////////////////////// 80 columns wide //////////////////////////////////

import java.util.List;

public class NaiveBayesNetPlayer_spatil5 extends NannonPlayer
{
	// Get board size and number of pieces per player; this can vary
	private static int boardSize = NannonGameBoard.getCellsOnBoard();
	private static int pieces = NannonGameBoard.getPiecesPerPlayer();

	// win and lose count
	int winCount;
	int loseCount;

	// arrays keep count of wins and losses respective to the features
	int[] safePiecesX_wins;
	int[] safePiecesO_wins;
	int[] hitOpp_wins;
	int[] breaksPrime_wins;
	int[] createsPrime_wins;
	int[] extendsPrime_wins;

	int[] safePiecesX_loses;
	int[] safePiecesO_loses;
	int[] hitOpp_loses;
	int[] breaksPrime_loses;
	int[] createsPrime_loses;
	int[] extendsPrime_loses;

	public String getPlayerName()
	{
		return "spatil5";
	}

	// Constructors.
	public NaiveBayesNetPlayer_spatil5()
	{
		initialize();

	}

	public NaiveBayesNetPlayer_spatil5(NannonGameBoard gameBoard)
	{
		super(gameBoard);
		initialize();
	}

	/**
	 * Initialize the class variables
	 */
	private void initialize()
	{
		safePiecesX_wins = new int[pieces + 1];
		safePiecesO_wins = new int[pieces + 1];
		hitOpp_wins = new int[2];
		breaksPrime_wins = new int[2];
		createsPrime_wins = new int[2];
		extendsPrime_wins = new int[2];

		safePiecesX_loses = new int[pieces + 1];
		safePiecesO_loses = new int[pieces + 1];
		hitOpp_loses = new int[2];
		breaksPrime_loses = new int[2];
		createsPrime_loses = new int[2];
		extendsPrime_loses = new int[2];

		// initialized to one to take the m-estimates into account
		winCount = 1;
		loseCount = 1;
	}

	/**
	 * Choose the best move for the NaiveBayes player
	 */
	public List<Integer> chooseMove(int[] boardConfiguration,
			List<List<Integer>> legalMoves)
	{

		List<Integer> returnedMove = null;

		// the current odds
		double odds = -1;

		if (legalMoves != null)
		{
			// FOR each move
			for (List<Integer> move : legalMoves)
			{
				// Get the effect
				int effect = move.get(2);

				// The 'effect' of move is encoded in these four booleans:
				// Did the move land on the opponent sending it back to HOME
				boolean hitOpponent = ManageMoveEffects.isaHit(effect);
				int hitOpp = 1;
				if (hitOpponent)
				{
					hitOpp = 0;
				}

				// Did the move break my prime; a prime is when 2 pieces from
				// the same player are adjacent on the board; opponent cannot
				// land on pieces that are in the prime making it ideal not to
				// break the prime
				boolean brokeMyPrime = ManageMoveEffects.breaksPrime(effect);
				int brokePrime = 1;
				if (brokeMyPrime)
				{
					brokePrime = 0;
				}

				// Did it extend the existing prime?
				boolean extendsPrimeOfMine = ManageMoveEffects
						.extendsPrime(effect);
				int extendsPrime = 1;
				if (extendsPrimeOfMine)
				{
					extendsPrime = 0;
				}

				// Did the move CREATE a new prime? Cannot have extend and
				// create!
				boolean createsPrimeOfMine = ManageMoveEffects
						.createsPrime(effect);
				int createsPrime = 1;
				if (createsPrimeOfMine)
				{
					createsPrime = 0;
				}

				// the the resulting board from the current board and the move
				int[] resultingBoard = gameBoard
						.getNextBoardConfiguration(boardConfiguration, move);
				int safePiecesX = resultingBoard[3];
				int safePiecesO = resultingBoard[4];

				double part1 = ((double) hitOpp_wins[hitOpp] / winCount)
						* ((double) breaksPrime_wins[brokePrime] / winCount)
						* ((double) createsPrime_wins[createsPrime] / winCount)
						* ((double) safePiecesX_wins[safePiecesX] / winCount)
						* ((double) safePiecesO_wins[safePiecesO] / winCount)
						* ((double) extendsPrime_wins[extendsPrime] / winCount)
						* ((double) winCount / (winCount + loseCount));

				double part2 = ((double) hitOpp_loses[hitOpp] / loseCount)
						* ((double) breaksPrime_loses[brokePrime] / loseCount)
						* ((double) createsPrime_loses[createsPrime]
								/ loseCount)
						* ((double) safePiecesX_loses[safePiecesX] / loseCount)
						* ((double) safePiecesO_loses[safePiecesO] / loseCount)
						* ((double) extendsPrime_loses[extendsPrime]
								/ loseCount)
						* ((double) loseCount / (winCount + loseCount));

				double newOdds = part1 / part2;

				// IF the newOdds beat the current odds, update the current odds
				// and the current chosen move
				if (newOdds > odds)
				{
					returnedMove = move;
					odds = newOdds;
				}
			}
		}
		return returnedMove;
	}

	/**
	 * Update the statistics for the NB player
	 */
	public void updateStatistics(boolean didIwinThisGame,
			List<int[]> allBoardConfigurationsThisGameForPlayer,
			List<Integer> allCountsOfPossibleMovesForPlayer,
			List<List<Integer>> allMovesThisGameForPlayer)
	{

		int numberOfMyMovesThisGame = allBoardConfigurationsThisGameForPlayer
				.size();

		// FOR each move used in the game
		for (int myMove = 0; myMove < numberOfMyMovesThisGame; myMove++)
		{
			int[] currentBoard = allBoardConfigurationsThisGameForPlayer
					.get(myMove);

			int numberPossibleMoves = allCountsOfPossibleMovesForPlayer
					.get(myMove);

			// Get the move
			List<Integer> moveChosen = allMovesThisGameForPlayer.get(myMove);

			// Get the resulting board->this allows you to get safePiecesX and
			// safePiecesO
			int[] resultingBoard = (numberPossibleMoves < 1 ? currentBoard
					: gameBoard.getNextBoardConfiguration(currentBoard,
							moveChosen));
			int safePiecesX = resultingBoard[3];
			int safePiecesO = resultingBoard[4];

			// If NO move possible, nothing to learn from
			if (numberPossibleMoves < 1)
			{
				continue;
			}

			// Get the effect
			int effect = moveChosen.get(2);

			// The 'effect' of move is encoded in these four booleans:

			boolean hitOpponent = ManageMoveEffects.isaHit(effect);
			int hitOpp = 1;
			if (hitOpponent)
			{
				hitOpp = 0;
			}

			boolean brokeMyPrime = ManageMoveEffects.breaksPrime(effect);
			int brokePrime = 1;
			if (brokeMyPrime)
			{
				brokePrime = 0;
			}

			boolean extendsPrimeOfMine = ManageMoveEffects.extendsPrime(effect);
			int extendsPrime = 1;
			if (extendsPrimeOfMine)
			{
				extendsPrime = 0;
			}

			boolean createsPrimeOfMine = ManageMoveEffects.createsPrime(effect);
			int createsPrime = 1;
			if (createsPrimeOfMine)
			{
				createsPrime = 0;
			}

			// Update the statistics by incrementing the counters
			if (didIwinThisGame)
			{
				hitOpp_wins[hitOpp] += 1;
				breaksPrime_wins[brokePrime] += 1;
				createsPrime_wins[createsPrime] += 1;
				extendsPrime_wins[extendsPrime] += 1;
				safePiecesX_wins[safePiecesX] += 1;
				safePiecesO_wins[safePiecesO] += 1;
				winCount++;
			} else
			{
				hitOpp_loses[hitOpp] += 1;
				breaksPrime_loses[brokePrime] += 1;
				createsPrime_loses[createsPrime] += 1;
				extendsPrime_loses[createsPrime] += 1;
				safePiecesX_loses[safePiecesX] += 1;
				safePiecesO_loses[safePiecesO] += 1;
				loseCount++;
			}
		}
	}

	/**
	 * Print out data indicating what the player learned
	 */
	public void reportLearnedModel()
	{
		// Get 'most likely to win' and 'least likely to win' values for
		// hitOpponent
		int hoWinIndex = 0;
		double hoWin = ((double) hitOpp_wins[0] / winCount)
				/ ((double) hitOpp_loses[0] / loseCount);
		int hoLoseIndex = 0;
		double hoLose = ((double) hitOpp_wins[0] / winCount)
				/ ((double) hitOpp_loses[0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			double newHO = ((double) hitOpp_wins[i] / winCount)
					/ ((double) hitOpp_loses[i] / loseCount);
			if (newHO > hoWin)
			{
				hoWin = newHO;
				hoWinIndex = i;
			}
			if (newHO < hoLose)
			{
				hoLose = newHO;
				hoLoseIndex = i;
			}
		}

		// Get 'most likely to win' and 'least likely to win' values for
		// breaksPrime
		int bpWinIndex = 0;
		double bpWin = ((double) breaksPrime_wins[0] / winCount)
				/ ((double) breaksPrime_loses[0] / loseCount);
		int bpLoseIndex = 0;
		double bpLose = ((double) breaksPrime_wins[0] / winCount)
				/ ((double) breaksPrime_loses[0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			double newBP = ((double) breaksPrime_wins[i] / winCount)
					/ ((double) breaksPrime_loses[i] / loseCount);
			if (newBP > bpWin)
			{
				bpWin = newBP;
				bpWinIndex = i;
			}
			if (newBP < bpLose)
			{
				bpLose = newBP;
				bpLoseIndex = i;
			}
		}

		// Get 'most likely to win' and 'least likely to win' values for
		// createsPrime
		int cpWinIndex = 0;
		double cpWin = ((double) createsPrime_wins[0] / winCount)
				/ ((double) createsPrime_loses[0] / loseCount);
		int cpLoseIndex = 0;
		double cpLose = ((double) createsPrime_wins[0] / winCount)
				/ ((double) createsPrime_loses[0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			double newCP = ((double) createsPrime_wins[i] / winCount)
					/ ((double) createsPrime_loses[i] / loseCount);
			if (newCP > cpWin)
			{
				cpWin = newCP;
				cpWinIndex = i;
			}
			if (newCP < cpLose)
			{
				cpLose = newCP;
				cpLoseIndex = i;
			}
		}

		// Get 'most likely to win' and 'least likely to win' values for
		// extendsPrime
		int epWinIndex = 0;
		double epWin = ((double) extendsPrime_wins[0] / winCount)
				/ ((double) extendsPrime_loses[0] / loseCount);
		int epLoseIndex = 0;
		double epLose = ((double) extendsPrime_wins[0] / winCount)
				/ ((double) extendsPrime_loses[0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			double newEP = ((double) extendsPrime_wins[i] / winCount)
					/ ((double) extendsPrime_loses[i] / loseCount);
			if (newEP > epWin)
			{
				epWin = newEP;
				epWinIndex = i;
			}
			if (newEP < epLose)
			{
				epLose = newEP;
				epLoseIndex = i;
			}
		}

		// Get 'most likely to win' and 'least likely to win' values for
		// safePiecesX
		int spxWinIndex = 0;
		double spxWin = ((double) safePiecesX_wins[0] / winCount)
				/ ((double) safePiecesX_loses[0] / loseCount);
		int spxLoseIndex = 0;
		double spxLose = ((double) safePiecesX_wins[0] / winCount)
				/ ((double) safePiecesX_loses[0] / loseCount);

		for (int i = 0; i < pieces + 1; i++)
		{
			double newSPX = ((double) safePiecesX_wins[i] / winCount)
					/ ((double) safePiecesX_loses[i] / loseCount);
			if (newSPX > spxWin)
			{
				spxWin = newSPX;
				spxWinIndex = i;
			}
			if (newSPX < spxLose)
			{
				spxLose = newSPX;
				spxLoseIndex = i;
			}
		}

		// Get 'most likely to win' and 'least likely to win' values for
		// safePiecesO
		int spoWinIndex = 0;
		double spoWin = ((double) safePiecesO_wins[0] / winCount)
				/ ((double) safePiecesO_loses[0] / loseCount);
		int spoLoseIndex = 0;
		double spoLose = ((double) safePiecesO_wins[0] / winCount)
				/ ((double) safePiecesO_loses[0] / loseCount);

		for (int i = 0; i < pieces + 1; i++)
		{
			double newSPO = ((double) safePiecesO_wins[i] / winCount)
					/ ((double) safePiecesO_loses[i] / loseCount);
			if (newSPO > spoWin)
			{
				spoWin = newSPO;
				spoWinIndex = i;
			}
			if (newSPO < spoLose)
			{
				spoLose = newSPO;
				spoLoseIndex = i;
			}
		}

		// Print out the statistics
		Utils.println(
				"NAIVE BAYES NET:\nHit Oppponent = 0,1 (true, false)\nBreaks Prime = 0,1 (true, false)\nCreates Prime = 0,1 (true, false)\nExtends Prime = 0,1 (true, false)\nSafe Pieces X = 0,1,2,3\nSafe Peices O = 0,1,2,3\n");

		Utils.println("\nFor a WIN, variables most likely have values:\n");
		Utils.println("Hit Opponent: " + hoWinIndex + "\n" + "Breaks Prime: "
				+ bpWinIndex + "\n" + "Creates Prime: " + cpWinIndex + "\n"
				+ "Extends Prime: " + epWinIndex + "\n" + "Safe Pieces X: "
				+ spxWinIndex + "\n" + "Safe Pieces O: " + spoWinIndex + "\n");

		Utils.println(
				"\nCombination of values of features in Naive Bayes that most likely result in LOSE are:\n");
		Utils.println("Hit Opponent: " + hoLoseIndex + "\n" + "Breaks Prime: "
				+ bpLoseIndex + "\n" + "Creates Prime: " + cpLoseIndex + "\n"
				+ "Extends Prime: " + epLoseIndex + "\n" + "Safe Pieces X: "
				+ spxLoseIndex + "\n" + "Safe Pieces O: " + spoLoseIndex
				+ "\n");
	}
}
