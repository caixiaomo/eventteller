package news.core;

//import java.awt.AlphaComposite;
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.Graphics2D;
//import java.awt.Image;
import java.awt.image.BufferedImage;
//import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.OutputStream;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
//import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.hibernate.Query;
import org.hibernate.Session;

import util.Const;
import util.Log;
import util.Util;
import db.HSession;
//import db.data.Topic;
import db.data.article;
import db.data.event;


class TImage implements Comparable<TImage>{
	public String MD5;
	public byte[] bys;
	public int hight;
	public int width;
	@Override
	public int compareTo(TImage arg0) {
		if(arg0.bys.length > this.bys.length)
			return 1;
		else if(arg0.bys.length == this.bys.length)
			return 0;
		else
			return -1;
	}
}



public class EventImgsGenerator {
	public Session session = null;
	
	public EventImgsGenerator(){
		if(null == session){
			session = new HSession().createSession();
		}
		Const.loadTaskid();
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getMaxDayEventFromDB(int days){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.id = 1144891";
//		String hql = "from event as obj where obj.title = null and obj.day !="+String.valueOf(days);
		Query query = session.createQuery(hql);
		results = (List<event>)query.list();
		return results;
	}

	
	public int getMaxDayFromEventDB(){
		int results  = 0 ;
		String hql = "select max(obj.day) from event as obj";
		results = (Integer) session.createQuery(hql).uniqueResult();		
		return results;
	}
	
	public void saveImg(TImage ti,String path){
		File file = new File(path);
		if(!file.exists()){
			file.mkdir();
		}
		File sfile = new File(path + ti.MD5 + ".jpg");
		try {
			if(!sfile.exists()){
				 FileOutputStream fos = new FileOutputStream(sfile);  
		         fos.write(ti.bys);  
	   	         fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public TImage getImgValue(String url_tmp,int state){
			
		TImage result = new TImage();
		try {
			BufferedImage image;
			if(state == 1){
				image = ImageIO.read(new URL(url_tmp));
			}else{
				image = ImageIO.read(new File(url_tmp));
			}
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    boolean flag = ImageIO.write(image, "jpg", out);
		    if(flag){
		     	byte[] b = out.toByteArray();
		       	String md5 = Util.MD5OfByte(b);
		       	result.MD5 = md5;
		       	result.bys = b;
		       	result.width = image.getWidth();
		       	result.hight = image.getHeight(); 
	        }
		    System.out.println(image.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<article> getArticleFromEvent(event en){
		List<article> results = new ArrayList<article>();
		int en_id = 0;
		en_id = en.getId();
		String hql = "from article as obj where obj.eventid ="+String.valueOf(en_id);
		results = (List<article>)session.createQuery(hql).list();
		return results;
	}
	
	
	
	public event setEventImgs(event et){
		List<article> ats = getArticleFromEvent(et);
		List<TImage> imgs = new ArrayList<TImage>();
		Map<String,TImage> mp_md5 = new HashMap<String,TImage>();
		Set<String> hs_urls = new HashSet<String>();
		for(article at : ats){
			String str_imgs = at.getImgs();
			if(str_imgs.length() <= 0){
				continue;
			}else if(str_imgs.indexOf(",") <= 0 && !hs_urls.contains(str_imgs)){
				hs_urls.add(str_imgs);
			}else{
				String[] its = str_imgs.split(",");
				for(String it : its){
					if(it.length() > 0 && !hs_urls.contains(it)){
						hs_urls.add(it);
					}
				}
			}
		}
		
		System.out.println(hs_urls.size());
		int check_num = 0;
		Iterator<String> imgs_urls = hs_urls.iterator();
		while(imgs_urls.hasNext()){
			check_num++;
			String url = imgs_urls.next();
			System.out.println(check_num + " " + url);
			TImage ti = getImgValue(url,1);
			if(ti.bys != null && ti.bys.length > 10240){
				if(!mp_md5.containsKey(ti.MD5)){
					mp_md5.put(ti.MD5,ti);
					imgs.add(ti);
				}
			}
		}
		Collections.sort(imgs);
		
		for(TImage tim : imgs){
			saveImg(tim,"d:\\test\\" + et.getId() +"\\");
		}		
		return et;
	}
	
	
	
	
	
	public void runTask(){
		while(true){
			List<event> results_en = new ArrayList<event>();
			int maxDay = 0;
			int num_update = 0;
			List<event> events = new ArrayList<event>();
			maxDay = getMaxDayFromEventDB();
			events = getMaxDayEventFromDB(maxDay);
			for(event en : events){
				num_update++;
				event en_new = new event();
				en_new = setEventImgs(en);
				results_en.add(en_new);
				if(num_update % 10 == 0){
					System.out.println("update the events!...");
//					updateEventDBList(results_en);
					results_en.clear();
				}
			}
//			updateEventDBList(results_en);
			try {
				System.out.println("now end of EventInfoGenerator,sleep for:"+Const.EventInfoGeneratorSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end EventInfoGenerator,sleep for:"+Const.EventInfoGeneratorSleepTime/1000/60+" minutes");
				Thread.sleep(Const.EventInfoGeneratorSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in EventInfoGenerator!");
				e.printStackTrace();
			}						
		}	
	}
	
	public static void main(String[] args){
//			EventImgsGenerator eig = new EventImgsGenerator();
//			eig.saveImg("http://imgworld.gmw.cn/attachement/jpg/site2/20121021/bc305bc987e911ed76ef25.jpg","dd");
//			eig.runTask();
//			eig.getImgValue("http://imgworld.gmw.cn/attachement/jpg/site2/20121021/bc305bc987e911ed76ef25.jpg", 1);
			
			
	}
	

}
