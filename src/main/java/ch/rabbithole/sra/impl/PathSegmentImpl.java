/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package ch.rabbithole.sra.impl;

import org.apache.cxf.common.util.UrlUtils.UrlUtils;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.ext.Provider;

@Provider
public class PathSegmentImpl implements PathSegment {

  private String path;

  public PathSegmentImpl(String path) {
    this(path, true);
  }

  public PathSegmentImpl(String path, boolean decode) {
    this.path = decode ? UrlUtils.pathDecode(path) : path;
  }

  public MultivaluedMap<String, String> getMatrixParameters() {
    throw new UnsupportedOperationException();
  }

  public String getPath() {
    int index = path.indexOf(';');
    String value = index != -1 ? path.substring(0, index) : path;
    if (value.startsWith("/")) {
      value = value.length() == 1 ? "" : value.substring(1);
    }
    return value;
  }

  public String toString() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PathSegmentImpl that = (PathSegmentImpl) o;

    return !(path != null ? !path.equals(that.path) : that.path != null);

  }

  @Override
  public int hashCode() {
    return path != null ? path.hashCode() : 0;
  }
}