import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class that represents a deterministic finite automata
 * and has built-in algorithm for minimizing it.
 * <p>
 * It uses the equivalence and divide method for achieving
 * the minimal possible automata.
 * 
 * @author Filip Nemec
 */
public class MinDka {
	
	/** All the possible states that this automata can be in. */
	private Set<String> states = new TreeSet<>();
	
	/** All the possible symbols in the alphabet. */
	private Set<String> alphabet = new TreeSet<>();
	
	/** Set of all the final (accepted) states. */
	private Set<String> finalStates = new TreeSet<>();
	
	/** The starting state for this automata. */
	private String initialState;
	
	/** Defines the next state for certain state-symbol pair. */
	private Map<StateSymbolPair, String> transitionFunction = new HashMap<>();
	
	/**
	 * Initializes this deterministic finite automata.
	 */
	public void initialize() {
		try(Scanner scanner = new Scanner(System.in)) {
			states.	 	addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			alphabet.	addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			finalStates.addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			initialState = scanner.nextLine().trim();
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				
				if(line.isEmpty()) break;
				
				String[] transition = line.split("->");
				String[] stateSymbolPair = transition[0].split(",");
				String nextState = transition[1];
				
				StateSymbolPair pair = new StateSymbolPair(stateSymbolPair[0], stateSymbolPair[1]);
				
				transitionFunction.put(pair, nextState);
			}
		}
	}
	
	/**
	 * Minimizes this deterministic finite automata using
	 * the dividing method.
	 */
	public void minimize() {
		findAllReachableStates();
		
		Set<String> nonAccepted = new TreeSet<>();
		Set<String> accepted = new TreeSet<>();
		
		states.forEach(state -> {
			if(finalStates.contains(state)) {
				accepted.add(state);
			} else {
				nonAccepted.add(state);
			}
		});
		
		List<Set<String>> allPartitions = new LinkedList<Set<String>>();
		allPartitions.add(accepted);
		allPartitions.add(nonAccepted);

		List<Set<String>> equivalentStates = minimizeRecursive(allPartitions);
		Map<String, String> excessStateToFirst = new HashMap<>();
		
		// Remove all excess states.
		for(Set<String> sameStateSet : equivalentStates) {
			Iterator<String> iter = sameStateSet.iterator();
			String first = iter.next();
			
			while(iter.hasNext()) {
				String excessState = iter.next();
				
				excessStateToFirst.put(excessState, first);
				states.remove(excessState);
			}
		}
		
		// Remove all transitions to the excess states.
		for(String state : states) {
			for(String symbol : alphabet) {
				StateSymbolPair pair = new StateSymbolPair(state, symbol);
				String nextState = transitionFunction.get(pair);
				
				if(!states.contains(nextState)) {
					transitionFunction.put(pair, excessStateToFirst.get(nextState));
				}
			}
		}
		
		// Remove all excess final states.
		Iterator<String> iter = finalStates.iterator();
		while(iter.hasNext()) {
			String state = iter.next();
			if(!states.contains(state)) {
				iter.remove();
			}
		}
		
		// Checking if the initial state is excess.
		// If yes, we need to change initial state to replacing state.
		if(!states.contains(initialState)) {
			initialState = excessStateToFirst.get(initialState);
		}
	}
	
	/**
	 * Prints the current definition of this DFA. "Current"
	 * because the definition can change if {@linkplain #minimize()}
	 * method is called upon this DFA.
	 */
	public void printDefinition() {
		printSet(states);
		printSet(alphabet);
		printSet(finalStates);
		System.out.println(initialState);
		printTransitions();
	}
	
	/**
	 * Prints all of the transitions that this DFA has.
	 */
	private void printTransitions() {
		for(String state : states) {
			for(String symbol : alphabet) {
				StateSymbolPair pair = new StateSymbolPair(state, symbol);
				String nextState = transitionFunction.get(pair);
				System.out.println(state + "," + symbol + "->" + nextState);
			}
		}
	}
	
	/**
	 * Helping method used to print the given {@code String} set.
	 * 
	 * @param set the set to be printed
	 */
	private void printSet(Set<String> set) {
		if(set.isEmpty()) {
			System.out.println();
			return;
		}
		
		Iterator<String> iter = set.iterator();
		while(true) {
			System.out.print(iter.next());
			
			if(!iter.hasNext()) {
				System.out.println();
				break;
			}
			System.out.print(",");
		}
	}
	
	/**
	 * Minimizes all of the given partitions and returns the list
	 * of sets each holding states that are equivalent one to another.
	 * 
	 * @param allPartitions all the partitions that need to be minimizes
	 * @return list of sets each holding states that are equivalent
	 */
	private List<Set<String>> minimizeRecursive(List<Set<String>> allPartitions) {
		List<Set<String>> newPartitions = new LinkedList<Set<String>>();
		
		for(Set<String> partition : allPartitions) {
			if(partition.size() == 1) {
				newPartitions.add(partition);
				continue;
			}
			
			List<Set<String>> splittedPartitions = processPartition(partition, allPartitions);
			
			if(splittedPartitions != null) {
				splittedPartitions.forEach(newPartitions::add);
			}
		}
		
		if(newPartitions.size() > allPartitions.size()) {
			newPartitions = minimizeRecursive(newPartitions);
		}
		
		return newPartitions;
	}
	
	/**
	 * Processes the given partition by minimizing it into maximal
	 * number of sub-partitions possible.
	 * 
	 * @param partition the partition to minimize
	 * @param allPartitions list of all partition used by the minimization process
	 * @return the list of sets that are minimized sub-partitions
	 */
	private List<Set<String>> processPartition(Set<String> partition, List<Set<String>> allPartitions) {
		if(partition.isEmpty()) return null;
			
		List<Set<String>> result = new LinkedList<Set<String>>();
		Iterator<String> iter = partition.iterator();
		
		Set<String> firstPartition = new TreeSet<>();
		firstPartition.add(iter.next());
		result.add(firstPartition);
		
		while(iter.hasNext()) {
			String checkingState = iter.next();
			
			boolean foundMatchingPartition = false;
			for(Set<String> p : result) {
				String firstInPartition = p.iterator().next();
				
				if(areStatesEquivalent(checkingState, firstInPartition, allPartitions)) {
					p.add(checkingState);
					foundMatchingPartition = true;
					break;
				}
			}
			
			if(!foundMatchingPartition) {
				Set<String> newPartition = new TreeSet<>();
				newPartition.add(checkingState);
				result.add(newPartition);
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if the given states are equal, depending on the given
	 * partitioning and transition functions. States will be equal
	 * if transitions of every single alphabet symbol lead to states
	 * that are being held in the same partition.
	 * 
	 * @param s1 the first state
	 * @param s2 the second state
	 * @param allPartitions the partitions
	 * @return {@code true} if equivalent, {@code false} otherwise
	 */
	private boolean areStatesEquivalent(String s1, String s2, List<Set<String>> allPartitions) {
		for(String symbol : alphabet) {
			String s1toState = transitionFunction.get(new StateSymbolPair(s1, symbol));
			String s2toState = transitionFunction.get(new StateSymbolPair(s2, symbol));
			
			for(Set<String> partition : allPartitions) {
				if(partition.contains(s1toState)) {
					if(!partition.contains(s2toState))
						return false;
					
					break;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Finds all of the reachable states using recursive
	 * approach.
	 */
	private void findAllReachableStates() {
		Set<String> statesToCheck = new TreeSet<>();
		statesToCheck.add(initialState);
		
		Set<String> reachableStates = new TreeSet<>();
		findAllReachableStatesRecursive(statesToCheck, reachableStates);
		
		states = reachableStates;
	}
	
	/**
	 * Finds all of the states that can potentially
	 * be reached by some input string.
	 */
	private void findAllReachableStatesRecursive(Set<String> statesToCheck, Set<String> reachableStates) {
		statesToCheck.forEach(reachableStates::add);
		
		Set<String> newStatesToCheck = new TreeSet<>();
		
		for(String wantedState : statesToCheck) {
			
			transitionFunction.forEach((pair, state) -> {
				if(pair.getState().equals(wantedState)) {
					if(reachableStates.add(state)) {
						newStatesToCheck.add(state);
					}
				}
			});
		}
		
		if(!newStatesToCheck.isEmpty()) {
			findAllReachableStatesRecursive(newStatesToCheck, reachableStates);
		}
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
		
		/**
		 * Returns the state part of this state-symbol pair.
		 * 
		 * @return the state part of this state-symbol pair
		 */
		public String getState() {
			return state;
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
	 * <b>IMPORTANT:</b> The definition should not have any spaces between symbols!
	 * <p>
	 * The program prompts the user to define the DFA in
	 * this format:
	 * <br> 1. row: list of all states separated by ","
	 * <br> 2. row: list of all symbols in the alphabet separated by ","
	 * <br> 3. row: list of all the final states
	 * <br> 4. row: the initial state
	 * <br> 5. row and later: the transition function of format "state,symbol->state"
	 * <p>
	 * Example of a valid definition:
	 * <br>s1,s2,s3
	 * <br>a,b
	 * <br>s2,s3
	 * <br>s1
	 * <br>s1,a->s2
	 * <br>s1,b->s1
	 * <br>s2,a->s1
	 * <br>s2,b->s1
	 * <br>s3,a->s2
	 * <br>s3,b->s3
	 * 
	 * @param args none are used
	 */
	public static void main(String[] args) {
		MinDka nka = new MinDka();
		
		nka.initialize();
		nka.minimize();
		
		nka.printDefinition();
	}
}
