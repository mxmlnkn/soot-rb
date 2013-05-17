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

import soot.*;

public class ClassConstantReader {

  public Type stringToType(String value) {
    int dims = 0;
    while(value.charAt(0) == '['){
      dims++;
      value = value.substring(1);
    }
    if(dims != 0 && value.charAt(0) == 'L'){
      value = value.substring(1, value.length()-1);       
    }
    value = value.replace("/", ".");
    Type base_type = getType(value);
    if(dims != 0){
      return ArrayType.v(base_type, dims);
    } else {
      return base_type;
    }
  }
  
  private Type getType(String constant) {
    if(constant.equals("Z")){
      return BooleanType.v();
    } else if(constant.equals("B")){
      return ByteType.v();
    } else if(constant.equals("S")){
      return ShortType.v();
    } else if(constant.equals("C")){
      return CharType.v();
    } else if(constant.equals("I")){
      return IntType.v();
    } else if(constant.equals("J")){
      return LongType.v();
    } else if(constant.equals("F")){
      return FloatType.v();
    } else if(constant.equals("D")){
      return DoubleType.v();
    } else {
      return RefType.v(constant);
    }
  }
}
