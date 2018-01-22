
import java.util.Random;

/**
 * Copyrighted 2013, 2014, 2015 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

       
   /**
    * Note: I (Jude) use Eclipse and have a wide monitor.  If these lines are too long for your preferences, it is ok to reformat (eg, via Eclipse's 'correct indentation' menu item?).
    *
    * To play against various opponents, you provide two names as arguments to this class' main().  See 'argsSpecString' for details.
    * (Or you can simply manually edit the default settings to arg1 and arg2 below.)
    *
    * Students should use the main() in this class.  After watching the default players play each other, students should
    *
    *     First copy GreedyHandCodedPlayer.java and rename as HandCodedPlayer_yourLoginName.java
    *     WHERE 'yourLoginName' is replaced with your UW Moodle login name
    *     (eg, mine would be HandCodedPlayer_jshavlik.java).
    *    
    *        Then write and experiment with your own hand-coded solution by editing what is there
    *        (lots of design choices, so every student's approach should be a little different).
    *      
    *        The point of this is to get experience with the testbed, *so don't spend much time on this*;
    *        your solutions below are the main point of this HW.  In fact, we are not even asking you to
    *        turn in this player for grading.
    *   
    *     Next copy RandomNannonPlayer.java and rename as FullJointProbTablePlayer_YourMoodleLoginName.java
    *     WHERE 'yourLoginName' is replaced with your UW Moodle login name
    *     (eg, mine would be FullJointProbTablePlayer_jshavlik.java).
    *    
    *        Then write and experiment with your own solution that uses a full joint probability table
    *        (lots of design choices, so every student's approach should be at least a little different).
    *       
    *        I recommend you create one big, multi-dimensional Java array; or one for WIN and one for LOSE.
    *        (Note that in Java all the dimensions of the multi-dimensional array need not be the same size.)
    *        
    *          Eg, if your space had five dimensions, you might declare:
    *         
    *             int[][][][][] fullState_win  = new int[#][#][#][#][#]; // Where the #'s are actual numbers (consider using NannonGameBoard.getPiecesPerPlayer()
    *                                                                    // and NannonGameBoard.getCellsOnBoard())
    *             int[][][][][] fullState_lose = new int[#][#][#][#][#]; // For readability, one might divide the full-joint table into two pieces like done here.
    *                                                                    // But then note we are doing this:  prob(fullState) = prob(fullState | win) x prob(win)
    *                                                                    // and ditto for 'lose' - ie, don't forget about the prob(win) and prob(lose).
    *           
    *        This table can use a lot of memory (as we discussed in class).  So use your judgment as to how big this array can get on your
    *        home computer.  You probably will need to tell Java to allow more RAM using these commands to the Java VM (Virtual Machine):
    *       
    *                 -Xms1g -Xmx6g    <--- if your machine does have 6 GB RAM, if not, ok to use '3g' or even '2g' instead of '6g'
    *                
    *        Tip: do NOT do this:   double ratio = fullState_win / fullState_lose because integer division will produce 0.0 if more losses than wins!
    *             Instead do this:  double ratio = fullState_win / (double) fullState_lose  (I left out the []'s for the arrays here for clarity.)
    *   
    *     Finally copy RandomNannonPlayer.java and rename as BayesNetPlayer_YourMoodleLoginName.java
    *     WHERE 'yourLoginName' is replaced with your UW Moodle login name (eg, mine would be BayesNetPlayer_jshavlik.java).
    *    
    *        Then write and experiment with your own Bayesian Network solution
    *        (lots of design choices, so every student's approach should be at least a little different).
    *        This is the player that will compete in the class-wide tournament.
    *       
    *        It is ok to use Naive Bayes here, though I recommend augmenting that with a few features that
    *        are dependent on one another, as discussed in class.
    *    
    *        THIS PLAYER SHOULD WORK FOR *ANY* OF THESE VALUES FOR NannonGameBoard.piecesPerPlayer:
    *                                            3, 4, and 5 (if 6 or more, the game can deadlock)
    *       
    *        AND      IT SHOULD WORK FOR *ANY* OF THESE VALUES FOR NannonGameBoard.cellsOnBoard:
    *                                        6, 7, 8, 9, 10, 11, and 12.
    *       
    *        THE CLASS-WIDE TOURNAMENT IS LIKELY TO INVOLVE VARIOUS SETTINGS
    *        OF THE NUMBER OF PIECES PER PLAYER AND THE NUMBER OF CELLS ON THE BOARD.
    *        YOU SHOULD ONLY WRITE *ONE* SOLUTION, BUT YOUR CODE SHOULD CALL
    *        NannonGameBoard.getPiecesPerPlayer() AND NannonGameBoard.getCellsOnBoard()
    *        WHEN PLAYER INSTANCES ARE CREATED (ie, do *NOT* use these NannonGameBoard
    *        class variables when you declare variables in BayesNetPlayer_YourMoodleLoginName.java
    *        because they might change before your 'class constructor' is created
    *        - talk to me if you don't know what this paragraph is talking about).
    *       
    *        I expect that when we run the tournament, we will use at most 1 GB RAM,
    *        since that is standard in Condor (s/w that runs Java in parallel)
    *       
    *                    -Xms500k -Xmx1g   // Let's see if this suffices; if need be, we'll increase.
    *                                      // I ran my BayesNet player against itself (in Eclipse),
    *                                      // with piecesPerPlayer = 5 and cellsOnBoard = 12,
    *                                      // and it had a peak memory of 600KB RAM according to Windows Task Manager.
    *                                      // Having two random players (which store nothing) play each other
    *                                      // uses about 500KB of RAM.
    *       
    *        NOTE: FullJointProbTablePlayer_YourMoodleLoginName.java should assume
    *              that NannonGameBoard.piecesPerPlayer = 3 and NannonGameBoard.cellsOnBoard = 6. 
    *              Larger games are likely to cause memory overflow and so we
    *              won't use them for the FullJointProbTablePlayer.
    *             
    */

