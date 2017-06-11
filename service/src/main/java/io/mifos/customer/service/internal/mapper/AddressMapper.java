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

import io.mifos.customer.api.v1.domain.Address;
import io.mifos.customer.service.internal.repository.AddressEntity;

public final class AddressMapper {

  private AddressMapper() {
    super();
  }

  public static AddressEntity map(final Address address) {
    final AddressEntity addressEntity = new AddressEntity();
    addressEntity.setStreet(address.getStreet());
    addressEntity.setCity(address.getCity());
    addressEntity.setPostalCode(address.getPostalCode());
    addressEntity.setRegion(address.getRegion());
    addressEntity.setCountryCode(address.getCountryCode());
    addressEntity.setCountry(address.getCountry());
    return addressEntity;
  }

  public static Address map(final AddressEntity addressEntity) {
    final Address address = new Address();
    address.setStreet(addressEntity.getStreet());
    address.setCity(addressEntity.getCity());
    address.setPostalCode(addressEntity.getPostalCode());
    address.setRegion(addressEntity.getRegion());
    address.setCountryCode(addressEntity.getCountryCode());
    address.setCountry(addressEntity.getCountry());
    return address;
  }
}
