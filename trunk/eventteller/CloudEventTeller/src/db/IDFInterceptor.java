package db;

import org.hibernate.EmptyInterceptor;

/**
* @PackageName:db
* @ClassName: IDFInterceptor
* @author: mblank
* @date: 2012-3-30 上午9:53:36
* @Description: just for idf tables
* @Marks: 
*/
public class IDFInterceptor extends EmptyInterceptor{
	
	
	private static final long serialVersionUID = 1L;
	private int strNum ;
	
	public IDFInterceptor(int num){
		this.strNum = num;
	}
	
	public String onPrepareStatement(String sql){

		String table = "idf_" + String.valueOf(strNum); 
		if(!sql.contains(table)){
			sql = sql.replace("idf", table);
		}
		return sql;
	}

}
