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
import soot.SootField;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.Set;
import java.util.HashSet;

public class SubScene {

  private Set<String> m_methodSignatures;
  private Set<Type> m_allTypes;
  private Set<String> m_classes;

  public SubScene(){
    m_methodSignatures = new HashSet<String>();
    m_allTypes = new HashSet<Type>();
    m_classes = new HashSet<String>();
  }

  public boolean containsMethod(String signature){
    return m_methodSignatures.contains(signature);
  }

  public void addMethod(String signature){
    m_methodSignatures.add(signature);
  }

  public boolean containsType(Type type){
    return m_allTypes.contains(type);
  }

  public void addType(Type type){
    m_allTypes.add(type);
  }

  public void addInstanceOf(Type type){

  }

  public void addField(SootField field){

  }

  public void addCallGraphEdge(SootMethod src, Stmt stmt, SootMethod dest){

  }

  public void addSuperClass(Type curr, Type superclass) {

  }

  public boolean containsClass(String name){
    return m_classes.contains(name);
  }

  public void addClass(String name){
    m_classes.add(name);
  }
}
