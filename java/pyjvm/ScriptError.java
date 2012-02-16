// Copyright (C) 2011 by Michal Zielinski
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.


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
	public static final int IndexError = 8;
	public static final int InternalError = 9;
	public static final int ImportError = 10;
	public static final int AttributeError = 11;
	public static final int RuntimeError = 12;
	public static final int NameError = 13;
	
	public static String[] names = new String[] {
		"Error", "KeyError", "ValueError", "TypeError", "IOError",
		"NotImplementedError", "LookupError", "AssertionError",
		"IndexError", "InternalError", "ImportError", "AttributeError",
		"RuntimeError", "NameError"
	};
	
	public static ExceptionType[] excClasses;
	
	public final String message;
	public final int kind;
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
		if(this.kind >= ScriptError.names.length || this.kind < 0)
			return "ScriptError(kind=" + this.kind + ")";
		else
			return ScriptError.names[this.kind];
	}
	
	public String toString() {
		String rest;
		if(message != null && !message.equals(""))
			rest = ": " + message;
		else
			rest = "";
		String str = getName() + rest;
		if(sender != null)
			str += " (" + Obj.typeRepr(sender) + ")";
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
	
	static {
		excClasses = new ExceptionType[names.length];
		for(int i=0; i<excClasses.length; i++) {
			excClasses[i] = new ExceptionType(names[i]);
		}
	}
	
	static final class ExceptionType extends Type {
		private final String name;
		
		public ExceptionType(String name) {
			this.name = name;
		}
		
		public String toString() {
			return "<ExceptionClass " + name + ">";
		}
		
		public Obj getEntry(int name) {
			return SExceptionClass.instance.getEntry(name);
		}
		
		public Type[] getBases() {
			return BASES;
		}
		
		public static final Type[] BASES = new Type[]{ SExceptionClass.instance };
		
		public Obj call(Obj[] args) {
			return new ExceptionInstance(this, args);
		}

		public Obj create(ScriptError scriptError) {
			ExceptionInstance obj = new ExceptionInstance(this, new Obj[] { SString.fromJavaString(scriptError.message) });
			obj.scriptError = scriptError;
			return obj;
		}
	}

	public static final class ExceptionInstance extends Obj {
		private final ExceptionType type;
		private final Obj[] args;
		private ScriptError scriptError;
		
		public ExceptionInstance(ExceptionType type, Obj[] args) {
			this.type = type;
			this.args = args;
		}
		
		public String toString() {
			if(scriptError != null) {
				return scriptError.toString();
			} else {
				return this.type.name + ": " + new Tuple(args);
			}
		}
		
		public Type getType() {
			return type; 
		}
	}
	
	public static Obj createObject(Throwable throwable) {
		if(throwable instanceof ScriptError)
			return ((ScriptError)throwable).createObject();
		else if(throwable instanceof ExistingScriptError)
			return ((ExistingScriptError)throwable).exception;
		else
			return new ScriptError(ScriptError.Error, throwable).createObject();
	}

	private Obj createObject() {
		ExceptionType type;
		if(this.kind >= 0 && this.kind < excClasses.length)
			type = excClasses[this.kind];
		else
			type = excClasses[0];
		
		return type.create(this);
	}
	
	
}
