// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

import pyjvm.BinOpInstrs.BinOpFactory;

public final class GenericInstrs {

	/**
	 * Container class for various instructions.
	 */
	
	private GenericInstrs() {}
	
	public static class JumpIfNot extends JumpIfLikeInstr {
		public void init1(Obj label) {} 
		
		public Instr run(Frame frame) {
			Obj value = frame.reg[inreg0];
			if(value.boolValue())
				return next;
			else
				return next2;
		}

	}
	
	public static class JumpIf extends JumpIfLikeInstr {
		public void init1(Obj label) {} 
		
		public Instr run(Frame frame) {
			Obj value = frame.reg[inreg0];
			if(value.boolValue())
				return next2;
			else
				return next;
		}

	}
	

	public static class AssertFail extends Instr {
		public void init0() {} 
		
		public Instr run(Frame frame) {
			Obj err = frame.reg[this.inreg0];
			throw new ScriptError(ScriptError.AssertionError, err == Obj.None? null: err.toString());
		}
	}
	
	public static final class Const extends Instr {
		Obj value;
		
		public void init1(Obj value) {
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
			// TODO: this instruction is now useless
			frame.reg[outreg0] = frame.globals;
			return next;
		}
		
	}
	
	public static final class Global extends Instr {
		private int name;
		
		public void init1(Obj name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			Obj val = frame.globals.getOrNull(name);
			if(val == null)
				val = frame.builtins.get(name);
			frame.reg[outreg0] = val;
			return next;
		}

	}

	public static final class SetGlobal extends Instr {
		private int name;
		
		public void init1(Obj name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			Obj val = frame.reg[inreg0];
			if(val == null)
				throw new ScriptError(ScriptError.InternalError, "Cannot store nulls in globals (error is in previous instruction)");
			frame.globals.put(name, val);
			return next;
		}
	}
	

	public static final class SetLocal extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = frame.reg[inreg0];
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
		
		public void init1(Obj length) {
			this.length = length.intValue();
		}
		
