package is.web.beans.system.status;


import is.ejb.bl.business.Application;
import is.ejb.dl.dao.DAODeviceAlertsConfiguration;
import is.ejb.dl.dao.DAODeviceProfile;
import is.ejb.dl.dao.DAOServerStats;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.DeviceAlertsConfigurationEntity;
import is.ejb.dl.entities.DeviceProfileEntity;
import is.ejb.dl.entities.UserEntity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram.Bucket;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;

@ManagedBean(name="systemStatusBean")
@SessionScoped
public class SystemStatusBean implements Serializable {

	@Inject
	private Logger logger;

	@Inject
	private DAOServerStats daoStats;
	
	private CartesianChartModel chartJava = null;
	private CartesianChartModel chartGC = null;
	private CartesianChartModel chartCWMPLoad = null;
	private CartesianChartModel chartDBLoad = null;
	private CartesianChartModel chartStorageReadLoad = null;
	private CartesianChartModel chartStorageWriteLoad = null;
	
	private CartesianChartModel chartNetDownload = null;
	private CartesianChartModel chartNetUpload = null;

	private String selectedTab = "";
   @PostConstruct
   public void init() {
	   //retrieve reference of an objection from session
	   //FacesContext fc = FacesContext.getCurrentInstance();
	   chartJava = new CartesianChartModel();
	   //just to prevent errors during load of the page when no stats are available
	   LineChartSeries paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartJava.addSeries(paramStatsSeries);
	   
	   chartGC = new CartesianChartModel();	   //just to prevent errors during load of the page when no stats are available
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartGC.addSeries(paramStatsSeries);
	   
	   chartCWMPLoad = new CartesianChartModel();
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartCWMPLoad.addSeries(paramStatsSeries);
	   
	   chartDBLoad = new CartesianChartModel();
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartDBLoad.addSeries(paramStatsSeries);
	   
	   chartStorageReadLoad = new CartesianChartModel();
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartStorageReadLoad.addSeries(paramStatsSeries);

	   chartStorageWriteLoad = new CartesianChartModel();
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartStorageWriteLoad.addSeries(paramStatsSeries);

	   chartNetDownload = new CartesianChartModel();
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartNetDownload.addSeries(paramStatsSeries);

	   chartNetUpload = new CartesianChartModel();
	   paramStatsSeries = new LineChartSeries();
	   paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	   paramStatsSeries.setLabel("Loading...");
	   chartNetUpload.addSeries(paramStatsSeries);
   }

	public void refresh() {
		try {
			//selectedTab = "Server Info";
			logger.info("refresh called...");
			//regenerate charts only for the currently selected tab
			updateTab(selectedTab);
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Error refreshing list of monitoring scripts: "+e.toString());
		}
	}

	public void onTabChange(TabChangeEvent event) {
		selectedTab = event.getTab().getTitle();
        FacesMessage msg = new FacesMessage("Tab Changed", "Active Tab: " + event.getTab().getTitle());  
        logger.info("tab changed to: "+selectedTab);
        updateTab(selectedTab);
    }  

	private void updateTab(String currentlyDisplayedTabTitle) {
        if(currentlyDisplayedTabTitle.equals("") || currentlyDisplayedTabTitle.equals("Server Info")) {
        	refreshServerInfo();
        	RequestContext.getCurrentInstance().update("tabView:tabStats:idServerInfoPanel");
        }
        if(currentlyDisplayedTabTitle.equals("") || currentlyDisplayedTabTitle.equals("Java Virtual Machine Stats")) {
        	generateJavaChart("1h");
        	RequestContext.getCurrentInstance().update("tabView:tabStats:idJavaStatsChart");
        	RequestContext.getCurrentInstance().update("tabView:tabStats:idGCStatsChart");
        }
        if(currentlyDisplayedTabTitle.equals("Server Load")) {
        	generateServerLoadChart("1h");
        	RequestContext.getCurrentInstance().update("tabView:tabStats:idCWMPLoadChart");
    		RequestContext.getCurrentInstance().update("tabView:tabStats:idDBLoadChart");
    	}
        if(currentlyDisplayedTabTitle.equals("Storage Load")) {
        	generateStorageLoadChart("1h");
        	RequestContext.getCurrentInstance().update("tabView:tabStats:idStorageReadLoadChart");
    		RequestContext.getCurrentInstance().update("tabView:tabStats:idStorageWriteLoadChart");
    	}
        if(currentlyDisplayedTabTitle.equals("Network Load")) {
        	generateNetworkLoadChart("1h");
        	RequestContext.getCurrentInstance().update("tabView:tabStats:idNetworkDownloadChart");
    		RequestContext.getCurrentInstance().update("tabView:tabStats:idNetworkUploadChart");
    	}
	}
	
