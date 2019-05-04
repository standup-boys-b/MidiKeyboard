package application;

import java.util.HashMap;

public class MapKeyNote {
	HashMap<String, String> map;
	public MapKeyNote() {
		map = new HashMap<String, String>();
		map.put("1", "79");
		map.put("q", "80"); // A4
		map.put("2", "81");
		map.put("w", "82");
		map.put("3", "0"); //
		map.put("e", "83"); //C4
		map.put("4", "84");
		map.put("r", "85");
		map.put("5", "86");
		map.put("t", "87");
		map.put("6", "0"); //
		map.put("y", "88");
		map.put("7", "89");
		map.put("u", "90"); // G5
		map.put("8", "91");
		map.put("i", "92");
		map.put("9", "93");
		map.put("o", "94");
		map.put("0", "0");
		map.put("p", "95"); //C6
		map.put("-", "96");
//		map.put("\"", "97");
//		map.put("~", "98");
		map.put("@", "97");
		map.put("^", "98");
		map.put("[", "99"); //E6
		map.put("\\", "100"); //F6

		map.put("a", "55");
		map.put("z", "56");
		map.put("s", "57");
		map.put("d", "0"); //
		map.put("x", "58");
		map.put("c", "59"); //C2
		map.put("f", "60");
		map.put("v", "61");
		map.put("g", "62");
		map.put("b", "63");
		map.put("h", "0"); //
		map.put("n", "64");
		map.put("j", "65");
		map.put("m", "66");
		map.put("k", "67");
		map.put(",", "68");
		map.put("l", "69");
		map.put(".", "70");
		map.put(";", "0"); //
		map.put("/", "71"); //C3
		map.put(":", "72");
		map.put("_", "73");
		map.put("]", "74");
	}
	public int get(String s){
		if (map.containsKey(s)){
//			return Integer.parseInt(map.get(s));
			
			int n = Integer.parseInt(map.get(s));
			if (n == 0){
				return 0;
			} else if (n < 75){
				return n+24;
			} else {
				return n-24;
			}
		} else {
			return 0;
		}
	}
}
