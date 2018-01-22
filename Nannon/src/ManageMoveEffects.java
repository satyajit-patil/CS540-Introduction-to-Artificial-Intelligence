/*
 * Copyrighted 2013 by Jude Shavlik.  Maybe be freely used for non-profit educational purposes.
 */

public class ManageMoveEffects {
	// A 'prime' is when two or more pieces from the same player are adjacent - such pieces are safe and cannot be knocked back to HOME.
	// These are only for legal moves, so cannot break the OPPONENT's prime.
	//
	//   HITting an opponent means you land on its location.
	//   BREAKing a prime means that a move moves one of two adjacent pieces, eg going from board _XX_OOO to board _X_X_000 with die=1.
	//   EXTENDING a prime means going from a prime of length N to one of N+1, eg XX_OOO -> XXXOOO with die=3.
	//   CREATING a prime means moving a piece next to an isolated piece, eg X_XOOO -> _XXOOO with die=1. 
	//      So we have these 4 boolean: HIT/BREAK/EXTEND/CREATE, which would produce 16 possibilities.
	//      However cannot both EXTEND and CREATE a prime, by definition, so only 12 possibilities.
	//   The names below state which of these 4 booleans are true (other than 'NON_DESCRIPT_MOVE', which says NONE are true) 
	//
	//   Here are examples of all 12 effects (in all cases X is moved and never from HOME):
	//
	//       NON_DESCRIPT_MOVE                                   X_____ -> _X____
	//       MOVE_HITS_OPPONENT                                  XO____ -> _X____
	//       MOVE_BREAKS_PRIME                                   XX____ -> X_X___
	//       MOVE_EXTENDS_PRIME                                  X_XX__ -> _XXX__
	//       MOVE_CREATES_PRIME                                  X_X___ -> _XX___
	//       MOVE_BREAKS_CREATES_PRIME                           XX_X__ -> X_XX__
	//       MOVE_BREAKS_PRIME_AND_HITS_OPPONENT                 XX___O -> X____X
	//       MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT                X__XXO -> ___XXX
	//       MOVE_CREATES_PRIME_AND_HITS_OPPONENT                X___XO -> ____XX
	//       MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT         XX_XO_ -> X__XX_
	//       MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT       XX_XXO_O -> X__XXX_O // This cannot happen on a board with only 6 locations since two primes needed.
	//       MOVE_BREAKS_EXTENDS_PRIME                         XX_XXOOO -> X_XXXOOO // Ditto.
	
	public final static int NON_DESCRIPT_MOVE                           =  0; // These need to have different values, but ok if they conflict with other defined constants.
	public final static int MOVE_HITS_OPPONENT                          =  1; 
	public final static int MOVE_BREAKS_PRIME                           =  2;
	public final static int MOVE_EXTENDS_PRIME                          =  3;
	public final static int MOVE_CREATES_PRIME                          =  4;
	public final static int MOVE_BREAKS_EXTENDS_PRIME                   =  5;
	public final static int MOVE_BREAKS_CREATES_PRIME                   =  6;
	public final static int MOVE_BREAKS_PRIME_AND_HITS_OPPONENT         =  7;
	public final static int MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT        =  8;
	public final static int MOVE_CREATES_PRIME_AND_HITS_OPPONENT        =  9;
	public final static int MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT = 10;
	public final static int MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT = 11;
	public final static int COUNT_OF_MOVE_DESCRIPTORS                   = 12; // This one, though, needs to be a count of the above (starting from 0).
	
	public static boolean[] convertEffectToFourBooleans(int effect) {
		boolean[] results = new boolean[4];
		
		results[0] = isaHit(      effect);
		results[1] = breaksPrime( effect);
		results[2] = extendsPrime(effect);
		results[3] = createsPrime(effect);
		
		return results;
	}
	
	public static boolean isaHit(int i) {
		switch (i) {
		case NON_DESCRIPT_MOVE:                           return false;
		case MOVE_HITS_OPPONENT:                          return true;
		case MOVE_BREAKS_PRIME:                           return false;
		case MOVE_EXTENDS_PRIME:                          return false;
		case MOVE_CREATES_PRIME:                          return false;
		case MOVE_BREAKS_EXTENDS_PRIME:                   return false;
		case MOVE_BREAKS_CREATES_PRIME:                   return false;
		case MOVE_BREAKS_PRIME_AND_HITS_OPPONENT:         return true;
		case MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT:        return true;
		case MOVE_CREATES_PRIME_AND_HITS_OPPONENT:        return true;
		case MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT: return true;
		case MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT: return true;
		}
		Utils.error("Should not happen in isaHit: " + i);
		return false;
	}
	
