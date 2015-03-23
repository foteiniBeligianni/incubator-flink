/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.examples.lambda;

import net.spy.memcached.compat.log.Logger;
import net.spy.memcached.compat.log.LoggerFactory;
import org.apache.flink.api.common.Plan;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.CsvReader;
import org.apache.flink.api.java.io.TypeSerializerOutputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.client.LocalExecutor;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.function.source.FileMonitoringFunction.WatchType;
import org.apache.flink.streaming.examples.unifiedStreamBatch.BatchJob;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class LambdaJoin {

	private static final Logger log = LoggerFactory.getLogger(LambdaJoin.class);

	private static final String JAR = "/home/fobeligi/workspace/incubator-flink/flink-staging/" +
			"flink-streaming/flink-streaming-examples/target/flink-streaming-examples-0.9-SNAPSHOT-LambdaJoin.jar";




	public static void main(String[] args) throws Exception {

//		exec.setTaskManagerNumSlots(8);
//		exec.start();

		final ExecutionEnvironment batchEnv = ExecutionEnvironment.createRemoteEnvironment("127.0.0.1",
				6123, 1, JAR);

		StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.createRemoteEnvironment(
				"127.0.0.1", 6123, 1, JAR);

		CsvReader csvR = batchEnv.readCsvFile("dataSet-files/exampleCSV_1.csv");
		csvR.lineDelimiter("\n");
		DataSet<Tuple2<Double,Integer>> ds = csvR.types(Double.class,Integer.class);

//		DataSet<Integer> dataSet = batchEnv.readFileOfPrimitives("example.txt", Integer.class);
		ds.write(new TypeSerializerOutputFormat<Tuple2<Double, Integer>>(), "/home/fobeligi/FlinkTmp/temp",
				WriteMode.OVERWRITE);
		ds.print();

		DataStream<Tuple2<String,Tuple2<Double,Integer>>> dataSetStream = streamEnv.readFileStream(
				"file:///home/fobeligi/FlinkTmp", ds.getType(), 1000,
				WatchType.REPROCESS_WITH_APPENDED);

		dataSetStream.print();

		try {
			runPeriodically(batchEnv, 5000);
			streamEnv.execute();
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private static void runPeriodically(ExecutionEnvironment env, long millis) {
		new Thread(new PeriodicJob(env, millis)).start();
	}

	private static class PeriodicJob implements Runnable {

		private Plan plan;
		private long millis;
		private static LocalExecutor exec = new LocalExecutor(false);

		public PeriodicJob(ExecutionEnvironment env, long millis) {
			this.plan = env.createProgramPlan();
			this.millis = millis;
			exec.setTaskManagerNumSlots(8);
			try {
				exec.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			if (true) {
				try {
					exec.executePlan(plan);
					Thread.sleep(millis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
