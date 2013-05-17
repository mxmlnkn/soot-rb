/* Soot - a J*va Optimization Framework
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* The soot.rbclassload package is:
 * Copyright (C) 2012 Phil Pratt-Szeliga
 * Copyright (C) 2012 Marc-Andre Laverdiere-Papineau
 */

package soot.rbclassload;

import java.util.*;
import soot.ArrayType;
import soot.Type;

public class MultiDimensionalArrayTypeCreator {

  public MultiDimensionalArrayTypeCreator(){
  }

  public Set<String> createString(Set<String> types){
    StringToType converter = new StringToType();
    Set<String> ret = new HashSet<String>();
    for(String type_str : types){
      ArrayType type = (ArrayType) converter.convert(type_str);
      Type base_type = type.baseType;
      int dim = type.numDimensions;
      for(int i = dim; i > 0; --i){
        ArrayType curr = ArrayType.v(base_type, i);
        ret.add(curr.toString());
      }
    }
    return ret;
  }

  public Set<ArrayType> createType(Set<ArrayType> types){
    Set<String> input = new HashSet<String>();
    for(ArrayType array_type : types){
      input.add(array_type.toString());
    }
    Set<String> output = createString(input);
    StringToType converter = new StringToType();
    Set<ArrayType> ret = new HashSet<ArrayType>();
    for(String output_str : output){
      ArrayType type = (ArrayType) converter.convert(output_str);
      ret.add(type);
    }
    return ret;
  }
}
