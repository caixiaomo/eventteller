package web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import web.util.Model;

import db.HSession;
import db.data.Topic;
import db.data.article;
import db.data.event;

public class Find {
	
	private String url;
	private List<String> infos;
	public Session session;
	private Integer articleId;
	private List<event> UrlEvents;
	private List<event> REvents;
	private List<article> RArticles;
	private List<article> UrlArticles;
	private event UrlEvent;
	private article UrlArticle;
	private Topic UrlTopic;
	private article news;
	
	
	
	
	
	
	public article getNews() {
		return news;
	}
	public void setNews(article news) {
		this.news = news;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getInfos() {
		return infos;
	}
	public void setInfos(List<String> infos) {
		this.infos = infos;
	}

	public Integer getArticleId() {
		return articleId;
	}
	public void setArticleId(Integer articleId) {
		this.articleId = articleId;
	}
	
	public List<event> getUrlEvents() {
		return UrlEvents;
	}
	public void setUrlEvents(List<event> urlEvents) {
		UrlEvents = urlEvents;
	}
	public event getUrlEvent() {
		return UrlEvent;
	}
	public void setUrlEvent(event urlEvent) {
		UrlEvent = urlEvent;
	}
	public article getUrlArticle() {
		return UrlArticle;
	}
	public void setUrlArticle(article urlArticle) {
		UrlArticle = urlArticle;
	}
	public Topic getUrlTopic() {
		return UrlTopic;
	}
	public void setUrlTopic(Topic urlTopic) {
		UrlTopic = urlTopic;
	}
		
	public List<event> getREvents() {
		return REvents;
	}
	public void setREvents(List<event> rEvents) {
		REvents = rEvents;
	}
	public List<article> getRArticles() {
		return RArticles;
	}
	public void setRArticles(List<article> rArticles) {
		RArticles = rArticles;
	}
	
	
	public List<article> getUrlArticles() {
		return UrlArticles;
	}
	public void setUrlArticles(List<article> urlArticles) {
		UrlArticles = urlArticles;
	}
	
	
	public Find(){
		infos = new ArrayList<String>();
		UrlEvents = new ArrayList<event>();
		UrlEvent = new event();
		UrlArticle = new article();
		UrlTopic = new Topic();
		if(session == null){
			session = new HSession().createSession();
		}
	}
	
	public String getTitleFromUrl(String url){
		String title = "";
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			title = doc.title();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		html = doc.html();
		
		return title;
	}

	
	public String show(){
		Model md = new Model();
		if(url == null || url.equalsIgnoreCase("")){
			return "fail";
		}
		if(url.indexOf("http") < 0){
			//find keywords;
			REvents = md.getReleventEvents(url);
			RArticles = md.getReleventArticles(url);
			if(REvents == null || REvents.size() <= 0){
				event et = new event();
				et.setTitle("no events find!");
				REvents.add(et);
			}
			if(RArticles == null || RArticles.size() <= 0){
				article at = new article();
				at.setTitle("no articles find!");
				RArticles.add(at);
			}
			return "keywords";
		}
		articleId = md.getIdsFromUrl(url);
		if(articleId == null || articleId <= 0){
			if(url.indexOf(".html") >= 0 || url.indexOf(".htm") >= 0||url.indexOf(".shtml") >= 0){
				infos.add("not in DB! You can parse Online now~");
				String title = "";
				title = getTitleFromUrl(url);
				news = new article();
				news.setTitle(title);
				REvents = md.getMostReleventEvents(title);	
//				RArticles = md.getMostReleventArticles(title);
				return "onlineParse";
			}else{
				infos.add("not a news page url!");
				return "noid";
			}		
		}
		UrlArticle = md.getArticleByid(articleId);
		UrlEvent = md.getEventByid(UrlArticle.getEventid());
		UrlTopic = md.getTopicByid(UrlEvent.getTopicid());
		UrlEvents = md.getEventsByEID(UrlTopic.getId());
		UrlArticles = md.getArticlesByEID(UrlEvent.getId());
		return "success";
	}

}
