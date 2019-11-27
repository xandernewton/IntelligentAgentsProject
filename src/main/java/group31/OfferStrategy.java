package main.java.group31;

import agents.anac.y2010.Yushu.Utility;
import com.google.common.collect.Sets;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;
import org.javatuples.Pair;


import java.util.*;

class OfferStrategy {

    protected AdditiveUtilitySpace utilitySpace;
    private TreeMap<Double, Bid> allPossibleBids = new TreeMap<Double, Bid>();
    private HashMap<Integer,Integer> mapIssue =  new  HashMap<Integer, Integer>();
    private HashMap<Integer, Issue> issueMap = new HashMap<Integer, Issue>();
    private List<Set<Pair<Integer,ValueDiscrete>>> interimList = new ArrayList<Set<Pair<Integer, ValueDiscrete>>>();
    private Domain domain;

    OfferStrategy(AdditiveUtilitySpace utilitySpace, Domain domain) {

        this.utilitySpace = utilitySpace;
        this.domain = domain;
    }

    void computeAllPossibleBids(){


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
        System.out.println("test");

        return newBid;
    }


}
