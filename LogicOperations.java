import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LogicOperations extends Application {
    private static final String NOT = "¬", AND = "∧", OR = "∨", IMPLY = "→", BICONDITIONAL = "↔";
    private TextField SFinput;
    private Text SFmessage;
    
    public void start(Stage stage) {
        GridPane main = new GridPane();
        main.setPadding(new Insets(5,5,5,5));
        
        VBox TBPane = new VBox(5);
        
        HBox inputH = new HBox(5);
        SFinput = new TextField();
        SFinput.setPrefColumnCount(20);
        SFinput.setPromptText("Enter statement form here");
        SFinput.setOnKeyPressed(e -> insertSymbol(e));
        SFinput.setOnAction(e -> generateTable());
        Button generate = new Button("Generate");
        generate.setOnAction(e -> generateTable());
        inputH.getChildren().addAll(SFinput, generate);
        
        HBox buttonH = new HBox(10);
        Button notB = new Button(NOT), andB = new Button(AND), orB = new Button(OR), 
                implyB = new Button(IMPLY), biConB = new Button(BICONDITIONAL);
        buttonH.getChildren().addAll(notB, andB, orB, implyB, biConB);
        for(int i = 0; i < buttonH.getChildren().size(); i++) {
            Button b = (Button)(buttonH.getChildren().get(i));
            b.setOnMouseClicked(e -> SFinput.appendText(b.getText()));
        }
        buttonH.setTranslateX(20);
        
        SFmessage = new Text("Warning here");
        SFmessage.setTranslateX(10);
        SFmessage.setVisible(false);
        SFmessage.setFill(Color.RED);
        
        TBPane.getChildren().addAll(inputH, buttonH, SFmessage);
        
        main.add(TBPane, 0, 0);
        
        Scene scene = new Scene(main);
        stage.setTitle("Logic tool");
        stage.setScene(scene);
        stage.show();
    }
    
    private void generateTable() {
        SFmessage.setVisible(false);
        String sf = SFinput.getText().trim();
        if(sf.equals("")) {reportProblem(SFmessage, "Empty input");return;}
        
        List<String> all = Arrays.asList(sf.split("")).stream().filter(p -> !p.trim().equals("")).collect(Collectors.toList());
        List<String> vars = Arrays.asList(sf.split("[¬, ∧, ∨, →, ↔, (, )]")).stream().filter(p -> !p.trim().equals("")).collect(Collectors.toList());
        //System.out.println(all + " " + vars);
        
        //Check for validity
        int leftP = 0, rightP = 0;
        for(int i = 0; i < all.size(); i++) {
            if(all.get(i).equals("(")) leftP++;
            else if(all.get(i).equals(")")) rightP++;
        }
        if(leftP != rightP) {reportProblem(SFmessage, "Missing " + (leftP < rightP? "(": ")"));return;}
        

        List<String> var = new LinkedList<>();
        // First loop get all variables and pick out independent variables
        for(int i = 0; i < vars.size(); i++) {
            if(!var.contains(vars.get(i))) var.add(vars.get(i));
        }
        if(var.isEmpty()) {reportProblem(SFmessage, "Should at least have one variable"); return;}
        var.sort(new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return arg0.charAt(0)-arg1.charAt(0);
            }
        });
        
        // Remove all constant that indicate T or F
        var.removeAll(Arrays.asList("T", "F"));
        
        // All are constant values
        if(var.isEmpty()) {
            boolean result;
            try {
                result = compute(all, new LinkedList<String>(), new boolean[0]);
            } catch(Exception e) {
                return;
            }
            showTable(new LinkedList<String>(Arrays.asList(sf)), result? "T": "F", new ArrayList<boolean[]>(), new boolean[0]);
            return;
        }
        //System.out.println(var);
        
        int numberOfVars = var.size();
        ArrayList<boolean[]> booleans = new ArrayList<>();
        boolean[] bools = new boolean[numberOfVars];
        boolean[] results = new boolean[(int)Math.pow(2, numberOfVars)];
        
        //Perform compute on all possible permutation
        boolean success = true;
        for(int i = 0; i < results.length; i++) {
            String binary = Integer.toBinaryString(i);
            //System.out.println("binary: " + binary);
            
            int startIndex = bools.length - binary.length();
            for(int j = 0; j < binary.length(); j++) {
                bools[startIndex + j] = binary.charAt(j) == '1';
            }
            
            boolean result = false;
            try {
                result = compute(all, var, bools);
            } catch(Exception e) {
                success = false;
                break;
            }
            //System.out.println(all + " " + result);
            
            results[i] = result;
            booleans.add(Arrays.copyOf(bools, bools.length));
        }
        
        if(success) showTable(var, sf, booleans, results);
    }
    
    private boolean compute(final List<String> input, final List<String> var, final boolean[] bools) {
        List<String> all = new LinkedList<>(input);
        
        for (int k = 0; k < all.size(); k++) {
            if (var.contains(all.get(k))) {
                all.set(k, bools[var.indexOf(all.get(k))] ? "T" : "F");
            }
        }
        //System.out.println(all);
        
        // Compute part, add parenthesis to compute at least once
        all.add(all.size(), ")");
        all.add(0, "(");
        
        List<String> sub = null;
        while(all.contains("(")) {
            int low = all.lastIndexOf("(");
            sub = all.subList(low, all.size());
            int up = sub.indexOf(")") + 1;
            sub = sub.subList(0, up);
            //System.out.println(sub);
            
            // Loop for not
            for(int i = sub.size()-1; i >= 0; i--) {
                if(sub.get(i).equals(NOT)) {
                    String next = sub.get(i+1);
                    if(!var.contains(next) && !next.equals("T") && !next.equals("F")) {
                        reportProblem(SFmessage, "Expected variable after NOT"); throw new IllegalArgumentException();
                    } else {
                        sub.set(i+1, next.equals("T")? "F": "T");
                    }
                    sub.remove(i);
                    //System.out.println(sub);
                }
            }
            
            // Loop for and and or
            for(int i = 0; i < sub.size(); i++) {
                if(sub.get(i).equals(AND)) {
                    char first = sub.get(i-1).charAt(0);
                    char second = sub.get(i+1).charAt(0);
                    if(first != 'T' && first != 'F') {reportProblem(SFmessage, "Expected variable before AND"); throw new IllegalArgumentException();}
                    else if(second != 'T' && second != 'F') {reportProblem(SFmessage, "Expected variable after AND"); throw new IllegalArgumentException();}
                    
                    boolean result = (first == 'T') & (second == 'T');
                    sub.set(i-1, result? "T": "F");
                    sub.remove(i);sub.remove(i);
                    i--;
                    
                    //System.out.println(sub);
                } else if(sub.get(i).equals(OR)) {
                    char first = sub.get(i-1).charAt(0);
                    char second = sub.get(i+1).charAt(0);
                    if(first != 'T' && first != 'F') {reportProblem(SFmessage, "Expected variable before OR"); throw new IllegalArgumentException();}
                    else if(second != 'T' && second != 'F') {reportProblem(SFmessage, "Expected variable after OR"); throw new IllegalArgumentException();}
                    
                    boolean result = (first == 'T') | (second == 'T');
                    sub.set(i-1, result? "T": "F");
                    sub.remove(i);sub.remove(i);
                    i--;
                    
                    //System.out.println(sub);
                }
            }
            
            // Loop for imply
            for(int i = 0; i < sub.size(); i++) {
                if(sub.get(i).equals(IMPLY)) {
                    char first = sub.get(i-1).charAt(0);
                    char second = sub.get(i+1).charAt(0);
                    if(first != 'T' && first != 'F') {reportProblem(SFmessage, "Expected variable before " + IMPLY); throw new IllegalArgumentException();}
                    else if(second != 'T' && second != 'F') {reportProblem(SFmessage, "Expected variable after " + IMPLY); throw new IllegalArgumentException();}
                    
                    boolean result = (first == 'F') | (second == 'T');
                    sub.set(i-1, result? "T": "F");
                    sub.remove(i);sub.remove(i);
                    i--;
                    
                    //System.out.println(sub);
                }
            }
            
            //Loop for biconditional
            for(int i = 0; i < sub.size(); i++) {
                if(sub.get(i).equals(BICONDITIONAL)) {
                    char first = sub.get(i-1).charAt(0);
                    char second = sub.get(i+1).charAt(0);
                    if(first != 'T' && first != 'F') {reportProblem(SFmessage, "Expected variable before " + BICONDITIONAL); throw new IllegalArgumentException();}
                    else if(second != 'T' && second != 'F') {reportProblem(SFmessage, "Expected variable after " + BICONDITIONAL); throw new IllegalArgumentException();}
                    
                    boolean result = (((first == 'T') & (second == 'T')) | ((first == 'F') & (second == 'F')));
                    sub.set(i-1, result? "T": "F");
                    sub.remove(i);sub.remove(i);
                    i--;
                    
                    //System.out.println(sub);
                }
            }
            
            // Finished
            if(sub.size() <= 3) {
                sub.remove(0);sub.remove(1);
            } else {
                reportProblem(SFmessage, "Unsolvable formula");
                throw new IllegalArgumentException();
            }
            //System.out.println(all);
        }
        
        return sub == null? false: sub.get(0).equals("T");
    }
    
    private void showTable(final List<String> var, String formula, final ArrayList<boolean[]> bools,  final boolean[] results) {
        Stage tableStage = new Stage();
        tableStage.setTitle("Result");
        
        tableStage.setWidth(400);
        tableStage.setHeight(600);
        TextArea ta = new TextArea();
        ta.setFont(Font.font(20));
        ta.setEditable(false);
        int[] length = new int[var.size()+1];
        for(int i = 0; i < var.size(); i++) {
            String v = var.get(i);
            ta.appendText(String.format(" %s ", v));
            length[i] = v.length()+2;
        }
        ta.appendText(" | ");
        ta.appendText(String.format(" %s ", formula) + "\n");
        length[length.length-1] = formula.length()+2;
        
        for(int i = 0; i < bools.size(); i++) {
            boolean[] bs = bools.get(i);
            for(int j = 0; j < bs.length; j++) {
                int space = (length[j]-1)/2;
                ta.appendText(String.format("%" + space + "s%s%" + (length[j]-space-1) + "s", " ", bs[j]?"T":"F", " "));
            }
            int space = (length[length.length-1]-1)/2;
            ta.appendText(" | " + String.format("%" + space + "s%s%" + (length[length.length-1]-space-1) + "s", " ", results[i]?"T":"F", " ") + "\n");
        }
        
        Scene scene = new Scene(ta);
        tableStage.initModality(Modality.APPLICATION_MODAL);
        tableStage.setScene(scene);
        tableStage.show();
    }
    
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
    
    private void reportProblem(Text component, String warning) {
        System.out.println("Warining: " + warning);
        component.setText(warning);
        component.setVisible(true);
    }
    
    public static void main(String[] args) {
        Application.launch(args);
        //new LogicOperations().generateTable("¬(((¬r∧q)∨(p∨¬r))∨r");
    }
}
