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
package io.mifos.customer.api.v1;

@SuppressWarnings("unused")
public interface CustomerEventConstants {

  String DESTINATION = "customer-v1";

  String SELECTOR_NAME = "action";

  String INITIALIZE = "initialize";

  String POST_CUSTOMER = "post-customer";
  String PUT_CUSTOMER = "put-customer";
  String PUT_ADDRESS = "put-address";
  String PUT_CONTACT_DETAILS = "put-contact-details";
  String POST_IDENTIFICATION_CARD = "post-identification-card";
  String PUT_IDENTIFICATION_CARD = "put-identification-card";
  String DELETE_IDENTIFICATION_CARD = "delete-identification-card";

  String ACTIVATE_CUSTOMER = "activate-customer";
  String LOCK_CUSTOMER = "lock-customer";
  String UNLOCK_CUSTOMER = "unlock-customer";
  String CLOSE_CUSTOMER = "close-customer";
  String REOPEN_CUSTOMER = "reopen-customer";

  String POST_TASK = "post-task";
  String PUT_TASK = "put-task";

  String POST_PORTRAIT = "post-portrait";
  String DELETE_PORTRAIT = "delete-portrait";

  String SELECTOR_INITIALIZE = SELECTOR_NAME + " = '" + INITIALIZE + "'";

  String SELECTOR_POST_CUSTOMER = SELECTOR_NAME + " = '" + POST_CUSTOMER + "'";
  String SELECTOR_PUT_CUSTOMER = SELECTOR_NAME + " = '" + PUT_CUSTOMER + "'";
  String SELECTOR_PUT_ADDRESS = SELECTOR_NAME + " = '" + PUT_ADDRESS + "'";
  String SELECTOR_PUT_CONTACT_DETAILS = SELECTOR_NAME + " = '" + PUT_CONTACT_DETAILS + "'";

  String SELECTOR_POST_IDENTIFICATION_CARD = SELECTOR_NAME + " = '" + POST_IDENTIFICATION_CARD + "'";
  String SELECTOR_PUT_IDENTIFICATION_CARD = SELECTOR_NAME + " = '" + PUT_IDENTIFICATION_CARD + "'";
  String SELECTOR_DELETE_IDENTIFICATION_CARD = SELECTOR_NAME + " = '" + DELETE_IDENTIFICATION_CARD + "'";

  String SELECTOR_ACTIVATE_CUSTOMER = SELECTOR_NAME + " = '" + ACTIVATE_CUSTOMER + "'";
  String SELECTOR_LOCK_CUSTOMER = SELECTOR_NAME + " = '" + LOCK_CUSTOMER + "'";
  String SELECTOR_UNLOCK_CUSTOMER = SELECTOR_NAME + " = '" + UNLOCK_CUSTOMER + "'";
  String SELECTOR_CLOSE_CUSTOMER = SELECTOR_NAME + " = '" + CLOSE_CUSTOMER + "'";
  String SELECTOR_REOPEN_CUSTOMER = SELECTOR_NAME + " = '" + REOPEN_CUSTOMER + "'";

  String SELECTOR_POST_TASK = SELECTOR_NAME + " = '" + POST_TASK + "'";
  String SELECTOR_PUT_TASK = SELECTOR_NAME + " = '" + PUT_TASK + "'";

  String SELECTOR_PUT_PORTRAIT = SELECTOR_NAME + " = '" + POST_PORTRAIT + "'";
  String SELECTOR_DELETE_PORTRAIT = SELECTOR_NAME + " = '" + DELETE_PORTRAIT + "'";
}
