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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import soot.Body;
import soot.Modifier;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class CloneClass {
  
  public SootClass execute(SootClass soot_class, String new_class_name, StringCallGraph string_cg, Set<String> reachable_fields) {

    SootClass ret = new SootClass(new_class_name, Modifier.PUBLIC);
    List<SootMethod> methods = soot_class.getMethods();
    for(SootMethod method : methods){
      String sig = method.getSignature();
      if(string_cg.isReachable(sig) == false){
        continue;
      }
      
      SootMethod new_method = new SootMethod(method.getName(), method.getParameterTypes(), method.getReturnType(), method.getModifiers(), method.getExceptions());
      if(method.isConcrete()){
        Body body = method.retrieveActiveBody();
        new_method.setActiveBody((Body) body.clone());
      }
      ret.addMethod(new_method);
    }
    Iterator<SootField> iter = soot_class.getFields().iterator();
    while(iter.hasNext()){
      SootField next = iter.next();
      String field_sig = next.getSignature();
      if(reachable_fields.contains(field_sig) == false){
        continue;
      }
      SootField cloned = new SootField(next.getName(), next.getType(), next.getModifiers());
      ret.addField(cloned);
    }
    Iterator<SootClass> iter2 = soot_class.getInterfaces().iterator();
    while(iter2.hasNext()){
      SootClass next = iter2.next();
      ret.addInterface(next);
    }
    if(soot_class.hasSuperclass()){
      ret.setSuperclass(soot_class.getSuperclass());
    }
    if(soot_class.hasOuterClass()){
      ret.setOuterClass(soot_class.getOuterClass());
    }
    return ret;
  }
}
