// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

import java.io.UnsupportedEncodingException;

public final class SString extends Obj {
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
}
