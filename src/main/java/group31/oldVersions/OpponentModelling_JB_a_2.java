package group31.oldVersions;

import genius.core.Bid;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import org.javatuples.Pair;
import java.lang.Math;

import java.util.*;


/**
 * The type Opponent modelling jb.
 */
public class OpponentModelling_JB_a_2 {

    public Double epsilon = 0.2;
    public AbstractMap.SimpleEntry<Double, Bid> opponentBestEntry;
    public ArrayList<AbstractMap.SimpleEntry<Double, Bid>> opponentBidHistory = new ArrayList<AbstractMap.SimpleEntry<Double, Bid>>();
    public AbstractMap.SimpleEntry<Double, Bid> opponentLastBid;//contains the utility and the bid
    public Integer numberOfBids = 0;
    public Integer getNumberOfBids() {
        return numberOfBids;
    }
    public AbstractMap.SimpleEntry<Double, Bid> getOpponentLastBid() {
        return opponentLastBid;
    }

    private AdditiveUtilitySpace utilitySpace;
    private Integer[][] frequency;
    private Double[][] opValues;
    private HashMap<Integer, Integer> mapIssue = new HashMap<Integer, Integer>();
    // maps each issue number to 0,1,2 etc so they can be used to index an array
    private HashMap<String, Integer> mapOptions = new HashMap<>();
    // maps option to a issue id (number)
    private List<Issue> issues;
    private HashMap<Integer, Double> issueAndWeights = new HashMap<>();
    // An array of Pairs, item 0 contains the issue id, item 1 contains the weight
    private HashMap<Integer, Issue> intToIssues = new HashMap<>();



    public OpponentModelling_JB_a_2(AdditiveUtilitySpace utilitySpace) {

        this.utilitySpace = utilitySpace;
        issues = utilitySpace.getDomain().getIssues();
        setupFrequencyTable();
        // create a 2D array containing the frequencies for each option in each issue
        for (Issue issue : issues) {
            intToIssues.put(issue.getNumber(), issue);
        }

    }

    public void updateOpponentBids(Bid opponentLastBid){

        Double utilityOfOpponentsBid = utilitySpace.getUtility(opponentLastBid);
        // our utility of opponents bid
        AbstractMap.SimpleEntry<Double,Bid> currentBid = new AbstractMap.SimpleEntry<Double, Bid>(utilityOfOpponentsBid,opponentLastBid);
        this.opponentLastBid = currentBid;
        this.opponentBidHistory.add(currentBid);
        if (opponentBestEntry == null){
            this.opponentBestEntry  = currentBid;
        }
        else if(opponentBestEntry.getKey() < utilityOfOpponentsBid){
            this.opponentBestEntry  = currentBid;
        }


    }

    public Double getOpponentUtility(Bid bid) {

        HashMap<Integer, Value> values = bid.getValues();
        Pair<Integer, Integer> currentIndexes;
        Double currentWeight;
        Double optionValue;
        Double utility = 0.0;
        for (Map.Entry<Integer, Value> entry : values.entrySet()) {
            currentIndexes = getIndex(intToIssues.get(entry.getKey()), (ValueDiscrete) entry.getValue());
            currentWeight = issueAndWeights.get(currentIndexes.getValue0());
            optionValue = opValues[currentIndexes.getValue0()][currentIndexes.getValue1()];
            utility = utility + currentWeight * optionValue;

        }
        //System.out.println(String.format("Current Opponent Utility is: %f",utility));
        return utility;
        // loops through each issue in the bid
        // and the chosen options for each issue,
        // calculates the utility of the bid by summing up the
        // weight of the issue * the utility of the option
    }


    public void updateFrequency(Issue issue, ValueDiscrete valueDiscrete) {
        Pair<Integer, Integer> indexes = getIndex(issue, valueDiscrete);
        Integer issueIndex = indexes.getValue0();
        Integer optionIndex = indexes.getValue1();
        frequency[issueIndex][optionIndex] = frequency[issueIndex][optionIndex] + 1;
        // increments the value a for the specific issue and its corresponding value
    }

    public void updateIssue(Issue issue, ValueDiscrete valueDiscrete, double Time) {
        epsilon = 0.2;
        Pair<Integer, Integer> indexes = getIndex(issue, valueDiscrete);
        Integer issueIndex = indexes.getValue0();
        Integer optionIndex = indexes.getValue1();

        boolean isnull = false;
        try {
            Bid b = opponentBidHistory.get(opponentBidHistory.size()-1).getValue();
        }
        catch (NullPointerException e){
            System.out.println("your move first");
            isnull=true;
        }
        catch (Exception e){
            isnull=true;
        }
        if (!isnull) {
            Bid bb = opponentBidHistory.get(opponentBidHistory.size()-1).getValue();

            if (valueDiscrete==bb.getValue(issue)){
                issueAndWeights.replace(issueIndex, (issueAndWeights.get(issueIndex) + epsilon));
                Normalise();
            }

            // increments the value a for the specific issue and its corresponding value
        }
    }

    public void Normalise(){
        double total = 0.0;
        //gettotal

        for (int j = 0; j < issueAndWeights.size(); j++) {//for each issue
            total += issueAndWeights.get(j);//add the total
        }
        for (int j = 0; j < issueAndWeights.size(); j++) {//for each issue
            issueAndWeights.replace(j, (issueAndWeights.get(j)/total));//add the total
            System.out.print(intToIssues.get(j+1) + ": "+ issueAndWeights.get(j)+ "|  ");
        }
        System.out.print("\n");
    }


