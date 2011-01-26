package pyjvm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Unserializer {
	private InputStream in;
	private int pos;

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
	
	public SObject read() {
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
				case 'l':
					return null; // remaining of process done by transformer.py, skip
			}
			throw new ScriptError(ScriptError.ValueError, "Invalid type letter (" + ((char)type) + ", at=" + pos + ")");
		} catch (IOException e) {
			throw new ScriptError(ScriptError.IOError, e);
		}
	}

	private SObject readFunction() throws IOException {
		int argcount = readUInt();
		int loadargs = readUInt();
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
			SObject val = read();
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

	private SUnicode readUnicode() throws IOException {
		int length = readUInt();
		byte[] bytes = new byte[length];
		this.readToArray(bytes);
		return SUnicode.createFromUtf8(bytes);
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
		STuple[] data = new STuple[length];
		
		for(int i=0; i<length; i++) {
			readInstr(i, instrs, data);
		}
		
		for(int i=0; i<length; i++) {
			instrs[i].setupInstr(i, instrs, data[i]);
		}
		
		return instrs[0];
	}
	
	private STuple readSizedIntTuple(int size) throws IOException {
		SObject[] values = new SObject[size];
		for(int i=0; i<size; i++) {
			values[i] = SInt.get(readInt());
		}
		return new STuple(values);
	}
	
	private STuple readSizedTuple(int size) throws IOException {
		SObject[] values = new SObject[size];
		for(int i=0; i<size; i++) {
			values[i] = read();
		}
		return new STuple(values);
	}
	
	private void readInstr(int i, Instr[] instrs, STuple[] data) throws IOException {
		int type = readUInt();
		
		int argsCount = readUInt();
		int inCount = readUInt();
		int outCount = readUInt();
		
		int next1offset = readInt();
		int next2offset = readInt();
	
		STuple args = readSizedTuple(argsCount);
		STuple inreg = readSizedIntTuple(inCount);
		STuple outreg = readSizedIntTuple(outCount);
		int lineno = readUInt();
	
		instrs[i] = Instr.create(type, args);
		
		data[i] = new STuple(new SObject[]{
			args, inreg, outreg,
			SInt.get(next1offset), SInt.get(next2offset), SInt.get(lineno)
		});
	}
}
