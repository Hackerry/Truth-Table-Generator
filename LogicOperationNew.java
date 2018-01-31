import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Logic Operation Improved
 * @author Hackerry
 *
 */
public class LogicOperationNew extends Application {
    private static final String NOT = "~", AND = "∧", OR = "∨", IMPLY = "→", BICONDITIONAL = "↔";
    private static final String[] operators = new String[] {NOT, AND, OR, IMPLY, BICONDITIONAL, "(", ")"};
    private TextField SFinput;
    private Text SFmessage;
    private boolean binary;
    private int caret;
    
    /**
     * JavaFX entrance
     */
    public void start(Stage stage) {
        GridPane main = new GridPane();
        main.setPadding(new Insets(5,5,5,5));
        
        VBox TBPane = new VBox(5);
        
        HBox inputH = new HBox(5);
        SFinput = new TextField();
        SFinput.setPrefColumnCount(20);
        SFinput.setPromptText("Enter statement form here");
        SFinput.setOnKeyPressed(e -> insertSymbol(e));
        SFinput.setOnAction(e -> checkValid(SFinput.getText()));
        SFinput.caretPositionProperty().addListener((e, oldVal, newVal) -> {
            if(SFinput.isFocused()) {
                caret = newVal.intValue();
            }
        });
        Button generate = new Button("Generate");
        generate.setOnAction(e -> checkValid(SFinput.getText()));
        inputH.getChildren().addAll(SFinput, generate);
        
        HBox buttonH = new HBox(10);
        Button notB = new Button(NOT), andB = new Button(AND), orB = new Button(OR), 
                implyB = new Button(IMPLY), biConB = new Button(BICONDITIONAL);
        CheckBox binaryOutput = new CheckBox("Binary output");
        binaryOutput.setOnAction(e -> {
            binary = !binary;
        });
        buttonH.getChildren().addAll(notB, andB, orB, implyB, biConB);
        for(int i = 0; i < buttonH.getChildren().size(); i++) {
            Button b = (Button)(buttonH.getChildren().get(i));
            b.setOnMouseClicked(e -> {
                SFinput.insertText(caret, b.getText());
                caret++;
            });
        }
        
        SFmessage = new Text("Warning here");
        SFmessage.setTranslateX(10);
        SFmessage.setVisible(false);
        SFmessage.setFill(Color.RED);
        
        TBPane.getChildren().addAll(inputH, buttonH, binaryOutput, SFmessage);
        
        main.add(TBPane, 0, 0);
        
        Scene scene = new Scene(main);
        stage.setTitle("Logic tool");
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Check whether the input is valid statement form
     * @param input input statement form
     */
    private void checkValid(String input) {
        SFmessage.setVisible(false);
        input = input.trim().replace(" ", "");
        
        //Check parenthesis
        int count = 0, index = 0;
        while((index = input.indexOf("(", index)) != -1) {
            count++;
            index++;
        }
        
        index = 0;
        while((index = input.indexOf(")", index)) != -1) {
            count--;
            index++;
        }
        
        if(count > 0) {reportError("Missing )"); return;}
        else if(count < 0) {reportError("Missing (");return;}
        
        //Check valid characters
        for(char c: input.toCharArray()) {
            if(!Character.isAlphabetic(c) && !Arrays.asList(operators).contains(Character.toString(c))) {
                reportError("Invalid character");return;
            }
        }
        
        String[] split = input.split("([" + NOT + AND + OR + IMPLY + BICONDITIONAL + "\\(\\)])");
        List<String> vars = new ArrayList<>();
        for(String s: split) {
            if(!vars.contains(s)) {
                vars.add(s);
            }
        }
        vars = vars.stream().filter(p -> !p.trim().equals("")).collect(Collectors.toList());
        vars.sort(new Comparator<String>(){
            @Override
            public int compare(String arg0, String arg1) {
                return arg0.length()-arg1.length();
            }
        });
        //System.out.println(vars);
        
        if(vars.size() == 0) {reportError("No variable found");return;}
        
        compute(input, vars);
    }
    
    /**
     * Compute part
     * @param input input formula
     * @param vars variables in this formula
     */
    private void compute(String input, List<String> vars) {
        String copy = input;
        
        //Check constants. Avoid some variable names like Tu, FTF
        if(vars.contains("T")) {copy = copy.replace('T', '1');vars.remove("T");}
        if(vars.contains("F")) {copy = copy.replace('F', '0');vars.remove("F");}
        //System.out.println("Compute start: " + copy);
        
        //Only constant
        if(vars.isEmpty()) {
            if(input.length() != 1) {
                reportError("Unexpected connective");
            } else {
                generateTable(input, Arrays.asList(input), new ArrayList<String>(Arrays.asList(input)));
            }
            return;
        }
        
        ArrayList<String> result = new ArrayList<>();
        //Compute part
        for(int i = 0; i < Math.pow(2, vars.size()); i++) {
            String assign = String.format("%" + vars.size() + "s", Integer.toBinaryString(i)).replace(' ', '0');
            result.add(assign);
            String temp = copy;
            for(int j = assign.length()-1; j >= 0; j--) {
                temp = temp.replaceAll(vars.get(j), Character.toString(assign.charAt(j)));
            }
            
            //Parenthesis at both ends of formula to start loop
            StringBuilder workString = new StringBuilder("(" + temp + ")");
            //System.out.println(temp + " " + assign);
            
            while(workString.toString().contains("(")) {
                int start = workString.lastIndexOf("(");
                int end = workString.indexOf(")", start)+1;
                StringBuilder currString = new StringBuilder(workString.substring(start, end));
                
                //Check not
                for(int k = currString.length()-1; k >= 0; k--) {
                    if(currString.charAt(k) == NOT.charAt(0)) {
                        char next = currString.charAt(k+1);
                        if(k + 1 < currString.length() && Character.isDigit(next)) {
                            currString.setCharAt(k+1, next == '0'? '1': '0');
                            currString.deleteCharAt(k);
                        } else {
                            reportError("Expect variable after NOT");
                            return;
                        }
                    }
                }
                
                //Check and
                for(int k = currString.length()-1; k >= 0; k--) {
                    if(currString.charAt(k) == AND.charAt(0)) {
                        char prev = currString.charAt(k-1);
                        char next = currString.charAt(k+1);
                        if(k-1 > 0 && k + 1 < currString.length() && Character.isDigit(prev) && Character.isDigit(next)) {
                            currString.setCharAt(k+1, (prev=='1' & next=='1')? '1':'0');
                            currString.delete(k-1, k+1);
                        } else {
                            reportError("Incorrect use of AND");
                            return;
                        }
                    }
                }
                
              //Check or
                for(int k = currString.length()-1; k >= 0; k--) {
                    if(currString.charAt(k) == OR.charAt(0)) {
                        char prev = currString.charAt(k-1);
                        char next = currString.charAt(k+1);
                        if(k-1 > 0 && k + 1 < currString.length() && Character.isDigit(prev) && Character.isDigit(next)) {
                            currString.setCharAt(k+1, (prev=='1' | next=='1')? '1':'0');
                            currString.delete(k-1, k+1);
                        } else {
                            reportError("Incorrect use of OR");
                            return;
                        }
                    }
                }
                
              //Check imply
                for(int k = currString.length()-1; k >= 0; k--) {
                    if(currString.charAt(k) == IMPLY.charAt(0)) {
                        char prev = currString.charAt(k-1);
                        char next = currString.charAt(k+1);
                        if(k-1 > 0 && k + 1 < currString.length() && Character.isDigit(prev) && Character.isDigit(next)) {
                            currString.setCharAt(k+1, (prev=='0' | next=='1')? '1':'0');
                            currString.delete(k-1, k+1);
                        } else {
                            reportError("Incorrect use of IMPLY");
                            return;
                        }
                    }
                }
                
              //Check biconditional
                for(int k = currString.length()-1; k >= 0; k--) {
                    if(currString.charAt(k) == BICONDITIONAL.charAt(0)) {
                        char prev = currString.charAt(k-1);
                        char next = currString.charAt(k+1);
                        if(k-1 > 0 && k + 1 < currString.length() && Character.isDigit(prev) && Character.isDigit(next)) {
                            currString.setCharAt(k+1, ((prev=='1' & next=='1') | (prev=='0' & next=='0'))? '1':'0');
                            currString.delete(k-1, k+1);
                        } else {
                            reportError("Incorrect use of BICONDITIONAL");
                            return;
                        }
                    }
                }
                
                //System.out.println(currString + " " + start + " " + end);
                if(currString.length() != 3) {
                    reportError("Unsolvable formula"); return;
                }
                workString.setCharAt(end-1, currString.charAt(1));
                workString.delete(start, end-1);
            }
            
            //System.out.println("Result: " + workString.charAt(0));
            result.add("" + workString.charAt(0));
        }
        
        generateTable(input, vars, result);
    }
    
    /**
     * Generate the truth table in new window
     * @param formula entered formula
     * @param vars variables in the formula
     * @param result computed result
     */
    private void generateTable(String formula, List<String> vars, ArrayList<String> result) {
        Stage tableStage = new Stage();
        tableStage.setTitle("Result");
        
        tableStage.setWidth(400);
        tableStage.setHeight(600);
        TextArea ta = new TextArea();
        ta.setFont(Font.font(20));
        ta.setEditable(false);
        int[] length = new int[vars.size()+1];
        for(int i = 0; i < vars.size(); i++) {
            String v = vars.get(i);
            ta.appendText(String.format(" %s ", v));
            length[i] = v.length()+2;
        }
        ta.appendText(" | ");
        ta.appendText(String.format(" %s ", formula) + "\n");
        length[length.length-1] = formula.length()+2;
        
        for(int i = 0; i < result.size()/2; i++) {
            String input = result.get(2*i);
            for(int j = 0; j < vars.size(); j++) {
                int space = (length[j]-1)/2;
                if(!binary) ta.appendText(String.format("%" + space + "s%s%" + (length[j]-space-1) + "s", " ", input.charAt(j)=='1'?"T":"F", " "));
                else ta.appendText(String.format("%" + space + "s%s%" + (length[j]-space-1) + "s", " ", input.charAt(j)=='0'?"0":"1", " "));
            }
            int space = (length[length.length-1]-1)/2;
            if(!binary) ta.appendText(" | " + String.format("%" + space + "s%s%" + (length[length.length-1]-space-1) + "s", " ", result.get(2*i+1).equals("1")?"T":"F", " ") + "\n");
            else ta.appendText(" | " + String.format("%" + space + "s%s%" + (length[length.length-1]-space-1) + "s", " ", result.get(2*i+1).equals("1")?"1":"0", " ") + "\n");
        }
        
        Scene scene = new Scene(ta);
        tableStage.setScene(scene);
        tableStage.show();
    }
    
    /**
     * Report error in window
     * @param message message shown to user
     */
    private void reportError(String message) {
        SFmessage.setVisible(true);
        SFmessage.setText(message);
    }
    
    /**
     * Used to insert logic operators
     * @param e KeyEvent from input
     */
    private void insertSymbol(KeyEvent e) {
        if(e.isControlDown()) {
            switch(e.getCode()) {
                case A:
                    SFinput.insertText(SFinput.getCaretPosition(), AND);
                    break;
                case O:
                    SFinput.insertText(SFinput.getCaretPosition(), OR);
                    break;
                case N:
                    SFinput.insertText(SFinput.getCaretPosition(), NOT);
                    break;
                case I:
                    SFinput.insertText(SFinput.getCaretPosition(), IMPLY);
                    break;
                case B:
                    SFinput.insertText(SFinput.getCaretPosition(), BICONDITIONAL);
                    break;
                case V:
                case C:
                case Z:
                    return;
                default: break;
            }
            e.consume();
        }
    }
    
    /**
     * Driver method
     * @param args not used
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
