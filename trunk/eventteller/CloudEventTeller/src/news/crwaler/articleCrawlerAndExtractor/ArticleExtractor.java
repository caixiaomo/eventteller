package news.crwaler.articleCrawlerAndExtractor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.TreeMap;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//import org.jsoup.select.Elements;

import util.Const;
import util.Util;

/**
* @PackageName:news.extractor
* @ClassName: ArticleExtractor
* @author: mblank
* @date: 2012-3-6 下午8:47:44
* @Description: in this class,we will do somethings below:
* @Marks:  1.accoding the url ,download the htmlsource using "Jsoup" package
* @Marks:  2.from htmlsource,using "Jsoup" to get the plaintext(text only)
* @Marks:  3.from plaintext,using some rules to get most posible title and publishtime,not reliable
* @Marks:  4.from plaintext,using some rules to get mainparagraph 
* @Marks:  5.split mainparagraph and title to words List,computing their frequency(using ikanalyzer.jar to split chinese words)
* @Marks:  6.combine title words and mainparagraph words togethor           
* @Marks: TODO
*/
public class ArticleExtractor {
	
	public String HTML;
	public Document DOC;
//	public List<String> plaintext;
//	public String rawtext;
	public String title;
	public String publishtime;
	public String mainParagraph;
//	public String rawtitle;
//	public List<String> cleanRawText;
//	public static final char[] NAVIALSIGNS=new char[]{'-','|','>','/','<'};
//	public static final String[] STARTSPECSIGNS=new String[]{"?"};
	
	public ArticleExtractor(String html){
		this.HTML = html;
		try{
			DOC = Jsoup.parse(HTML);	
		}catch(Exception e){
			e.printStackTrace();
		}
//		rawtext = getPlainText();
//		plaintext = filterShortStr(rawtext);
		mainParagraph = getMainParagraph();
//		rawtitle = getRawTitle();
		title = getTitle();
		publishtime = getPublishTime();
//		cleanRawText = cleanRawText();
		
	}
	
	/**
	 * @return
	 * @Description:get all the text in the html
	 */
	public String getPlainText(){
		String result = "";
		try{
			result = DOC.text();
		}catch(Exception e){
			e.printStackTrace();
		}		
		return result;
	}
//	
//	public String getRawTitle(){
//		if(DOC!=null){
//			return DOC.title();
//		}else
//			return "";		
//	}
	
	
	public String cleanTitle(String rawTitle){
		if(rawTitle.indexOf("_") > 0){
			rawTitle = rawTitle.substring(0, rawTitle.indexOf("_"));
		}
		if(rawTitle.indexOf("(") > 0){
			rawTitle = rawTitle.substring(0, rawTitle.indexOf("("));
		}
		if(rawTitle.indexOf("[") > 0){
			rawTitle = rawTitle.substring(0, rawTitle.indexOf("["));
		}
		if(rawTitle.indexOf("--") > 0){
			rawTitle = rawTitle.substring(0, rawTitle.indexOf("--"));
		}
		
		return rawTitle;
	}
	
	public String getTitle(){
		String result = "";
		if(DOC == null)
			return result;
		String rawTitle = DOC.title();
		result = rawTitle;
		Elements elh1 = DOC.getElementsByTag("h1");
		if(elh1.size() > 0){
			for(Element el : elh1){
				if(el.children().size() == 0){
					result = el.text();
					break;
				}
			}
			if(!rawTitle.contains(result)){
				result = rawTitle;
			}else{
				return result;
			}
		}
		/// fuck southcn...
		if(result.contains("南方网")){
			Element titles_tag = DOC.getElementById("ScDetailTitle");
			if(titles_tag != null){
				result = titles_tag.text();
			}
		}		
		result = cleanTitle(result);		
		return result;
	}
	
	/**
	 * @param str
	 * @return
	 * @Description:change string to time format
	 */
	public String strToTime(String str){
		String result =str;
		result = result.replace("年", "-");
		result = result.replace("月", "-");
		result = result.replace("日", "-");
		result = result.replace("时", "-");
		result = result.replace("分", "-");
		result = result.replace("秒", "-");
		result = result.replace("_", "-");
		result = result.replace(":", "-");
		result = result.replace(" ", "-");
		result = result.replaceAll("\\p{P}", "-");
		return result;
	}
		
