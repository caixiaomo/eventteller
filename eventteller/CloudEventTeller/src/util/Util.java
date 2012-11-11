package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStr(List<String> texts,String split){
		String result = "";
		int num = 0;
		for(String text:texts ){
			num++;
			if(num == 1){
				result = text;
			}else{
				result += split +  text ;	
			}		
		}		
		return result;
	}
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStr(List<String> texts,String split,int n){
		String result = "";
		int num = 0;
		for(String text:texts ){
			num++;
			if(num == 1){
				result = text;
			}else{
				result += split +  text ;	
			}		
			if(num > n)
				break;
		}		
		return result;
	}
	
	public static String MD5(String str){
		String result = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			  byte[] dataBytes = str.getBytes();
		      md.update(dataBytes);
		        byte[] mdbytes = md.digest();		 
		        //convert the byte to hex format method 1
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < mdbytes.length; i++) {
		          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		        }
		        //convert the byte to hex format method 2
		        StringBuffer hexString = new StringBuffer();
		    	for (int i=0;i<mdbytes.length;i++) {
		    		String hex=Integer.toHexString(0xff & mdbytes[i]);
		   	     	if(hex.length()==1) hexString.append('0');
		   	     	hexString.append(hex);
		    	}
		    	result =  hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;     
	}
	
	public static String MD5OfByte(byte[] dataBytes){
		String result = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		      md.update(dataBytes);
		        byte[] mdbytes = md.digest();		 
		        //convert the byte to hex format method 1
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < mdbytes.length; i++) {
		          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		        }
		        //convert the byte to hex format method 2
		        StringBuffer hexString = new StringBuffer();
		    	for (int i=0;i<mdbytes.length;i++) {
		    		String hex=Integer.toHexString(0xff & mdbytes[i]);
		   	     	if(hex.length()==1) hexString.append('0');
		   	     	hexString.append(hex);
		    	}
		    	result =  hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;     
	}
	

}
