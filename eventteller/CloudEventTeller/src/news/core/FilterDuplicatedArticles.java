package news.core;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import util.Const;
import db.HSession;
import db.data.article;

public class FilterDuplicatedArticles {
	
	/**
	 * @param id
	 * @return
	 * @Description:from id to get article
	 */
	@SuppressWarnings("unchecked")
	public article getArticleById(int id){
		List<article> results = new ArrayList<article>();
		String hql = "from article as obj where obj.id="+String.valueOf(id);
		Session session = new HSession().createSession();
		Query query = session.createQuery(hql).setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<article>)query.list();
		article result = new article();
		if(results.size()>0){
			result = results.get(0);
		}
		session.close();
		return result;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException{
		FilterDuplicatedArticles fda = new FilterDuplicatedArticles();
		article a1 = fda.getArticleById(4815851);
//		double not_avg = 0;
//		int not_count = 1;
//		double is_avg= 0;
//		int is_count = 1;
		String hasha = util.SimHash.hash_2(a1.getMainwords());
//		for(int i = 4805110; i < 4809469; i++){
//			article a2 = fda.getArticleById(i);
//			if(a2.getMainwords() == null)
//				continue;
//			String hashb = util.SimHash.hash_2(a2.getMainwords());			
//			
//			int diff = 0;
//
//			for (int k = 0; k < hasha.length(); k++) {
//				if (hasha.charAt(k) != hashb.charAt(k)) {
//					diff++;
//				}
//			}
//			if(a1.getEventid() - a2.getEventid() == 0){
//				System.out.println(a2.getId() + "\t" + a2.getTitle() + "\t" + diff);
//				is_count++;
//				is_avg += diff;
//			}else{
//				not_count++;
//				not_avg += diff;
//				if(diff < 40){
//					System.out.println("----------" + a2.getId() + "\t" + a2.getTitle() + "\t" + diff);
//				}
//			}
////			System.out.println(a2.getId() + "\t" +a1.getEventid() + "\t" + a2.getEventid() +"\t"+ diff);
//		}
//		
//		System.out.print(not_avg/not_count + "\t" + is_avg/is_count);
		
		
		article a2 = fda.getArticleById(4812321);
		String hashb = util.SimHash.hash_2(a2.getMainwords());	
		int diff = 0;
		
					for (int k = 0; k < hasha.length(); k++) {
						if (hasha.charAt(k) != hashb.charAt(k)) {
							diff++;
						}
					}
		System.out.println(diff);
		
	}
	

}
