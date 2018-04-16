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
package org.apache.fineract.cn.customer.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

  @Query("SELECT d FROM DocumentEntity d WHERE d.customer.identifier = :customerIdentifier AND d.identifier = :documentIdentifier")
  Optional<DocumentEntity> findByCustomerIdAndDocumentIdentifier(
      @Param("customerIdentifier") String customerIdentifier, @Param("documentIdentifier") String documentIdentifier);

  @Query("SELECT d FROM DocumentEntity d WHERE d.customer.identifier = :customerIdentifier")
  Stream<DocumentEntity> findByCustomerId(
      @Param("customerIdentifier") String customerIdentifier);
}
