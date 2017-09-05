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
package io.mifos.customer.service.internal.mapper;

import io.mifos.core.lang.DateConverter;
import io.mifos.customer.api.v1.domain.PayrollAllocation;
import io.mifos.customer.api.v1.domain.PayrollDistribution;
import io.mifos.customer.service.internal.repository.PayrollDistributionEntity;

import java.util.stream.Collectors;

public class PayrollDistributionMapper {

  private PayrollDistributionMapper() {
    super();
  }

  public static PayrollDistribution map(final PayrollDistributionEntity payrollDistributionEntity) {
    final PayrollDistribution payrollDistribution = new PayrollDistribution();
    payrollDistribution.setMainAccountNumber(payrollDistributionEntity.getMainAccountNumber());
    payrollDistribution.setCreatedBy(payrollDistributionEntity.getCreatedBy());
    payrollDistribution.setCreatedOn(DateConverter.toIsoString(payrollDistributionEntity.getCreatedOn()));
    if (payrollDistributionEntity.getLastModifiedBy() != null) {
      payrollDistribution.setLastModifiedBy(payrollDistributionEntity.getLastModifiedBy());
      payrollDistribution.setLastModifiedOn(DateConverter.toIsoString(payrollDistributionEntity.getLastModifiedOn()));
    }

    if (payrollDistributionEntity.getPayrollAllocationEntities() != null) {
      payrollDistribution.setPayrollAllocations(
          payrollDistributionEntity.getPayrollAllocationEntities().stream().map(payrollAllocationEntity -> {
            final PayrollAllocation payrollAllocation = new PayrollAllocation();
            payrollAllocation.setAccountNumber(payrollAllocationEntity.getAccountNumber());
            payrollAllocation.setAmount(payrollAllocationEntity.getAmount());
            payrollAllocation.setProportional(payrollAllocationEntity.getProportional());
            return payrollAllocation;
          }).collect(Collectors.toSet())
      );
    }

    return payrollDistribution;
  }
}
