package db.data;

import java.util.Date;
public class titleNews {
	
	private Integer id;
	private String  webSite;
	private String  title;
	private String  url;
	private Date    crawlTime;
	private Integer subtopicId;
	private Integer taskStatus;
	private Integer EventId;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getWebSite() {
		return webSite;
	}
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getCrawlTime() {
		return crawlTime;
	}
	public void setCrawlTime(Date crawlTime) {
		this.crawlTime = crawlTime;
	}
	public Integer getSubtopicId() {
		return subtopicId;
	}
	public void setSubtopicId(Integer subtopicId) {
		this.subtopicId = subtopicId;
	}
	public Integer getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(Integer taskStatus) {
		this.taskStatus = taskStatus;
	}
	public Integer getEventId() {
		return EventId;
	}
	public void setEventId(Integer eventId) {
		EventId = eventId;
	}
	
	
	

}
