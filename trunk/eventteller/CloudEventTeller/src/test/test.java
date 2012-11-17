package test;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.BitSet;
import java.util.List;

//import news.crawler.articleCrawlerAndExtractor.ArticleExtractor;
import news.crawler.urlBloom.BloomFilter;

//import org.apache.hadoop.hbase.util.Bytes;



//import org.jsoup.Jsoup;






public class test {
	
	
	
	public static int bytesToInt(byte[] intByte) {
		int fromByte = 0;
		for (int i = 0; i < 3; i++)
		{
		int n = (intByte[i] < 0 ? (int)intByte[i] + 256 : (int)intByte[i]) << (8 * i);
		fromByte += n;
		}
		return fromByte;
	}
	
	

	public static void main(String[] args) throws IOException{


		BloomFilter bf = new BloomFilter();
		BufferedReader br = new BufferedReader(new FileReader(new File("E:\\share\\urls")));
		String line = "";
		int total = 0;
		int in_check = 0 ;
		List<String> lss = new ArrayList<String>();
		while((line = br.readLine()) != null){
			if(Math.abs(line.hashCode() % 3) == 0){
				bf.add(line);				
				in_check++;
			}
			total++;
			lss.add(line);
		}
		br.close();
		int check = 0;
		
		for(String ls : lss){
			if(bf.contains(ls)){
				check++;
			}
		}		
		System.out.println(in_check + " " +check + " " + total);
	}
}