package group31;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.Timeline;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.ExperimentalUserModel;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UncertainAdditiveUtilitySpace;
import group31.oldVersions.*;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.chocosolver.solver.Model;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;


/**
 * A simple example agent that makes random bids above a minimum target utility.
 *
 * @author Alex Newton
 */
public class Agent31 extends AbstractNegotiationParty {
	private static double MINIMUM_TARGET = 0.8;
	private Bid lastOffer;
	private AdditiveUtilitySpace estimatedUtilitySpace;
	private Timeline timeLine = (Timeline) getTimeLine();
	private BidGenerator bidGenerator;
	//private OpponentModelling_JB opponentModel;
	//private OpponentModelling_JB_2 opponentModel;
	//private OpponentModelling_JB_a opponentModel;
	//private OpponentModelling_JB_a_2 opponentModel;
	private OpponentModelling_JB_test opponentModel;
	private PreferenceElicititation preferenceElicititation;
	private Double discountFactor;
	private Integer round = 0;
	private ArrayList<Map.Entry<Double, Bid>> mybidHistory = new ArrayList<Map.Entry<Double, Bid>>();
	private Boolean firstRound;
	private TreeMap<Double, Bid> allBids = new TreeMap<Double, Bid>();
	private LinkedList<Map.Entry<Double, Bid>> offerQueue = new LinkedList<Map.Entry<Double, Bid>>();
	private Double lowestUtility = Double.POSITIVE_INFINITY;
	private MovingAverage movingAverage = new MovingAverage(10);
	private ArrayList<Double> movingAverages = new ArrayList<>();
	private Boolean detected = false;
	private boolean isBoulware = false;
	private List<Double> UtilityList = new ArrayList<>();
	private UncertainAdditiveUtilitySpace realUSpace;

