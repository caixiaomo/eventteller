package news.core;

import org.hibernate.Session;
import org.hibernate.Transaction;

import db.HSession;
import db.data.Topic;

public class TopicClean {
	
	public Session session;
	
	public TopicClean(){
		if(null == session){
			session = new HSession().createSession();
		}
	}
	
	public Topic getTopicById(int id){
		Topic tp = new Topic();
		String hql ="from Topic as obj where obj.id =" + id;
		tp = (Topic)session.createQuery(hql).uniqueResult();
		return tp;
	}
	
	public void updateTopic(Topic tp){
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(tp);
		tx.commit();
	}
	
	public Topic changeTopic(Topic tp,int id){
		String events = tp.getEvents();
		int number = tp.getNumber();
		String[] events_str = events.split(" ");
		String results = "";
		for(String event : events_str){
			if(!event.equalsIgnoreCase(String.valueOf(id))){
				results =results + event + " ";
			}
		}
		number --;
		tp.setNumber(number);
		tp.setEvents(results);
		return tp;
	}
	
	
	
	public void runTask(){
		Topic tp = new Topic();
		int id = 67212;
//		int cid = 0;
		String cids = "269395";
		tp = getTopicById(id);
		String[] cids_str = cids.split(" ");
		for(String cid : cids_str){
			int cid_temp = Integer.valueOf(cid);
			System.out.println(cid_temp);
			tp = changeTopic(tp,cid_temp);
		}
		System.out.println(tp.getEvents());
		updateTopic(tp);
	}
	
	public static void main(String[] args){
		TopicClean tc = new TopicClean();
		tc.runTask();
	}
	

}
