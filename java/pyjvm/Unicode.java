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

public final class Unicode extends Obj {
	public final char[] chars;

	public Unicode() {
		this(new char[0]);
	}

	public Unicode(char[] data) {
		throw new NotImplementedError();
		//this.chars = data;
	}

	public String toString() {
		return new String(chars);
	}

	public int hashCode() {
		int h = 0;
		char val[] = chars;
		int len = chars.length;

		for (int i = 0; i < len; i++) {
		    h = 31*h + val[i];
		}
		return h;
	}

	public static Unicode createFromUtf8(byte[] bytes) {
		String s;
		try {
			s = new String(bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ScriptError(ScriptError.InternalError, "utf-8 encoding is not present", e);
		}
		return new Unicode(s.toCharArray());
	}

	// TODO: equals
}
