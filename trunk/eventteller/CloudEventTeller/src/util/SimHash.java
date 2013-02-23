package util;


import java.io.UnsupportedEncodingException;
import org.apache.hadoop.hbase.util.Bytes;

public class SimHash {

	public static int hash(String str) {
			int results = 0;
			int[] weightVector = new int[32];
			///some features 
			String[] words = str.split(" ");
			for (String wd : words) {
				String[] wds = wd.split("\t");
				String word = "";
				int weight = 1;
				if(words.length == 2){
					word = wds[0];
					weight = Integer.valueOf(wds[1]);
				}
				int wd_hash = word.hashCode();
				byte[] hash = Bytes.toBytes(wd_hash);
				/*
				 * get bits of every byte of the hash and add them to the weight
				 * Vector
				 */
				for (int j = 0; j < hash.length; j++) {
					for (int k = 0; k < 8; k++) {
						if ((hash[j] >> (7 - k) & 0x01) == 1)
							weightVector[j * 8 + k] += weight;
						else
							weightVector[j * 8 + k] -= weight;
					}
				}
			}			
			// for bytes result
			byte[] result = new byte[4];
			/*
			 * Convert weightVector to hash number by setting every bit >0 to 1
			 * and all the others to 0
			 */
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < 8; j++) {
					if (weightVector[i * 8 + j] > 0) {
						result[i] |= 1 << (7 - j);
					}
				}
			}
			results = Bytes.toInt(result);
			return results;
	}

	
	/**
	 * @param num_a
	 * @param num_b
	 * @return different bits of two number
	 * @Description:
	 */
	public static int diffBitsOfNums(int num_a , int num_b){
		int result = 0;
		num_a ^= num_b;		
		while(num_a >0)
		{
		   num_a&=(num_a-1);
		   result++;
		}		
		return result;
	}

	public static void main(String[] args) throws UnsupportedEncodingException  {

		String str1 = "我是一个人	1 他	2";
		String str2 = "我是一个人	1 他	2";

		
		int hash1 = hash(str1);
		int hash2 = hash(str2);
		System.out.println(hash1);
		System.out.println(hash2);

		int diff = 0;

		diff = diffBitsOfNums(hash1,hash2);
		System.out.println("Document 1 and 2 differ by " + diff);
	}

}