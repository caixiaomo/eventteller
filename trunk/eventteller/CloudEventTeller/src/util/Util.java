package util;

import java.util.List;

public class Util {
	
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStr(List<String> texts){
		String result = "";
		for(String text:texts ){
			result+=text;
		}		
		return result;
	}
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStrForm(List<String> texts){
		String result = "";
		for(String text:texts ){
			result += "<p>    " + text + "</p>\n";
		}		
		return result;
	}

}
