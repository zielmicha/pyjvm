// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Unserializer {
	private InputStream in;
	private int pos;
	private SString filename = SString.fromJavaString("<unknown>");

	public Unserializer(InputStream in) {
		this.in = in;
		this.pos = 0;
	}
	
	private byte readByte() throws IOException {
		int val = in.read();
		if(val < 0)
			throw new EOFException();
		pos++;
		return (byte) val;
	}
	
	public Obj read() {
		byte type;
		try {
			type = readByte();
			switch((char)type) {
				case 'I':
					return SInt.get(readInt());
				case 'F':
					return new SFloat(readFloat());
				case 'S':
					return readString();
				case 'U':
					return readUnicode();
				case 'N':
					return None.None;
				case 'B':
					return SBool.get(readBoolean());
				case 'D':
					return readDict();
				case 'L':
					return readList();
				case 'i':
					return readInstrs();
				case 'f':
					return readFunction();
				case 'm':
					filename = (SString)read();
					return read();
				case 'l':
					return null; // remaining of process done by transformer.py, skip
			}
			throw new ScriptError(ScriptError.ValueError, "Invalid type letter (" + ((char)type) + ", at=" + pos + ")");
		} catch (IOException e) {
			throw new ScriptError(ScriptError.IOError, e);
		}
	}

	private Obj readFunction() throws IOException {
		int argcount = readUInt();
		int loadargsCount = readUInt();
		int[] loadargs = readSizedIntTuple(loadargsCount).toIntArray();
		int varcount = readUInt();
		Instr body = (Instr)read();
		return new FunctionConst(argcount, loadargs, varcount, body);
	}

	private double readFloat() throws IOException {
		int length = readUInt();
		byte[] bytes = new byte[length];
		this.readToArray(bytes);
		String val = new String(bytes);
		return Double.parseDouble(val);
	}

	private List readList() throws IOException {
		int length = this.readUInt();
		List list = new List(length);
		for(int i=0; i<length; i++) {
			list.append(this.read());
		}
		return list;
	}

	private StringDict readDict() throws IOException {
		int length = this.readUInt();
		StringDict dict = new StringDict(length);
		
		for(int i=0; i<length; i++) {
			SString key = readString();
			Obj val = read();
			dict.put(key, val);
		}
		
		return dict;
	}

	private boolean readBoolean() throws IOException {
		byte val = readByte();
		if(val == '0')
			return false;
		else if(val == '1')
			return true;
		else
			throw new IOException("Bad boolean value");
	}

	private Unicode readUnicode() throws IOException {
		int length = readUInt();
		byte[] bytes = new byte[length];
		this.readToArray(bytes);
		return Unicode.createFromUtf8(bytes);
	}

	private SString readString() throws IOException {
		int length = readUInt();
		byte[] bytes = new byte[length];
		this.readToArray(bytes);
		return new SString(bytes);
	}

	private void readToArray(byte[] bytes) throws IOException {
		int read = in.read(bytes);
		if(read != bytes.length)
			throw new EOFException();
		pos += bytes.length;
	}

	private int readInt() throws IOException {
		byte b = readByte();
		int sign = ((b & 128) == 0) ? 1: -1;
		int rest = b & 63;
		if((b & 64) == 0)
			return sign * rest;
		else
			return sign * (rest + (readUInt() << 6));
	}
	
	private int readUInt() throws IOException {
		int shift = 0;
		int val = 0;
		while(true) {
			byte b = readByte();
			val += (b & 127) << shift;
			shift += 8;
			if((b & 128) == 0) break;
		}
		return val;
	}
	
	private Instr readInstrs() throws IOException {
		readInt(); // int id=
		readByte();
		int length = readUInt();
		Instr[] instrs = new Instr[length];
		Tuple[] data = new Tuple[length];
		
		for(int i=0; i<length; i++) {
			readInstr(i, instrs, data);
		}
		
		for(int i=0; i<length; i++) {
			instrs[i].setupInstr(i, instrs, data[i]);
		}
		
		return instrs[0];
	}
	
	private Tuple readSizedIntTuple(int size) throws IOException {
		Obj[] values = new Obj[size];
		for(int i=0; i<size; i++) {
			values[i] = SInt.get(readInt());
		}
		return new Tuple(values);
	}
	
	private Tuple readSizedTuple(int size) throws IOException {
		Obj[] values = new Obj[size];
		for(int i=0; i<size; i++) {
			values[i] = read();
		}
		return new Tuple(values);
	}
	
	private void readInstr(int i, Instr[] instrs, Tuple[] data) throws IOException {
		int type = readUInt();
		
		int argsCount = readUInt();
		int inCount = readUInt();
		int outCount = readUInt();
		
		int next1offset = readInt();
		int next2offset = readInt();
	
		Tuple args = readSizedTuple(argsCount);
		Tuple inreg = readSizedIntTuple(inCount);
		Tuple outreg = readSizedIntTuple(outCount);
		int lineno = readUInt();
	
		instrs[i] = Instr.create(type, args);
		instrs[i].filename = filename;
		
		data[i] = new Tuple(new Obj[]{
			args, inreg, outreg,
			SInt.get(next1offset), SInt.get(next2offset), SInt.get(lineno)
		});
	}
}
