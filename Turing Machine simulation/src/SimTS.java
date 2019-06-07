import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * A simple simulator that demonstrates the workings
 * of a Turing machine.
 * 
 * @author FICHEKK
 */
public class SimTS {
	
	/** All the states that this Turing machine can be in. */
	private Set<String> states = new HashSet<>();
	
	/** All the possible symbols in the alphabet. */
	private Set<String> alphabet = new HashSet<>();
	
	/** All the possible tape symbols. */
	private Set<String> tapeAlphabet = new HashSet<>();
	
	/** The empty cell symbol. */
	private String emptyCell;
	
	/** The input tape. */
	private char[] tape;
	
	/** A set of all the final states. */
	private Set<String> finalStates = new HashSet<>();
	
	/** The starting state for this Turing machine. */
	private String initialState;
	
	/** The tape head position. */
	private int head;
	
	/** Transition mapping. */
	private Map<Input, Output> transitions = new HashMap<>();
	
	/**
	 * Creates a new Turing machine and prompts the user to provide
	 * a formal definition. The format definition <b>must</b> be of
	 * the following format:
	 * <br>
	 * <br> 1. line: set of states
	 * <br> 2. line: alphabet
	 * <br> 3. line: tape alphabet
	 * <br> 4. line: empty cell symbol
	 * <br> 5. line: initial tape state
	 * <br> 6. line: set of final states
	 * <br> 7. line: initial state
	 * <br> 8. line: initial head position
	 * <br> 9+ lines: transitions
	 * 
	 * <p>An example of the definition
	 * <br><b> NOTE: You don't type the line number at the start (it is here just as a help)! </b>
	 * <ol>
	 * 		<li> q0,q1,q2,q3,q4 </li>
	 * 		<li> 0,1 </li>
	 * 		<li> 0,1,X,Y,B </li>
	 * 		<li> B </li>
	 * 		<li> 0011BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB </li>
	 * 		<li> q4 </li>
	 * 		<li> q0 </li>
	 * 		<li> 0 </li>
	 * 		<li> q0,0->q1,X,R </li>
	 * 		<li> q1,0->q1,0,R </li>
	 * 		<li> q2,0->q2,0,L </li>
	 * 		<li> q1,1->q2,Y,L </li>
	 * 		<li> q2,X->q0,X,R </li>
	 * 		<li> q0,Y->q3,Y,R </li>
	 * 		<li> q1,Y->q1,Y,R </li>
	 * 		<li> q2,Y->q2,Y,L </li>
	 * 		<li> q3,Y->q3,Y,R </li>
	 * 		<li> q3,B->q4,B,R </li>
	 * </ol>
	 * 
	 * @param args none are used
	 */
	public static void main(String[] args) {
		new SimTS().start();
	}
	
	/**
	 * Constructs a new Turing machine and prompts
	 * the user to enter its definition.
	 */
	public SimTS() {
		initialize();
	}
	
