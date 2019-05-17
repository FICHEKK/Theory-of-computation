import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * A simple simulation of the deterministic pushdown
 * automata (DPDA).
 * 
 * @author Filip Nemec
 */
public class SimPa {
	
	/**
	 * Represents the empty string.
	 */
	private static final String EPSILON = "$";
	
	/**
	 * All of the input strings that need to be processed.
	 */
	private String[] inputStrings;
	
	/**
	 * A set of all the existing states. This set is usually
	 * denoted as 'Q'.
	 */
	private Set<String> states = new HashSet<>();
	
	/**
	 * A set of all the existing symbols in the alphabet. This
	 * set is usually denoted as a Greek big sigma symbol.
	 */
	private Set<String> alphabet = new HashSet<>();
	
	/**
	 * A set of all the existing symbols in the stack alphabet.
	 * This set is usually denoted as a Greek big gamma symbol.
	 */
	private Set<String> stackAlphabet = new HashSet<>();
	
	/**
	 * A set of all the final states. This set is usually denoted 
	 * as 'F'.
	 */
	private Set<String> finalStates = new HashSet<>();
	
	/**
	 * The initial (starting) state 'q0'.
	 */
	private String initialState;
	
	/**
	 * The initial (starting) stack symbol 'Z'.
	 */
	private String initialStackSymbol;
	
	/**
	 * Maps the input tuple (current state, input symbol, stack symbol)
	 * to the output tuple (next state, stack symbols).
	 */
	private Map<InputTuple, OutputTuple> transition = new HashMap<>();
	
	/**
	 * A {@cod Writer} implementation which writes the given
	 * string to the console.
	 */
	private final Writer CONSOLE_WRITER = string -> System.out.print(string);
	
