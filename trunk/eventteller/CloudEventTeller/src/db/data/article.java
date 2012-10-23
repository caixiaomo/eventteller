package db.data;

import java.util.Date;

public class article {
	private Integer id;
	private String  title;
	private String  publishtime;
	private String  mainparagraph;
	private String  mainwords;
	private String  titlewords;
	private String  summary;
	private Date    extracttime;
	private Integer  eventid;
	private Integer  taskstatus;
	private Date   crawltime;
	private String img;
	private String imgs;
	private Integer  subtopicid;
	
	
	
	public Integer getSubtopicid() {
		return subtopicid;
	}
	public void setSubtopicid(Integer subtopicid) {
		this.subtopicid = subtopicid;
	}
	public Date getCrawltime() {
		return crawltime;
	}
	public void setCrawltime(Date crawltime) {
		this.crawltime = crawltime;
	}
	public Integer getTaskstatus() {
		return taskstatus;
	}
	public void setTaskstatus(Integer taskstatus) {
		this.taskstatus = taskstatus;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPublishtime() {
		return publishtime;
	}
	public void setPublishtime(String publishtime) {
		this.publishtime = publishtime;
	}
	public String getMainparagraph() {
		return mainparagraph;
	}
	public void setMainparagraph(String mainparagraph) {
		this.mainparagraph = mainparagraph;
	}
	public String getMainwords() {
		return mainwords;
	}
	public void setMainwords(String mainwords) {
		this.mainwords = mainwords;
	}
	public String getTitlewords() {
		return titlewords;
	}
	public void setTitlewords(String titlewords) {
		this.titlewords = titlewords;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public Date getExtracttime() {
		return extracttime;
	}
	public void setExtracttime(Date extracttime) {
		this.extracttime = extracttime;
	}
	public Integer getEventid() {
		return eventid;
	}
	public void setEventid(Integer eventid) {
		this.eventid = eventid;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	

	
	
	
}