	public static boolean breaksPrime(int i) {
		switch (i) {
		case NON_DESCRIPT_MOVE:                           return false;
		case MOVE_HITS_OPPONENT:                          return false;
		case MOVE_BREAKS_PRIME:                           return true;
		case MOVE_EXTENDS_PRIME:                          return false;
		case MOVE_CREATES_PRIME:                          return false;
		case MOVE_BREAKS_EXTENDS_PRIME:                   return true;
		case MOVE_BREAKS_CREATES_PRIME:                   return true;
		case MOVE_BREAKS_PRIME_AND_HITS_OPPONENT:         return true;
		case MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT:        return false;
		case MOVE_CREATES_PRIME_AND_HITS_OPPONENT:        return false;
		case MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT: return true;
		case MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT: return true;
		}
		Utils.error("Should not happen in breaksPrime: " + i);
		return false;		
	}
	
	public static boolean extendsPrime(int i) {
		switch (i) {
		case NON_DESCRIPT_MOVE:                           return false;
		case MOVE_HITS_OPPONENT:                          return false;
		case MOVE_BREAKS_PRIME:                           return false;
		case MOVE_EXTENDS_PRIME:                          return true;
		case MOVE_CREATES_PRIME:                          return false;
		case MOVE_BREAKS_EXTENDS_PRIME:                   return true;
		case MOVE_BREAKS_CREATES_PRIME:                   return false;
		case MOVE_BREAKS_PRIME_AND_HITS_OPPONENT:         return false;
		case MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT:        return true;
		case MOVE_CREATES_PRIME_AND_HITS_OPPONENT:        return false;
		case MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT: return true;
		case MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT: return false;
		}
		Utils.error("Should not happen in breaksPrime: " + i);
		return false;		
	}
	
	public static boolean createsPrime(int i) {
		switch (i) {
		case NON_DESCRIPT_MOVE:                           return false;
		case MOVE_HITS_OPPONENT:                          return false;
		case MOVE_BREAKS_PRIME:                           return false;
		case MOVE_EXTENDS_PRIME:                          return false;
		case MOVE_CREATES_PRIME:                          return true;
		case MOVE_BREAKS_EXTENDS_PRIME:                   return false;
		case MOVE_BREAKS_CREATES_PRIME:                   return true;
		case MOVE_BREAKS_PRIME_AND_HITS_OPPONENT:         return false;
		case MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT:        return false;
		case MOVE_CREATES_PRIME_AND_HITS_OPPONENT:        return true;
		case MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT: return false;
		case MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT: return true;
		}
		Utils.error("Should not happen in createsPrime: " + i);
		return false;		
	}

	public static String convertMoveEffectToString(int i) {
		switch (i) {
		case NON_DESCRIPT_MOVE:                           return "";
		case MOVE_HITS_OPPONENT:                          return ", which hits an opponent";
		case MOVE_BREAKS_PRIME:                           return ", which breaks up a prime";
		case MOVE_EXTENDS_PRIME:                          return ", which extends a prime";
		case MOVE_CREATES_PRIME:                          return ", which creates a prime";
		case MOVE_BREAKS_EXTENDS_PRIME:                   return ", which extends a prime but also breaks up a prime";
		case MOVE_BREAKS_CREATES_PRIME:                   return ", which creates a prime but also breaks up a prime";
		case MOVE_BREAKS_PRIME_AND_HITS_OPPONENT:         return ", which hits an opponent but breaks up a prime";
		case MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT:        return ", which hits an opponent and extends a prime";
		case MOVE_CREATES_PRIME_AND_HITS_OPPONENT:        return ", which hits an opponent and creates a prime";
		case MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT: return ", which hits an opponent and extends a prime but also breaks up a prime";
		case MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT: return ", which hits an opponent and creates a prime but also breaks up a prime";
		}
		Utils.error("Should not happen in convertMoveImpactToString:" + i);
		return "?";
	}

	public static String convertMoveEffectToShortString(int i) {
		switch (i) {
		case NON_DESCRIPT_MOVE:                           return "";
		case MOVE_HITS_OPPONENT:                          return "; hits opp";
		case MOVE_BREAKS_PRIME:                           return "; breaks prime";
		case MOVE_EXTENDS_PRIME:                          return "; extends prime";
		case MOVE_CREATES_PRIME:                          return "; creates prime";
		case MOVE_BREAKS_EXTENDS_PRIME:                   return "; breaks+extends prime";
		case MOVE_BREAKS_CREATES_PRIME:                   return "; breaks+creates prime";
		case MOVE_BREAKS_PRIME_AND_HITS_OPPONENT:         return "; hits opp, breaks prime";
		case MOVE_EXTENDS_PRIME_AND_HITS_OPPONENT:        return "; hits opp, extends prime";
		case MOVE_CREATES_PRIME_AND_HITS_OPPONENT:        return "; hits opp, creates prime";
		case MOVE_BREAKS_EXTENDS_PRIME_AND_HITS_OPPONENT: return "; hits opp, breaks+extends prime";
		case MOVE_BREAKS_CREATES_PRIME_AND_HITS_OPPONENT: return "; hits opp, breaks+creates prime";
		}
		Utils.error("Should not happen in convertMoveImpactToString:" + i);
		return "?";
	}
}
