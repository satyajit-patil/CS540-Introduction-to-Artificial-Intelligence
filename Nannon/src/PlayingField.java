import java.awt.*;
import java.awt.event.*;


//
//
// PlayingField - taken from the AgentWorld (another cs540 testbed) and modified for Nannon.
//
//   There is a lot of unneeded junk in here from 15-20 year-old code, so don't try to understand it all.
//

public class PlayingField extends Canvas implements MouseListener, MouseMotionListener
{ 
  private static final long serialVersionUID = 1L;

  private static final boolean debuggingThisClass = false;
  
          static final int objectSize     = 10, // The radius of the circular objects.
                           objectDiameter =  2 * objectSize,
                           objectDiameterSquared = objectDiameter * objectDiameter;

          static     Color wallColor  = Color.blue,
                           fieldColor = new Color(25, 150,50); // A green.
  private static final int maxLayoutTries = 1000; // Try random placements no more than this many times.
  private              int minWidth       =  100, minHeight = 66; // These settings are the minimums.

  private Boolean   synchForPlayerCheckIn = new Boolean(true); // One per field is fine here.

  static int round(int    x) { return x; }
  static int round(double x) { return (int) Math.round(x); }
  
  static  double unitOrig = 8; // Should typically be in 3-7, depending on #players.
          double unit = unitOrig; 
  private double radius, diameter, outerDiameter, x_firstCell, y_firstCell, 
                 screenWidthToUse, screenHeightToUse, orig_maxScreenWidth, orig_maxScreenHeight,
                 x_SafeO_base, y_SafeO_base, x_HomeX_base, y_HomeX_base, x_HomeO_base, y_HomeO_base, x_SafeX_base, y_SafeX_base;
  private int    numbCells = 6, numbPieces = 3;
  
  private Image     offscreen = null; // A double-buffer drawing scheme is used.
  private Graphics  offscreenGraphics;
  private Dimension dimensionOfPlayingField = null;
  private Position  centerPosition, tempPosition;
  private Rectangle innerBoxInner, innerBoxOuter, mineralsRectangle, vegetablesRectangle;
  private boolean   configured       = false, // Has the initial configuration been created?                    
                    allowReshaping   = true,  // Can this canvas be reshaped?
                    started          = false, // Has the START button been pushed?
                    circleAllObjects = false, // Show true boundaries of objects.
                    displayOn        = true,
                    debugging        = debuggingThisClass;



  public void setConfigured(boolean configured) { this.configured = configured; }

  private int       offscreenWidth   =  -1, offscreenHeight  = -1,
                    playersReadyToGo =   0, playersResumed   =  0,
                    clockPeriod      = 100, lastMeasuredTime =  0;
  
  private Entity    entityGrabbed;

  // Have access to user clicks.
  private Position mouseUpAt, mouseDownAt;
  private boolean  mouseDown      = false;
  
  

  // The manager thread needs access to these.
  NannonGUI     nannonGUI;
  FastRectangle outerBoxInner;
  boolean       initialLayoutCompleted = false, showHelp, paused = false, singleStepping = false; // Stop after each step?
  int           playersSoFar = 0, vegetablesSoFar =  0, mineralsSoFar =  0,
                gamesPlayed  = 0, gamesToPlay     = -1, gameDuration  = -1; // Indicate these should be ignored.

  private boolean debugSizing = false;;
  
  private void resetLayoutParameters() {
	  
	    if (debugSizing) Utils.println("\nSTART resetLayoutParameters: unit = " + unit + " numbCells = " + numbCells);
//	    if (numbCells >  5) { unitOrig = Math.min(unitOrig,  9); unit = unitOrig; }
//	    if (numbCells >  8) { unitOrig = Math.min(unitOrig,  8); unit = unitOrig; }
//	    if (numbCells > 10) { unitOrig = Math.min(unitOrig,  7); unit = unitOrig; }
	    if (unitOrig  <  2) { unitOrig = 2;                      unit = unitOrig; }
	    
	    double multiplier = 5;
	    radius        = multiplier * unit;
	    diameter      = 2 * radius; 
	    outerDiameter = diameter + 2 * unit; // I.e., (2 * multiplier + 2) units.
	    
	    double widthSpace  = 140; // Half on each side.  These do not have 'units' (ie, need to be multiplied by 'unit').
	    double heightSpace =  75;
	    double safeSpace   = (widthSpace / 6);
	    double homeSpace   = safeSpace + (2 * multiplier + 2) + 10;
	    
	    int width  = round(widthSpace  * unit + numbCells  * outerDiameter);
	    int height = round(heightSpace * unit + numbPieces * outerDiameter);
	    double unitW = unit, unitH = unit;
	    
	    if (debugSizing) Utils.println(" A: width = " + width + " (max = " + screenWidthToUse + "), height = " + height + " (max = " + screenHeightToUse + ")");
	    if (width  > screenWidthToUse || configured) {
	    	unitW = screenWidthToUse / (widthSpace   + (2 * multiplier + 2) * numbCells);   if (debugSizing && width  > screenWidthToUse)  Utils.println("     width too big, so unit = " + unitW);
	   }
	    if (height > screenHeightToUse || configured) {
	    	unitH = screenHeightToUse / (heightSpace + (2 * multiplier + 2) * numbPieces) ; if (debugSizing && height > screenHeightToUse) Utils.println("     height too big, so unit = " + unitH);
	    }
	    unit = Math.max(1, Math.min(unitW, unitH)); // Don't allow to get too small.
	    
	    // Redo in case unit changed.
	    radius        = multiplier * unit;
	    diameter      = 2 * radius; 
	    outerDiameter = diameter + 2 * unit;
	    width  = round(widthSpace  * unit + numbCells  * outerDiameter); // Reset these so all is consistent.
	    height = round(heightSpace * unit + numbPieces * outerDiameter);
	    
	    int widthMismatch  = round(screenWidthToUse  - width);
	    int heightMismatch = round(screenHeightToUse - height);
	    if (debugSizing) Utils.println("    widthMismatch =  " + widthMismatch + " and heightMismatch = " + heightMismatch);
	    
	    width  = round(Math.min(orig_maxScreenWidth,  Math.max(width,  minWidth)));
	    height = round(Math.min(orig_maxScreenHeight, Math.max(height, minHeight)));
	    
	    if (debugSizing) Utils.println(" B: width = " + width + " (max = " + screenWidthToUse+ "), height = " + height + " (max = " + screenHeightToUse + ")");
	    
	    x_firstCell   =     ((screenWidthToUse  - (numbCells  * outerDiameter)) / 2);
	    y_firstCell   = 7 * ((screenHeightToUse - (numbPieces * outerDiameter)) / 8) + 10; // This '10' might need rethinking.
	    x_SafeO_base  = 1 * x_firstCell / 3   - radius            ; y_SafeO_base = y_firstCell; // Allow these to be different.
	    x_HomeX_base  = 2 * x_firstCell / 3   - radius            ; y_HomeX_base = y_firstCell; // Go back a radius to center here.
	    x_HomeO_base  = screenWidthToUse - radius - 2 * x_firstCell / 3; y_HomeO_base = y_firstCell;
	    x_SafeX_base  = screenWidthToUse - radius - 1 * x_firstCell / 3; y_SafeX_base = y_firstCell;  
	    
	    if (debugSizing) Utils.println("DONE: unit = " + unit + ", x_firstCell = " + x_firstCell + " (screenWidth = " + screenWidthToUse + "),  y_firstCell = " + y_firstCell + " (screenHeight = " + screenHeightToUse + ")");
  }
  
