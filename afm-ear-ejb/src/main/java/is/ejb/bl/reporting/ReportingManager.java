package is.ejb.bl.reporting;

import static org.elasticsearch.node.NodeBuilder.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import is.ejb.bl.business.Application;
import is.ejb.bl.business.BlockedOfferCommand;
import is.ejb.bl.business.BlockedOfferType;
import is.ejb.bl.business.UserEventCategory;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.notificationSystems.gcm.test.TestGoogleNotificationSender;
import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerProviders.snapdeal.SnapdealReportType;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.rewardSystems.radius.SpinnerRewardsReport;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.system.logging.ESIndexName;
import is.ejb.bl.system.logging.ESLoggerWorkerThread;
import is.ejb.bl.system.logging.ESTypeName;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOInvitation;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.InvitationEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;
import is.ejb.dl.entities.UserEventEntity;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.jboss.marshalling.TraceInformation.IndexType;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Days;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.criterion.Order;
import org.hibernate.mapping.Array;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ReportingManager {
	
	public static final String DEFAULT_CLUSTER_NAME = "airrewardz";

	private SpinnerManager spinnerManager;
	private DAOUserEvent daoUserEvent;
	private DAOInvitation daoInvitation;

	protected static final Logger logger = Logger.getLogger(ReportingManager.class.getName());

	private Client client = null;
	private String hostName = "127.0.0.1";
	private String clusterName = "airrewardz";

	private ArrayList<BlockedOffer> listBlockedOffers = null;

	public ReportingManager() {

	}

	public ReportingManager(String hostName, String clusterName, SpinnerManager spinnerManager,
			DAOUserEvent daoUserEvent, DAOInvitation daoInvitation) {
		this.hostName = hostName;
		this.clusterName = clusterName;
		this.spinnerManager = spinnerManager;
		this.daoUserEvent = daoUserEvent;
		this.daoInvitation = daoInvitation;
		client = getClient(hostName, clusterName);
	}

	public ReportingManager(String hostName, String clusterName) {
		this.hostName = hostName;
		this.clusterName = clusterName;
		client = getClient(hostName, clusterName);
	}

	private Client getClient(String hostName, String clusterName) {
		try {
			// try {
			// client = Application.getElasticSearchLogger().getClient();
			// logger.info("*** ReportingManager ES client initialised from
			// ESLogger with reference: "+client.toString()+" host: "+hostName+"
			// cluster name: "+clusterName);
			// } catch(Exception exc) {
			// exc.printStackTrace();
			// client = null;
			// }
			if (client == null) {
				// once we find one node in the cluster ask about the others
				Builder settingsBuilder = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true);
				settingsBuilder.put("cluster.name", clusterName);
				settingsBuilder.put("client.transport.ping_timeout", "10s");
				settingsBuilder.put("http.enabled", "false");
				settingsBuilder.put("transport.tcp.port", "9300-9400");
				settingsBuilder.put("discovery.zen.ping.multicast.enabled", "true");
				settingsBuilder.put("discovery.zen.ping.unicast.hosts", hostName);
				Settings settings = settingsBuilder.build();

				client = new TransportClient(settings)
						.addTransportAddress(new InetSocketTransportAddress(hostName, 9300));

				logger.info("*** ReportingManager ES client initialised via new client creation with reference: "
						+ client.toString() + " host: " + hostName + " cluster name: " + clusterName);
				// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
				// -1,
				// LogStatus.OK,
				// Application.REPORTING_ACTIVITY+
				// " ReportingManager ES client initialised with reference:
				// "+client.toString()+" host: "+hostName+" cluster name:
				// "+clusterName);
			} else {
				logger.info("*** ReportingManager ES client already initialised, reusing existing reference: "
						+ client.toString() + " host: " + hostName + " cluster name: " + clusterName + " "
						+ client.toString());
				// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
				// -1,
				// LogStatus.OK,
				// Application.REPORTING_ACTIVITY+
				// " ReportingManager ES client already initialised, reusing
				// existing reference: "+client.toString()+" host: "+hostName+"
				// cluster name: "+clusterName +" "+client.toString());
			}

			logger.info("successfully established connection to es node running on host: " + hostName
					+ " cluster name: " + clusterName);

			return client;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			// Application.getElasticSearchLogger().indexLog(Application.CRF_TRIGGER_ACTIVITY,
			// -1,
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());
			return null;
		}
	}

	public ReportDH getReportData(RealmEntity realm, ReportPeriodName reportPeriodName, String reportPeriodNameString,
			Date dateStart, Date dateEnd, String rewardType, String networkName) {
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+
		// " returning report stats");

		ReportDH reportDH = new ReportDH();

		double conversionRate = -1;
		long totalClicks = getClicksCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalConversions = getConversionsCount(realm, dateStart, dateEnd, rewardType, networkName);

		double rewardSumInTargetCurrency = round(
				getRewardSumInTargetCurrency(realm, dateStart, dateEnd, rewardType, networkName), 2);
		double profitSumInTargetCurrency = round(
				getProfitSumInTargetCurrency(realm, dateStart, dateEnd, rewardType, networkName), 2);
		long totalRegistrations = getRegistrationsCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalSnapdealClickEvents = getSnapdealClickEventCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalSnapdealConversionEvents = getSnapdealConversionEventCount(realm, dateStart, dateEnd, rewardType,
				networkName);
		long totalSnapdealConversionApprovedEvents = getSnapdealConversionApprovedEventCount(realm, dateStart, dateEnd,
				rewardType, networkName);
		long totalQuidcoClickEvents = getQuidcoClickEventCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalQuidcoConversionEvents = getQuidcoConversionEventCount(realm, dateStart, dateEnd, rewardType,
				networkName);
		long totalQuidcoConversionApprovedEvents = getQuidcoConversionApprovedEventCount(realm, dateStart, dateEnd,
				rewardType, networkName);
		long totalSpins = getSpinCount(realm, dateStart, dateEnd, rewardType, networkName);
		SpinnerRewardsReport spinRewards = getSpinRewards(realm, dateStart, dateEnd, rewardType, networkName);
		long totalReferralClickEvents = getReferralClickEventCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalReferralSuccessEvents = getReferralSuccessEventCount(realm, dateStart, dateEnd, rewardType,
				networkName);

		double[] videosCalculation = calculateProfitFromEventType(UserEventCategory.VIDEO, dateStart, dateEnd,
				rewardType);
		double[] snapdealCalculation = calculateProfitFromEventType(UserEventCategory.SNAPDEAL, dateStart, dateEnd,
				rewardType);
		double[] quidcoCalculation = calculateProfitFromEventType(UserEventCategory.QUIDCO, dateStart, dateEnd,
				rewardType);

		double totalLoseFromReferral = calculateLoseFromReferrals(dateStart, dateEnd, rewardType);
		long totalWallSelections = getWallSelectionCount(realm, dateStart, dateEnd, rewardType, networkName);
		double totalPayout = getTotalOfferPayout(realm, dateStart, dateEnd, rewardType, networkName);
		double totalExpenses = getTotalExpenses(realm, dateStart, dateEnd, rewardType, networkName);
		totalExpenses = totalExpenses + totalLoseFromReferral;
		double totalProfit = totalPayout - totalExpenses;

		double[] installsData = getInstallEventsData(realm, dateStart, dateEnd, rewardType, networkName);
		long totalClicksDao = daoUserEvent.countTotalClicksForRewardTypeInDateRange(dateStart, dateEnd, rewardType);
		long totalUniqueClicksDao = daoUserEvent.countTotalUniqueClicksForRewardTypeInDateRange(dateStart, dateEnd,
				rewardType);
		long totalConversionsDao = daoUserEvent.countTotalConversionsForRewardTypeInDateRange(dateStart, dateEnd,
				rewardType);

		if (totalConversions != -1 && totalUniqueClicksDao > 0) {
			conversionRate = (double) totalConversions / (double) totalUniqueClicksDao;
			conversionRate = round(conversionRate, 2);
		}
		double conversionRateUnique = 0;
		if (totalUniqueClicksDao > 0 && totalConversionsDao > 0) {
			conversionRateUnique = (double) totalConversionsDao / (double) totalUniqueClicksDao;
			conversionRateUnique = round(conversionRateUnique, 2);
		}
		
		long totalSpinProfit = getTotalSpinProfit(dateStart,dateEnd);

		logger.info("total clicks: " + totalClicks);
		logger.info("total converions: " + totalConversions);
		logger.info("cr: " + conversionRate);
		logger.info("reward sum: " + rewardSumInTargetCurrency);
		logger.info("profit sum: " + profitSumInTargetCurrency);
		logger.info("total regs: " + totalRegistrations);
		logger.info("total snapdeal clicks events: " + totalSnapdealClickEvents);
		logger.info("total snapdeal conversions events: " + totalSnapdealConversionEvents);
		logger.info("total snapdeal conversions approved events: " + totalSnapdealConversionApprovedEvents);
		logger.info("total quidco clicks events: " + totalQuidcoClickEvents);
		logger.info("total quidco conversions events: " + totalQuidcoConversionEvents);
		logger.info("total quidco conversions approved events: " + totalQuidcoConversionApprovedEvents);
		logger.info("total spins events: " + totalSpins);
		logger.info("total referral events: " + totalReferralClickEvents);
		logger.info("total referral success events: " + totalReferralSuccessEvents);
		logger.info("spin rewards:" + spinRewards);
		logger.info("profit from videos: " + videosCalculation[0]);
		logger.info("profit from snapdeal: " + snapdealCalculation[0]);
		logger.info("profit from quidco: " + quidcoCalculation[0]);

		reportDH.setRewardTypeName(rewardType);
		reportDH.setReportPeriodName(reportPeriodName);
		reportDH.setReportPeriodNameString(reportPeriodNameString);
		reportDH.setDateStart(dateStart);
		reportDH.setDateEnd(dateEnd);
		reportDH.setClicksSum(totalClicks);
		reportDH.setConversionsSum(totalConversions);
		reportDH.setConversionRate(conversionRate);
		reportDH.setRewardSumInTargetCurrency(rewardSumInTargetCurrency);
		reportDH.setProfitSumInTargetCurrency(profitSumInTargetCurrency);
		reportDH.setRegistrationsSum(totalRegistrations);
		reportDH.setSnapdealClicksSum(totalSnapdealClickEvents);
		reportDH.setSnapdealConversionSum(totalSnapdealConversionEvents);
		reportDH.setSnapdealConversionApprovedSum(totalSnapdealConversionApprovedEvents);

		reportDH.setQuidcoClicksSum(totalQuidcoClickEvents);
		reportDH.setQuidcoConversionSum(totalQuidcoConversionEvents);
		reportDH.setQuidcoConversionApprovedSum(totalQuidcoConversionApprovedEvents);

		
		reportDH.setReferralClicksSum(totalReferralClickEvents);
		reportDH.setReferralSuccessSum(totalReferralSuccessEvents);
		reportDH.setReferralLoseSum(totalLoseFromReferral);

		reportDH.setProfitFromVideos(videosCalculation[0]);
		reportDH.setProfitFromSnapdeal(snapdealCalculation[0]);

		reportDH.setProfitFromQuidco(quidcoCalculation[0]);

		reportDH.setVideosCount((int) videosCalculation[1]);
		reportDH.setSnapdealCount((int) snapdealCalculation[1]);
		reportDH.setQuidcoCount((int) quidcoCalculation[1]);

		reportDH.setWallSelectionsSum(totalWallSelections);
		reportDH.setTotalPayout(totalPayout);
		reportDH.setTotalExpenses(totalExpenses);
		reportDH.setTotalProfit(totalProfit);

		reportDH.setTotalInstallPayout(installsData[0]);
		reportDH.setTotalInstallExpenses(installsData[1]);
		reportDH.setTotalInstallProfit(installsData[2]);

		reportDH.setTotalClicksDao(totalClicksDao);
		reportDH.setTotalUniqueClicksDao(totalUniqueClicksDao);
		reportDH.setTotalConversionsDao(totalConversionsDao);
		reportDH.setConversionRateDao(conversionRateUnique);
		
		reportDH.setTotalSpinProfit(totalSpinProfit);

		spinRewards.setProfit(totalSpinProfit + spinRewards.getLoss() );
		reportDH.setSpinSum(totalSpins);
		reportDH.setSpinRewards(spinRewards);

		return reportDH;
	}

	private long getTotalSpinProfit(Date dateStart, Date dateEnd) {
		QueryBuilder query;

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder
				.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
		boolQueryBuilder.must(QueryBuilders.wildcardQuery("message", "*bought*"));

		query = boolQueryBuilder;
		CountResponse response = client.prepareCount("*").setTypes("ab-log_data").setQuery(query).execute()
				.actionGet();
		return response.getCount();
		
	}

	private double[] getInstallEventsData(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {

		double totalExpenses = 0.0;
		double totalProfit = 0.0;
		double totalPayout = 0.0;
		try {

			List<UserEventEntity> installEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.INSTALL, dateStart, dateEnd, rewardType);

			for (UserEventEntity event : installEvents) {
				System.out.println(event);
				totalExpenses += event.getRewardValue();
				totalPayout += event.getOfferPayoutInTargetCurrency();
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}

		totalProfit = totalPayout - totalExpenses;
		System.out.println("TOTAL PAYOUT: " + totalPayout);
		System.out.println("TOTAL EXPENSES: " + totalExpenses);
		System.out.println("TOTAL PROFIT: " + totalProfit);
		return new double[] { totalPayout, totalExpenses, totalProfit };

	}

	private double getTotalExpenses(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		double totalExpenses = 0.0;
		try {
			List<UserEventEntity> events = new ArrayList<UserEventEntity>();
			List<UserEventEntity> installEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.INSTALL, dateStart, dateEnd, rewardType);
			List<UserEventEntity> snapdealEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.SNAPDEAL, dateStart, dateEnd, rewardType);
			List<UserEventEntity> quidcoEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.QUIDCO, dateStart, dateEnd, rewardType);
			List<UserEventEntity> videoEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.VIDEO, dateStart, dateEnd, rewardType);
			events.addAll(installEvents);
			events.addAll(snapdealEvents);
			events.addAll(quidcoEvents);
			events.addAll(videoEvents);

			for (UserEventEntity event : events) {

				totalExpenses += event.getRewardValue();
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return totalExpenses;
	}

	private double getTotalOfferPayout(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		double totalOfferPayout = 0.0;
		try {
			List<UserEventEntity> events = new ArrayList<UserEventEntity>();
			List<UserEventEntity> installEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.INSTALL, dateStart, dateEnd, rewardType);
			List<UserEventEntity> snapdealEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.SNAPDEAL, dateStart, dateEnd, rewardType);
			List<UserEventEntity> quidcoEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.QUIDCO, dateStart, dateEnd, rewardType);
			List<UserEventEntity> videoEvents = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(
					UserEventCategory.VIDEO, dateStart, dateEnd, rewardType);
			events.addAll(installEvents);
			events.addAll(snapdealEvents);
			events.addAll(quidcoEvents);
			events.addAll(videoEvents);

			for (UserEventEntity event : events) {
				totalOfferPayout += event.getOfferPayoutInTargetCurrency();
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return totalOfferPayout;
	}

	private long getWallSelectionCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("wallRewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));

			query = boolQueryBuilder;
			response = client.prepareCount("ab_wall_selections*").setTypes("wall_selections").setQuery(query).execute()
					.actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}

	}

	private double calculateLoseFromReferrals(Date dateStart, Date dateEnd, String rewardType) {
		double lose = 0.0;
		try {
			List<InvitationEntity> invitations = daoInvitation.findByRewardTypeInDateRange(rewardType, dateStart,
					dateEnd);
			System.out.println("**********************");
			System.out.println("dS: " + dateStart + " dE: " + dateEnd + " rewardType: " + rewardType);
			System.out.println("SELECTED: " + invitations.size());
			System.out.println("**********************");
			for (InvitationEntity invitation : invitations) {
				lose += invitation.getRewardValueInviting();
				lose += invitation.getRewardValueInvited();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return lose;
	}

	private double[] calculateProfitFromEventType(UserEventCategory category, Date dateStart, Date dateEnd,
			String rewardType) {
		System.out.println("CALCULATING PROFIT FROM EVENT TYPE : " + category + " " + dateStart + " " + dateEnd + " "
				+ rewardType);
		double result[] = new double[2];
		double resultProfit = 0.0;
		double resultCount = 0.0;
		try {

			List<UserEventEntity> events = daoUserEvent.findEventsWithCategoryAndDateRangeAndRewardType(category,
					dateStart, dateEnd, rewardType);

			for (UserEventEntity event : events) {
				resultProfit += event.getProfitValue();
				resultCount++;
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		System.out.println("Result profit: " + resultProfit);
		result[0] = resultProfit;
		result[1] = resultCount;
		return result;
	}

	private long getReferralClickEventCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("userEventCategory", UserEventCategory.INVITE.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "REFERRAL_CLICK"));

			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getReferralSuccessEventCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("userEventCategory", UserEventCategory.INVITE.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "REFERRAL_SUCCESS"));

			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getQuidcoConversionApprovedEventCount(RealmEntity realm, Date dateStart, Date dateEnd,
			String rewardType, String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("offerProviderName", UserEventCategory.QUIDCO.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", UserEventType.conversion.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("userEventCategory", "QUIDCO_PAID"));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private SpinnerRewardsReport getSpinRewards(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {

		SpinnerRewardsReport report = spinnerManager.generateReportInDateRange(dateStart, dateEnd, rewardType);
		/*
		 * String spinnerReportResult = "<b>Report:</b>";
		 * 
		 * spinnerReportResult += "<br/> Reward type: " +
		 * report.getRewardType(); spinnerReportResult += "<br/> Total spins: "
		 * + report.getTotalSpins(); spinnerReportResult +=
		 * "<br/> Loss from money events:" + report.getLoss();
		 * spinnerReportResult += "<br/> Start date:" + report.getStartDate();
		 * spinnerReportResult += "<br/> End date:" + report.getEndDate();
		 * spinnerReportResult += "<br/> Total unique user count: " +
		 * report.getUserCount(); spinnerReportResult += "<br/><br/>"; for
		 * (SpinnerRewardEntity spinnerReward :
		 * report.getSpinRewardsMap().keySet()) { spinnerReportResult +=
		 * "<br/>==============================="; spinnerReportResult +=
		 * "<br/> Spinner reward name: " + spinnerReward.getRewardName();
		 * spinnerReportResult += "<br/> Spinner reward type: " +
		 * spinnerReward.getRewardType(); spinnerReportResult +=
		 * "<br/> Spinner reward value: " + spinnerReward.getRewardValue(); int
		 * count = report.getSpinRewardsMap().get(spinnerReward);
		 * spinnerReportResult += "<br/> Event count: " + count; float
		 * percentage = (count * 100f) / report.getTotalSpins();
		 * spinnerReportResult += "<br/> Event percentage: " + percentage + "%";
		 * spinnerReportResult += "<br/> User unique count: " +
		 * report.getSpinRewardsUserMap().get(spinnerReward).size();
		 * 
		 * }
		 */
		return report;
	}

	private long getSpinCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType, String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "click"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("userEventCategory", UserEventCategory.SPINNER.toString()));

			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getQuidcoClickEventCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "click"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("offerProviderName", UserEventCategory.QUIDCO.toString()));

			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getQuidcoConversionEventCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "conversion"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("userEventCategory", UserEventCategory.QUIDCO.toString()));

			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getSnapdealConversionApprovedEventCount(RealmEntity realm, Date dateStart, Date dateEnd,
			String rewardType, String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("offerProviderName", UserEventCategory.SNAPDEAL.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", UserEventType.conversion.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("userEventCategory", "SNAPDEAL_APPROVED"));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getSnapdealConversionEventCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("offerProviderName", UserEventCategory.SNAPDEAL.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", UserEventType.conversion.toString()));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	private long getSnapdealClickEventCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {
		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "click"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			boolQueryBuilder.must(QueryBuilders.matchQuery("offerProviderName", UserEventCategory.SNAPDEAL.toString()));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", UserEventType.click.toString()));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}

	}

	public ReportDH getReportData(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType) {

		ReportDH reportDH = new ReportDH();
		String networkName = realm.getName();
		double conversionRate = -1;
		long totalClicks = getClicksCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalConversions = getConversionsCount(realm, dateStart, dateEnd, rewardType, networkName);
		long totalRegistrations = getRegistrationsCount(realm, dateStart, dateEnd, rewardType, networkName);
		if (totalConversions != -1 && totalClicks > 0) {
			conversionRate = (double) totalConversions / (double) totalClicks;
			conversionRate = round(conversionRate, 2);
		}

		double rewardSumInTargetCurrency = round(
				getRewardSumInTargetCurrency(realm, dateStart, dateEnd, rewardType, networkName), 2);
		double profitSumInTargetCurrency = round(
				getProfitSumInTargetCurrency(realm, dateStart, dateEnd, rewardType, networkName), 2);

		logger.info("total clicks: " + totalClicks);
		logger.info("total converions: " + totalConversions);
		logger.info("total regs: " + totalRegistrations);
		logger.info("cr: " + conversionRate);
		logger.info("reward sum: " + rewardSumInTargetCurrency);
		logger.info("profit sum: " + profitSumInTargetCurrency);

		reportDH.setRewardTypeName(rewardType);
		reportDH.setDateStart(dateStart);
		reportDH.setDateEnd(dateEnd);
		reportDH.setClicksSum(totalClicks);
		reportDH.setConversionsSum(totalConversions);
		reportDH.setRegistrationsSum(totalRegistrations);
		reportDH.setConversionRate(conversionRate);
		reportDH.setRewardSumInTargetCurrency(rewardSumInTargetCurrency);
		reportDH.setProfitSumInTargetCurrency(profitSumInTargetCurrency);

		// close es client as we create it upon every request triggered by AB UI
		closeESClient();

		return reportDH;
	}

	public long getClicksCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType, String networkName) {
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+
		// " returning report stats");

		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "click"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	public long getConversionsCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+
		// " returning report stats");

		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "conversion"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_clicks*").setTypes("clicks").setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	public long getRegistrationsCount(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+
		// " returning report stats");

		CountResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardTypeName", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareCount("ab_registrations*").setTypes("registrations").setQuery(query).execute()
					.actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getCount());
			System.out.println("elasticsearch response: {}: " + response.toString());

			return response.getCount();
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	public double getRewardSumInTargetCurrency(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "conversion"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_clicks*").setTypes("clicks").setQuery(query)
					.addAggregation(AggregationBuilders.sum("rewardSumInTargetCurrency").field("offerReward"))
					.setSize(0).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
			System.out.println("elasticsearch response: {}: " + response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			InternalSum internalSum = response.getAggregations().get("rewardSumInTargetCurrency");
			double sumValue = internalSum.getValue();

			return sumValue;

		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	public double getProfitSumInTargetCurrency(RealmEntity realm, Date dateStart, Date dateEnd, String rewardType,
			String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "conversion"));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_clicks*").setTypes("clicks").setQuery(query)
					.addAggregation(AggregationBuilders.sum("profitSumInTargetCurrency").field("profit")).setSize(0)
					.execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
			System.out.println("elasticsearch response: {}: " + response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			InternalSum internalSum = response.getAggregations().get("profitSumInTargetCurrency");
			double sumValue = internalSum.getValue();

			return sumValue;

		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY, realm.getId(),
					LogStatus.ERROR, Application.REPORTING_ACTIVITY + " error: " + exc.toString());

			return -1;
		}
	}

	public void getUserRetentionNew(RealmEntity realm, Date dateStart, Date dateEnd, int timeIntervalInDays,
			String rewardType, String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("wallRewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_wall_selections*").setTypes("wall_selections").setQuery(query)
					.addAggregation(AggregationBuilders.terms("uniquePhoneNumbers")
							.field("phoneNumber").subAggregation(AggregationBuilders.dateHistogram("dailyWallVisits")
									.field("@timestamp").interval(DateHistogram.Interval.MINUTE))
							.minDocCount(1))
					.execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			System.out.println("elasticsearch response: {}: " + response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			Terms terms = response.getAggregations().get("uniquePhoneNumbers");
			Collection<Terms.Bucket> bucketsUniquePhones = terms.getBuckets();
			Iterator it1 = bucketsUniquePhones.iterator();

			long uniqueUsers = 0;
			long uniqueDailyVisits = 0;

			while (it1.hasNext()) {
				uniqueDailyVisits = 0;
				Terms.Bucket b = (Terms.Bucket) it1.next();
				String phoneNumber = b.getKey();
				logger.info("-> " + phoneNumber);
				uniqueUsers++;
				DateHistogram wallRequests = b.getAggregations().get("dailyWallVisits");
				Collection<Bucket> bucketsWallRequets = (Collection<Bucket>) wallRequests.getBuckets();
				Iterator it2 = bucketsWallRequets.iterator();
				while (it2.hasNext()) {
					uniqueDailyVisits++;
					DateHistogram.Bucket dailyVisitsBucket = (DateHistogram.Bucket) it2.next();
					long uniqueDayVisits = dailyVisitsBucket.getDocCount();
				}
				logger.info("--> " + uniqueDailyVisits);// dailyVisitsBucket.toString());
				retentionMatrix[0][(int) uniqueDailyVisits] = retentionMatrix[0][(int) uniqueDailyVisits] + 1;

				// aggregate
				// put arraylist and increment its values - each array index
				// corresponds to days (startng from 1)
			}

			// calculate percentage fractions
			for (int x = 0; x < timeIntervalInDays; x++) {
				retentionMatrix[1][x] = round((double) retentionMatrix[0][x] / uniqueUsers, 2);
			}

			logger.info("time interval in days: " + timeIntervalInDays);
			logger.info("unique users: " + uniqueUsers);
			System.out.println("------ retention matrix ---------");
			for (int j = 0; j < timeIntervalInDays; j++) {
				System.out.print(j + "     ");
			}
			System.out.println("");

			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					System.out.print(round(retentionMatrix[i][j], 2) + "   ");
				}
				System.out.println("");
			}
			System.out.println("------ retention matrix ---------");
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());
		}
	}

	public double[][] getUserRetentionByWallRequests(RealmEntity realm, Date dateStart, Date dateEnd,
			int timeIntervalInDays, String rewardType, String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("wallRewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_wall_selections*").setTypes("wall_selections").setQuery(query)
					.addAggregation(AggregationBuilders.terms("uniquePhoneNumbers").field("phoneNumber").size(0)
							.subAggregation(AggregationBuilders.dateHistogram("dailyWallVisits").field("@timestamp")
									.interval(DateHistogram.Interval.DAY))
							.minDocCount(1).size(0))
					.execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			System.out.println("elasticsearch response: {}: " + response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			Terms terms = response.getAggregations().get("uniquePhoneNumbers");
			Collection<Terms.Bucket> bucketsUniquePhones = terms.getBuckets();
			Iterator it1 = bucketsUniquePhones.iterator();

			long uniqueUsers = 0;
			long uniqueDailyVisits = 0;

			while (it1.hasNext()) {
				uniqueDailyVisits = 0;
				Terms.Bucket b = (Terms.Bucket) it1.next();
				String phoneNumber = b.getKey();
				// logger.info("-> "+phoneNumber);
				uniqueUsers++;
				DateHistogram wallRequests = b.getAggregations().get("dailyWallVisits");
				Collection<Bucket> bucketsWallRequets = (Collection<Bucket>) wallRequests.getBuckets();
				Iterator it2 = bucketsWallRequets.iterator();
				while (it2.hasNext()) {
					uniqueDailyVisits++;
					DateHistogram.Bucket dailyVisitsBucket = (DateHistogram.Bucket) it2.next();
					long uniqueDayVisits = dailyVisitsBucket.getDocCount();
				}
				// logger.info("--> unique days visiting app:
				// "+uniqueDailyVisits+" for phone number:
				// "+phoneNumber);//dailyVisitsBucket.toString());
				retentionMatrix[0][(int) uniqueDailyVisits] = retentionMatrix[0][(int) uniqueDailyVisits] + 1;
			}

			// calculate percentage fractions
			for (int x = 0; x < timeIntervalInDays; x++) {
				retentionMatrix[1][x] = round(((double) retentionMatrix[0][x] / (double) uniqueUsers), 2);
			}

			logger.info("time interval in days: " + timeIntervalInDays);
			logger.info("unique users: " + uniqueUsers);
			System.out.println("------ retention matrix ---------");
			for (int j = 1; j < retentionMatrix[0].length; j++) {
				System.out.println("Day: " + j + " number of unique users (performing clicks): " + retentionMatrix[0][j]
						+ " percentage of total population: " + retentionMatrix[1][j] * 100 + "%");
			}
			System.out.println("");
			System.out.println("------ retention matrix ---------");

			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserRetention(RealmEntity realm, Date dateStart, Date dateEnd, int timeIntervalInDays,
			String rewardType, String networkName) {

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_clicks*").setTypes("clicks").setQuery(query)
					.addAggregation(AggregationBuilders.terms("uniquePhoneNumbers").field("phoneNumber").size(0)
							.subAggregation(AggregationBuilders.dateHistogram("dailyWallVisits").field("@timestamp")
									.interval(DateHistogram.Interval.DAY))
							.size(0).minDocCount(1))
					.execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			// System.out.println("elasticsearch response: {}: " +
			// response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			Terms terms = response.getAggregations().get("uniquePhoneNumbers");
			Collection<Terms.Bucket> bucketsUniquePhones = terms.getBuckets();
			Iterator it1 = bucketsUniquePhones.iterator();

			long uniqueUsers = 0;
			long uniqueDailyVisits = 0;

			while (it1.hasNext()) {
				uniqueDailyVisits = 0;
				Terms.Bucket b = (Terms.Bucket) it1.next();
				String phoneNumber = b.getKey();
				// logger.info("-> "+phoneNumber);
				uniqueUsers++;
				DateHistogram wallRequests = b.getAggregations().get("dailyWallVisits");
				Collection<Bucket> bucketsWallRequets = (Collection<Bucket>) wallRequests.getBuckets();
				Iterator it2 = bucketsWallRequets.iterator();
				while (it2.hasNext()) {
					uniqueDailyVisits++;
					DateHistogram.Bucket dailyVisitsBucket = (DateHistogram.Bucket) it2.next();
					long uniqueDayVisits = dailyVisitsBucket.getDocCount();
				}
				// logger.info("--> unique days visiting app:
				// "+uniqueDailyVisits+" for phone number:
				// "+phoneNumber);//dailyVisitsBucket.toString());
				retentionMatrix[0][(int) uniqueDailyVisits] = retentionMatrix[0][(int) uniqueDailyVisits] + 1;
			}

			// calculate percentage fractions
			for (int x = 0; x < timeIntervalInDays; x++) {
				logger.info("RetentionMatrix x " + x + " " + retentionMatrix[0][x] + " uniqueUsers" + uniqueUsers);
				if (uniqueUsers != 0 && retentionMatrix[0][x] != 0) {
					retentionMatrix[1][x] = round(((double) retentionMatrix[0][x] / (double) uniqueUsers), 2);
				} else {
					retentionMatrix[1][x] = 0;
				}
			}

			logger.info("time interval in days: " + timeIntervalInDays);
			logger.info("unique users: " + uniqueUsers);
			System.out.println("------ retention matrix ---------");
			for (int j = 1; j < retentionMatrix[0].length; j++) {
				System.out.println("Day: " + j + " number of unique users (performing clicks): " + retentionMatrix[0][j]
						+ " percentage of total population: " + retentionMatrix[1][j] * 100 + "%");
			}
			System.out.println("");
			System.out.println("------ retention matrix ---------");

			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserRetentionByWallRequestsToFile(RealmEntity realm, Date dateStart, Date dateEnd,
			int timeIntervalInDays, String rewardType, String networkName) {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("/home/mzj/tmp/userRetentionByWallRequestsDump.txt");
		} catch (IOException exception) {
			System.out.print(exception.getMessage());
			throw new RuntimeException("Could not open file for writing!");
		}

		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("wallRewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_wall_selections*").setTypes("wall_selections").setQuery(query)
					.addAggregation(AggregationBuilders.terms("uniquePhoneNumbers").field("phoneNumber").size(0)
							.subAggregation(AggregationBuilders.dateHistogram("dailyWallVisits").field("@timestamp")
									.interval(DateHistogram.Interval.DAY))
							.minDocCount(1).size(0))
					.execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			System.out.println("elasticsearch response: {}: " + response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			Terms terms = response.getAggregations().get("uniquePhoneNumbers");
			Collection<Terms.Bucket> bucketsUniquePhones = terms.getBuckets();
			Iterator it1 = bucketsUniquePhones.iterator();

			long uniqueUsers = 0;
			long uniqueDailyVisits = 0;

			while (it1.hasNext()) {
				uniqueDailyVisits = 0;
				Terms.Bucket b = (Terms.Bucket) it1.next();
				String phoneNumber = b.getKey();
				// logger.info("-> "+phoneNumber);
				uniqueUsers++;
				DateHistogram wallRequests = b.getAggregations().get("dailyWallVisits");
				Collection<Bucket> bucketsWallRequets = (Collection<Bucket>) wallRequests.getBuckets();
				Iterator it2 = bucketsWallRequets.iterator();
				while (it2.hasNext()) {
					uniqueDailyVisits++;
					DateHistogram.Bucket dailyVisitsBucket = (DateHistogram.Bucket) it2.next();
					long uniqueDayVisits = dailyVisitsBucket.getDocCount();
				}
				// logger.info("--> unique days visiting app:
				// "+uniqueDailyVisits+" for phone number:
				// "+phoneNumber);//dailyVisitsBucket.toString());
				retentionMatrix[0][(int) uniqueDailyVisits] = retentionMatrix[0][(int) uniqueDailyVisits] + 1;
			}

			// calculate percentage fractions
			for (int x = 0; x < timeIntervalInDays; x++) {
				retentionMatrix[1][x] = round(((double) retentionMatrix[0][x] / (double) uniqueUsers), 2);
			}

			logger.info("time interval in days: " + timeIntervalInDays);
			logger.info("unique users: " + uniqueUsers);
			System.out.println("------ retention matrix ---------");
			for (int j = 1; j < retentionMatrix[0].length; j++) {
				System.out.println("Day: " + j + " number of unique users (performing clicks): " + retentionMatrix[0][j]
						+ " percentage of total population: " + retentionMatrix[1][j] * 100 + "%");
				fileWriter.write("Day: " + j + " number of unique users (performing clicks): " + retentionMatrix[0][j]
						+ " percentage of total population: " + retentionMatrix[1][j] * 100 + "%");
				fileWriter.write("\n");
			}
			System.out.println("");
			System.out.println("------ retention matrix ---------");

			fileWriter.close();

			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserRetentionToFile(RealmEntity realm, Date dateStart, Date dateEnd, int timeIntervalInDays,
			String rewardType, String networkName) {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("/home/mzj/tmp/userRetention.txt");
		} catch (IOException exception) {
			System.out.print(exception.getMessage());
			throw new RuntimeException("Could not open file for writing!");
		}
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "conversion"));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_clicks*").setTypes("clicks").setQuery(query)
					.addAggregation(AggregationBuilders.terms("uniquePhoneNumbers").field("phoneNumber").size(0)
							.subAggregation(AggregationBuilders.dateHistogram("dailyWallVisits").field("@timestamp")
									.interval(DateHistogram.Interval.DAY))
							.size(0).minDocCount(1))
					.execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			System.out.println("elasticsearch response: {}: " + response.toString());

			// Terms terms = response.getAggregations().get("offerName");
			Terms terms = response.getAggregations().get("uniquePhoneNumbers");
			Collection<Terms.Bucket> bucketsUniquePhones = terms.getBuckets();
			Iterator it1 = bucketsUniquePhones.iterator();

			long uniqueUsers = 0;
			long uniqueDailyVisits = 0;

			while (it1.hasNext()) {
				uniqueDailyVisits = 0;
				Terms.Bucket b = (Terms.Bucket) it1.next();
				String phoneNumber = b.getKey();
				String bucketDate = "";
				// System.out.println("-> phone number: "+phoneNumber);
				uniqueUsers++;
				DateHistogram wallRequests = b.getAggregations().get("dailyWallVisits");
				Collection<Bucket> bucketsWallRequets = (Collection<Bucket>) wallRequests.getBuckets();
				Iterator it2 = bucketsWallRequets.iterator();
				while (it2.hasNext()) {
					DateHistogram.Bucket dailyVisitsBucket = (DateHistogram.Bucket) it2.next();
					long uniqueDayVisits = dailyVisitsBucket.getDocCount();
					bucketDate = dailyVisitsBucket.getKey();

					System.out.println(phoneNumber + ", " + bucketDate + ", " + dailyVisitsBucket.getDocCount());
					fileWriter.write(phoneNumber + ", " + bucketDate + ", " + dailyVisitsBucket.getDocCount());
					fileWriter.write("\n");
				}
			}

			fileWriter.close();
			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserClicksToFile(RealmEntity realm, Date dateStart, Date dateEnd, int timeIntervalInDays,
			String rewardType, String networkName) {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("/home/mzj/tmp/clicksDump.txt");
		} catch (IOException exception) {
			System.out.print(exception.getMessage());
			throw new RuntimeException("Could not open file for writing!");
		}
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "click"));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_clicks*").setTypes("clicks").setQuery(query).setSize(100000000)
					.addSort("phoneNumber", SortOrder.ASC).execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			// System.out.println("elasticsearch response: {}: " +
			// response.toString());

			SearchHit[] results = null;
			results = response.getHits().getHits();
			int counter = 0;
			for (SearchHit hit : results) {
				Map<String, Object> result = hit.getSource();
				String timestamp = (String) result.get("@timestamp");
				Date date = new Date((Long) result.get("@time")); // effectively
																	// GetField.getValue()
				String deviceType = (String) result.get("deviceType");
				String phoneNumber = (String) result.get("phoneNumber");
				String ipAddress = (String) result.get("ipAddress");
				double offerPayout = (double) result.get("offerPayout");
				double offerReward = (double) result.get("offerReward");
				String offerName = (String) result.get("offerName");
				String offerProviderName = (String) result.get("offerProviderName");
				String userEventCategory = (String) result.get("userEventCategory");
				String rewardTypeVal = (String) result.get("rewardType");
				String deviceTypeVal = (String) result.get("deviceType");

				if (ipAddress != null && ipAddress.length() > 0) {
					ipAddress = ipAddress.replace(" ", "");
					ipAddress = ipAddress.replace(",", "-");
				}

				// widgetDeviceEventLog
				// singleDevice-growlDeviceEventLog
				// System.out.println(result.toString());
				fileWriter.write(date.toString() + ", " + phoneNumber + ", " + deviceType + ", " + offerPayout + ", "
						+ offerReward + ", " + ipAddress + ", " + offerName + ", " + offerProviderName + ", "
						+ userEventCategory + ", " + rewardTypeVal + ", " + deviceTypeVal);
				fileWriter.write("\n");
				counter++;
			}

			fileWriter.close();

			logger.info("Total clicks: " + counter);
			fileWriter.close();
			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserConversionsToFile(RealmEntity realm, Date dateStart, Date dateEnd, int timeIntervalInDays,
			String rewardType, String networkName) {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("/home/mzj/tmp/conversionsDump.txt");
		} catch (IOException exception) {
			System.out.print(exception.getMessage());
			throw new RuntimeException("Could not open file for writing!");
		}
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", "conversion"));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_clicks*").setTypes("clicks").setQuery(query).setSize(100000000)
					.addSort("phoneNumber", SortOrder.ASC).execute().actionGet();

			// System.out.println("elasticsearch response: {} hits: " +
			// response.getHits().totalHits());
			// System.out.println("elasticsearch response: {}: " +
			// response.toString());

			SearchHit[] results = null;
			results = response.getHits().getHits();
			int counter = 0;
			for (SearchHit hit : results) {
				Map<String, Object> result = hit.getSource();
				String timestamp = (String) result.get("@timestamp");
				Date date = new Date((Long) result.get("@time")); // effectively
																	// GetField.getValue()
				String deviceType = (String) result.get("deviceType");
				String phoneNumber = (String) result.get("phoneNumber");
				String ipAddress = (String) result.get("ipAddress");
				double offerPayout = (double) result.get("offerPayout");
				double offerReward = (double) result.get("offerReward");
				String offerName = (String) result.get("offerName");
				String offerProviderName = (String) result.get("offerProviderName");
				String userEventCategory = (String) result.get("userEventCategory");
				String rewardTypeVal = (String) result.get("rewardType");
				String deviceTypeVal = (String) result.get("deviceType");

				if (ipAddress != null && ipAddress.length() > 0) {
					ipAddress = ipAddress.replace(" ", "");
					ipAddress = ipAddress.replace(",", "-");
				}

				// widgetDeviceEventLog
				// singleDevice-growlDeviceEventLog
				// System.out.println(result.toString());
				fileWriter.write(date.toString() + ", " + phoneNumber + ", " + deviceType + ", " + offerPayout + ", "
						+ offerReward + ", " + ipAddress + ipAddress + ", " + offerName + ", " + offerProviderName
						+ ", " + userEventCategory + ", " + rewardTypeVal + ", " + deviceTypeVal);

				fileWriter.write("\n");
				counter++;
			}

			fileWriter.close();

			logger.info("Total clicks: " + counter);
			fileWriter.close();
			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserRegistrationsToFile(RealmEntity realm, Date dateStart, Date dateEnd,
			int timeIntervalInDays, String rewardType, String networkName) {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("/home/mzj/tmp/registrationsDump.txt");
		} catch (IOException exception) {
			System.out.print(exception.getMessage());
			throw new RuntimeException("Could not open file for writing!");
		}
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			// boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType",
			// rewardType));
			// boolQueryBuilder.must(QueryBuilders.matchQuery("eventType",
			// "conversion"));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_registrations*").setTypes("registrations").setSize(10000000)
					.addSort("@timestamp", SortOrder.ASC).setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
			// System.out.println("elasticsearch response: {}: " +
			// response.toString());

			SearchHit[] results = null;
			results = response.getHits().getHits();
			int counter = 0;
			for (SearchHit hit : results) {
				Map<String, Object> result = hit.getSource();
				String timestamp = (String) result.get("@timestamp");
				Date date = new Date((Long) result.get("@time")); // effectively
																	// GetField.getValue()
				String deviceType = (String) result.get("deviceType");
				String email = (String) result.get("email");
				String phoneNumberExt = (String) result.get("phoneNumberExtension");
				String phoneNumber = (String) result.get("phoneNumber");
				String ipAddress = (String) result.get("ipAddress");
				String countryCode = (String) result.get("countryCode");
				String ageRange = (String) result.get("ageRange");
				// widgetDeviceEventLog
				// singleDevice-growlDeviceEventLog
				// System.out.println(result.toString());

				if (ipAddress != null && ipAddress.length() > 0) {
					ipAddress = ipAddress.replace(" ", "");
					ipAddress = ipAddress.replace(",", "-");
				}

				fileWriter.write(date.toString() + ", " + phoneNumberExt + ", " + phoneNumber + ", " + email + ", "
						+ deviceType + ", " + ipAddress + ", " + countryCode + ", " + ageRange);
				fileWriter.write("\n");
				counter++;
			}

			fileWriter.close();

			logger.info("Total registered users: " + counter);
			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	public double[][] getUserWallRequestsToFile(RealmEntity realm, Date dateStart, Date dateEnd, int timeIntervalInDays,
			String rewardType, String networkName) {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("/home/mzj/tmp/wallRequestsDump.txt");
		} catch (IOException exception) {
			System.out.print(exception.getMessage());
			throw new RuntimeException("Could not open file for writing!");
		}
		// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
		// realm.getId(),
		// LogStatus.OK,
		// Application.REPORT_GENERATION+ " returning report stats");

		SearchResponse response = null;
		client = getClient(hostName, clusterName);

		try {
			// first row - number of users that requested wall x days where x is
			// the array index
			// second row - percentage of users from the whole population of
			// identified users
			double[][] retentionMatrix = new double[2][timeIntervalInDays];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < timeIntervalInDays; j++) {
					retentionMatrix[i][j] = 0;
				}
			}

			QueryBuilder query;

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
			// boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType",
			// rewardType));
			// boolQueryBuilder.must(QueryBuilders.matchQuery("eventType",
			// "conversion"));
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(dateStart).to(dateEnd));
			query = boolQueryBuilder;
			response = client.prepareSearch("ab_wall_selections*").setTypes("wall_selections").setSize(10000000)
					.addSort("@timestamp", SortOrder.ASC).setQuery(query).execute().actionGet();

			System.out.println("elasticsearch response: {} hits: " + response.getHits().totalHits());
			// System.out.println("elasticsearch response: {}: " +
			// response.toString());

			SearchHit[] results = null;
			results = response.getHits().getHits();
			int counter = 0;
			for (SearchHit hit : results) {
				Map<String, Object> result = hit.getSource();
				String timestamp = (String) result.get("@timestamp");
				Date date = new Date((Long) result.get("@time")); // effectively
																	// GetField.getValue()
				String deviceType = (String) result.get("wallDeviceType");
				String email = (String) result.get("email");
				String phoneNumber = (String) result.get("phoneNumber");
				String ipAddress = (String) result.get("ipAddress");

				if (ipAddress != null && ipAddress.length() > 0) {
					ipAddress = ipAddress.replace(" ", "");
					ipAddress = ipAddress.replace(",", "-");
				}

				// widgetDeviceEventLog
				// singleDevice-growlDeviceEventLog
				// System.out.println(result.toString());
				fileWriter.write(
						date.toString() + ", " + phoneNumber + ", " + email + ", " + deviceType + ", " + ipAddress);
				fileWriter.write("\n");
				counter++;
			}

			fileWriter.close();

			logger.info("Total registered users: " + counter);
			return retentionMatrix;
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();

			// Application.getElasticSearchLogger().indexLog(Application.REPORTING_ACTIVITY,
			// realm.getId(),
			// LogStatus.ERROR,
			// Application.REPORTING_ACTIVITY+" error: "+exc.toString());

			return null;
		}
	}

	private List<IEventLog> getUserClicks(String eventType, DateTime startDate, DateTime endDate, String rewardType,
			String networkName) throws Exception {
		final String LOG_PATTERN = "ab_clicks*";
		final String EVENT_TYPE = eventType;
		final String SEARCH_TYPE = "clicks";
		final String SORT_FIELD = "@timestamp";

		List<IEventLog> eventLogs = new ArrayList<IEventLog>();

		try {

			SearchResponse response = getEventLogResponse(networkName, rewardType, EVENT_TYPE, startDate, endDate,
					LOG_PATTERN, SEARCH_TYPE, SORT_FIELD);

			SearchHit[] results = response.getHits().getHits();

			for (SearchHit hit : results) {
				ClickLog eventLog = new ClickLog();
				Map<String, Object> result = hit.getSource();

				eventLog.setTimestamp((String) result.get("@timestamp"));
				eventLog.setPhoneNumber((String) result.get("phoneNumber"));
				eventLog.setCountryCode((String) result.get("countryCode"));
				eventLog.setDeviceType((String) result.get("deviceType"));
				eventLog.setUserEventCategory((String) result.get("userEventCategory"));
				eventLog.setEventType((String) result.get("eventType"));
				eventLog.setRewardType((String) result.get("rewardType"));
				eventLog.setOfferProviderName((String) result.get("offerProviderName"));
				eventLog.setOfferId((String) result.get("offerId"));
				eventLog.setOfferName((String) result.get("offerName"));
				eventLog.setOfferCurrency((String) result.get("offerCurrency"));
				eventLog.setOfferPayout((double) result.get("offerPayout"));
				eventLog.setOfferReward((double) result.get("offerReward"));
				eventLog.setProfit((double) result.get("profit"));
				eventLog.setInternalTransactionId((String) result.get("internalTransactionId"));
				eventLog.setGaid((String) result.get("gaid"));
				eventLog.setIdfa((String) result.get("idfa"));

				eventLogs.add(eventLog);
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			throw new Exception("getUserClicks exception: " + exc.getMessage());
		}

		return eventLogs;
	}

	public List<IEventLog> getUserClicks(DateTime startDate, DateTime endDate, String rewardType, String networkName)
			throws Exception {

		return getUserClicks("click", startDate, endDate, rewardType, networkName);
	}

	public List<IEventLog> getUserConversions(DateTime startDate, DateTime endDate, String rewardType,
			String networkName) throws Exception {

		return getUserClicks("conversion", startDate, endDate, rewardType, networkName);
	}

	public List<IEventLog> getUserRegistrations(DateTime startDate, DateTime endDate, String rewardType,
			String networkName) throws Exception {
		final String LOG_PATTERN = "ab_registrations*";
		final String SEARCH_TYPE = "registrations";
		final String SORT_FIELD = "@timestamp";

		List<IEventLog> eventLogs = new ArrayList<IEventLog>();

		try {
			SearchResponse response = getEventLogResponse(networkName, null, null, startDate, endDate, LOG_PATTERN,
					SEARCH_TYPE, SORT_FIELD);

			SearchHit[] results = response.getHits().getHits();

			for (SearchHit hit : results) {
				RegistrationLog eventLog = new RegistrationLog();
				Map<String, Object> result = hit.getSource();

				eventLog.setTimestamp((String) result.get("@timestamp"));
				eventLog.setPhoneNumberExtension((String) result.get("phoneNumberExtension"));
				eventLog.setPhoneNumber((String) result.get("phoneNumber"));
				eventLog.setEmail((String) result.get("email"));
				eventLog.setDeviceType((String) result.get("deviceType"));
				eventLog.setMale((boolean) result.get("male"));
				eventLog.setAgeRange((String) result.get("ageRange"));
				eventLog.setLocale((String) result.get("locale"));
				eventLog.setSystemInfo((String) result.get("systemInfo"));
				eventLog.setGaid((String) result.get("gaid"));
				eventLog.setIdfa((String) result.get("idfa"));
				eventLog.setApplicationName((String) result.get("applicationName"));

				eventLogs.add(eventLog);
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			throw new Exception("getUserRegistrations exception: " + exc.getMessage());
		}

		return eventLogs;
	}

	public List<IEventLog> getUserWallRequests(DateTime startDate, DateTime endDate, String rewardType,
			String networkName) throws Exception {
		final String LOG_PATTERN = "ab_wall_selections*";
		final String SEARCH_TYPE = "wall_selections";
		final String SORT_FIELD = "@timestamp";

		List<IEventLog> eventLogs = new ArrayList<IEventLog>();

		try {

			SearchResponse response = getEventLogResponse(networkName, null, null, startDate, endDate, LOG_PATTERN,
					SEARCH_TYPE, SORT_FIELD);

			SearchHit[] results = response.getHits().getHits();

			for (SearchHit hit : results) {
				WallRequestLog eventLog = new WallRequestLog();
				Map<String, Object> result = hit.getSource();

				eventLog.setTimestamp((String) result.get("@timestamp"));
				eventLog.setPhoneNumberExtension((String) result.get("phoneNumberExtension"));
				eventLog.setPhoneNumber((String) result.get("phoneNumber"));
				eventLog.setEmail((String) result.get("email"));
				eventLog.setLocale((String) result.get("locale"));
				eventLog.setIpAddress((String) result.get("ipAddress"));
				eventLog.setWallRewardType((String) result.get("wallRewardType"));
				eventLog.setWallGeo((String) result.get("wallGeo"));
				eventLog.setWallDeviceType((String) result.get("wallDeviceType"));
				eventLog.setWallId((int) result.get("wallId"));
				eventLog.setUa((String) result.get("ua"));
				eventLog.setSystemInfo((String) result.get("systemInfo"));

				eventLogs.add(eventLog);
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			throw new Exception("getUserWallRequests exception: " + exc.getMessage());
		}

		return eventLogs;
	}

	public List<IEventLog> getCrashReports(DateTime startDate, DateTime endDate, String rewardType, String networkName)
			throws Exception {
		final String LOG_PATTERN = "ab_crash_reports*";
		final String SEARCH_TYPE = "crash_reports";
		final String SORT_FIELD = "@timestamp";

		List<IEventLog> eventLogs = new ArrayList<IEventLog>();

		try {
			SearchResponse response = getEventLogResponse(null, null, null, startDate, endDate, LOG_PATTERN,
					SEARCH_TYPE, SORT_FIELD);

			SearchHit[] results = response.getHits().getHits();

			for (SearchHit hit : results) {
				CrashReportLog eventLog = new CrashReportLog();
				Map<String, Object> result = hit.getSource();

				eventLog.setTimestamp((String) result.get("@timestamp"));
				eventLog.setPhoneNumberExtension((String) result.get("phoneNumberExtension"));
				eventLog.setPhoneNumber((String) result.get("phoneNumber"));
				eventLog.setIpAddress((String) result.get("ipAddress"));
				eventLog.setApplicationName((String) result.get("applicationName"));
				eventLog.setDeviceInfo((String) result.get("deviceInfo"));
				eventLog.setDeviceVersion((String) result.get("deviceVersion"));
				eventLog.setBreadcrumb((String) result.get("breadcrumb"));

				eventLogs.add(eventLog);
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			throw new Exception("getCrashReports exception: " + exc.getMessage());
		}

		return eventLogs;
	}

	public List<IEventLog> getMobileFaults(DateTime startDate, DateTime endDate, String rewardType, String networkName)
			throws Exception {
		final String LOG_PATTERN = "ab_mobile_faults*";
		final String SEARCH_TYPE = "mobile_faults";
		final String SORT_FIELD = "@timestamp";

		List<IEventLog> eventLogs = new ArrayList<IEventLog>();

		try {
			SearchResponse response = getEventLogResponse(null, null, null, startDate, endDate, LOG_PATTERN,
					SEARCH_TYPE, SORT_FIELD);

			SearchHit[] results = response.getHits().getHits();

			for (SearchHit hit : results) {
				MobileFaultLog eventLog = new MobileFaultLog();
				Map<String, Object> result = hit.getSource();

				eventLog.setTimestamp((String) result.get("@timestamp"));
				eventLog.setPhoneNumberExtension((String) result.get("phoneNumberExtension"));
				eventLog.setPhoneNumber((String) result.get("phoneNumber"));
				eventLog.setIpAddress((String) result.get("ipAddress"));
				eventLog.setAction((String) result.get("action"));
				eventLog.setErrorMessage((String) result.get("errorMessage"));
				eventLog.setMiscData((String) result.get("miscData"));
				eventLog.setSystemInfo((String) result.get("systemInfo"));

				eventLogs.add(eventLog);
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			throw new Exception("getMobileFaults exception: " + exc.getMessage());
		}

		return eventLogs;
	}

	public List<IEventLog> getSupportRequests(DateTime startDate, DateTime endDate, String rewardType,
			String networkName) throws Exception {
		final String LOG_PATTERN = "ab_support_requests*";
		final String SEARCH_TYPE = "support_requests";
		final String SORT_FIELD = "@timestamp";

		List<IEventLog> eventLogs = new ArrayList<IEventLog>();

		try {
			SearchResponse response = getEventLogResponse(networkName, null, null, startDate, endDate, LOG_PATTERN,
					SEARCH_TYPE, SORT_FIELD);

			SearchHit[] results = response.getHits().getHits();

			for (SearchHit hit : results) {
				SupportRequestLog eventLog = new SupportRequestLog();
				Map<String, Object> result = hit.getSource();

				eventLog.setTimestamp((String) result.get("@timestamp"));
				eventLog.setPhoneNumberExtension((String) result.get("phoneNumberExtension"));
				eventLog.setPhoneNumber((String) result.get("phoneNumber"));
				eventLog.setEmail((String) result.get("email"));
				eventLog.setDeviceType((String) result.get("deviceType"));
				eventLog.setLocale((String) result.get("locale"));
				eventLog.setIpAddress((String) result.get("ipAddress"));
				eventLog.setErrorCategory((String) result.get("errorCategory"));
				eventLog.setSupportQuestion((String) result.get("supportQuestion"));
				eventLog.setMiscData((String) result.get("miscData"));
				eventLog.setSystemInfo((String) result.get("systemInfo"));

				eventLogs.add(eventLog);
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			exc.printStackTrace();
			throw new Exception("getSupportRequests exception: " + exc.getMessage());
		}

		return eventLogs;
	}

	private SearchResponse getEventLogResponse(String networkName, String rewardType, String eventType,
			DateTime startDate, DateTime endDate, String logPattern, String searchType, String sortField) {

		SearchResponse response = null;
		QueryBuilder query;
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (networkName != null && !networkName.isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery("networkName", networkName));
		}
		if (rewardType != null && !rewardType.isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery("rewardType", rewardType));
		}
		if (eventType != null && !eventType.isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery("eventType", eventType));
		}
		if (startDate != null && endDate != null) {
			boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(startDate).to(endDate));
		}
		query = boolQueryBuilder;
		response = client.prepareSearch(logPattern).setTypes(searchType).setQuery(query).setSize(100000000)
				.addSort(sortField, SortOrder.ASC).execute().actionGet();

		closeESClient();

		return response;
	}

	public double round(double value, int places) {
		BigDecimal bd = new BigDecimal(0);
		try {
			if (places < 0)
				throw new IllegalArgumentException();

			bd = new BigDecimal(value);
			bd = bd.setScale(places, RoundingMode.HALF_DOWN);
		} catch (Exception exception) {
			logger.info("Failed value: " + value + " places: " + places);
			exception.printStackTrace();
		}
		return bd.doubleValue();
	}

	public void closeESClient() {
		logger.info("*** closing ES client from ReportingManager");
		client.close();
	}

	public List<LogEntry> getLogs(String searchWord) {
		final String TAG_TIME = "@time";
		final String TAG_LOG_STATUS = "@logStatus";
		final String TAG_MESSAGE = "message";
		final String TAG_SERVER_NAME = "serverName";

		List<SearchHit> searchHits = getLogsList(searchWord);
		List<LogEntry> logs = new ArrayList<LogEntry>();

		for (SearchHit hit : searchHits) {
			Timestamp time = null;
			String logStatus = "";
			String message = "";
			String serverName = "";
			Map<String, Object> log = hit.getSource();

			for (Entry<String, Object> entry : log.entrySet()) {
				switch (entry.getKey()) {
				case TAG_TIME:
					time = new Timestamp((long) entry.getValue());
					break;
				case TAG_LOG_STATUS:
					logStatus = new String(entry.getValue().toString());
					break;
				case TAG_MESSAGE:
					message = new String(entry.getValue().toString());
					break;
				case TAG_SERVER_NAME:
					serverName = new String(entry.getValue().toString());
					break;
				default:
					break;
				}
			}
			logs.add(new LogEntry(time, logStatus, message, serverName));
		}

		logger.info("returing found logs: " + logs.size() + " based on key: " + searchWord);

		return logs;
	}

	private List<SearchHit> getLogsList(String searchWord) {
		SearchResponse response = null;
		QueryBuilder query;

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder.must(QueryBuilders.matchQuery("message", searchWord));
		boolQueryBuilder.mustNot(QueryBuilders.matchQuery("SN", Application.LOGS_VIEWER_ACTIVITY));
		query = boolQueryBuilder;

		response = client.prepareSearch("ab*").setQuery(query).setSize(100000000).execute().actionGet();

		SearchHit[] searchHitsArray = null;
		searchHitsArray = response.getHits().getHits();

		List<SearchHit> searchHits = new ArrayList<SearchHit>();
		for (SearchHit searchHit : searchHitsArray) {
			searchHits.add(searchHit);
		}

		return searchHits;
	}

	public static void main(String[] args) {
		// ReportingManager manager = new ReportingManager("104.155.72.76",
		// "airrewardz"); //test server

		// http://52.19.88.104/ab/svc/v1/reportingApi/userRegistrations?reportingServerLogin=test&reportingServerPass=test&startDate=2016-01-01T01:01&endDate=2016-01-10T01:01&rewardType=Trippa-GB&networkName=BPM&hostName=52.19.88.104
		// ReportingManager manager = new ReportingManager("52.19.88.104",
		// "airrewardz"); //adjockey server
		// String rewardType = "Trippa-GB";

		// http://104.155.68.211:8080/ab/svc/v1/reportingApi/userRegistrations?reportingServerLogin=test&reportingServerPass=test&startDate=2016-01-01T01:01&endDate=2016-01-10T01:01&rewardType=AirRewardz-India&networkName=BPM&hostName=104.155.69.180
		ReportingManager manager = new ReportingManager("104.155.69.180", "airrewardz"); // prod
																							// server
		String rewardType = "AirRewardz-India";

		int numberOfHistoryDays = 6;
		Date dateEnd = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(dateEnd);
		c.add(Calendar.DAY_OF_MONTH, -numberOfHistoryDays);
		Date dateStart = new Date();
		dateStart.setTime(c.getTime().getTime());

		manager.getUserRegistrationsToFile(null, dateStart, dateEnd, numberOfHistoryDays + 1, rewardType, "BPM");

		manager.getUserClicksToFile(null, dateStart, dateEnd, numberOfHistoryDays + 1, rewardType, "BPM");

		manager.getUserConversionsToFile(null, dateStart, dateEnd, numberOfHistoryDays + 1, rewardType, "BPM");

		manager.getUserWallRequestsToFile(null, dateStart, dateEnd, numberOfHistoryDays + 1, rewardType, "BPM");

		// manager.getUserRetentionByWallRequestsToFile(null,
		// dateStart, dateEnd,
		// numberOfHistoryDays+2,
		// rewardType, "BPM");

		// ----------------- all below is deprecated -------------------
		// manager.getReportData(null,
		// ReportPeriodName.LAST_HOUR,
		// "Last hour",
		// dateStart, dateEnd, "AirRewardz-India", "BPM");

		// manager.getUserRetentionByWallRequests(null,
		// dateStart, dateEnd,
		// numberOfHistoryDays+2,
		// rewardType, "BPM");

		// manager.getUserRetention(null,
		// dateStart, dateEnd,
		// numberOfHistoryDays+2,
		// rewardType, "BPM");

		// manager.getUserRetentionToFile(null,
		// dateStart, dateEnd,
		// numberOfHistoryDays+1,
		// rewardType, "BPM");

		logger.info("Analysis interval: " + dateStart.toString() + " - " + dateEnd.toString());
		manager.closeESClient();
	}

}
