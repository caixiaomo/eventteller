package web.page;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;

import db.HSession;
import db.data.Topic;
import db.data.event;

public class TimeLine {
	

	
	@SuppressWarnings("unchecked")
	public List<Topic> getTopicFromDB(){
		Session session = new HSession().createSession();
		List<Topic> results = new ArrayList<Topic>();
		String hql = "from Topic as obj where obj.number > 50 "; 
		Query query = session.createQuery(hql);
		results = (List<Topic>)query.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public event getEventById(int id){
		Session session = new HSession().createSession();
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.id="+String.valueOf(id);
		Query query = session.createQuery(hql).setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<event>)query.list();
		event result = new event();
		if(results.size()>0){
			result = results.get(0);
		}
		return result;
	}
	
	public void updateTimeline(List<db.data.TimeLine> tls){
		Session session = new HSession().createSession();
		Transaction tx = session.beginTransaction();
		for(db.data.TimeLine tl : tls){
			session.saveOrUpdate(tl);
		}		
		tx.commit();
		session.flush();	
	}
	
	public void runTask(){
		List<Topic> topics = getTopicFromDB();
		List<db.data.TimeLine> timelines = new ArrayList<db.data.TimeLine>();
		for(Topic tp : topics){
			event max_event = new event();	
			max_event.setNumber(0);
			String imgs = "";
			String events_id = tp.getEvents();
			String[] events_id_num = events_id.split(" ");
			for(String eventid : events_id_num){
				if(eventid.length() == 0 || eventid.equals("null"))
					continue;
				int et_id = Integer.valueOf(eventid);
				event event = getEventById(et_id);
				if(event.getNumber() > max_event.getNumber()){
					max_event = event;
					if(event.getImg().length() > 0){
						imgs = event.getImg();
					}
				}
//				if(event.getImg().length() > 0){
//					imgs += event.getImg() + " ";
//				}
			}
			db.data.TimeLine timeline = new db.data.TimeLine();
			timeline.setId(max_event.getTopicid());
			timeline.setStartTime(tp.getStartTime());
			timeline.setEndTime(tp.getEndTime());
			timeline.setImgs(imgs);
			timeline.setKeywords(tp.getKeywords());
			timeline.setSummary(max_event.getSummary());
			timeline.setTitle(max_event.getTitle());
			timeline.setTitleKeywords(tp.getTitle());
			timeline.setNumber(tp.getNumber());
			if(timeline.getTitle().length() > 8){
				timelines.add(timeline);
			}			
		}
		updateTimeline(timelines);
	}
	
	public static void main(String[] args){
		TimeLine sample = new TimeLine();
		sample.runTask();
	}
	
	

}