  // Constructor
  PlayingField(NannonGUI nannonGUI, int numbCells, int numbPieces)
  {
    super();
  
    setMinimumSize(new Dimension(minWidth, minHeight)); // Seems to be ignored.  Still some ugliness in sizing, but good enough.
    
    this.nannonGUI  = nannonGUI;    
    this.numbCells  = numbCells; 
    this.numbPieces = numbPieces;
    
    Toolkit toolkit = Toolkit.getDefaultToolkit ();
    Dimension   dim = toolkit.getScreenSize();
    if (debugSizing) Utils.println();
    if (debugSizing) Utils.println("PlayingField: Width  of Screen Size is " + dim.width  + " pixels.");
    if (debugSizing) Utils.println("PlayingField: Height of Screen Size is " + dim.height + " pixels.");
    orig_maxScreenWidth  = (100 * dim.width)  / 100; // For some reason, there will still be a small border (which is good).
    orig_maxScreenHeight = ( 85 * dim.height) / 100; // Leave some room for the scores below.
    screenWidthToUse       = orig_maxScreenWidth  / Math.max(1, Math.min(2.75, 14 - numbCells)); // Don't be too large initially.
    screenHeightToUse      = orig_maxScreenHeight / Math.max(1, Math.min(3.50,  7 - numbPieces));
    
    resetLayoutParameters(); 
    
    centerPosition      = new Position( 0,  0);
    tempPosition        = new Position( 0,  0);
    mouseDownAt         = new Position(-1, -1);
    mouseUpAt           = new Position(-1, -1);

    innerBoxInner       = new Rectangle();
    innerBoxOuter       = new Rectangle();
    outerBoxInner       = new FastRectangle();
    mineralsRectangle   = new Rectangle();
    vegetablesRectangle = new Rectangle();

    if (allowReshaping) { setSize((int) screenWidthToUse, (int) screenHeightToUse); } else { super.setSize((int) screenWidthToUse, (int) screenHeightToUse); }
    addMouseListener(      this);
    addMouseMotionListener(this);

//  Utils.println("INITIAL Width  of Window Size is " + getWidth()  + " pixels");
//  Utils.println("INITIAL Height of Window Size is " + getHeight() + " pixels");
    debugging = (debuggingThisClass && NannonGUI.masterDebugging);
    validate();
//  Utils.println("FINAL   Width  of Window Size is " + getWidth()  + " pixels");
//  Utils.println("FINAL   Height of Window Size is " + getHeight() + " pixels");
    setVisible(true);
  }
  
  long getCurrentTime() { return System.currentTimeMillis(); }

  void useSingleStepMode(boolean value)
  {
    if (singleStepping)
    { //long currentTime = getCurrentTime();
    
    }

    singleStepping = value;
    if (!singleStepping) resume();
  }

  // Don't resize/reshape once running.   isResizable()
  public void setSize(Dimension d)
  {
	if (debugSizing) Utils.println("\nPlayingField (in):  setSize(Dimension d)  deltaW = " + (d.width - screenWidthToUse) + " and deltaH =  " + (d.height - screenHeightToUse));
    if (allowReshaping) {
    	if (configured) {
            int width  = round(Math.min(orig_maxScreenWidth,  Math.max(d.width,  minWidth)));
            int height = round(Math.min(orig_maxScreenHeight, Math.max(d.height, minHeight)));
            setSize(width, height);
    	} else {
    	  	super.setSize(d);
    	}
    }
    if (debugSizing) Utils.println("PlayingField (out): setSize(d)\n");
  }
  public void setSize(int width, int height)
  {
	if (debugSizing) Utils.println("\nPlayingField (in):  setSize(width = " + width + ", height =  " + height + ")");
	
	if (configured) {
        width           = round(Math.min(orig_maxScreenWidth,  Math.max(width,  minWidth)));
        height          = round(Math.min(orig_maxScreenHeight, Math.max(height, minHeight)));
	}
	
	if (allowReshaping) { 
    	super.setSize(width, height);
		if (configured) { resetLayoutParameters(); }
    }
    if (debugSizing) Utils.println("PlayingField (out): setSize(int width, int height)\n");
    
  }
  public void setBounds(int x, int y, int width, int height)
  {
	if (debugSizing) Utils.println("\n PlayingField: setBounds: x = " + x + ", y = " + y + ", width = " + width + ", and height = " + height);
	if (debugSizing) Utils.println(  "               screenWidth = " + screenWidthToUse + ", screenHeight = " + screenHeightToUse + ", outerDiameter = " + outerDiameter + ", numbCells = " + numbCells);

	if (configured) {
        width           = round(Math.min(orig_maxScreenWidth,  Math.max(width,  minWidth)));
        height          = round(Math.min(orig_maxScreenHeight, Math.max(height, minHeight)));
	}
	screenWidthToUse  = width;
	screenHeightToUse = height;
	
	if (allowReshaping) {
     	super.setBounds(x, 
     			        y, 
     					width,
     					height);
		if (configured) { resetLayoutParameters(); }
    } else { // Keep current size, but recenter.
       super.setBounds((int) (x + (width  - screenWidthToUse)  / 2),
    		           (int) (y + (height - screenHeightToUse) / 2),
                       (int) screenWidthToUse, 
                       (int) screenHeightToUse);
    } 
	if (debugSizing) Utils.println(" PlayingField: setBounds(4args, out) maxScreenHeight = " + screenHeightToUse + " and maxScreenWidth = " + screenWidthToUse);  
  }
  public Dimension getMinimumSize()
  {
    return new Dimension(minWidth, minHeight);
  }
  public Dimension getPreferredSize()
  {
    return new Dimension((int) screenWidthToUse, (int) screenHeightToUse);
  }

  void gamesToPlay(int games)
  {
    gamesToPlay = Math.max(0, games);
  }

  void gameDuration(int duration)
  {
    gameDuration = Math.max(0, duration);
  }

  public void reportMessage(String msg)
  {
    nannonGUI.reportInInfoBar(msg);
  }

  void setCircleAllObjects(boolean value)
  {
    if (circleAllObjects != value)
    {
      circleAllObjects = value;
      redisplay(true);
      if (value) reportMessage("All objects are internally treated as circles.");
      else reportMessage("");
    }
  }


  void showHelp()
  {
    showHelp = true;
    pause();
    repaint();
  }

  void redisplay(boolean override)
  { // Override having the display being on hold.
    if (override && !isDisplayOn()) nannonGUI.setDisplayON();
    else redisplay();
  }
  void redisplay()
  {
    if (displayOn) repaint();
  }

  boolean isDisplayOn()
  {
    return displayOn;
  }

  void setDisplayOn(boolean value)
  {
    if (displayOn != value)
    {
      displayOn = value;
      repaint();
    }
  }

  // Update the "offscreen" image buffer, then dump it to the screen.
  public void update(Graphics g)
  {
    paint(g);
  }
  
  private void relayoutThenRefreshScreen() {
  	resetLayoutParameters();
    nannonGUI.resetFonts();
    nannonGUI.validate();
  }

