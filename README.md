# Truth-Table-Generator
This two programs generate truth tables for first-order logic expressions.
An updated version (LogicOperationNew.java) uses StringBuilder instead of multiple lists to process expressions. This new version also supports variables of two or more characters long.

ShortCut:<br>
<b>Ctrl+N</b> = NOT<br>
<b>Ctrl+A</b> = AND<br>
<b>Ctrl+O</b> = OR<br>
<b>Ctrl+I</b> = IMPLY<br>
<b>Ctrl+B</b> = BICONDITIONAL<br>
"Binary Ouput" means the result will be shown with 0/1 instead of F/T<br>

Capital <b>T</b> means TRUE and capital <b>F</b> means FALSE.
Please encoding with utf-8: <code>javac -encoding utf-8 LogicOperation.java</code>

<b>Update:</b>
(Tips for those who want to implement similar things)
<b>An overview of process</b>

1. Filter out variables<br>
This step involves picking out variable names and properly indexing them for later lookup. If the program is to support only single character variable names, this step is relatively easy (just pick out those alphabetical characters one by one). If it is to support longer variable names (> 1 char), one can explictly ask the user to enter all used variable names in a seperate field, but this is inconvenient for the user and may introduce conflicts (ie. avar & var). A better strategy is to use the standard set of notations (~,∧,∨) so that one can split the string using these symbols as delimiters and retrieve variable names no matter how long they may be.<br><br>

2. Initialize variable<br>
If there are <b>n</b> variables in the functions, then there should be <b>2<sup>n</sup></b> combinations of variable values. One can loop through these [0,2<sup>n</sup>] values and assign each variable based on the current counter's binary value. Logical operations are really handy here.<br><br>

3. Calculate result<br>
With each variable value set, one can start calculating the function. I find the easiest way to achieve this is to use a <a href='https://en.wikipedia.org/wiki/Stack_machine'>stack machine</a>. Each time I evaluate the most recent inner parenthesis and put the result back on the stack. After all parenthesis has been popped off the stack, the result is on the top of the stack.<br><br>

4. Format and output<br>
After calculating results for every possible combinations, just format the result (printf() or equivalent) and output.<br><br>