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

public class SStringBuilder {
	private byte[] bytes;
	private int length = 0;
	
	public SStringBuilder(int initialCapacity) {
		bytes = new byte[initialCapacity];
	}
	
	public void append(SString s) {
		checkAppend(s.length());
		s.copyTo(this.bytes, this.length);
		this.length += s.length();
	}
	
	public void append(byte ch) {
		checkAppend(1);
		this.bytes[length] = ch;
		this.length ++;
	}
	
	private void checkAppend(int length) {
		if(length + this.length >= this.bytes.length) {
			byte[] oldBytes = this.bytes;
			this.bytes = new byte[oldBytes.length * 2];
			System.arraycopy(oldBytes, 0, this.bytes, 0, this.length);
		}
	}

	public SString getValue() {
		byte[] dest = new byte[length];
		System.arraycopy(bytes, 0, dest, 0, length);
		return new SString(dest);
	}

	public void append(char c) {
		append((byte)c);
	}
}
