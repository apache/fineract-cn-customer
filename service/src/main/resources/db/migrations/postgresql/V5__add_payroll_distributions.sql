--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

CREATE TABLE maat_payroll_distributions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  main_account_number VARCHAR(34) NOT NULL,
  created_by VARCHAR(32) NOT NULL,
  created_on TIMESTAMP(3) NOT NULL,
  last_modified_by VARCHAR(32) NULL,
  last_modified_on TIMESTAMP(3) NULL,
  CONSTRAINT maat_payroll_distributions_pk PRIMARY KEY (id),
  CONSTRAINT maat_payroll_dist_main_acct_uq UNIQUE (customer_id, main_account_number),
  CONSTRAINT maat_payroll_dist_customers_fk FOREIGN KEY (customer_id) REFERENCES maat_customers (id)
);

CREATE TABLE maat_payroll_allocations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  payroll_distribution_id BIGINT NOT NULL,
  account_number VARCHAR(34) NOT NULL,
  amount NUMERIC(15,5) NOT NULL,
  proportional BOOLEAN NOT NULL,
  CONSTRAINT maat_payroll_allocations_pk PRIMARY KEY (id),
  CONSTRAINT maat_payroll_alloc_acct_uq UNIQUE (payroll_distribution_id, account_number),
  CONSTRAINT maat_payroll_alloc_dist_fk FOREIGN KEY (payroll_distribution_id) REFERENCES maat_payroll_distributions (id)
);