  public void paint(Graphics g)
  { 
	  dimensionOfPlayingField = getSize();

      if (debugging && (dimensionOfPlayingField.width != minWidth || dimensionOfPlayingField.height != minHeight))
      {
          Utils.println("Inconsistency: w = " + minWidth + " vs " + dimensionOfPlayingField.width
	    	            + "  h = " + minHeight + " vs " + dimensionOfPlayingField.height);
          System.exit(-1);
      }

      if (Math.abs(dimensionOfPlayingField.width  - offscreenWidth)  > 10 || // Create a new buffered screen if the screen has been resized.
    	  Math.abs(dimensionOfPlayingField.height - offscreenHeight) > 10)   // There have been some oscillations due to relayoutThenRefreshScreen, so don't worry if only a few pixels change.
      { if (debugSizing) Utils.println("\nnew screen size in paint(" + dimensionOfPlayingField.width + ", " + dimensionOfPlayingField.height + ")");
        offscreenWidth    = dimensionOfPlayingField.width;
        offscreenHeight   = dimensionOfPlayingField.height;
        offscreen         = createImage(dimensionOfPlayingField.width, dimensionOfPlayingField.height);
        offscreenGraphics = offscreen.getGraphics();
        
        if (configured) { relayoutThenRefreshScreen(); }
      }

    setColorJWS(wallColor);
    fillRectJWS(0,      0,      dimensionOfPlayingField.width,            dimensionOfPlayingField.height);
    setColorJWS(fieldColor);
    fillRectJWS(unit/2, unit/2, dimensionOfPlayingField.width - unit - 1, dimensionOfPlayingField.height - unit - 1);

    if (showHelp)
    { int indent = 15, spacing = 20;
      String s1  = "Help for Nannon",
             s2  = "Visit www.nannon.com/rules.html for the game's rules.",
             s3  = "",
             s4  = "The players that can be moved are highlighted",
             s5  = "Use the mouse to select one if you are playing and not simply observing.",
  //         s5  = "Press " + NannonGUI.startLabel.trim() + " to begin the Agent World.  You can request that the simulator wait for you to press " + NannonGUI.goLabel.trim() + " before each step.",
  //         s6  = (isDisplayOn()
  //                ? "Press \"" + NannonGUI.displayOnLabel.trim()  + "\" to save cpu cycles for training."
  //                : "Press \"" + NannonGUI.displayOffLabel.trim() + "\" to see what is happening."),
  //           s7  = "Clicking on a player reports its name and current score.  Pressing Pause also produces plots of players' scores across games.",
  //           s8  = "If a FollowMouse player has been created, it'll move toward the mouse location whenever a mouse button is down.",
  //           s9  = " ",
  //           s10 = "Additional options are available via the menu:",
  //           s11 = "   You can have the simulator report which players haven't selected a move in the allotted interval.",
  //           s12 = "   You can request to only see what the selected player sees.  (See how high you can score guiding a FollowMouse player.)",
   //          s13 = "   Or you can have the sensors readings of the selected player be displayed.  (Light grey lines indicate nothing sensed.)",
   //          s14 = "   Finally, you can have the display show the 'true' (circular) shape of all objects.",
             sN   = "Click the mouse on the playing field (i.e., this window) in order to return to the game.";

      setColorJWS(Color.black);
      setFont(nannonGUI.regularBoldFont);
      drawStringJWS(s1, indent / 2,  spacing);
      setFont(nannonGUI.helpFont);
      drawStringJWS(s2,  indent,  3 * spacing);
      //setFont(NannonGUI.tinyBoldFont);
      drawStringJWS(s3,  indent,  4 * spacing);
      drawStringJWS(s4,  indent,  5 * spacing);
      setFont(nannonGUI.helpFont);
      drawStringJWS(s5,  indent,  6 * spacing);
//      drawString(s6,  indent,  6 * spacing);
//      drawString(s7,  indent,  7 * spacing);
//      drawString(s8,  indent,  8 * spacing);
//      drawString(s9,  indent,  9 * spacing);
      //setFont(NannonGUI.tinyBoldFont);
//      drawString(s10, indent, 10 * spacing);
//      drawString(s11, indent, 11 * spacing);
//      drawString(s12, indent, 12 * spacing);
//      drawString(s13, indent, 13 * spacing);
//      drawString(s14, indent, 14 * spacing);
      setColorJWS(Color.blue);
      setFont(nannonGUI.smallFont);
      drawStringJWS(sN,   indent, 8 * spacing);
     // drawString(sNp1, indent, 9 * spacing);
    }
    else if (!displayOn)
    {
      writeMessageOnPlayingField(nannonGUI.largeFont,
                                 "The playing field is not being displayed to conserve cpu cycles for training",
                                 "and testing.  Click on \"" + nannonGUI.displayOffLabel.trim() + " to resume displaying.");
    }
    else if (configured)
    { // long currentTime = getCurrentTime();
        
      Color pieceColor = (nannonGUI.currentPlayerIsX ? Color.RED : Color.WHITE);
      double thirdDiameter   = diameter / 3;
      double blueTableStartX = x_firstCell                     - thirdDiameter;
      double blueTableStartY = y_firstCell                     - thirdDiameter;
      double blueTableBottom = y_firstCell + outerDiameter +     thirdDiameter;
      double blueTableWidth  = outerDiameter * numbCells   + 2 * thirdDiameter;
      double blueTableHeight = outerDiameter               + 2 * thirdDiameter - 2 * unit;
      double bottomSpace     = screenHeightToUse - blueTableBottom - 2 * unit; // Seems this isn't quite balanced (due to line thickness?), so tweak slightly. 
      
      setColorJWS(Color.BLUE);
      fillRoundRectJWS(blueTableStartX,  blueTableStartY, 
    		  	 	   blueTableWidth,   blueTableHeight, // Seems this isn't quite balanced (due to line thickness?), so tweak slightly. (No longer needed?)
    		  		   3 * diameter / 4, 3 * diameter / 4);
            
      setFontJWS(nannonGUI.largeBoldFont); // Make red bold to see better.
      FontMetrics fontMetrics = offscreenGraphics.getFontMetrics(nannonGUI.largeBoldFont);
      double halfFontHeight   = fontMetrics.getHeight() / 2;
      
      setColorJWS(Color.RED);
      drawStringJWS("HOME", x_HomeX_base + radius - (fontMetrics.stringWidth("HOME") / 2), blueTableBottom + bottomSpace / 4 + halfFontHeight); 
      drawStringJWS("SAFE", x_SafeX_base + radius - (fontMetrics.stringWidth("SAFE") / 2), blueTableBottom + bottomSpace / 4 + halfFontHeight);     
      
      setFontJWS(nannonGUI.largeBoldFont);
      fontMetrics    = offscreenGraphics.getFontMetrics(nannonGUI.largeBoldFont);
      halfFontHeight = fontMetrics.getHeight() / 2;
      setColorJWS(Color.WHITE);
      drawStringJWS("SAFE", x_SafeO_base + radius - (fontMetrics.stringWidth("SAFE") / 2), blueTableBottom + bottomSpace / 4 + halfFontHeight);
      drawStringJWS("HOME", x_HomeO_base + radius - (fontMetrics.stringWidth("HOME") / 2), blueTableBottom + bottomSpace / 4 + halfFontHeight); 
      
      setColorJWS(pieceColor);
      int dieValue = (nannonGUI.currentPlayerIsX ? nannonGUI.die_playerX : nannonGUI.die_playerO);
      
      if (dieValue > 0 && dieValue <= NannonGameBoard.sidesOnDice) {
	      double dotSize         =          radius  / 3;
	      double dotShift        =          unit    / 3; // Correct visually.
	      double dieSize         =     (3 * radius) / 2; // Do these in a 'long form' to minimize impact of integer division (no longer relevant since switched to doubles).
	      double quarterDie      =     (dieSize) / 4;       
	      double halfDie         =     (dieSize) / 2;
	      double thirdDie        =     (dieSize) / 3;
	      double twoThirdsDie    = (2 * dieSize) / 3;
	      double threeFourthsDie = (3 * dieSize) / 4;
	      
	      for (int pass = 0; pass < 1; pass++) { // Figure out if we are in the pre-move state.
		      double x =  nannonGUI.currentPlayerIsX ? x_firstCell                                    + outerDiameter / 2 - dieSize / 2  // Move between the first two cells.
		    		                                 : x_firstCell + (numbCells - 2) * outerDiameter  + outerDiameter / 2 - dieSize / 2; // Between the last two.
		      if (pass > 0) { // Show BOTH die here;
		           x   = !nannonGUI.currentPlayerIsX ? x_firstCell                                    + outerDiameter / 2 - dieSize / 2  // Move between the first two cells.
		                                             : x_firstCell + (numbCells - 2) * outerDiameter  + outerDiameter / 2 - dieSize / 2; // Between the last two.
		      }
		      double y = blueTableBottom + bottomSpace / 2 - dieSize / 2; // Center in the bottom.
		      
		      fillRoundRectJWS(x + unit, y + unit, dieSize, dieSize, quarterDie, quarterDie);
		      setColorJWS(                  nannonGUI.currentPlayerIsX ? Color.WHITE : Color.BLACK);
		      if (pass == 0) { setColorJWS( nannonGUI.currentPlayerIsX ? Color.WHITE : Color.BLACK); } 
		      else           { setColorJWS(!nannonGUI.currentPlayerIsX ? Color.WHITE : Color.BLACK); }
		      
		      x += dotShift;
		      y += dotShift;
		      switch (nannonGUI.currentPlayerIsX ? nannonGUI.die_playerX : nannonGUI.die_playerO) {
		      case 1:
		    	  fillOvalJWS(x + halfDie,         y + halfDie,         dotSize, dotSize); 
		    	  break;
		      case 2:
		    	  fillOvalJWS(x + thirdDie,        y + twoThirdsDie,    dotSize, dotSize); 
		    	  fillOvalJWS(x + twoThirdsDie,    y + thirdDie,        dotSize, dotSize); 
		    	  break;
		      case 3:
		    	  fillOvalJWS(x + quarterDie,      y + threeFourthsDie, dotSize, dotSize); 
		    	  fillOvalJWS(x + halfDie,         y + halfDie,         dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + quarterDie,      dotSize, dotSize); 
		    	  break;
		      case 4:
		    	  fillOvalJWS(x + quarterDie,      y + quarterDie,      dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + quarterDie,      dotSize, dotSize); 
		    	  fillOvalJWS(x + quarterDie,      y + threeFourthsDie, dotSize, dotSize);
		    	  fillOvalJWS(x + threeFourthsDie, y + threeFourthsDie, dotSize, dotSize);  
		    	  break;
		      case 5:
		    	  fillOvalJWS(x + quarterDie,      y + quarterDie,      dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + quarterDie,      dotSize, dotSize); 
		    	  fillOvalJWS(x + halfDie,         y + halfDie,         dotSize, dotSize); 
		    	  fillOvalJWS(x + quarterDie,      y + threeFourthsDie, dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + threeFourthsDie, dotSize, dotSize); 
		    	  break;
		      case 6:
		    	  fillOvalJWS(x + quarterDie,      y + quarterDie,      dotSize, dotSize); 
		    	  fillOvalJWS(x + quarterDie,      y + halfDie,         dotSize, dotSize); 
		    	  fillOvalJWS(x + quarterDie,      y + threeFourthsDie, dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + quarterDie,      dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + halfDie,         dotSize, dotSize); 
		    	  fillOvalJWS(x + threeFourthsDie, y + threeFourthsDie, dotSize, dotSize); 
		    	  break;
		      }
	      }
      }

      double locOfMoveDestination_x = -1, locOfMoveDestination_y = -1;
      
      for (int loc = 0; loc < numbCells; loc++) {
    	  if (highlightFrom[loc] || highlightTo[loc]) { // To highlight a cell, draw a bigger circle underneath it.
        	  setColorJWS(Color.BLACK);
    		  fillOvalJWS(x_firstCell + loc * outerDiameter - unit, y_firstCell - unit, diameter + 2 * unit, diameter + 2 * unit);    		  
    	  }
      }
      
      for (int loc = 0; loc < numbCells; loc++) if (loc == nannonGUI.pieceBeingMoved) { // Show the 'ghost' of this piece.
 		 setColorJWS(Color.GRAY);
		 fillOvalJWS(     x_firstCell + loc * outerDiameter,        y_firstCell, diameter, diameter);
 	 	 setColorJWS(nannonGUI.atCell[loc] == NannonGameBoard.playerX ? Color.RED : Color.WHITE);
 	 	 drawOvalJWS(     x_firstCell + loc * outerDiameter,        y_firstCell, diameter, diameter);   	  
      } else if (loc == nannonGUI.targetOfMove) { // Show the destination 'ghost' of this piece.
    	 locOfMoveDestination_x = x_firstCell + loc * outerDiameter;
    	 locOfMoveDestination_y = y_firstCell;
 		 setColorJWS(nannonGUI.atCell[loc] == NannonGameBoard.empty ? Color.GRAY : (nannonGUI.atCell[loc] == NannonGameBoard.playerX ? Color.RED : Color.WHITE));
 		 fillOvalJWS(locOfMoveDestination_x, locOfMoveDestination_y, diameter, diameter);
 		 setColorJWS(pieceColor);
 		 drawOvalJWS(locOfMoveDestination_x, locOfMoveDestination_y, diameter, diameter);
      } else { // Draw a piece (or empty cell) that isn't moving.
    	  if (nannonGUI.atCell[loc] != NannonGameBoard.empty) {
    		  setColorJWS(nannonGUI.atCell[loc] == NannonGameBoard.playerX ? Color.RED : Color.WHITE);
    		  fillOvalJWS(x_firstCell + loc * outerDiameter, y_firstCell, diameter, diameter); 
    	  } else {
    		  setColorJWS(Color.GRAY); // EMPTY
    		  fillOvalJWS(x_firstCell + loc * outerDiameter, y_firstCell, diameter, diameter);
    	  }
      }
      
      for (int loc = 0; loc < numbCells; loc++) { // Mark moveable and reachable pieces with smaller inner circles.
    	  if (nannonGUI.moveable[loc]) {
        	  setColorJWS(Color.BLACK);
    		  fillOvalJWS(x_firstCell + thirdDiameter + loc * outerDiameter, y_firstCell + thirdDiameter, thirdDiameter, thirdDiameter);  
    	  }
    	  if (nannonGUI.reachable[loc]) {
        	  setColorJWS(pieceColor);
    		  fillOvalJWS(x_firstCell + thirdDiameter + loc * outerDiameter, y_firstCell + thirdDiameter, thirdDiameter, thirdDiameter);  
    	  }
      }

      if (nannonGUI.moveable[numbCells] || nannonGUI.reachable[numbCells]) { 
    	  if (highlightFrom[numbCells]) {
        	  setColorJWS(Color.BLACK);
    		  fillOvalJWS((nannonGUI.currentPlayerIsX ? x_HomeX_base : x_HomeO_base) - unit, 
    				  					 (nannonGUI.currentPlayerIsX ? y_HomeX_base : y_HomeO_base) - unit 
    				  					 	- outerDiameter * ((nannonGUI.currentPlayerIsX ? nannonGUI.homePieces_playerX : nannonGUI.homePieces_playerO) - 1), 
    				  					 diameter + 2 * unit, diameter + 2 * unit);     		  
    	  } else if (highlightTo[numbCells]) {
        	  setColorJWS(Color.BLACK);
    		  fillOvalJWS((nannonGUI.currentPlayerIsX ? x_SafeX_base : x_SafeO_base) - unit, 
    				  					 (nannonGUI.currentPlayerIsX ? y_SafeX_base : y_SafeO_base) - unit
    				  					 	- outerDiameter * ((nannonGUI.currentPlayerIsX ? nannonGUI.safePieces_playerX : nannonGUI.safePieces_playerO) + 0), 
    				  					 diameter + 2 * unit, diameter + 2 * unit);   
    	  }
      }
      
      for (int sO = 0; sO < nannonGUI.safePieces_playerO; sO++) {
    	  setColorJWS(Color.WHITE);
    	  fillOvalJWS(x_SafeO_base, y_SafeO_base - outerDiameter * sO, diameter, diameter);    	      	  
      }
      for (int hX = 0; hX < (nannonGUI.homePieces_playerX - ( nannonGUI.currentPlayerIsX && numbCells == nannonGUI.pieceBeingMoved ? 1 : 0)); hX++) { // If the top is the piece being moved, we need to skip it.  Since we will draw it later in its intermediate position.
    	  setColorJWS(Color.RED);
    	  fillOvalJWS(x_HomeX_base, y_HomeX_base - outerDiameter * hX, diameter, diameter);    	      	  
      }
      for (int hO = 0; hO < (nannonGUI.homePieces_playerO - (!nannonGUI.currentPlayerIsX && numbCells == nannonGUI.pieceBeingMoved ? 1 : 0)); hO++) { // If the top is the piece being moved, we need to skip it.
    	  setColorJWS(Color.WHITE);
    	  fillOvalJWS(x_HomeO_base, y_HomeO_base - outerDiameter * hO, diameter, diameter);    	      	  
      }
      for (int sX = 0; sX < nannonGUI.safePieces_playerX; sX++) {
    	  setColorJWS(Color.RED);
    	  fillOvalJWS(x_SafeX_base, y_SafeX_base - outerDiameter * sX, diameter, diameter);    	      	  
      }
      
      // DRAW the score.
      if (nannonGUI.lastCellFilled >= 0) {
		  double xOffset     = blueTableStartX +  5 * unit;
		  double yOffset     = 0.65 * blueTableStartY;
		  double widthToUse  = blueTableWidth  - 10 * unit;  
		  double heightToUse = 0.50 * blueTableStartY;
	//	  double extraOnTopY = blueTableStartY - heightToUse;
		  double dotSize     = unit    / 2;
		  double halfDotSize = dotSize / 2;
		  
		  double maxY        = Math.max(0.65, Math.min(nannonGUI.maxFraction * 1.2, 1.01));
		  double xMult       = widthToUse  / ((nannonGUI.getPlayThisManyPostBurninGamesBeforeVisualizing() / (double) nannonGUI.reportingPeriodForGames) + 0.00001); // Assume reportingPeriodForGames games will be visualized at most (and if not, still room to continue on to the right).
		  double yMult       = heightToUse / maxY;
	
		  setColorJWS(Color.BLACK);
		  double startOfXaxis = xOffset;
	      double endOfXaxis   = xOffset + widthToUse  + 4 * unit;
	      double startOfYaxis = yOffset;
	      double endOfYaxis   = yOffset - heightToUse - 4 * unit;
	      
		  drawLineJWS(startOfXaxis - 2 * unit, startOfYaxis,            endOfXaxis,      startOfYaxis);     // X axis
		  drawLineJWS(startOfXaxis,            startOfYaxis + 2 * unit, startOfXaxis,      endOfYaxis);     // Y axis
		  drawLineJWS(startOfXaxis - 2 * unit, startOfYaxis + 1,        endOfXaxis,      startOfYaxis + 1); // X axis double thick
		  drawLineJWS(startOfXaxis + 1,        startOfYaxis + 2 * unit, startOfXaxis + 1,  endOfYaxis);     // Y axis double thick
	      
	      // Draw some arrow heads on the axes.
	      double arrowHeadSize  = 2 * dotSize;
	      
	      drawLineJWS(  endOfXaxis, startOfYaxis,   endOfXaxis - arrowHeadSize, startOfYaxis + arrowHeadSize); // On the x axis.
	      drawLineJWS(  endOfXaxis, startOfYaxis,   endOfXaxis - arrowHeadSize, startOfYaxis - arrowHeadSize);
	      
	      drawLineJWS(startOfXaxis,   endOfYaxis, startOfXaxis + arrowHeadSize,   endOfYaxis + arrowHeadSize); // On the Y axis.
	      drawLineJWS(startOfXaxis,   endOfYaxis, startOfXaxis - arrowHeadSize,   endOfYaxis + arrowHeadSize);
		  
	      for (int x = 1; x <= nannonGUI.lastCellFilled; x++) {
	      //  setLineThicknessJWS(unit / 2);
	    	  setColorJWS(Color.RED);
	    	  drawLineJWS(startOfXaxis + ((x - 1) * xMult)              , startOfYaxis - (nannonGUI.fractionWonByX[x - 1] * yMult), // Seems no easy way to draw thicker lines?
	    			      startOfXaxis +  (x      * xMult)              , startOfYaxis - (nannonGUI.fractionWonByX[x]     * yMult));
	    	  drawOvalJWS(startOfXaxis +  (x      * xMult) - halfDotSize, startOfYaxis - (nannonGUI.fractionWonByX[x]     * yMult) - halfDotSize, dotSize, dotSize);
	    	  setColorJWS(Color.WHITE);
	    	  drawLineJWS(startOfXaxis + ((x - 1) * xMult)              , startOfYaxis - (nannonGUI.fractionWonByO[x - 1] * yMult), 
	    			      startOfXaxis +  (x      * xMult)              , startOfYaxis - (nannonGUI.fractionWonByO[x]     * yMult));
	    	  drawOvalJWS(startOfXaxis +  (x      * xMult) - halfDotSize, startOfYaxis - (nannonGUI.fractionWonByO[x]     * yMult) - halfDotSize, dotSize, dotSize);
	      }	
	
	      setFontJWS(nannonGUI.largeBoldFont); 
	      fontMetrics = offscreenGraphics.getFontMetrics(nannonGUI.largeBoldFont);
	      halfFontHeight = fontMetrics.getHeight() / 2;
		  setColorJWS(Color.BLACK);
	      String xAxisLabel = "Games Played";
	      String yAxisLabel = "Win %";
	      drawStringJWS(xAxisLabel, xOffset + widthToUse / 2 - (fontMetrics.stringWidth(xAxisLabel) / 2), startOfYaxis + 4 * halfFontHeight);
	      drawStringJWS(yAxisLabel, xOffset - (25 * dotSize) -  fontMetrics.stringWidth(yAxisLabel),      startOfYaxis - heightToUse / 2);
	
	      setFontJWS(nannonGUI.regularBoldFont); 
	      fontMetrics = offscreenGraphics.getFontMetrics(nannonGUI.regularBoldFont);
	      halfFontHeight = fontMetrics.getHeight() / 2;
	      
	      double tickWidth = unit / 2; // Actually the tickmark's length is TWICE this.
	      double tickSize = widthToUse / 10;
	      double lastLabelLocationXrightEnd = startOfXaxis + unit;
	      for (double tickX = tickSize; tickX <= widthToUse * 1.05; tickX += tickSize) {
	      //  Utils.println("tickX = " + tickX + " tickSize = " + tickSize + " xMult = " + xMult);
	    	  drawLineJWS(startOfXaxis + tickX, startOfYaxis - tickWidth,  
	    			      startOfXaxis + tickX, startOfYaxis + tickWidth);
	    	  
	    	  int    postBurnInGames   = Math.max(100, nannonGUI.getPlayThisManyPostBurninGamesBeforeVisualizing());
	    	  boolean useK             = (postBurnInGames >= 5000); // Hack in some code in case we don't have many games.
	    	  NannonGUI.reportingPeriodForGames = (useK ? 1000 : 1);
    		  double xValueInK         = (tickX / widthToUse) * postBurnInGames / (useK ? 1000 : 1);
	    	  String xLabel            = Utils.truncate(xValueInK, 0) + (useK ? "K" : "");
	    	  int    labelWidth        = fontMetrics.stringWidth(xLabel);
	    	  double thisLabelLocation = startOfXaxis + tickX - labelWidth / 2;
	    	  if (thisLabelLocation - lastLabelLocationXrightEnd > 2 * unit) {
	    		  lastLabelLocationXrightEnd = thisLabelLocation + labelWidth;
	    		  drawStringJWS(xLabel, thisLabelLocation, startOfYaxis + (2 * dotSize) + fontMetrics.getHeight());
	    	  }
	      }
	      tickSize = yMult / 10;
	      double lastLabelLocationYtop = startOfYaxis - halfFontHeight;
	      for (double tickY = tickSize; tickY <= maxY * yMult * 1.05;    tickY += tickSize) {
	      //  Utils.println("tickY = " + tick + " tickSize = " + tickSize + " yMult = " + yMult);
	    	  drawLineJWS(startOfXaxis - tickWidth, startOfYaxis - tickY,  
	    			      startOfXaxis + tickWidth, startOfYaxis - tickY);

	    	  String yLabel            = Utils.truncate(100 * tickY / yMult, 0) + "%";
	    	  double thisLabelLocation = startOfYaxis - tickY + halfFontHeight / 2;
	    	  if (lastLabelLocationYtop - thisLabelLocation > 2 * unit) {
	    		  drawStringJWS(yLabel, xOffset - (3 * dotSize) - fontMetrics.stringWidth(yLabel), thisLabelLocation);
	    		  lastLabelLocationYtop = thisLabelLocation - fontMetrics.getHeight();
	    	  }
	      }
      }
      
      ///////////////////////////////////////////////////////////

      // Do this after the score in case some lines cross (so the pieces move OVER the score board).
      
      if (nannonGUI.moveable[numbCells]) {  // Moving from HOME is possible.  Mark the TOP piece in the HOME pile.
    	  setColorJWS(Color.BLACK);
    	  fillOvalJWS((nannonGUI.currentPlayerIsX ? x_HomeX_base : x_HomeO_base) + thirdDiameter, 
    			      (nannonGUI.currentPlayerIsX ? y_HomeX_base : y_HomeO_base) +
		                - outerDiameter * ((nannonGUI.currentPlayerIsX ? nannonGUI.homePieces_playerX : nannonGUI.homePieces_playerO) - 1) + thirdDiameter,
		              thirdDiameter, thirdDiameter);      	  
      }
      if (nannonGUI.reachable[numbCells]) {  // Moving to SAFETY is possible.  Mark *above* the TOP piece in the SAFE pile (but note we count from zero).
    	  setColorJWS(pieceColor); // Might by on an opponent or an empty cell.
    	  fillOvalJWS((nannonGUI.currentPlayerIsX ? x_SafeX_base : x_SafeO_base) + thirdDiameter, 
    			      (nannonGUI.currentPlayerIsX ? y_SafeX_base : y_SafeO_base) +
                       - outerDiameter * ((nannonGUI.currentPlayerIsX ? nannonGUI.safePieces_playerX : nannonGUI.safePieces_playerO) + 0) + thirdDiameter,
                     thirdDiameter, thirdDiameter);  
      }
      
      // Moving to SAFE?
      if (numbCells == nannonGUI.targetOfMove) { 
    	 locOfMoveDestination_x = (nannonGUI.currentPlayerIsX ? x_SafeX_base : x_SafeO_base);
    	 locOfMoveDestination_y = (nannonGUI.currentPlayerIsX ? y_SafeX_base : y_SafeO_base) +
                                   - outerDiameter * ((nannonGUI.currentPlayerIsX ? nannonGUI.safePieces_playerX : nannonGUI.safePieces_playerO) + 0);
    	 setColorJWS(pieceColor);
   		 drawOvalJWS(locOfMoveDestination_x, locOfMoveDestination_y,	diameter, diameter);
      }
      if (nannonGUI.pieceBeingMoved >= 0) { // If negative, no piece is being moved during this repaint.
    	  double locOfMove_x = x_firstCell + nannonGUI.pieceBeingMoved * outerDiameter;
    	  double locOfMove_y = y_firstCell;
    	  if (numbCells == nannonGUI.pieceBeingMoved) {  // If moving from HOME, then need to correct the two lines directly above. 
    		  locOfMove_x = (nannonGUI.currentPlayerIsX ? x_HomeX_base : x_HomeO_base);
    		  locOfMove_y = (nannonGUI.currentPlayerIsX ? y_HomeX_base : y_HomeO_base) +
    		                 - outerDiameter * ((nannonGUI.currentPlayerIsX ? nannonGUI.homePieces_playerX : nannonGUI.homePieces_playerO) - 1);
   	 		  setColorJWS(pieceColor);
   	 		  drawOvalJWS(locOfMoveDestination_x, locOfMoveDestination_y, diameter, diameter);
    	  }
    	  double deltaX = nannonGUI.moveFraction * (locOfMoveDestination_x - locOfMove_x);
    	  double deltaY = nannonGUI.moveFraction * (locOfMoveDestination_y - locOfMove_y);
    	  setColorJWS(Color.BLACK);
    	  fillOvalJWS(locOfMove_x + deltaX - unit, locOfMove_y + deltaY - unit, diameter + 2 * unit, diameter + 2 * unit); // Add a black border.
    	  setColorJWS(pieceColor);
    	  fillOvalJWS(locOfMove_x + deltaX,        locOfMove_y + deltaY,        diameter,            diameter);
      }
      
/*
 * 
 
  
  
      for(int i = 0; i < vegetablesSoFar; i++) if (vegetable[i].exists(currentTime))
      { p = vegetable[i].getPosition();

        if (onlyDrawIfSeen() && !vegetable[i].getCanBeSeenBySelectedPlayer()) continue;

        if (circleAllObjects)
        {
          offscreenGraphics.setColor(Color.black);
          offscreenGraphics.fillOval(p.x - objectSize, p.y - objectSize,
                                     objectDiameter,   objectDiameter);
        }
        offscreenGraphics.setColor(Vegetable.VegetableColor);
        offscreenGraphics.fillOval(p.x - Vegetable.Xwidth, p.y - Vegetable.Ywidth,
                                   2 * Vegetable.Xwidth,   2 * Vegetable.Ywidth);
        if (debugging && i < 10)
        {
          offscreenGraphics.setColor(Color.black);
          offscreenGraphics.drawString(vegetable[i].getID() + "",
                                       p.x - Vegetable.Xwidth + 1,
                                       p.y + 2 * unit);
        }
      }

      if ((started && selectedPlayer != null && !selectedPlayer.trainingThisPlayer()) ||
          (!started && entityGrabbed != null)) // Indicate that the grabbed object is located here.
      { 
        if (started) p = selectedPlayer.getPosition(); else p = entityGrabbed.getPosition();
              
        offscreenGraphics.setColor(Color.white);
        offscreenGraphics.fillOval(p.x - Player.Xwidth / 2, p.y - Player.Ywidth / 2,
                                   Player.Xwidth,           Player.Ywidth);
      }
*/
/*
      if (paused && started && gamesPlayed > 0) // Report game scores.
      { int xDim      = gamesPlayed + 4, // Have a little interior border, as well.
            yDim      = 40,
            xHalfDim  = xDim / 2,
            yDimUpper = (8 * yDim) / 10,
            yDimLower = (2 * yDim) / 10,
            yScale    = yDimUpper - 2, // Leave a little border.
            maxScoreUpper =  3000,     // Truncate if outside of ± this value.
            maxScoreLower = -3000 / 4; // Need to match this 4 and the 8/2 ratio above!
      /*
        for(int i = 0; i < playersSoFar; i++) if (player[i].exists(currentTime) && player[i].showScores)
        {
          p = player[i].getPosition();

          int yAxis    = p.x - xHalfDim - 2, // Center above the player.
              xAxis    = p.y - Player.Ywidth / 2 - yDimLower - 10; // Recall there is a border around the box.

          offscreenGraphics.setColor(Color.black);
          offscreenGraphics.fillRect(yAxis - 4, xAxis - yDimUpper - 4, xDim + 8, yDim + 8);
          offscreenGraphics.setColor(Color.gray);
          offscreenGraphics.fillRect(yAxis - 2, xAxis - yDimUpper - 2, xDim + 4, yDim + 4);

          // Draw the axes.
          offscreenGraphics.setColor(Color.blue);
          offscreenGraphics.drawLine(yAxis, xAxis, yAxis + gamesPlayed + 2, xAxis); // The xAxis line.
          offscreenGraphics.drawLine(yAxis, xAxis - (yScale * maxScoreLower) / maxScoreUpper,
                                     yAxis, xAxis - yScale);

          for(int game = 0; game < gamesPlayed; game++)
          { int temp = Math.max(maxScoreLower, Math.min(player[i].getGameScore(game), maxScoreUpper));

            if      (temp > 0)
            { // Negate since Y-down is increasing, but want to use the usual convention.
            
              offscreenGraphics.setColor(Color.green);
              offscreenGraphics.drawLine(yAxis + game + 1, xAxis - 1,
                                         yAxis + game + 1, xAxis - (yScale * temp) / maxScoreUpper);
            }
            else if (temp < 0)
            {
              offscreenGraphics.setColor(Color.red);
              offscreenGraphics.drawLine(yAxis + game + 1, xAxis + 1,
                                         yAxis + game + 1, xAxis - (yScale * temp) / maxScoreUpper);
            }
          }
        }
        reportMessage("Scores over "  + gamesPlayed 
                                    + " games are plotted in [" + maxScoreLower + "," + maxScoreUpper
                                    + "].  If no other options have been selected, clicking on a player will toggle showing its plot.");
      }
      */
    }
    else
    { 
      writeMessageOnPlayingField(nannonGUI.largeFont,
                                 "Press Configure to create initial configurations.",
                                 "Press Start to begin the Agent World.");
    }
    g.drawImage(offscreen, 0, 0, null);
  }

