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
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class HierarchyValueSwitch {

  private Set<String> m_refTypes;
  private Set<String> m_arrayTypes;
  private Set<String> m_allTypes;
  private Set<String> m_methodRefs;
  private Set<String> m_fieldRefs;
  private Set<String> m_instanceofs;
  private Set<String> m_newInvokes;
  private MethodSignatureUtil m_methodUtil;
  private FieldSignatureUtil m_fieldUtil;
  private StringToType m_stringToType;
  private HierarchySootMethod m_method;

  public HierarchyValueSwitch(){
    m_refTypes = new HashSet<String>();
    m_arrayTypes = new HashSet<String>();
    m_allTypes = new HashSet<String>();
    m_methodRefs = new HashSet<String>();
    m_fieldRefs = new HashSet<String>();
    m_instanceofs = new HashSet<String>();
    m_newInvokes = new HashSet<String>();
    m_methodUtil = new MethodSignatureUtil();
    m_fieldUtil = new FieldSignatureUtil();
    m_stringToType = new StringToType();
  }

  public Set<String> getRefTypes(){
    return m_refTypes;
  }

  public Set<String> getAllTypes(){
    return m_allTypes;
  }

  public Set<String> getArrayTypes(){
    return m_arrayTypes;
  }

  public Set<String> getMethodRefs(){
    return m_methodRefs;
  }

  public Set<String> getFieldRefs(){
    return m_fieldRefs;
  }

  public Set<String> getInstanceOfs(){
    return m_instanceofs;
  }

  public Set<String> getNewInvokes(){
    return m_newInvokes;
  }

  public void run(String signature){
    ClassHierarchy class_hierarchy = RootbeerClassLoader.v().getClassHierarchy();
    HierarchySootMethod method = class_hierarchy.getHierarchySootMethod(signature);
    if(method == null){
      return;
    }
    m_method = method;

    HierarchySootClass hclass = method.getHierarchySootClass();

    if(method.isConcrete() == false){
      return;
    }

    addHierarchy(hclass.getName());
    addSignature(method);
    
    List<HierarchyInstruction> instructions = method.getInstructions();
    for(HierarchyInstruction inst : instructions){
      addInstruction(inst);
    }
    
    List<String> ex_types = method.getCodeAttrExTypes();
    for(String ex_type : ex_types){
      addHierarchy(ex_type);
    }
  }

  /**
   * type can be ArrayType, RefType or PrimType
   */
  private void addHierarchy(String type){
    ClassHierarchy class_hierarchy = RootbeerClassLoader.v().getClassHierarchy();
    LinkedList<String> hierarchy_queue = new LinkedList<String>();
    hierarchy_queue.add(type);
    while(hierarchy_queue.isEmpty() == false){
      String class_name = hierarchy_queue.removeFirst();
      addRefType(class_name);
      
      if(m_stringToType.isArrayType(class_name)){
        addArrayType(class_name);
        class_name = m_stringToType.getBaseType(class_name);
        addRefType(class_name);
      }
      if(m_stringToType.isRefType(class_name) == false){
        continue;
      }

      Set<String> existing_news = RootbeerClassLoader.v().getNewInvokes();
      if(existing_news.contains(class_name) == false){
        continue;
      }
  
      HierarchySootClass curr_hclass = class_hierarchy.getHierarchySootClass(class_name);
      if(curr_hclass == null){
        System.out.println("  curr_hclass == null: "+class_name);
        continue;
      }
      if(curr_hclass.hasSuperClass()){
        hierarchy_queue.add(curr_hclass.getSuperClass());
      }
      hierarchy_queue.addAll(curr_hclass.getInterfaces());
    }
  }

  private void addRefType(String type){
    m_refTypes.add(type);
    m_allTypes.add(type);
  }

  private void addArrayType(String type){
    m_arrayTypes.add(type);
    m_allTypes.add(type);
  }

  private void addSignature(HierarchySootMethod method){
    addHierarchy(method.getReturnType());
    for(String param : method.getParameterTypes()){
      addHierarchy(param);
    }
    for(String except : method.getExceptionTypes()){
      addHierarchy(except);
    }
  }
  
  private void addInstruction(HierarchyInstruction inst){
    addInstructionName(inst);
    addInstructionOperands(inst);
  }

  private void addInstructionName(HierarchyInstruction inst){
    String name = inst.getName();
    if(name.equals("anewarray")){
      addNewInvoke(inst);
    } else if(name.equals("instanceof")){
      addInstanceOf(inst);
    } else if(name.equals("multianewarray")){
      addNewInvoke(inst);
    } else if(name.equals("newarray")){
      addNewInvoke(inst);
    } else if(name.equals("new")){
      addNewInvoke(inst);
    }
  }

  private void addInstructionOperands(HierarchyInstruction inst){
    List<Operand> operands = inst.getOperands();
    for(Operand operand : operands){
      String value = operand.getValue();
      String type = operand.getType();

      if(type.equals("class_ref")){
        addHierarchy(value);
      } else if(type.equals("method_ref")){
        m_methodRefs.add(value);
        m_methodUtil.parse(value);
        addHierarchy(m_methodUtil.getClassName());
        addHierarchy(m_methodUtil.getReturnType());
        for(String param : m_methodUtil.getParameterTypes()){
          addHierarchy(param);
        }
      } else if(type.equals("field_ref")){
        m_fieldRefs.add(value);
        m_fieldUtil.parse(value);
        addHierarchy(m_fieldUtil.getDeclaringClass());
        addHierarchy(m_fieldUtil.getType());
      } 
    }
  }

  private void addNewInvoke(HierarchyInstruction inst){
    List<Operand> operands = inst.getOperands();
    for(Operand operand : operands){
      String value = operand.getValue();
      String type = operand.getType();

      if(type.equals("class_ref")){
        m_newInvokes.add(value);
      }
    }
  }

  private void addInstanceOf(HierarchyInstruction inst){
    List<Operand> operands = inst.getOperands();
    for(Operand operand : operands){
      String value = operand.getValue();
      String type = operand.getType();

      if(type.equals("class_ref")){
        m_instanceofs.add(value);
        addHierarchy(value);
      }
    }
  }
}