	/**
	 * Initializes the Turing machine.
	 */
	private void initialize() {
		try(Scanner scanner = new Scanner(System.in)) {
			states		.addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			alphabet	.addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			tapeAlphabet.addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			emptyCell 	 = scanner.nextLine().trim();
			tape 		 = scanner.nextLine().trim().toCharArray();
			finalStates .addAll(Arrays.asList(scanner.nextLine().trim().split(",")));
			initialState = scanner.nextLine().trim();
			head	 	 = Integer.parseInt(scanner.nextLine().trim());
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				
				if(line.trim().isEmpty()) break;
				
				String[] transition = line.split("->");
				String[] i = transition[0].split(",");
				String[] o = transition[1].split(",");
				
				transitions.put(new Input(i[0], i[1]), new Output(o[0], o[1], o[2]));
			}
		}
	}
	
	/**
	 * Starts the Turing machine which processes the tape until an unsupported
	 * transition occurs, at which point the machine will stop. In the end,
	 * processing information will be displayed in the following format:
	 * <p>
	 * finishing state <b>|</b> head position <b>|</b> tape with symbols <b>|</b> string acceptance
	 */
	public void start() {
		String currentState = initialState;
		
		while(true) {
			Input input = new Input(currentState, String.valueOf(tape[head]));
			Output output = transitions.get(input);
			
			if(output == null) break;
			
			currentState = output.state;
			overwriteTape(output.symbol, head);

			if(!shiftHead(output.shift)) break;
		}
		
		boolean accepted = finalStates.contains(currentState);
		System.out.println(currentState + "|" + head + "|" + String.valueOf(tape) + "|" + (accepted ? "1" : "0"));
	}
	
	/**
	 * Shifts the tape head left or right, defined by the provided {@code shift}
	 * argument.
	 * 
	 * <p>
	 * If the tape head is at the start of the tape, and left shift was provided,
	 * this method will return {@code false}, indicating that the shifting could not
	 * be performed. The same situation happens if the tape head is at the last tape
	 * index, and user provided a right shift.
	 * 
	 * @param shift the shifting argument
	 * @return {@code true} if shifting was performed successfully, {@code false}
	 *         otherwise
	 */
	private boolean shiftHead(String shift) {
		if(shift.equals("L")) {
			if(head == 0) return false;
			
			head = head - 1;
			
		} else if(shift.equals("R")) {
			if(head == tape.length - 1) return false;
			
			head = head + 1;
			
		} else {
			String msg = "Valid shifts are 'L' (left) and 'R' (right). Given shift was '" + shift + "'";
			throw new IllegalArgumentException(msg);
		}
		
		return true;
	}

	/**
	 * Overwrites the tape symbol at the given tape position.
	 * 
	 * @param symbol the new symbol
	 * @param index  the tape position
	 * @throws IllegalArgumentException  if the given tape symbol is not a part of
	 *                                   the tape alphabet
	 * @throws IndexOutOfBoundsException if the given tape position is invalid
	 */
	private void overwriteTape(String symbol, int index) {
		if(!tapeAlphabet.contains(symbol))
			throw new IllegalArgumentException("Invalid tape symbol '" + symbol + "'");
		
		if(index < 0 || index >= tape.length)
			throw new IndexOutOfBoundsException("For tape position '" + index + "'");
		
		tape[index] = symbol.charAt(0);
	}
	
	/**
	 * Encapsulates the Turing machine input in the transition
	 * function: the current state and tape symbol.
	 * 
	 * @author FICHEKK
	 */
	private static class Input {
		
		/** The current state. */
		final String state;
		
		/** The current tape symbol. */
		final String symbol;
		
		/**
		 * Constructs a new input.
		 * 
		 * @param state the current state
		 * @param symbol the tape symbol
		 */
		public Input(String state, String symbol) {
			this.state = state;
			this.symbol = symbol;
		}
		
		@Override
		public String toString() {
			return "(" + state + ", " + symbol + ")";
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
			if (!(obj instanceof Input))
				return false;
			Input other = (Input) obj;
			return Objects.equals(state, other.state) &&
				   Objects.equals(symbol, other.symbol);
		}
	}
	
	/**
	 * Encapsulates the Turing machine output in the transition
	 * function: the next state, new tape symbol and tape shift.
	 * 
	 * @author FICHEKK
	 */
	private static class Output {
		
		/** The next state. */
		final String state;
		
		/** The new tape symbol. */
		final String symbol;
		
		/** The tape head shift direction. */
		final String shift;
		
		/**
		 * Constructs a new output.
		 * 
		 * @param state the next state
		 * @param symbol the new tape symbol
		 * @param shift the tape head shift direction
		 */
		public Output(String state, String symbol, String shift) {
			this.state = state;
			this.symbol = symbol;
			this.shift = shift;
		}
		
		@Override
		public String toString() {
			return "(" + state + ", " + symbol + ", " + shift + ")";
		}

		@Override
		public int hashCode() {
			return Objects.hash(state, shift, symbol);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Output))
				return false;
			Output other = (Output) obj;
			return Objects.equals(state, other.state) &&
				   Objects.equals(shift, other.shift) &&
				   Objects.equals(symbol, other.symbol);
		}
	}
}