  private void drawRectJWS(double x, double y, double width, double height) {
	  offscreenGraphics.drawRect(round(x), round(y), round(width), round(height));	
  }

  private void fillRectJWS(double x, double y, double width, double height) {
	  offscreenGraphics.fillRect(round(x), round(y), round(width), round(height));	
  }

  private void drawOvalJWS(double x, double y, double width, double height) {
	  offscreenGraphics.drawOval(round(x), round(y), round(width), round(height));	
  }

  private void fillOvalJWS(double x, double y, double width, double height) {
//	  Utils.println("fillOval: " + x + " " + y);
	  offscreenGraphics.fillOval(round(x), round(y), round(width), round(height));
  }

  private void drawLineJWS(double x1, double y1, double x2, double y2) {
	  offscreenGraphics.drawLine(round(x1), round(y1), round(x2), round(y2));
  }

  private void drawStringJWS(String string, double x, double y) {
	  offscreenGraphics.drawString(string, round(x), round(y));	
  }

  private void fillRoundRectJWS(double x, double y, double width, double height, double arcWidth, double arcHeight) {
//	  Utils.println("fillRoundRect");
	  offscreenGraphics.fillRoundRect(round(x), round(y), round(width), round(height), round(arcWidth), round(arcHeight));	
  }

  private void setColorJWS(Color color) {
	  if (offscreenGraphics == null) Utils.waitForEnter("setColorJWS: have offscreenGraphics == null");
	  offscreenGraphics.setColor(color);
  }
  
