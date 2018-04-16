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
package org.apache.fineract.cn.customer.api.v1.domain;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.test.domain.ValidationTest;
import org.apache.fineract.cn.test.domain.ValidationTestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Myrle Krantz
 */
@RunWith(Parameterized.class)
public class CustomerDocumentTest extends ValidationTest<CustomerDocument> {

  public CustomerDocumentTest(final ValidationTestCase<CustomerDocument> testCase)
  {
    super(testCase);
  }

  @Override
  protected CustomerDocument createValidTestSubject() {
    final CustomerDocument ret = new CustomerDocument();
    ret.setIdentifier(RandomStringUtils.randomAlphanumeric(8));
    ret.setCompleted(false);
    ret.setCreatedOn(null);
    ret.setCreatedBy(null);
    ret.setDescription(RandomStringUtils.random(5));
    return ret;
  }

  @Parameterized.Parameters
  public static Collection testCases() {
    final Collection<ValidationTestCase> ret = new ArrayList<>();

    ret.add(new ValidationTestCase<CustomerDocument>("valid"));
    ret.add(new ValidationTestCase<CustomerDocument>("null description")
        .adjustment(x -> x.setDescription(null))
        .valid(true));
    ret.add(new ValidationTestCase<CustomerDocument>("minimum length description")
        .adjustment(x -> x.setDescription(RandomStringUtils.random(0)))
        .valid(true));
    ret.add(new ValidationTestCase<CustomerDocument>("maximum length description")
        .adjustment(x -> x.setDescription(RandomStringUtils.random(4096)))
        .valid(true));
    ret.add(new ValidationTestCase<CustomerDocument>("too long description")
        .adjustment(x -> x.setDescription(RandomStringUtils.random(4097)))
        .valid(false));

    return ret;
  }

}
