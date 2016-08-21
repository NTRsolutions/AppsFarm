package is.ejb.bl.spinner;

import is.ejb.bl.business.Application;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.SpinnerRewardEntity;

import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class UniformDistributionSpinnerAlgorithm extends AbstractSpinnerAlgorithm {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static volatile long seedUniquifier = 8682522807148012L;
	private Random generator;
	
	
	@PostConstruct
	public void init(){
		generator = new Random(++seedUniquifier + System.nanoTime());
	}
	

	public SpinnerRewardEntity generateReward(List<SpinnerRewardEntity> list) {
		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Generating reward for list:" + list);
		log.info("Generating reward.");

		list = this.validateSpinnerRewardList(list);
		this.printSpinnerRewardsInformation(list);
		List<RewardInterval> rewardIntervalList = this.prepareSpinnerRewardIntervals(list);

		return generate(rewardIntervalList);
	}

	private SpinnerRewardEntity generate(List<RewardInterval> rewardIntervalList) {
		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Generating reward for spinner interval list: " + rewardIntervalList);
		log.info("Generate:" + rewardIntervalList);

		Double generatedValue = 0.0;
		generatedValue = generateRandomValue();
		SpinnerRewardEntity reward = this.findSpinnerRewardForGeneratedValue(rewardIntervalList, generatedValue);

		return reward;
	}

	private Double generateRandomValue() {
		Double generatedValue;
		
		do {
			generatedValue = generator.nextDouble();
		} while (generatedValue > 1);
		
		log.info("Generated value:" + generatedValue);
		Application.getElasticSearchLogger().indexLog(Application.SPINNER_GENERATOR_ACTIVITY, -1, LogStatus.OK,
				"Generated value: " + generatedValue);
		
		return generatedValue;
	}

}
