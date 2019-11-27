package main.java.group31;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.UtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import main.java.group31.OfferStrategy;
import org.chocosolver.solver.Model;

/**
 * A simple example agent that makes random bids above a minimum target utility. 
 *
 * @author Tim Baarslag
 */
public class MyAgent extends AbstractNegotiationParty
{
	private static double MINIMUM_TARGET = 0.8;
	private Bid lastOffer;
	private AdditiveUtilitySpace estimatedUtilitySpace;


	/**
	 * Initializes a new instance of the agent.
	 */
	@Override
	public void init(NegotiationInfo info) 
	{
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
		// Setup essential variables

		estimatedUtilitySpace = (AdditiveUtilitySpace) estimateUtilitySpace();
		OfferStrategy acceptOfferStrategy = new OfferStrategy(estimatedUtilitySpace, domain);
		// Get estimated utility space until Kai has finished his code

		acceptOfferStrategy.computeAllPossibleBids();
		// Create new acceptOfferStrategy object and compute all the possible bids




	}


	/**
	 * Makes a random offer above the minimum utility target
	 * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise. 
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) 
	{
		// Check for acceptance if we have received an offer
		if (lastOffer != null)
			if (timeline.getTime() >= 0.99)
				if (getUtility(lastOffer) >= utilitySpace.getReservationValue()) 
					return new Accept(getPartyId(), lastOffer);
				else
					return new EndNegotiation(getPartyId());
		
		// Otherwise, send out a random offer above the target utility 
		return new Offer(getPartyId(), generateRandomBidAboveTarget());
	}

	private Bid generateRandomBidAboveTarget() 
	{
		Bid randomBid;
		double util;
		int i = 0;
		// try 100 times to find a bid under the target utility
		do 
		{
			randomBid = generateRandomBid();
			util = utilitySpace.getUtility(randomBid);
		} 
		while (util < MINIMUM_TARGET && i++ < 100);		
		return randomBid;
	}

	/**
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) 
	{
		if (action instanceof Offer) 
		{
			lastOffer = ((Offer) action).getBid();
		}
	}

	@Override
	public String getDescription() 
	{
		return "I am the greatest negotiation agent" + MINIMUM_TARGET;
	}

	/**
	 * This stub can be expanded to deal with preference uncertainty in a more sophisticated way than the default behavior.
	 */
	@Override
	public AbstractUtilitySpace estimateUtilitySpace() 
	{
		return super.estimateUtilitySpace();
	}




}
