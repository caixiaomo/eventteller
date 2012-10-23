package news.crwaler.webSiteCrawler;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


import java.util.Set;

import org.dom4j.Element;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;



import db.HSession;
import db.data.titleNews;

import news.filter.titleNewsFilter;
import news.model.WebSite;
import util.Config;
import util.Const;
import util.Log;
/**
* @PackageName:news.crwaler.webSiteCrawler
* @ClassName: WebSiteCrawler
* @author: mblank
* @date: 2012-2-27 上午10:11:44
* @Description: From the WebSite.xml,get all urls from the page 
* @Marks: (1)use jsoup to parser the html,in the filter,contains average title length
* @Marks: (1)and average url length ,two parameters in the WebSite.xml which get from statistics
* @Marks: (2)basic filter such as http://news.sina.com.cn/w/2012-02-27/xxxxxx.shtml
* @Marks: (2)1.news.sina.com 2.w ,we will spilt the url by "/" so,we will get some parts of the url
* @Marks: (3)anthoer basic filter is ".shtml" or "html" or "htm" we will check the end of the url
* @Marks: (4)addSomeConditon() this is for some condition that in the page,the url get by jsoup is not
* @Marks: (4)coplement,so we must predo for the url,such as add some string to the url
* @Marks: (5)We will update the disapper title  and insert new title for db update,the disapper is that
* @Marks: (5)now we can get a url in a page, but after some time ,it will replace by other url.so we check
* @Marks: (5)the time and update it. 
* @Marks: (6)disappertime is not reliable,because we can't confirm the net environment,when can't connect to
* @Marks: (6)the url,the system will get no url from web,so it will be set to disapper
*/
public class WebSiteCrawler {
	

	private Session session ;
		
	public WebSiteCrawler(){
		Log.getLogger().info("Start TitleCrawler!");
		session = new HSession().createSession();
	}
	
	
	/**
	 * @param url
	 * @return
	 * @Description:get jsoup_doc from url
	 */
	public Document getJsoupDocument(String url){
		Document doc = null;
		try{
			doc = Jsoup.connect(url).get();
		}catch(IOException e){
			e.printStackTrace();
		}	
		return doc;
	}
	

	/**
	 * @param doc
	 * @param query
	 * @return Elements
	 * @Description:get all the selected elements
	 */
	public Elements getAllElements(Document doc,String query){
		Elements els = null;
		els = doc.select(query);
		return els;
	}
	
	/**
	 * @param doc
	 * @return
	 * @Description:from doc get all the urls
	 * @mark:there is a basic filter,if the url_title_length less than 
	 * Const.WebSiteUrlTitleFilerLength,than it will not be return
	 */
	public Map<String,String> getALLhrefs(Document doc){
		Map<String,String> result = new HashMap<String,String>();
		Elements els = null;
		els = doc.getElementsByTag("a");
		String name = "";
		String url_name = "";
		Iterator<org.jsoup.nodes.Element> it_el = els.iterator();
		while(it_el.hasNext()){
			org.jsoup.nodes.Element el = it_el.next();
			name = el.text();
			url_name = el.attr("href");
			if(name.length()>Const.WebSiteUrlTitleFilerLength){
				//check url exists or not!
				if(!result.containsKey(url_name)){
					result.put(url_name, name);
				}
			}
		}		
		return result;
	}
	
	/**
	 * @param scr
	 * @return Map<String,String>, url + title
	 * @Description: filter of the urls
	 */
	public Map<String,String> filterForHrefs(Map<String,String> scr){
		Map<String,String> results = new HashMap<String,String>();
		Set<String> st_url = scr.keySet();
		Iterator<String> it_url = st_url.iterator();
		while(it_url.hasNext()){
			String url = it_url.next();
			if(url.contains(".html")&&(url.length()-url.lastIndexOf(".html")==5)){
				results.put(url, scr.get(url));
			}
			if(url.contains(".shtml")&&(url.length()-url.lastIndexOf(".shtml")==6)){
				results.put(url, scr.get(url));
			}
			if(url.contains(".htm")&&(url.length()-url.lastIndexOf(".htm")==4)){
				results.put(url, scr.get(url));			
			}	
		}	
		return results;
	}
	
