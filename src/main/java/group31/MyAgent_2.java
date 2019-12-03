package main.java.group31;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.Timeline;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
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
public class MyAgent_2 extends AbstractNegotiationParty {
	private static double MINIMUM_TARGET = 0.8;
	private Bid lastOffer;
	private AdditiveUtilitySpace estimatedUtilitySpace;
	private Timeline timeLine = (Timeline) getTimeLine();
	private BidGenerator bidGenerator;
	private OpponentModelling opponentModel;
	private Double discountFactor;
	private Integer round;
	private ArrayList<Map.Entry<Double, Bid>> mybidHistory = new ArrayList<Map.Entry<Double, Bid>>();
	private Boolean firstRound;
	private TreeMap<Double, Bid> allBids = new TreeMap<Double, Bid>();
	private LinkedList<Map.Entry<Double,Bid>> offerQueue = new LinkedList<Map.Entry<Double, Bid>>();
	private Double lowestUtility = Double.POSITIVE_INFINITY;

	private static int getRandomNumberInRange(int min, int max) {
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
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		if (hasPreferenceUncertainty()) {
			System.out.println("Preference uncertainty is enabled.");
		}
		// Just to check that Preference Uncertainty is enabled


		Model model = new Model("my first problem");

		// This is for you KAI ^^^^^^ just to test that loading the constraint stuff work


		BidRanking bidRanking = userModel.getBidRanking();
		Domain domain = this.userModel.getDomain();
		discountFactor = utilitySpace.getDiscountFactor();
		timeLine = (Timeline) getTimeLine();
		firstRound = true;
		opponentModel = new OpponentModelling();
		// Setup essential variables

		estimatedUtilitySpace = (AdditiveUtilitySpace) estimateUtilitySpace();
		// Get estimated utility space until Kai has finished his code

		this.bidGenerator = new BidGenerator(estimatedUtilitySpace, domain, timeLine);
		this.allBids = bidGenerator.getAllPossibleBids();
		// get all the possible bids in the domain

	}

	/**
	 * Makes a random offer above the minimum utility target
	 * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {

		Double utilityThreshold = bidGenerator.CalculateUtilityToOffer();
		boolean difference = false;
		Map.Entry<Double, Bid> replacementBidEntry;
		Map.Entry<Double, Bid>  comparisonHighestBid;
		ArrayList<Map.Entry<Double,Bid>> sortingArray = new ArrayList<>();
		Double opponentLastBidUtility;
		// calculate the current

		if (firstRound) {
			Map.Entry<Double, Bid> highestValuedBidEntry = allBids.lastEntry();
			offerQueue.add(highestValuedBidEntry);
			this.firstRound = false;
		} else if (offerQueue.isEmpty()) {
			TreeMap<Double, Bid> potentialBids = new TreeMap<Double, Bid>();
			Map.Entry<Double, Bid> nextHighestBid =  allBids.lowerEntry((Double) mybidHistory.get(mybidHistory.size() - 1).getKey());
			Double lastBidUtil = (Double) nextHighestBid.getKey();
			potentialBids.put(nextHighestBid.getKey(), nextHighestBid.getValue());
			if (lastBidUtil < utilityThreshold) {
				int bidIndex = getRandomNumberInRange(1, mybidHistory.size());
				replacementBidEntry = mybidHistory.get(bidIndex);
				// Choose random previously made bid
				potentialBids.remove(replacementBidEntry.getKey());
				potentialBids.put(replacementBidEntry.getKey(), replacementBidEntry.getValue());
				// remove the old bid and replace with the previously used bid
			}

			while(!difference){
				comparisonHighestBid = allBids.lowerEntry((Double) nextHighestBid.getKey());
				if((Double) comparisonHighestBid.getKey() > utilityThreshold){
					potentialBids.put(comparisonHighestBid.getKey(), comparisonHighestBid.getValue());
				}
				if((Double) comparisonHighestBid.getKey() - (Double) nextHighestBid.getKey() < 0.01D)
					difference = true;
					// if there is a minimal difference between the current and last bid, stop

				nextHighestBid = comparisonHighestBid;

			}

			if(potentialBids.size() < 4){
				offerQueue.addAll(potentialBids.entrySet());
			}
			else{
				sortingArray.addAll(potentialBids.entrySet());
				sortingArray.sort((bid1, bid2) -> {
					if (opponentModel.getOpponentUtility(bid1.getValue()) < opponentModel.getOpponentUtility(bid2.getValue()))
						return -1;
					if (opponentModel.getOpponentUtility(bid1.getValue()) > opponentModel.getOpponentUtility(bid2.getValue()))
						return 1;
					return 0;
				});
				// sort the potential bids by the utility the opponent would get from the bid
				for(int x= 0; x<4; x++){
					offerQueue.add(sortingArray.get(x));
				}
				// add the top 4 bids to the offer queue
				if(opponentModel.opponentBestEntry.getKey() > offerQueue.getFirst().getKey()){
					offerQueue.add(opponentModel.opponentBestEntry);
				}


			}

		}

		opponentLastBidUtility = this.utilitySpace.getUtility(opponentModel.opponentLastBid);
		if(opponentLastBidUtility > lowestUtility ||
				opponentLastBidUtility >= this.utilitySpace.getUtility(offerQueue.getFirst().getValue())){
			return new Accept(getPartyId(), opponentModel.opponentLastBid);
		}
		else{
			Map.Entry<Double, Bid> removedBid = offerQueue.remove(0);
			mybidHistory.add(removedBid);
			if(removedBid.getKey() < lowestUtility){
				lowestUtility = removedBid.getKey();
			}
		}

		// Check for acceptance if we have received an offer
		if (lastOffer != null)
			if (timeline.getTime() >= 0.99)
				if (getUtility(lastOffer) >= utilitySpace.getReservationValue())
					return new Accept(getPartyId(), lastOffer);
				else
					return new EndNegotiation(getPartyId());

		// Otherwise, get a bid from the offer strategy function

		Bid BidToOffer = bidGenerator.getBid();
		Offer OfferToSend = new Offer(getPartyId(), BidToOffer);
		return OfferToSend;
	}

	/**
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		if (action instanceof Offer) {
			lastOffer = ((Offer) action).getBid();
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
}
