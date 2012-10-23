package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//import org.apache.hadoop.hbase.util.Bytes;



//import org.jsoup.Jsoup;



//import db.data.Word;
//import java.util.Map;
//import java.util.Date;


//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;

//import db.data.Word;

//import news.crwaler.articleCrawlerAndExtractor.ArticleExtractor;
//import news.core.IDF;
//import news.crwaler.articleCrawlerAndExtractor.ArticleExtractor;
//import news.crwaler.articleCrawlerAndExtractor.ArticleExtractor;
//import news.crwaler.articleCrawlerAndExtractor.ChineseSplit;



public class test {
	
	
	
	public static int bytesToInt(byte[] intByte) {
		int fromByte = 0;
		for (int i = 0; i < 3; i++)
		{
		int n = (intByte[i] < 0 ? (int)intByte[i] + 256 : (int)intByte[i]) << (8 * i);
//		System.out.println(n);
		fromByte += n;
		}
		return fromByte;
	}
	
	

	public static void main(String[] args) throws IOException{

//		String url = "http://news.ifeng.com/world/detail_2012_10/20/18402632_0.shtml";
//		String html = Jsoup.connect(url).get().html();
//		ArticleExtractor ae = new ArticleExtractor(html);
////		String rawtext = ae.title;
//		System.out.println(ae.mainParagraph);
//		System.out.println(ae.getImg());
		
		BufferedReader br = new BufferedReader(new FileReader(new File("E:\\share\\test.hs")));
		long max = 152822270;
		byte[] res = new byte[3];
		String line = "";
		while(true){
			
			line = br.readLine();
			if(line == null)
				break;
			

				
				
				
				byte[] bys = line.getBytes();
				System.out.println(bys.length);
				for(int j = 0 ;j<bys.length -2 ;j=j+3){
					 res[0] = bys[j];
					 res[1] = bys[j+1];
					 res[2] = bys[j+2];
					 long tmp = 	bytesToInt(bys);
					 if(tmp < max){
						 max = tmp;
					 }
				}
			
		
	}
		System.out.println(max);
	}

}