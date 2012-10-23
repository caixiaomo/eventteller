package news.core;

import java.util.ArrayList;
import java.util.List;


import org.hibernate.Query;

import db.data.event;

public class EventInfoGeneratorOldDay extends EventInfoGenerator{
	

	@SuppressWarnings("unchecked")
	public List<event> getOldDayEventFromDB(int days){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.day ="+String.valueOf(days-1);
		Query query = session.createQuery(hql);
		results = (List<event>)query.list();
		return results;
	}
	
	public void runTask(){

			List<event> results_en = new ArrayList<event>();
			int maxDay = 0;
			List<event> events = new ArrayList<event>();
			maxDay = getMaxDayFromEventDB();
			events = getOldDayEventFromDB(maxDay);
			for(event en : events){
				event en_new = new event();
				en_new = setEventContent(en);
				results_en.add(en_new);
			}
			System.out.println("updating the EventDB....."+results_en.size());
			updateEventDBList(results_en);					
	}
	
	public static void main(String[] args){
		EventInfoGeneratorOldDay eigod = new EventInfoGeneratorOldDay();
		eigod.runTask();
	}

}