	/**
	 * @return
	 * @Description:get the publishtime from text,not reliable!!!
	 */
	public String getPublishTime(){
		String result = "";
		int title_pos = 0;
		int pos = 0,min_pos = 10000;
		String[]  plaintext_temp = getPlainText().split(" ");
		for(int i=0;i<plaintext_temp.length;i++){
			if(title.equalsIgnoreCase(plaintext_temp[i])){
				title_pos = i;
				break;
			}
		}
		for(int j=0;j<plaintext_temp.length;j++){
			if(plaintext_temp[j].contains("2012")||plaintext_temp[j].contains("2010")||plaintext_temp[j].contains("2011")){
				pos = j - title_pos;
				if(pos>=0&&pos<min_pos){
					min_pos=j;					
				}
			}
		}
		if(min_pos!=10000){
			result = plaintext_temp[min_pos];
		}
		if(result.length()>20||result.length()<8){
			result = "";
		}	
		return strToTime(result);
	}
	
//	/**
//	 * @return
//	 * @Description:remove the logo of website from raw html
//	 */
//	public String filterSiteLogo(){
//		String[] split_raw = rawtext.split(" ");
//		String result = "";
//		if(split_raw.length<2*Const.SiteLogoLineCount){
//			return rawtext;
//		}
//		for(int i =0 ;i<split_raw.length-Const.SiteLogoLineCount;i++){
//			result += split_raw[i] + " ";
//		}		
//		return result;
//	}
	
//	/**
//	 * @param elements
//	 * @return
//	 * @Description:remove dupLine from the list
//	 */
//	public List<String> removeDupLine(List<String> elements){
//	
//		List<String> results=new ArrayList<String>();
//		HashMap<String,Boolean> map=new HashMap<String,Boolean>();
//		for(int i=0;i<elements.size();i++){
//			String element=elements.get(i);
//			if(map.containsKey(element)){
//				continue;
//			}else{
//				results.add(element);
//				map.put(element, true);
//			}
//		}
//		///remove title string from the mainparagraph
//		if(results.contains(rawtitle)){
//			results.remove(rawtitle);
//		}		
//		return results;
//	}
			
//	/**
//	 * @param elements
//	 * @return
//	 * @Description:sort the List by string length
//	 * @Mark:not used in this version!
//	 */
//	public List<String> sortRawTextByLen(List<String> elements){
//		List<String> results = new ArrayList<String>();
//		Map<Integer,String> temp_mp = new TreeMap<Integer,String>();
//		for(int i =0;i<elements.size();i++){
//			String tempi = elements.get(i);
//			temp_mp.put(tempi.length(), tempi);
//		}
//		Iterator<Integer> it_results = temp_mp.keySet().iterator();
//		while(it_results.hasNext()){
//			int len = it_results.next();
//			String temps = temp_mp.get(len);
//			results.add(temps);
//		}
//		return results;
//	}
	
	/**
	 * @param text
	 * @return
	 * @Description:split string to words
	 */
	public List<String> splitStrToWords(String text){
		List<String> result = new ArrayList<String>();
		result = ChineseSplit.SplitStr(text);		
		return result;
	}
	
	/**
	 * @return
	 * @Description:split the title to word list
	 */
	public List<String> splitTitleToWords(){
		List<String> result = new ArrayList<String>();
		if(title==null||title.equalsIgnoreCase("")){
			title = getTitle();
		}
		result = splitStrToWords(title);
		return result;
	}
	
