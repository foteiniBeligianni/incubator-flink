/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.streaming.sampling.helpers;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.incrementalML.inspector.PageHinkleyTest;
import org.apache.flink.streaming.sampling.generators.GaussianDistribution;

import java.util.Properties;

/**
 * Created by marthavk on 2015-05-12.
 */
public class DriftDetector implements MapFunction<Tuple2<GaussianDistribution, Double>, Tuple3<GaussianDistribution, Double, Boolean>> {

	PageHinkleyTest detector;
	double lambda;
	double delta;

	public DriftDetector() {
		Properties props = SamplingUtils.readProperties(SamplingUtils.path + "distributionconfig.properties");
		lambda = Double.parseDouble(props.getProperty("lambda"));
		delta = Double.parseDouble(props.getProperty("delta"));
		detector = new PageHinkleyTest(lambda, delta, 30);
	}


	@Override
	public Tuple3<GaussianDistribution, Double, Boolean> map(Tuple2<GaussianDistribution, Double> value) throws Exception {
		detector.input(value.f1);
		boolean drift = detector.isChangedDetected();
		if (drift) {
			detector.reset();
		}
		return new Tuple3<GaussianDistribution, Double, Boolean>(value.f0, value.f1, drift);
	}
}