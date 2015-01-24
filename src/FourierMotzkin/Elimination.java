package FourierMotzkin;

import java.util.LinkedList;

public class Elimination {

	private LinkedList<Integer> P;

	private LinkedList<Integer> N;

	private LinkedList<Integer> Z;

	// an array with all variables we have to eliminate
	private int[] eliminationVariables;

	// the matrix with all coefficients of the conditions and the vector b in
	// the last column.
	// to keep it easy, the first column and the first row is 0
	private double[][] conditions;

	/**
	 * This contructor needed informations the Reader read in out of the
	 * inputfile. This is the coefficientsmatrix with vector b in it and the
	 * variables we have to eliminate
	 */
	public Elimination(double[][] conditions, int[] eliminationVariables) {
		this.eliminationVariables = eliminationVariables;
		this.conditions = conditions;
	}

	/**
	 * getter method for elimination variables
	 * 
	 * @return eliminationVariables
	 */
	public int[] getEliminationVariables() {
		return this.eliminationVariables;
	}

	/**
	 * getter method for matrix A
	 * 
	 * @return matrixA
	 */
	public double[][] getConditions() {
		return this.conditions;
	}

	/**
	 * assign the lines to P, N, Z depending on elVar
	 */
	public void assign(double[][] system, int elVar) {

		LinkedList<Integer> P = new LinkedList<Integer>();
		LinkedList<Integer> N = new LinkedList<Integer>();
		LinkedList<Integer> Z = new LinkedList<Integer>();

		for (int i = 1; i < system.length; i++) {
			if (system[i][elVar] < 0) {
				N.add(i);
			} else if (system[i][elVar] > 0) {
				P.add(i);
			} else {
				Z.add(i);
			}
		}

		this.P = P;
		this.N = N;
		this.Z = Z;

	}

	/**
	 * Scale the lines of the matrix, so that the row with index elVar only
	 * contain {-1,0,1}
	 */
	public double[][] scale(double[][] system, int elVar) {

		for (int i = 1; i < system.length; i++) {

			double n = system[i][elVar];
			if (n < 0)
				n = -n;
			for (int j = 0; j < system[1].length; j++) {

				if (n != 0)
					system[i][j] = system[i][j] / n;

			}
		}
		return system;
	}

	/**
	 * tests if a linevector is zero
	 * 
	 * @param dummy
	 * @return
	 */
	public boolean testZero(double[] dummy) {
		boolean nonZero = false;
		int i = 0;
		while (i < dummy.length - 1) {
			if (dummy[i] != 0)
				nonZero = true;
			i++;
		}

		return nonZero;
	}

	public boolean testEqualOrRedundant(double[][] solution, double[] dummy,
			int i) {
		boolean Equal = true;

		for (int j = 1; j < i; i++) {

			for (int k = 1; k < dummy.length - 1; k++) {
				if (dummy[k] != solution[j][k]) {
					Equal = false;
					break;
				}
			}
			if (Equal) {
				if (dummy[dummy.length - 1] >= solution[j][dummy.length - 1])
					return Equal;
			}
		}
		return false;
	}

	public double[][] testEqualOrRedundant2(double[][] solution,
			double[] dummy, int i) {
		boolean Equal = true;

		for (int j = 1; j < i; j++) {

			for (int k = 1; k < dummy.length - 1; k++) {
				if (dummy[k] != solution[j][k]) {
					Equal = false;
					break;
				}
			}
			if (Equal) {
				if (dummy[dummy.length - 1] >= solution[j][dummy.length - 1]) {
					return solution; // no change necessary
				} else {
					solution[j] = dummy; // change line j with dummy
					return solution;
				}
			}

		}
		solution[i] = dummy;
		return solution;
	}

	/**
	 * 
	 * @param system
	 *            Matrix der Ungleichungen
	 * @param elVar
	 *            zu eliminierende Variable
	 * @return
	 */

	public void eliminate(int elVar) {

		assign(this.conditions, elVar); // fill N,P,Z
		System.out.println(Z.toString());
		System.out.println(N.toString());
		System.out.println(P.toString());

		int numberOfVariables = this.conditions[1].length;	// number of variables in the problem
		int numberOfConditionsNotZero = 0;		// count the number of conditions that are not zero

		this.conditions = scale(this.conditions, elVar); // scale matrix

		// Create a new bigger matrix
		double[][] solution = new double[N.size() * P.size() + Z.size() + 1][numberOfVariables];
		for (int j = 0; j < this.conditions[0].length; j++) {
			solution[0][j] = 0; // Fill first line with 0
		}

		// insert lines from Z to the new matrix

		int i = 1;
		int x = 1;
		for (; x <= Z.size(); x++) {
			if (testZero(this.conditions[Z.get(x - 1)])) {
				solution[i] = this.conditions[Z.get(x - 1)];
				i++;
				numberOfConditionsNotZero++;
			}
		}

		// insert the combination of lines from N and P in the matrix

		for (int l = 0; l < N.size(); l++) {

			for (int k = 0; k < P.size(); k++) {

				double[] dummy = new double[numberOfVariables];

				for (int j = 0; j < dummy.length; j++) {
					dummy[j] = this.conditions[N.get(l)][j]
							+ this.conditions[P.get(k)][j];

				}
				if (testZero(dummy)) { // test if all variables are zero
					solution[i] = dummy; // if not insert the line in our matrix
					i++;
					numberOfConditionsNotZero++;
					// solution=testEqualOrRedundant2(solution, dummy, i);
					// if (!testZero(solution[i])) i++;

				}
			}
		}
		
		// now create an array with the right number of conditions. so we wont have any 0-lines
		this.conditions = new double[numberOfConditionsNotZero+1][numberOfVariables];

		// copy the non-0-lines from solution to conditions
		for (int j = 1; j <= numberOfConditionsNotZero; j++) {
			this.conditions[j] = solution[j];
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfConditions() {
		return this.getConditions().length - 1;
	}

	/**
	 * Method to print the coefficients matrix
	 * 
	 * @return String which represents the coefficients matrix
	 */
	public String toStringCoefficients() {

		StringBuffer string = new StringBuffer();

		for (int j = 1; j < conditions.length; j++) {
			for (int i = 1; i < conditions[j].length - 1; i++) {
				string.append(conditions[j][i] + " ");
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	/**
	 * Method to print the conditions
	 * 
	 * @return String which contains all the conditions we can create out of the
	 *         matrix line by line
	 */
	public String toStringConditions() {

		StringBuffer string = new StringBuffer();

		for (int j = 1; j < conditions.length; j++) {

			for (int i = 1; i < conditions[j].length; i++) {

				// the last entry in a row is the constant of b
				if (i == conditions[j].length - 1) {
					string.append("<= " + conditions[j][i]);
				} else { // other entries are coefficients
					string.append(conditions[j][i] + "*x" + i + " ");
				}
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	/**
	 * Method to print the elimination variables
	 * 
	 * @return String which contains all elimination variables
	 */
	public String toStringEliminationVariables() {

		StringBuffer string = new StringBuffer();

		for (int i = 0; i < eliminationVariables.length; i++) {
			string.append("X" + eliminationVariables[i] + " ");
		}
		string.append("\n \n");
		return string.toString();
	}

}
