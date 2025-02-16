/*
 * Shannon Duvall
 * A small Java Interpreter
 * Practice using reflection and understanding how Java works.
 */

import java.util.HashMap;
import java.util.Scanner;

public class Interpreter {
	// The symbol table is a dictionary of variable identifiers to bindings.
	private HashMap<String, Object> mySymbolTable = new HashMap<String, Object>();
	private Parser myParser;
	
	public Interpreter(){
		mySymbolTable = new HashMap<String,Object>();
		myParser = new Parser();
	}
	
	/*
	 * Continually ask the user to enter a line of code.
	 */
	public void promptLoop(){
		System.out.println("This is a simple interpreter.  I'm not a good compiler, so be careful and follow my special rules:\n" +
				"Each class name should be fully qualified, \n"+
				"I only create objects and call methods. \n" +
				"I only use literals of integers and Strings. \n"+
				"Enter 'Q' to quit.");
		Scanner scan = new Scanner(System.in);
		String line = scan.nextLine();
		while(!line.equalsIgnoreCase("q")){
			// Find the important words out of the line.
			ParseResults parse = myParser.parse(line);
			// Do the command, and give a String response telling what happened.
			String answer = process(parse);
			System.out.println(answer);
			line = scan.nextLine();
		}
	}
	
	/*
	 * This method does the work of carrying out the intended command.
	 * The input is a ParseResults object with all the important information
	 * from the typed line identified.  The output is the String to print telling
	 * the result of carrying out the command.
	 */
	public String process(ParseResults parse){
		//System.out.println(parse);
		if (parse.isMethodCall){
			return callMethod(parse);
		}
		else return makeObject(parse);
		
	}
	
	/*
	 * TODO: convertNameToInstance
	 * Given a name (identifier or literal) return the object.
	 * If the name is in the symbol table, you can just return the object it is 
	 * associated with.  If it's not in the symbol table, then it is either 
	 * a String literal or it is an integer literal.  Check to see if the 
	 * first character is quotation marks.  If so, create a new String object to
	 * return that has the same characters as the name, just without the quotes.
	 * If there aren't quotation marks, then it is an integer literal.  Use Integer.parseInt
	 * to turn the String into an int.
	 */
	public Object convertNameToInstance(String name){
		if(mySymbolTable.containsKey(name)){
			Object value = mySymbolTable.get(name);
			System.out.println("Retrieving variable "+name+" = "+value);
			return value;
		}

		if(name.startsWith("\"") && name.endsWith("\"")){
			return name.substring(1,name.length() -1);
		}

		try{
			return Integer.parseInt(name);
		} catch (NumberFormatException e){
			return null;
		}
	}
	
	
	/*TODO: convertNameToInstance.  
	 * Takes an array of Strings and converts all of them to their associated objects.
	 * Simply call the other helper method of the same name on each item in the array.
	 */
	public Object[] convertNameToInstance(String[] names){
		
		Object[] objects = new Object[names.length];
    
		for (int i = 0; i < names.length; i++) {
			objects[i] = convertNameToInstance(names[i]);
		}
		
		return objects;
	}
	
	
	/* TODO: makeObject
	 * This method does the "process" job for making objects.
	 * Don't forget to add the new object to the symbol table.
	 * The String that is returned should be a basic message telling what happened.
	 */
	public String makeObject(ParseResults parse){		
	    Object[] params = convertNameToInstance(parse.arguments);
    	Object newInstance = ReflectionUtilities.createInstance(parse.className, params);
    
    	if (newInstance != null) {
        	mySymbolTable.put(parse.objectName, newInstance);
        	return "Created " + parse.className + " instance with name " + parse.objectName;
    }
    
    	return "Failed to create instance of " + parse.className;
}
	
	/*
	 * TODO: callMethod
	 * This method does the "process" job for calling methods.
	 * You MUST use the ReflectionUtilities to call the method for 
	 * this to work, because ints can't be directly transformed into Objects.
	 * When you call a method, if it has an output, be sure to 
	 * either create a new object for that output or change the 
	 * existing object.  This will require either adding a new 
	 * thing to the symbol table or removing what is currently there
	 * and replacing it with something else.
	 */
	public String callMethod(ParseResults parse) {
		Object target = mySymbolTable.get(parse.objectName);
		
		if (target == null) {
			return "Error: " + parse.objectName + " not found.";
		}
	
		try {
			Object[] params = convertNameToInstance(parse.arguments);
	
			System.out.println("Calling method: " + parse.methodName + " on " + parse.objectName);
			System.out.println("Arguments:");
			for (Object param : params) {
				System.out.println(" - " + param + " (type: " + (param == null ? "null" : param.getClass().getName()) + ")");
			}
	
			Object result = ReflectionUtilities.callMethod(target, parse.methodName, params);
	
			if (result != null) {
				System.out.println("Storing variable: " + parse.objectName + " = " + result);
				mySymbolTable.put(parse.objectName, result);
			}
	
			// Print the full symbol table to check if `nextWord` is stored
			System.out.println("Current Variables in Symbol Table: " + mySymbolTable);
	
			return "Method " + parse.methodName + " called successfully on " + parse.objectName + ". Result was: " + result;
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	
}
