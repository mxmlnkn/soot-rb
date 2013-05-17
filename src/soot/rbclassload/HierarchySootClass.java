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
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;

import soot.coffi.ClassFile;
import soot.coffi.HierarchySootMethodFactory;
import soot.coffi.ConstantPoolReader;
import soot.coffi.field_info;

import soot.Modifier;

public class HierarchySootClass {

  private int m_className;
  private boolean m_hasSuperClass;
  private int m_superClassName;
  private List<Integer> m_interfaceNames;
  private List<HierarchyField> m_fields;
  private List<HierarchySootMethod> m_methods;
  private Map<String, HierarchySootMethod> m_covarientMethods;
  private ClassFile m_classFile;
  private int m_modifiers;
  private boolean m_isApplicationClass;
  private ConstantPoolReader m_constantReader;

  public HierarchySootClass(int class_name, boolean has_super_class, 
    int super_class_name, List<Integer> interfaces, List<HierarchyField> fields, 
    int modifiers, ClassFile class_file){

    m_className = class_name;
    m_hasSuperClass = has_super_class;
    m_superClassName = super_class_name;
    m_interfaceNames = interfaces;
    m_fields = fields;
    m_classFile = class_file;
    m_modifiers = modifiers;
    m_constantReader = new ConstantPoolReader();
  }

  public void readMethods(){
    m_methods = new ArrayList<HierarchySootMethod>();
    m_covarientMethods = new HashMap<String, HierarchySootMethod>();
    HierarchySootMethodFactory method_factory = new HierarchySootMethodFactory();
    MethodSignatureUtil util = new MethodSignatureUtil();
    for(int i = 0; i < m_classFile.methods_count; ++i){
      HierarchySootMethod method = method_factory.create(m_classFile, i);
      method.setHierarchySootClass(this);
      m_methods.add(method);

      util.parse(method.getSignature());
      m_covarientMethods.put(util.getCovarientSubSignature(), method);
    }
  }

  public int getFieldModifiers(String field_name){
    for(field_info field : m_classFile.fields){
      int name_index = field.name_index;
      String curr_name = m_constantReader.get(name_index, m_classFile.constant_pool);
      if(curr_name.equals(field_name)){
        return field.access_flags;
      }
    }
    return 0;
  }

  public String getName(){
    return RootbeerClassLoader.v().getStringNumbers().getString(m_className);
  }

  public boolean hasSuperClass(){
    return m_hasSuperClass;
  }

  public String getSuperClass(){
    return RootbeerClassLoader.v().getStringNumbers().getString(m_superClassName);
  }

  public List<String> getInterfaces(){
    List<String> ret = new ArrayList<String>();
    for(Integer iface_num : m_interfaceNames){
      ret.add(RootbeerClassLoader.v().getStringNumbers().getString(iface_num));
    }
    return ret;
  }

  public List<HierarchySootMethod> getMethods(){
    if(m_methods == null){
      readMethods();
    }
    return m_methods;
  }

  public List<HierarchyField> getFields(){
    return m_fields;
  }

  public boolean hasField(String name){
    for(HierarchyField field : m_fields){
      if(field.getName().equals(name)){
        return true;
      }
    }
    return false;
  }

  public boolean declaresCovarientSubSignature(String covarient_subsignature){
    if(m_methods == null){
      readMethods();
    }
    return m_covarientMethods.containsKey(covarient_subsignature);
  }

  public HierarchySootMethod getMethodForCovarientSubSignature(String covarient_subsignature){
    if(m_methods == null){
      readMethods();
    }
    return m_covarientMethods.get(covarient_subsignature);
  }

  public int getModifiers(){
    return m_modifiers;
  }

  public boolean isInterface(){
    return Modifier.isInterface(m_modifiers);
  }

  public void setApplicationClass(boolean value){
    m_isApplicationClass = value;
  }

  public boolean isApplicationClass(){
    return m_isApplicationClass;
  }

  public HierarchySootMethod findMethodByName(String name){
    for(HierarchySootMethod method : getMethods()){
      if(method.getName().equals(name)){
        return method;
      }
    }
    return null;
  }

  public HierarchySootMethod findMethodBySubSignature(String sub_sig){
    for(HierarchySootMethod method : getMethods()){
      if(method.getSubSignature().equals(sub_sig)){
        return method;
      }
    }
    return null;
  }
}
