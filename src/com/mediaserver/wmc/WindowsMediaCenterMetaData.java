package com.mediaserver.wmc;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.cfs.xml.CommaSpaceListAdapter;
import com.cfs.xml.SemiColonSpaceListAdapter;
import com.cfs.xml.YYYY_MM_DD_WhitespaceSeparatedDateAdapter;

@XmlRootElement(name="METADATA")
public class WindowsMediaCenterMetaData {
	@XmlElement(name="MDR-DVD")
	private RippedDVD dvdInfo;
	private String dvdId;
	private String needsAttribution;
	private String cover;
	private String imdbCode;
	
	public static class Title {
		@XmlElement(name="titleNum")
		private Integer index;
		@XmlElement(name="titleTitle")
		private String title;
		@XmlElement(name="studio")
		private String studio;
		@XmlElement(name="director")
		private String director;
		@XmlElement(name="leadPerformer")
		private String leadPerformer;
		@XmlElement(name="MPAARating")
		private String mpaaRating;
		@XmlElement(name="genre")
		private String genre;
		@XmlElement(name="synopsis")
		private String synopsis;
		@XmlElement(name="chapter")
		List<Chapter> chapter;
	}
	
	public static class Chapter {
		@XmlElement(name="chapterNum")
		private Integer index;
		@XmlElement(name="chapterTitle")
		private String chapterTitle;
		
	}
	
	public static class RippedDVD {
		@XmlElement(name="MetadataExpires")
		private Date metadataExpires;
		@XmlElement(name="largeCoverParams")
		private String largeCoverParameters;
		@XmlElement(name="smallCoverParams")
		private String smallCoverParameters;
		@XmlElement(name="dvdTitle")
		private String title;
		@XmlJavaTypeAdapter(value=SemiColonSpaceListAdapter.class)
		@XmlElement(name="leadPerformer")
		private List<String> leadPerformer;
		@XmlJavaTypeAdapter(value=SemiColonSpaceListAdapter.class)
		@XmlElement(name="director")
		private List<String> director;
		@XmlElement(name="MPAARating")
		private String mpaaRating;
		@XmlJavaTypeAdapter(value=CommaSpaceListAdapter.class)
		@XmlElement(name="genre")
		private List<String> genre;
		@XmlElement(name="studio")
		private String studio;
		@XmlJavaTypeAdapter(value=YYYY_MM_DD_WhitespaceSeparatedDateAdapter.class)
		@XmlElement(name="releaseDate")
		private Date releaseDate;
		@XmlElement(name="language")
		private String language;
		@XmlElement(name="duration")
		private Integer duration;
		@XmlElement(name="dataProvider")
		private String dataProvider;
		@XmlElement(name="version")
		private BigDecimal version;
		@XmlElement(name="rating")
		private String rating;
		@XmlElement(name="title")
		private List<Title> titles;

		public File getFirstAvailableCover() {
			File file = null;
			if (largeCoverParameters != null && largeCoverParameters.length() > 0) {
				file = new File(WindowsMediaCenterManager.MEDIA_CENTER_COVER_CACHE + largeCoverParameters.replaceAll("/", "-"));
				if (file.exists() && file.isFile())
					return file;
			}

			if (smallCoverParameters != null && smallCoverParameters.length() > 0) {
				file = new File(WindowsMediaCenterManager.MEDIA_CENTER_COVER_CACHE + smallCoverParameters.replaceAll("/", "-"));
				if (file.exists() && file.isFile())
					return file;
			}

			return null;
		}
	}
	
	public WindowsMediaCenterMetaData() {
		this.dvdInfo = new RippedDVD();
	}
	
	@XmlTransient
	public File getThumbnailFile() {
		return dvdInfo.getFirstAvailableCover();
	}
	
	@XmlTransient
	public String getTitle() {
		return dvdInfo != null?dvdInfo.title:null;
	}
	public void setTitle(String title) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.title = title;
	}
	
	@XmlTransient
	public List<String> getGenre() {
		return dvdInfo != null?dvdInfo.genre:null;
	}
	public void setGenre(List<String> genre) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.genre = genre;
	}
	
	@XmlTransient
	public String getStudio() {
		return dvdInfo != null?dvdInfo.studio:null;
	}
	public void setStudio(String studio) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.studio = studio;
	}
	
	@XmlTransient
	public List<String> getDirector() {
		return dvdInfo != null?dvdInfo.director:null;
	}
	public void setDirector(List<String> director) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.director = director;
	}
	
	@XmlTransient
	public String getMPAARating() {
		return dvdInfo != null?dvdInfo.mpaaRating:null;
	}
	public void setMPAARating(String mpaaRating) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.mpaaRating = mpaaRating;
	}
	
	@XmlTransient
	public List<String> getLeadPerformer() {
		return dvdInfo != null?dvdInfo.leadPerformer:null;
	}
	public void setLeadPerformer(List<String> leadPerformer) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.leadPerformer = leadPerformer;
	}
	
	@XmlTransient
	public Date getReleaseDate() {
		return dvdInfo != null?dvdInfo.releaseDate:null;
	}
	public void setReleaseDate(Date releaseDate) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.releaseDate = releaseDate;
	}
	
	@XmlTransient
	public Date getMetadataExpires() {
		return dvdInfo != null?dvdInfo.metadataExpires:null;
	}
	public void setMetadataExpires(Date metadataExpires) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.metadataExpires = metadataExpires;
	}
	
	@XmlTransient
	public String getLanguage() {
		return dvdInfo != null?dvdInfo.language:null;
	}
	public void setLanguage(String language) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.language = language;
	}

	@XmlTransient
	public Integer getDuration() {
		return dvdInfo != null?dvdInfo.duration:null;
	}
	public void setDuration(Integer duration) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.duration = duration;
	}

	@XmlTransient
	public String getDataProvider() {
		return dvdInfo != null?dvdInfo.dataProvider:null;
	}
	public void setDataProvider(String dataProvider) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.dataProvider = dataProvider;
	}

	@XmlTransient
	public BigDecimal getVersion() {
		return dvdInfo != null?dvdInfo.version:null;
	}
	public void setVersion(BigDecimal version) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.version = version;
	}

	@XmlTransient
	public String getRating() {
		return dvdInfo != null?dvdInfo.rating:null;
	}
	public void setRating(String rating) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.rating = rating;
	}

	@XmlTransient
	public String getLargeCoverParameters() {
		return dvdInfo != null?dvdInfo.largeCoverParameters:null;
	}
	public void setLargeCoverParameters(String largeCoverParameters) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.largeCoverParameters = largeCoverParameters;
	}

	@XmlTransient
	public String getSmallCoverParameters() {
		return dvdInfo != null?dvdInfo.smallCoverParameters:null;
	}
	public void setSmallCoverParameters(String smallCoverParameters) {
		if (dvdInfo == null)
			dvdInfo = new RippedDVD();
		
		this.dvdInfo.smallCoverParameters = smallCoverParameters;
	}

	@XmlTransient
	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	@XmlElement(name="DvdId")
	public String getDvdId() {
		return dvdId;
	}
	public void setDvdId(String dvdId) {
		this.dvdId = dvdId;
	}
	
	@XmlElement(name="NeedsAttribution")
	public String getNeedsAttribution() {
		return needsAttribution;
	}
	public void setNeedsAttribution(String needsAttribution) {
		this.needsAttribution = needsAttribution;
	}

	@XmlElement(name="IMDBCode")
	public String getImdbCode() {
		return imdbCode;
	}

	public void setImdbCode(String imdbCode) {
		this.imdbCode = imdbCode;
	}
}
