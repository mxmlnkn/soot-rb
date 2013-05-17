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

import java.util.List;
import java.util.ArrayList;

import soot.Type;
import soot.RefType;

public class References {

  private String m_super;
  private List<Type> m_interfaces;
  private List<Type> m_fields;
  private List<MethodReference> m_methods;
  private List<Type> m_annotations;
  private List<Type> m_all;

  public References(){
    m_interfaces = new ArrayList<Type>();
    m_fields = new ArrayList<Type>();
    m_methods = new ArrayList<MethodReference>();
    m_annotations = new ArrayList<Type>();
    m_all = new ArrayList<Type>();
  }

  public void setSuperClass(String super_name){
    m_super = super_name;
  }

  public void addInterface(String name){
    m_interfaces.add(RefType.v(name));
  }

  public void addField(Type type){
    m_fields.add(type);
  }

  public void addMethod(MethodReference method){
    m_methods.add(method);
  }

  public void addAnnotation(String name){
    m_annotations.add(RefType.v(name));
  }

  public void addOther(String name){
    m_all.add(RefType.v(name));
  }

  public void addOther(Type type){
    m_all.add(type);
  }

  public List<Type> getHierarchy(){
    List<Type> ret = new ArrayList<Type>();
    if(m_super != null){
      ret.add(RefType.v(m_super));
    }
    ret.addAll(m_interfaces);
    return ret;
  }

  public List<Type> getSignatures(){
    List<Type> ret = new ArrayList<Type>();
    ret.addAll(getHierarchy());
    for(MethodReference method : m_methods){
      ret.addAll(method.getSignatures());
    }
    return ret;
  }

  public List<Type> getBody(){
    List<Type> ret = new ArrayList<Type>();
    ret.addAll(getSignatures());
    ret.addAll(m_all);
    return ret;
  }

  public List<Type> getAll(){
    return getBody();
  }
}
