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

public final class Frame {
	public StringDict globals = null;
	public StringDict builtins;
	public Module module = null;
	public final Obj[] reg = new Obj[128];
	public Frame parent = null;
	public Instr counter;
	public final Instr[] excHandlers = new Instr[16];
	public int excHandlersCount = 0;
	
	public Frame setFrame = null;
	public Instr setInstr = null;
	public int returnValueTo = -1;
	private Throwable throwable;
	private Obj exceptionObject;
	
	public Frame(Frame parent) {
		this.parent = parent;
	}

	public static void execute(Frame frame, Instr instr) {
		while(instr != null) {
			try {
				while(instr != null) {
					/*System.err.print("\033[01;36mreg:\033[00m ");
					for(int i=0; i<10; i++)
						System.err.print(frame.reg[i] + ", ");
					System.err.println();
					
					instr.dump();*/
					
					instr = instr.run(frame);
				}
			} catch(Throwable e) {
				frame.counter = instr;
				Frame.causeException(frame, e);
			}
			if(frame.setFrame != null) {
				Frame setFrame = frame.setFrame;
				instr = frame.setInstr;
				frame.setFrame = null;
				frame = setFrame;
			}
		}
	}

	private static void causeException(Frame frame, Throwable e) {
		frame.throwable = e;
		frame.exceptionObject = null;
		Frame current = frame;
		while(current != null) {
			while(current.excHandlersCount > 0) {
				current.excHandlersCount--;
				Instr handler = current.excHandlers[current.excHandlersCount];
				frame.setFrame = current;
				frame.setInstr = handler;
				return;
			}
			current = current.parent;
		}
		printExc(frame, e);
		throw new ScriptError(ScriptError.Error, e);
	}

	public static void printExc(Frame frame, Throwable e) {
		System.err.println("Traceback (most recent call last):");
		Frame current = frame;
		while(current != null) {
			Instr instr = current.counter;
			System.err.println("  File " + instr.filename.repr() + ", line " + instr.lineno + ", in ?");
			current = current.parent;
		}
		System.err.println(e);
		System.err.println();
	}
	
	public final void loadRegisters(int[] inregs, Obj[] args) {
		int length = inregs.length;
		for(int i=0; i<length; i++) {
			args[i] = reg[inregs[i]];
		}
	}

	public void addExcHandler(Instr handler) {
		excHandlers[excHandlersCount++] = handler;
	}

	public Obj getException() {
		if(throwable == null)
			throw new ScriptError(ScriptError.RuntimeError, "there is no error thrown");
		if(exceptionObject == null)
			exceptionObject = ScriptError.createObject(throwable); 
		return exceptionObject;
	}

	public Traceback getTraceback() {
		return null;
	}
	
	public static Obj call(CallInExistingFrame func, Obj[] args) {
		Frame frame = new Frame(null);
		frame.builtins = Builtins.dict;
		
		Instr instr = func.callInExistingFrame(frame, args);
		
		frame.setInstr = null;
		Frame.execute(frame, instr);
		
		return frame.reg[0];
	}

}
