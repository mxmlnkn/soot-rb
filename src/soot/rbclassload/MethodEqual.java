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

import soot.Type;
import java.util.List;

public class MethodEqual {

  public boolean exceptReturnType(String lhs, String rhs){
    MethodSignatureUtil lhs_util = new MethodSignatureUtil(lhs);
    MethodSignatureUtil rhs_util = new MethodSignatureUtil(rhs);

    if(lhs_util.getMethodName().equals(rhs_util.getMethodName()) == false){
      return false;
    }
    return typesEqual(lhs_util.getParameterTypesTyped(), rhs_util.getParameterTypesTyped());
  }

  private boolean typesEqual(List<Type> types1, List<Type> types2) {
    for(int i = 0; i < types1.size(); ++i){
      Type lhs = types1.get(i);
      Type rhs = types2.get(i);
      if(lhs.equals(rhs) == false){
        return false;
      }
    }
    return true;
  }
}
