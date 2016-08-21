package is.web.beans.system;

import is.ejb.bl.spinner.RewardInterval;
import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.bl.spinner.SpinnerRewardType;
import is.ejb.bl.spinner.UniformDistributionSpinnerAlgorithm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOSpinnerReward;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;
import is.web.beans.offerRewardTypes.RewardTypeDataModelBean;
import is.web.beans.users.LoginBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.PieChartModel;


@ManagedBean(name = "spinnerGamificationBean")
@SessionScoped
public class SpinnerGamificationBean {
	@Inject
	private DAORewardType daoRewardType;

	@Inject
	private DAOSpinnerReward daoSpinnerReward;

	@Inject
	private Logger log;

	@Inject
	private SpinnerManager spinnerManager;

	private List<RewardTypeEntity> rewardTypeEntityList;
	private VideoGamificationRewardTypeTableDataModelBean rewardTypeDataModelBean;

	private RewardTypeEntity editRewardTypeEntity = new RewardTypeEntity();
	private RewardTypeEntity editRewardTypeEntityForSpinnerReward = new RewardTypeEntity();
	private SpinnerRewardTableDataModelBean spinnerRewardTableDataModelBean;
	private SpinnerRewardEntity editSpinnerRewardEntity = new SpinnerRewardEntity();
	private SpinnerRewardEntity addSpinnerRewardEntity = new SpinnerRewardEntity();
	private RewardTypeEntity testRewardType = new RewardTypeEntity();
	private int testSamples;
	private int dailySpinsSamples;
	private int totalSamples;
	private String testAnalysisResult[] = new String[3];
	private PieChartModel spinnerRewardsChartModel;
	private List<SelectItem> spinnerRewardTypes;

