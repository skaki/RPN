import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

public class Spreadsheet {

	final static String operands = "^*+-/" ;
	final static int PRECEDENCE_PLUS=  1;
	final static int PRECEDENCE_MINUS=  1;
	final static int PRECEDENCE_MULTIPLIY=  2;
	final static int PRECEDENCE_DIVIDE=  2;
	final static int PRECEDENCE_EXPONENT=  3;
	final static int PRECEDENCE_PARANTHESIS=  4;
	final static int COLUMN_START_INDEX  =  1;
	final static char ROW_START_INDEX =  'A';

	private static Hashtable refs = new Hashtable();
	private static ArrayList<Object> lines = new ArrayList<Object>();
	
	public static void main(String args[]) {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		int width = 0;
		int height = 0;
		
		// Step 1: Read the first line, figure spreadsheet dimensions
		try {
			if ((line = stdin.readLine()) != null){
				String[] dims = line.split(" ");
				width = Integer.parseInt(dims[0]);
				height = Integer.parseInt(dims[1]);
				lines.add(line);
				
				// output the first line
				System.out.println(line);
			}
			else {
				System.out.println("Empty Inputfile");
				System.exit(10);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int wIndex = COLUMN_START_INDEX;
		char hIndex = ROW_START_INDEX; // A 
		
		int lineIndex = 1;
		
		// Step 2: Start reading the expressions, evaluate literal expressions, build the reference table
		try {
			while ((line = stdin.readLine()) != null){
				Double v = parseRPN(line);
				if ( v != null) {
					lines.add(v);
					String CellName = "" + String.valueOf(hIndex) + String.valueOf(wIndex);
					refs.put(CellName, v);
				}
				else 
					lines.add(line);
				
				wIndex = lineIndex % width + 1;
				if ( wIndex ==  1 ) hIndex++;
				
				lineIndex++;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Step 3: Second Pass : Dereference Cell references, evaluate expressions and print the output.
		wIndex = COLUMN_START_INDEX;
		hIndex = ROW_START_INDEX; // A 
		
		for ( int i=1; i < lines.size(); i++ ) {
			Object solved = lines.get(i);
			if ( solved instanceof Double) { 
				System.out.println(String.format("%.5f",solved));
				continue;
			}
			
			String nextLine = solved.toString();
			String CellName = "" + String.valueOf(hIndex) + String.valueOf(wIndex);
			try {
				Double v = parseRPN(nextLine);
				if ( v != null) {
					lines.set(i, v);
					refs.put(CellName, v);
					System.out.println(String.format("%.5f",v));
				}
				else  {
					System.out.println("Cyclical Dependency noted at Cell : " + CellName + ", expression : " + nextLine );
					System.exit(10);
				}
				wIndex = i % width + 1;
				if ( wIndex ==  1 ) hIndex++;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private static Double parseRPN(String input) throws Exception{
		String rpnStr = input;
		String[] tokens = rpnStr.split("\\s+");
		Stack<Double> numberStack =new Stack<Double>();

		boolean  bAllowParenthesis = false;
		for( String token : tokens)
		{
			if(token.equals("-")==false && isNumber(token ))
			{ 	  
				double d = Double.parseDouble( token  ) ;
				numberStack.push(d ) ;
			}
			else if( isOperand( token , bAllowParenthesis   ) )
			{
				if( numberStack.size() <  2 )
				{
					throw new Exception("Invalid Syntax, operator " + token + " must be preceeded by at least two operands");
				}
				double num1 = numberStack.pop();
				double num2 = numberStack.pop() ;
				double result = calculate( num2 , num1 , token  ) ;
				numberStack.push( result);
			}
			else if( token.trim().length() > 0 ) {
				Object val = refs.get(token);
				if ( val != null)
					numberStack.push((Double)val) ;
				else 					
					return null;  // This cell still has some references that need to be solved.
			}
		}
		return numberStack.pop();		
	}
	private static Double calculate(double num1, double num2, String op ) throws Exception {
		if( op.equals("+")) 
				return num1 + num2;
		else if( op.equals("-")) 
				return num1 - num2;
		else if( op.equals("*")) 
			return num1 * num2;
		else if( op.equals("^")) 
			return Math.pow(num1 , num2 );
		else if( op.equals("/") )
		{
				if(num2 ==0 )
						throw new ArithmeticException("Division by zero!"); 
				return num1 / num2;
		}
		else
		{
			throw new Exception(op + " is not a supported operand") ;
		}
	}
	private int operatorToPrecedence(String op){
		if( op.equals("+")) 
			return PRECEDENCE_PLUS ;
		else if( op.equals("-")) 
			return PRECEDENCE_MINUS;
		else if( op.equals("*")) 
			return PRECEDENCE_MULTIPLIY ;
		else if( op.equals("^")) 
			return PRECEDENCE_EXPONENT ;
		else if( op.equals("/") )
			return PRECEDENCE_DIVIDE ;
		else 
			return PRECEDENCE_PARANTHESIS;
	}
	private static boolean isNumber(String s){
		String  master="-0123456789.";
		s = s.trim();
		for( int i = 0;i < s.length()  ;i++)	{
			String lttr = s.substring(i, i+1);
			if(master.indexOf( lttr) == -1)
					return false;
		}
		return true ;		
	}	
	private static boolean isOperand(String s, boolean allowParanethesis){
		s = s.trim();
		if (s.length() != 1 )
				return false;
		if (allowParanethesis &&  ( s.equals("(") ||  s.equals(")") ) )
				return true;
		else return 	operands.indexOf( s ) != -1 ;		
	}
}