    private Pair<Integer, Integer> getIndex(Issue issue, ValueDiscrete value) {
        Integer issueid = issue.getNumber();
        Integer issueIndex = mapIssue.get(issueid);
        String option_name = value.toString();
        Integer optionIndex = mapOptions.get(option_name + String.valueOf(issueIndex));
        return new Pair<>(issueIndex, optionIndex);

        // return pair of indexes
        // item 0 is the issueIndex
        // item 1 is the value/option index for that issue
        // used to index the frequency/ordervalue array
    }

    private void setupFrequencyTable() {

        int option_counter;
        int issue_counter = 0;
        frequency = new Integer[issues.size()][];
        // the frequency of each option in each value
        opValues = new Double[issues.size()][];
        // the utility of each option for in each issue

        for (Issue issue : issues) {

            option_counter = 0;
            mapIssue.put(issue.getNumber(), issue_counter);
            issue_counter++;

            int issueNumber = issue.getNumber();
            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            // Add array to the issue list which is the length of the number of options

            frequency[mapIssue.get(issue.getNumber())] = new Integer[issueDiscrete.getValues().size()];
            opValues[mapIssue.get(issue.getNumber())] = new Double[issueDiscrete.getValues().size()];
            // add new arrays in the nested array
            // Initialise all entries in the list to zero
            Arrays.fill(frequency[mapIssue.get(issue.getNumber())], 0);
            Arrays.fill(opValues[mapIssue.get(issue.getNumber())], 0.0D);
            // fill with zeros

            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                String option_input = valueDiscrete.toString() + String.valueOf(issue.getNumber() - 1);
                mapOptions.put(option_input, option_counter);
                option_counter++;

                // each value in an issue has the issue number appended to the front of it
                // issue 1 and option cheese would be 1cheese
            }
        }


    }


    private void calculateWeights() {
        numberOfBids++;
        // increment the number of bids we have seen
        Double sumOfWeights = 0.0D;
        Double calculatedWeight;
        int index = 0;
        for (Issue issue : issues) {
            calculatedWeight = calculateWeightForIssue(issue);
            issueAndWeights.put(mapIssue.get(issue.getNumber()), calculatedWeight);
            index++;
        }
        // go over each issue and calculate the un-normalised weight
        for (Map.Entry<Integer, Double> entry : issueAndWeights.entrySet()) {
            sumOfWeights += entry.getValue();
        }
        // calculate the sum of weights
        Double currentWeight;
        for (Issue issue : issues) {
            currentWeight = issueAndWeights.get(mapIssue.get(issue.getNumber()));
            currentWeight = currentWeight / sumOfWeights;
            issueAndWeights.put(mapIssue.get(issue.getNumber()), currentWeight);
        }

        // normalise each of the weights

    }

    private Double calculateWeightForIssue(Issue issue) {

        // gets weights for an issue from the frequency of the options

        ArrayList sumArray = new ArrayList();
        Integer issueIndex = mapIssue.get(issue.getNumber());
        int numberOfOptions = frequency[issueIndex].length;
        double totalWeight = 0;
        for (int x = 0; x < numberOfOptions; x++) {
            totalWeight = totalWeight + (Math.pow(frequency[issueIndex][x], 2.00D) / numberOfBids);
            // calculate the weight according the equation in JB paper
        }

        return totalWeight;
    }

    private void calculateValues() {

        for (Issue issue : issues) {
            calculateValuesForIssue((IssueDiscrete) issue);
        }
        // calculates the utilities for each value/option for each issue
    }

    private void calculateValuesForIssue(IssueDiscrete issue) {

        double V;
        ArrayList<Pair<Integer, Integer>> rankings = getRanking(issue);
        Pair<Integer, Integer> optionRank;
        for (int x = 0; x < rankings.size(); x++) {
            optionRank = rankings.get(x);
            // get the ranking, i.e order options by their frequency
            V = (rankings.size() - (x + 1) + 1.00D) / rankings.size();
            opValues[mapIssue.get(issue.getNumber())][optionRank.getValue0()] = V;
            // calculates utility based on equation from paper
            // adds to the orderValues array
        }
    }


    private ArrayList getRanking(Issue issue) {

        ArrayList<Pair<Integer, Integer>> ordering = new ArrayList<>();
        Integer issueid = issue.getNumber();
        Integer issueIndex = mapIssue.get(issueid);
        int numberOfOptions = frequency[issueIndex].length;
        for (int x = 0; x < numberOfOptions; x++) {
            ordering.add(new Pair<>(x, frequency[issueIndex][x]));
        }
        // create an array on Pairs
        // item 0 is the index of the element in the array
        // item 1 is the frequency

        ordering.sort((a, b) -> {
            if (a.getValue1() < b.getValue1())
                return 1;
            if (a.getValue1() > b.getValue1())
                return -1;
            return 0;
        });
        // sort in reverse order by frequency, i.e largest frequency first
        return ordering;


    }

    public void updateOpponentModel(Bid opponentsLastBid) {
        calculateValues();
        calculateWeights();
        updateOpponentBids(opponentsLastBid);
        // when we get a new opponent bid calculate utilities and weights of issues
    }

}
