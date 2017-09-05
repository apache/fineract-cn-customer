/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.customer;

import com.google.common.collect.Sets;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.customer.api.v1.domain.PayrollAllocation;
import io.mifos.customer.api.v1.domain.PayrollDistribution;
import io.mifos.customer.util.CustomerGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

public class TestPayrollDistribution extends AbstractCustomerTest {

  @Test
  public void shouldSetInitialPayrollDistribution() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier()));

    final PayrollAllocation firstPayrollAllocation = new PayrollAllocation();
    firstPayrollAllocation.setAccountNumber("08154712");
    firstPayrollAllocation.setAmount(BigDecimal.valueOf(234.56D));

    final PayrollAllocation secondPayrollAllocation = new PayrollAllocation();
    secondPayrollAllocation.setAccountNumber("08154713");
    secondPayrollAllocation.setAmount(BigDecimal.valueOf(5.00D));
    secondPayrollAllocation.setProportional(Boolean.TRUE);

    final PayrollDistribution payrollDistribution =  new PayrollDistribution();
    payrollDistribution.setMainAccountNumber("08154711");
    payrollDistribution.setPayrollAllocations(Sets.newHashSet(firstPayrollAllocation, secondPayrollAllocation));
    this.customerManager.setPayrollDistribution(customer.getIdentifier(), payrollDistribution);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.PUT_PAYROLL_DISTRIBUTION, customer.getIdentifier()));

    final PayrollDistribution fetchedPayrollDistribution = this.customerManager.getPayrollDistribution(customer.getIdentifier());

    this.compare(payrollDistribution, fetchedPayrollDistribution);
  }

  @Test
  public void shouldUpdatePayrollDistribution() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier()));

    final PayrollAllocation firstPayrollAllocation = new PayrollAllocation();
    firstPayrollAllocation.setAccountNumber("08154712");
    firstPayrollAllocation.setAmount(BigDecimal.valueOf(234.56D));

    final PayrollAllocation secondPayrollAllocation = new PayrollAllocation();
    secondPayrollAllocation.setAccountNumber("08154713");
    secondPayrollAllocation.setAmount(BigDecimal.valueOf(5.00D));
    secondPayrollAllocation.setProportional(Boolean.TRUE);

    final PayrollDistribution payrollDistribution =  new PayrollDistribution();
    payrollDistribution.setMainAccountNumber("08154711");
    payrollDistribution.setPayrollAllocations(Sets.newHashSet(firstPayrollAllocation, secondPayrollAllocation));
    this.customerManager.setPayrollDistribution(customer.getIdentifier(), payrollDistribution);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.PUT_PAYROLL_DISTRIBUTION, customer.getIdentifier()));
    this.eventRecorder.clear();

    final PayrollDistribution fetchedPayrollDistribution = this.customerManager.getPayrollDistribution(customer.getIdentifier());

    final PayrollAllocation replacedPayrollAllocation = new PayrollAllocation();
    replacedPayrollAllocation.setAccountNumber("08154714");
    replacedPayrollAllocation.setAmount(BigDecimal.valueOf(10.00D));
    replacedPayrollAllocation.setProportional(Boolean.TRUE);

    final PayrollDistribution replacedPayrollDistribution = new PayrollDistribution();
    replacedPayrollDistribution.setMainAccountNumber(fetchedPayrollDistribution.getMainAccountNumber());
    replacedPayrollDistribution.setPayrollAllocations(Sets.newHashSet(replacedPayrollAllocation));
    this.customerManager.setPayrollDistribution(customer.getIdentifier(), replacedPayrollDistribution);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.PUT_PAYROLL_DISTRIBUTION, customer.getIdentifier()));

    final PayrollDistribution updatedPayrollDistribution = this.customerManager.getPayrollDistribution(customer.getIdentifier());

    this.compare(replacedPayrollDistribution, updatedPayrollDistribution);

    final Optional<PayrollAllocation> optionalPayrollAllocation =
        updatedPayrollDistribution.getPayrollAllocations().stream().findFirst();

    final PayrollAllocation payrollAllocation = optionalPayrollAllocation.orElseThrow(IllegalStateException::new);
    Assert.assertEquals(replacedPayrollAllocation.getAccountNumber(), payrollAllocation.getAccountNumber());
    Assert.assertTrue(replacedPayrollAllocation.getAmount().compareTo(payrollAllocation.getAmount()) == 0);
    Assert.assertEquals(replacedPayrollAllocation.getProportional(), payrollAllocation.getProportional());
  }

  private void compare(final PayrollDistribution payrollDistribution, final PayrollDistribution fetchedPayrollDistribution) {
    Assert.assertEquals(payrollDistribution.getMainAccountNumber(), fetchedPayrollDistribution.getMainAccountNumber());
    Assert.assertTrue(payrollDistribution.getPayrollAllocations().size() == fetchedPayrollDistribution.getPayrollAllocations().size());
  }
}