	@PostConstruct
	public void init() {

		try {

			editRewardTypeEntity = new RewardTypeEntity();
			rewardTypeEntityList = daoRewardType.findAll();
			rewardTypeDataModelBean = new VideoGamificationRewardTypeTableDataModelBean(rewardTypeEntityList);
			createPieModel1();
			updateSpinnerRewardsChartModel();
			initSpinnerRewardTypes();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void initSpinnerRewardTypes() {
		spinnerRewardTypes = new ArrayList<SelectItem>();
		for (SpinnerRewardType spinnerRewardType : SpinnerRewardType.values()) {
			spinnerRewardTypes.add(new SelectItem(spinnerRewardType.toString(), spinnerRewardType.toString()));
		}

	}

	public VideoGamificationRewardTypeTableDataModelBean getRewardTypeDataModelBean() {
		return rewardTypeDataModelBean;
	}

	public void setRewardTypeDataModelBean(VideoGamificationRewardTypeTableDataModelBean rewardTypeDataModelBean) {
		this.rewardTypeDataModelBean = rewardTypeDataModelBean;
	}

	public RewardTypeEntity getEditRewardTypeEntity() {
		return editRewardTypeEntity;
	}

	public void setEditRewardTypeEntity(RewardTypeEntity editRewardTypeEntity) {
		this.editRewardTypeEntity = editRewardTypeEntity;
	}

	public void update() {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Data successfully updated"));
		RequestContext.getCurrentInstance().execute("widgetEditVideoGamification.hide()");

	}

	public void refresh() {
		try {
			rewardTypeEntityList = daoRewardType.findAll();
			rewardTypeDataModelBean = new VideoGamificationRewardTypeTableDataModelBean(rewardTypeEntityList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getRewardTypeSpinnerRewardCount(RewardTypeEntity rewardType) {
		int count = 0;
		try {
			if (rewardType != null) {

				List<SpinnerRewardEntity> rewards = daoSpinnerReward.findByRewardTypeId(rewardType.getId());
				if (rewards != null) {
					count = rewards.size();
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();

		}

		return count;

	}

	public RewardTypeEntity getEditRewardTypeEntityForSpinnerReward() {
		return editRewardTypeEntityForSpinnerReward;
	}

	public void setEditRewardTypeEntityForSpinnerReward(RewardTypeEntity editRewardTypeEntityForSpinnerReward) {
		try {
			log.info("Setting reward type for spinner rewards:" + editRewardTypeEntityForSpinnerReward.getId());
			this.editRewardTypeEntityForSpinnerReward = editRewardTypeEntityForSpinnerReward;
			List<SpinnerRewardEntity> spinnerRewards = daoSpinnerReward.findByRewardTypeId(editRewardTypeEntityForSpinnerReward.getId());
			spinnerRewardTableDataModelBean = new SpinnerRewardTableDataModelBean(spinnerRewards);
			updateSpinnerRewardsChartModel();
			log.info("Table spinner reward model bean created.");
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public SpinnerRewardTableDataModelBean getSpinnerRewardTableDataModelBean() {
		return spinnerRewardTableDataModelBean;
	}

	public void setSpinnerRewardTableDataModelBean(SpinnerRewardTableDataModelBean spinnerRewardTableDataModelBean) {
		this.spinnerRewardTableDataModelBean = spinnerRewardTableDataModelBean;
	}

	public SpinnerRewardEntity getEditSpinnerRewardEntity() {
		return editSpinnerRewardEntity;
	}

	public void setEditSpinnerRewardEntity(SpinnerRewardEntity editSpinnerRewardEntity) {
		this.editSpinnerRewardEntity = editSpinnerRewardEntity;
	}

	public void editTableRow(SpinnerRewardEntity edit) {
		log.info("Setting to edit:" + editSpinnerRewardEntity.getId());
		log.info("NAME:" + editSpinnerRewardEntity.getRewardName());
		editSpinnerRewardEntity = edit;

		RequestContext.getCurrentInstance().update("tabView:idWidgetSpinnerRewardEditEntry");
		RequestContext.getCurrentInstance().update("tabView:idSpinnerModelRowEditGrid");
	}

	public void processAddSpinnerReward() {
		log.info("Processing add spinner reward");
		this.addSpinnerRewardEntity.setRewardTypeId(this.getEditRewardTypeEntityForSpinnerReward().getId());
		this.addSpinnerRewardEntity.setUpdateTime(new Date());

		BigDecimal probability = BigDecimal.valueOf(addSpinnerRewardEntity.getRatioX())
				.divide(BigDecimal.valueOf(addSpinnerRewardEntity.getRatioY()), 7, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
		BigDecimal probabilityLeft = countLeftProbabilityForRewardType(null);
		log.info("Probability:" + probability + "Left:" + probabilityLeft);
		if (probability.compareTo(probabilityLeft) == 1) {
			if (probabilityLeft.compareTo(new BigDecimal(0.0)) == 1) {
				BigDecimal probabilityLeftRatio = BigDecimal.valueOf(100.0).divide(probabilityLeft, 0, RoundingMode.FLOOR);
				log.info("Probability to big.");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Probability is too big. Max probability: 1:" + probabilityLeftRatio));
				RequestContext.getCurrentInstance().update("tabView:idSpinnerRewardModelGrowl");
				return;
			} else {
				log.info("There is already max probability (100%) in all spinner rewards.");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "There is already max probability (100%) in all spinner rewards."));
				RequestContext.getCurrentInstance().update("tabView:idSpinnerRewardModelGrowl");
				return;
			}
		}

		this.addSpinnerRewardEntity.setRewardProbability(probability);
		daoSpinnerReward.createOrUpdate(addSpinnerRewardEntity);
		this.addSpinnerRewardEntity = new SpinnerRewardEntity();
		updateTabViews();

	}

	public void processEditSpinnerReward() {
		log.info("Updating spinner reward");

		this.editSpinnerRewardEntity.setRewardTypeId(this.getEditRewardTypeEntityForSpinnerReward().getId());
		this.editSpinnerRewardEntity.setUpdateTime(new Date());
		BigDecimal probability = BigDecimal.valueOf(editSpinnerRewardEntity.getRatioX())
				.divide(BigDecimal.valueOf(editSpinnerRewardEntity.getRatioY()), 7, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

		BigDecimal probabilityLeft = countLeftProbabilityForRewardType(editSpinnerRewardEntity);
		log.info("Probability:" + probability + "Left:" + probabilityLeft);
		if (probability.compareTo(probabilityLeft) == 1) {
			if (probabilityLeft.compareTo(BigDecimal.valueOf(0.0)) != 0) {

				BigDecimal probabilityLeftRatio = BigDecimal.valueOf(100.0).divide(probabilityLeft, 0, RoundingMode.FLOOR);
				log.info("Probability to big.");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Probability is too big. Max probability: 1:" + probabilityLeftRatio));
				RequestContext.getCurrentInstance().update("tabView:idSpinnerRewardModelGrowl");
				return;
			}
		}

		editSpinnerRewardEntity.setRewardProbability(probability);
		log.info("changing spinner probability to:" + probability);
		this.daoSpinnerReward.createOrUpdate(editSpinnerRewardEntity);
		daoSpinnerReward.createOrUpdate(editSpinnerRewardEntity);
		this.editSpinnerRewardEntity = new SpinnerRewardEntity();
		log.info("Updating success");
		updateTabViews();

	}

	public void refreshSpinnerRewardTable() {
		try {
			List<SpinnerRewardEntity> spinnerRewards = daoSpinnerReward.findByRewardTypeId(editRewardTypeEntityForSpinnerReward.getId());
			spinnerRewardTableDataModelBean = new SpinnerRewardTableDataModelBean(spinnerRewards);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateTabViews() {

		updateSpinnerRewardsChartModel();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Data updated."));
		refreshSpinnerRewardTable();

		RequestContext.getCurrentInstance().update("tabView:idSpinnerRewardModelGrowl");
		RequestContext.getCurrentInstance().update("tabView:idSpinnerGamificationTable");
		// RequestContext.getCurrentInstance().update("tabView:idWidgetSpinnerRewardEditDialog");
		RequestContext.getCurrentInstance().update("tabView:idWidgetSpinnerRewardEditTable");
		RequestContext.getCurrentInstance().update("tabView:idSpinnerRewardChart");

	}

	public void deleteSpinnerRewardEntity(SpinnerRewardEntity spinner) {

		daoSpinnerReward.delete(spinner);
		updateTabViews();
	}

	public SpinnerRewardEntity getAddSpinnerRewardEntity() {
		return addSpinnerRewardEntity;
	}

	public void setAddSpinnerRewardEntity(SpinnerRewardEntity addSpinnerRewardEntity) {
		this.addSpinnerRewardEntity = addSpinnerRewardEntity;
	}

	public RewardTypeEntity getTestRewardType() {
		return testRewardType;
	}

	public void setTestRewardType(RewardTypeEntity testRewardType) {
		this.testRewardType = testRewardType;
	}

	public int getTestSamples() {
		return testSamples;
	}

	public void setTestSamples(int testSamples) {
		this.testSamples = testSamples;
	}

	public void testRewardEntity(RewardTypeEntity rewardType) {

		log.info("Setting to test reward type with id:" + rewardType.getId());
		this.testRewardType = rewardType;
		this.testSamples = 0;
		RequestContext.getCurrentInstance().update("tabView:idWidgetGeneratorTest");
	}

	private List<SpinnerRewardEntity> addFailIfNecessary(List<SpinnerRewardEntity> list) {
		BigDecimal sumOfProbabilities = BigDecimal.valueOf(0.0);
		for (SpinnerRewardEntity spinnerReward : list) {
			sumOfProbabilities = sumOfProbabilities.add(spinnerReward.getRewardProbability());
		}
		if (sumOfProbabilities.compareTo(BigDecimal.valueOf(100.00)) == -1) {
			log.info("Adding fail reward : " + sumOfProbabilities);

			list.add(spinnerManager.produceFailedSpinnerReward(sumOfProbabilities));

		}

		return list;
	}

	public void runTest() {
		try {
			log.info("Running test for:" + this.testRewardType.getName() + " with " + testSamples + " samples.");
			List<SpinnerRewardEntity> spinnerRewardList = daoSpinnerReward.findByRewardTypeId(testRewardType.getId());
			spinnerRewardList = addFailIfNecessary(spinnerRewardList);
			List<SpinnerRewardEntity> rewardList = new ArrayList<SpinnerRewardEntity>();
			totalSamples = testSamples + dailySpinsSamples;
			for (int i = 0; i < totalSamples; i++) {
				rewardList.add(spinnerManager.generateRewardForTest(testRewardType));
			}
			List<SpinnerRewardEntity> currentIterationRewards = rewardList;
			while (true) {
				log.info("************************************************************************");
				log.info("Current iteration item list size:" + currentIterationRewards.size());
				log.info("************************************************************************");
				if (currentIterationRewards.size() == 0) {
					break;
				}
				int count = 0;
				for (SpinnerRewardEntity spinnerReward : currentIterationRewards) {
					if (spinnerReward.getRewardType().equals(SpinnerRewardType.SPIN_AGAIN.toString())) {
						count++;
					}
				}
				currentIterationRewards = new ArrayList<SpinnerRewardEntity>();
				for (int i = 0; i < count; i++) {
					SpinnerRewardEntity spinnerReward = spinnerManager.generateRewardForTest(testRewardType);
					rewardList.add(spinnerReward);
					currentIterationRewards.add(spinnerReward);
				}

			}

			analyzeResultList(spinnerRewardList, rewardList);

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void analyzeResultList(List<SpinnerRewardEntity> origList, List<SpinnerRewardEntity> resultList) {
		testAnalysisResult = new String[4];
		int additionalResults = resultList.size() - totalSamples;
		testAnalysisResult[2] = "Tested " + this.testRewardType.getName() + " with:" + "<br>-> " + testSamples
				+ " bought spins" + "<br>-> " + dailySpinsSamples + " free spins"
				+ "<br>Total " + totalSamples + " samples." + "<br>Resulted in " + resultList.size() + " generated rewards (" + totalSamples
				+ " samples and " + additionalResults + " as spin rewards) ";

		System.out.println("ANALYZE");
		int length = resultList.size();
		System.out.println("List contains: " + length + "elements");
		List<RewardInterval> rewardIntervalList = spinnerManager.getPreparedIntervalsForSpinnerRewards(origList);
		testAnalysisResult[1] = "";

		for (SpinnerRewardEntity res : resultList) {
			System.out.println(res);
		}
		testAnalysisResult[1] += "<ul>";
		testAnalysisResult[0] = "";
		for (SpinnerRewardEntity orig : origList) {

			int count = 0;
			double sum = 0;
			for (SpinnerRewardEntity result : resultList) {
				if (orig.getId() == result.getId()) {
					count++;
					sum = sum + result.getGenerated();
				}
			}
			double mean = sum / count;
			double percentage = (count * 100.0) / length;
			double variance = 0;
			for (SpinnerRewardEntity result : resultList) {
				if (orig.getId() == result.getId()) {
					variance = variance + Math.pow(result.getGenerated() - mean, 2);
				}
			}
			variance = variance / count;
			double deviation = Math.sqrt(variance);

			String result = "<b>Reward: </b> " + orig.getRewardName() + "</b></br><b>Value: " + orig.getRewardValue()
					+ "</b></br><b>Original Probability: </b>" + orig.getRewardProbability() + "%</b></br><b>Generated probability: </b>" + count
					+ "/" + length + " = " + percentage + " %</br>" + "<b>Mean: </b>" + mean + "</br><b>Variance: </b>" + variance
					+ "</br><b>Deviation: </b>" + deviation + "</br>-------------------------------</br>";
			System.out.println(result);
			testAnalysisResult[0] += result + "</br>";

			testAnalysisResult[1] += "<li>";

			testAnalysisResult[1] += this.getIntervalForSpinnerRewrd(rewardIntervalList, orig);
			testAnalysisResult[1] += " <b>" + orig.getRewardName() + "</b> ";
			testAnalysisResult[1] += "<br> ->HITS: <b>" + count + " </b> MEAN: <b>" + mean + "</b> DEVIATION: <b>" + deviation + "</b> VARIANCE: <b>"
					+ variance + "</b> <br><br>";
			testAnalysisResult[1] += "</li>";

		}
		testAnalysisResult[1] += "</ul>";
		testAnalysisResult[3] = monetizationAnalysis(origList, resultList);
		this.updatePieChartModel(origList, resultList);
	}

	private double getSpinnerUseValueForGeo(int rewardTypeId) {
		try {
			RewardTypeEntity rewardType = daoRewardType.findById(rewardTypeId);
			if (rewardType != null) {
				return rewardType.getSpinnerUseValue();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return 0.0;
	}

	private String monetizationAnalysis(List<SpinnerRewardEntity> origList, List<SpinnerRewardEntity> resultList) {

		String monetizationResult = "";
		monetizationResult += "<br>==================================<b>MONETIZATION</b>====================================<br>";
		BigDecimal totalSpentValue = new BigDecimal(0.0);
		for (SpinnerRewardEntity orig : origList) {
			int count = 0;
			for (SpinnerRewardEntity result : resultList) {
				if (orig.getId() == result.getId()) {
					count++;
				}
			}
			if (orig.getRewardType().equals(SpinnerRewardType.MONEY.toString())) {
				BigDecimal amount = orig.getRewardValue().multiply(new BigDecimal(count));
				monetizationResult += "Reward type MONEY(" + orig.getRewardName() + "): " + count + " * " + orig.getRewardValue() + " = <b>" + amount
						+ "</b><br>";
				totalSpentValue = totalSpentValue.add(amount);
			}
			if (orig.getRewardType().equals(SpinnerRewardType.SPIN_AGAIN.toString())) {
				monetizationResult += "Reward type SPIN AGAIN(" + orig.getRewardName() + "): " + count + " counted as additional spins<br>";
			}
			if (orig.getRewardType().equals(SpinnerRewardType.UNLOCK_VIDEOS.toString())) {
				monetizationResult += "Reward type UNLOCK_VIDEOS(" + orig.getRewardName() + "): " + "doesnt count<br>";

			}
			if (orig.getRewardType().equals(SpinnerRewardType.FAIL.toString())) {
				monetizationResult += "Reward type FAIL(" + orig.getRewardName() + "): " + count + " doesnt count<br>";
			}

		}
		BigDecimal gainedProfitValue = new BigDecimal(testSamples).multiply(new BigDecimal(
				getSpinnerUseValueForGeo(origList.get(0).getRewardTypeId())));

		monetizationResult += "<br>TOTAL SPENT: <b>" + totalSpentValue + "</b> <br>";
		monetizationResult += "TOTAL GAINED: <b>" + gainedProfitValue + "</b> <br>";
		monetizationResult += "Gained is samples which are bought by users * spinner use value = " + this.testSamples + " * "
				+ getSpinnerUseValueForGeo(origList.get(0).getRewardTypeId());
		monetizationResult += "<br>=================================================================================<br>";
		return monetizationResult;
	}

	private String getIntervalForSpinnerRewrd(List<RewardInterval> rewardIntervalList, SpinnerRewardEntity spinnerReward) {
		String intervalResult = "";
		for (RewardInterval interval : rewardIntervalList) {
			if (interval.getSpinnerRewardEntity().getId() == spinnerReward.getId()) {

				if (interval.isFromClosed())
					intervalResult += "< ";
				else
					intervalResult += "( ";

				intervalResult += interval.getFromValue() + " ; " + interval.getToValue();
				if (interval.isToClosed())
					intervalResult += " >";
				else
					intervalResult += " )";

			}
		}
		return intervalResult;

	}

	private void updatePieChartModel(List<SpinnerRewardEntity> origList, List<SpinnerRewardEntity> resultList) {
		pieModel1 = new PieChartModel();
		for (SpinnerRewardEntity orig : origList) {
			int count = 0;
			for (SpinnerRewardEntity result : resultList) {
				if (orig.getId() == result.getId()) {
					count++;

				}
			}
			double percentage = (count * 100.0) / resultList.size();
			percentage = Math.floor(percentage * 100) / 100;
			pieModel1.set(orig.getRewardName() + "(id:" + orig.getId() + ")[" + percentage + "%]", count);

		}
		RequestContext.getCurrentInstance().update("tabView:idWidgetGeneratorTestResult");
	}

	public PieChartModel getSpinnerRewardsChartModel() {
		return spinnerRewardsChartModel;
	}

	private BigDecimal countLeftProbabilityForRewardType(SpinnerRewardEntity spinnerRewardExcluded) {
		log.info("Counting left probability");
		BigDecimal leftProbability = BigDecimal.valueOf(0.0);
		try {
			if (this.editRewardTypeEntityForSpinnerReward != null) {
				leftProbability = BigDecimal.valueOf(100.0);
				List<SpinnerRewardEntity> spinnerRewardList = daoSpinnerReward.findByRewardTypeId(this.editRewardTypeEntityForSpinnerReward.getId());
				for (SpinnerRewardEntity spinnerReward : spinnerRewardList) {

					if (spinnerRewardExcluded != null && spinnerReward.getId() == spinnerRewardExcluded.getId()) {
						log.info("SpinnerRewardType excluded:"+spinnerRewardExcluded.getId());
					} else {

						leftProbability = leftProbability.subtract(spinnerReward.getRewardProbability());
						log.info("Left probability:" + leftProbability);
					}
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return leftProbability;
	}

	private void updateSpinnerRewardsChartModel() {
		try {
			log.info("Updating spinner rewards chart model. reward type:" + this.editRewardTypeEntityForSpinnerReward);
			spinnerRewardsChartModel = new PieChartModel();
			if (this.editRewardTypeEntityForSpinnerReward != null) {
				BigDecimal totalPercentage = BigDecimal.valueOf(0.0);
				List<SpinnerRewardEntity> spinnerRewardsList = daoSpinnerReward.findByRewardTypeId(this.editRewardTypeEntityForSpinnerReward.getId());
				for (SpinnerRewardEntity spinnerReward : spinnerRewardsList) {
					totalPercentage = totalPercentage.add(spinnerReward.getRewardProbability());

					spinnerRewardsChartModel.set(
							spinnerReward.getRewardName() + " (" + spinnerReward.getId() + ") [" + spinnerReward.getRewardProbability() + "%]",
							spinnerReward.getRewardProbability());
				}
				if (totalPercentage.compareTo(BigDecimal.valueOf(100)) == -1) {
					BigDecimal failValue = BigDecimal.valueOf(100).subtract(totalPercentage);
					spinnerRewardsChartModel.set("FAIL (0) [" + failValue + "%]", failValue);
				}
			} else {
				spinnerRewardsChartModel.set("test", 100);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private PieChartModel pieModel1;

	public PieChartModel getPieModel1() {
		return pieModel1;
	}

	private void createPieModel1() {
		pieModel1 = new PieChartModel();

		pieModel1.set("Brand 1", 540);
		pieModel1.set("Brand 2", 325);
		pieModel1.set("Brand 3", 702);
		pieModel1.set("Brand 4", 421);

	}

	public String[] getTestAnalysisResult() {
		return testAnalysisResult;
	}

	public void setTestAnalysisResult(String[] testAnalysisResult) {
		this.testAnalysisResult = testAnalysisResult;
	}

	public List<SelectItem> getSpinnerRewardTypes() {
		return spinnerRewardTypes;
	}

	public void setSpinnerRewardTypes(List<SelectItem> spinnerRewardTypes) {
		this.spinnerRewardTypes = spinnerRewardTypes;
	}

	public void saveRewardType() {
		try {
			log.info("Saving edit reward type entity for spinner reward");
			daoRewardType.createOrUpdate(editRewardTypeEntityForSpinnerReward);
			updateTabViews();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public int getDailySpinsSamples() {
		return dailySpinsSamples;
	}

	public void setDailySpinsSamples(int dailySpinsSamples) {
		this.dailySpinsSamples = dailySpinsSamples;
	}

}
