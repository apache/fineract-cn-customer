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

CREATE TABLE maat_portraits (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  content_type VARCHAR(256) NOT NULL,
  size BIGINT NOT NULL,
  image MEDIUMBLOB NOT NULL,
  CONSTRAINT maat_portraits_pk PRIMARY KEY (id),
  CONSTRAINT maat_id_portraits_customers_fk FOREIGN KEY (customer_id) REFERENCES maat_customers (id) ON UPDATE RESTRICT
);

ALTER TABLE maat_identification_cards ADD created_by VARCHAR(32) NULL;
ALTER TABLE maat_identification_cards ADD created_on TIMESTAMP(3) NULL;
ALTER TABLE maat_identification_cards ADD last_modified_by VARCHAR(32) NULL;
ALTER TABLE maat_identification_cards ADD last_modified_on TIMESTAMP(3) NULL;
