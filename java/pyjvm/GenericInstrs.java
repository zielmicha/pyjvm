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
		public void init1(Obj a) {}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = frame.reg[inreg0];
			return next;
		}
	}
	
	public static final class UnpackTuple extends Instr {
		private int length;
		private int[] outreg;
		
		public void init1(Obj length) {
			this.length = length.intValue();
		}
		
		public void initReg(int[] inreg, int[] outreg) {
			this.outreg = outreg;
			if(inreg.length != 1)
				throw new ScriptError(ScriptError.TypeError, "Expected 1 inreg");
			this.inreg0 = inreg[0];
		}
		
		public Instr run(Frame frame) {
			Obj seq = frame.reg[inreg0];
			Obj iter = seq.getIter();
			
			for(int i=0; i<this.length; i++) {
				Obj val = iter.next();
				if(val == null)
					throw new ScriptError(ScriptError.ValueError, "need more than " + i + " values to unpack");
				frame.reg[outreg[i]] = val;
			}
			if(iter.next() != null)
				throw new ScriptError(ScriptError.ValueError, "too many values to unpack");
			
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
	
	public static final class MakeDict extends Instr {
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
			Dict dict = new Dict(length * 3 / 2);
			for(int i=0; i<length*2; i+=2) {
				dict.put(frame.reg[inreg[i]], frame.reg[inreg[i+1]]);
			}
			frame.reg[outreg0] = dict;
			
			return next;
		}
		
	}
	
	public static final class Print extends Instr {
		private boolean hasLF;
		
		public void init1(Obj obj) {
			hasLF = obj.boolValue();
		}
		
		public Instr run(Frame frame) {
			// TODO: dest
			Tuple args = (Tuple) frame.reg[this.inreg1];
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
		
		private int[] kwargs;
		private boolean hasKwargs;
		
		public void init2(Obj count, Obj kwargNames){
			this.kwargs = ((List)kwargNames).toInternedStringArray();
			hasKwargs = this.kwargs.length != 0;
			this.argCount = count.intValue();
		}
		
		public void initReg(int[] inreg, int[] outreg) {
			this.outreg0 = outreg[0];
			this.inArgs = new int[inreg.length - 1];
			for(int i=1; i<inreg.length; i++)
				this.inArgs[i - 1] = inreg[i];
			this.inFunc = inreg[0];
		}
		
		public Instr run(Frame frame) {
			Obj[] args = new Obj[argCount];
			frame.loadRegisters(inArgs, args);
			Obj funcObj = frame.reg[inFunc];
			
			frame.counter = next;
			boolean wasCalled;
			if(!hasKwargs)
				wasCalled = funcObj.callInFrame(frame, args);
			else
				wasCalled = funcObj.callInFrame(frame, args, kwargs);
			if(wasCalled) {
				frame.returnValueTo = outreg0;
				return null;
			} else {
				frame.reg[outreg0] = hasKwargs? funcObj.call(args, kwargs) : funcObj.call(args);
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
			Obj obj = frame.reg[inreg1];
			Obj value = frame.reg[inreg0];
			obj.setAttr(name, value);
			
			return next;
		}

	}
	
	public static final class GetItem extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			Obj seq = frame.reg[inreg0];
			Obj key = frame.reg[inreg1];
			Obj result = seq.getItem(frame, key);
			frame.reg[outreg0] = result;
			return next;
		}
	}
	
	public static final class GetSlice extends Instr {
		public void init0() {}
		
		public int inreg2;
		
		public void initReg(int[] inreg, int[] outreg) {
			inreg0 = inreg[0];
			inreg1 = inreg[1];
			inreg2 = inreg[2];
			outreg0 = outreg[0];
		}
		
		public Instr run(Frame frame) {
			Obj seq = frame.reg[inreg0];
			Obj lower = frame.reg[inreg1];
			Obj upper = frame.reg[inreg2];
			Obj result = seq.getSlice(frame, lower, upper);
			frame.reg[outreg0] = result;
			return next;
		}
	}
	
	public static final class SetItem extends Instr {
		public void init0() {}
		
		public int inreg2;
		
		public void initReg(int[] inreg, int[] outreg) {
			inreg0 = inreg[0];
			inreg1 = inreg[1];
			inreg2 = inreg[2];
		}
		
		public Instr run(Frame frame) {
			Obj seq = frame.reg[inreg1];
			Obj index = frame.reg[inreg2];
			Obj item = frame.reg[inreg0];
			seq.setItem(frame, index, item);
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
	
	public static final class FunctionInstr extends Instr {
		private FunctionConst functionConst;

		public void init1(Obj o) {
			this.functionConst = (FunctionConst)o;
		}
		
		public Instr run(Frame frame) {
			frame.reg[outreg0] = functionConst.createInstance(frame);
			
			return next;
		}

	}

	public static final class MakeFunction extends Instr {
		private boolean varargs;
		private boolean kwargs;
		private int[] argnames;

		public void init1(Obj o) {
			StringDict args = (StringDict)o;
			this.varargs = args.get("varargs").boolValue();
			this.kwargs = args.get("kwargs").boolValue();
			this.argnames = ((List)args.get("argnames")).toInternedStringArray();
		}
		
		public Instr run(Frame frame) {
			Function func = (Function) frame.reg[inreg0];
			Tuple defaults = (Tuple) frame.reg[inreg1];
			frame.reg[outreg0] = new UserFunction(func, defaults, this.varargs, this.kwargs, this.argnames);
			return next;
		}

	}
	
	public static final class Raise3 extends Instr {
		public void init0() {}
		
		public int inreg2;
		
		public void initReg(int[] inreg, int[] outreg) {
			inreg0 = inreg[0];
			inreg1 = inreg[1];
			inreg2 = inreg[2];
		}
		
		public Instr run(Frame frame) {
			Obj exc = frame.reg[inreg0];
			Obj value = frame.reg[inreg1];
			Obj traceback = frame.reg[inreg2];
			
			// TODO: implement traceback
			// 'raise' - reraise
			// 'raise IOError' == 'raise IOError()'
			// 'raise IOError, 5' == 'raise IOError(5)'
			
			if(exc == None)
				throw new ExistingScriptError(frame.getException(), frame.getTraceback());
			
			if(value != None) {
				if(exc instanceof ScriptError.ExceptionInstance)
					throw new ScriptError(ScriptError.TypeError, "instance exception may not have a separate value");
				exc = exc.call(new Obj[]{ value });
			} else if(exc instanceof ScriptError.ExceptionType) {
				exc = exc.call(new Obj[]{ value });
			}
			
			throw new ExistingScriptError(exc, null);
		}
	}
	
	public static final class Nop extends Instr {
		public void init0() {}
		public void init1(Obj useless) {}
		
		public Instr run(Frame frame) {
			return next;
		}
	}
	
	public static abstract class BinOp extends Instr {
		public void init1(Obj name) {}
		
		public void operatorFailed(Obj a, Obj b, String name) {
			throw new ScriptError(ScriptError.TypeError, "Unsupported operand type " + name + " for " + Obj.typeRepr(a) + " and " + Obj.typeRepr(b));
		}

		public static Instr createBinOp(Tuple args) {
			if(args.length() != 1)
				throw new ScriptError(ScriptError.TypeError, "Expected 1 argument, got " + args.length());
			SString name = (SString)args.get(0);
			BinOpInstrs.BinOpFactory factory = (BinOpFactory) BinOpInstrs.binOpTypes.get(name.intern());
			return factory.create();
		}
		
		static { 
			BinOpInstrs.binOpTypes.put("is not", new BinOpFactory() {
				public BinOp create() {
					return new BinOp() {
						public Instr run(Frame frame) {
							Obj a = frame.reg[inreg0];
							Obj b = frame.reg[inreg1];
							frame.reg[outreg0] = a!=b? SBool.True: SBool.False;
							return next;
						}
					};
				}
			});
			BinOpInstrs.binOpTypes.put("is", new BinOpFactory() {
				public BinOp create() {
					return new BinOp() {
						public Instr run(Frame frame) {
							Obj a = frame.reg[inreg0];
							Obj b = frame.reg[inreg1];
							frame.reg[outreg0] = a==b? SBool.True: SBool.False;
							return next;
						}
					};
				}
			});
			BinOpInstrs.binOpTypes.put("!=", new BinOpFactory() {
				public BinOp create() {
					return new BinOp() {
						public Instr run(Frame frame) {
							Obj a = frame.reg[inreg0];
							Obj b = frame.reg[inreg1];
							Obj result = a.isEqual(frame, b);
							if(result == null) {
								result = b.isEqual(frame, a);
								if(result == null)
									this.operatorFailed(a, b, "==");
							}
							frame.reg[outreg0] = result.boolValue() ? SBool.False: SBool.True;
							return next;
						}
					};
				}
			});
			BinOpInstrs.binOpTypes.put("in", new BinOpFactory() {
				public BinOp create() {
					return new BinOp() {
						public Instr run(Frame frame) {
							Obj a = frame.reg[inreg0];
							Obj b = frame.reg[inreg1];
							Obj result = b.contains(frame, a)?SBool.True:SBool.False;
							frame.reg[outreg0] = result;
							return next;
						}
					};
				}
			});
			BinOpInstrs.binOpTypes.put("not in", new BinOpFactory() {
				public BinOp create() {
					return new BinOp() {
						public Instr run(Frame frame) {
							Obj a = frame.reg[inreg0];
							Obj b = frame.reg[inreg1];
							Obj result = b.contains(frame, a)?SBool.False:SBool.True;
							frame.reg[outreg0] = result;
							return next;
						}
					};
				}
			});
		}
	}
	
	public static abstract class UnaryOp extends Instr {
		public void init1(Obj name) {}

		public static Instr createOp(Tuple args) {
			SString type = args.get(0).stringValue();
			int interned = type.intern();
			
			if(interned == NOT)
				return new UnaryNot();
			else if(interned == UNARYSUB)
				return new UnarySub();
			else
				throw new ScriptError(ScriptError.TypeError, "unknown unary op: " + type);
		}
		public static final int NOT = SString.intern("not");
		public static final int UNARYSUB = SString.intern("unarysub");
		
		public static class UnaryNot extends UnaryOp {
			public final Instr run(Frame frame) {
				Obj a = frame.reg[inreg0];
				Obj done = a.boolValue()? SBool.False: SBool.True;
				frame.reg[outreg0] = done;
				
				return next;
			}
		}
		
		public static class UnarySub extends UnaryOp {
			public final Instr run(Frame frame) {
				Obj a = frame.reg[inreg0];
				Obj done = a.unarySub(frame);
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
			StringDict dict = (StringDict)frame.reg[inreg0];
			Tuple bases = (Tuple)frame.reg[inreg1];
			
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
			Obj excClass = frame.reg[inreg1];
			Obj exception = frame.reg[inreg0];
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
	
	public static final class GetImportAttr extends Instr {
		public SString name;
		public SString modname;
		
		public void init2(Obj name, Obj modname) {
			this.name = name.stringValue();
			this.modname = modname.stringValue();
		}
		
		public Instr run(Frame frame) {
			Obj obj = frame.reg[inreg0];
			Obj result;
			try {
				result = obj.getAttr(name.intern());
			} catch (ScriptError e) {
				if(e.kind == ScriptError.AttributeError) {
					Importer.importModule((SString)modname.add(".").add(name));
					result = obj.getAttr(name.intern());
				} else {
					throw e;
				}
			}
			frame.reg[outreg0] = result;
			
			return next;
		}

	}
	
	public static final class DelGlobal extends Instr {
		private int name;
		
		public void init1(Obj name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			frame.globals.delete(name);
			return next;
		}

	}
	
	public static final class DelAttr extends Instr {
		private int name;
		
		public void init1(Obj name) {
			this.name = name.stringValue().intern();
		}
		
		public Instr run(Frame frame) {
			Obj obj = frame.reg[inreg0];
			obj.delAttr(name);
			return next;
		}

	}
	
	public static final class ListAppend extends Instr {
		public void init0() {}
		
		public Instr run(Frame frame) {
			Obj list = frame.reg[inreg0];
			Obj item = frame.reg[inreg1];
			
			((List)list).append(item);
			return next;
		}
	}
}