	/**
	 * @param url
	 * @return url+title
	 * @Description:give a url ,return all the urls and titles
	 */
	public Map<String,String> getUrls(String url){
		Map<String,String> result = new HashMap<String,String>();
		try{
			Document doc = Jsoup.connect(url).timeout(5000).get();
			result = getALLhrefs(doc);
			result = filterForHrefs(result);	
		}catch(Exception e){
			Log.getLogger().error("can't connect to the url: "+url);
			e.printStackTrace();
		}	
		return result;
	}
	
	/**
	 * @param tnf
	 * @param tn
	 * @return
	 * @Description:according to the filters in WebSite.xml,filter some urls;
	 */
	public boolean filterTitleNews(titleNewsFilter tnf,titleNews tn){
		boolean bo_result = true;
		String[] check_str = tnf.getStr_filter();
		String url = tn.getUrl();
		url = url.replaceAll("//", "/");
		String[] url_spilt = url.split("/");
		//there is no filter for the url
		if(check_str[1].equalsIgnoreCase("null")){
			return true;
		}		
		for(int i=1;i<check_str.length;i++){
			if(!url_spilt[i].equalsIgnoreCase(check_str[i]))
				bo_result = false;
		}	
		if(tn.getTitle()==null){
			return false;
		}	
		if(url.length()<=tnf.getUrl_avg_len()||tn.getTitle().length()<tnf.getTitle_avg_len()){
			return false;
		}
		return bo_result;
	}
	
	/**
	 * @param ws
	 * @param str_url
	 * @param url
	 * @return
	 * @Description:add some str to the url crawl from the page
	 */
	public String addSomeConditon(WebSite ws,String str_url,String url){

		if(!url.contains("http")){
			if(ws.SiteName.equalsIgnoreCase("tencent")){
				if((str_url.indexOf(".shtml")>0)){
					url = "http://news.qq.com" + url;
				}
			}else if(ws.SiteName.equalsIgnoreCase("chinanews")){
				
				if(url.indexOf("/ty")==0){
					url = "sports.chinanews.com" +url;
				}else{
					url = "http://www.chinanews.com" +url;
				}								
			}else if(str_url.equalsIgnoreCase("http://news.cyol.com/node_10005.htm")||str_url.equalsIgnoreCase("http://news.cyol.com/node_10006.htm")){
				url = "http://news.cyol.com/"+url;
			}else if(str_url.equalsIgnoreCase("http://news.china.com/social")||str_url.equalsIgnoreCase("http://news.china.com/international")||str_url.equalsIgnoreCase("http://news.china.com/domestic")){
				url = "http://news.china.com" + url;
			}else if(str_url.contains("china.com.cn")&&str_url.contains("node_")){
				String[] temp = str_url.split("/");
				url = temp[0]+"//"+temp[2]+"/"+temp[3]+"/"+url;
			}else if(str_url.contains("gmw.cn")&&str_url.contains("node_")){
				String[] temp = str_url.split("/");
				url = temp[0]+"//"+temp[2]+"/"+"/"+url;
			}else{
				url = str_url + url;
			}
		}
		return url;
	}
	
	
	/**
	 * @param ws
	 * @return
	 * @Description:From WebSite,get all the site urls
	 */
	public List<titleNews> getAllTitleNewsFromWebSite(WebSite ws){
		Map<String,titleNews> mp_tn = new HashMap<String,titleNews>();
		List<titleNews> ls_result = new ArrayList<titleNews>();
		Set<String> st_url = ws.Sites.keySet();
		Iterator<String> it_url = st_url.iterator();
		while(it_url.hasNext()){
			
			int url_len = 0;
			int title_len = 0;
			int url_size = 0;
			int title_size = 0;
			String str_url = it_url.next();		
			Map<String,String> mp_url = new HashMap<String,String>();
			mp_url = getUrls(str_url);   			
			Set<String> st_mp = mp_url.keySet();			
			Iterator<String> it_urls = st_mp.iterator();			
			while(it_urls.hasNext()){
				
				String tilte_temp = "";
				String url = it_urls.next();
				tilte_temp = mp_url.get(url);
				url = addSomeConditon(ws,str_url,url);
				titleNews tn = new titleNews();
				tn.setCrawlTime(new java.util.Date());
				tn.setUrl(url);
				tn.setTitle(tilte_temp);
				tn.setTaskStatus(Const.TASKID.get("urlToMysql"));
				tn.setWebSite(ws.SiteName);
				tn.setSubtopicId(Const.SUBTOPICID[ws.getSites().get(str_url)]);
				if(!filterTitleNews(ws.getFilters().get(str_url),tn)){
					continue;
				}			
				if(mp_tn.containsKey(url)){
					int type = tn.getSubtopicId();
					type = type | mp_tn.get(url).getSubtopicId();
					tn.setSubtopicId(type);
					mp_tn.remove(url);
				}
				mp_tn.put(url, tn);
				url_len+=url.length();
				url_size++;
				title_len+=tn.getTitle().length();
				title_size++;
			}
				System.out.println(str_url+"-----"+"--"+url_size+"--"+((float)url_len/(float)url_size)+"--"+((float)title_len/(float)title_size));
				Log.getLogger().info(ws.SiteName+" "+str_url+" "+url_len+" "+url_size+" "+title_len+" "+title_size);
		}
		Collection<titleNews> st_tn_mp = mp_tn.values();
		Iterator<titleNews> it_tn_mp = st_tn_mp.iterator();
		while(it_tn_mp.hasNext()){
			titleNews re_tn = it_tn_mp.next();
			ls_result.add(re_tn);
		}
		return ls_result;
	}
	
