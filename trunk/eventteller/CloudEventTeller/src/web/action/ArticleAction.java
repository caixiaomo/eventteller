package web.action;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import web.util.Model;

import com.opensymphony.xwork2.ActionSupport;

import db.data.article;
import db.data.event;
import db.HSession;

@SuppressWarnings("serial")
public class ArticleAction extends ActionSupport{
	
	private event event;
	private List<article> articles;
	private int eid;
	private Session session;
	
	public ArticleAction(){
		event = new event();
		articles = new ArrayList<article>();
		if(null == session){
			session = new HSession().createSession();
		}
	}

	
	public int getEid() {
		return eid;
	}

	public void setEid(int eid) {
		this.eid = eid;
	}

	public event getEvent() {
		return event;
	}

	public void setEvent(event event) {
		this.event = event;
	}

	public List<article> getArticles() {
		return articles;
	}

	public void setArticles(List<article> articles) {
		this.articles = articles;
	}
	
	
	
	public String  show(){
		Model md = new Model();
		event = md.getEventByid(eid);
		articles = md.getArticlesByEID(eid);		
		return "success";
	}
	

}
