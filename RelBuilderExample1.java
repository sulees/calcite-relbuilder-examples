/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.calcite.examples;

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.test.RelBuilderTest;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelBuilder;

public class RelBuilderExample1 {
  private final boolean verbose;

  public RelBuilderExample1(boolean verbose) {
    this.verbose = verbose;
  }

  public static void main(String[] args) {
    new RelBuilderExample1(true).runAllExamples();
  }

  public void runAllExamples() {
    final FrameworkConfig config = RelBuilderTest.config().build();
    final RelBuilder builder = RelBuilder.create(config);

    for (int i = 0; i <= 4; i++) {
      RelNode node = doExample(builder, i).build();
      if (verbose) {
        System.out.println(RelOptUtil.toString(node));
      }
    }
  }

  private RelBuilder doExample(RelBuilder builder, int i) {
    switch (i) {
    case 0: return example0(builder);
    case 1: return example1(builder);
    case 2: return example2(builder);
    case 3: return example3(builder);
    case 4: return example4(builder);
    default: throw new AssertionError("Unknown example: " + i);
    }
  }
// now here we have our first example simple select * from BONUS
  private RelBuilder example0(RelBuilder builder) {
    return builder.scan("BONUS");
  }
//here we are scanning and filtering here, as you can see SELECT * FROM EMP WHERE SAL > 2000;
//as you can see here, the filter is not based on the aggregate's column but rather the SAL column itself
  private RelBuilder example1(RelBuilder builder) {
    return builder
        .scan("EMP")
        .filter(
            builder.call(
                SqlStdOperatorTable.GREATER_THAN,
                builder.field("SAL"),
                builder.literal(2000)
            )
        );
  }
  //here we just changed the the greater_than to a less_than
  private RelBuilder example2(RelBuilder builder) {
    return builder
        .scan("EMP")
        .filter(
            builder.call(
                SqlStdOperatorTable.LESS_THAN,
                builder.field("SAL"),
                builder.literal(2000)
            )
        );
  }
  //now here we are joining two scans, and we projecting two fields which would be like:
  // SELECT ENAME, DNAME FROM EMP INNER JOIN DEPT ON DEPT.DEPTNO = EMP.DEPTNO
  private RelBuilder example3(RelBuilder builder) {
    return builder
        .scan("EMP")
        .scan("DEPT")
        .join(JoinRelType.INNER, "DEPTNO") 
        .project(
            builder.field("ENAME"),
            builder.field("DNAME")
        );
  }
/**
SELECT COUNT(*) AS EMP_COUNT 
FROM EMP 
GROUP BY DEPTNO 
HAVING COUNT(*) > 3;
  */
  private RelBuilder example4(RelBuilder builder) {
    return builder
        .scan("EMP")
        .aggregate(
            builder.groupKey("DEPTNO"),
            builder.count(false, "EMP_COUNT")
        )
        .filter(
            builder.call(
                SqlStdOperatorTable.GREATER_THAN,
                builder.field("EMP_COUNT"),
                builder.literal(3)
            )
        );
  }
}
