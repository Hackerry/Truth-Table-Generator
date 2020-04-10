import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class BooleanAlgebra {
	public static void main(String[] args) {		
		char[] t = args[0].toCharArray();
		ArrayList<Character> vars = new ArrayList<>();
		for(char c: t) vars.add(c);
		
		int numVars = vars.size();
		
		String equation = new String("("+args[1]+")");
		
		int rows = (int)Math.pow(2, numVars);
		
		for(int j = 0; j < numVars; j++) {
			System.out.print(vars.get(j) + " ");
		}
		System.out.println("Result");
		
		boolean[] values = new boolean[numVars];
		int temp;
		Stack<Character> stack = new Stack<>();
		for(int i = 0; i < rows; i++) {
			temp = i;
			for(int j = numVars-1; j >= 0; j--) {
				values[j] = ((temp & 1) == 1);
				temp >>>= 1;
			}
			
			for(int j = 0; j < numVars; j++)
				System.out.print((values[j]? '1': '0') + " ");
			
			stack.clear();
			for(char c: equation.toCharArray()) {
				if(c != ')') {
					stack.push(c);
					// System.out.println("Push "+ c);
				} else if(c == ' ') {
					
				} else {
					// System.out.println("Else");
					char last = stack.pop();
					StringBuffer s = new StringBuffer("");
					while(last != '(') {
						s.append(last);
						last = stack.pop();
					}
					//System.out.println("S:" + s.toString());
					
					// Replace vars
					for(int j = 0;  j < s.length(); j++) {
						if(Character.isAlphabetic(s.charAt(j))) {
							s.setCharAt(j, values[vars.indexOf(s.charAt(j))]? '1': '0');
						}
					}
					
					// Process AND first
					String[] prods = s.toString().split("\\+");
					char curr;
					boolean result = false;
					int lastIdx;
					for(String st: prods) {
						lastIdx = st.charAt(0) == '\''? 1: 0;
						if(lastIdx == 1) curr = st.charAt(lastIdx) == '0'? '1': '0';
						else curr = st.charAt(lastIdx);
						
						for(int j = lastIdx+1; j < st.length(); j++) {
							if(st.charAt(j) == '\'') {
								curr = ((curr == '1') & (st.charAt(++j) == '0'))? '1': '0';
							} else {
								curr = ((curr == '1') & (st.charAt(j) == '1'))? '1': '0';
							}
						}
						
						result |= (curr == '1'? true: false);
					}
					
					// Push back result
					stack.push(result? '1': '0');
					// System.out.println(result);
				}
			}
			
			System.out.print("  " + stack.pop() + "\n");
		}
	}
}
