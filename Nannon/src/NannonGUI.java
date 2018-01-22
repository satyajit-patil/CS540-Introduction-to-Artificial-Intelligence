//          Taken from the AgentWorld (another cs540 testbed) and modified for Nannon.  So probably some excess code in here and maybe some odd variable names.
//          One location of the original files: ~shavlik/AgentWorld/OldCS760Java

// Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.

//  Nannon is copyrighted (2004) by Jordan Pollack.  He has given permission for use by Jude Shavlik in his U-Wisconsin courses.

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class NannonGUI extends Frame implements ActionListener, ItemListener
{         /**
	 * 
	 */
  private static final long    serialVersionUID = 1L;
  static final boolean         masterDebugging    = true,
                               reportSynchs       = false; // Report monitor state in the score board?
  private static final boolean debuggingThisClass = false;

  private Color        foregndColor, backgndColor;
  private Button       quit, playAgain, help;
          Button       pause, display;
          PlayingField playingField;
  private boolean      singleStepping = false, started = false, debugging = false;
          Panel        topHolder, top, bottom, buttonBar, buttonBar2, buttonBarHolder;
          Label        winsForRedLabel, winsForWhiteLabel, gameCounter;
  private Label        infoBar;
  static  Label        developerLabel1, developerLabel2; // Report debugging messages.

  static int round(double x) { return (int) Math.round(x); }
  
               Font tinyFont        = null,
                    tinyBoldFont    = null,
                    smallFont       = null,
                    smallBoldFont   = null,
                    buttonFont      = null,
                    helpFont        = null,
                    regularFont     = null,
                    regularBoldFont = null,
                    bigFont         = null,
                    bigBoldFont     = null,
                    largeFont       = null,
                    largeBoldFont   = null;

  private CheckboxMenuItem    reportSlowPlayersCheckBox;
  private static final String reportSlowPlayersString   = "Report players slow to provide their chosen move.";
  private boolean             reportSlowPlayersValue    = false;
  
  private CheckboxMenuItem    onlySeeViewOfPlayerCheckBox;
  private static final String onlySeeViewOfPlayerString = "Only see view seen by the selected player.";
  private boolean             onlySeeViewOfPlayerValue  = false;

  private CheckboxMenuItem    toggleShowingSensorsOfPlayerCheckBox;
  private static final String toggleShowingSensorsOfPlayerString = "Toggle showing sensors of selected player.";
  private boolean             toggleShowingSensorsOfPlayerValue  = false;

  private CheckboxMenuItem    circleAllObjectsCheckBox;
  private static final String circleAllObjectsString    = "Show true shape of all objects.";
  private boolean             circleAllObjectsValue     = false;
  
  private              Menu   menu, helpMenu;
  
  int moveNumber          = 0;
  int gameNumber          = 0;
  double winPercentage    = 0.0;
  
  int whoseTurnToPlay     = NannonGameBoard.empty;
  int homePieces_playerX  = 1;
  int homePieces_playerO  = 1;
  int safePieces_playerX  = 0;
  int safePieces_playerO  = 0;
  int die_playerX         = 3;
  int die_playerO         = 3;
  
  int[]     atCell         = null;
  boolean[] moveable       = null;
  boolean[] reachable      = null;
  String    opponentsName  = "Who Knows?";
  boolean   waitingForUser = true;
  int       chosenFrom     = -1;
  int       chosenTo       = -1;
  boolean   showNextMove    = true;
  boolean   playAnotherGame = false;
  
  private static Boolean  humanIsPlayerX = null;
  
  boolean  currentPlayerIsX          = true;
  boolean  playingAgainstHumanPlayer = false;
  
  private String nameOfPlayerX = null;
  private String nameOfPlayerO = null;

  private static final String helpString      = "Help";

          static       String reportWinsForRED   = "  Wins for RED:  ";
          static       String reportWinsForWHITE = " Wins for WHITE: ";
          
  private static       String singleStepLabel = " Play another game? ";
          static final String continuousLabel = "    Run Continuously Instead  ";
  private static final String pauseLabel      = "  Pause ";
  private static final String resumeLabel     = " Resume ";
          static final String goLabel         = "   GO   ";
          static final String displayOnLabel  = " Turn Graphics OFF ";
          static final String displayOffLabel = "  Turn Graphics ON  ";
  private static final String quitLabel       = " Quit ";
  private static final String helpLabel       = " Help ";
  
  private static boolean printedInfoToConsole = false; // Used to make sure some info is only printed ONCE.

  // The constructor(s).
  // Specify the width and height of PLAYING FIELD (want this to be constant independent of the number of player scores reported, etc).
  // Use humanIsPlayerX = null to say "observing rather than playing."
  public NannonGUI(int numbCells, int numbPieces, Boolean humanIsPlayerX, boolean includeThePlayAgainButton, String opponentsName)
  {
    super("Nannon");
    
    playingField  = new PlayingField(this, numbCells, numbPieces);
    
    if (humanIsPlayerX == null) {
    	playingAgainstHumanPlayer = false;
    } else {
    	currentPlayerIsX = humanIsPlayerX;
    }
    singleStepLabel = (playingAgainstHumanPlayer ? " Play another game? " : " Click to See Chosen Move ");
    
    
    atCell    = new int[    numbCells]; 
    moveable  = new boolean[numbCells + 1]; 
    reachable = new boolean[numbCells + 1];
    resetToStartingState(true);
    
    NannonGUI.setHumanIsPlayerX(humanIsPlayerX);
    this.opponentsName  = opponentsName;

    menu = new Menu("Options");
    menu.addActionListener(this);

    reportSlowPlayersCheckBox = new CheckboxMenuItem(reportSlowPlayersString);
    reportSlowPlayersCheckBox.setState(reportSlowPlayersValue);
    reportSlowPlayersCheckBox.addItemListener(this);
//   menu.add(reportSlowPlayersCheckBox);

    onlySeeViewOfPlayerCheckBox  = new CheckboxMenuItem(onlySeeViewOfPlayerString);
    onlySeeViewOfPlayerCheckBox.setState(onlySeeViewOfPlayerValue);
    onlySeeViewOfPlayerCheckBox.addItemListener(this);
//    menu.add(onlySeeViewOfPlayerCheckBox);

    toggleShowingSensorsOfPlayerCheckBox  = new CheckboxMenuItem(toggleShowingSensorsOfPlayerString);
    toggleShowingSensorsOfPlayerCheckBox.setState(toggleShowingSensorsOfPlayerValue);
    toggleShowingSensorsOfPlayerCheckBox.addItemListener(this);
//    menu.add(toggleShowingSensorsOfPlayerCheckBox);

    circleAllObjectsCheckBox  = new CheckboxMenuItem(circleAllObjectsString);
    circleAllObjectsCheckBox.setState(circleAllObjectsValue);
    circleAllObjectsCheckBox.addItemListener(this);
//    menu.add(circleAllObjectsCheckBox);

    helpMenu = new Menu("Help");
    helpMenu.addActionListener(this);
    helpMenu.add(helpString);
    
    MenuBar mainMenu = new MenuBar();
//  mainMenu.add(menu);
    mainMenu.add(helpMenu);
    mainMenu.setHelpMenu(helpMenu);
//  setMenuBar(mainMenu);

    topHolder     = new Panel();
    top           = new Panel();
    gameCounter   = new Label(" _________  _________ _________ _________ _________ _________ ");
    Label left    = new Label(" ");
    Label right   = new Label(" ");
    bottom        = new Panel();
    buttonBar     = new Panel();
    buttonBar2    = new Panel();
    buttonBarHolder = new Panel();
    infoBar       = new Label(" ", Label.CENTER);
    winsForRedLabel   = new Label(humanIsPlayerX == null ? " _________  _________ _________ " : reportWinsForRED); // Get enough space allocated for these buttons.
    winsForWhiteLabel = new Label(humanIsPlayerX == null ? " _________  _________ _________ " : reportWinsForWHITE);
    playAgain     = new Button(singleStepLabel);
    pause         = new Button(pauseLabel);
    display       = new Button(displayOffLabel);
    quit          = new Button(quitLabel);
    help          = new Button(helpLabel);
    
	winsForRedLabel.setAlignment(  Label.RIGHT);
	winsForWhiteLabel.setAlignment(Label.RIGHT);
	
    resetFonts();    

    foregndColor = Color.darkGray;
    backgndColor = Color.lightGray;
    setForeground(foregndColor);
    setBackground(backgndColor);

    winsForRedLabel.setForeground(  Color.RED);   winsForRedLabel.setMinimumSize(  new Dimension((int) (10 * playingField.unit), (int) (15 * playingField.unit)));
    winsForWhiteLabel.setForeground(Color.WHITE); winsForWhiteLabel.setMinimumSize(new Dimension((int) (10 * playingField.unit), (int) (15 * playingField.unit)));
    
    top.setBackground(Color.BLUE);
    
//   gameCounter.setMinimumSize(new Dimension(humanIsPlayerX != null ? 750 : 1000, 15));  // Has no effect?
    
    
//    winsForRedLabel.addActionListener(this);
//    winsforWhiteLabel.addActionListener(this);
    playAgain.addActionListener(this);
    pause.addActionListener(this);
    display.addActionListener(this);
    help.addActionListener(this);
    quit.addActionListener(this);

    // See http://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html

    setLayout(new BorderLayout(5, 0));
    topHolder.setLayout(new BorderLayout(0, 0)); 
//  top.setLayout(      new GridLayout(  0, 4)); // Report players' scores four-across.
    top.setLayout(      new GridLayout(  0, 1));
    bottom.setLayout(   new BorderLayout(15, 15)); // Space between infoBar and buttonBar.
    buttonBar.setLayout(new GridLayout(2, 1));
    buttonBar2.setLayout(new FlowLayout());//(new GridLayout(1, 0));
    buttonBarHolder.setLayout(new BorderLayout(2, 1));

    //Label topSpacerN = new Label("");
    //Label topSpacerE = new Label("");
    //Label topSpacerW = new Label("");
    Label topSpacerS = new Label("");
    //topHolder.add("North",  topSpacerN);
    //topHolder.add("East",   topSpacerE);
    //topHolder.add("West",   topSpacerW);
    topHolder.add("South",  topSpacerS);
    topHolder.add("Center", top);

    bottom.add("North",  infoBar);
    bottom.add("Center", buttonBarHolder);    

    top.add(gameCounter); // Put in upper left.
    if (reportSynchs)  // Have some labels for reporting internal state (for debugging purposes).
    {
      developerLabel1 = new Label("");
      developerLabel2 = new Label("");
      developerLabel1.setFont(tinyFont);
      developerLabel2.setFont(tinyFont);
      top.add(developerLabel1);
      top.add(developerLabel2);
    }

    buttonBar.add(winsForWhiteLabel);  // buttonBar.add(new Label(""));
    buttonBar.add(winsForRedLabel);    // buttonBar.add(new Label(""));

    Label spacerA1 = new Label("");
    Label spacerA2 = new Label("");   spacerA2.setFont(tinyFont);  spacerA2.setMaximumSize(new Dimension(1, 1));
    Label spacerA3 = new Label("");   spacerA3.setFont(tinyFont);  spacerA3.setMaximumSize(new Dimension(1, 1));
    Label spacerB  = new Label("");   spacerB.setFont( tinyFont);  spacerB.setMaximumSize( new Dimension(1, 1));
    Label spacerC1 = new Label("");   spacerC1.setFont(tinyFont);  spacerC1.setMaximumSize(new Dimension(1, 1));  
    Label spacerC2 = new Label("");   spacerC2.setFont(tinyFont);  spacerC2.setMaximumSize(new Dimension(1, 1)); 
    Label spacerC3 = new Label("");    
    
    if (includeThePlayAgainButton)  { 
        buttonBar2.add(spacerA1);
    	buttonBar2.add(playAgain);
        buttonBar2.add(spacerA2);
        buttonBar2.add(spacerA3);
    	buttonBar2.add(quit); 
        buttonBar2.add(spacerB);
        buttonBar2.add(help);
    	buttonBar2.add(spacerC3);
    } else { 
        buttonBar2.add(spacerA1);
        buttonBar2.add(quit);
    	buttonBar2.add(spacerB);
    	buttonBar2.add(help);
    	buttonBar2.add(spacerC3);
    }
//  buttonBar.add(pause);
//  buttonBar.add(spacerB);
//  buttonBar.add(display);

    pause.setEnabled(false); // Wait until started.
    
    buttonBarHolder.add("North", buttonBar);
    buttonBarHolder.add("South", buttonBar2);

    add("North",  topHolder);
    add("West",   left);
    add("Center", playingField);
    add("East",   right);
    add("South",  bottom);
    
//    if (displayOn) setDisplayON(); else setDisplayOFF();

    debugging = (debuggingThisClass && NannonGUI.masterDebugging);

	playAgain.setEnabled(includeThePlayAgainButton);

//    int size = 10 * round(PlayingField.unitOrig) + 15;
//   setBounds(size / 5, size / 5, size, size);

	setResizable(true);
    pack();
	validate();
    addNotify();    
	setVisible(true);
    
	if (!printedInfoToConsole) {
		printedInfoToConsole = true;
		Utils.println();
		Utils.println("% This is the console of the Nannon GUI.");
		Utils.println("% (Do a control-C here if the program appears to be hanging.)");
		Utils.println();
	}
    
    playingField.repaint();
    playingField.setConfigured(true); // From now on, should resize when messages received (too many odd things happening during startup otherwise).
  }
  
  void resetFonts() {
	 if (playingField == null) return; // Ignore early requests.
	 
	 double delta = 1.0;
	  
	 tinyFont        = new Font("TimesRoman", Font.PLAIN, round(5 + playingField.unit * delta * 1.0));
     tinyBoldFont    = new Font("TimesRoman", Font.BOLD,  round(5 + playingField.unit * delta * 1.0));
	 smallFont       = new Font("TimesRoman", Font.PLAIN, round(5 + playingField.unit * delta * 1.5));
	 smallBoldFont   = new Font("TimesRoman", Font.BOLD,  round(5 + playingField.unit * delta * 1.5));
	 buttonFont      = new Font("TimesRoman", Font.PLAIN, round(5 + playingField.unit * delta * 2.0));
	 helpFont        = new Font("TimesRoman", Font.BOLD,  round(5 + playingField.unit * delta * 2.0));
	 regularFont     = new Font("TimesRoman", Font.PLAIN, round(5 + playingField.unit * delta * 2.5));
	 regularBoldFont = new Font("TimesRoman", Font.BOLD,  round(5 + playingField.unit * delta * 2.5));
	 bigFont         = new Font("TimesRoman", Font.PLAIN, round(5 + playingField.unit * delta * 3.0));
	 bigBoldFont     = new Font("TimesRoman", Font.BOLD,  round(5 + playingField.unit * delta * 3.0));
	 largeFont       = new Font("TimesRoman", Font.PLAIN, round(5 + playingField.unit * delta * 3.5));
	 largeBoldFont   = new Font("TimesRoman", Font.BOLD,  round(5 + playingField.unit * delta * 3.5));

	 setFont(regularFont);
	 
	 infoBar.setFont(helpFont);
	 playAgain.setFont(buttonFont);
	 pause.setFont(buttonFont);
	 display.setFont(buttonFont);
	 quit.setFont(buttonFont);
	 help.setFont(buttonFont);

	 helpMenu.setFont(smallFont);
	 menu.setFont(smallFont);
	 reportSlowPlayersCheckBox.setFont(smallFont);
	 onlySeeViewOfPlayerCheckBox.setFont(smallFont);
	 circleAllObjectsCheckBox.setFont(smallFont);
	 toggleShowingSensorsOfPlayerCheckBox.setFont(smallFont);
	 
	 if (getHumanIsPlayerX() == null) {
	   	gameCounter.setForeground(Color.PINK); 
	   	gameCounter.setFont(      largeBoldFont);
	    winsForRedLabel.setFont(  largeBoldFont);
		winsForWhiteLabel.setFont(largeBoldFont);
		
	 } else {
	 	gameCounter.setForeground(getHumanIsPlayerX() ? Color.RED     : Color.WHITE); 
	   	gameCounter.setFont(      regularBoldFont);
	    winsForRedLabel.setFont(  largeBoldFont);
	    winsForWhiteLabel.setFont(largeBoldFont);
	 }
	 setFont(regularFont);
		 
//	 pack();
  }
 
  // These tests are probably redundant.
  public void setSize(Dimension d)
  { 
    if (isResizable()) super.setSize(d);
    Utils.println("NannonGUI:  setSize: d = " + d);
  }
  public void setSize(int width, int height)
  {
    if (isResizable()) super.setSize(width, height);
    Utils.println("NannonGUI:  setSize: width = " + width + ", height = " + height);
  }
  public void setBounds(int x, int y, int width, int height)
  { // int oldWidth  = getWidth();   double scaleW = oldWidth  / (double) width;
    // int oldHeight = getHeight();  double scaleH = oldHeight / (double) height;

    if (isResizable()) super.setBounds(x, y, width, height);
    Utils.println("NannonGUI:  setBounds: width = " + width + ", height = " + height);
  }

  public void setClockPeriod(int msecs)
  {
    playingField.setClockPeriod(msecs);
  }

  public void reportInInfoBar(String message)
  {
    infoBar.setText(message);
  }

  private void provideHelp()
  {
    playingField.showHelp();
  }
  
  void reportGameCounter(int count) {
	  if (getHumanIsPlayerX() == null) {
		  gameCounter.setText("  Playing Game #" + Utils.comma(count) + (opponentsName == null ? "" : " Against " + opponentsName) + "                           ");
		  return;
	  }
	  gameCounter.setText("  Playing Game #" + Utils.comma(count) + " as the " + (getHumanIsPlayerX() ? "RED" : "WHITE") + (opponentsName == null ? " Pieces" : " Pieces Against " + opponentsName) + "                           ");
  }
  
  void reportMoveAndGameCounters(int countMoves, int countGames) {
	  if (getHumanIsPlayerX() == null) {
		  gameCounter.setText("  Playing Move #" + Utils.comma(countMoves) + " in Game #" + Utils.comma(countGames) + (opponentsName == null ? "" : " Against " + opponentsName) + "                           ");
		  return;
	  }
	  gameCounter.setText("  Playing Move #" + Utils.comma(countMoves) + " in Game #" + Utils.comma(countGames) + 
			  			  " as " + (getHumanIsPlayerX() ? "RED" : "WHITE") + (opponentsName == null ? " " : " Against " + opponentsName) + "  ");
  }
  
  void reportMoveAndGameAndWinPercentageCounter(int countMoves, int countGames, double winningPercentage) {
	  if (getHumanIsPlayerX() == null || countGames < 2) { reportMoveAndGameCounters(countMoves, countGames); return; }

	  gameCounter.setText("  Playing Move #" + Utils.comma(countMoves) + " in Game #" + Utils.comma(countGames) 
			  			    + " (won " + Utils.truncate(winningPercentage, 1) + "%)"
			  				+ " as " + (getHumanIsPlayerX() ? "RED" : "WHITE") + (opponentsName == null ? " " : " Against " + opponentsName) 
			  				+ "  ");
  }
  
  private int max_chunksOfGamesPlayed = 100000;
  double fractionWonByX[] = new double[max_chunksOfGamesPlayed + 1]; // These will round to nearest percentage point.
  double fractionWonByO[] = new double[max_chunksOfGamesPlayed + 1];
  int    lastCellFilled   =   0; // Set to a negative number if you don't want the graph to be drawn before any results are in.
  double maxFraction      =   0;
  double minFraction      = 100;
  static int reportingPeriodForGames = 1000;
  void recordWinningPercentageDuringSilentRunning(int winsForX, int winsForO, int gamesPlayed) { // Don't count burn-in since that is random walkinhg.
	  if (gamesPlayed < 0) return;
	  int    gamesChunksPlayed = Math.min(max_chunksOfGamesPlayed, Math.max(0, (gamesPlayed / reportingPeriodForGames) - 1)); // Subtract 1 so the 0 cell is based on a reasonable number of games (other wise will vary too much).
	  double Xpercentage       = winsForX / (winsForX + winsForO + 0.0000001); // Cheap trick to avoid dividing by zero or by an int. 
	  double Opercentage       = winsForO / (winsForX + winsForO + 0.0000001);
	  
	  if (lastCellFilled < gamesChunksPlayed) lastCellFilled = gamesChunksPlayed;
	  if (Xpercentage    < minFraction)      minFraction    = Xpercentage;
	  if (Opercentage    < minFraction)      minFraction    = Opercentage;
	  if (Xpercentage    > maxFraction)      maxFraction    = Xpercentage;
	  if (Opercentage    > maxFraction)      maxFraction    = Opercentage;
	  fractionWonByX[gamesChunksPlayed] = Xpercentage; 
	  fractionWonByO[gamesChunksPlayed] = Opercentage; 
  }
  
  
  void setWins(int forX, int forO) {
	  if (getHumanIsPlayerX() == null) {
		  if (nameOfPlayerO == null) {
			  winsForRedLabel.setText(  "Wins for RED: "   + Utils.comma(forX) + "       ");
			  winsForWhiteLabel.setText("Wins for WHITE: " + Utils.comma(forO) + "       ");
			  return;
		  }
		  winsForRedLabel.setText(  "Wins for " + nameOfPlayerX + " = " + Utils.comma(forX) + "       ");
		  winsForWhiteLabel.setText("Wins for " + nameOfPlayerO + " = " + Utils.comma(forO) + "       ");
	  } else {
		  if (getHumanIsPlayerX() == Boolean.TRUE) {
			  winsForRedLabel.setText(  "Wins for " + Utils.getUserName() + " playing manually = " + Utils.comma(forX) + "       ");
			  winsForWhiteLabel.setText("Wins for " + nameOfPlayerO      + " = " + Utils.comma(forO) + "       ");
		  } else {
			  winsForRedLabel.setText(  "Wins for " + nameOfPlayerX      + " = " + Utils.comma(forX) + "       ");
			  winsForWhiteLabel.setText("Wins for " + Utils.getUserName() + " playing manually = " + Utils.comma(forO) + "       ");
		  }
	  }
	  playingField.repaint();
  }

  public void setSingleStepMode(boolean value)
  {
    if (!started)
    {
      if (value) gameCounter.setText(" Press " + goLabel.trim() + " to start ...");
    }
    if (singleStepping != value)
    {
      if (value) singleSteppingON(); else singleSteppingOFF();
    }
  }

  private void singleSteppingON()
  {
    setDisplayON();
    singleStepping = true;
    playAgain.setLabel(continuousLabel);
    pause.setLabel(goLabel);
    if (playingField.paused) pause.setEnabled(true); // Need to resume/go.
    else pause.setEnabled(false);
    playingField.useSingleStepMode(true);
    reportInInfoBar("Single-stepping has been turned ON.  Press "
                    + goLabel.trim() + " to continue.");
  }

  private void singleSteppingOFF()
  {
    singleStepping = false;
    playAgain.setLabel(singleStepLabel);
    if (playingField.paused) pause.setLabel(resumeLabel);
    else pause.setLabel(pauseLabel);
    playingField.useSingleStepMode(false);
    reportInInfoBar("Single-stepping has been turned OFF.");
  }
  
  void setDisplayON()
  {
    display.setLabel(displayOnLabel);
    playingField.setDisplayOn(true);
    playingField.repaint();
    reportInInfoBar("");
  }

  void setDisplayOFF()
  {
    display.setLabel(displayOffLabel);
    playingField.setDisplayOn(false);
    playingField.repaint();
    reportInInfoBar("Clicking in the playing field will also resume the graphics.");
  }
  
  // Function called when checkbox is modified on the options menu.
  public void itemStateChanged(ItemEvent event)
  { //Object target = event.getItem();
    //String label  = target.toString();
  }

  public void actionPerformed(ActionEvent event)
  { Object target = event.getSource();
    //String label  = event.getActionCommand();

    if      (target == quit)
    {
      System.exit(1); // to do: should confirm
    }
    else if (target == pause)
    { String label = pause.getLabel();

      if (label.equals(pauseLabel))
      {
        playingField.pause();
        pause.setLabel(resumeLabel);
      }
      else if (label.equals(resumeLabel))
      {
        pause.setLabel(pauseLabel);
        playingField.resume();
      }
      else
      { // This button is used during single-stepping as well.
        pause.setEnabled(false);
        playingField.resume();
      }
    }
    else if (target == playAgain)
    { 
    	if (playAgain.isEnabled()) {
    		if (playingAgainstHumanPlayer) {
    			playAgain.setEnabled(false);  
    			playAnotherGame = true;
    		} else {
    			showNextMove = true; 
    		}
    	}
    } /*
    else if (target == singleStep)
    { String label = singleStep.getLabel();

      if (label.equals(singleStepLabel)) singleSteppingON();
      else                               singleSteppingOFF();
    }*/
    else if (target == display)
    { String label = display.getLabel();

      if (label.equals(displayOnLabel)) setDisplayOFF();
      else                              setDisplayON();
    }
    else if (target == help)
    {      
      provideHelp();
    }
    else if (target == circleAllObjectsCheckBox)
    {
      setCircleAllObjects(!circleAllObjectsValue);
    }
    else if (target instanceof MenuItem)
    { String label = ((MenuItem)target).getLabel(); 

      if (label.equals(helpString))
      {
        provideHelp();
      }
    }
  }

  void setCircleAllObjects(boolean value)
  {
    circleAllObjectsCheckBox.setState(value);
    circleAllObjectsValue = value;
    playingField.setCircleAllObjects(value);
  }
  
  public void resetToStartingState(boolean firstTime) {
		whoseTurnToPlay    = NannonGameBoard.empty;
		
		homePieces_playerX = NannonGameBoard.getPiecesPerPlayer() - 2;
		homePieces_playerO = NannonGameBoard.getPiecesPerPlayer() - 2;
		safePieces_playerX = 0;
		safePieces_playerO = 0;
		die_playerX        = 3;
		die_playerO        = 3;		

		for (int i = 0; i <  NannonGameBoard.getCellsOnBoard(); i++) { atCell[i] = NannonGameBoard.empty; }		
		for (int i = 0; i <= NannonGameBoard.getCellsOnBoard(); i++) { moveable[i] = false;  reachable[i] = false; }
		atCell[0]                                = NannonGameBoard.playerX;
		atCell[1]                                = NannonGameBoard.playerX;
		atCell[NannonGameBoard.getCellsOnBoard() - 2] = NannonGameBoard.playerO;
		atCell[NannonGameBoard.getCellsOnBoard() - 1] = NannonGameBoard.playerO;
		
	    if (!firstTime) playingField.repaint();	  
  }
  
  public void drawBoard(int[] currentBoardConfig, List<List<Integer>> legalMoves, int moveNumber, int gameNumber, double winningPercentage) {
	  
	  	this.moveNumber    = moveNumber;
	  	this.gameNumber    = gameNumber;
	  	this.winPercentage = winningPercentage;
	  	reportMoveAndGameAndWinPercentageCounter(moveNumber, gameNumber, winningPercentage);
		whoseTurnToPlay    = currentBoardConfig[0]; // Need to get this from the current board, since this might be a REPLAY of a game (i.e., during training).
				
		homePieces_playerX = currentBoardConfig[1];
		homePieces_playerO = currentBoardConfig[2];
		safePieces_playerX = currentBoardConfig[3];
		safePieces_playerO = currentBoardConfig[4];
		die_playerX        = currentBoardConfig[5];
		die_playerO        = currentBoardConfig[6];		

		for (int i = 0; i <  NannonGameBoard.getCellsOnBoard(); i++) { atCell[i] = currentBoardConfig[7 + i]; }
		
		for (int i = 0; i <= NannonGameBoard.getCellsOnBoard(); i++) { moveable[i] = false;  reachable[i] = false; }
		if (legalMoves != null) for (List<Integer> move : legalMoves) if (move.get(0) == NannonGameBoard.movingFromHOME) { moveable[ NannonGameBoard.getCellsOnBoard()] = true; } else { moveable[ move.get(0) - 1] = true; }
		if (legalMoves != null) for (List<Integer> move : legalMoves) if (move.get(1) == NannonGameBoard.movingToSAFETY) { reachable[NannonGameBoard.getCellsOnBoard()] = true; } else { reachable[move.get(1) - 1] = true; }

		chosenFrom      = -1;
		chosenTo        = -1;
		pieceBeingMoved = -1;
		waitingForUser  = true;
	    playingField.repaint();
	    
//	    Utils.println("\nmoveNumber  = " + moveNumber);
//	    Utils.println(  "gameNumber  = " + gameNumber);
//	    Utils.println(  "die_playerX = " + die_playerX);
//	    Utils.println(  "die_playerO = " + die_playerO);
//	    Utils.println(  "whose turn  = " + (whoseTurnToPlay == 1 ? "X" : "O"));
//	    Utils.println(  "homePieces_playerX = " + homePieces_playerX);
//	    Utils.println(  "homePieces_playerO = " + homePieces_playerO);
//	    Utils.println(  "safePieces_playerX = " + safePieces_playerX);
//	    Utils.println(  "safePieces_playerO = " + safePieces_playerO);
//	    if (legalMoves != null) for (List<Integer> move : legalMoves) Utils.println("  possible move: " + move.get(0) + " -> " + move.get(1));
	    
  }

  int pieceBeingMoved = -1, targetOfMove = -1;
  double moveFraction = 0;
  
  private static int sleepKnob = 50;  // Non-positive numbers mean 'draw as fast as possible'.
  public static void setAnimationSpeed(int speedSetting) {
	  sleepKnob = 100 - speedSetting;
	  if (speedSetting > 100)  sleepKnob = 100;
	  if (speedSetting <   1)  sleepKnob =   1;
	  
  }

  public void animateMove(int[] boardConfiguration, int from, int to) {
	  
		for (int i = 0; i <= NannonGameBoard.getCellsOnBoard(); i++) { moveable[i] = false;  reachable[i] = false; }  
		if (from == NannonGameBoard.movingFromHOME) { pieceBeingMoved = NannonGameBoard.getCellsOnBoard(); } else { pieceBeingMoved = from - 1; }  
		if (to   == NannonGameBoard.movingToSAFETY) { targetOfMove    = NannonGameBoard.getCellsOnBoard(); } else { targetOfMove    = to   - 1; }
		playingField.repaint();
		
		boolean veryShortOffFieldMove = false;
		boolean shortOffFieldMove     = false;
		int middle  = NannonGameBoard.getCellsOnBoard() / 2;
		int sixth   = NannonGameBoard.getCellsOnBoard() / 6;
		if (pieceBeingMoved == NannonGameBoard.getCellsOnBoard()) {
			veryShortOffFieldMove = (currentPlayerIsX ? targetOfMove < 3      : targetOfMove > NannonGameBoard.getCellsOnBoard() - 3);
			shortOffFieldMove     = (currentPlayerIsX ? targetOfMove < middle : targetOfMove > middle);
		}
		if (targetOfMove    == NannonGameBoard.getCellsOnBoard()) {
			veryShortOffFieldMove = (currentPlayerIsX ? pieceBeingMoved > 3      : pieceBeingMoved > NannonGameBoard.getCellsOnBoard() - 3);
			shortOffFieldMove     = (currentPlayerIsX ? pieceBeingMoved > middle : pieceBeingMoved < middle);
		}
		
	  	int sleepSpeedScale = 2;
		if (playAgain.isEnabled() && !playingAgainstHumanPlayer) {
			showNextMove    = false;
			sleepSpeedScale = 3; // (sleepSpeedScale - 1) needs to be a divisor of 50 or the graphics will look choppy.
//			Utils.println("waiting");
			while (!showNextMove)  { try { Thread.sleep(100); } catch (InterruptedException e) {	} }
		} else {
			sleepSpeedScale = 2; // Need this to be positive when we subtract 1.
			if (sleepKnob > 0) try { Thread.sleep(25 * sleepKnob / sleepSpeedScale);	} catch (InterruptedException e) {	}
		}
		
		int sleepSpeedScaleForLoop = sleepSpeedScale;
		if (from != NannonGameBoard.movingFromHOME && (to != NannonGameBoard.movingToSAFETY)) {
			switch ((int) Math.abs(pieceBeingMoved - targetOfMove)) {
			case 1: sleepSpeedScaleForLoop =  6 - sixth;   break; //  Speed up short moves.
			case 2: sleepSpeedScaleForLoop =  5 - sixth;   break; //  Speed up short moves.
			case 3: sleepSpeedScaleForLoop =  4 - sixth/2; break; //  Speed up short moves.
			case 4: sleepSpeedScaleForLoop =  3;           break; //  Speed up short moves.
			}
		} else if (veryShortOffFieldMove) {
			 sleepSpeedScaleForLoop =  5;
		} else if (shortOffFieldMove) {
			 sleepSpeedScaleForLoop =  4;
		}
		int offset = 100 % (2 * (sleepSpeedScaleForLoop - 1));
		for (int moveStep = offset; moveStep <= 100; moveStep +=  2 * (sleepSpeedScaleForLoop - 1)) {
			moveFraction = moveStep / 100.0;
			playingField.repaint();
			if (sleepKnob > 0) try { Thread.sleep(sleepKnob); } catch (InterruptedException e) {	}
		}
		if (sleepKnob > 0) try { Thread.sleep(50 * sleepKnob / sleepSpeedScale); } catch (InterruptedException e) {	}
		pieceBeingMoved = -1;  targetOfMove = -1;  moveFraction = 0;
  }

  public boolean stillWaitingForUser() {
	  return waitingForUser;
  }
  public int getMoveFrom() {
	  if (chosenFrom == NannonGameBoard.getCellsOnBoard()) return NannonGameBoard.movingFromHOME;
	  return chosenFrom + 1; // Convert to one-based counting.
  }
  public int getMoveTo() {
	  if (chosenTo == NannonGameBoard.getCellsOnBoard()) return NannonGameBoard.movingToSAFETY;
	  return chosenTo + 1;
  }

  public void seeIfUserWantsToPlayAnotherGame() {
	  playAnotherGame = false;
	  playAgain.setEnabled(true);
  }
  public boolean readyToPlayAgain() {
		return playAnotherGame;
  }

  public void setPlayersNames(String playerX_Name, String playerO_Name) {
	  nameOfPlayerX = playerX_Name;
	  nameOfPlayerO = playerO_Name;	
  }

  private static int numberOfGamesInBurnInPhase, playThisManyPostBurninGamesBeforeVisualizing;
  public void setBurnin(int numberOfGamesInBurnInPhase, int playThisManyPostBurninGamesBeforeVisualizing) {
	  this.setNumberOfGamesInBurnInPhase(numberOfGamesInBurnInPhase);
	  this.setPlayThisManyPostBurninGamesBeforeVisualizing(playThisManyPostBurninGamesBeforeVisualizing);
	  
	  gameCounter.setText(" Waiting for " + Utils.comma(numberOfGamesInBurnInPhase / 1000) + "K burn-in games to complete. " 
			  				+ (playThisManyPostBurninGamesBeforeVisualizing > 0 ? "There will be " + Utils.comma(playThisManyPostBurninGamesBeforeVisualizing / 1000) + "K post burn-in games before visualization. " : ""));
	
  }

 public void burninPhaseOver(int playThisManyPostBurninGamesBeforeVisualizing) {
	 gameCounter.setText(" Burn-in phase over. " + (playThisManyPostBurninGamesBeforeVisualizing > 0 ? "There will be " + Utils.comma(playThisManyPostBurninGamesBeforeVisualizing / 1000) + "K post burn-in games before visualization. " : ""));
 }

 
// Some accessors.
 
 public void setPlayThisManyPostBurninGamesBeforeVisualizing(int playThisManyPostBurninGamesBeforeVisualizing) {
	 NannonGUI.playThisManyPostBurninGamesBeforeVisualizing = playThisManyPostBurninGamesBeforeVisualizing;
 }

 public int getPlayThisManyPostBurninGamesBeforeVisualizing() {
	 return playThisManyPostBurninGamesBeforeVisualizing;
 }

 public void setNumberOfGamesInBurnInPhase(int numberOfGamesInBurnInPhase) {
	 NannonGUI.numberOfGamesInBurnInPhase = numberOfGamesInBurnInPhase;
 }

 public int getNumberOfGamesInBurnInPhase() {
	 return numberOfGamesInBurnInPhase;
 }

 public static void setHumanIsPlayerX(Boolean humanIsPlayerX) {
	NannonGUI.humanIsPlayerX = humanIsPlayerX;
 }

 public static Boolean getHumanIsPlayerX() {
	return humanIsPlayerX;
 }

}