package is.ejb.bl.testing;

public class TestCarrierPayouts {

	public static void main(String[] args) {
		new TestCarrierPayouts();
	}
	
	public TestCarrierPayouts() {
		calculateRewardPayoutBasedOnCarrier(5);
		calculateRewardPayoutBasedOnCarrier(10);
		calculateRewardPayoutBasedOnCarrier(11);
		calculateRewardPayoutBasedOnCarrier(15);
		calculateRewardPayoutBasedOnCarrier(19);
		calculateRewardPayoutBasedOnCarrier(21);
		calculateRewardPayoutBasedOnCarrier(35);
		calculateRewardPayoutBasedOnCarrier(45);
		calculateRewardPayoutBasedOnCarrier(49);
		calculateRewardPayoutBasedOnCarrier(149);
	}
	
	public double[] calculateRewardPayoutBasedOnCarrier(double rewardValue)
	{
		double[] rewardPayout = new double[2];
		rewardPayout[0] = 0;
		rewardPayout[1] = 0;
		
		
		//we calculate payouts only from 10
		if (rewardValue >= 10)
		{
			double baseValue = Math.floor(rewardValue / 10);
			double restValue = rewardValue - baseValue*10;
			rewardPayout[0] = baseValue*10;
			rewardPayout[1] = restValue;
		}
		
		System.out.println("----------------------");
		System.out.println("Payout: "+rewardPayout[0]);
		System.out.println("Wallet: "+rewardPayout[1]);
		
		return rewardPayout;
	}
}
