/**
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Main Class File:  PlayNannon.java
// File:             FullJointProbTablePlayer.java
// Semester:         CS540 Fall 2016
//
// Author:           Satyajit Patil / spatil5@wisc.edu
// CS Login:         jit
// Lecturer's Name:  Jude Shavlik
// Lab Section:      
//////////////////////////// 80 columns wide //////////////////////////////////

import java.util.List;

public class FullJointProbTablePlayer_spatil5 extends NannonPlayer
{
	// declare multidimensional arrays; one for wins and one for losses
	int[][][][][][] fullJoint_wins = null;
	int[][][][][][] fullJoint_losses = null;

	/**
	 * choose a name for your player here in your (renamed) copy of this class
	 * (ok to simply use your normal name or your initials, but also consider
	 * including your login name so unique).
	 */
	public String getPlayerName()
	{
		return "spatil5";
	}

	/**
	 * Constructors to create FullJointProbabilityTable
	 */
	public FullJointProbTablePlayer_spatil5()
	{
		initialize();
	}

	public FullJointProbTablePlayer_spatil5(NannonGameBoard gameBoard)
	{
		super(gameBoard);
		initialize();
	}

	/**
	 * Initialize FullJointProbabilityTable
	 */
	private void initialize()
	{
		// Multi-Dimensional arrays for WIN and LOSE
		fullJoint_wins = new int[4][4][2][2][2][2];
		fullJoint_losses = new int[4][4][2][2][2][2];

		// initialize all values to 1
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				for (int k = 0; k < 2; k++)
				{
					for (int m = 0; m < 2; m++)
					{
						for (int u = 0; u < 2; u++)
						{
							for (int t = 0; t < 2; t++)
							{
								fullJoint_wins[i][j][k][m][u][t] = 1;
								fullJoint_losses[i][j][k][m][u][t] = 1;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Chose a move using the FullJointProbabilityTable
	 */
	public List<Integer> chooseMove(int[] boardConfiguration,
			List<List<Integer>> legalMoves)
	{
		// the move returned by the FullJointProbabilityTable
		List<Integer> returnedMove = null;

		if (legalMoves != null)
		{
			// the current probability
			double ratio = -1;

			// For each legal move
			for (List<Integer> move : legalMoves)
			{
				double newRatio = 0;

				int effect = move.get(2);

				// The 'effect' of move is encoded in these four booleans:
				// hitOpponent, brokeMyPrime, extendsPrimeOfMine,
				// createsPrimeOfMine

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

				// get the resulting board using the current board and the move
				int[] resultingBoard = gameBoard
						.getNextBoardConfiguration(boardConfiguration, move);

				// safe pieces for player x and safe pieces for player o in the
				// resulting board
				int safePiecesX = resultingBoard[3];
				int safePiecesO = resultingBoard[4];

				// calculate the new ratio and update the current ratio and
				// current chosen move if the probability of winning is higher
				newRatio = ((double) fullJoint_wins[safePiecesX][safePiecesO][brokePrime][hitOpp][extendsPrime][createsPrime])
						/ (double) (fullJoint_losses[safePiecesX][safePiecesO][brokePrime][hitOpp][extendsPrime][createsPrime]);
				if (newRatio > ratio)
				{
					returnedMove = move;
					ratio = newRatio;
				}
			}
			// return the chosen move
			return returnedMove;
		}
		return null;
	}

	/**
	 * Update the statistics of the FullJointProbabilityTable. Statistics are
	 * computed using features in the resulting board and the chosenMove's
	 * effects
	 */
	public void updateStatistics(boolean didIwinThisGame,
			List<int[]> allBoardConfigurationsThisGameForPlayer,
			List<Integer> allCountsOfPossibleMovesForPlayer,
			List<List<Integer>> allMovesThisGameForPlayer)
	{
		int numberOfMyMovesThisGame = allBoardConfigurationsThisGameForPlayer
				.size();

		// FOR each move made in the game
		for (int myMove = 0; myMove < numberOfMyMovesThisGame; myMove++)
		{
			// Get current board
			int[] currentBoard = allBoardConfigurationsThisGameForPlayer
					.get(myMove);

			// get number of possible moves
			int numberPossibleMoves = allCountsOfPossibleMovesForPlayer
					.get(myMove);

			// get the move chosen
			List<Integer> moveChosen = allMovesThisGameForPlayer.get(myMove);

			// If there are no possible moves, there is nothing to learn from
			if (numberPossibleMoves < 1)
			{
				continue;
			}
			// get resulting board due to the move chose
			int[] resultingBoard = gameBoard
					.getNextBoardConfiguration(currentBoard, moveChosen);
			int safePiecesX = resultingBoard[3];
			int safePiecesO = resultingBoard[4];

			// A move is a list of three integers. Their meanings should be
			// clear from the variable names below.
			// Convert below to an internal count-from-zero system
			int fromCountingFromOne = moveChosen.get(0);
			int toCountingFromOne = moveChosen.get(1);
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

			// Update the statistics by incrementing either wins or losses
			if (didIwinThisGame)
			{
				fullJoint_wins[safePiecesX][safePiecesO][brokePrime][hitOpp][extendsPrime][createsPrime] += 1;
			} else if (!didIwinThisGame)
			{
				fullJoint_losses[safePiecesX][safePiecesO][brokePrime][hitOpp][extendsPrime][createsPrime] += 1;
			}
		}
	}

	/**
	 * Print out data indicating what the player learned
	 */
	public void reportLearnedModel()
	{
		// declare and initialize the values of the features giving the highest
		// probability for wins and loses
		
		double mostImpWin = (double) fullJoint_wins[0][0][0][0][0][0]
				/ fullJoint_losses[0][0][0][0][0][0];
		double mostImpLose = (double) fullJoint_wins[0][0][0][0][0][0]
				/ fullJoint_losses[0][0][0][0][0][0];

		int iwin = 0;
		int jwin = 0;
		int kwin = 0;
		int qwin = 0;
		int wwin = 0;
		int rwin = 0;

		int ilose = 0;
		int jlose = 0;
		int klose = 0;
		int qlose = 0;
		int wlose = 0;
		int rlose = 0;

		//Update the values of the features giving the highest probability for wins and loses
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				for (int k = 0; k < 2; k++)
				{
					for (int q = 0; q < 2; q++)
					{
						for (int w = 0; w < 2; w++)
						{
							for (int r = 0; r < 2; r++)
							{
								double ratio = (double) fullJoint_wins[i][j][k][q][w][r]
										/ fullJoint_losses[i][j][k][q][w][r];
								if (ratio > mostImpWin)
								{
									iwin = i;
									jwin = j;
									kwin = k;
									qwin = q;
									wwin = w;
									rwin = r;
									mostImpWin = ratio;
								}
								if (ratio < mostImpLose)
								{
									ilose = i;
									jlose = j;
									klose = k;
									qlose = q;
									wlose = w;
									rlose = r;
									mostImpLose = ratio;
								}
							}
						}
					}
				}
			}
		}
		
		//Print out the statistics
		Utils.println("FULL JOINT PROBABILITY TABLE:"
				+ "fullJoint_(wins/losses)[0,1,2,3 (SafePiecesX)] [0,1,2,3 (SafePiecesO)] [0,1 (True,false Broke Opponent)] [0,1 (True, false Hit Opponent)] [0,1 (True, false Extends Prime)] [0,1 (True, false Creates Prime)]");

		Utils.println("fullJoint_wins[" + iwin + "]" + "[" + jwin + "]" + "["
				+ kwin + "]" + "[" + qwin + "]" + "[" + wwin + "]" + "[" + rwin
				+ "] = " + fullJoint_wins[iwin][jwin][kwin][qwin][wwin][rwin]);
		Utils.println("fullJoint_losses[" + ilose + "]" + "[" + jlose + "]"
				+ "[" + klose + "]" + "[" + qlose + "]" + "[" + wlose + "]"
				+ "[" + rlose + "] = "
				+ fullJoint_losses[ilose][jlose][klose][qlose][wlose][rlose]);
	}
}
