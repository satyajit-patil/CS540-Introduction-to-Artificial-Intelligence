import java.util.List;

/**
 * This is a non-learning player that has some if-then rules for playing Nannon.  It isn't anything fancy.  You should
 * aim to spend a short amount of time modifying your (renamed) copy of this file to see how well you can do by
 * handcrafting a Nannon player.   Then see if a probabilistic reasoner you write can beat your hand-crafted player.
 * (Ie, can you write s/w that learns to become smarter than you are?)
 * 
 *  Note: your version of this file is not to be turned in, nor will it be graded.
 */

/**
 * @author shavlik
 *
 */
public class HandCodedPlayer_spatil5 extends NannonPlayer
{

	/**
	 * You should rename this file to: HandCodedPlayer_yourLoginName.java
	 * 
	 */
	public HandCodedPlayer_spatil5()
	{
	}

	public HandCodedPlayer_spatil5(NannonGameBoard gameBoard)
	{
		super(gameBoard);
	}

	@Override
	public String getPlayerName()
	{
		// Choose your own name for your hand-coded player (ok to use your
		// normal name or initials, but consider including your login name so
		// unique).
		return "spatil5";
	}

	@Override
	public List<Integer> chooseMove(int[] boardConfiguration,
			List<List<Integer>> legalMoves)
	{

		/*
		 * Some code taken from RandomNannonPlayer.
		 * 
		 * int fromCountingFromOne = move.get(0);
		 * 
		 * // A board position or NannonGameBoard.movingFromHOME. 
		 * int toCountingFromOne = move.get(1); 
		 * // A board position or NannonGameBoard.movingToSAFE 
		 * int effect = move.get(2);
		 * 
		 * boolean hitOpponent = ManageMoveEffects.isaHit( effect); 
		 * boolean breakMyPrime = ManageMoveEffects.breaksPrime( effect); 
		 * boolean extendPrimeOfMine = ManageMoveEffects.extendsPrime(effect); 
		 * boolean createPrimeOfMine = ManageMoveEffects.createsPrime(effect);
		 * 
		 * For a move in legalMoves, you can get the the next board via: 
		 * int[] resultingBoard = gameBoard.getNextBoardConfiguration(boardConfiguration, move);
		 */

		// Note: when only one piece left, all moves are forced. So no need to
		// check for a move that wins the game (because this method is only
		// called when there is a choice to be made).

		// First try to move pieces to SAFETY but don't ruin a prime.
		for (List<Integer> move : legalMoves)
			if (move.get(1) == NannonGameBoard.movingToSAFETY
					&& !ManageMoveEffects.breaksPrime(move.get(2)))
			{
				return move; // Take first such move found.
			}

		// Next try to create or extend a prime AND hit opponent.
		for (List<Integer> move : legalMoves)
			if (ManageMoveEffects.isaHit(move.get(2))
					&& (ManageMoveEffects.createsPrime(move.get(2))
							|| ManageMoveEffects.extendsPrime(move.get(2))))
			{
				return move;
			}

		// If nothing found yet, try to create or extend a prime.
		for (List<Integer> move : legalMoves)
			if (ManageMoveEffects.createsPrime(move.get(2))
					|| ManageMoveEffects.extendsPrime(move.get(2)))
			{
				return move;
			}

		// Next try to hit an opponent.
		for (List<Integer> move : legalMoves)
			if (ManageMoveEffects.isaHit(move.get(2)))
			{
				return move;
			}

		// Prefer moving from HOME (this won't break a prime since coming from
		// off the board).
		for (List<Integer> move : legalMoves)
			if (move.get(0) == NannonGameBoard.movingFromHOME)
			{
				return move;
			}

		// Make a move that doesn't break a prime.
		for (List<Integer> move : legalMoves)
			if (!ManageMoveEffects.breaksPrime(move.get(2)))
			{
				return move;
			}

		// Move to SAFETY if you can and no good move, even though a prime will
		// be broken.
		for (List<Integer> move : legalMoves)
			if (move.get(1) == NannonGameBoard.movingToSAFETY)
			{
				return move;
			}

		// Else make a random move.
		return Utils.chooseRandomElementFromThisList(legalMoves);
	}

	@Override
	public void updateStatistics(boolean didIwinThisGame,
			List<int[]> allBoardConfigurationsThisGameForPlayer,
			List<Integer> allCountsOfPossibleMovesForPlayer,
			List<List<Integer>> allMovesThisGameForPlayer)
	{
		; // Do no learning.
	}

	@Override
	public void reportLearnedModel()
	{
		Utils.println("\n-------------------------------------------------");
		Utils.println("\n I (" + getPlayerName()
				+ ") was born smart enough and don't try to learn.");
		Utils.println("\n-------------------------------------------------");
	}

}
