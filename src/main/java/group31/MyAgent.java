package group31;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.Timeline;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import group31.oldVersions.OpponentModelling_JB;
import org.chocosolver.solver.Model;


/**
 * A simple example agent that makes random bids above a minimum target utility.
 *
 * @author Alex Newton
 */
public class MyAgent extends AbstractNegotiationParty {
	private static double MINIMUM_TARGET = 0.8;
	private Bid lastOffer;
	private AdditiveUtilitySpace estimatedUtilitySpace;
	private Timeline timeLine = (Timeline) getTimeLine();
	private BidGenerator bidGenerator;
	private OpponentModelling_JB opponentModel;
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
		// Setup essential variables

		estimatedUtilitySpace = (AdditiveUtilitySpace) estimateUtilitySpace();
		// Get estimated utility space until Kai has finished his code

		this.bidGenerator = new BidGenerator(estimatedUtilitySpace, domain, timeLine);
		this.allBids = bidGenerator.getAllPossibleBids();
		// get all the possible bids in the domain

		this.opponentModel = new OpponentModelling_JB(estimatedUtilitySpace);

	}

	/**
	 * Makes a random offer above the minimum utility target
	 * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {

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

	/**-
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		if (action instanceof Offer)
		{
			List<Issue> issues;
			lastOffer = ((Offer) action).getBid();
			issues = lastOffer.getIssues();
			for(Issue issue : issues){
				Value value = lastOffer.getValue(issue);
				opponentModel.updateFrequency(issue, (ValueDiscrete) value);
			}
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
}
