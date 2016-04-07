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

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
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
	
	private static int numTest;
	private static Logger log;

	static BasicNetwork createElmanNetwork(int numInputNeurons, int numOutputNeurons) {
		// construct an Elman type network
		ElmanPattern pattern = new ElmanPattern();
		pattern.setActivationFunction(new ActivationSigmoid());
		pattern.setInputNeurons(numInputNeurons);
		pattern.addHiddenLayer(6);
		pattern.setOutputNeurons(numOutputNeurons);
		return (BasicNetwork)pattern.generate();
	}
	
	
	public static void main(final String args[]) {
		numTest = FileWriter.getTestNumber("result/ElmanSequence");
		
		Handler fh = null;
		try {
			fh = new FileHandler("result/ElmanSequence" + numTest + ".log");
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log = Logger.getLogger("");
		log.addHandler(fh);
		log.setLevel(Level.FINEST);
		SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
		
		
		
		MyFileHandler fileHandler = new MyFileHandler(log);
		int numInputNeurons = 1;
		int numOutputNeurons = 1;

		float[][] trainData = null;
		int[] trainDataLengthVector = new int[1];
		log.log( Level.FINEST, "Reading input files");
		try {
			trainData = fileHandler.readAllInputFromDirectory("potholes"+"/"+"train", trainDataLengthVector,
					numInputNeurons+numOutputNeurons);
		} catch (IOException e) {
			log.log( Level.SEVERE, e.toString(), e );
		}
		log.log( Level.FINE, "Training files read");
		int inputDataLength = trainDataLengthVector[0];
		
		log.log( Level.FINE, "Generating training sequence");

		final Sequence trainingSequence = new Sequence(trainData, inputDataLength, 
				numInputNeurons, numOutputNeurons);
		MLDataSet trainingSet = trainingSequence.generate(1);

		log.log( Level.FINE, "Creating elman network");
		
		final BasicNetwork elmanNetwork = ElmanSequence.createElmanNetwork(numInputNeurons, numOutputNeurons);

		log.log( Level.FINE, "Training elman network");
		
		final double elmanError = ElmanSequence.trainNetwork("Elman", elmanNetwork,
				trainingSet);	
		
		log.log( Level.FINE, "Elman network trained. Now its testing time");
		
		trainData = null;
		trainingSet = null;
		
		float[][] testData = null;
		int[] testDataLengthVector = new int[1];
		
		log.log( Level.FINE, "Reading test data");
		
		try {
			testData = fileHandler.readAllInputFromDirectory("potholes"+"/"+"test", testDataLengthVector,
					numInputNeurons+numOutputNeurons);
		} catch (IOException e) {
			log.log( Level.SEVERE, e.toString(), e );
		}
		
		log.log( Level.FINE, "Test files read");
		
		int testDataLength = testDataLengthVector[0];

		log.log( Level.FINE, "Generating testing sequence");
		
		final Sequence testSequence = new Sequence(testData, testDataLength, 
				numInputNeurons, numOutputNeurons);
		final MLDataSet testingSet = testSequence.generate(1);
		
		log.log( Level.FINE, "Testing elman network");
		
		ElmanSequence.testNetwork(elmanNetwork, testingSet);		
		
		log.log( Level.FINE, "Testing finished");
		
		log.log( Level.FINE, "Shutting down");
		
		Encog.getInstance().shutdown();
	}

	private static void testNetwork(BasicNetwork elmanNetwork,
			MLDataSet testingSet) {
		FileWriter fileWriter = new FileWriter(log);
		fileWriter.openNewFile("result/testing" + numTest);
		for(int datasetIdx = 0; datasetIdx < testingSet.size(); datasetIdx++){
			MLDataPair mldataPair = testingSet.get(datasetIdx);
			MLData output = elmanNetwork.compute(mldataPair.getInput());
			fileWriter.setFloat((float)mldataPair.getInput().getData(0));
			fileWriter.setFloat((float)output.getData(0));
			fileWriter.setFloat((float)mldataPair.getIdeal().getData(0));
			fileWriter.nextLine();
		}
		fileWriter.closeFile();
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

		FileWriter fileWriter = new FileWriter(log);
		fileWriter.openNewFile("result/logSyso" + numTest);
		
		int epoch = 0;		
		
		while (!stop.shouldStop()) {
			trainMain.iteration();
			fileWriter.setLine("Training " + what + ", Epoch #" + epoch
					+ " Error:" + trainMain.getError());
			epoch++;
		}
		
		fileWriter.setLine("Best error rate with Elman Network: " + trainMain.getError());

		fileWriter.closeFile();
		return trainMain.getError();
	}
}
