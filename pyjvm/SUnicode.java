package pyjvm;

import java.io.UnsupportedEncodingException;

public final class SUnicode extends SObject {
	public final char[] chars;
	
	public SUnicode() {
		this.chars = new char[0];
	}
	
	public SUnicode(char[] data) {
		this.chars = data;
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

	public static SUnicode createFromUtf8(byte[] bytes) {
		String s;
		try {
			s = new String(bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
		return new SUnicode(s.toCharArray());
	}
	
	// TODO: equals
}
