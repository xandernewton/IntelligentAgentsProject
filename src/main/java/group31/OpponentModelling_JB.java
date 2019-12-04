package main.java.group31;

import genius.core.Bid;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import org.javatuples.Pair;

import java.util.*;


public class OpponentModelling_JB {


    public Map.Entry<Double, Bid> opponentBestEntry;
    public Bid opponentLastBid;
    public Integer numberOfBids = 0;
    private AdditiveUtilitySpace utilitySpace;
    private Integer[][] frequency;
    private Double[][] orderValues;
    private HashMap<Integer, Integer> mapIssue = new HashMap<Integer, Integer>();
    private HashMap<String, Integer> mapOptions = new HashMap<>();
    private List<Issue> issues;
    //private ArrayList<Pair<Integer,Double>> issueAndWeights;
    private HashMap<Integer, Double> issueAndWeights = new HashMap<>();
    // An array of Pairs, item 0 contains the issue id, item 1 contains the weight
    private HashMap<Integer, Issue> intToIssues = new HashMap<>();

    public OpponentModelling_JB(AdditiveUtilitySpace utilitySpace) {

        this.utilitySpace = utilitySpace;
        issues = utilitySpace.getDomain().getIssues();
        setupFrequencyTable();
        //issueAndWeights = new ArrayList<>(Collections.nCopies(issues.size(), new Pair<>(0,0.0D)));
        for (Issue issue : issues) {
            intToIssues.put(issue.getNumber(), issue);
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
            optionValue = orderValues[currentIndexes.getValue0()][currentIndexes.getValue1()];
            utility = utility + currentWeight * optionValue;

        }

        System.out.println(String.format("Current Opponent Utility is: %f",utility));
        return utility;
    }

    public void updateFrequency(Issue issue, ValueDiscrete valueDiscrete) {

        Pair<Integer, Integer> indexes = getIndex(issue, valueDiscrete);
        Integer issueIndex = indexes.getValue0();
        Integer optionIndex = indexes.getValue1();
        frequency[issueIndex][optionIndex] = frequency[issueIndex][optionIndex] + 1;

    }

    private Pair<Integer, Integer> getIndex(Issue issue, ValueDiscrete value) {
        Integer issueid = issue.getNumber();
        Integer issueIndex = mapIssue.get(issueid);
        String option_name = value.toString();
        Integer optionIndex = mapOptions.get(option_name + String.valueOf(issueIndex));
        return new Pair<>(issueIndex, optionIndex);
    }

    private void setupFrequencyTable() {

        int option_counter;
        int issue_counter = 0;
        frequency = new Integer[issues.size()][];
        orderValues = new Double[issues.size()][];

        for (Issue issue : issues) {

            option_counter = 0;
            mapIssue.put(issue.getNumber(), issue_counter);
            issue_counter++;

            int issueNumber = issue.getNumber();
            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            // Add array to the issue list which is the length of the number of options

            frequency[mapIssue.get(issue.getNumber())] = new Integer[issueDiscrete.getValues().size()];
            orderValues[mapIssue.get(issue.getNumber())] = new Double[issueDiscrete.getValues().size()];

            // Initialise all entries in the list to zero

            Arrays.fill(frequency[mapIssue.get(issue.getNumber())], 0);
            Arrays.fill(orderValues[mapIssue.get(issue.getNumber())], 0.0D);

            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                String option_input = valueDiscrete.toString() + String.valueOf(issue.getNumber() - 1);
                mapOptions.put(option_input, option_counter);
                option_counter++;
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
            sumOfWeights = sumOfWeights + entry.getValue();
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

        ArrayList sumArray = new ArrayList();
        Integer issueIndex = mapIssue.get(issue.getNumber());
        int numberOfOptions = frequency[issueIndex].length;
        double totalWeight = 0;
        for (int x = 0; x < numberOfOptions; x++) {
            totalWeight = totalWeight + (Math.pow(frequency[issueIndex][x], 2.00D) / numberOfBids);
        }

        return totalWeight;
    }

    private void calculateValues() {

        for (Issue issue : issues) {
            calculateValuesForIssue((IssueDiscrete) issue);
        }
    }

    private void calculateValuesForIssue(IssueDiscrete issue) {

        double V;
        ArrayList<Pair<Integer, Integer>> rankings = getRanking(issue);
        Pair<Integer, Integer> optionRank;
        for (int x = 0; x < rankings.size(); x++) {
            optionRank = rankings.get(x);
            V = (rankings.size() - (x + 1) + 1.00D) / rankings.size();
            orderValues[mapIssue.get(issue.getNumber())][optionRank.getValue0()] = V;

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
        ordering.sort((a, b) -> {
            if (a.getValue1() < b.getValue1())
                return 1;
            if (a.getValue1() > b.getValue1())
                return -1;
            return 0;
        });
        // sort in reverse order
        return ordering;


    }

    public void updateOpponentModel() {
        calculateValues();
        calculateWeights();
    }

}
