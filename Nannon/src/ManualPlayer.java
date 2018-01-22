import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


/*
 * NOTES: recall player is only called when there is a CHOICE of moves, which is a bit odd for humans playing.
 * 
 * Because boards and moves are drawn BEFORE players are called, I need to skip the first post burn-in game since I want all the code changes to be local to this class.
 * Bad side effect: the number of wins or losses will be off by one.  But players probably wont run until the last game anyway and will instead abort.
 */

public class ManualPlayer extends NannonPlayer {

	private int     wins                      = 0;
	private int     losses                    = 0;
	private boolean burnInPhaseOver           = false;
	private boolean haveSeenGameThatIsIgnored = false;
	
	@Override
	public String getPlayerName() {
		return "Human-Powered Plain-ASCII Player";
	}
	
	public ManualPlayer() {
	}
	public ManualPlayer(NannonGameBoard gameBoard) {
		super(gameBoard);
	}
	
	private static BufferedReader inBufferedReader = null;
	@Override
	public List<Integer> chooseMove(int[] boardConfiguration,	List<List<Integer>> legalMoves) {
		int choice = -1;
		String readThis = null;

		if (!burnInPhaseOver) {	burnInPhaseOver = true;	}
		if (!haveSeenGameThatIsIgnored) return Utils.chooseRandomElementFromThisList(legalMoves);  // See comment above regarding why we need to skip the first game,
		
		Utils.print("\nYour turn #" + Utils.comma(getMoveNumber()) + " in Game #" + Utils.comma(wins + losses + 1) + ".");
		while (choice < 1 || choice > legalMoves.size()) {
			Utils.print(" Choose one of the possible moves by typing one of {1");
			for (int i = 2; i < legalMoves.size(); i++) Utils.print(", " + i);
			Utils.print(" or " + legalMoves.size());
			Utils.print("} and then hitting Enter.  ");
			try {
	        	if (inBufferedReader == null) { inBufferedReader = new BufferedReader(new InputStreamReader(System.in)); }
	        	readThis = inBufferedReader.readLine();
	        	if (readThis != null) {
	        		choice = Integer.parseInt(readThis);
	        	}
	        	if (choice < 1 || choice > legalMoves.size()) { Utils.println("  Your choice of '" + choice + "' is invalid.  Please try again."); }
	        } catch (Exception e) {
	        	Utils.println("  Your entered something other than an integer.  Please try again.");
	        	inBufferedReader = null;  // If something went wrong, reset the reader. 
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
		
		if (!burnInPhaseOver || !haveSeenGameThatIsIgnored) {
			if (burnInPhaseOver) {
				haveSeenGameThatIsIgnored = true;
				gameBoard.printProgress = true;
			}
			return;
		}
		
		// No machine learning.
		if (didIwinThisGame) { wins++; } else { losses++; }
		Utils.println("\n***** You " + (didIwinThisGame ? "won" : "lost") + " this game.  Overall, " + Utils.comma(wins) + " wins and " + Utils.comma(losses) + " losses (" 
					  + Utils.truncate(100.0 * wins / (wins + losses), 2) + "% wins).  Hit Enter to play another game (or type 'q' and Enter to quit).");
		
		try {
			String readThis = inBufferedReader.readLine();
    		if ("q".equals(readThis)) { Utils.println("\nThanks for playing!\n"); System.exit(0); }
		} catch (IOException e) {
			// Just continue regardless of what happened.
		}
		
	}

	@Override
	public void reportLearnedModel() {
		Utils.println("\nIt's all in your head. Not mine (" + getPlayerName() + ").");

	}

}
