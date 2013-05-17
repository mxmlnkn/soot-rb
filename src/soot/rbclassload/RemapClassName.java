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

import soot.options.Options;
import soot.SootClass;
import soot.Scene;
import soot.Type;
import soot.ArrayType;
import soot.RefType;

public class RemapClassName {

  private BuiltInRemaps m_builtIns;
  private StringCallGraph m_stringCG;

  public RemapClassName(){
    m_builtIns = new BuiltInRemaps();
    m_stringCG = null;
  }

  public boolean shouldMap(SootClass soot_class){
    String class_name = soot_class.getName();
    return shouldMap(class_name);
  }

  public boolean shouldMap(String class_name){
    String prefix = Options.v().rbcl_remap_prefix();
    if(m_builtIns.containsKey(class_name)){
      return true;
    }    
    if(isLibraryClass(class_name) == false){
      return false;
    }
    if(class_name.contains(prefix)){
      return false;
    }    
    if(Options.v().rbcl_remap_all()){
      return true;   
    } else {
      return false;
    }
  }

  private boolean isLibraryClass(String class_name){
    //delayed load to stop singleton init stack overflow
    if(m_stringCG == null){ 
      m_stringCG = RootbeerClassLoader.v().getDfsInfo().getStringCallGraph();
    }
    return m_stringCG.isLibraryClass(class_name);
  }

  public Type getMapping(Type type){
    if(type instanceof ArrayType){
      ArrayType array_type = (ArrayType) type;
      Type new_base = getMapping(array_type.baseType);
      return ArrayType.v(new_base, array_type.numDimensions);
    } else if(type instanceof RefType){
      RefType ref_type = (RefType) type;
      String class_name = ref_type.getClassName();
      class_name = getMapping(class_name);
      return RefType.v(class_name);      
    } else {
      return type;
    }
  }

  public SootClass getMapping(SootClass soot_class){
    String class_name = soot_class.getName();
    class_name = getMapping(class_name);
    return Scene.v().getSootClass(class_name);
  }

  public String getMapping(String class_name){
    String ret = "";    
    if(shouldMap(class_name)){
      if(m_builtIns.containsKey(class_name)){
        ret = m_builtIns.get(class_name);
      } else {
        String prefix = Options.v().rbcl_remap_prefix();
        ret = prefix + class_name;
      }
    } else {
      ret = class_name;
    }
    return ret;
  }
}
