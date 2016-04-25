/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.mlbenchmark.validation.measures;

public class AccMethodPredAccWeighted implements MeasureMethodTwoValued {

	private boolean balanced = false;
	private double posWeight = 1;
	private double negWeight = 1;

	public double getMeasure(int tp, int fn, int fp, int tn) {
		int posExamples = tp + fn;
		int negExamples = fp + tn;

		if (balanced) {
			posWeight = 1/(double)posExamples;
			negWeight = 1/(double)negExamples;
		}
		return ((tp*posWeight) + (tn*negWeight)) / ((posExamples*posWeight)+(negExamples*negWeight));
	}

	public boolean isBalanced() {
		return balanced;
	}

	public void setBalanced(boolean balanced) {
		this.balanced = balanced;
	}

	public double getPosWeight() {
		return posWeight;
	}

	public void setPosWeight(double posWeight) {
		this.posWeight = posWeight;
	}

	public double getNegWeight() {
		return negWeight;
	}

	public void setNegWeight(double negWeight) {
		this.negWeight = negWeight;
	}

}
