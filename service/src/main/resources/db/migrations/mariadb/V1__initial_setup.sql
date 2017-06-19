--
-- Copyright 2017 The Mifos Initiative.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE maat_addresses (
  id BIGINT NOT NULL AUTO_INCREMENT,
  street VARCHAR(256) NOT NULL,
  city VARCHAR(256) NOT NULL,
  postal_code VARCHAR(32) NULL,
  region VARCHAR(256) NULL,
  country_code VARCHAR(2) NOT NULL,
  country VARCHAR(256) NOT NULL,
  CONSTRAINT maat_addresses_pk PRIMARY KEY (id)
);

CREATE TABLE maat_customers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  identifier VARCHAR(32) NOT NULL,
  a_type VARCHAR(32) NOT NULL,
  given_name VARCHAR(256) NOT NULL,
  middle_name VARCHAR(256) NULL,
  surname VARCHAR(256) NOT NULL,
  date_of_birth DATE NOT NULL,
  account_beneficiary VARCHAR(512) NULL,
  reference_customer VARCHAR(32) NULL,
  assigned_office VARCHAR(32) NULL,
  assigned_employee VARCHAR(32) NULL,
  current_state VARCHAR(32) NOT NULL,
  address_id BIGINT NOT NULL,
  created_by VARCHAR(32) NULL,
  created_on TIMESTAMP(3) null,
  last_modified_by VARCHAR(32) NULL,
  last_modified_on TIMESTAMP(3) NULL,
  CONSTRAINT maat_customers_pk PRIMARY KEY (id),
  CONSTRAINT maat_customer_identifier_uq UNIQUE (identifier),
  CONSTRAINT maat_customers_addresses_fk FOREIGN KEY (address_id) REFERENCES maat_addresses (id)
);

CREATE TABLE maat_identification_cards (
  id BIGINT NOT NULL AUTO_INCREMENT,
  a_type VARCHAR(128) NOT NULL,
  customer_id BIGINT NOT NULL,
  a_number VARCHAR(32) NOT NULL,
  expiration_date DATE NOT NULL,
  issuer VARCHAR(256) NULL,
  CONSTRAINT maat_identification_cards_pk PRIMARY KEY (id),
  CONSTRAINT maat_id_cards_customers_fk FOREIGN KEY (customer_id) REFERENCES maat_customers (id) ON UPDATE RESTRICT
);

CREATE TABLE maat_contact_details (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  a_type VARCHAR(32) NOT NULL,
  a_group VARCHAR(256) NOT NULL,
  a_value VARCHAR(32) NOT NULL,
  preference_level TINYINT NULL,
  validated BOOLEAN NULL,
  CONSTRAINT maat_contact_details_pk PRIMARY KEY (id),
  CONSTRAINT maat_contact_details_cust_fk FOREIGN KEY (customer_id) REFERENCES maat_customers (id) ON UPDATE RESTRICT
);

CREATE TABLE maat_commands (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  a_type VARCHAR(32) NOT NULL,
  a_comment VARCHAR(32) NULL,
  created_by VARCHAR(32) NOT NULL,
  created_on TIMESTAMP(3) NULL,
  CONSTRAINT maat_commands_pk PRIMARY KEY (id),
  CONSTRAINT maat_commands_customers_fk FOREIGN KEY (customer_id) REFERENCES maat_customers (id) ON UPDATE RESTRICT
);

CREATE TABLE maat_task_definitions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  identifier VARCHAR(32) NOT NULL,
  a_type VARCHAR(32) NOT NULL,
  a_name VARCHAR(256) NOT NULL,
  description VARCHAR(4096) NULL,
  assigned_commands VARCHAR(512) NOT NULL,
  mandatory BOOLEAN NULL,
  predefined BOOLEAN NULL,
  CONSTRAINT maat_task_definitions_pk PRIMARY KEY (id),
  CONSTRAINT maat_task_def_identifier_uq UNIQUE (identifier)
);

CREATE TABLE maat_task_instances (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_definition_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  a_comment VARCHAR(4096) NULL,
  executed_on TIMESTAMP(3) NULL,
  executed_by VARCHAR(32) NULL,
  CONSTRAINT maat_task_instances_pk PRIMARY KEY (id),
  CONSTRAINT maat_task_instances_def_fk FOREIGN KEY (task_definition_id) REFERENCES maat_task_definitions (id) ON UPDATE RESTRICT,
  CONSTRAINT maat_task_instances_cust_fk FOREIGN KEY (customer_id) REFERENCES maat_customers (id) ON UPDATE RESTRICT
);

CREATE TABLE nun_catalogs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  identifier VARCHAR(32) NOT NULL,
  a_name VARCHAR(256) NOT NULL,
  description VARCHAR(4096) NULL,
  created_by VARCHAR(32) NULL,
  created_on TIMESTAMP(3) null,
  last_modified_by VARCHAR(32) NULL,
  last_modified_on TIMESTAMP(3) NULL,
  CONSTRAINT nun_catalogs_pk PRIMARY KEY (id),
  CONSTRAINT nun_catalogs_identifier_uq UNIQUE (identifier)
);

CREATE TABLE nun_fields (
  id BIGINT NOT NULL AUTO_INCREMENT,
  catalog_id BIGINT NOT NULL,
  identifier VARCHAR(32) NOT NULL,
  data_type VARCHAR(256) NOT NULL,
  a_label VARCHAR(256) NOT NULL,
  a_hint VARCHAR(512) NULL,
  description VARCHAR(4096) NULL,
  mandatory BOOLEAN NULL,
  a_length BIGINT NULL,
  a_precision BIGINT NULL,
  min_value BIGINT NULL,
  max_value BIGINT NULL,
  created_by VARCHAR(32) NULL,
  created_on TIMESTAMP(3) null,
  CONSTRAINT nun_fields_pk PRIMARY KEY (id),
  CONSTRAINT nun_fields_uq UNIQUE (catalog_id, identifier),
  CONSTRAINT nun_fields_catalogs_fk FOREIGN KEY (catalog_id) REFERENCES nun_catalogs (id)
);

CREATE TABLE nun_options (
  id BIGINT NOT NULL AUTO_INCREMENT,
  field_id BIGINT NOT NULL,
  a_label VARCHAR(256) NOT NULL,
  a_value BIGINT NOT NULL,
  created_by VARCHAR(32) NULL,
  created_on TIMESTAMP(3) null,
  CONSTRAINT nun_options_pk PRIMARY KEY (id),
  CONSTRAINT nun_options_uq UNIQUE (field_id, a_label),
  CONSTRAINT nun_options_fields_fk FOREIGN KEY (field_id) REFERENCES nun_fields (id)
);

CREATE TABLE nun_field_values (
  id BIGINT NOT NULL AUTO_INCREMENT,
  entity_id BIGINT NOT NULL,
  field_id BIGINT NOT NULL,
  a_value VARCHAR(4096) NOT NULL,
  CONSTRAINT nun_field_values_pk PRIMARY KEY (id),
  CONSTRAINT nun_field_values_uq UNIQUE (entity_id, field_id),
  CONSTRAINT nun_field_values_entities_fk FOREIGN KEY (entity_id) REFERENCES maat_customers (id),
  CONSTRAINT nun_field_values_fields_fk FOREIGN KEY (field_id) REFERENCES nun_fields (id)
);