	/**
	 * @return
	 * @Description:split the title to word list
	 */
	public List<String> splitMainParagraphToWords(){
		List<String> result = new ArrayList<String>();
		if(mainParagraph==null||mainParagraph.equalsIgnoreCase("")){
			mainParagraph = getMainParagraph();
		}
		result = splitStrToWords(mainParagraph);
		return result;
	}
	
//	/**
//	 * @param texts
//	 * @return
//	 * @Description:according the title split words ,filter the article
//	 * @e.g. if title words are xx1 xx2 xx3,if the string not contain any of them
//	 * @e.g. it will be filtered 
//	 */
//	public List<String> removeInvalidStr(List<String> texts){
//		List<String> results = new ArrayList<String>();
//		List<String> title_split = new ArrayList<String>();
//		title_split = splitTitleToWords();
//		for(String text : texts){
//			boolean check = false;
//			for(String tw:title_split){
//				if(text.contains(tw)){
//					check = true;
//					break;
//				}
//			}
//			if(check){
//				results.add(text);
//			}
//		}
//		return results;
//	}
	
//	/**
//	 * @param c
//	 * @param str
//	 * @return
//	 * @Description:how many input char contains in the str
//	 */
//	public static int getSignCount(char c,String str){
//		int count=0;
//		for(int i=0;i<str.length();i++){
//			if(str.charAt(i)==c){
//				count++;
//			}
//		}
//		return count;
//	}
	
//	/**
//	 * @param texts
//	 * @return
//	 * @Description:remove some navigate sentences using filter the navialsigns
//	 */
//	public List<String> removeSpecialSigns(List<String> texts){
//		List<String> results = new ArrayList<String>();
//		for(String text:texts){
//			boolean check = false;
//			for(int i=0;i<text.length();i++){
//				for(int j=0;j<NAVIALSIGNS.length;j++){
//					if(getSignCount(NAVIALSIGNS[j],text)>=Const.NoMeaningSigns){
//						check = true;
//						break;
//					}
//				}
//				if(check)
//					break;
//			}
//			if(!check)
//				results.add(text);
//		}		
//		return results;
//	}
	
//	public int getMostNumberFromSentences(Map<Integer,Integer> scrs){
//		Iterator<Integer> it_nums = scrs.keySet().iterator();
//		int max = 0;
//		int results = 0;
//		while(it_nums.hasNext()){
//			int num = it_nums.next();
//			if(scrs.get(num) > max){
//				results = num;
//				max = scrs.get(num);
//			}
//		}
//		return results;
//	}
	

//	
//	public double getAvgStationOfSentences(List<String> scrs){
//		double results =0 ;
//		if(scrs.size()>= 1){
//			results = HTML.indexOf(scrs.get(scrs.size()-1));
//		}	
//		return results;		
//	}
	

//	public String getImg(){
//		String results = "";
//		double avgLen = 0;
//		List<String> sentences = new ArrayList<String>();
//		sentences = cleanRawText();
//		avgLen = getAvgStationOfSentences(sentences);
//		results = getMostPosiblePic(avgLen);
//		return results;
//	}
	
	public String getImg(){
		String results = "";
		results = getMostPosiblePic();
		return results;
	}
	
	public String getMainParagraph(){
		String result = "";
		NewsMainContentExtractor nmce = new NewsMainContentExtractor(DOC);
		Element main = nmce.getMainElement();
		result = Util.ListToStrForm(nmce.getMainParagraph(main));
		return result;
	}
	
