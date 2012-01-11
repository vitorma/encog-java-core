package org.encog.ml.hmm.distributions;

import java.util.Arrays;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;

public class DiscreteDistribution 
implements StateDistribution
{
	private double[] probabilities;
	
	public DiscreteDistribution(int c)
	{
		probabilities = new double[c];
		
		for (int i = 0; i < c; i++)
			probabilities[i] = 1. / ((double) c);
	}
	
	
	public DiscreteDistribution(double[] probabilities)
	{		
		if (probabilities.length == 0) 
			throw new IllegalArgumentException("Invalid empty array");
		
		this.probabilities = new double[probabilities.length];
		
		for (int i = 0; i < probabilities.length; i++)
			if ((this.probabilities[i] = probabilities[i]) < 0.)
				throw new IllegalArgumentException();
	}
	
	
	public double probability(MLDataPair o)
	{
		if (o.getInput().getData(0) > probabilities.length-1)
			throw new IllegalArgumentException("Wrong observation value");
		
		return probabilities[(int)o.getInput().getData(0)];
	}
	
	
	public MLDataPair generate()
	{	
		MLData result = new BasicMLData(1);
		double rand = Math.random();
		
		for (int i = 0; i < probabilities.length - 1; i++)
			if ((rand -= probabilities[i]) < 0.) {
				result.setData(0, i);
				return new BasicMLDataPair(result);
			}
		
		result.setData(0, probabilities.length - 1);
		return new BasicMLDataPair(result);
	}
	
	public void fit(MLDataSet co)
	{	
		if (co.size()<1)
			throw new IllegalArgumentException("Empty observation set");
		
		for (int i = 0; i < probabilities.length; i++)
			probabilities[i] = 0.;
		
		for (MLDataPair o : co)
			probabilities[(int)o.getInput().getData(0)]++;
		
		for (int i = 0; i < probabilities.length; i++)
			probabilities[i] /= co.size();
	}	
	
	public void fit(MLDataSet co,
			double[] weights)
	{	
		if (co.size()<1 || co.size() != weights.length)
			throw new IllegalArgumentException();
		
		Arrays.fill(probabilities, 0.);
		
		int i = 0;
		for (MLDataPair o : co) 
			probabilities[(int)o.getInput().getData(0)] += weights[i++];
	}
	
	
	public DiscreteDistribution clone()
	{	
		try {
			return (DiscreteDistribution) super.clone();
		} catch(CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
	}
	
}