package pyjvm;

public final class Frame {
	public StringDict globals = null;
	public SObject[] reg = new SObject[128];
	public Frame parent = null;
	public Instr counter;
	
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
		// TODO Implement
		throw new ScriptError(ScriptError.Error, e);
	}

	public final void loadRegisters(int[] inregs, SObject[] args) {
		int length = inregs.length;
		for(int i=0; i<length; i++) {
			args[i] = reg[inregs[i]];
		}
	}
}
