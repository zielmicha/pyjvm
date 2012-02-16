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

import java.util.Arrays;

public abstract class Instr extends Obj {
	protected Instr next;
	public int lineno;
	public SString filename;
	
	protected int inreg0 = -1;
	protected int inreg1 = -1;
	
	protected int outreg0; 
	
	public void initReg(int[] inreg, int[] outreg) {
		if(outreg.length > 1 || inreg.length > 2)
			throw new ScriptError(ScriptError.TypeError, "Instr " + this + " expects 1 outreg and 2 inregs");
		if(inreg.length >= 1)
			inreg0 = inreg[0];
		if(inreg.length >= 2)
			inreg1 = inreg[1];
		if(outreg.length >= 1)
			outreg0 = outreg[0];
	}
	
	public void init(Tuple args) {
		if(args.length() == 0)
			init0();
		else if(args.length() == 1)
			init1(args.get(0));
		else if(args.length() == 2)
			init2(args.get(0), args.get(1));
		else
			throw new ScriptError(ScriptError.TypeError, "Invalid number of arguments (" + args.length() + ")");
	}
	
	public void init0() {
		throw new ScriptError(ScriptError.TypeError, "Invalid number of arguments (0)");
	}
	
	public void init1(Obj arg) {
		throw new ScriptError(ScriptError.TypeError, "Invalid number of arguments (1)");
	}

	public void init2(Obj arg0, Obj arg1) {
		throw new ScriptError(ScriptError.TypeError, "Invalid number of arguments (2)");
	}
	
	public void setNext2(Instr next) {
		throw new ScriptError(ScriptError.TypeError, "Setting next2 on non-JumpIf like instr");
	}

	public void setupInstr(int id, Instr[] instrs, Tuple data) {
		Tuple args = (Tuple)data.get(0);
		Tuple inreg = (Tuple)data.get(1);
		Tuple outreg = (Tuple)data.get(2);
		
		int next1offset = data.getInt(3);
		int next2offset = data.getInt(4);
		
		if(next1offset != 0) {
			int next1 = id + next1offset;
			this.next = instrs[next1];
		}
		if(next2offset != 0) {
			int next2 = id + next2offset;
			this.setNext2(instrs[next2]);
		}
		
		this.lineno = data.getInt(5);
		
		this.initReg(inreg.toIntArray(), outreg.toIntArray());
		this.init(args);
	}
	
	public String toString() {
		return "<Instr name=" + getClass().getSimpleName() + " lineno=" + lineno + " filename=" + filename +
			" inreg0=" + inreg0 + " outreg0=" + outreg0 + ">";
	}
	
