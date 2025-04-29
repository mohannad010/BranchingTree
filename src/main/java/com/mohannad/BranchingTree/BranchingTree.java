package com.mohannad.BranchingTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

/**
 * Simulates a branching process for different child-distributions (D1, D2, D3),
 * runs multiple trials, records average generation sizes, and plots results.
 */
public class BranchingTree {
	public static final int MAX_GEN = 20;
	public static final double TRIALS = 1000;

	public static void main(String[] args) {
		int sumLastGen;
		double[][] Ds = { { 0.5, 0.25, 0.25 }, { 0.25, 0.25, 0.5 }, { 1.0/3.0, 1.0/3.0, 1.0/3.0 } };
		String[] names = { "D1", "D2", "D3" };
		double[] sumSizes;
		Random rnd = new Random();
		List<double[]> allAverages = new ArrayList<>();
		double[][] theoryGenSizes = new double[Ds.length][MAX_GEN]; // rows = dist, cols = generation

		// Loop through each distribution
		for (int i = 0; i < Ds.length; i++) {
			sumLastGen = 0;
			sumSizes = new double[MAX_GEN + 1];

			// Run trials for the current distribution
			for (int j = 0; j < TRIALS; j++) {
				sumLastGen += singleTrial(Ds[i], sumSizes, rnd);
			}

			// compute average size per generation
			double[] averages = new double[MAX_GEN];
			for (int g = 1; g <= MAX_GEN; g++) {
				averages[g - 1] = sumSizes[g] / TRIALS;
			}
			allAverages.add(averages);

			// Print experiment results for generation MAX_GEN
			double avgLastGen = sumLastGen / TRIALS;
			System.out.printf("Experiment (Empirical): %s, avg size at gen %d over %,d trials = %.3f\n", names[i],
					MAX_GEN, (int) TRIALS, avgLastGen);

			// Print theoretical expected size at generation MAX_GEN
			double expected = theoretical(Ds[i], theoryGenSizes, i);
			System.out.printf("Theoretical: %s, avg size at gen %d over %,d trials = %.3f\n", names[i], MAX_GEN,
					(int) TRIALS, expected);

		}

		// Build the chart
		XYChart chart = new XYChartBuilder().width(800).height(600).title("E[# of nodes] per generation")
				.xAxisTitle("Generation").yAxisTitle("Average # nodes").build();

		// Use a line chart
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

		// X data: 1 to 20
		double[] gens = IntStream.rangeClosed(1, MAX_GEN).asDoubleStream().toArray();
		
		// Add theoretical expected lines
		for (int d = 0; d < names.length; d++) {
			chart.addSeries(names[d] + " (theory)", gens, theoryGenSizes[d]);
		}
		
		// Add experimental results lines
		for (int d = 0; d < names.length; d++) {
			chart.addSeries(names[d] + " (empirical)", gens, allAverages.get(d));
		}

		// Show it
		new SwingWrapper<>(chart).displayChart();

	}

	/**
	 * Fills theoryGenSizes[dist] with expected values at generations 1..MAX_GEN.
	 *
	 * @param D                Probability distribution over {0, 1, 2} children
	 * @param theoryGenSizes   Matrix to store theoretical sizes per generation
	 * @param dist             Index of the current distribution
	 * @return Expected number of nodes at generation MAX_GEN
	 */
	public static double theoretical(double[] D, double[][] theoryGenSizes, int dist) {
		double mean = D[1] * 1 + D[2] * 2;
		for (int i = 1; i <= MAX_GEN; i++) {
			theoryGenSizes[dist][i - 1] = Math.pow(mean, i);
		}
		return Math.pow(mean, MAX_GEN);
	}

	/**
	 * Simulates one trial of the branching process using a given child distribution.
	 * For each generation, it counts how many children are produced, and accumulates
	 * the total number of nodes in the sumSizes array.
	 *
	 * @param D        Array of probabilities for 0, 1, and 2 children
	 * @param sumSizes Accumulates total number of nodes per generation (across trials)
	 * @return Number of nodes in the last generation of this trial
	 */
	public static int singleTrial(double[] D, double[] sumSizes, Random rnd) {
		int currentGen = 1; // number of nodes at current generation (we start with the root)
		int nextGen;
		// sumSizes[g] accumulates total nodes at generation g across all trials

		// build a cumulative distribution
		double[] cumulativeDist = new double[D.length];
		cumulativeDist[0] = D[0];
		for (int i = 1; i < D.length; i++) {
			cumulativeDist[i] = cumulativeDist[i - 1] + D[i];
		}

		// loop over each generation
		for (int i = 0; i < MAX_GEN && currentGen > 0; i++) {
			nextGen = 0;
			// each node in current generation reproduces
			for (int j = 0; j < currentGen; j++) {
				double pick = rnd.nextDouble();
				if (pick < cumulativeDist[0]) {
					nextGen += 0;
				} else if (pick < cumulativeDist[1]) {
					nextGen += 1;
				} else if (pick < cumulativeDist[2]) {
					nextGen += 2;
				}
			}
			currentGen = nextGen;
			sumSizes[i + 1] += currentGen; // accumulate size for this generation
		}

		return currentGen; // the number of nodes at the last generation
	}

}