  private void setLineThicknessJWS(double thickness) {
	 //  offscreenGraphics.setS  HOW TO DO?
  }

  private void setFontJWS(Font font) {
	  offscreenGraphics.setFont(font);
  }

 void writeMessageOnPlayingField(Font fontToUse, String s1, String s2)
  { FontMetrics fontMetrics = offscreenGraphics.getFontMetrics(fontToUse);

    setColorJWS(wallColor);
    setFont(fontToUse);
    drawStringJWS(s1, (dimensionOfPlayingField.width - fontMetrics.stringWidth(s1)) / 2, dimensionOfPlayingField.height / 2 - 15);
    drawStringJWS(s2, (dimensionOfPlayingField.width - fontMetrics.stringWidth(s1)) / 2, dimensionOfPlayingField.height / 2 + 20);
  }

  // What position is the middle of the playing field?
  private Position centerPosition()
  {
    centerPosition.x = dimensionOfPlayingField.width  / 2;
    centerPosition.y = dimensionOfPlayingField.height / 2;

    return centerPosition;
  }

  private Position createPointInsideBox(Rectangle box, Rectangle avoid)
  { int layoutTries = 0;

    do
    {
      tempPosition.x = (int)(Math.random() * dimensionOfPlayingField.width);
      tempPosition.y = (int)(Math.random() * dimensionOfPlayingField.height);
    }
    // Be safe and make sure inside the outerBox (for safety) as well as the inner.
    // If avoid != null, don't place inside it.
    while ((!box.contains(tempPosition.x, tempPosition.y) && ++layoutTries < maxLayoutTries) ||
           (avoid != null && avoid.contains(tempPosition.x, tempPosition.y)) ||
           !outerBoxInner.inside(tempPosition));

    return tempPosition;
  }