	public static Instr create(int type, Tuple args) {
		switch(type) {
			case INSTR_BINOP:
				return GenericInstrs.BinOp.createBinOp(args);
			case INSTR_CONST:
				return new GenericInstrs.Const();
			case INSTR_USEONLYGLOBALS:
				return new GenericInstrs.UseOnlyGlobals();
			case INSTR_RAISE3:
				return new GenericInstrs.Raise3();
			case INSTR_MAKETUPLE:
				return new GenericInstrs.MakeTuple();
			case INSTR_PRINT:
				return new GenericInstrs.Print();
			case INSTR_MAKEMODULE:
				return new GenericInstrs.MakeModule();
			case INSTR_RETURN:
				return new GenericInstrs.Return();
			case INSTR_SETGLOBAL:
				return new GenericInstrs.SetGlobal();
			case INSTR_GLOBAL:
				return new GenericInstrs.Global();
			case INSTR_JUMPIFNOT:
				return new GenericInstrs.JumpIfNot();
			case INSTR_JUMPIF:
				return new GenericInstrs.JumpIf();
			case INSTR_ASSERTFAIL:
				return new GenericInstrs.AssertFail();
			case INSTR_COMPARE:
				return GenericInstrs.BinOp.createBinOp(args);
			case INSTR_FUNCTION:
				return new GenericInstrs.FunctionInstr();
			case INSTR_MAKEFUNCTION:
				return new GenericInstrs.MakeFunction();
			case INSTR_CALL:
				return new GenericInstrs.Call();
			case INSTR_BINOPIP:
				return GenericInstrs.BinOp.createBinOp(args);
			case INSTR_MAKELIST:
				return new GenericInstrs.MakeList();
			case INSTR_GETITEM:
				return new GenericInstrs.GetItem();
			case INSTR_GETITER:
				return new GenericInstrs.GetIter();
			case INSTR_FORITER:
				return new GenericInstrs.ForIter();
			case INSTR_NOP:
				return new GenericInstrs.Nop();
			case INSTR_SETLOCAL:
			case INSTR_COPY:
				return new GenericInstrs.SetLocal();
			case INSTR_GETATTR:
				return new GenericInstrs.GetAttr();
			case INSTR_SETUPEXC:
				return new GenericInstrs.SetupExc();
			case INSTR_GETEXC:
				return new GenericInstrs.GetExc();
			case INSTR_POPEXC:
				return new GenericInstrs.PopExc();
			case INSTR_UNARYOP:
				return GenericInstrs.UnaryOp.createOp(args);
			case INSTR_GETLOCALSDICT:
				return new GenericInstrs.GetLocalsDict();
			case INSTR_MAKECLASS:
				return new GenericInstrs.MakeClass();
			case INSTR_SETATTR:
				return new GenericInstrs.SetAttr();
			case INSTR_IMPORT:
				return new GenericInstrs.Import();
			case INSTR_EXCMATCH:
				return new GenericInstrs.ExcMatch();
			case INSTR_RERAISE:
				return new GenericInstrs.Reraise();
			case INSTR_GETIMPORTATTR:
				return new GenericInstrs.GetImportAttr(); 
			case INSTR_DELGLOBAL:
				return new GenericInstrs.DelGlobal();
			case INSTR_DELATTR:
				return new GenericInstrs.DelAttr();
			case INSTR_GETSLICE:
				return new GenericInstrs.GetSlice();
			case INSTR_MAKEDICT:
				return new GenericInstrs.MakeDict();
			case INSTR_SETITEM:
				return new GenericInstrs.SetItem();
			case INSTR_LISTAPPEND:
				return new GenericInstrs.ListAppend();
			case INSTR_UNPACKTUPLE:
				return new GenericInstrs.UnpackTuple();
			default:
				throw new ScriptError(ScriptError.ValueError, "Unknown Instr type code (" + type + ")");
		}
	}
	
	public abstract Instr run(Frame frame);
	
	public static final int INSTR_ASSERTFAIL = 0;
	public static final int INSTR_BINOP = 1;     
	public static final int INSTR_BINOPIP = 2;   
	public static final int INSTR_CALL = 3;      
	public static final int INSTR_COMPARE = 4;   
	public static final int INSTR_CONST = 5;     
	public static final int INSTR_COPY = 6;      
	public static final int INSTR_EXCMATCH = 7;  
	public static final int INSTR_FORITER = 8;   
	public static final int INSTR_FUNCTION = 9;  
	public static final int INSTR_GETATTR = 10;  
	public static final int INSTR_GETEXC = 11;   
	public static final int INSTR_GETITEM = 12;
	public static final int INSTR_GETITER = 13;
	public static final int INSTR_GETLOCALSDICT = 14;
	public static final int INSTR_GETSLICE = 15;
	public static final int INSTR_GLOBAL = 16;
	public static final int INSTR_IMPORT = 17;
	public static final int INSTR_JUMPIF = 18;
	public static final int INSTR_JUMPIFNOT = 19;
	public static final int INSTR_LISTAPPEND = 20;
	public static final int INSTR_MAKECLASS = 21;
	public static final int INSTR_MAKEFUNCTION = 22;
	public static final int INSTR_MAKELIST = 23;
	public static final int INSTR_MAKEMODULE = 24;
	public static final int INSTR_MAKETUPLE = 25;
	public static final int INSTR_NOP = 26;
	public static final int INSTR_POPEXC = 27;
	public static final int INSTR_PRINT = 28;
	public static final int INSTR_RAISE3 = 29;
	public static final int INSTR_RERAISE = 30;
	public static final int INSTR_RETURN = 31;
	public static final int INSTR_SETATTR = 32;
	public static final int INSTR_SETGLOBAL = 33;
	public static final int INSTR_SETITEM = 34;
	public static final int INSTR_SETLOCAL = 35;
	public static final int INSTR_SETUPEXC = 36;
	public static final int INSTR_UNARYOP = 37;
	public static final int INSTR_UNPACKTUPLE = 38;
	public static final int INSTR_USEONLYGLOBALS = 39;
	
	public static final int INSTR_GETIMPORTATTR = 40;
	public static final int INSTR_DELATTR = 41;
	public static final int INSTR_DELGLOBAL = 42;
	public static final int INSTR_MAKEDICT = 43;
}

abstract class JumpIfLikeInstr extends Instr {
	public Instr next2;
	
	public void setNext2(Instr next) {
		this.next2 = next;
	}
}
