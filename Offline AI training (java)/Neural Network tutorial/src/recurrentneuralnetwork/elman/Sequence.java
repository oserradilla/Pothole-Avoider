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

import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;

/**
 * Utility class that presents the XOR operator as a serial stream of values.
 * This is used to predict the next value in the XOR sequence. This provides a
 * simple stream of numbers that can be predicted.
 * 
 * @author jeff
 * 
 */
public class Sequence {

	private final float[][] trainData;
	private final int numRows;
	private final int numInputNeurons;
	private final int numOutputNeurons;
	
	public Sequence (final float[][] trainData, final int numRows, 
			final int numInputNeurons, final int numOutputNeurons) {
		this.trainData = trainData;
		this.numRows = numRows;
		this.numInputNeurons = numInputNeurons;
		this.numOutputNeurons = numOutputNeurons;
	}

	public MLDataSet generate(final float numTimesRepeatTrainData) {
		
		final int totalNumOfInstancesToTrain = (int) numTimesRepeatTrainData*numRows;
		double[][] input = new double[totalNumOfInstancesToTrain][numInputNeurons];
		double[][] ideal = new double[totalNumOfInstancesToTrain][numOutputNeurons];

		for (int instanceIdx = 0; instanceIdx < totalNumOfInstancesToTrain; instanceIdx++) {
			for(int inputIdx = 0; inputIdx < numInputNeurons; inputIdx++) {
				input[instanceIdx][inputIdx] = trainData[instanceIdx % numRows][inputIdx];
			}
			for(int outputIdx = 0; outputIdx < numOutputNeurons; outputIdx++) {
				ideal[instanceIdx][outputIdx] = trainData[instanceIdx % numRows][numInputNeurons+outputIdx];
			}
		}

		return new BasicMLDataSet(input, ideal);
	}
}
