/**
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Main Class File:  PlayNannon.java
// File:             BayesNetPlayer.java
// Semester:         CS540 Fall 2016
//
// Author:           Satyajit Patil / spatil5@wisc.edu
// CS Login:         jit
// Lecturer's Name:  Jude Shavlik
// Lab Section:      
//////////////////////////// 80 columns wide //////////////////////////////////

import java.util.List;

public class BayesNetPlayer_spatil5 extends NannonPlayer
{
	// Initialize and declare boardSize and pieces per player to allow player to
	// play in different types of board games
	private static int boardSize = NannonGameBoard.getCellsOnBoard();
	private static int pieces = NannonGameBoard.getPiecesPerPlayer();

	int winCount;
	int loseCount;

	// arrays for variable (wins and losses)
	int[] safePiecesX_wins;
	int[] safePiecesO_wins;
	
	int[] safePiecesX_loses;
	int[] safePiecesO_loses;

	// the multi-dimensional arrays different from the Naive-Bayes; this goes
	// beyond the NB conditional-independence assumption
	int[][] hitBreak_wins;
	int[][] hitCreate_wins;
	int[][] hitExtend_wins;
	int[][] hitBreak_loses;
	int[][] hitCreate_loses;
	int[][] hitExtend_loses;

	public String getPlayerName()
	{
		return "spatil5";
	}

	// Constructors.
	public BayesNetPlayer_spatil5()
	{
		initialize();

	}

	public BayesNetPlayer_spatil5(NannonGameBoard gameBoard)
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

		hitBreak_wins = new int[2][2];
		hitCreate_wins = new int[2][2];
		hitExtend_wins = new int[2][2];

		safePiecesX_loses = new int[pieces + 1];
		safePiecesO_loses = new int[pieces + 1];

		hitBreak_loses = new int[2][2];
		hitCreate_loses = new int[2][2];
		hitExtend_loses = new int[2][2];

		// initialized to 1 to account for the m-estimates
		winCount = 1;
		loseCount = 1;
	}

	/**
	 * Choose best move for the BayesNetPlayer
	 */
	public List<Integer> chooseMove(int[] boardConfiguration,
			List<List<Integer>> legalMoves)
	{

		List<Integer> returnedMove = null;

		// the current probability
		double odds = -1;

		if (legalMoves != null)
		{
			// FOR each move
			for (List<Integer> move : legalMoves)
			{
				// Get effect
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

				// Use resulting board to get safePiecesX and safePiecesO
				int[] resultingBoard = gameBoard
						.getNextBoardConfiguration(boardConfiguration, move);
				int safePiecesX = resultingBoard[3];
				int safePiecesO = resultingBoard[4];

				// Calculate the new odds
				double part1 = ((double) hitBreak_wins[hitOpp][brokePrime]
						/ winCount)
						* ((double) hitCreate_wins[hitOpp][createsPrime]
								/ winCount)
						* ((double) hitExtend_wins[hitOpp][extendsPrime]
								/ winCount)
						* ((double) safePiecesX_wins[safePiecesX] / winCount)
						* ((double) safePiecesO_wins[safePiecesO] / winCount)
						* ((double) winCount / (winCount + loseCount));

				double part2 = ((double) hitBreak_loses[hitOpp][brokePrime]
						/ loseCount)
						* ((double) hitCreate_loses[hitOpp][createsPrime]
								/ loseCount)
						* ((double) hitExtend_loses[hitOpp][extendsPrime]
								/ loseCount)
						* ((double) safePiecesX_loses[safePiecesX] / loseCount)
						* ((double) safePiecesO_loses[safePiecesO] / loseCount)
						* ((double) loseCount / (winCount + loseCount));

				double newOdds = part1 / part2;

				// If the new odds beat the current odds, update the current
				// odds and the returned move
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
	 * Update the statistics for the BN player
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

			List<Integer> moveChosen = allMovesThisGameForPlayer.get(myMove);

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
				safePiecesX_wins[safePiecesX] += 1;
				safePiecesO_wins[safePiecesO] += 1;

				hitBreak_wins[hitOpp][brokePrime] += 1;
				hitCreate_wins[hitOpp][createsPrime] += 1;
				hitExtend_wins[hitOpp][extendsPrime] += 1;
				winCount++;
			} else
			{
				safePiecesX_loses[safePiecesX] += 1;
				safePiecesO_loses[safePiecesO] += 1;

				hitBreak_loses[hitOpp][brokePrime] += 1;
				hitCreate_loses[hitOpp][createsPrime] += 1;
				hitExtend_loses[hitOpp][extendsPrime] += 1;
				loseCount++;
			}
		}
	}

	/**
	 * Print out data indicating what the player learned (only consider the part
	 * of your non-Naive BN that is different than your BN)
	 */
	public void reportLearnedModel()
	{
		// get most 'likely to win' and 'most likely to lose' P(HitOpponent and
		// breaksPrime | WIN=true) / P(HitOpponent and breaksPrime | WIN=false)
		int hbWinIndex = 0;
		int bWinIndex = 0;
		double hbWin = ((double) hitBreak_wins[0][0] / winCount)
				/ ((double) hitBreak_loses[0][0] / loseCount);

		int hbLoseIndex = 0;
		int bLoseIndex = 0;
		double hbLose = ((double) hitBreak_wins[0][0] / winCount)
				/ ((double) hitBreak_loses[0][0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				double newHB = ((double) hitBreak_wins[i][j] / winCount)
						/ ((double) hitBreak_loses[i][j] / loseCount);
				if (newHB > hbWin)
				{
					hbWin = newHB;
					hbWinIndex = i;
					bWinIndex = j;
				}

				if (newHB < hbLose)
				{
					hbLose = newHB;
					hbLoseIndex = i;
					bLoseIndex = j;
				}
			}
		}

		// get most 'likely to win' and 'most likely to lose' P(HitOpponent and
		// createsPrime | WIN=true) / P(HitOpponent and createsPrime | WIN=false)
		int hcWinIndex = 0;
		int cWinIndex = 0;
		double hcWin = ((double) hitCreate_wins[0][0] / winCount)
				/ ((double) hitCreate_loses[0][0] / loseCount);

		int hcLoseIndex = 0;
		int cLoseIndex = 0;
		double hcLose = ((double) hitCreate_wins[0][0] / winCount)
				/ ((double) hitCreate_loses[0][0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				double newHC = ((double) hitCreate_wins[i][j] / winCount)
						/ ((double) hitCreate_loses[i][j] / loseCount);
				if (newHC > hcWin)
				{
					hcWin = newHC;
					hcWinIndex = i;
					cWinIndex = j;
				}

				if (newHC < hcLose)
				{
					hcLose = newHC;
					hcLoseIndex = i;
					cLoseIndex = j;
				}
			}
		}

		// get most 'likely to win' and 'most likely to lose' P(HitOpponent and
		// extendsPrime | WIN=true) / P(HitOpponent and extendsPrime | WIN=false)
		int heWinIndex = 0;
		int eWinIndex = 0;
		double heWin = ((double) hitExtend_wins[0][0] / winCount)
				/ ((double) hitExtend_loses[0][0] / loseCount);

		int heLoseIndex = 0;
		int eLoseIndex = 0;
		double heLose = ((double) hitExtend_wins[0][0] / winCount)
				/ ((double) hitExtend_loses[0][0] / loseCount);

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				double newHE = ((double) hitExtend_wins[i][j] / winCount)
						/ ((double) hitExtend_loses[i][j] / loseCount);
				if (newHE > heWin)
				{
					heWin = newHE;
					heWinIndex = i;
					eWinIndex = j;
				}

				if (newHE < heLose)
				{
					heLose = newHE;
					heLoseIndex = i;
					eLoseIndex = j;
				}
			}
		}

		// Print out the statistics
		Utils.println(
				"BAYES NET:\nHit Oppponent = 0,1 (true, false)\nBreaks Prime = 0,1 (true, false)\nCreates Prime = 0,1 (true, false)\nExtends Prime = 0,1 (true, false)\nSafe Pieces X = 0,1,2,3\nsSafe Peices O = 0,1,2,3\n");

		Utils.println("\nFor a WIN, NON-NB entries most likely have values:\n");
		Utils.println("Hit Opponent = " + hbWinIndex + " and Breaks Prime = "
				+ bWinIndex + "\n" + "Hit Opponent = " + hcWinIndex
				+ " and Creates Prime = " + cWinIndex + "\n" + "Hit Opponent = "
				+ heWinIndex + " and Extends Prime = " + eWinIndex + "\n");

		Utils.println(
				"\nFor a LOSE, NON-NB entries most likely have values:\n");
		Utils.println("Hit Opponent = " + hbLoseIndex + " and Breaks Prime = "
				+ bLoseIndex + "\n" + "Hit Opponent = " + hcLoseIndex
				+ " and Creates Prime = " + cLoseIndex + "\n"
				+ "Hit Opponent = " + heLoseIndex + " and Extends Prime = "
				+ eLoseIndex + "\n");
	}
}
