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
import java.util.Arrays;

public final class SString extends NativeObj { //!export SString BaseString
	public final byte[] bytes;
	public int interned = -1;

	public SString() {
		this.bytes = new byte[0];
	}

	public SString(byte b) {
		this.bytes = new byte[]{ b };
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

	public Obj repr_bytes() { //!export
		SStringBuilder builder = new SStringBuilder(this.length() * 5);

		for(int i=0; i < length(); i++) {
			byte ch = charAt(i);
			builder.append(((int)ch) + ",");
		}

		return builder.getValue();
	}

	public Obj repr() {
		SStringBuilder builder = new SStringBuilder(this.length() * 15 / 10);

		builder.append('"');
		for(int i=0; i < length(); i++) {
			byte ch = charAt(i);
			if(ch == '"')
				builder.append('\\');
			if(ch == '\n')
				builder.append("\\n");
			else
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
		//if(this.interned != -1) // UAHAHAHAHAHAHA
			//return this.interned;

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

	public SString mul(int times) {
		SStringBuilder builder = new SStringBuilder(times * length());
		for(int i=0; i<times; i++)
			builder.append(this);
		return builder.getValue();
	}

	public Obj mul(Obj other) {
		if(other instanceof SInt)
			return this.mul(other.intValue());
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

	public SString strip() { //!export
		int left, right;
		for(left=0; left < length(); left++) {
			byte ch = bytes[left];
			if(!(ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r'))
				break;
		}
		for(right=length()-1; right > left; right--) {
			byte ch = bytes[left];
			if(!(ch == ' ' || ch == '\n' || ch == '\t' || ch == '\t'))
				break;
		}
		return (SString)getSlice(SInt.get(left), SInt.get(right));
	}

	public SString lower() { //!export
		SString n = dataCopy();
		for(int i=0; i<bytes.length; i++)
			n.bytes[i] = (byte) Character.toLowerCase(n.bytes[i]);
		return n;
	}

	public SString upper() { //!export
		SString n = dataCopy();
		for(int i=0; i<bytes.length; i++)
			n.bytes[i] = (byte) Character.toUpperCase(n.bytes[i]);
		return n;
	}

	public boolean contains(Obj other) { //!export
		return find(other, 0) != -1;
	}

	private SString dataCopy() {
		byte[] newBytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
		return new SString(newBytes);
	}

	public int find(Obj[] args) { //!export direct
		if(args.length > 2 || args.length == 0)
			throw new ScriptError(ScriptError.TypeError, "str.find() requires 1 or 2 arguments");
		Obj other = args[0];
		int start;
		if(args.length == 2) {
			start = args[1].intValue();
		} else {
			start = 0;
		}
		return find(other, start);
	}

	public int find(Obj other, int start) {
		SString o = other.stringValue();
		byte[] ob = o.bytes;
		if(ob.length > bytes.length - start)
			return -1;
		if(ob.length * (bytes.length-ob.length) < 500) {
			m:
			for(int i=start; i<bytes.length-ob.length+1; i++) {
				for(int j=0; j<ob.length; j++) {
					if(ob[j] != bytes[i + j])
						continue m;
				}
				return i;
			}
		} else {
			// this is KMP
			// I know that it is useless
			// but tommorow I have contest
			int[] pi = new int[ob.length];

			// compute Knuth prefix function

			int q = 0;
			pi[0] = 0;
			for(int i=1; i<pi.length; ) {
				if(ob[q] == ob[i]) {
					q++;
				} else {
					q = 0;
					if(ob[i] == ob[0])
						continue;
				}
				pi[i] = q;
				i++;
			}

			// find
			q = 0;
			for(int i=start; i<bytes.length; ) {
				if(ob[q] == bytes[i]) {
					q++;
					if(q == ob.length) {
						return i + 1 - ob.length;
						// q = pi[q]
					}
					i++;
				} else if(q == 0) {
					i++;
				} else {
					q = pi[q];
				}
			}
		}
		return -1;
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


	public Obj add(String string) {
		return add(new SString(string));
	}

	public boolean boolValue() {
		return bytes.length != 0;
	}

	public Obj getSlice(Obj lower, Obj upper) {
		int len = this.length();
		int l = (lower == None)? 0 : lower.intValue();
		int u = (upper == None)? len : upper.intValue();
		if(l < 0) {
			l = len + l;
			if(l < 0) l = 0;
		} else if(l >= len) {
			l = len;
		}
		if(u < 0) {
			u = len + u;
			if(u < 0) u = 0;
		} else if(u >= len) {
			u = len;
		}

		byte[] n = new byte[u - l];
		System.arraycopy(this.bytes, l, n, 0, u - l);
		return new SString(n);
	}

	public Obj getItem(int index) {
		int nindex = index;
		if(index < 0)
			nindex = bytes.length + index;
		if(nindex >= bytes.length)
			throw new ScriptError(ScriptError.IndexError, "bad index " + index);
		return new SString(bytes[nindex]);
	}

	public Obj getItem(Obj key) {
		return getItem(key.intValue());
	}

	public Obj compare(Obj other) {
		SString o = (SString)other;
		int min = Math.min(o.bytes.length, bytes.length);
		for(int i=0; i<min; i++) {
			if(o.bytes[i] == bytes[i])
				continue;
			return bytes[i] > o.bytes[i]? SInt.ONE: SInt.MINUS_ONE;
		}
		if(o.bytes.length == bytes.length)
			return SInt.ZERO;

		return o.bytes.length < bytes.length ? SInt.ONE: SInt.MINUS_ONE;
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
				this.interned = key;
			} else {
				this.interned = val.intValue();
			}

			return this.interned;
		}
	}

	public static final SString unintern(int code) {
		SString result;
		synchronized (internTable) {
			result = (SString) reverseInternTable.getOrNull(SInt.get(code));
		}
		if(result == null)
			throw new ScriptError(ScriptError.LookupError, code + " is not a key of an interned string");
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
