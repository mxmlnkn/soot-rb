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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import soot.Type;

public class TypeHierarchySorter {
  
  public List<Type> sort(List<Type> input){
    TypeHierarchySorterValue[] type_array = new TypeHierarchySorterValue[input.size()];
    for(int i = 0; i < input.size(); ++i){
      type_array[i] = new TypeHierarchySorterValue(input.get(i));
    }
    Arrays.sort(type_array);
    List<Type> ret = new ArrayList<Type>();
    for(TypeHierarchySorterValue curr : type_array){
      Type curr_type = curr.getType();
      if(ret.contains(curr_type) == false){
        ret.add(curr_type);
      }
    }
    return ret;
  }
  
  
}
