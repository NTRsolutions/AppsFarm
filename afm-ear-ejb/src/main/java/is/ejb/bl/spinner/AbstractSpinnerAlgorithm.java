package is.ejb.bl.spinner;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.SpinnerRewardEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractSpinnerAlgorithm {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public AbstractSpinnerAlgorithm() {
	}

	public List<SpinnerRewardEntity> validateSpinnerRewardList(List<SpinnerRewardEntity> list) {

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Validating spinner reward list" + list);
		logger.info("Validating spinner reward list");

		if (list == null) {
			logger.info("List is null. Cant start spinner.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Validating spinner reward list is null. Creating arraylist.");
			list = new ArrayList<SpinnerRewardEntity>();
		}

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Validating spinner reward list:" + list.toString());
		validateProbabilitySum(list);
		logger.info("Validating spinner reward list completed");

		return list;

	}

	private void validateProbabilitySum(List<SpinnerRewardEntity> list) {
		if (list == null) {
			logger.info("List is null.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Validating probability sum failed - list null");
			return;
		}

		BigDecimal sum = countSumOfProbabilityOfAllElements(list);
		logger.info("Sum of probability of all elements is:" + sum);
		if (sum.compareTo(BigDecimal.valueOf(100.0)) == -1) {
			logger.info("Sum is below 100, adding empty reward.");
			logger.info("Sum:" + sum);
			list.add(produceFailedSpinnerReward(sum));
		}
	}

	public SpinnerRewardEntity produceFailedSpinnerReward(BigDecimal probability) {
		if (probability == null) {
			return null;
		}
		return new SpinnerRewardEntity(0, 0, new Date(), BigDecimal.valueOf(100.0).subtract(probability), "FAIL",
				"FAIL", BigDecimal.valueOf(0.0),"Spinner reward failed. Please try again.");
	}

	public void printSpinnerRewardsInformation(List<SpinnerRewardEntity> list) {

		String result = "";
		result += "==============================";
		result += " Spinner Reward Table";
		result += "==============================";
		if (list != null) {
			for (SpinnerRewardEntity spinnerReward : list) {
				result += "\n" + spinnerReward.toString();
			}
		}

		logger.info(result);

	}

	private BigDecimal countSumOfProbabilityOfAllElements(List<SpinnerRewardEntity> list) {
		if (list == null) {
			logger.info("List is null.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Count sum of probabilities failed - list null");
			return null;
		}

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Count sum of probability of all elements:" + list.toString());
		BigDecimal count = BigDecimal.valueOf(0);
		for (SpinnerRewardEntity spinnerElement : list) {
			count = count.add(spinnerElement.getRewardProbability());
		}

		return count;
	}

	public List<RewardInterval> prepareSpinnerRewardIntervals(List<SpinnerRewardEntity> list) {

		if (list == null) {
			logger.info("List is null.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Prepare spinner reward intervals failed - list null");
			return null;
		}

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Preparing spinner reward intervals" + list.toString());
		List<RewardInterval> rewardIntervalList = new ArrayList<RewardInterval>();
		BigDecimal intervalsStarting = BigDecimal.valueOf(0.0);
		BigDecimal intervalsClosing = BigDecimal.valueOf(0.0);
		BigDecimal divisor = BigDecimal.valueOf(100);
		for (SpinnerRewardEntity spinnerReward : list) {

			RewardInterval interval = new RewardInterval();
			interval.setSpinnerRewardEntity(spinnerReward);
			interval.setFromValue(intervalsStarting);
			interval.setFromClosed(true);
			intervalsClosing = intervalsClosing.add(spinnerReward.getRewardProbability().divide(divisor));
			interval.setToValue(intervalsClosing);
			interval.setToClosed(false);
			intervalsStarting = intervalsClosing;

			rewardIntervalList.add(interval);

		}
		rewardIntervalList.get(rewardIntervalList.size() - 1).setToClosed(true);

		return rewardIntervalList;
	}

	public SpinnerRewardEntity findSpinnerRewardForGeneratedValue(List<RewardInterval> intervalList,
			double generated) {

		if (intervalList == null) {
			logger.info("List is null.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Find spinner reward for generated value failed - list null");
			return null;
		}

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Finding spinner reward for generated value" + intervalList + " generated:" + generated);
		SpinnerRewardEntity foundSpinnerRewardEntity = null;
		for (RewardInterval interval : intervalList) {
			if (checkIfGeneratedValueFitInInterval(interval, generated)) {
				foundSpinnerRewardEntity = interval.getSpinnerRewardEntity().clone();
				foundSpinnerRewardEntity.setGenerated(generated);
			}
		}
		return foundSpinnerRewardEntity;

	}

	private boolean checkIfGeneratedValueFitInInterval(RewardInterval interval, double generatedValue) {
		if (interval == null) {
			logger.info("List is null.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Check if generated value fir in intervals failed - interval null");
			return false;
		}

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"check if generated value fit in interval" + interval + " generated:" + generatedValue);
		boolean fitFrom = false;
		boolean fitTo = false;
		BigDecimal generatedValueAsBigDecimal = BigDecimal.valueOf(generatedValue);
		int resultFrom = interval.getFromValue().compareTo(generatedValueAsBigDecimal);
		int resultTo = interval.getToValue().compareTo(generatedValueAsBigDecimal);

		fitFrom = checkFitFrom(interval, fitFrom, resultFrom);
		fitTo = checkFitTo(interval, fitTo, resultTo);

		return fitFrom && fitTo;

	}

	private boolean checkFitTo(RewardInterval interval, boolean fitTo, int resultTo) {
		if (interval != null && interval.isToClosed()) {
			// a <= b
			if (resultTo == 0 || resultTo == 1) {
				fitTo = true;
			}
		} else {

			// a < b
			if (resultTo == 1) {
				fitTo = true;
			}
		}
		return fitTo;
	}

	private boolean checkFitFrom(RewardInterval interval, boolean fitFrom, int resultFrom) {
		if (interval != null && interval.isFromClosed()) {
			// a => b
			if (resultFrom == 0 || resultFrom == -1) {
				fitFrom = true;
			}

		} else {

			// a >b
			if (resultFrom == -1) {
				fitFrom = true;
			}
		}
		return fitFrom;
	}

	public List<SpinnerRewardEntity> prepareDataForGenerator(List<SpinnerRewardEntity> list) {

		if (list == null) {
			logger.info("List is null.");
			Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.ERROR,
					"Prepare adta for generator failed - list null");
			return null;
		}

		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Preapring data for generator: " + list);

		List<RewardInterval> rewardIntervalList = prepareSpinnerRewardIntervals(list);
		for (RewardInterval interval : rewardIntervalList) {
			String charFrom = "(";
			if (interval.isFromClosed())
				charFrom = "<";
			String charTo = ")";
			if (interval.isToClosed())
				charTo = ">";

			logger.info("Reward:" + interval.getSpinnerRewardEntity().getRewardName() + " probability: "
					+ interval.getSpinnerRewardEntity().getRewardProbability() + " interval: " + charFrom
					+ interval.getFromValue() + ";" + interval.getToValue() + charTo);
		}

		List<SpinnerRewardEntity> preparedList = new ArrayList<SpinnerRewardEntity>();

		return preparedList;
	}

}
