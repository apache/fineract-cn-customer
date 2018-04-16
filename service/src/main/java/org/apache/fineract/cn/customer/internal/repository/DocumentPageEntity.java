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

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@Entity
@Table(name = "maat_document_pages")
public class DocumentPageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "document_id")
  private DocumentEntity document;

  @Column(name = "page_number")
  private Integer pageNumber;

  @Column(name = "content_type")
  private String contentType;


  @Column(name = "size")
  private Long size;

  @Lob
  @Column(name = "image")
  private byte[] image;

  public DocumentPageEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @SuppressWarnings("unused")
  public DocumentEntity getDocument() {
    return document;
  }

  public void setDocument(DocumentEntity document) {
    this.document = document;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber) {
    this.pageNumber = pageNumber;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DocumentPageEntity that = (DocumentPageEntity) o;
    return Objects.equals(document, that.document) &&
        Objects.equals(pageNumber, that.pageNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(document, pageNumber);
  }

  @Override
  public String toString() {
    return "DocumentPageEntity{" +
        "id=" + id +
        ", document=" + document +
        ", pageNumber=" + pageNumber +
        ", contentType='" + contentType + '\'' +
        ", size=" + size +
        ", image=" + Arrays.toString(image) +
        '}';
  }
}