  void pause()
  {
    if (debugging) Utils.println("pausing");
    paused = true;
    redisplay(true); // Draw the score boards.
  }

  void resume()
  {
    if (debugging) Utils.println("resuming");
    paused = false;
  }

  // Tell all the players they can process the updated sensors.
  void prepareForNextCycle(int currentTime)
  {
    if (singleStepping && debugging) Utils.println("Preparing for time = " + currentTime);
    synchronized (synchForPlayerCheckIn)
    {
      if (NannonGUI.reportSynchs) NannonGUI.developerLabel2.setText(Thread.currentThread().getName() + " in preparForNextCycle()");
      playersResumed = 0;
      playersReadyToGo = 0;
      if (NannonGUI.reportSynchs) NannonGUI.developerLabel2.setText("");
    }
    if (singleStepping && debugging) Utils.println("Done preparing for the next cycle");
  }

  // Keep track of players saying they are done computing their next action.
  // When all have reported, resume the manager.
  void readyToGo(int playerID)
  {

    // Do this as late as possible, since it might reduce odds of the odd thread deaths occuring.
    synchronized (synchForPlayerCheckIn)
    {
      if (NannonGUI.reportSynchs) NannonGUI.developerLabel2.setText(Thread.currentThread().getName() + " in readyToGo(" + playerID + ")");
      playersReadyToGo++; 
      if (NannonGUI.reportSynchs) NannonGUI.developerLabel2.setText("");
    }
    // Should directly interrupt the Manager thread, but this doesn't
    // seem to be working in Java 1.0.2 - so the Manager periodically awakes and
    // checks if all the players are ready.
  }