	String hostName = "";
	String serverCurrentTime = "";
	String serverUpTime = "";
	String osName = "";
	String osCodeName = "";
	String osVendor = "";
	String osVendorVersion = "";
	String osPatchLevel = "";
	
	public void refreshServerInfo() {
		try {
			int identifiedDataPoints = 0;
			logger.info("refresh server info called...");
			//lastest sample from last 100s
		   SearchHit[] results = daoStats.getLatestStats(Application.esServerStatsIndexName, Application.esServerStatsTypeName,1000*100);
		   logger.info("got results: "+results.length);
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));
				
				hostName = (String)result.get("hostName");
				hostName = (String)result.get("hostName");
				serverCurrentTime = (String)result.get("serverCurrentTime");
				serverUpTime = (String)result.get("serverUpTime");
				osName = (String)result.get("osName");
				osCodeName = (String)result.get("osCodeName");
				osVendor = (String)result.get("osVendor");
				osVendorVersion = (String)result.get("osVendorVersion");
				osPatchLevel = (String)result.get("osPatchLevel");
				identifiedDataPoints++;
			}
			   
			//plot only if there are elements in series
			if(identifiedDataPoints > 0) 
			{
			    FacesMessage msg = new FacesMessage("Success", "Server information sucessfully refreshed.");  
			    FacesContext.getCurrentInstance().addMessage(null, msg);
			    RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			} else {
				logger.info("Error server information");
				FacesMessage msg = new FacesMessage("Failed", "Error displaying server information");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			}
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Error refreshing java stats : "+e.toString());
		}
	}
	
	public void refreshJavaStats() {
		try {
			logger.info("refresh java stats called...");
			generateJavaChart("1h");
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Error refreshing java stats : "+e.toString());
		}
	}

	   //time intervals are: 1h 6h 12h 1d 7d 1m
	   public void generateJavaChart(String strTimeInterval) {
		   int identifiedDataPoints = 0;
		   //create new chart model
		   chartJava = new CartesianChartModel();
		   chartGC = new CartesianChartModel();
		   
		   LineChartSeries series1 = new LineChartSeries();  
		   series1.setLabel("Total memory usage percentage");
		   series1.setShowLine(true);

		   LineChartSeries series2 = new LineChartSeries();  
		   series2.setLabel("JVM memory usage ratio (used/allocated)");
		   series2.setShowLine(true);

		   LineChartSeries series3 = new LineChartSeries();  
		   series3.setLabel("CPU utilisation");
		   series3.setShowLine(true);

		   LineChartSeries series1GC = new LineChartSeries();  
		   series1GC.setLabel("Garbage collection count");
		   series1GC.setShowLine(true);

		   LineChartSeries series2GC = new LineChartSeries();  
		   series2GC.setLabel("Garbage collection time");
		   series2GC.setShowLine(true);

		   Date startDate = null;
		   Calendar cal = Calendar.getInstance();
		   int timeIntervalForDataPoints = 0; //in minutes

		   if(strTimeInterval.equals("1h")) {
			   cal.add(Calendar.HOUR, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 10;
		   } else if(strTimeInterval.equals("6h")) {
			   cal.add(Calendar.HOUR, -6);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 15;
		   } else if(strTimeInterval.equals("12h")) {
			   cal.add(Calendar.HOUR, -12);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 30;
		   } else if(strTimeInterval.equals("1d")) {
			   cal.add(Calendar.DAY_OF_MONTH, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60;
		   } else if(strTimeInterval.equals("7d")) {
			   cal.add(Calendar.DAY_OF_MONTH, -7);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60*6;
		   } else if(strTimeInterval.equals("1m")) {
			   cal.add(Calendar.MONTH, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60*12;
		   }   

		   SearchResponse responseParam1 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "memTotalUsedPercentage",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   InternalDateHistogram  agHistogram = responseParam1.getAggregations().get("by_minutes");
		   Collection agBuckets = agHistogram.getBuckets();
		   Iterator agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   series1.set(date.getMillis(), round(avgValue,2));
			   identifiedDataPoints++;
		   }

		   SearchResponse responseParam2 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "memJavaUsageRatio",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   agHistogram = responseParam2.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   //System.out.println("  key=> "+hist.getKey()+" "+hist.getDocCount()+" avg: "+avgMetrics.getValue());
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   avgValue = avgValue * (double)100;
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   series2.set(date.getMillis(), round(avgValue,2));
		   }

		   SearchResponse responseParam3 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "cpuUtilisation",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));
		   agHistogram = responseParam3.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   //System.out.println("  key=> "+hist.getKey()+" "+hist.getDocCount()+" avg: "+avgMetrics.getValue());
			   
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   series3.set(date.getMillis(), round(avgValue,2));
		   }

		   SearchResponse responseParam4 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "gcCount",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));
		   
		   agHistogram = responseParam4.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   //System.out.println("  key=> "+hist.getKey()+" "+hist.getDocCount()+" avg: "+avgMetrics.getValue());
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   series1GC.set(date.getMillis(), round(avgValue,2));
		   }

		   SearchResponse responseParam5 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "gcTime",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));
		   
		   agHistogram = responseParam5.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   //System.out.println("  key=> "+hist.getKey()+" "+hist.getDocCount()+" avg: "+avgMetrics.getValue());
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   series2GC.set(date.getMillis(), round(avgValue,2));
		   }
		   
