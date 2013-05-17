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
import java.util.List;
import soot.*;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;

public class MethodRefFinder {

  private MethodFieldFinder m_methodFieldFinder;
  
  public MethodRefFinder(){
    m_methodFieldFinder = new MethodFieldFinder();
  }
          
  public List<String> find(String curr_signature) {
    List<SootMethod> methods = (List<SootMethod>) m_methodFieldFinder.findMethod(curr_signature);
    SootMethod curr = methods.get(0);
    List<String> ret = new ArrayList<String>();
    if(curr == null || curr.isConcrete() == false){
      return ret;
    }
    Body body = curr.retrieveActiveBody();
    List<ValueBox> boxes = body.getUseAndDefBoxes();
    for(ValueBox box : boxes){
      Value value = box.getValue();
      if(value instanceof InvokeExpr){
        InvokeExpr expr = (InvokeExpr) value;
        SootMethodRef ref = expr.getMethodRef();
        String class_name = ref.declaringClass().getName();
        ret.add(class_name);
      } else if(value instanceof RefType){
        RefType ref_type = (RefType) value;
        ret.add(ref_type.getClassName());
      } else if(value instanceof FieldRef){
        FieldRef field_ref = (FieldRef) value;
        String soot_class = field_ref.getFieldRef().declaringClass().getName();
        ret.add(soot_class);
      }
    }
    return ret;
  }
  
}
