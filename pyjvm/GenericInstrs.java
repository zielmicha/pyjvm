package pyjvm;

import pyjvm.BinOpInstrs.BinOpFactory;

public final class GenericInstrs {

	/**
	 * Container class for various instructions.
	 */
	
	private GenericInstrs() {}
	
	public static class JumpIfNot extends JumpIfLikeInstr {
		public void init1(SObject label) {} 
		
		public Instr run(Frame frame) {
			SObject value = frame.reg[inreg0];
			if(value.boolValue())
				return next;
			else
				return next2;
		}

	}
	
	public static class JumpIf extends JumpIfLikeInstr {
		public void init1(SObject label) {} 
		
		public Instr run(Frame frame) {
			SObject value = frame.reg[inreg0];
			if(value.boolValue())
				return next2;
			else
				return next;
		}

	}
	

	public static class AssertFail extends Instr {
		public void init0() {} 
		
		public Instr run(Frame frame) {
			SObject err = frame.reg[this.inreg0];
			throw new ScriptError(ScriptError.AssertionError, err.toString());
		}
	}
	
	public static final class Const extends Instr {
		SObject value;
		
		public void init1(SObject value) {
			this.value = value;
		}
		public String name() { return "Const"; }
		public Instr run(Frame frame) {
			frame.reg[outreg0] = value;
			return next;
		}
	}

	public static final class UseOnlyGlobals extends Instr {
		public void init0() {}

		public Instr run(Frame frame) {
			if(frame.globals == null)
				frame.globals = new StringDict();
			frame.reg[outreg0] = frame.globals;
			return next;
		}
		
	}
	
	public static final class Global extends Instr {
		private int name;
		
		public void init1(SObject name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			SObject val = frame.globals.get(name);
			frame.reg[outreg0] = val;
			return next;
		}

	}

	public static final class SetGlobal extends Instr {
		private int name;
		
		public void init1(SObject name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			SObject val = frame.reg[inreg0];
			frame.globals.put(name, val);
			return next;
		}
	}
	
	public static final class MakeTuple extends Instr {
		private int length;
		private int[] inreg;
		
		public void initReg(int[] inreg, int[] outreg) {
			this.inreg = inreg;
			if(outreg.length != 1)
				throw new ScriptError(ScriptError.TypeError, "Expected 1 outreg");
			this.outreg0 = outreg[0];
		}
		
		public void init1(SObject length) {
			this.length = length.intValue();
		}
		
		public Instr run(Frame frame) {
			STuple tuple;
			if(this.length == 0)
				tuple = STuple.Empty;
			else {
				SObject[] arr = new SObject[length];
				for(int i=0; i<length; i++) {
					arr[i] = frame.reg[inreg[i]];
				}
				tuple = new STuple(arr);
			}
			
			frame.reg[outreg0] = tuple;
			
			return next;
		}
		
	}
	
	public static final class Print extends Instr {
		private boolean hasLF;
		
		public void init1(SObject obj) {
			hasLF = obj.boolValue();
		}
		
		public Instr run(Frame frame) {
			STuple args = (STuple)frame.reg[this.inreg0];
			for(int i=0; i<args.length(); i++) {
				System.out.print(args.get(i));
				System.out.print(" ");
			}
			if(hasLF)
				System.out.println();
			return next;
		}
		
	}
	
	public static final class MakeModule extends Instr {
		private SObject doc;
		
		public void init1(SObject doc) {
			this.doc = doc;
		}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = new Module(frame.reg[inreg0],doc);
			return next;
		}
		
	}
	
	public static final class Return extends Instr {
		public void init0(){}
		
		public Instr run(Frame frame) {
			SObject value = frame.reg[inreg0];
			if(frame.parent == null) {
				frame.reg[0] = value;
			} else {
				frame.setFrame = frame.parent;
				frame.setInstr = frame.parent.counter;
			}
			return null;
		}
		
	}
	
	public static final class Call extends Instr {
		private int argCount;
		private int[] inArgs;
		private int inFunc;
		
		public void init2(SObject count, SObject kwargNames){
			if(kwargNames.length() != 0)
				throw new NotImplementedError("kwargs");
			this.argCount = count.intValue();
		}
		
		public void initReg(int[] inreg, int[] outreg) {
			this.outreg0 = outreg[0];
			this.inArgs = new int[inreg.length - 1];
			System.arraycopy(inreg, 0, inArgs, 0, inreg.length - 1);
			this.inFunc = inreg[inreg.length - 1];
		}
		
		public Instr run(Frame frame) {
			SObject[] args = new SObject[argCount];
			frame.loadRegisters(inArgs, args);
			SObject funcObj = frame.reg[inFunc];
			
			UserFunction func = funcObj.getUserFunction();
			if(func != null) {
				frame.counter = this;
				Frame newFrame = new Frame(frame);
				return func.callInFrame(newFrame, args);
			} else {
				frame.reg[outreg0] = funcObj.call(args);
				return next;
			}
		}

	}

	public static class FunctionInstr extends Instr {
		private FunctionConst functionConst;

		public void init1(SObject o) {
			this.functionConst = (FunctionConst)o;
		}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = functionConst.createInstance(frame);
			
			return next;
		}

	}

	public static class MakeFunction extends Instr {
		private int defaults;
		private boolean varargs;
		private boolean kwargs;
		private int[] argnames;

		public void init1(SObject o) {
			StringDict args = (StringDict)o;
			this.defaults = args.get("defaults").intValue();
			if(defaults != 0)
				throw new ScriptError(ScriptError.NotImplementedError, "Defaults not implemented");
			this.varargs = args.get("varargs").boolValue();
			this.kwargs = args.get("kwargs").boolValue();
			this.argnames = ((List)args.get("argnames")).toInternedStringArray();
		}
		
		public Instr run(Frame frame) {
			Function func = (Function) frame.reg[inreg0];
			frame.reg[outreg0] = new UserFunction(func, this.defaults, this.varargs, this.kwargs, this.argnames);
			return next;
		}

	}
	
	public static final class Raise3 extends Instr {
		public Instr run(Frame frame) {
			// TODO Auto-generated method stub
			return next;
		}
	}
	
	public static abstract class BinOp extends Instr {
		public void init1(SObject name) {}
		
		public void operatorFailed(SObject a, SObject b, String name) {
			throw new ScriptError(ScriptError.TypeError, "Unsupported operand type " + name + " for " + SObject.repr(a) + " and " + SObject.repr(b));
		}

		public static Instr createBinOp(STuple args) {
			if(args.length() != 1)
				throw new ScriptError(ScriptError.TypeError, "Expected 1 argument");
			SString name = (SString)args.get(0);
			BinOpInstrs.BinOpFactory factory = (BinOpFactory) BinOpInstrs.binOpTypes.get(name.intern());
			return factory.create();
		}
	}
	
	
}