  boolean allPlayersReadyToGo()
  { int a, b;

    synchronized (synchForPlayerCheckIn)
    {
      if (NannonGUI.reportSynchs) NannonGUI.developerLabel2.setText(Thread.currentThread().getName() + " in allPlayersReadyToGo()");
      a = playersReadyToGo;
      b = playersResumed;
      if (NannonGUI.reportSynchs) NannonGUI.developerLabel2.setText("");
    }
    return (a >= b);
  }

  // It is possible that all the players are ready to go before
  // the manager even attempts to sleep, so the manager uses this method
  // to decide to go to sleep.
  boolean shouldManagerSleep()
  {
    return (!allPlayersReadyToGo());
  }

  void setClockPeriod(int msecs)
  {
    clockPeriod = msecs; // Need to save in case manager dies.
  //  if (managerThread != null) managerThread.setClockPeriod(msecs);
  }
/*
  int getCurrentTime()
  {
    if (managerThread != null)
    {
      lastMeasuredTime = managerThread.currentTime;
      return lastMeasuredTime; // Need in case manager needs to be restarted.
    }
    else return 0;
    
    
    
  
  	private int radius = 15, diameter = 2 * radius, outerDiameter = diameter + 5, x_firstCell = 150, y_firstCell,
  			  x_SafeO_base, y_SafeO_base, x_HomeX_base, y_HomeX_base, x_HomeO_base, y_HomeO_base, x_SafeX_base, y_SafeX_base;
    
  }
 */
  
  private double distanceSq(int X1, int Y1, int X2, int Y2) {
	  return (X1 - X2) * (X1 - X2) + (Y1 - Y2) * (Y1 - Y2);
  }
  
  private final int downPress = 1, release = 2, click = 3;
  
