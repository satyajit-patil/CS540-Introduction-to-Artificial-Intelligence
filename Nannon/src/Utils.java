/*
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

/*
 * These are various files I took from my Utils.java file used for research projects.  There is no need to read this file nor understand the methods.  
 * Most of of the time, the method names 'self document' the methods.
 * 
 * You might, though, want to try various 'seeds' for the random-number generator in order to get different results.
 * 
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class Utils {
	
	// You don't need to understand these 'helper' functions.  Hopefully the method names are 'self documenting.'  Most are from my Utils.java file for research projects.
	
	// One thing to note is that things printed via Utils.print are printed to BOTH the screen and a file, called a 'dribble file.'
	// Another thing to note is the method randomInstance below; you can vary your runs by changing its value.
	
	// Changing these will lead to different results (with the SAME 'seed' will get the SAME results every run, if nothing else changes in the code).
	public static Random randomInstance = new Random(540); // new Random(12345678);  // new Random(248369); // new Random(135792468);

	public  static boolean        runningInCondor  = false; // This gets set to true when running the class tourney in Condor, since there we don't want to hang awaiting user input.
	private static BufferedReader inBufferedReader = null;

	// This is used for debugging Condor task assignment.
	static boolean waitEvenIfRunningInCondor = false; // When we FAKE running in Condor, we can set this to true and still treat waitHere's as waits and not exit's.
	
	public static void waitForEnter() {
		waitForEnter(null);
	}
	public static void waitForEnter(String str) {
		 println((str != null ? str + "  " : "") + "Hit ENTER to continue (or 'e' and then ENTER to see the call stack). "); 
		
		 if (runningInCondor && !waitEvenIfRunningInCondor ) { Utils.error("Since running in Condor, exiting."); }
		 try {
	        	if (inBufferedReader == null) { inBufferedReader = new BufferedReader(new InputStreamReader(System.in)); }
	        	String readThis = inBufferedReader.readLine();
	        	if (readThis != null && readThis.startsWith("e")) { // This had been 'interrupt' instead of 'error' but then these might not be immediately caught, and doing just that is the intent of an 'e' being pressed.
	        		try {
	        			throw new RuntimeException("\nYou requested the current run be interrupted by returning something that starts with 'e'.");
	        		} catch (Exception e) {
	        			reportStackTrace(e);
	        			println("\nHit the ENTER key to continue if you wish.");
	        			inBufferedReader.readLine();
	        		}
	        	}
	        } catch (IOException e) {
	            // Ignore any errors here.
	        	inBufferedReader = null;  // If something went wrong, reset the reader. 
	        	return;
	        };
	}	
	
	public static void error(String string) {
		println("\nError: " + string + "\nContinuing from an error will likely leave the 'game engine' in an inconsistent state,\nso you should consider aborting this run.");
		if (runningInCondor) { System.exit(0); }  // Had been -1 but seemed to hang Condor: "The job attribute OnExitRemove expression '( ExitBySignal == false ) && ( ExitCode == 0 )' evaluated to FALSE"  (Seems I should alter my Condor submit file to OR in ExitCode == -1, but I am not sure of the purpose of this line.)
		throw new RuntimeException();
	}
	
	public static void reportStackTrace(Throwable e) {
		StackTraceElement[] trace = e.getStackTrace();
		int traceSize = trace.length;
		int sizeToUse = Math.min(traceSize, 50); // <-------- change this if you need to see more of the stack.
		println("\nStack trace:");
		if (sizeToUse < traceSize) {
			for (int i = 0; i < sizeToUse / 2; i++) {
				println("  Element #" + (traceSize - i) + ": " + trace[i].toString());
			}
			println("% ...");
			for (int i = sizeToUse / 2; i > 0; i--) {
				println("  Element #" +              i  + ": " + trace[traceSize - i].toString());
			}
		} else {
			for (int i = 0; i < sizeToUse; i++) {
				println("  Element #" + (traceSize - i) + ": " + trace[i].toString());
				}		
			}
	}
	
	public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("H:mm:ss M/d/yy"); //"yyyy/MM/dd HH:mm:ss"
        Date       date       = new Date();
        return dateFormat.format(date);
    }

	private static int dribbleCharCount = 0;
	public static void print(String string) {
		// By having all printing go through here, we could ALSO write a copy to a file (called a "dribble" file in AI, by some people at least):
		//   if (dribbleStream != null) { dribbleStream.print(string); }   // Use something like this (and dont forget to close the stream when no longer needed): dribbleStream = new PrintStream(outStream, false);
		System.out.print(string);
		if (dribbleStream != null) { 
			dribbleCharCount += string.length();
			dribbleStream.print(string); 
			if (dribbleCharCount > (runningInCondor ? 1000000000 : 10000000)) { 
				dribbleStream.print("\n\n// DRIBBLING TERMINATED (by Utils.java) SINCE THIS FILE HAS OVER " + (runningInCondor ? "ONE BILLION" : "TEN MILLION") + " CHARACTERS IN IT.\n");
				closeDribbleFile();
			}
		} 
	}

	public static void println() {
		print("\n");
	}
	public static void println(String string) {
		print(string);
		print("\n"); // Do two calls so no time wasting concatenating strings.
	}

    public static void createDribbleFile() {
        if ( dribbleStream == null ) {
            createDribbleFile("dribble.txt");
        }
    }
    private static PrintStream dribbleStream = null; 
    public  static String    dribbleFileName = null;

    public static void createDribbleFile(String fileName) {
    	createDribbleFile(fileName, true);
    }
    public static void createDribbleFile(String fileName, boolean reportHostName) {
    	if (dribbleStream != null) { 
    		dribbleStream.println("\n\n// Closed existing dribble file due to a createDribble call with file = " + fileName);
    	}
    	closeDribbleFile();
        try {
        	ensureDirExists(fileName);
        	FileOutputStream outStream = new FileOutputStream(fileName);
            dribbleStream = new PrintStream(outStream, false); // No auto-flush (can slow down code).
            dribbleFileName = fileName;
            if (reportHostName) println("% Running on host: " + getHostName());
        } catch (FileNotFoundException e) {
        	reportStackTrace(e);
            error("Unable to successfully open this file for writing:\n " + fileName + ".\nError message: " + e.getMessage());
        }
    }
    
    public static void closeDribbleFile() {
    	dribbleFileName = null;
    	if (dribbleStream == null) { return; }
    	dribbleStream.close();
    	dribbleStream = null;
    }
    
    public static File ensureDirExists(String file) {
    	if (file == null) { return null; }
    	if (file.endsWith("/") || file.endsWith("\\")) { file += "dummy.txt"; } // A hack to deal with directories being passed in.
		File f = new File(file);

    	String parentName = f.getParent();
    	File   parentDir  = (parentName == null ? null : f.getParentFile());
		if (parentDir != null) { 
			if (!parentDir.exists() && !parentDir.mkdirs()) { // Be careful to not make the file into a directory.
				waitForEnter("Unable to create (sometimes these are intermittent; will try again)\n   file      = " + file +
																						    "\n   parentDir = " + parentDir);
				parentDir.mkdirs();
			}
		}
		return f;
	}	
	public static String getHostName() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostName = addr.getHostName();
			if (hostName == null) { return "unknownHost"; }
			int locFirstPeriod = hostName.indexOf('.');
			if (locFirstPeriod > 0) { // Not sure what a leading period would be, but keep it if this case ever occurs.
				return hostName.substring(0, locFirstPeriod);
			}
			return hostName;
		} catch (UnknownHostException e) {
			return "unknownHost";
		}
    }

    public static String getUserName() {
        String result = System.getProperty("user.name");
        
        if (result == null)       { result = "unknownUserName";  }
        if (result.contains(" ")) { result.replace(' ', '_');    }
        if (result.contains("@")) { result.replace("@", "_at_"); }
        // Let me (Jude) know if your user name does something weird when converted to a file name.
        return result;
    }

    public static void writeStringToFile(String stringToPrint, String fileName, boolean usePrintln) { 
        try {
        	ensureDirExists(fileName);
            FileOutputStream outStream = new FileOutputStream(new File(fileName));
            PrintStream         stream = new PrintStream(outStream);
            if (usePrintln) { stream.println(stringToPrint); } else { stream.print(stringToPrint); }
            stream.close();
        } catch (FileNotFoundException e) {
            error("Unable to successfully open this file for writing:\n " + fileName + ".\nError message:\n " + e.getMessage());
        }
    }  
    
    public static double random() {
        return randomInstance.nextDouble();
    }
	
	public static int randomInInterval(int lower, int upper) {
	    return lower + (int) Math.floor(random() * (1 + upper - lower)); // Add one so possible to get 'upper.'
	}

    public static <E> E chooseRandomElementFromThisList(List<E> list) {
    	if (list == null) { return null; }
    	int size = list.size();
    	if (size == 0) { return null; }
    	return list.get(randomInInterval(1, size) - 1);
    }	
    
	public static String[] chopCommentFromArgs(String[] args) {
	  if (args == null) { return null; }
	  int commentStart = -1;
	  for (int i = 0; i < args.length; i++) {
		  if (args[i] != null && args[i].startsWith("//") ) {
			  commentStart = i;
			  break;
		  }
	  }  
	  if (commentStart < 0) { return args; }
	  String[] newArgs = new String[commentStart];
	  for (int i = 0; i < commentStart; i++) {
		  newArgs[i] = args[i];
	  }
	  return newArgs;
	}
    
    public static String comma(int value) { // Always use separators (e.g., "100,000").
    	return String.format("%,d", value);    	
    }    
    public static String comma(long value) { // Always use separators (e.g., "100,000").
    	return String.format("%,d", value);    	
    }   
    public static String comma(double value) { // Always use separators (e.g., "100,000").
    	return String.format("%,f", value);    	
    }
    
    public static String truncate(double d) {
        return truncate(d, 1);
    }
    public static String truncate(double d, int decimals) {
    	double abs = Math.abs(d);
    	if (abs > 1e13)             { 
    		return String.format("%."  + (decimals + 4) + "g", d);
    	} else if (abs > 0 && abs < Math.pow(10, -decimals))  { 
    		return String.format("%."  +  decimals      + "g", d);
    	}
        return     String.format("%,." +  decimals      + "f", d);
    }
    public static String truncateNoSciNotation(double d, int decimals) {
        return String.format("%." + decimals + "f", d);
    }

	public static String padRight(String original, int N) {	
		int len = (original == null ? 0 : original.length());
		if (len >= N) { return original; }
		String extra = "";
		for (int i = 0; i < N - len; i++) { extra += " "; }
		return (original == null ? "" : original) + extra;
	}
	
    public static String padLeft(String value, int width) {
    	String spec = "%" + width + "s";
    	return String.format(spec, value);    	
    }
    public static String padLeft(int value, int width) {
    	String spec = "%, " + width + "d"; // Always use separators (e.g., "100,000").
    	return String.format(spec, value);    	
    }      
    public static String padLeft(long value, int width) {
    	String spec = "%, " + width + "d"; // Always use separators (e.g., "100,000").
    	return String.format(spec, value);    	
    } 
    
	private static final long millisecInMinute = 60000;
	private static final long millisecInHour   = 60 * millisecInMinute;
	private static final long millisecInDay    = 24 * millisecInHour;
	public static String convertMillisecondsToTimeSpan(long millisec) {
		return convertMillisecondsToTimeSpan(millisec, 0);
	}
	public static String convertMillisecondsToTimeSpan(long millisec, int digits) {
		if (millisec ==    0) { return "0 seconds"; } // Handle these cases this way rather than saying "0 milliseconds."
		if (millisec <  1000) { return comma(millisec) + " milliseconds"; } // Or just comment out these two lines?
		if (millisec > millisecInDay)    { return comma(millisec / millisecInDay)    + " days and "    + convertMillisecondsToTimeSpan(millisec % millisecInDay,    digits); }
		if (millisec > millisecInHour)   { return comma(millisec / millisecInHour)   + " hours and "   + convertMillisecondsToTimeSpan(millisec % millisecInHour,   digits); }
		if (millisec > millisecInMinute) { return comma(millisec / millisecInMinute) + " minutes and " + convertMillisecondsToTimeSpan(millisec % millisecInMinute, digits); }
		
		return truncate(millisec / 1000.0, digits) + " seconds"; 
	}
	
	public static String converStringListToString(String[] strings) {
		if (strings == null) { return ""; }
		
		StringBuffer sb = new StringBuffer(64);
		
		for (String str : strings) { sb.append(str); sb.append(" "); }
		return sb.toString();
	}

	
	public static Boolean fileExists(String fileName) {
		return ((new File(fileName)).exists());
	}
    
	public static String readFileAsString(String fileName) throws IOException {
		if (fileName.endsWith(".gz")) { // BUGGY if caller asks for *.gz file but really wanted the newer one if both * and *.gz exist.
			Utils.error("This code cannot read compressed files.");
		} else if (fileExists(fileName + ".gz")) {
			if (!fileExists(fileName)) {
				Utils.error("This code cannot read compressed files: " + fileName + ".gz");
			}
		}
	    return readFileAsString(new File(fileName));
	}

	public static String readFileAsString(File file) throws IOException {
	    byte[] buffer = new byte[(int) file.length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(file));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}


}