	/**
	 * Initializes this PDA using the provided definition.
	 */
	public void initialize() {
		try(Scanner scanner = new Scanner(System.in)) {
			inputStrings = scanner.nextLine().trim().split("\\|");
			
			states		 .addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			alphabet	 .addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			stackAlphabet.addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			finalStates	 .addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			
			initialState 	   = scanner.nextLine().trim();
			initialStackSymbol = scanner.nextLine().trim();
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				
				if(line.isEmpty()) break;
				
				String[] side = line.split("->");
				String[] input = side[0].split(",");
				String[] output = side[1].split(",");
				
				InputTuple inputTuple =   new InputTuple(input[0], input[1], input[2]);
				OutputTuple outputTuple = new OutputTuple(output[0], output[1]);
				
				transition.put(inputTuple, outputTuple);
			}
		}
	}
	
	/**
	 * Processes all of the input strings.
	 */
	public void processInputStrings() {
		for(String input : inputStrings) {
			process(input);
			CONSOLE_WRITER.write("\n");
		}
	}
	
	/**
	 * Processes the given input string.
	 * 
	 * @param input the input string
	 */
	private void process(String input) {
		String currentState = initialState;
		
		Stack<String> stack = new Stack<String>();
		stack.push(initialStackSymbol);
		
		printCurrentAutomataState(currentState, stack);
		
		String[] symbols = input.split(",");
		
		for(int i = 0; ; i++) {
			boolean wasEpsilonTransition = false;
			boolean shouldPop = false;
			OutputTuple output = null;
			
			if(i < symbols.length) {
				if(stack.isEmpty()) {
					output = transition.get(new InputTuple(currentState, symbols[i], EPSILON));
				} else {
					output = transition.get(new InputTuple(currentState, symbols[i], stack.peek()));
					shouldPop = true;
				}
			} else {
				if(finalStates.contains(currentState)) break;
			}
			
			if(output == null) {
				if(stack.isEmpty()) {
					output = transition.get(new InputTuple(currentState, EPSILON, EPSILON));
				} else {
					output = transition.get(new InputTuple(currentState, EPSILON, stack.peek()));
					shouldPop = true;
				}
				
				wasEpsilonTransition = true;
			}
			
			if(output == null) {
				if(i >= symbols.length) break;
				
				CONSOLE_WRITER.write("fail|0");
				return;
			}
			
			if(wasEpsilonTransition) {
				i--;
			}
			
			if(shouldPop) {
				stack.pop();
			}
			
			currentState = output.getState();
			addSymbolsToStack(stack, output.getStackSymbols());
			
			printCurrentAutomataState(currentState, stack);
		}
		
		CONSOLE_WRITER.write(finalStates.contains(currentState) ? "1" : "0");
	}
	
	/**
	 * Adds all of the symbols from the given {@code String} to the
	 * given stack.
	 * 
	 * @param stack the stack to be filled
	 * @param stackSymbols the {@code String} that contains all of the symbols
	 */
	private void addSymbolsToStack(Stack<String> stack, String stackSymbols) {
		char[] symbols = stackSymbols.toCharArray();
		
		// Add from last to first since the most left symbol
		// must be on top of stack.
		for(int i = symbols.length - 1; i >= 0; i--) {
			if(symbols[i] == '$') continue;
			stack.add(String.valueOf(symbols[i]));
		}
	}
	
	/**
	 * Prints the current automata state, that is prints the
	 * current state and all of the current symbols on the stack.
	 * 
	 * @param state the current state
	 * @param stack the current stack
	 */
	private void printCurrentAutomataState(String state, Stack<String> stack) {
		CONSOLE_WRITER.write(state + "#");
		
		if(stack.isEmpty()) {
			CONSOLE_WRITER.write("$");
		} else {
			String[] symbols = stack.toArray(new String[0]);
			
			for(int i = symbols.length - 1; i >= 0; i--) {
				CONSOLE_WRITER.write(symbols[i]);
			}
		}
		
		CONSOLE_WRITER.write("|");
	}
	
	//-------------------------------------------------------------
	//						Writer strategy
	//-------------------------------------------------------------
	
	/**
	 * A strategy that defines how the given string should
	 * be written. For example, this could be writing to 
	 * the console or writing to a file.
	 * 
	 * @author Filip Nemec
	 */
	private interface Writer {
		void write(String string);
	}
	
	//-------------------------------------------------------------
	//						Helping classes
	//-------------------------------------------------------------
	
	/**
	 * Defines an input tuple which represents the input of
	 * a transition function. It consists of the current state,
	 * input symbol and a stack symbol.
	 * 
	 * @author Filip Nemec
	 */
	private static class InputTuple {
		
		/** The current state. */
		private final String state;
		
		/** The input symbol. */
		private final String symbol;
		
		/** The stack symbol. */
		private final String stackSymbol;
		
		/**
		 * Constructs a new input tuple that consists of the
		 * current state, input symbol and a stack symbol.
		 * 
		 * @param state the current state
		 * @param symbol the input symbol
		 * @param stackSymbol the stack symbol
		 */
		public InputTuple(String state, String symbol, String stackSymbol) {
			this.state = state;
			this.symbol = symbol;
			this.stackSymbol = stackSymbol;
		}

		@Override
		public int hashCode() {
			return Objects.hash(stackSymbol, state, symbol);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof InputTuple))
				return false;
			InputTuple other = (InputTuple) obj;
			return Objects.equals(stackSymbol, other.stackSymbol) && Objects.equals(state, other.state)
					&& Objects.equals(symbol, other.symbol);
		}
		
		@Override
		public String toString() {
			return state + "," + symbol + "," + stackSymbol;
		}
	}
	
	/**
	 * Defines an output tuple which represents the output of
	 * a transition function. It consists of the next state and
	 * symbols that will be pushed onto a stack.
	 * 
	 * @author Filip Nemec
	 */
	private static class OutputTuple {
		
		/** The next state. */
		private final String state;
		
		/** The stack symbols. */
		private String stackSymbols;
		
		/**
		 * Constructs a new output tuple that consists
		 * of the next state and stack symbols.
		 * 
		 * @param state
		 * @param stackSymbols
		 */
		public OutputTuple(String state, String stackSymbols) {
			this.state = state;
			this.stackSymbols = stackSymbols;
		}
		
		/**
		 * Returns the state.
		 * 
		 * @return the state
		 */
		public String getState() {
			return state;
		}
		
		/**
		 * Returns the stack symbols.
		 * 
		 * @return the stack symbols
		 */
		public String getStackSymbols() {
			return stackSymbols;
		}
		
		@Override
		public String toString() {
			return state + "," + stackSymbols;
		}
	}
	
	//-------------------------------------------------------------
	//							main
	//-------------------------------------------------------------
	
	/**
	 * A simple program that creates the pushdown automata
	 * and prompts the user to initialize it with the given
	 * definition.
	 * 
	 * @param args none are used
	 */
	public static void main(String[] args) {
		SimPa PDA = new SimPa();
		PDA.initialize();
		PDA.processInputStrings();
	}
}
