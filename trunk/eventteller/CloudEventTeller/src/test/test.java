package test;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
import java.io.IOException;

import news.crwaler.articleCrawlerAndExtractor.ArticleExtractor;

//import org.apache.hadoop.hbase.util.Bytes;



import org.jsoup.Jsoup;



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
		fromByte += n;
		}
		return fromByte;
	}
	
	

	public static void main(String[] args) throws IOException{

		String url = "http://sports.163.com/12/1023/22/8EHKU9MV00051C89.html";
		String html = Jsoup.connect(url).get().html();
		ArticleExtractor ae = new ArticleExtractor(html);
//		String rawtext = ae.title;
		System.out.println(ae.mainParagraph);
		System.out.println(ae.getImgs());
		
	}
}