public class oldPlayNannon {
       
    static String argsSpecString = 
     "\nThere are TWO input arguments: playerX_identifier  playerO_identifier\n" +
          "\n" +
          "   - legal values for these two arguments are (case does not matter EXCEPT for those involving <someLoginName>):\n" +
          "\n" +
          "       random                           Player will make random moves.\n" +
          "\n" +
          "       GUI                              Play manually (after the burn-in phase) via a GUI\n" +
          "       manual                           Play manually (after the burn-in phase) via an ASCII interface\n" +
          "\n" +
// These players are not released since it is easy to 'reverse compile' a Java class file.
//        "       jshavlik_easy                    Will play a weakly trained version of Jude's NB solution (only learns from 0.01% of games; beats random player about 59% of the time).\n" +
//        "       jshavlik_med                     Will play Jude's Naive Bayes (NB) solution (beats random player about 61% of the time).\n" +
//        "       jshavlik_smart                   Will play Jude's BayesNet solution (beats random player about 67% of the time).\n" +
//        "\n" +
          "       greedyHandCoded                  A hand-coded player that performs slightly better (63% wins against random) than jshavlik_med but not as good as jshavlik_smart.\n" +
          "\n" +
          "       <someLoginName>                  Use the player in class BayesNetPlayer_someLoginName (must be in the same directory as this file)\n" +
          "\n" +
          "       FullJointProbTablePlayer_<login> Will use the player in FullJointProbTablePlayer_login (must be in the same directory as this file)\n" +
          "\n" +
          "       HandCodedPlayer_<someLoginName>  Will use the (non-learning) player in HandCodedPlayer_someLoginName (must be in the same directory as this file)\n" +
          "\n" +         
          "   - if only ONE argument provided, the second argument defaults to random\n" +
          "\n" + 
          "   - if NO arguments provided, jshavlik_smart plays random\n" +
          "\n" + 
          "   - <someLoginName> should be your UW Moodle login name and *NOT* your CS Dept login name\n" +
          "";
       
        public static void main(String[] argsRaw) {
               
                Utils.randomInstance = new Random(123456789);  // This is the SEED for the pseudo-random number generator. 
                                                               // Use a different value and you should get different results due to randomness.

                // NOTE: Printing won't start until after the burn-in phase.  So set Nannon.numberOfGamesInBurnInPhase=0 if you want to see printing right away.
                Nannon.setPrintProgress(false); // Set to 'true' to watch the games being played.  ***** >>>>> Set to 'false' when doing long runs. <<<<< *****
                Nannon.setWaitAfterEachMove(Nannon.isPrintProgress() && false); // Replace 'false' with 'true' to pause after each move.
                Nannon.setWaitAfterEachGame(Nannon.isPrintProgress() && false); // And/or after each game.
               
                Nannon.setReportLearnedModels(true); // When done, should the players report on what they learned?  Can be useful for debugging.
                                                     // YOU SHOULD DETERMINE THE MOST IMPORTANT FEATURES (and report at least the top two - one for WIN and one for LOSE - in your project report).
               
                if (argsRaw.length > 2) {
                        Utils.error("Too many arguments provided: " + Utils.converStringListToString(argsRaw) + "\n" + argsSpecString + "\n");
                }
               
                String[] args = Utils.chopCommentFromArgs(argsRaw);
                String   arg1 = "random"; // See argsSpecString for other options.
                String   arg2 = "greedyHandCoded";
               
                if (args.length >= 1) { arg1 = args[0]; } // Override the defaults if args provided.
                if (args.length >= 2) { arg2 = args[1]; }
       
            // If you print a LOT, comment this out (Utils.java will also stop 'dribbling' if more than 10M characters are printed).
                Utils.createDribbleFile("dribbleFiles/PlayNannonMain_" + Utils.getUserName() + ".txt");
               
                int numberPostBurninGamesToPlay =   1000000;
                Nannon.setNumberOfGamesInBurnInPhase(100000); // This (e.g., 100k) is a good number for experiments, but when debugging might want to set this to 0.
                Nannon.setGamesToPlay(numberPostBurninGamesToPlay + Nannon.getNumberOfGamesInBurnInPhase()); // Maybe play 5M (or 10M) games after the burn-in if doing a long run.            

                Nannon.setUseGUItoWatch(true);       // Set this to 'true' to watch the two players after some burn-in training.
                Nannon.setPlayThisManyPostBurninGamesBeforeVisualizing(numberPostBurninGamesToPlay - 5);  // If you wish to have some post burn-in training before watching games, set this to some number of games.
                Nannon.setWaitBeforeEachMove(false);  // If true, will wait for user to click a button before progressing.
                NannonGUI.setAnimationSpeed(50);      // Set this to 1-100 to vary speed of animation; 50 seems a good value, but will be machine-dependent (if 0 or less, than no waiting, ie drawing is as fast as possible).
               
                Nannon.playGames(arg1, arg2);
        }
}
