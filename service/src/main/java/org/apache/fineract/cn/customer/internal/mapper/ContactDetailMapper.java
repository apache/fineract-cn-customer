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
package org.apache.fineract.cn.customer.internal.mapper;

import org.apache.fineract.cn.customer.api.v1.domain.ContactDetail;
import org.apache.fineract.cn.customer.internal.repository.ContactDetailEntity;

public final class ContactDetailMapper {

  private ContactDetailMapper() {
    super();
  }

  public static ContactDetailEntity map(final ContactDetail contactDetail) {
    final ContactDetailEntity contactDetailEntity = new ContactDetailEntity();
    contactDetailEntity.setType(contactDetail.getType());
    contactDetailEntity.setGroup(contactDetail.getGroup());
    contactDetailEntity.setValue(contactDetail.getValue());
    contactDetailEntity.setPreferenceLevel(contactDetail.getPreferenceLevel());
    contactDetailEntity.setValid(contactDetail.getValidated());
    return contactDetailEntity;
  }

  public static ContactDetail map(final ContactDetailEntity contactDetailEntity) {
    final ContactDetail contactDetail = new ContactDetail();
    contactDetail.setType(contactDetailEntity.getType());
    contactDetail.setGroup(contactDetailEntity.getGroup());
    contactDetail.setValue(contactDetailEntity.getValue());
    contactDetail.setPreferenceLevel(contactDetailEntity.getPreferenceLevel());
    contactDetail.setValidated(contactDetailEntity.getValid());
    return contactDetail;
  }
}