	/**
	 * @param node_website
	 * @return
	 * @Description:read the xml to get WebSite
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WebSite getWebSiteFromElement(Element node_website){
		WebSite result = new WebSite();
		Map<String,Integer> url_types = new HashMap<String,Integer>();			
		Map<String,titleNewsFilter> url_filter = new HashMap<String,titleNewsFilter>();
		String siteName = "";
		String str_url = "";
		int  url_avg_len = 0,title_avg_len=0;
		int  type = 0,levels = 0;
		///get the siteName
		siteName = node_website.attributeValue("name");
		List urls = node_website.elements();		
		Iterator<Element> url = urls.iterator();		
		while(url.hasNext()){
			Element node_url = url.next();		
			///get the url 
			str_url = node_url.element("Addr").getText();
			///get url's type
			type = Integer.valueOf(node_url.element("Type").getText());
			//get all filters
			Element el_filter = node_url.element("Filter");
			List<Element> els_filter = el_filter.elements();
			levels = els_filter.size();
			String[] str_filter = new String[levels+1];
			for(int i=0;i<levels;i++){
				str_filter[i+1] = els_filter.get(i).getText();
			}
			Element el_url_len = node_url.element("AvgUrlLength");
			url_avg_len = Integer.valueOf(el_url_len.getText());
			Element el_title_len = node_url.element("AvgTitleLength");
			title_avg_len = Integer.valueOf(el_title_len.getText());
			titleNewsFilter tnf = new titleNewsFilter();
			tnf.setStr_filter(str_filter);
			tnf.setTitle_avg_len(title_avg_len);
			tnf.setUrl_avg_len(url_avg_len);
			url_types.put(str_url, type);
			url_filter.put(str_url, tnf);
		}
		result.setSiteName(siteName);
		result.setSites(url_types);		
		result.setFilters(url_filter);	
		return result;
	}
	
	
	
	
	/**
	 * @return List<WebSite>
	 * @Description:read WebSites.xml and get all titleNews urls + titles+filter
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<titleNews> getAllTitleNews(){

		List<titleNews> ls_results = new ArrayList<titleNews>();
		List<titleNews> ls_temp = new ArrayList<titleNews>();
		///read the WebSites.xml
		Config cfg = new Config(Const.WEB_SITES_PATH);
		///get all elements of website
		List res_nodeList = cfg.selectNodes("/WebSites/WebSite");
		Iterator<Element> it_nodes = res_nodeList.iterator();
		while(it_nodes.hasNext()){
		
			Element  node_website = it_nodes.next();
			WebSite result = new WebSite();	
			//from xml element get WebSite
			result = getWebSiteFromElement(node_website);		
			//get List<titleNews> from WebSite
			ls_temp = getAllTitleNewsFromWebSite(result);
			Iterator<titleNews> it_tn = ls_temp.iterator();
			while(it_tn.hasNext()){
				titleNews tn_temp = it_tn.next();
				ls_results.add(tn_temp);
			}	
			Log.getLogger().info("Crawl from "+result.SiteName+"("+ls_temp.size()+") titleNews");
		}
		return ls_results;
	}
	

	
	
	/**
	 * @param tn
	 * @Description:update db (titlenews)
	 */
	public void updateDB(titleNews tn){
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(tn);
		tx.commit();
		session.flush();
	}
	
