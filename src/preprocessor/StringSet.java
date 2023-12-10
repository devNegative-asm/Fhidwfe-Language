package preprocessor;

public class StringSet {
	int len = 0;
	String[] strings = new String[10];
	public StringSet() {
		
	}
	public boolean add(String s) {
		if(len < strings.length) {
			strings[len++] = s;
		} else {
			String[] newStrings = new String[len*2];
			System.arraycopy(strings, 0, newStrings, 0, len);
			strings[len++] = s;
		}
		return true;
	}
	public boolean contains(String e) {
		for(int i=0;i<len;i++) {
			if(strings[i]==e)
				return true;
		}
		return false;
	}
	public void clear() {
		len = 0;
	}
}