  private boolean[] highlightFrom = new boolean[NannonGameBoard.getCellsOnBoard() + 1];
  private boolean[] highlightTo   = new boolean[NannonGameBoard.getCellsOnBoard() + 1];
  
  private void reportPieceAtXandY(int x, int y, int status) {
	  for (int i = 0; i <= numbCells; i++) { highlightFrom[i] = false; }
	  for (int i = 0; i <= NannonGameBoard.getCellsOnBoard(); i++) { highlightTo[  i] = false; }
	  if (status == release || (nannonGUI.playingAgainstHumanPlayer && !nannonGUI.waitingForUser))   { repaint(); return; }
	    
	  double rSq    = radius * radius;
/*	  
	  if (distanceSq(x, y, x_SafeO_base + radius, y_SafeO_base + radius) < rSq) {
		  Utils.println("In SAFE(O)!");
	  } else
	  if (distanceSq(x, y, x_SafeX_base + radius, y_SafeX_base + radius) < rSq) {
		  Utils.println("In SAFE(X)!");
	  } else
*/
	  if (!nannonGUI.currentPlayerIsX && nannonGUI.moveable[numbCells] && distanceSq(x, y, round(x_HomeO_base + radius), round(y_HomeO_base + radius - outerDiameter * (nannonGUI.homePieces_playerO - 1))) < rSq) {
		  if      (status == downPress) { highlightFrom[numbCells] = true; highlightTo[numbCells - nannonGUI.die_playerO] = true; }
		  else if (status == click)     { nannonGUI.chosenFrom = numbCells; nannonGUI.chosenTo = numbCells - nannonGUI.die_playerO; nannonGUI.waitingForUser = false; }
	  } else
	  if ( nannonGUI.currentPlayerIsX && nannonGUI.moveable[numbCells] && distanceSq(x, y, round(x_HomeX_base + radius), round(y_HomeX_base + radius - outerDiameter * (nannonGUI.homePieces_playerX - 1))) < rSq) {
		  if      (status == downPress) { highlightFrom[numbCells] = true; highlightTo[nannonGUI.die_playerX - 1] = true;}
		  else if (status == click)     { nannonGUI.chosenFrom = numbCells; nannonGUI.chosenTo = nannonGUI.die_playerX - 1; nannonGUI.waitingForUser = false;  }
	  } else
	  {
		  for (int i = 0; i < numbCells; i++) if (nannonGUI.moveable[i]) {
			  if (distanceSq(x, y, round(x_firstCell + radius + outerDiameter * i), round(y_firstCell + radius)) < rSq) {
				  int chosenTo = keepInRange(numbCells, nannonGUI.currentPlayerIsX ? i + nannonGUI.die_playerX : i - nannonGUI.die_playerO);
				  if      (status == downPress) { 
					  highlightFrom[i]      = true; 
				  	  highlightTo[chosenTo] = true; 
				  } else if (status == click) { nannonGUI.chosenFrom = i; nannonGUI.chosenTo = chosenTo; nannonGUI.waitingForUser = false; }
				  break;
			  }
		  }
	  }	  
	  repaint();
  }
  
  private int keepInRange(int cells, int v) { // See if moved to HOME.
	  if (v < 0 || v > cells) { return cells; }
	  return v;
  }
  
  public void mouseMoved(MouseEvent   event) {}
  public void mouseClicked(MouseEvent event) {
	int x = event.getX(), y = event.getY(); 
	// Utils.println("Mouse clicked at: x = " + x + " and y = " + y);
	reportPieceAtXandY(x, y, click);
  }
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent  event) {}

  public void mousePressed(MouseEvent event)
  { int x = event.getX(), y = event.getY();

  //	Utils.println("Mouse pressed at: x = " + x + " and y = " + y); 
  	reportPieceAtXandY(x, y, downPress);
  
    if (showHelp)
    {
      showHelp = false;
      resume();
      repaint();
    }

  }

  public void mouseDragged(MouseEvent event)
  { /*int x = event.getX(), y = event.getY(); 

    if (started)
    {
      mouseDownAt.x = x;
      mouseDownAt.y = y;
      mouseDown     = true;
    }
    */
  }
  
  public void mouseReleased(MouseEvent event)
  { int x = event.getX(), y = event.getY();
  
    reportPieceAtXandY(x, y, release);
    
	//Utils.println("Mouse released at: x = " + x + " and y = " + y);
    if (started)
    {
      mouseUpAt.x = x;
      mouseUpAt.y = y;
      mouseDown   = false;
    }
    else if (entityGrabbed != null) // Release the grabbed object (if any).
    { int layoutTrials = 0;

      x = Math.max(outerBoxInner.x, Math.min(x, outerBoxInner.lastX));
      y = Math.max(outerBoxInner.y, Math.min(y, outerBoxInner.lastY));

      entityGrabbed.directlyMoveTo(x, y);
      while (intersectingEntity(entityGrabbed) != null && layoutTrials < 1000)
      { int newX, newY;
        
        // Find some nearby free space by doing a random walk of ever-increasing step sizes.
        layoutTrials++;
        // Need to keep on board. Also, always walk from initial location.
        do
        {
          newX = x + Utils.randomInInterval(-layoutTrials, layoutTrials);
          newY = y + Utils.randomInInterval(-layoutTrials, layoutTrials);
        }
        while (!outerBoxInner.contains(newX, newY));
        entityGrabbed.directlyMoveTo(newX, newY);
      }
      entityGrabbed.setGrabbed(false);
      entityGrabbed = null;
      redisplay(true);
    }
  }
  
  private Object intersectingEntity(Entity entityGrabbed2) {
	Utils.waitForEnter("Write me!");
	return null;
  }

  Position getLastMouseDownPosition()
  {
    return mouseDownAt;
  }
  
  Position getLastMouseUpPosition()
  {
    return mouseUpAt;
  }

  boolean isMouseDown()
  {
    return mouseDown;
  }
  
  int    square(int    i) { return i * i; }
  double square(double x) { return x * x; }

  boolean pointInsideEntity(int x, int y, Entity e)
  { Position pos = e.getPosition();

    return square(pos.x - x) + square(pos.y - y) <= objectSize;
  }

  // Return first entity that intersects this location.
  Entity getEntityAtThisLocation(int x, int y)
  { 
/*
    for(int i = 0; i < playersSoFar;  i++)   if (player[i].exists(0))
    { 
      if (pointInsideEntity(x, y, player[i]))    return player[i];
    }
*/
    return null; // Nothing here.
  }


}//
//
//Various miscellaneous supporting classes - taken from the AgentWorld (another cs540 testbed) and modified for Nannon.
//
//
//

class Entity
{

	public Position getPosition() {
		return null;
	}

	public void directlyMoveTo(int newX, int newY) {		
	}

	public void setGrabbed(boolean b) {

	}

}

class FastRectangle extends Rectangle // Store lower-right corner for faster computation.
{ 
	private static final long serialVersionUID = 1L;
	int lastX, lastY;

	public boolean inside(Position p)
	{
		return (p.x >= x && p.x <= lastX && p.y >= y && p.y <= lastY);
	}

}

class Position extends Point
{	
	private static final long serialVersionUID = 1L;

	Position(int x, int y)
	{
		super(x, y);
	}

	public String toString()
	{
		return x + "," + y;
	}
}


class WatcherThread extends Thread
{ private PlayingField playingField;
private int sleepPeriod = 5 * 60 * 1000; // Every 5 mins check if the managerThread has progressed.

WatcherThread(PlayingField playingField)
{
	this.playingField  = playingField;
}

public void run()
{
	while (isAlive() && (playingField.gamesToPlay < 0 || playingField.gamesPlayed < playingField.gamesToPlay))
	{
		try
		{
			sleep(sleepPeriod);
		}
		catch(InterruptedException e) { } // Just continue if interrupted.
	}
}
}

