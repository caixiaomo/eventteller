package news.model;



import java.util.Map;


import news.filter.titleNewsFilter;

public class WebSite {
	
	public String SiteName ;
	public Map<String,Integer> Sites;
	public Map<String,titleNewsFilter> filters;



	public Map<String, titleNewsFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, titleNewsFilter> filters) {
		this.filters = filters;
	}

	public void setSiteName(String siteName) {
		SiteName = siteName;
	}

	public void setSites(Map<String,Integer> sites) {
		Sites = sites;
	}
	public String getSiteName() {
		return SiteName;
	}

	public Map<String,Integer> getSites() {
		return Sites;
	}
	
	

}
