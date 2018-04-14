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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class TestInfrastructure extends AbstractCustomerTest {

  @Autowired
  private DataSource dataSource;

  @Test
  public void shouldInitializeCustomer() throws Exception {
    try (final Connection connection = this.dataSource.getConnection()) {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_customers", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_addresses", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_contact_details", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_identification_cards", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_commands", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_task_definitions", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_task_instances", null).next());
    }
  }
}
