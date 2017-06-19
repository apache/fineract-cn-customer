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
package io.mifos.customer.util;

import io.mifos.customer.api.v1.domain.ContactDetail;
import org.apache.commons.lang3.RandomStringUtils;

public final class ContactDetailGenerator {

  private ContactDetailGenerator() {
    super();
  }

  public static ContactDetail createRandomContactDetail() {
    final ContactDetail contactDetail = new ContactDetail();
    contactDetail.setType(ContactDetail.Type.MOBILE.name());
    contactDetail.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetail.setValue(RandomStringUtils.randomAlphanumeric(32));
    contactDetail.setPreferenceLevel(Integer.valueOf(1));
    contactDetail.setValidated(Boolean.FALSE);
    return contactDetail;
  }
}
