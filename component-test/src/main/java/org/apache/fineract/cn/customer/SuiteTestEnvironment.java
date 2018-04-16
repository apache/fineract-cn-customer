/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.customer;

import org.apache.fineract.cn.test.env.TestEnvironment;
import org.apache.fineract.cn.test.fixture.cassandra.CassandraInitializer;
import org.apache.fineract.cn.test.fixture.mariadb.MariaDBInitializer;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.RunExternalResourceOnce;
import org.junit.rules.TestRule;

/**
 * @author Myrle Krantz
 */
public class SuiteTestEnvironment {
  static final String APP_NAME = "customer-v1";
  final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  final static MariaDBInitializer mariaDBInitializer = new MariaDBInitializer();

  @ClassRule
  public static TestRule orderClassRules = RuleChain
      .outerRule(new RunExternalResourceOnce(testEnvironment))
      .around(new RunExternalResourceOnce(cassandraInitializer))
      .around(new RunExternalResourceOnce(mariaDBInitializer));
}
