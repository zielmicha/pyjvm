// Copyright 2011 Michal Zielinski
// for license see LICENSE file
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
