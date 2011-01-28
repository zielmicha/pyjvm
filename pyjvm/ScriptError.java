package pyjvm;

import java.io.PrintStream;

public class ScriptError extends RuntimeException {
	private static final long serialVersionUID = -2181712954185348016L;
	
	public static final int Error = 0;
	public static final int KeyError = 1;
	public static final int ValueError = 2;
	public static final int TypeError = 3;
	public static final int IOError = 4;
	public static final int NotImplementedError = 5;
	public static final int LookupError = 6;
	public static final int AssertionError = 7;
	
	public static String[] names = new String[] {
		"Error", "KeyError", "ValueError", "TypeError", "IOError",
		"NotImplementedError", "LookupError", "AssertionError"
	};
	
	private String message;
	private int kind;
	private Obj sender;
	
	public ScriptError(int kind, String message) {
		super(message);
		this.kind = kind;
		this.message = message;
	}
	
	public ScriptError(int kind, Throwable e) {
		this(kind, "", e);
	}

	public ScriptError(int kind, String message, Throwable e) {
		super(message, e);
		this.kind = kind;
		this.message = message;
	}
	
	public ScriptError(int kind, String message, Obj sender) {
		super(message);
		this.kind = kind;
		this.message = message;
		this.sender = sender;
	}

	public String getName() {
		if(this.kind >= ScriptError.names.length)
			return "ScriptError(kind=" + this.kind + ")";
		else
			return ScriptError.names[this.kind];
	}
	
	public String toString() {
		String str = getName() + ": " + message;
		if(sender != null)
			str += " (" + sender + ")";
		return str;
	}
	
	public boolean equals() {
		throw new RuntimeException("Not implemented");
	}
	public int hashCode() {
		throw new RuntimeException("Not implemented");
	}
	
	public void printStackTrace() {
		printStackTrace(System.out);
	}
	
	public void printStackTrace(PrintStream s) {
		s.print("\r                          \r"); // hides "Exception in..." ugly message 
		super.printStackTrace(s);
	}
	
	
}
