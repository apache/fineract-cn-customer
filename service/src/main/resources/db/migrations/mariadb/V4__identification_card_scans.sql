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

CREATE TABLE maat_identification_card_scans (
  id BIGINT NOT NULL AUTO_INCREMENT,
  identifier VARCHAR(32) NOT NULL,
  description VARCHAR(4096) NOT NULL,
  identification_card_id BIGINT NOT NULL,
  content_type VARCHAR(256) NOT NULL,
  size BIGINT NOT NULL,
  image MEDIUMBLOB NOT NULL,
  created_on TIMESTAMP(3) NOT NULL,
  created_by VARCHAR(32) NOT NULL,
  CONSTRAINT maat_ident_card_scans_pk PRIMARY KEY (id),
  CONSTRAINT maat_ident_card_scans_ident_uq UNIQUE (identifier, identification_card_id),
  CONSTRAINT maat_ident_card_scans_fk FOREIGN KEY (identification_card_id) REFERENCES maat_identification_cards (id)
);