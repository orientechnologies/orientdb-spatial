/**
 * Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information: http://www.orientdb.com
 */
package com.orientechnologies.spatial.shape;

import org.locationtech.spatial4j.shape.Shape;

/**
 * Created by Enrico Risa on 29/09/15.
 */
public interface OShapeOperation {

  public double distance(Shape shapes1, Shape s2);

  public boolean isWithInDistance(Shape s1, Shape s2, Double dist);

  public boolean intersect(Shape s1, Shape s2);
}
