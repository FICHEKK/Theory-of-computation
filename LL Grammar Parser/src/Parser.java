import java.util.Scanner;

/**
 * A simple implementation of a parser that determines if
 * the given input sequence belongs to the specified grammar.
 * The grammar used in this parser is defined as:
 * <p>
 * <br> S -> aAB | bBA
 * <br> A -> bC | a
 * <br> B -> ccSbc | $
 * <br> C -> AA
 *
 * @author Filip Nemec
 */
public class Parser {
	
	/** The end-of-file character. */
	private static final char EOF = '$';
	
	/** The input sequence. */
	private String sequence;
	
	/** The current input character. */
	private char input;

	/** The current index of the input in the sequence. */
	private int index = -1;
	
	/**
	 * Constructs a new {@code Parser} that will check
	 * the given input sequence.
	 * 
	 * @param sequence the input sequence
	 */
	public Parser(String sequence) {
		this.sequence = sequence;
	}
	
	/**
	 * Parses the input sequence.
	 */
	public void parse() {
		S();
		System.out.println();
		System.out.print(getNextInput() == '$' ? "DA" : "NE");
	}
	
	/** Processes the state S. */
	private void S() {
		if(input == EOF) return;
		
		System.out.print("S");
		input = getNextInput();
		
		if(input == 'a') {
			A();
			B();
			
		} else {
			requireSymbol('b');
			B();
			A();
			
		}
	}
	
	/** Processes the state A. */
	private void A() {
		if(input == EOF) return;
		
		System.out.print("A");
		input = getNextInput();
		
		if(input == 'b') {
			C();
			
		} else {
			requireSymbol('a');
		}
	}
	
	/** Processes the state B. */
	private void B() {
		if(input == EOF) return;
		
		System.out.print("B");
		input = getNextInput();
		
		if(input == 'c') {
			input = getNextInput();
			requireSymbol('c');
			
			S();
			
			input = getNextInput();
			requireSymbol('b');
			
			input = getNextInput();
			requireSymbol('c');
			
		} else {
			index--;
			
		}
	}
	
	/** Processes the state C. */
	private void C() {
		if(input == EOF) return;
		
		System.out.print("C");
		A();
		A();
	}
	
	/**
	 * Returns the next input character.
	 * 
	 * @return the next input character
	 */
	private char getNextInput() {
		index++;
		return (index >= sequence.length()) ? '$' : sequence.charAt(index);
	}
	
	/**
	 * A helper method which requires the given input
	 * to be equal as the given argument {@code symbol}.
	 * 
	 * @param symbol the required symbol
	 */
	private void requireSymbol(char symbol) {
		if(input != symbol) {
			System.out.println();
			System.out.print("NE");
			System.exit(0);
		}
	}
	
	/**
	 * Starts the program which requires the user to input
	 * the sequence which will be parsed. If the given sequence
	 * belongs to the specified grammar, the steps to achieve
	 * the sequence will be shown and the message "DA" will
	 * be printed. Otherwise, the steps until the parsing fails
	 * will be shown and the message "NO" will be printed.
	 * 
	 * @param args none are used
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		new Parser(scanner.next()).parse();
		scanner.close();
	}
}