/*		   
		   SearchHit[] results = daoStats.getStats(Application.esServerStatsIndexName, Application.esServerStatsTypeName, timeInterval);
		   logger.info("got results: "+results.length);
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));
				
				double dMemTotalUsed = (double)result.get("memTotalUsedPercentage");
				double dMemJavaUsageRatio = (double)result.get("memJavaUsageRatio") * (double)100;
				double dCpuUtilisation = (double)result.get("cpuUtilisation");
				
				int gcCount = (int)result.get("gcCount");
				int gcTime = (int)result.get("gcTime");
				
				//logger.info("java stats: "+date.toString()+" "+dMemTotalUsed+" "+dMemJavaUsageRatio+" "+dCpuUtilisation);
				series1.set(date.getTime(), dMemTotalUsed);
				series2.set(date.getTime(), dMemJavaUsageRatio);
				series3.set(date.getTime(), dCpuUtilisation);
				
				series1GC.set(date.getTime(), gcCount);
				series2GC.set(date.getTime(), gcTime);
				
				identifiedDataPoints++;
			}
		   */
			//plot only if there are elements in series
			if(identifiedDataPoints > 0) 
			{
	 		    FacesMessage msg = new FacesMessage("Success", "Displaying memory usage chart for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			} else {
				logger.info("Error displaying chart");
				series1.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series2.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series3.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series1GC.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series2GC.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				FacesMessage msg = new FacesMessage("Failed", "Error displaying memory usage chart for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			}

			chartJava.addSeries(series1);
			chartJava.addSeries(series2);
			chartJava.addSeries(series3);
			
			chartGC.addSeries(series1GC);
			chartGC.addSeries(series2GC);
	   }

	   //============================================================================================
	//requestsCWMP, requestsDB, requestsMonitoring
	public void refreshServerLoadStats() {
		try {
			logger.info("refresh server load stats called...");
			generateServerLoadChart("1h");
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Error refreshing java stats : "+e.toString());
		}
	}

	   //time intervals are: 1h 6h 12h 1d 7d 1m
	   public void generateServerLoadChart(String strTimeInterval) {
		   int identifiedDataPoints = 0;
		   //create new chart model
		   chartCWMPLoad = new CartesianChartModel();
		   
		   LineChartSeries series1 = new LineChartSeries();  
		   series1.setLabel("HTTP load r/s");
		   series1.setShowLine(true);

		   chartDBLoad = new CartesianChartModel();
		   LineChartSeries dbLoadSeries1 = new LineChartSeries();  
		   dbLoadSeries1.setLabel("Database load r/s");
		   dbLoadSeries1.setShowLine(true);
		   LineChartSeries dbLoadSeries2 = new LineChartSeries();  
		   dbLoadSeries2.setLabel("Storage load r/s");
		   dbLoadSeries2.setShowLine(true);
		   
		   Date startDate = null;
		   Calendar cal = Calendar.getInstance();
		   int timeIntervalForDataPoints = 0; //in minutes

		   if(strTimeInterval.equals("1h")) {
			   cal.add(Calendar.HOUR, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 10;
		   } else if(strTimeInterval.equals("6h")) {
			   cal.add(Calendar.HOUR, -6);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 15;
		   } else if(strTimeInterval.equals("12h")) {
			   cal.add(Calendar.HOUR, -12);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 30;
		   } else if(strTimeInterval.equals("1d")) {
			   cal.add(Calendar.DAY_OF_MONTH, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60;
		   } else if(strTimeInterval.equals("7d")) {
			   cal.add(Calendar.DAY_OF_MONTH, -7);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60*6;
		   } else if(strTimeInterval.equals("1m")) {
			   cal.add(Calendar.MONTH, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60*12;
		   }   

		   SearchResponse responseParam1 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "requestsCWMP",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   InternalDateHistogram  agHistogram = responseParam1.getAggregations().get("by_minutes");
		   Collection agBuckets = agHistogram.getBuckets();
		   Iterator agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   series1.set(date.getMillis(), round(avgValue,2));
			   identifiedDataPoints++;
		   }

		   SearchResponse responseParam2 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "requestsDB",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   agHistogram = responseParam2.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   dbLoadSeries1.set(date.getMillis(), round(avgValue,2));
		   }

		   SearchResponse responseParam3 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "requestsMonitoring",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   agHistogram = responseParam3.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   dbLoadSeries2.set(date.getMillis(), round(avgValue,2));
		   }

		   /*
		   SearchHit[] results = daoStats.getStats(Application.esServerStatsIndexName, Application.esServerStatsTypeName, timeInterval);
		   logger.info("got results: "+results.length);
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));

				double dRequestsCWMP = (double)result.get("requestsCWMP");
				series1.set(date.getTime(), dRequestsCWMP);
				
				double dRequestsDB = (double)result.get("requestsDB");
				dbLoadSeries1.set(date.getTime(), dRequestsDB);
				double dRequestsStorage = (double)result.get("requestsMonitoring");
				dbLoadSeries2.set(date.getTime(), dRequestsStorage);
	
				identifiedDataPoints++;
			}
			*/
			//plot only if there are elements in series
			if(identifiedDataPoints > 0) 
			{
	 		    FacesMessage msg = new FacesMessage("Success", "Displaying server load charts for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			} else {
				logger.info("Error displaying chart");
				series1.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				dbLoadSeries1.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				dbLoadSeries2.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				FacesMessage msg = new FacesMessage("Failed", "Error displaying server load charts for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			}

			chartCWMPLoad.addSeries(series1);
			chartDBLoad.addSeries(dbLoadSeries1);
			chartDBLoad.addSeries(dbLoadSeries2);
	   }

	//===================================================================================================
	//database (mysql) and es load
	public void refreshStorageLoadStats() {
		try {
			logger.info("refresh storage load stats called...");
			generateStorageLoadChart("1h");
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Error refreshing storage load stats : "+e.toString());
		}
	}

	   //time intervals are: 1h 6h 12h 1d 7d 1m
	   public void generateStorageLoadChart(String strTimeInterval) {
		   int identifiedDataPoints = 0;
		   //create new chart model
		   chartStorageReadLoad = new CartesianChartModel();
		   chartStorageWriteLoad = new CartesianChartModel();
		   LineChartSeries seriesRead = new LineChartSeries();  
		   seriesRead.setLabel("Read throughput MB/s");
		   seriesRead.setShowLine(true);

		   LineChartSeries seriesWrite = new LineChartSeries();  
		   seriesWrite.setLabel("Write throughput MB/s");
		   seriesWrite.setShowLine(true);

		   Date startDate = null;
		   Calendar cal = Calendar.getInstance();
		   int timeIntervalForDataPoints = 0; //in minutes

		   if(strTimeInterval.equals("1h")) {
			   cal.add(Calendar.HOUR, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 10;
		   } else if(strTimeInterval.equals("6h")) {
			   cal.add(Calendar.HOUR, -6);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 15;
		   } else if(strTimeInterval.equals("12h")) {
			   cal.add(Calendar.HOUR, -12);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 30;
		   } else if(strTimeInterval.equals("1d")) {
			   cal.add(Calendar.DAY_OF_MONTH, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60;
		   } else if(strTimeInterval.equals("7d")) {
			   cal.add(Calendar.DAY_OF_MONTH, -7);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60*6;
		   } else if(strTimeInterval.equals("1m")) {
			   cal.add(Calendar.MONTH, -1);
			   startDate = cal.getTime();
			   timeIntervalForDataPoints = 60*12;
		   }   

		   SearchResponse responseParam1 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "diskReads",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   InternalDateHistogram  agHistogram = responseParam1.getAggregations().get("by_minutes");
		   Collection agBuckets = agHistogram.getBuckets();
		   Iterator agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   seriesRead.set(date.getMillis(), round(avgValue,2));
			   identifiedDataPoints++;
		   }

		   SearchResponse responseParam2 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
				    "diskWrites",
				   	timeIntervalForDataPoints,
					startDate, new Date(System.currentTimeMillis()));

		   agHistogram = responseParam2.getAggregations().get("by_minutes");
		   agBuckets = agHistogram.getBuckets();
		   agIt = agBuckets.iterator();
		   while(agIt.hasNext()) {
			   Bucket hist = (Bucket)agIt.next();
			   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
			   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
			   DateTime date = format.parseDateTime(hist.getKey());
			   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			   String dateStr = date.toString(fmt);
			   double avgValue = avgMetrics.getValue();
			   if(Double.isNaN(avgMetrics.getValue())){
				   avgValue = 0;
			   }
			   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
			   seriesWrite.set(date.getMillis(), round(avgValue,2));
		   }

		   /*
		   SearchHit[] results = daoStats.getStats(Application.esServerStatsIndexName, Application.esServerStatsTypeName, timeInterval);
		   logger.info("got results: "+results.length);
			for (SearchHit hit : results) {
				Map<String,Object> result = hit.getSource();
				String timestamp = (String)result.get("@timestamp");
				Date date = new Date((Long) result.get("@time"));

				double dDiskReads = (double)result.get("diskReads");
				seriesRead.set(date.getTime(), dDiskReads);
				
				double dDiskWrites = (double)result.get("diskWrites");
				seriesWrite.set(date.getTime(), dDiskWrites);
				double dRequestsStorage = (double)result.get("requestsMonitoring");
	
				identifiedDataPoints++;
			}
			*/
			//plot only if there are elements in series
			if(identifiedDataPoints > 0) 
			{
	 		    FacesMessage msg = new FacesMessage("Success", "Displaying storage load charts for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			} else {
				logger.info("Error displaying chart");
				seriesRead.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				seriesWrite.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				FacesMessage msg = new FacesMessage("Failed", "Error displaying storage load charts for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
			}

			chartStorageReadLoad.addSeries(seriesRead);
			chartStorageWriteLoad.addSeries(seriesWrite);
	   }

	public void refreshNetworkLoadStats() {
		try {
			logger.info("refresh network load stats called...");
			generateNetworkLoadChart("1h");
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Error refreshing network stats : "+e.toString());
		}
	}

   //time intervals are: 1h 6h 12h 1d 7d 1m
   public void generateNetworkLoadChart(String strTimeInterval) {
	   int identifiedDataPoints = 0;
	   //create new chart model
	   chartNetDownload = new CartesianChartModel();
	   chartNetUpload = new CartesianChartModel();
	   LineChartSeries seriesDownload = new LineChartSeries();  
	   seriesDownload.setLabel("Network download KB/s");
	   seriesDownload.setShowLine(true);

	   LineChartSeries seriesUpload = new LineChartSeries();  
	   seriesUpload.setLabel("Network upload KB/s");
	   seriesUpload.setShowLine(true);

	   Date startDate = null;
	   Calendar cal = Calendar.getInstance();
	   int timeIntervalForDataPoints = 0; //in minutes

	   if(strTimeInterval.equals("1h")) {
		   cal.add(Calendar.HOUR, -1);
		   startDate = cal.getTime();
		   timeIntervalForDataPoints = 10;
	   } else if(strTimeInterval.equals("6h")) {
		   cal.add(Calendar.HOUR, -6);
		   startDate = cal.getTime();
		   timeIntervalForDataPoints = 15;
	   } else if(strTimeInterval.equals("12h")) {
		   cal.add(Calendar.HOUR, -12);
		   startDate = cal.getTime();
		   timeIntervalForDataPoints = 30;
	   } else if(strTimeInterval.equals("1d")) {
		   cal.add(Calendar.DAY_OF_MONTH, -1);
		   startDate = cal.getTime();
		   timeIntervalForDataPoints = 60;
	   } else if(strTimeInterval.equals("7d")) {
		   cal.add(Calendar.DAY_OF_MONTH, -7);
		   startDate = cal.getTime();
		   timeIntervalForDataPoints = 60*6;
	   } else if(strTimeInterval.equals("1m")) {
		   cal.add(Calendar.MONTH, -1);
		   startDate = cal.getTime();
		   timeIntervalForDataPoints = 60*12;
	   }   

	   SearchResponse responseParam1 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
			    "netDownload",
			   	timeIntervalForDataPoints,
				startDate, new Date(System.currentTimeMillis()));

	   InternalDateHistogram  agHistogram = responseParam1.getAggregations().get("by_minutes");
	   Collection agBuckets = agHistogram.getBuckets();
	   Iterator agIt = agBuckets.iterator();
	   while(agIt.hasNext()) {
		   Bucket hist = (Bucket)agIt.next();
		   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
		   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
		   DateTime date = format.parseDateTime(hist.getKey());
		   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		   String dateStr = date.toString(fmt);
		   double avgValue = avgMetrics.getValue();
		   if(Double.isNaN(avgMetrics.getValue())){
			   avgValue = 0;
		   }
		   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
		   seriesDownload.set(date.getMillis(), round(avgValue,2));
		   identifiedDataPoints++;
	   }

	   SearchResponse responseParam2 = daoStats.calculateParameterValues(Application.esServerStatsIndexName,Application.esServerStatsTypeName, 
			    "netUpload",
			   	timeIntervalForDataPoints,
				startDate, new Date(System.currentTimeMillis()));

	   agHistogram = responseParam2.getAggregations().get("by_minutes");
	   agBuckets = agHistogram.getBuckets();
	   agIt = agBuckets.iterator();
	   while(agIt.hasNext()) {
		   Bucket hist = (Bucket)agIt.next();
		   InternalAvg avgMetrics = (InternalAvg)hist.getAggregations().get("AVG");
		   DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//.withLocale(Locale.UK);	        	 
		   DateTime date = format.parseDateTime(hist.getKey());
		   DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		   String dateStr = date.toString(fmt);
		   double avgValue = avgMetrics.getValue();
		   if(Double.isNaN(avgMetrics.getValue())){
			   avgValue = 0;
		   }
		   //logger.info("-> transformed date: "+dateStr+" "+avgValue);
		   seriesUpload.set(date.getMillis(), round(avgValue,2));
	   }

	   /*
	   SearchHit[] results = daoStats.getStats(Application.esServerStatsIndexName, Application.esServerStatsTypeName, timeInterval);
	   logger.info("got results: "+results.length);
		for (SearchHit hit : results) {
			Map<String,Object> result = hit.getSource();
			String timestamp = (String)result.get("@timestamp");
			Date date = new Date((Long) result.get("@time"));

			double dNetDownload = (double)result.get("netDownload");
			seriesDownload.set(date.getTime(), dNetDownload);
			
			double dNetUpload = (double)result.get("netUpload");
			seriesUpload.set(date.getTime(), dNetUpload);

			identifiedDataPoints++;
		}
		*/
		//plot only if there are elements in series
		if(identifiedDataPoints > 0) 
		{
 		    FacesMessage msg = new FacesMessage("Success", "Displaying network load charts for period: "+strTimeInterval);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
		} else {
			logger.info("Error displaying chart");
			seriesDownload.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
			seriesUpload.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
			FacesMessage msg = new FacesMessage("Failed", "Error displaying network load charts for period: "+strTimeInterval);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			RequestContext.getCurrentInstance().update("tabView:idSystemStatusGrowl");
		}

		chartNetDownload.addSeries(seriesDownload);
		chartNetUpload.addSeries(seriesUpload);
   
   }


   private double round(double value, int places) {
	   if (places < 0) throw new IllegalArgumentException();
	   long factor = (long) Math.pow(10, places);
	   value = value * factor;
	   long tmp = Math.round(value);
	   return (double) tmp / factor;
   }

	public CartesianChartModel getChartJava() {
		return chartJava;
	}

	public void setChartJava(CartesianChartModel chartJava) {
		this.chartJava = chartJava;
	}

	public CartesianChartModel getChartGC() {
		return chartGC;
	}

	public void setChartGC(CartesianChartModel chartGC) {
		this.chartGC = chartGC;
	}

	public CartesianChartModel getChartCWMPLoad() {
		return chartCWMPLoad;
	}

	public void setChartCWMPLoad(CartesianChartModel chartCWMPLoad) {
		this.chartCWMPLoad = chartCWMPLoad;
	}

	public CartesianChartModel getChartDBLoad() {
		return chartDBLoad;
	}

	public void setChartDBLoad(CartesianChartModel chartDBLoad) {
		this.chartDBLoad = chartDBLoad;
	}

	public CartesianChartModel getChartStorageReadLoad() {
		return chartStorageReadLoad;
	}

	public void setChartStorageReadLoad(CartesianChartModel chartStorageReadLoad) {
		this.chartStorageReadLoad = chartStorageReadLoad;
	}

	public CartesianChartModel getChartStorageWriteLoad() {
		return chartStorageWriteLoad;
	}

	public void setChartStorageWriteLoad(CartesianChartModel chartStorageWriteLoad) {
		this.chartStorageWriteLoad = chartStorageWriteLoad;
	}

	public CartesianChartModel getChartNetDownload() {
		return chartNetDownload;
	}

	public void setChartNetDownload(CartesianChartModel chartNetDownload) {
		this.chartNetDownload = chartNetDownload;
	}

	public CartesianChartModel getChartNetUpload() {
		return chartNetUpload;
	}

	public void setChartNetUpload(CartesianChartModel chartNetUpload) {
		this.chartNetUpload = chartNetUpload;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getServerCurrentTime() {
		return serverCurrentTime;
	}

	public void setServerCurrentTime(String serverCurrentTime) {
		this.serverCurrentTime = serverCurrentTime;
	}

	public String getServerUpTime() {
		return serverUpTime;
	}

	public void setServerUpTime(String serverUpTime) {
		this.serverUpTime = serverUpTime;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsCodeName() {
		return osCodeName;
	}

	public void setOsCodeName(String osCodeName) {
		this.osCodeName = osCodeName;
	}

	public String getOsVendor() {
		return osVendor;
	}

	public void setOsVendor(String osVendor) {
		this.osVendor = osVendor;
	}

	public String getOsVendorVersion() {
		return osVendorVersion;
	}

	public void setOsVendorVersion(String osVendorVersion) {
		this.osVendorVersion = osVendorVersion;
	}

	public String getOsPatchLevel() {
		return osPatchLevel;
	}

	public void setOsPatchLevel(String osPatchLevel) {
		this.osPatchLevel = osPatchLevel;
	}
	
}

