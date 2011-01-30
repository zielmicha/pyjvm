// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class Frame {
	public StringDict globals = null;
	public StringDict builtins;
	public final Obj[] reg = new Obj[128];
	public Frame parent = null;
	public Instr counter;
	public final Instr[] excHandlers = new Instr[16];
	public int excHandlersCount = 0;
	
	public Frame setFrame = null;
	public Instr setInstr = null;
	public int returnValueTo = -1;
	
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
				instr = Frame.causeException(frame, e);
			}
			if(frame.setFrame != null) {
				Frame setFrame = frame.setFrame;
				instr = frame.setInstr;
				frame.setFrame = null;
				frame = setFrame;
			}
		}
	}

	private static Instr causeException(Frame frame, Throwable e) {
		while(frame.excHandlersCount != 0) {
			frame.excHandlersCount--;
			Instr handler = frame.excHandlers[frame.excHandlersCount];
			return handler;
		}
		printExc(frame);
		throw new ScriptError(ScriptError.Error, e);
	}

	public static void printExc(Frame frame) {
		System.out.println("Traceback (most recent call last):");
		Frame current = frame;
		while(current != null) {
			Instr instr = current.counter;
			System.out.println("  File " + instr.filename.repr() + ", line " + instr.lineno + ", in ?");
			current = current.parent;
		}
		System.out.println();
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
		return Obj.None;
	}
}
