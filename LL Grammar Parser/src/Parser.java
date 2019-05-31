import java.util.Scanner;

public class Parser {
	
	private static final char EOF = '$';
	
	private String sequence;
	
	private char input;
	
	private int index = -1;
	
	
	public Parser(String sequence) {
		this.sequence = sequence;
	}
	
	public void parse() {
		S();
		System.out.println();
		System.out.print(getNextInput() == '$' ? "DA" : "NE");
	}
	
	private void S() {
		if(input == EOF) return;
		
		System.out.print("S");
		input = getNextInput();
		
		if(input == 'a') {
			A();
			B();
			
		} else if(input == 'b') {
			B();
			A();
			
		} else {
			System.out.println();
			System.out.print("NE");
		}
	}
	
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
	
	private void C() {
		if(input == EOF) return;
		
		System.out.print("C");
		A();
		A();
	}
	
	private char getNextInput() {
		index++;
		return (index >= sequence.length()) ? '$' : sequence.charAt(index);
	}
	
	private void requireSymbol(char symbol) {
		if(input != symbol) {
			System.out.println();
			System.out.print("NE");
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Insert");
		Scanner scanner = new Scanner(System.in);
		new Parser(scanner.next()).parse();
		scanner.close();
	}
}
