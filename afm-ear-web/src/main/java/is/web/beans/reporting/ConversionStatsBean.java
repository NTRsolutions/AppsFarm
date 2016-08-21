package is.web.beans.reporting;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.SecurityManager;
import is.ejb.dl.dao.DAOLicense;
import is.ejb.dl.dao.DAONetworkStatsHourly;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.LicenseEntity;
import is.ejb.dl.entities.NetworkStatsHourlyEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.license.License;
import is.web.beans.license.LicenseManager;
import is.web.beans.tab.SentinelTabBean;
import is.web.beans.tab.SentinelTabs;
import is.web.beans.tab.SentinelTabBean.SingleTabBean;
import is.web.beans.users.LoginBean;
import is.web.geo.GeoLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram.Bucket;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;

@ManagedBean(name="conversionStatsBean")
@SessionScoped
public class ConversionStatsBean implements Serializable  {
	
	@Inject
	Logger logger;
 
	@Inject
	private DAORealm daoRealm; 

	@Inject
	private DAONetworkStatsHourly daoNetworkStatsHourly; 

	private RealmEntity realm = null;
	private LoginBean loginBean = null;

	private CartesianChartModel chartProfit = null;
	private CartesianChartModel chartConversions = null;

	private String revenueStatsTitle = "";
	public ConversionStatsBean() {
	}

	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
		realm = loginBean.getUser().getRealm();
		
		chartProfit = new CartesianChartModel();
  	    //just to prevent errors during load of the page when no stats are available
	    LineChartSeries paramStatsSeries = new LineChartSeries();
	    paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	    paramStatsSeries.setLabel("Loading...");
	    chartProfit.addSeries(paramStatsSeries);

		chartConversions = new CartesianChartModel();
  	    //just to prevent errors during load of the page when no stats are available
	    paramStatsSeries = new LineChartSeries();
	    paramStatsSeries.set(new Timestamp(System.currentTimeMillis()), 0);
	    paramStatsSeries.setLabel("Loading...");
	    chartConversions.addSeries(paramStatsSeries);
	    
 	   refreshJavaStats();
 	   RequestContext.getCurrentInstance().update("tabView:idProfitStatsChart");
 	   RequestContext.getCurrentInstance().update("tabView:idConversionStatsChart");
	}

	public void refresh() {
       try {
    	   //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Refreshed API information"));
    	   //RequestContext.getCurrentInstance().update("tabView:idApiGrowl");
    	   refreshJavaStats();
    	   RequestContext.getCurrentInstance().update("tabView:idProfitStatsChart");
    	   RequestContext.getCurrentInstance().update("tabView:idConversionStatsChart");
       } catch (Exception e) {
    	   e.printStackTrace();
    	   logger.severe(e.toString());
    	   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Unable to retrieve stats data: "+e.toString()));
    	   RequestContext.getCurrentInstance().update("tabView:idConversionStatsGrowl");
       }
	}

	public void refreshJavaStats() {
		try {
			logger.info("refresh java stats called...");
			//generateJavaChart("1h");
			generateJavaChart("1d");
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error refreshing java stats : "+e.toString());
		}
	}

	//time intervals are: 1h 6h 12h 1d 7d 1m
	public void generateJavaChart(String strTimeInterval) {
		   int identifiedDataPoints = 0;
		   //create new chart model
		   chartProfit = new CartesianChartModel();
		   
		   //create new chart model
		   chartConversions = new CartesianChartModel();

		   LineChartSeries series1 = new LineChartSeries();  
		   series1.setLabel("Revenue");
		   series1.setShowLine(true);

		   LineChartSeries series2 = new LineChartSeries();  
		   series2.setLabel("Profit");
		   series2.setShowLine(true);

		   LineChartSeries series3 = new LineChartSeries();  
		   series3.setLabel("Rewards");
		   series3.setShowLine(true);

		   LineChartSeries series21 = new LineChartSeries();  
		   series21.setLabel("Clicks");
		   series21.setShowLine(true);

		   LineChartSeries series22 = new LineChartSeries();  
		   series22.setLabel("Conversions");
		   series22.setShowLine(true);

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

		   try {
			   Timestamp startSearchDate = new Timestamp(startDate.getTime());
			   Timestamp endSearchDate = new Timestamp(System.currentTimeMillis());
			   logger.info("requesting stats from period: "+startSearchDate.toString()+" "+endSearchDate.toString());
			   List<NetworkStatsHourlyEntity> samples = daoNetworkStatsHourly.findAllByRealmIdAndTimeRange(loginBean.getUser().getRealm().getId(), 
						   startSearchDate, endSearchDate);
				Iterator i = samples.iterator();
				while(i.hasNext()) {
					identifiedDataPoints++;
					NetworkStatsHourlyEntity sample = (NetworkStatsHourlyEntity)i.next();
					revenueStatsTitle = "Revenue (in "+sample.getPayoutIsoCurrencyCode()+")";

					series1.set(sample.getGenerationEndDate().getTime(), sample.getPayout());
					series2.set(sample.getGenerationEndDate().getTime(), sample.getProfit());
					series3.set(sample.getGenerationEndDate().getTime(), sample.getReward());
					
					series21.set(sample.getGenerationEndDate().getTime(), sample.getClicks());
					series22.set(sample.getGenerationEndDate().getTime(), sample.getConversions());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    //plot only if there are elements in series
			if(identifiedDataPoints > 0) 
			{
	 		    FacesMessage msg = new FacesMessage("Success", "Displaying system status for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idConversionStatsGrowl");
			} else {
				series1.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series2.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series3.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series21.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				series22.set(new Timestamp(System.currentTimeMillis()).getMinutes(), 0);
				FacesMessage msg = new FacesMessage("Failed", "Error displaying system status for period: "+strTimeInterval);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("tabView:idConversionStatsGrowl");
			}

			chartProfit.addSeries(series1);
			chartProfit.addSeries(series2);
			chartProfit.addSeries(series3);
			chartConversions.addSeries(series21);
			chartConversions.addSeries(series22);
	   }


	public CartesianChartModel getChartProfit() {
		return chartProfit;
	}

	public void setChartProfit(CartesianChartModel chartProfit) {
		this.chartProfit = chartProfit;
	}

	public CartesianChartModel getChartConversions() {
		return chartConversions;
	}

	public void setChartConversions(CartesianChartModel chartConversions) {
		this.chartConversions = chartConversions;
	}

	public String getRevenueStatsTitle() {
		return revenueStatsTitle;
	}

	public void setRevenueStatsTitle(String revenueStatsTitle) {
		this.revenueStatsTitle = revenueStatsTitle;
	}
	   
}
                    