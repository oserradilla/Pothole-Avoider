/*
 * Encog(tm) Java Examples v3.3
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 *
 * Copyright 2008-2014 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package recurrentneuralnetwork.elman;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Greedy;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.pattern.ElmanPattern;

/**
 * Implement an Elman style neural network with Encog. This network attempts to
 * predict detect if a sequence (peak of a signal) has passed or not. The
 * internal state stored by an Elman neural network allows better performance.
 * Elman networks are typically used for temporal neural networks. An Elman
 * network has a single context layer connected to the hidden layer.
 * 
 * @author jeff
 * 
 */
public class ElmanSequence {

	static BasicNetwork createElmanNetwork() {
		// construct an Elman type network
		ElmanPattern pattern = new ElmanPattern();
		pattern.setActivationFunction(new ActivationSigmoid());
		pattern.setInputNeurons(1);
		pattern.addHiddenLayer(6);
		pattern.setOutputNeurons(1);
		return (BasicNetwork)pattern.generate();
	}

	public static void main(final String args[]) {
		
		final TemporalSequence temp = new TemporalSequence();
		final MLDataSet trainingSet = temp.generate(1700); //Multiple of sequence

		final BasicNetwork elmanNetwork = ElmanSequence.createElmanNetwork();

		final double elmanError = ElmanSequence.trainNetwork("Elman", elmanNetwork,
				trainingSet);	

		System.out.println("Best error rate with Elman Network: " + elmanError);
		
		System.out.println("\n\n");
		
		double[][] input = new double[3][1];
		input[0][0] = 1;
		input[1][0] = 2;
		input[2][0] = 3;
		
		MLData mldata1 = new BasicMLData(input[0]);
		MLData computedData = elmanNetwork.compute(mldata1);
		System.out.print("Given: " + input[0][0]);
		System.out.println(" Predicted: "+computedData.getData(0));
		
		MLData mldata2 = new BasicMLData(input[1]);
		MLData computedData2 = elmanNetwork.compute(mldata2);
		System.out.print("Given: " + input[1][0]);
		System.out.println(" Predicted: "+computedData2.getData(0));
		
		MLData mldata3 = new BasicMLData(input[2]);
		MLData computedData3 = elmanNetwork.compute(mldata3);
		System.out.print("Given: " + input[2][0]);
		System.out.println(" Predicted: "+computedData3.getData(0));
		
		input[0][0] = 4;
		input[1][0] = 3;
		input[2][0] = 2;
		
		mldata1 = new BasicMLData(input[0]);
		computedData = elmanNetwork.compute(mldata1);
		System.out.print("Given: " + input[0][0]);
		System.out.println(" Predicted: "+computedData.getData(0));
		
		mldata2 = new BasicMLData(input[1]);
		computedData2 = elmanNetwork.compute(mldata2);
		System.out.print("Given: " + input[1][0]);
		System.out.println(" Predicted: "+computedData2.getData(0));
		
		mldata3 = new BasicMLData(input[2]);
		computedData3 = elmanNetwork.compute(mldata3);
		System.out.print("Given: " + input[2][0]);
		System.out.println(" Predicted: "+computedData3.getData(0));
		
		input[0][0] = 1;
		input[1][0] = 1;
		input[2][0] = 1;
		
		mldata1 = new BasicMLData(input[0]);
		computedData = elmanNetwork.compute(mldata1);
		System.out.print("Given: " + input[0][0]);
		System.out.println(" Predicted: "+computedData.getData(0));
		
		mldata2 = new BasicMLData(input[1]);
		computedData2 = elmanNetwork.compute(mldata2);
		System.out.print("Given: " + input[1][0]);
		System.out.println(" Predicted: "+computedData2.getData(0));
		
		mldata3 = new BasicMLData(input[2]);
		computedData3 = elmanNetwork.compute(mldata3);
		System.out.print("Given: " + input[2][0]);
		System.out.println(" Predicted: "+computedData3.getData(0));
		
		
		Encog.getInstance().shutdown();
	}

	public static double trainNetwork(final String what,
			final BasicNetwork network, final MLDataSet trainingSet) {
		// train the neural network
		CalculateScore score = new TrainingSetScore(trainingSet);
		final MLTrain trainAlt = new NeuralSimulatedAnnealing(
				network, score, 10, 2, 100);

		final MLTrain trainMain = new Backpropagation(network, trainingSet,0.000001, 0.0);

		final StopTrainingStrategy stop = new StopTrainingStrategy();
		trainMain.addStrategy(new Greedy());
		trainMain.addStrategy(new HybridStrategy(trainAlt));
		trainMain.addStrategy(stop);

		int epoch = 0;
		while (!stop.shouldStop()) {
			trainMain.iteration();
			System.out.println("Training " + what + ", Epoch #" + epoch
					+ " Error:" + trainMain.getError());
			epoch++;
		}
		return trainMain.getError();
	}
}
