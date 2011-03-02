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

import java.io.UnsupportedEncodingException;

public final class SString extends NativeObj { //!export SString BaseString
	public final byte[] bytes;
	public int interned = -1;
	
	public SString() {
		this.bytes = new byte[0];
	}
	
	public SString(String data) {
		this(data.getBytes());
	}
	
	public SString(byte[] data) {
		this.bytes = data;
	}
	
	public String toString() {
		return new String(bytes);
	}
	
	public Obj repr() {
		SStringBuilder builder = new SStringBuilder(this.length() * 15 / 10);
		
		builder.append('"');
		for(int i=0; i < length(); i++) {
			byte ch = charAt(i);
			if(ch == '"')
				builder.append('\\');
			builder.append(ch);
		}
		builder.append('"');
		
		return builder.getValue();
	}
	
	public byte charAt(int pos) {
		return bytes[pos];
	}
	
	public void copyTo(byte[] dest, int offset) {
		System.arraycopy(this.bytes, 0, dest, offset, this.length());
	}
	
	public int length() {
		return bytes.length;
	}

	public int hashCode() {
		if(this.interned != -1)
			return this.interned;
		
		int h = 0;
		byte val[] = bytes;
		int len = bytes.length;
		
		for (int i = 0; i < len; i++) {
		    h = 31*h + val[i];
		}
		return h;
	}
	
	public SString add(SString other) {
		byte[] newBytes = new byte[this.length() + other.length()];
		this.copyTo(newBytes, 0);
		other.copyTo(newBytes, this.length());
		return new SString(newBytes);
	}
	
	public Obj add(Obj other) {
		if(other instanceof SString)
			return this.add((SString)other);
		return NotImplemented;
	}
	
	public SString join(Obj seq) { //!export 
		Obj iter = seq.getIter();
		SStringBuilder builder = new SStringBuilder(16);
		boolean first = true;
		Obj item;
		
		while((item=iter.next()) != null) {
			if(!first) {
				builder.append(this);
			} else {
				first = false;
			}
			builder.append(item.stringValue());
		}
		
		return builder.getValue();
	}
	
	public static Obj construct(Obj val) { //!export - <new>
		return val.str();
	}
	
	public SBool isEqual(Obj other) {
		if(other == this)
			return SBool.True;
		if(other instanceof SString) {
			SString casted = (SString) other;
			if(this.interned != -1 && casted.interned != -1)
				return this.interned == casted.interned? SBool.True: SBool.False;
			byte[] bytes = this.bytes;
			byte[] otherBytes = casted.bytes;
			if(bytes.length != otherBytes.length)
				return SBool.False;
			for(int i=0; i<bytes.length; i++) {
				if(bytes[i] != otherBytes[i])
					return SBool.False;
			}
			return SBool.True;
		} else {
			return null;
		}
	}

	public SString stringValue() {
		return this;
	}
	
	public static SString fromJavaString(String string) {
		try {
			return new SString(string.getBytes("utf8"));
		} catch (UnsupportedEncodingException ex) {
			throw new ScriptError(ScriptError.LookupError, ex);
		}
	}
	
	public Type getType() {
		return SStringClass.instance;
	}
	
	// intern
	
	public final int intern() {
		if(this.interned != -1)
			return this.interned;
		synchronized(internTable) {
			Obj val = internTable.getOrNull(this);
			if(val == null) {
				int key = nextIdent++;
				internTable.put(this, SInt.get(key));
				reverseInternTable.put(SInt.get(key), this);
				return this.interned=key;
			} else {
				return this.interned=((SInt)val).value; 
			}
		}
	}
	
	public static final SString unintern(int code) {
		SString result;
		synchronized (internTable) {
			result = (SString) reverseInternTable.getOrNull(SInt.get(code));
		}
		if(result == null)
			throw new ScriptError(ScriptError.LookupError, code + " is not a key of interned string");
		return result;
	}
	
	public static final Dict internTable = new Dict(128);
	public static final Dict reverseInternTable = new Dict(128);
	private static int nextIdent = 0;

	public static int intern(String s) {
		return new SString(s).intern();
	}

	public static SString uninternQuiet(int key) {
		try {
			return unintern(key);
		} catch(ScriptError err) {
			return new SString("<" + key +" is not a key of an interned string>");
		}
	}

	public Obj add(String string) {
		return add(new SString(string));
	}
	
	public boolean boolValue() {
		return bytes.length != 0;
	}
}
