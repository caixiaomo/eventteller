package news.crwaler.articleCrawlerAndExtractor;


import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.chainsaw.Main;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsMainContentExtractor {
	
 
	public  Double MAX = 0.0;
	public  String TEXT = "";
	public  Document DOC ;
	
	
	
	class TagCheck{
		public double text_num = 1;
		public double tag_num = 1;
		
	}
	
	public NewsMainContentExtractor(String url){
		try {
			DOC = Jsoup.connect(url).get();
		}catch(Exception e){
			
		}
	}
	
	public NewsMainContentExtractor(Document DOC_in){
		DOC = DOC_in;
	}
	
	public TagCheck showTag(Element element, int level){

		TagCheck total = new TagCheck();
		total.tag_num = 1;
		if(element.children().size() == 0){
			return total;
		}
		for(Element el : element.children()){
			TagCheck tmp = new TagCheck();
			tmp = showTag(el,level+1);
			if(tmp.text_num != 0 ){
				total.tag_num += tmp.tag_num ;
			}	
			if(el.tagName().equalsIgnoreCase("p") ||  el.tagName().equalsIgnoreCase("br")){
				total.text_num++;
			}			
		}	
		Double res = element.text().length() / total.tag_num * total.text_num;
//		System.out.println(res + " " + element.attr("class") + " " +" " + total.tag_num +" " + total.text_num+" "+ element.text());
		if(res > MAX ){
			MAX = res;
			TEXT = res.toString();
			if(!element.tagName().equalsIgnoreCase("img") || !element.tagName().equalsIgnoreCase("a")||
					!element.tagName().equalsIgnoreCase("font")){
				element.tagName(TEXT);
			}			
		}	
		return total;
	}
	
	
	public Element getMainElement(){
		Element main = null;
		if(DOC == null){
			return null;
		}
		Elements els = DOC.getElementsByTag("body");
		if(els.size() > 0){
			showTag(els.get(0),0);
		}			
		else
			return null;
		if(TEXT.length() <= 0)
			return null;
		Elements mains = DOC.getElementsByTag(TEXT);
		if(mains.size() > 0){
			main = mains.get(0);
		}		
		return main;
			
	}
	
	
	public  List<String> getMainParagraph(Element main){
		List<String> result = new ArrayList<String>();
			if(main != null){
				for(Element el : main.children()){
//					System.out.println(el.tagName() + " " + el.children().size() + " " + el.text());
					if(el.text().contains("编辑")|| el.text().toLowerCase().contains("copyright")&&
							el.children().size() ==0 || el.text().contains("本报记者") || el.text().contains("原标题"))
						continue;
					if(el.tagName().equals("a") || el.tagName().equals("img"))
						continue;
					if(el.children().size() >1 || el.text().trim().length() < 2 
							|| el.tagName().equalsIgnoreCase("span") 
							|| el.tagName().equalsIgnoreCase("div")||el.tagName().equalsIgnoreCase("strong")||el.tagName().contains("."))
						continue;									
					result.add(el.text().trim());
				}
				if(result.size() == 0 && !main.text().toLowerCase().contains("copyright")){
					result.add(main.text());
				}
			}
		return result;
	}
	
	public List<String> cleanImgUrls(List<String> scrs, String line){
		String html = DOC.html();
		List<String> results = new ArrayList<String>();
		int index_last_line = html.indexOf(line);
		if(index_last_line < 0){
			return scrs;
		}		
		for(String scr : scrs){
			int tmp = html.indexOf(scr);
			if(tmp < index_last_line){
				results.add(scr);
			}
		}
		return results;
	}
	
	public List<String> getImgUrls(Element main){
		
		List<String> urls = new ArrayList<String>();
			Elements els = main.getElementsByTag("img");
			if(els.size() == 0){
				main = main.parent();
				els = main.getElementsByTag("img");
			}
			for(Element el : els){
				if(el.children().size() == 0 && el.tagName().equals("img")){
					String tmp_url = el.attr("src");
					if(tmp_url.length() > 0 && tmp_url.indexOf("http") == 0 ){
						urls.add(tmp_url);
					}
				}
			}
		return urls;
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		
		String url = "http://sports.sohu.com/20121020/n355304880.shtml";
		
		NewsMainContentExtractor nmc = new NewsMainContentExtractor(url);

		Element main = nmc.getMainElement();	
		List<String> imgs = new ArrayList<String>();
//		System.out.println(main.tagName());
		
		List<String> mainParagraph = nmc.getMainParagraph(main);
		for(String mp : mainParagraph){
			System.out.println(mp);
		}
		
		if(mainParagraph.size() > 0){
			imgs = nmc.getImgUrls(main);
			imgs = nmc.cleanImgUrls(imgs,mainParagraph.get(mainParagraph.size() -1));
		}
		
//		System.out.println(mainParagraph);
		System.out.println(nmc.MAX + "----" + imgs.size() + "  " + main.id());
		
	}

}
