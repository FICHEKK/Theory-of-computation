import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A simple class created to simulate the workings
 * of the epsilon non-deterministic finite automata.
 * 
 * @author Filip Nemec
 */
public class SimEnka {
	
	/** All the sequences that will be tested on this NKA. */
	private String[] inputSequences;
	
	/** All the possible states that this automata can be in. */
	private Set<String> possibleStates = new HashSet<>();
	
	/** All the possible symbols in the alphabet. */
	private Set<String> alphabet = new HashSet<>();
	
	/** All the states that are will be accepted after finishing automata. */
	private Set<String> acceptableStates = new HashSet<>();
	
	/** The starting state for this automata. */
	private String initialState;
	
	/** Set of states that this automata is currently in. */
	private TreeSet<String> currentStates = new TreeSet<String>();
	
	/** Defines the next state(s) for certain state-symbol pair. */
	private Map<StateSymbolPair, TreeSet<String>> transitionFunction = new TreeMap<>();
	
	/**
	 * Initializes this epsilon-NFA.
	 */
	private void initialize() {
		try(Scanner scanner = new Scanner(System.in)) {
			inputSequences = scanner.nextLine().trim().split("\\|");
			possibleStates.	 addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			alphabet.		 addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			acceptableStates.addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			initialState = scanner.nextLine().trim();
			currentStates.add(initialState);
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				
				if(line.trim().isEmpty()) break;
				
				String[] transition = line.split("->");
				String[] stateSymbolPair = transition[0].split(",");
				String[] nextStates = transition[1].split(",");
				
				StateSymbolPair pair = new StateSymbolPair(stateSymbolPair[0], stateSymbolPair[1]);
				TreeSet<String> set = new TreeSet<String>();
				set.addAll(Arrays.asList(nextStates));
				
				transitionFunction.put(pair, set);
			}
		}
	}
	
	/**
	 * Processes each provided input sequence.
	 */
	public void process() {
		for(String sequence : inputSequences) {
			processSequence(sequence);
			reset();
		}
	}

	/**
	 * Processes the given sequence of symbols.
	 * @param sequence the sequence of symbols separated by ','
	 */
	private void processSequence(String sequence) {
		processEpsilonTransitions();
		printCurrentStates();
		System.out.print("|");
		
		String[] inputSymbols = sequence.split(",");
		
		for(int i = 0; i < inputSymbols.length; i++) {
			processSymbol(inputSymbols[i]);
			processEpsilonTransitions();
			printCurrentStates();
			
			if(i != inputSymbols.length - 1) {
				System.out.print("|");
			}
		}
	}
	
	/**	
	 * Processes all of the epsilon transitions for all of the current states.
	 * If new epsilon states are found, this method will recursively call itself
	 * on the new set of those transitioned states and check for new epsilon
	 * transitions.
	 */
	private void processEpsilonTransitions() {
		TreeSet<String> transitionedStates = new TreeSet<String>();
		
		// We need a copy because we are modifying the original tree.
		TreeSet<String> currentStatesCopy = new TreeSet<String>(currentStates);
		
		for(String state : currentStatesCopy) {
			TreeSet<String> nextStates = transitionFunction.get(new StateSymbolPair(state, "$"));
			
			if(nextStates != null) {
				if(nextStates.first().equals("#")) continue;
				
				for(String s : nextStates) {
					if(!currentStates.contains(s)) {
						currentStates.add(s);
						transitionedStates.add(s);
					}
				}
			}
		}
		
		if(transitionedStates.size() > 0) {
			processEpsilonTransitions();
		}
	}

	/**
	 * Processes all the current states with the given symbol.
	 * @param symbol the symbol for processing all the current states
	 */
	private void processSymbol(String symbol) {
		TreeSet<String> nextCurrentStates = new TreeSet<String>();
		
		for(String state : currentStates) {
			StateSymbolPair pair = new StateSymbolPair(state, symbol);
			TreeSet<String> nextStates = transitionFunction.get(pair);
			
			if(nextStates == null || nextStates.first().equals("#")) continue;
			
			for(String nextState : nextStates) {
				nextCurrentStates.add(nextState);
			}
		}
		
		if(nextCurrentStates.isEmpty()) {
			nextCurrentStates.add("#");
		}
		
		currentStates = nextCurrentStates;
	}
	
	/**
	 * Prints all the current states in the following format:
	 * <br>s1,s2,s3,s4</br>
	 */
	private void printCurrentStates() {
		Iterator<String> iterator = currentStates.iterator();
	    while (iterator.hasNext()) {
	        String state = iterator.next();
	        System.out.print(state);
	        
	        if (iterator.hasNext()) {
	        	System.out.print(",");
	        }
	     }
	}
	
	/**
	 * Resets this automata to the default settings.
	 */
	private void reset() {
		currentStates.clear();
		currentStates.add(initialState);
		System.out.print("\n");
	}
	
	/**
	 * Defines a pair (tuple) of state and symbol (state, symbol).
	 * 
	 * @author Filip Nemec
	 */
	private static class StateSymbolPair implements Comparable<StateSymbolPair> {
		
		/** The state. */
		private String state;
		
		/** The symbol. */
		private String symbol;
		
		/**
		 * Constructs a new state-symbol pair.
		 * 
		 * @param state the state
		 * @param symbol the symbol
		 */
		public StateSymbolPair(String state, String symbol) {
			this.state = state;
			this.symbol = symbol;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(state, symbol);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof StateSymbolPair))
				return false;
			StateSymbolPair other = (StateSymbolPair) obj;
			return Objects.equals(state, other.state) && Objects.equals(symbol, other.symbol);
		}
		
		@Override
		public String toString() {
			return "(" + state + ", " + symbol + ")";
		}

		@Override
		public int compareTo(StateSymbolPair o) {
			int r = state.compareTo(o.state);
			
			if(r != 0) return r;
			
			return symbol.compareTo(o.symbol);
		}
	}
	
	/**
	 * Simulation starts from here. User is expected to
	 * give the definition of the e-NFA in order for this
	 * simulation to work. Format is as follows:
	 * <br>
	 * <b>IMPORTANT:</b> The definition should not have any spaces between symbols!
	 * <p>
	 * <br> 1. row: input strings divided by "|"
	 * <br> 2. row: list of all states separated by ","
	 * <br> 3. row: list of all symbols in the alphabet separated by ","
	 * <br> 4. row: list of all the final states
	 * <br> 5. row: the initial state
	 * <br> 6. row and later: the transition function of format "state,symbol->state"
	 * <br>
	 * <br> As this is a non-deterministic automata, one state can lead to multiple
	 * <br> next states (set of states). Empty set is defined by a "#". Epsilon
	 * <br> symbol is defined by a "$".
	 * <br>
	 * <br> Example of a valid definition:
	 * <br> 
	 * <br> a,b,a
	 * <br> s1,s2,s3,s4,s5,s6,s7
	 * <br> a,b,c,d
	 * <br> s2,s5
	 * <br> s1
	 * <br> s1,$->s3,s6,s7
	 * <br> s1,a->s1,s3
	 * <br> s1,b->s5
	 * <br> s1,c->s3
	 * <br> s1,d->s3
	 * <br> s2,$->s2
	 * <br> s2,a->s3
	 * <br> s2,b->s1,s5
	 * <br> s2,c->s2,s2
	 * <br> s2,d->s4
	 * <br> s3,$->s3
	 * <br> s3,a->#
	 * <br> s3,b->s2,s3
	 * <br> s3,c->s4,s5
	 * <br> s3,d->s1
	 * <br> s4,a->s4
	 * <br> s4,b->s1,s4
	 * <br> s4,c->s1,s5
	 * <br> s5,a->s1
	 * <br> s5,b->s5
	 * 
	 * @param args none are used
	 */
	public static void main(String[] args) {
		SimEnka nka = new SimEnka();
		
		nka.initialize();
		nka.process();
	}
}
