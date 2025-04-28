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
 * 
 */
public class BranchingTree {
	public static final int MAX_GEN = 20;
	public static final double TRIALS = 1000;

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int sumLastGen;
		double[][] Ds = { { 0.5, 0.25, 0.25 }, { 0.25, 0.25, 0.5 }, { 0.333, 0.334, 0.333 } };
		String[] names = { "D1", "D2", "D3" };
        double[] sumSizes;
        List<double[]> allAverages = new ArrayList<>();

		for (int i = 0; i < Ds.length; i++) {
			sumLastGen = 0;
			sumSizes = new double[MAX_GEN + 1];
			for (int j = 0; j < TRIALS; j++) {
				sumLastGen += singleTrial(Ds[i], sumSizes);
			}
			// compute averages for generations 1..generations
	        double[] averages = new double[MAX_GEN];
	        for (int g = 1; g <= MAX_GEN; g++) {
	            averages[g - 1] = sumSizes[g] / TRIALS;
	        }
	        allAverages.add(averages);
			double avgLastGen = sumLastGen / TRIALS;
			System.out.printf("Experiment (Empirical): %s, avg size at gen %d over %,d trials = %.3f\n", names[i], MAX_GEN, (int) TRIALS, avgLastGen);
			double expected = theoretical(Ds[i]);
			System.out.printf("Theoretical: %s, avg size at gen %d over %,d trials = %.3f\n", names[i], MAX_GEN, (int) TRIALS, expected);

		}

		// Build the chart
        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("E[# of nodes] per generation")
            .xAxisTitle("Generation")
            .yAxisTitle("Average # nodes")
            .build();

        // Use a line chart
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

        // X data: 1,2,...,12
        double[] gens = IntStream.rangeClosed(1,  MAX_GEN)
                                 .asDoubleStream()
                                 .toArray();

        // Add one series per distribution
        for (int d = 0; d < names.length; d++) {
            chart.addSeries(names[d], gens, allAverages.get(d));
        }

        // Show it
        new SwingWrapper<>(chart).displayChart();
        
        
	}

	public static double theoretical(double[] D) {
		double mean = D[1] * 1 + D[2]*2;
		return Math.pow(mean, 20);
	}
	/**
	 * 
	 * @param D
	 * @return
	 */
	public static int singleTrial(double[] D, double[] sumSizes) {
		int currentGen = 1; // number of nodes at current generation (we start with the root)
		int nextGen;
		Random rnd = new Random();
		   // sumSizes[g] accumulates total nodes at generation g across all trials
        
		// build a cumulative distribution
		double[] cdf = new double[D.length];
		cdf[0] = D[0];
		for (int i = 1; i < D.length; i++) {
			cdf[i] = cdf[i - 1] + D[i];
		}

		// loop over each generation
		for (int i = 0; i < MAX_GEN && currentGen > 0; i++) {
			nextGen = 0;
			// each individual in current generation reproduces
			for (int j = 0; j < currentGen; j++) {
				double pick = rnd.nextDouble();
				if (pick < cdf[0]) {
					nextGen += 0;
				} else if (pick < cdf[1]) {
					nextGen += 1;
				} else if (pick < cdf[2]) {
					nextGen += 2;
				}
			}
			currentGen = nextGen;
            sumSizes[i+1] += currentGen;
		}

        
		return currentGen; // the number of nodes at the last generation
	}
	

}
