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
package org.apache.flink.streaming.scala.examples.incrementalML.classifier

import org.apache.flink.streaming.scala.examples.incrementalML.classifier.AttributeType
.AttributeType

class VFDTAttributes(
  val id: Int,
  val value: Double,
  val clazz: Double,
  val leaf: Int,
  val attributeType: AttributeType)
  extends Metrics
  with Serializable {


  def getClazz : Double ={
    clazz
  }

  def getValue : Double ={
    value
  }

  override def toString: String = {
    s"Attr. $id= $value, leaf: $leaf"
  }
}

object VFDTAttributes {
  def apply(id: Int, value: Double, clazz: Double, leaf: Int, attrType: AttributeType): VFDTAttributes = {
    new VFDTAttributes(id, value, clazz, leaf, attrType)
  }
}


object AttributeType extends Enumeration with Serializable {
  type AttributeType = Value
  val Nominal,Numerical = Value
}
