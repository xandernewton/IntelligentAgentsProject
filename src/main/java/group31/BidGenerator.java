package main.java.group31;

import com.google.common.collect.Sets;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.timeline.Timeline;
import genius.core.utility.AdditiveUtilitySpace;
import org.javatuples.Pair;


import java.util.*;

class BidGenerator {

    private AdditiveUtilitySpace utilitySpace;
    private TreeMap<Double, Bid> allPossibleBids = new TreeMap<Double, Bid>();
    private HashMap<Integer,Integer> mapIssue =  new  HashMap<Integer, Integer>();
    private HashMap<Integer, Issue> issueMap = new HashMap<Integer, Issue>();
    private List<Set<Pair<Integer,ValueDiscrete>>> interimList = new ArrayList<Set<Pair<Integer, ValueDiscrete>>>();
    private Domain domain;
    private Timeline timeLine;
    private Double initialConcession = 0.05D; // this needs testing for the optimal value
    private Double minUtility = 0.58D; // from the paper
    private Double maxUtility;
    private Double discountFactor;
    private Double concessionRateCoefficient = 0.02D;

    public TreeMap<Double, Bid> getAllPossibleBids() {
        return allPossibleBids;
    }



    BidGenerator(AdditiveUtilitySpace utilitySpace, Domain domain, Timeline timeLine) {

        this.utilitySpace = utilitySpace;
        this.domain = domain;
        this.timeLine = timeLine;
        this.discountFactor = utilitySpace.getDiscountFactor();

        try {
            Bid MaxUtilityBid = utilitySpace.getMaxUtilityBid();
            maxUtility =  utilitySpace.getUtility(MaxUtilityBid);
        }
        catch (Exception x) {
            x.printStackTrace();
        }

        computeAllPossibleBids();        // Create new acceptOfferStrategy object and compute all the possible bids
    }

    private void computeAllPossibleBids(){


        // A list containing sets of values for each issue
        List<Issue> issues = this.utilitySpace.getDomain().getIssues();
        for(Issue issue: issues){
            issueMap.put(issue.getNumber(),issue); // Add issues to a hashmap for quick access
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            Set<Pair<Integer,ValueDiscrete>> interimSet   = new HashSet<Pair<Integer, ValueDiscrete>>();
            for(ValueDiscrete value : ((IssueDiscrete) issue).getValues()){
                interimSet.add(new Pair(issue.getNumber(),value));
            }
            interimList.add(interimSet);
        }

        Set<List<Pair<Integer, ValueDiscrete>>> tuples = Sets.cartesianProduct(interimList);
        // Get all possible combinations of bids

        for (List<Pair<Integer,ValueDiscrete>> valueList : tuples) {
            Bid bidToAddToTree = covertToBid(valueList);
            //Bid randomBid = domain.getRandomBid(new Random());
            Double utilityOfBid = this.utilitySpace.getUtility(bidToAddToTree);
            this.allPossibleBids.put(utilityOfBid, bidToAddToTree);
        }
        System.out.println("test");

    }

    private Bid covertToBid(List<Pair<Integer,ValueDiscrete>> values){

        HashMap<Integer, Value> valueMap = new HashMap<Integer, Value>();
        // Bids are created by having a hashMap of integers to ValuesDiscrete

        for(Pair<Integer,ValueDiscrete> currentValue : values) {
            valueMap.put(currentValue.getValue0(), currentValue.getValue1());
        }

        Bid newBid = new Bid(this.domain,valueMap);
        //System.out.println("test");

        return newBid;
    }


    public Bid getBid(){

        Double currentUtility = this.CalculateUtilityToOffer();
        System.out.println(currentUtility);
        Map.Entry utilityOfLowestBid = allPossibleBids.lowerEntry(currentUtility);
        // Finds mapping pair with the greatest key strictly less than the current utility
        Bid bidToOffer = (Bid) utilityOfLowestBid.getValue();
        return bidToOffer;

    }


    private Double concessionFunction(Double time){

        /**
         * Need to add code to change the concessionRateCoefficient
         */

        Double conecessionLimit =  this.initialConcession + (1- this.initialConcession)*Math.pow(time/discountFactor, 1.0D/ concessionRateCoefficient) ;
        return conecessionLimit;

    }

    private Double CalculateUtilityToOffer(){

        Double currentTime = timeLine.getCurrentTime();
        Double conecessionLimit = concessionFunction(currentTime);
        Double dynamicMinUtility = calculateDynamicMinUtility();
        Double currentUtility = 0.0;
        currentUtility = dynamicMinUtility + (1- conecessionLimit) * (maxUtility - dynamicMinUtility);
        return currentUtility;
    }

    private Double calculateDynamicMinUtility(){

        return  (maxUtility - minUtility) * discountFactor + minUtility;

    }



}