	private static int getRandomNumberInRange(int min, int max) {

		if (min == max) {
			return min;
		}

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	/**
	 * Initializes a new instance of the agent.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		// Just to check that Preference Uncertainty is enabled


		Model model = new Model("my first problem");

		// This is for you KAI ^^^^^^ just to test that loading the constraint stuff work


		BidRanking bidRanking = userModel.getBidRanking();
		Domain domain = this.userModel.getDomain();
		discountFactor = utilitySpace.getDiscountFactor();
		timeLine = (Timeline) getTimeLine();
		firstRound = true;
		// Setup essential variables

//		if (bidRanking.getSize() <= 500){
//			preferenceElicititation = new PreferenceElicititation(userModel,info);
//			estimatedUtilitySpace = (AdditiveUtilitySpace) preferenceElicititation.generateNewEstimate();
//		}
//		else{
//			estimatedUtilitySpace = (AdditiveUtilitySpace) estimateUtilitySpace();
//
//		}
		estimatedUtilitySpace = (AdditiveUtilitySpace) estimateUtilitySpace();
		ExperimentalUserModel e = ( ExperimentalUserModel ) userModel ;
		realUSpace = e. getRealUtilitySpace();
		// Get estimated utility space until Kai has finished his code


		this.bidGenerator = new BidGenerator(estimatedUtilitySpace, domain, timeLine);
		this.allBids = bidGenerator.getAllPossibleBids();
		// get all the possible bids in the domain

		//opponentModel = new OpponentModelling_JB(estimatedUtilitySpace);
		opponentModel = new OpponentModelling_JB_test(estimatedUtilitySpace);
		//opponentModel = new OpponentModelling_JB_2(estimatedUtilitySpace);
		//opponentModel = new OpponentModelling_JB_a(estimatedUtilitySpace);
		//opponentModel = new OpponentModelling_JB_a_2(estimatedUtilitySpace);
		//ignore this line, its for testing

	}

	/**
	 * Makes a random offer above the minimum utility target
	 * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		//System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out))); // To be removed
		Double utilityThreshold = bidGenerator.CalculateUtilityToOffer();
		boolean difference = false;
		Map.Entry<Double, Bid> replacementBidEntry;
		Map.Entry<Double, Bid> comparisonHighestBid;
		ArrayList<Map.Entry<Double, Bid>> sortingArray = new ArrayList<>();
		Double opponentLastBidUtility;
		Object actionTaken = null;
		Integer modNumber = 25;
		Double average = 0.0;
		Double stantardDeviation = 0.0;
		Double errorPenalty = 0.2;
		//Double errorPenalty_2 = 0.1;
		Double opponentError =  ((double) allBids.size() / 25000) * errorPenalty;
		Double myError =  ((double) allBids.size() / 25000) * errorPenalty;



		round++;
		// update the number of rounds
		if (opponentModel.opponentBidHistory.size() > 0) {
			Double currentOpponentUtility = opponentModel.getOpponentUtility(opponentModel.opponentLastBid.getValue());
			movingAverage.add(currentOpponentUtility);
		}
		// add values to moving average

		if (round % modNumber == 0) {
			average = movingAverage.getAverage();
			movingAverages.add(average);
		}
		// every 10 rounds add average to a list

		if(timeLine.getTime() > 0.90){
			if (!detected) {
				isBoulware = detectTypeOfAgent();
				//System.out.println(String.format("Boulware agent: %b",isBoulware));
				detected = true;
			}
		}


		if (timeLine.getTime() > 0.7) {
			opponentLastBidUtility = opponentModel.getOpponentUtility(opponentModel.opponentLastBid.getValue());
			average = movingAverage.getAverage();
			StandardDeviation sd = new StandardDeviation(false);
			stantardDeviation = sd.evaluate(movingAverage.getSamples());
			Double OutlierValue = average - 2 * stantardDeviation;
			opponentLastBidUtility = opponentModel.getOpponentUtility(opponentModel.opponentLastBid.getValue());
			//Double realUtil = realUSpace.getUtility(opponentModel.opponentLastBid.getValue());
			System.out.println(String.format("%f", opponentLastBidUtility));
			//System.out.println(String.format("average is %f and outlier value is %f opponents utility is %f", average, OutlierValue, opponentLastBidUtility))
			if (isBoulware) {

				/*if(timeLine.getTime() > 0.99 && opponentLastBidUtility - opponentError <= average - 3.5 * stantardDeviation && this.utilitySpace.getUtility(opponentModel.opponentLastBid.getValue()) + errorPenalty >= bidGenerator.minUtility){
					return new Accept(getPartyId(), opponentModel.opponentLastBid.getValue());
				}*/

				if (opponentLastBidUtility - opponentError  <= average - 2 * stantardDeviation && this.utilitySpace.getUtility(opponentModel.opponentLastBid.getValue())  >= bidGenerator.minUtility + opponentError) {
					return new Accept(getPartyId(), opponentModel.opponentLastBid.getValue());
				}
			}
			/*if(timeLine.getTime() > 0.99){
				if (opponentLastBidUtility - opponentError  <= average - 2 * stantardDeviation && this.utilitySpace.getUtility(opponentModel.opponentLastBid.getValue())  >= bidGenerator.minUtility + opponentError) {
					return new Accept(getPartyId(), opponentModel.opponentLastBid.getValue());
				}
			}*/
		}

		if (firstRound) {
			//System.out.println("First round");
			//System.out.println(String.format("current time is %f",timeLine.getTime()));
			Map.Entry<Double, Bid> highestValuedBidEntry = allBids.lastEntry();
			offerQueue.add(highestValuedBidEntry);
			this.firstRound = false;
			// on first round, offer our best bid
			if (opponentModel.opponentLastBid == null) {
				replacementBidEntry = offerQueue.removeFirst();
				actionTaken = new Offer(getPartyId(), replacementBidEntry.getValue());
				mybidHistory.add(replacementBidEntry);

			}

		}

		else if (offerQueue.isEmpty()) {
			TreeMap<Double, Bid> potentialBids = new TreeMap<Double, Bid>();
			Map.Entry<Double, Bid> nextHighestBid = allBids.lowerEntry(this.mybidHistory.get(this.mybidHistory.size() - 1).getKey());
			Double lastBidUtil = (Double) nextHighestBid.getKey();
			potentialBids.put(nextHighestBid.getKey(), nextHighestBid.getValue());
			if (lastBidUtil < utilityThreshold) {
				int bidIndex = getRandomNumberInRange(1, mybidHistory.size());
				replacementBidEntry = mybidHistory.get(bidIndex - 1);
				// Choose random previously made bid
				potentialBids.remove(replacementBidEntry.getKey());
				potentialBids.put(replacementBidEntry.getKey(), replacementBidEntry.getValue());
				// remove the old bid and replace with the previously used bid

			}

			while (!difference) {
				comparisonHighestBid = allBids.lowerEntry(nextHighestBid.getKey());
				if (comparisonHighestBid.getKey() > utilityThreshold) {
					potentialBids.put(comparisonHighestBid.getKey(), comparisonHighestBid.getValue());
				}
				if (comparisonHighestBid.getKey() - nextHighestBid.getKey() < 0.01D)
					difference = true;
				// if there is a minimal difference between the current and last bid, stop

				nextHighestBid = comparisonHighestBid;

			}

		/*	if (potentialBids.size() < 2) {
				offerQueue.addAll(potentialBids.entrySet());
			} else {*/
				sortingArray.addAll(potentialBids.entrySet());
				sortingArray.sort((bid1, bid2) -> {
					if (opponentModel.getOpponentUtility(bid1.getValue()) < opponentModel.getOpponentUtility(bid2.getValue()))
						return 1;
					if (opponentModel.getOpponentUtility(bid1.getValue()) > opponentModel.getOpponentUtility(bid2.getValue()))
						return -1;
					return 0;
				});
				// sort the potential bids by the utility the opponent would get from the bid
				for (int x = 0; x < sortingArray.size(); x++) {
					Double y = opponentModel.getOpponentUtility(sortingArray.get(x).getValue());
					offerQueue.add(sortingArray.get(x));
				}
				// add the top 4 bids to the offer queue
				if (opponentModel.opponentBestEntry.getKey() > offerQueue.getFirst().getKey()) {
					offerQueue.add(new AbstractMap.SimpleEntry<Double, Bid>(opponentModel.opponentBestEntry.getKey(),
							opponentModel.opponentBestEntry.getValue()));
			//	}
			}

		}

		if (opponentModel.opponentLastBid != null) {
			//System.out.println(String.format("My utility of last bid is %f", utilitySpace.getUtility(opponentModel.opponentLastBid.getValue())));
			opponentLastBidUtility = opponentModel.opponentLastBid.getKey()  ; //this.utilitySpace.getUtility(opponentModel.opponentLastBid.getValue());
			if (opponentLastBidUtility > lowestUtility -0.175  ||
					opponentLastBidUtility >= this.utilitySpace.getUtility(offerQueue.getFirst().getValue())) {
				actionTaken = new Accept(getPartyId(), opponentModel.opponentLastBid.getValue());

				// If opponents last bid is greater than best bid on offer queue - accept
				// If opponents last bid is greater than best lowest seen bid - accept
			} else {
				Double oppLastBidUtil = opponentModel.getOpponentUtility(opponentModel.opponentLastBid.getValue()) - opponentError;
				//System.out.println(String.format("lowest utility is %f and my last bid util is %f opp last bid util %f",lowestUtility,opponentLastBidUtility,oppLastBidUtil));
				Map.Entry<Double, Bid> removedBid = offerQueue.remove(0);
				mybidHistory.add(removedBid);
				if (removedBid.getKey() < lowestUtility) {
					lowestUtility = removedBid.getKey();
					//System.out.println(String.format("lowest utility is %f ",lowestUtility));
					// update lowest bid
				}
				actionTaken = new Offer(getPartyId(), removedBid.getValue());
				//
			}
		}

		return (Action) actionTaken;
	}

	/**
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		if (action instanceof Offer) {
			List<Issue> issues;
			lastOffer = ((Offer) action).getBid();
			if(opponentModel.opponentLastBid != null) {
				this.UtilityList.add(opponentModel.getOpponentUtility(lastOffer));
			}

			issues = lastOffer.getIssues();
			for (Issue issue : issues) {
				Value value = lastOffer.getValue(issue);
				opponentModel.updateFrequency(issue, (ValueDiscrete) value);

			}
			//opponentModel.updateWeights(lastOffer)
			opponentModel.updateOpponentModel(lastOffer);
			opponentModel.getOpponentUtility(lastOffer);


		}
	}

	@Override
	public String getDescription() {
		return "That's Mushy Snugglebites' badonkadonk. She's my main squeeze. Lady's got a gut fulla' dynamite and a booty like POOOW!";
	}

	/**
	 * This stub can be expanded to deal with preference uncertainty in a more sophisticated way than the default behavior.
	 */
	@Override
	public AbstractUtilitySpace estimateUtilitySpace() {
		return super.estimateUtilitySpace();
	}

	private boolean detectTypeOfAgent() {
		if (movingAverages.size() > 10) {
			Double min = Collections.min(movingAverages);
			Double max = Collections.max(movingAverages);
			Double range = max - min;
			if (range < 0.9) {
				return true;
				// they are a bouleware agent
			}
		}
		return false;
	}

}
