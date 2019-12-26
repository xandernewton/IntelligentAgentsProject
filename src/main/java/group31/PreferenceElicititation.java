package group31;

import agents.org.apache.commons.math.optimization.RealPointValuePair;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.UserModel;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.*;

// First we need to go through each of the issues. There are a total of ' m ' issues.
// Each issue ' i ' is discrete and can take a certain number of variables, denoted as ' n_i ', as it will
// likely be different for each issue. So we have a set ' V_i ' for every single issue.
// The total number of negotiation outputs is therefore V_1 * V_2 * ... * V_m, this is the Domain.
// Therefore, the outcome of the Domain is an m-sized array ' w ' which contains the values of the issues.
// So w_i (e.g. 0.5 gigawatts) points to a value in V_i (e.g. list of possible power) and is assigned to
// issue i (e.g. Energy cost?)

// To measure one outcome w over another, ' w_prime ', we need the utility function.
// We are using linear additive utility functions, so the utility of each issue is calculated separately,
// according to an evaluation function ' v '.
// This means that utility of negotiation output w, denoted as ' u(w) ', is calculated as:
// sum (weight_i) * v_i(w_i)
// The weights are normalised according to their importance to the user.

// Imagine a subset of the Domain, O, of possible negotiation outcomes. UserModel contains a ranking of these.
// The earlier they appear in the list, the higher the utility.
// If O = { o_1, o_2, ... , o_d } where each o is a possible negotiation outcome and that the
// outcomes are ordered with respect to utility, we can use the relation where utility of  o_i is greater
// than the utility of o_(i+1), making a set of size d-1 of pairwise comparisons called D

// First, we can rewrite our approximated utility function u(w) into ' sum phi_i(w_i) ' where
// phi_i(w_i) = (weight_i) * v_i(w_i)
// We have a set of discrete variables phi_i(w_i)

// We then consider the pairwise comparison set D, o and o_prime are elements in D
// The sum of phi_i(o_i) - phi_i(o_prime_i) >= 0
// We denote the above as Delta_o,o_prime >= 0

// To turn this into a linear optimization problem, we need to consider a slack variable z_o,o_prime
// We want to minimize z_o,o_prime subject to:
// 		1. z_o,o_prime + Delta_o,o_prime >= 0
// 		2. z_o,o_prime >= 0 for (o, o_prime) in D
// 		3. phi_i(x^i_j) >= 0 where i is an issue and j is all the possible values of that issue

// Mathematically, the above will lead to the state where z_o,o_prime = 0 and phi_i(x^i_j) = 0, this
// is not ideal. To solve this we use one final constraint, the optimal outcome for the user w^* with maximum
// utility. This does not tell us the importance of each issue, we simply know what state is ideal.
// So we have our final constraint
//		4. u(w^*) = 1

// Constraints 1. and 2. tell us z_o,o_prime = max{ 0, -Delta_o,o_prime }

// The final value of z_o,o_prime will give us the value of F which we can use to calculate F

public class PreferenceElicititation{
    // The final return type needs to be a user Model
    // The base version, userModel = user.elicitRank(bid, userModel), takes in as parameter the bid we want and the current userModel
    // The idea is that the returned userModel

    public UserModel currentModel;
    public NegotiationInfo info;
    public PreferenceElicititation(UserModel old_Model, NegotiationInfo negotiationInfo){
        currentModel = old_Model;
        info = negotiationInfo;
    }