	/**
	 * @return
	 * @Description:get titleNews from db.
	 * @mark: there maybe some limits set in the future
	 */
	@SuppressWarnings("unchecked")
	public List<titleNews> getExistsTitleNewsFromDB(){
		String hql = "from titleNews";
		Query query = session.createQuery(hql);
		List<titleNews> ls_tn = (List<titleNews>)query.list();
		Log.getLogger().info("Find titles in db: "+ls_tn.size());		
		return ls_tn;		
	}
	
//	/**
//	 * because the disappertime is not reliable,so remove it from the db
//	 * @2012-2-29-08.51
//	 * @param mp_tn
//	 * @param ls_url
//	 * @Description:update the disappertime of titleNews
//	 */
//	public void updateDisapperTime(Map<String,titleNews> mp_tn,List<String> ls_url){
//		int num = 0;
//		Iterator<String> it_url = ls_url.iterator();
//		while(it_url.hasNext()){
//			String url = it_url.next();
//			if(mp_tn.containsKey(url)){
//				mp_tn.remove(url);
////				mp_tn.put(url,null);
//			}
//		}
//		Collection<titleNews> con_tn = mp_tn.values();
//		Iterator<titleNews> it_tn = con_tn.iterator();
//		while(it_tn.hasNext()){
//			titleNews tn = it_tn.next();
//			if(tn!=null){
//				tn.setDisapperTime(new java.util.Date());
//				Transaction tx = session.beginTransaction();
//				session.saveOrUpdate(tn);
//				tx.commit();
//				num++;
//			}
//		}
//		Log.getLogger().info("update disapper titles: "+num);
//		session.flush();
//	}
	
	/**
	 * @param ls_tn
	 * @Description:insert new titlenews to db
	 */
	public void updateNewTitleNews(List<titleNews> ls_tn){
		Transaction tx = session.beginTransaction();
		Iterator<titleNews> it_tn = ls_tn.iterator();
		while(it_tn.hasNext()){
			titleNews tn = it_tn.next();
			session.saveOrUpdate(tn);
		}
		tx.commit();
		session.flush();
		Log.getLogger().info("update new titles: "+ls_tn.size());
	}
	
	
	/**
	 * @param ls_tn_old
	 * @param ls_tn_new
	 * @Description:update titlenews db,filter the exists and insert the news
	 * @mark: disappertime will be update too,it means the disappered time of news from the web
	 */
	public void updateTitleNewsDB(List<titleNews> ls_tn_old, List<titleNews> ls_tn_new){
		List<titleNews> ls_result = new ArrayList<titleNews>();
		Map<String,titleNews> mp_tn = new HashMap<String,titleNews>();
		List<String> ls_url_exist = new ArrayList<String>();
		Iterator<titleNews> it_tn_old = ls_tn_old.iterator();
		Iterator<titleNews> it_tn_new = ls_tn_new.iterator();
		while(it_tn_old.hasNext()){
			titleNews tn = it_tn_old.next();
			String str_url = tn.getUrl();
			mp_tn.put(str_url, tn);
		}
		while(it_tn_new.hasNext()){
			titleNews tn_new = it_tn_new.next();
			String url_new = tn_new.getUrl();
			if(mp_tn.containsKey(url_new)){
				ls_url_exist.add(url_new);
			}else{
				mp_tn.put(url_new, tn_new);
				ls_result.add(tn_new);
			}
		}
		System.out.println("updating the db!........");
		//will not update the disappertime
//		updateDisapperTime(mp_tn,ls_url_exist);
		updateNewTitleNews(ls_result);
		System.out.println("End,"+"("+ls_result.size()+") urls updated!");
		Log.getLogger().info("End,"+"("+ls_result.size()+") urls updated!");
	}
	
	
	public void runTask(){
		
		Const.loadTaskid();
		while(true){	
			Log.getLogger().info("Start Crawler for titleCrawler");
			updateTitleNewsDB(getExistsTitleNewsFromDB(),getAllTitleNews());
			System.out.println("now end of Crawler..but must run java_gc..");
			System.gc();
			System.out.println("end of java_gc..so happy~!");
			try {
				System.out.println("now end of one crawler,sleep for:"+Const.WebSiteSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end crawler,sleep for:"+Const.WebSiteSleepTime/1000/60+" minutes");
				Thread.sleep(Const.WebSiteSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in titleCrawler");
				e.printStackTrace();
			}
			
		}
	}
	
	
	
	public static void main(String[] args){
		
		
		WebSiteCrawler wsc = new WebSiteCrawler();
		wsc.runTask();
	}
	
	
	
	

	
	
	
	
	

	
	
}