	public String getFormatMainParagraph(){
		return mainParagraph;
	}
	
	
	public String getMostPosiblePic(){
		String result = "";
		if(DOC == null){
			try{
				DOC = Jsoup.parse(HTML);	
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		NewsMainContentExtractor nmce = new NewsMainContentExtractor(DOC);
		Element main = nmce.getMainElement();
		List<String> imgs = nmce.getImgUrls(main);
		
		System.out.println(imgs.size());
		
		List<String> mainP = nmce.getMainParagraph(main);
		imgs = nmce.cleanImgUrls(imgs,mainP.get(mainP.size() -1));
		if(imgs.size() > 0)
			result = imgs.get(0);	
		return result;
	}
	
	
	/**
	 * @param texts
	 * @return
	 * @Description:from list get the frequency of words
	 */
	public List<String> getFrequencyOfWords(List<String> texts){
		Map<String,Integer> mp_result = new HashMap<String,Integer>();
		List<String> results = new ArrayList<String>();
		for(String text:texts){
			if(mp_result.containsKey(text)){
				int num = mp_result.get(text);
				mp_result.put(text, num+1);
			}else{
				mp_result.put(text, 1);
			}
		}
		Iterator<String> it_result = mp_result.keySet().iterator();
		while(it_result.hasNext()){
			String temp = it_result.next();
			int temp_num = mp_result.get(temp);
			temp = temp + " " + String.valueOf(temp_num)+",";
			results.add(temp);
		}		
		return results;
	}
	
	/**
	 * @return
	 * @Description:get string of tilte words with frequency
	 */
	public String titleWordsWithFrequency(){
		String result = "";
		List<String> ls_in = new ArrayList<String>();
		ls_in = getTitleWordsWithFrequency();
		result = Util.ListToStr(ls_in);
		return result;
	}
	
	/**
	 * @return
	 * @Description:get List of title words with frequency
	 */
	public List<String> getTitleWordsWithFrequency(){
		List<String> ls_in = new ArrayList<String>();
		ls_in = splitTitleToWords();
		ls_in = getFrequencyOfWords(ls_in);
		return ls_in;
	}
	
	/**
	 * @return
	 * @Description:get string of mainParagraph words with frequency
	 */
	public String mainParagraphWordsWithFrequency(){
		String result = "";
		List<String> ls_in = new ArrayList<String>();
		ls_in = getMainParagraphWordsWithFrequency();
		result = Util.ListToStr(ls_in);
		return result;
	}
	
	/**
	 * @return
	 * @Description:get List of MainParagraph words with frequency
	 */
	public List<String> getMainParagraphWordsWithFrequency(){
		List<String> ls_in = new ArrayList<String>();
		ls_in = splitMainParagraphToWords();
		ls_in = getFrequencyOfWords(ls_in);
		return ls_in;
	}
	
	
	/**
	 * @return
	 * @Description:split mainParagraph to sentences
	 */
	public List<String> getRawSentences(){
		List<String> results = new ArrayList<String>();
		if(mainParagraph==null||mainParagraph.equalsIgnoreCase("")){
			mainParagraph = getMainParagraph();
		}
		String[] str_temp = mainParagraph.split(Const.Split_To_Sentences_Sign);
		for(String str:str_temp){
			if(str.length()>0&&str!=null){
				results.add(str);
			}
		}			
		return results;
	}
	
	/**
	 * @return
	 * @Description:combine title words with mainparagraph words to get summary words
	 * @Mark:title word will have higher weigh
	 */
	public List<String> getSummaryList(){
		List<String> ls_raw_title = new ArrayList<String>();
		List<String> ls_raw_main = new ArrayList<String>();
		List<String> results = new ArrayList<String>();
		Map<String,Integer> mp_results = new HashMap<String,Integer>(); 
		ls_raw_title = splitTitleToWords();
		ls_raw_main = splitMainParagraphToWords();
		
		for(String title :ls_raw_title){
			if(mp_results.containsKey(title)){
				int value = 1;
				value = mp_results.get(title);
				mp_results.put(title, value+Const.SummaryTitleWordsWeighs);
			}else{
				mp_results.put(title, Const.SummaryTitleWordsWeighs);
			}
		}
		for(String title :ls_raw_main){
			if(mp_results.containsKey(title)){
				int value = 1;
				value = mp_results.get(title);
				mp_results.put(title, value+1);
			}else{
				mp_results.put(title, 1);
			}
		}
		Iterator<String> it_words = mp_results.keySet().iterator();
		while(it_words.hasNext()){
			String temp = "";
			temp = it_words.next();
			int num = 0;
			num = mp_results.get(temp);
			temp = temp + " " + String.valueOf(num)+",";
			results.add(temp);
		}
		return results;
	}
	
	/**
	 * @return
	 * @Description:get string of tilte words with frequency
	 */
	public String SummaryWordsWithFrequency(){
		String result = "";
		List<String> ls_in = new ArrayList<String>();
		ls_in = getSummaryList();
		result = Util.ListToStr(ls_in);
		return result;
	}
	
	
	
}
