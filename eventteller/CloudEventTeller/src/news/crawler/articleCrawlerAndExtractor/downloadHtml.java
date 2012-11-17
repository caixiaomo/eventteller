package news.crawler.articleCrawlerAndExtractor;


import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;



import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
* @PackageName:news.crwaler.articleCrawler
* @ClassName: downHtmlSource
* @author: mblank
* @date: 2012-2-29 上午10:22:01
* @Description: download the htmlsource from url
* @Marks: TODO
*/
public class downloadHtml {
	

	
	/**
	 * @param url
	 * @return
	 * @throws Exception
	 * @Description:get int[] from url
	 */
	public int[] downloadArticle(String url) throws Exception {
	    URL urlObj = new URL(url);
	    List<Integer> content = new ArrayList<Integer>();
	    BufferedInputStream bi = new BufferedInputStream(urlObj.openStream());
	    int temp = -1;
	    while ((temp = bi.read()) != -1) {
	      content.add(Integer.valueOf(temp));
	    }
	    bi.close();
	    int[] result = new int[content.size()];
	    for (int i = 0; i < content.size(); i++) {
	      result[i] = ((Integer)content.get(i)).intValue();
	    }
	    return result;
	}
	
	/**
	 * @param url
	 * @return
	 * @Description:get html(string) from the url
	 */
	public static String downloadHtmlFromUrl(String url){
		String result = "";
		try {
			Document doc = Jsoup.connect(url).get();
			result = doc.html();
		}catch(UnsupportedCharsetException ucse){
			ucse.printStackTrace();
		}catch(IllegalArgumentException iae){
			iae.printStackTrace();
		}catch(UnknownHostException ue){
			ue.printStackTrace();
		}catch(SocketTimeoutException ste){
			ste.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * @param url
	 * @return
	 * @Description:convert string to bytes
	 */
	public static byte[] toBytes(String url){
		byte[] result = null;
		result = downloadHtmlFromUrl(url).getBytes();		
		return result;
	}
	
	

}
