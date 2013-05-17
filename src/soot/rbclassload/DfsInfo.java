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

import java.util.*;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class DfsInfo {

  private Set<String> m_dfsMethods;
  private Set<String> m_reverseDfsMethods;
  private Set<Type> m_dfsTypes;
  private List<Type> m_orderedTypes;
  private List<RefType> m_orderedRefTypes;
  private List<Type> m_orderedRefLikeTypes;
  private Set<SootField> m_dfsFields;
  private Set<ArrayType> m_arrayTypes;
  private List<NumberedType> m_numberedTypes;
  private Set<Type> m_instanceOfs;
  private List<String> m_reachableMethodSigs;
  private String m_rootMethod;
  private List<SootMethod> m_otherEntryPoints;
  private Set<String> m_modifiedClasses;
  private StringCallGraph m_stringCallGraph;
  private Set<String> m_reachableFields;

  public DfsInfo(String method_signature) {
    m_dfsMethods = new LinkedHashSet<String>();
    m_reverseDfsMethods = new LinkedHashSet<String>();
    m_dfsTypes = new HashSet<Type>();
    m_dfsFields = new HashSet<SootField>();
    m_arrayTypes = new HashSet<ArrayType>();
    m_instanceOfs = new HashSet<Type>();
    m_rootMethod = method_signature;
    m_otherEntryPoints = new ArrayList<SootMethod>();
    m_stringCallGraph = new StringCallGraph();
    m_numberedTypes = new ArrayList<NumberedType>();
    m_orderedTypes = new ArrayList<Type>();
    m_orderedRefTypes = new ArrayList<RefType>();
    m_orderedRefLikeTypes = new ArrayList<Type>();
  }

  public StringCallGraph getStringCallGraph(){
    return m_stringCallGraph;
  }
  
  public void expandArrayTypes(){  
    MultiDimensionalArrayTypeCreator creator = new MultiDimensionalArrayTypeCreator();
    m_arrayTypes = creator.createType(m_arrayTypes);    

    m_dfsTypes.addAll(m_arrayTypes);
    m_arrayTypes.addAll(m_arrayTypes);
  }
  
  private void printSet(String name, Set curr_set) {
    System.out.println(name);
    for(Object curr : curr_set){
      System.out.println("  "+curr.toString());
    }
  }

  public List<String> getForwardReachables() {
    List<String> ret = new ArrayList<String>();
    ret.addAll(m_dfsMethods);
    return ret;
  }

  public void addMethod(String signature) {
    m_dfsMethods.add(signature);
  }

  public boolean containsMethod(String signature) {
    return m_dfsMethods.contains(signature);
  }

  public boolean containsType(Type name) {
    return m_dfsTypes.contains(name);
  }

  public Set<Type> getDfsTypes(){
    return m_dfsTypes;
  }

  public void addType(Type type) {
    m_dfsTypes.add(type);
    if(type instanceof ArrayType){
      ArrayType array_type = (ArrayType) type;
      m_arrayTypes.add(array_type);
    }
  }

  public void addType(String type_str) {
    StringToType converter = new StringToType();
    Type type = converter.convert(type_str);
    addType(type);
  }
  
  public void addField(SootField field){
    m_dfsFields.add(field);
  }

  public List<Type> getOrderedTypes() {
    return m_orderedTypes;
  }

  public List<RefType> getOrderedRefTypes() {
    List<RefType> ret_copy = new ArrayList<RefType>();
    ret_copy.addAll(m_orderedRefTypes);
    return ret_copy;
  }

  public Set<String> getMethods() {
    return m_dfsMethods;
  }

  public Set<String> getReverseMethods(){
    return m_reverseDfsMethods;
  }

  public void addReverseDfsMethod(String sig){
    m_reverseDfsMethods.add(sig);
  }

  public Set<SootField> getFields() {
    return m_dfsFields;
  }

  public void addArrayType(ArrayType array_type){
    m_arrayTypes.add(array_type);
  }

  public Set<ArrayType> getArrayTypes() {
    return m_arrayTypes;
  }

  public List<Type> getOrderedRefLikeTypes() {
    return m_orderedRefLikeTypes;
  }

  public Set<Type> getInstanceOfs() {
    return m_instanceOfs;
  }

  public void addInstanceOf(Type type) {
    m_instanceOfs.add(type);
  }
  
  public Set<String> getAllMethods() {
    Set<String> ret = new HashSet<String>();
    ret.addAll(m_dfsMethods);
    ret.addAll(m_reverseDfsMethods);
    return ret;
  }

  public SootMethod getRootMethod() {
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(m_rootMethod);
    return util.getSootMethod();
  }

  public String getRootMethodSignature() {
    return m_rootMethod;
  }

  public List<SootMethod> getOtherEntryPoints(){
    return m_otherEntryPoints;
  }

  public void setModifiedClasses(Set<String> modified_classes) {
    m_modifiedClasses = modified_classes;
  }
  
  public Set<String> getModifiedClasses(){
    return m_modifiedClasses;
  }

  public void finalizeTypes(){
    //read in NumberedTypes
    StringToType converter = new StringToType();
    ClassHierarchy class_hierarchy = RootbeerClassLoader.v().getClassHierarchy();
    List<NumberedType> numbered_types = class_hierarchy.getNumberedTypes();
    Collections.reverse(numbered_types);
    for(NumberedType ntype : numbered_types){
      String type_str = ntype.getType();
      Type type = converter.convert(type_str);
      if(m_dfsTypes.contains(type) || m_arrayTypes.contains(type)){
        m_numberedTypes.add(ntype);
        m_orderedTypes.add(type);
        if(type instanceof RefType){
          RefType ref_type = (RefType) type;
          SootClass soot_class = ref_type.getSootClass();
          if(!soot_class.isInterface()){
            m_orderedRefTypes.add(ref_type);            
            m_orderedRefLikeTypes.add(type);
          }
        }
        if(type instanceof ArrayType){
          ArrayType array_type = (ArrayType) type;
          Type base_type = array_type.baseType;
          if(base_type instanceof RefType){
            RefType ref_type = (RefType) base_type;
            SootClass soot_class = ref_type.getSootClass();
            if(!soot_class.isInterface()){
              m_orderedRefTypes.add(ref_type);  
              m_orderedRefLikeTypes.add(type);
            }
          } else {
            m_orderedRefLikeTypes.add(type);
          }
        }
      }
    }
  }

  public List<NumberedType> getNumberedTypes(){
    return m_numberedTypes;
  }

  /**
   *  Get List<NumberedTypes> that are super types of soot_class
   */
  public List<NumberedType> getNumberedHierarchyUp(SootClass soot_class){
    List<NumberedType> ret = new ArrayList<NumberedType>();

    ClassHierarchy class_hierarchy = RootbeerClassLoader.v().getClassHierarchy();

    LinkedList<String> queue = new LinkedList<String>();
    queue.add(soot_class.getName());
    
    while(!queue.isEmpty()){
      String curr_class = queue.removeFirst();
      NumberedType ntype = class_hierarchy.getNumberedType(curr_class);
      ret.add(ntype);

      SootClass curr_soot_class = Scene.v().getSootClass(curr_class);
      if(curr_soot_class.hasSuperclass()){
        queue.add(curr_soot_class.getSuperclass().getName());
      }
    }

    return ret;
  }

}