		public Instr run(Frame frame) {
			Tuple tuple;
			if(this.length == 0)
				tuple = Tuple.Empty;
			else {
				Obj[] arr = new Obj[length];
				for(int i=0; i<length; i++) {
					arr[i] = frame.reg[inreg[i]];
				}
				tuple = new Tuple(arr);
			}
			
			frame.reg[outreg0] = tuple;
			
			return next;
		}
		
	}
	
	public static final class MakeList extends Instr {
		private int length;
		private int[] inreg;
		
		public void initReg(int[] inreg, int[] outreg) {
			this.inreg = inreg;
			if(outreg.length != 1)
				throw new ScriptError(ScriptError.TypeError, "Expected 1 outreg");
			this.outreg0 = outreg[0];
		}
		
		public void init1(Obj length) {
			this.length = length.intValue();
		}
		
		public Instr run(Frame frame) {
			List tuple;
			Obj[] arr = new Obj[length];
			for(int i=0; i<length; i++) {
				arr[i] = frame.reg[inreg[i]];
			}
			tuple = List.fromArrayUnsafe(arr);
			
			frame.reg[outreg0] = tuple;
			
			return next;
		}
		
	}
	
	public static final class Print extends Instr {
		private boolean hasLF;
		
		public void init1(Obj obj) {
			hasLF = obj.boolValue();
		}
		
		public Instr run(Frame frame) {
			Tuple args = (Tuple)frame.reg[this.inreg0];
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
		private Obj doc;
		
		public void init1(Obj doc) {
			this.doc = doc;
		}
		
		public Instr run(Frame frame) {
			frame.module.done(doc);
			frame.reg[outreg0] = frame.module;
			return next;
		}
		
	}
	
	public static final class Return extends Instr {
		public void init0(){}
		
		public Instr run(Frame frame) {
			Obj value = frame.reg[inreg0];
			if(frame.parent == null) {
				frame.reg[0] = value;
			} else {
				frame.setFrame = frame.parent;
				frame.setInstr = frame.parent.counter;
				frame.parent.reg[frame.parent.returnValueTo] = value;
			}
			return null;
		}
		
	}
	
	public static final class Call extends Instr {
		private int argCount;
		private int[] inArgs;
		private int inFunc;
		
		public void init2(Obj count, Obj kwargNames){
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
			Obj[] args = new Obj[argCount];
			frame.loadRegisters(inArgs, args);
			Obj funcObj = frame.reg[inFunc];
			
			frame.counter = next;
			boolean wasCalled = funcObj.callInFrame(frame, args);
			if(wasCalled) {
				frame.returnValueTo = outreg0;
				return null;
			} else {
				frame.reg[outreg0] = funcObj.call(args);
				return next;
			}
		}

	}
	
	public static final class GetAttr extends Instr {
		public int name;
		
		public void init1(Obj name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			Obj obj = frame.reg[inreg0];
			Obj result = obj.getAttr(name);
			frame.reg[outreg0] = result;
			
			return next;
		}

	}
	
	public static final class SetAttr extends Instr {
		public int name;
		
		public void init1(Obj name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			Obj obj = frame.reg[inreg0];
			Obj value = frame.reg[inreg1];
			obj.setAttr(name, value);
			
			return next;
		}

	}
	
	public static final class GetItem extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			Obj seq = frame.reg[inreg1];
			Obj key = frame.reg[inreg0];
			Obj result = seq.getItem(frame, key);
			frame.reg[outreg0] = result;
			return next;
		}
	}

	public static final class GetIter extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			Obj seq = frame.reg[inreg0];
			Obj result = seq.getIter(frame);
			frame.reg[outreg0] = result;
			return next;
		}
	}
	
	public static final class ForIter extends JumpIfLikeInstr {
		public void init1(Obj arg) {}
		
		public Instr run(Frame frame) {
			Obj iter = frame.reg[inreg0];
			Obj result = iter.next(frame);
			if(result == null)
				return next2;
			frame.reg[outreg0] = result;
			return next;
		}
	}
	
	public static class FunctionInstr extends Instr {
		private FunctionConst functionConst;

		public void init1(Obj o) {
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

		public void init1(Obj o) {
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
	
	public static final class Nop extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			return next;
		}
	}
	
	public static abstract class BinOp extends Instr {
		public void init1(Obj name) {}
		
		public void operatorFailed(Obj a, Obj b, String name) {
			throw new ScriptError(ScriptError.TypeError, "Unsupported operand type " + name + " for " + Obj.repr(a) + " and " + Obj.repr(b));
		}

		public static Instr createBinOp(Tuple args) {
			if(args.length() != 1)
				throw new ScriptError(ScriptError.TypeError, "Expected 1 argument, got " + args.length());
			SString name = (SString)args.get(0);
			BinOpInstrs.BinOpFactory factory = (BinOpFactory) BinOpInstrs.binOpTypes.get(name.intern());
			return factory.create();
		}
	}
	
	public static abstract class UnaryOp extends Instr {
		public void init1(Obj name) {}

		public static Instr createOp(Tuple args) {
			SString type = args.get(0).stringValue();
			int interned = type.intern();
			
			if(interned == NOT)
				return new UnaryNot();
			else
				throw new ScriptError(ScriptError.TypeError, "unknown unary op: " + type);
		}
		public static final int NOT = SString.intern("not");
		
		public static class UnaryNot extends UnaryOp {
			public final Instr run(Frame frame) {
				Obj a = frame.reg[inreg0];
				Obj done = a.boolValue()? SBool.False: SBool.True;
				frame.reg[outreg0] = done;
				
				return next;
			}
		}
	}
	
	public static final class SetupExc extends JumpIfLikeInstr {
		public void init1(Obj label) {}
		
		public Instr run(Frame frame) {
			if(next2 == null)
				throw new ScriptError(ScriptError.InternalError, "next2 is null");
			frame.addExcHandler(next2);
			
			return next;
		}
	}
	
	public static final class GetExc extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = frame.getException();
			return next;
		}
	}
	
	public static final class PopExc extends Instr {
		private int num;
		public void init1(Obj num) {
			this.num = num.intValue();
		}
		
		public Instr run(Frame frame) {
			frame.excHandlersCount -= num;
			return next;
		}
	}
	
	public static final class GetLocalsDict extends Instr {
		private int[] names;
		public void init1(Obj names) {
			this.names = ((List)names).toInternedStringArray();
		}
		
		public Instr run(Frame frame) {
			StringDict dict = new StringDict(names.length);
			for(int i=0; i<names.length; i++) {
				dict.put(names[i], frame.reg[i]);
			}
			frame.reg[outreg0] = dict;
			return next;
		}
	}
	
	public static final class MakeClass extends Instr {
		private int name;
		private Obj doc;
		public void init1(Obj argsObj) {
			StringDict args = (StringDict)argsObj;
			this.name = args.get("name").stringValue().intern();
			this.doc = args.get("doc");
		}
		
		public Instr run(Frame frame) {
			StringDict dict = (StringDict)frame.reg[inreg1];
			Tuple bases = (Tuple)frame.reg[inreg0];
			
			frame.reg[outreg0] = UserType.create(name, bases, dict);
			
			return next;
		}
	}
	
	public static final class Import extends Instr {
		private SString name;
		public void init1(Obj name) {
			this.name = name.stringValue();
		}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = Importer.importModule(name);
			
			return next;
		}
	}
	
	public static final class ExcMatch extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			Obj excClass = frame.reg[inreg0];
			Obj exception = frame.reg[inreg1];
			frame.reg[outreg0] = Obj.isInstance(excClass, exception)? SBool.True: SBool.False;
			return next;
		}
	}
	

	public static final class Reraise extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			throw new ExistingScriptError(frame.getException(), frame.getTraceback());
		}

	}
}