    public AdditiveUtilitySpace generateNewEstimate(){
        AbstractUtilitySpace utilitySpace = this.info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        BidRanking currentRanking = this.currentModel.getBidRanking();
        Domain userDomain = this.currentModel.getDomain();

        List< Issue > issues =  additiveUtilitySpace.getDomain().getIssues();
        ArrayList< ArrayList <Double> > evaluations = new ArrayList< ArrayList<Double> >(issues.size());
        for(int i = 0; i < issues.size(); i++){
            evaluations.add(new ArrayList<Double>());
        }

        // go through each issue
        for(Issue issue : issues) {
            int issueNumber = issue.getNumber();
            // double issueWeight = additiveUtilitySpace.getWeight(issueNumber);
            // Issues take a value that is a name not a number, eg color
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            // Evaluator maps bids into a real number in the range 0 : 1, 0 min, 1 max
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber);

            // Go over all discrete values for this issue, eg red blue yellow
            int index = 0;
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                String name_of_value = valueDiscrete.getValue();
                // Convert discrete value of a discrete issue into a utility, non-normalized
                //double eval_of_value = evaluatorDiscrete.getValue(valueDiscrete);
                //evaluations.get(issueNumber).add(eval_of_value);
                try {
                    // Get utility of one issue in a bid, normalized
                    evaluations.get(issueNumber-1).add(evaluatorDiscrete.getEvaluation(valueDiscrete));
                    index ++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // For each of the bids in the UserModel's Bidranking, calculate the utility of bids
        // by multiplying the weight to the Evaluation. This is one parameter. We call it phi_o_i where o_i is a bid.
        // Every value of phi_o_i must be greater than 0
        // We say that if the bidranking is correct, for o_i and o_i+1
        // sum(phi_o_i) - sum(phi_o_i+1) >= 0
        // denoted as delta_o_i >=0
        // For every comparison, minimize a parameter Z, while Z >= 0 and Z + delta_o_i >= 0
        // Finally, the sum of weights of the issues must be equal to one

        // Set method, this value must take its value from a given set.


        // For every possible value of weights that fit the bound
        // Calculate utility
        // Calculate delta utility
        // Minimize Z
        // Get weights.

        // minimize Z according to a given list of weights

        // w_1 + w_2 + ... + w_I = 1


        // Set parameters to find: w_1 + ... + w_n + Z_o_1_o_2 + Z_o_2_o_3 + ... + Z_o_n-1_o_n
        int number_of_weights = issues.size();
        int number_of_bid_comparisons = currentRanking.getSize();
        double[] weight_of_unknowns = new double[number_of_weights + number_of_bid_comparisons];
        Arrays.fill(weight_of_unknowns, 1.0);
        // Aim is to minimize the sum of Z s
        for(int i = 0; i < number_of_weights; i++){
            weight_of_unknowns[i] = 0.0;
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(weight_of_unknowns, 0.0);

        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();

        double[] extract_params = new double[number_of_weights + number_of_bid_comparisons];
        Arrays.fill(extract_params, 0.0);
        // Extract weights
        for(int i = 0; i < number_of_weights; i++){
            extract_params[i] = 1.0;
        }

        // Sum of weights must be equal to 1.0
        constraints.add(new LinearConstraint(extract_params, Relationship.EQ, 1.0));

        // Reset extract_params to be all 0s
        Arrays.fill(extract_params, 0.0);

        // All Z values must be >= 0
        for(int i = number_of_weights; i < number_of_weights + number_of_bid_comparisons; i++){
            //Extract individual values of Z
            extract_params[i] = 1.0;
            constraints.add(new LinearConstraint(extract_params, Relationship.GEQ, 0.0));
            Arrays.fill(extract_params, 0.0);
        }

        // Reset extract_params to be all 0s
        Arrays.fill(extract_params, 0.0);

        // All phis must be >= 0
        for (int i = 0; i < evaluations.size(); i++) {
            // One hot encoding, extract the weight with index i
            // Get evaluations from earlier
            for (int j = 0; j < evaluations.get(i).size(); j++) {
                // Generate phi
                extract_params[i] = evaluations.get(i).get(j);
                // Set constraint
                // A trick is used here. In truth, w_name should be placed as the weight and phi
                // Should be what we are changing. However, we are looking for the correct set of weights.
                // So if we use the evalutaions as weights, we can change w_name and get the output.
                constraints.add(new LinearConstraint(extract_params, Relationship.GEQ, 0.0));
            }
            // Reset extract_params so that the next weight is not compromised.
            Arrays.fill(extract_params, 0.0);
        }

        // Reset extract_params to be all 0s
        Arrays.fill(extract_params, 0.0);

        // Need to iterate through the possible bids in BidRanking currentRanking
        List < Bid > bid_list = currentRanking.getBidOrder();
        Collections.reverse(bid_list);

        boolean loop_control = true;
        int index = 0;

        double[] eval_o_list = new double[number_of_weights];
        double[] eval_o_prime_list = new double[number_of_weights];

        while(loop_control == true){
            if(index == bid_list.size()-1){
                loop_control = false;
            }
            else{
                // Get bid o
                Bid bid_o = bid_list.get(index);
                // Get bid o prime
                Bid bid_o_prime = bid_list.get(index + 1);

                HashMap<Integer, Value> values_o = bid_o.getValues();
                HashMap<Integer, Value> values_o_prime = bid_o_prime.getValues();

                Arrays.fill(eval_o_list, 0.0);
                Arrays.fill(eval_o_prime_list, 0.0);

                int issueNumber = 0;
                for(Map.Entry<Integer, Value> entry : values_o.entrySet()) {
                    ValueDiscrete value_o_i = (ValueDiscrete) entry.getValue();
                    EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber+1);
                    try{
                        double eval_o_i = evaluatorDiscrete.getEvaluation(value_o_i);
                        eval_o_list[issueNumber] = eval_o_i;
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    issueNumber++;
                }

                issueNumber = 0;
                for(Map.Entry<Integer, Value> entry : values_o_prime.entrySet()) {
                    ValueDiscrete value_o_prime_i = (ValueDiscrete) entry.getValue();
                    EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber+1);
                    try{
                        double eval_o_prime_i = evaluatorDiscrete.getEvaluation(value_o_prime_i);
                        eval_o_prime_list[issueNumber] = eval_o_prime_i;
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    issueNumber++;
                }

                // Assign the params to weight params
                for(int i = 0; i < number_of_weights; i++){
                    extract_params[i] = eval_o_list[i] - eval_o_prime_list[i];
                }
                //Specify which Z_val
                extract_params[number_of_weights + index] = 1;

                constraints.add(new LinearConstraint(extract_params, Relationship.GEQ, 0.0));
            }
            Arrays.fill(extract_params, 0.0);
            index ++;
        }
        PointValuePair solution = null;
        try {
            solution = new SimplexSolver().optimize(f, new LinearConstraintSet(constraints), GoalType.MINIMIZE);
        } catch(Exception e){
            e.printStackTrace();
        }
        double[] new_weights = new double[number_of_weights];
        if (solution != null) {
            for (int i = 0; i < number_of_weights; i++) {
                new_weights[i] = solution.getPoint()[i];
            }
        }
        additiveUtilitySpace.setWeights(issues, new_weights);
        return additiveUtilitySpace;
    }

}
