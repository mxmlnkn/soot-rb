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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

import soot.SootMethod;
import soot.SootClass;
import soot.Scene;
import soot.Type;
import soot.RefType;
import soot.RefLikeType;

public class ClassHierarchy {

  private Map<String, HierarchySootClass> m_hierarchySootClasses;
  private Map<String, List<HierarchyGraph>> m_unmergedGraphs;
  private Map<String, HierarchyGraph> m_hierarchyGraphs;
  private Map<String, List<String>> m_virtualMethodSignatures;
  private Set<String> m_roots;
  private Set<String> m_interfaces;
  private Set<String> m_arrayTypes;
  private List<NumberedType> m_numberedTypes;
  private Map<String, NumberedType> m_numberedTypeMap;
  private MethodSignatureUtil m_util;
  private Map<HierarchySootMethod, HierarchySootMethod> m_virtMap;
  private int m_ifaceCount;
  private Set<String> m_visited;

  public ClassHierarchy(){
    m_hierarchySootClasses = new HashMap<String, HierarchySootClass>();
    m_hierarchyGraphs = new HashMap<String, HierarchyGraph>();
    m_virtualMethodSignatures = new HashMap<String, List<String>>();
    m_arrayTypes = new HashSet<String>();
    m_numberedTypes = new ArrayList<NumberedType>();
    m_numberedTypeMap = new HashMap<String, NumberedType>();
    m_util = new MethodSignatureUtil();
  }

  public void put(String name, HierarchySootClass hierarchy_class){
    m_hierarchySootClasses.put(name, hierarchy_class);
  }

  public HierarchySootClass getHierarchySootClass(String name){
    return m_hierarchySootClasses.get(name);
  }

  public Set<String> getClasses(){
    return m_hierarchySootClasses.keySet();
  }

  public Collection<HierarchySootClass> getHierarchyClasses(){
    return m_hierarchySootClasses.values();
  }

  public HierarchySootMethod getHierarchySootMethod(String signature){
    m_util.parse(signature);
    String class_name = m_util.getClassName();
    HierarchySootClass hclass = getHierarchySootClass(class_name);
    if(hclass == null){
      return null;
    }
    return hclass.findMethodBySubSignature(m_util.getSubSignature());
  }

  public boolean containsClass(String name){
    return m_hierarchySootClasses.containsKey(name);
  }

  public void build(){
    //find roots
    m_roots = new HashSet<String>();
    m_interfaces = new HashSet<String>();
    m_roots.addAll(m_hierarchySootClasses.keySet());

    Iterator<String> all_classes = m_hierarchySootClasses.keySet().iterator();
    while(all_classes.hasNext()){
      String class_name = all_classes.next();
      HierarchySootClass hsoot_class = m_hierarchySootClasses.get(class_name);
      m_roots.remove(hsoot_class.getSuperClass());
      for(String iface : hsoot_class.getInterfaces()){
        m_roots.remove(iface);
        m_interfaces.add(iface);
      }
    }

    m_unmergedGraphs = new HashMap<String, List<HierarchyGraph>>();

    //foreach root
    for(String root : m_roots){    
      HierarchyGraph hgraph = new HierarchyGraph();
      LinkedList<String> queue = new LinkedList<String>();
      queue.add(root);

      while(queue.isEmpty() == false){
        String curr_class = queue.removeFirst();

        mapPut(m_unmergedGraphs, curr_class, hgraph);
        hgraph.addHierarchyClass(curr_class);

        HierarchySootClass hclass = m_hierarchySootClasses.get(curr_class);
        if(hclass == null){
          continue;
        }

        if(hclass.isInterface()){
          if(hclass.getInterfaces().isEmpty()){
            if(hclass.hasSuperClass()){
              String super_class = hclass.getSuperClass();
              hgraph.addSuperClass(curr_class, super_class);
              queue.add(super_class);
            }
          } else {
            for(String iface : hclass.getInterfaces()){
              hgraph.addInterface(curr_class, iface);
              queue.add(iface);
            }
          }
        } else {
          if(hclass.hasSuperClass()){
            String super_class = hclass.getSuperClass();
            hgraph.addSuperClass(curr_class, super_class);
            queue.add(super_class);
          }

          for(String iface : hclass.getInterfaces()){
            hgraph.addInterface(curr_class, iface);
            queue.add(iface);
          }
        }
      }
    }
    mergeGraphs();
  }

  public void addArrayType(String array_type){
    m_arrayTypes.add(array_type);
  }

  public void buildArrayTypes(){
    System.out.println("building array types...");
    MultiDimensionalArrayTypeCreator creator = new MultiDimensionalArrayTypeCreator();
    m_arrayTypes = creator.createString(m_arrayTypes);    

    HierarchyGraph hgraph = m_hierarchyGraphs.get("java.lang.Object");
    for(String curr_class : m_arrayTypes){
      hgraph.addHierarchyClass(curr_class);
      hgraph.addSuperClass(curr_class, "java.lang.Object");

      mapPut(m_unmergedGraphs, curr_class, hgraph);
      mapPut(m_unmergedGraphs, "java.lang.Object", hgraph);
    }

    mergeGraphs();
  }

  public HierarchySootMethod findMethod(String signature){
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(signature);
    String class_name = util.getClassName();

    StringToType string_to_type = new StringToType();
    LinkedList<String> queue = new LinkedList<String>();
    queue.add(class_name);
    while(queue.isEmpty() == false){
      String curr_name = queue.removeFirst();

      String subsig = util.getSubSignature();      
      HierarchySootClass hclass = getHierarchySootClass(curr_name);
      if(hclass == null){
        if(string_to_type.isArrayType(curr_name)){
          queue.add("java.lang.Object");
          continue;
        } else {
          return null;
        }
      }
      HierarchySootMethod hmethod = hclass.findMethodBySubSignature(subsig);
      if(hmethod == null){
        if(hclass.hasSuperClass()){
          queue.add(hclass.getSuperClass());
        } 
        for(String iface : hclass.getInterfaces()){
          queue.add(iface);
        }
      } else {
        return hmethod;
      }
    }
    return null;
  }

  private void mergeGraphs(){
    System.out.println("merging graphs...");
    m_hierarchyGraphs.clear();
    for(String curr_class : m_unmergedGraphs.keySet()){
      List<HierarchyGraph> graphs = m_unmergedGraphs.get(curr_class);
      HierarchyGraph new_graph = new HierarchyGraph();
      for(HierarchyGraph curr_graph : graphs){
        new_graph.merge(curr_graph);
      }
      m_hierarchyGraphs.put(curr_class, new_graph);
    }
  }

  public long getNumberForType(String type){
    if(m_numberedTypeMap.containsKey(type)){
      return m_numberedTypeMap.get(type).getNumber();
	}
    return -1;
  }
  
  public NumberedType getNumberedType(String str){
    if(m_numberedTypeMap.containsKey(str)){
      return m_numberedTypeMap.get(str);
    } else {
      System.out.println("cannot find numbered type: "+str);
      Iterator<String> iter = m_numberedTypeMap.keySet().iterator();
      while(iter.hasNext()){
        System.out.println("  "+iter.next());
      }      
      try {
        throw new RuntimeException("");
      } catch(Exception ex){
        ex.printStackTrace(System.out);
      }
      System.exit(0);
      return null;
    }
  }

  public void numberTypes(){
    System.out.println("numbering types...");
    int number = 1;
    LinkedList<String> queue = new LinkedList<String>();
    Set<String> visited = new HashSet<String>();
    HierarchyGraph hgraph = m_hierarchyGraphs.get("java.lang.Object");
    Set<String> children0 = hgraph.getChildren("java.lang.Object");

    NumberedType numbered_type = new NumberedType("java.lang.Object", number);
    m_numberedTypes.add(numbered_type);
    m_numberedTypeMap.put("java.lang.Object", numbered_type);
    number++;

    //count interfaces
    m_ifaceCount = 0;
    queue.add("java.lang.Object");
    while(queue.isEmpty() == false){
      String curr_type = queue.removeFirst();
      if(visited.contains(curr_type)){
        continue;
      }
      visited.add(curr_type);

      HierarchySootClass hclass = getHierarchySootClass(curr_type);
      //the hclass can be null if curr_type is an array type. skip it since they
      //are subclasses of java.lang.object
      if(hclass == null){
        continue;
      }

      Set<String> children = hgraph.getChildren(curr_type); 
      for(String child : children){
        queue.add(child);
      }

      if(hclass.isInterface()){
        ++m_ifaceCount;
      }
    }

    //topo sort numbering of ifaces
    number += m_ifaceCount + 1;
    m_visited = new HashSet<String>();
    topoVisit("java.lang.Object");

    //then load concrete classes
    queue.add("java.lang.Object");
    visited.clear();
    while(queue.isEmpty() == false){
      String curr_type = queue.removeFirst();

      if(visited.contains(curr_type)){
        continue;
      }
      visited.add(curr_type);
      
      //Here we only add the child interfaces of interfaces.
      //take java.io.PrintStream and java.io.FilterOutputStream. PrintStream
      //derives from FilterOutputStream but implements an extra interface
      //(java.lang.Appendable). Implementing the extra interface gets us 
      //to number PrintStream sooner unless we ignore concrete classes of 
      //interfaces.
      HierarchySootClass hclass = getHierarchySootClass(curr_type);
      if(hclass == null){
        //if the current type is an array, number it and continue    
        if(curr_type.contains("[]")){
          numbered_type = new NumberedType(curr_type, number);
          m_numberedTypes.add(numbered_type);
          m_numberedTypeMap.put(curr_type, numbered_type);
          number++;
        } 
        continue;
      } 

      if(hclass.isInterface()){
        continue;
      } else {
        Set<String> children = hgraph.getChildren(curr_type);
        //Add concrete classes. In this way interfaces that derive from
        //Object are visited before concrete classes that derive from Object.
        for(String child : children){
          HierarchySootClass child_hclass = getHierarchySootClass(child);
          if(child_hclass == null){
            //if child is an array, add it to the queue
            if(child.contains("[]")){
              queue.add(child);
            } 
            continue;
          }
          if(!child_hclass.isInterface()){
            queue.add(child);
          }
        }
      }

      if(curr_type.equals("java.lang.Object") == false){
        numbered_type = new NumberedType(curr_type, number);
        m_numberedTypes.add(numbered_type);
        m_numberedTypeMap.put(curr_type, numbered_type);
        number++;
      }
    }

    Collections.sort(m_numberedTypes, new NumberedTypeSorter());
  }

  private void topoVisit(String iface_class){
    if(m_visited.contains(iface_class)){
      return;
    }
    m_visited.add(iface_class);

    HierarchyGraph hgraph = m_hierarchyGraphs.get("java.lang.Object");
    Set<String> children = hgraph.getChildren(iface_class); 
    for(String child : children){
      HierarchySootClass hclass = getHierarchySootClass(child);
      if(hclass == null){
        continue;
      }
      if(hclass.isInterface()){
        topoVisit(child);
      }
    }

    if(iface_class.equals("java.lang.Object") == false){
      NumberedType numbered_type = new NumberedType(iface_class, m_ifaceCount);
      m_numberedTypes.add(numbered_type);
      m_numberedTypeMap.put(iface_class, numbered_type);
      --m_ifaceCount;
    }
  }

  public List<NumberedType> getNumberedTypes(){
    List<NumberedType> ret_copy = new ArrayList<NumberedType>();
    ret_copy.addAll(m_numberedTypes);
    return ret_copy;
  }

  public void cacheVirtualMethods(){   
    m_virtMap = new HashMap<HierarchySootMethod, HierarchySootMethod>();
    for(String base_class : getClasses()){
      if(m_interfaces.contains(base_class)){
        continue;
      }
      HierarchySootClass base_hclass = getHierarchySootClass(base_class);
      if(base_hclass == null){
        continue;
      }
      List<HierarchySootMethod> base_methods = base_hclass.getMethods(); 
      for(HierarchySootMethod method : base_methods){
        HierarchySootClass super_hclass = base_hclass;
        while(super_hclass.hasSuperClass()){
          super_hclass = getHierarchySootClass(super_hclass.getSuperClass());
          if(super_hclass == null){
            break;
          }
          if(super_hclass.declaresCovarientSubSignature(method.getCovarientSubSignature())){
            HierarchySootMethod super_hmethod = super_hclass.getMethodForCovarientSubSignature(method.getCovarientSubSignature());
            m_virtMap.put(method, super_hmethod);
            break;
          }
        }
      }          
    }

    Set<String> visited_sigs = new HashSet<String>();
    LinkedList<String> bfs_queue = new LinkedList<String>();
    bfs_queue.addAll(m_roots);

    while(bfs_queue.isEmpty() == false){
      String curr_class = bfs_queue.removeFirst();

      HierarchySootClass hclass = getHierarchySootClass(curr_class);
      if(hclass == null){
        continue;
      }

      List<HierarchySootMethod> methods = hclass.getMethods();
      for(HierarchySootMethod method : methods){
        String curr_sig = method.getSignature();
        if(visited_sigs.contains(curr_sig)){
          continue;
        }
        visited_sigs.add(curr_sig);

        List<String> path = new ArrayList<String>();
        path.add(curr_sig);
        m_virtualMethodSignatures.put(curr_sig, path);

        HierarchySootMethod trace_method = method;
        while(m_virtMap.containsKey(trace_method)){
          trace_method = m_virtMap.get(trace_method);
          String trace_sig = trace_method.getSignature();
          path.add(trace_sig);
          m_virtualMethodSignatures.put(trace_sig, path);
        }
      }

      if(hclass.hasSuperClass()){
        bfs_queue.add(hclass.getSuperClass());
      }
    }  

    visited_sigs.clear();
    bfs_queue.addAll(m_roots);
    while(bfs_queue.isEmpty() == false){
      String curr_class = bfs_queue.removeFirst();

      HierarchySootClass hclass = getHierarchySootClass(curr_class);
      if(hclass == null){
        continue;
      }

      List<HierarchySootMethod> methods = hclass.getMethods();
      for(HierarchySootMethod method : methods){
        String curr_sig = method.getSignature();
        if(visited_sigs.contains(curr_sig)){
          continue;
        }
        visited_sigs.add(curr_sig);

        String subsig = method.getSubSignature();
        for(String iface : hclass.getInterfaces()){
          HierarchySootClass iface_hclass = getHierarchySootClass(iface);
          if(iface_hclass == null){
            continue;
          }
          HierarchySootMethod iface_method = iface_hclass.findMethodBySubSignature(subsig);
          if(iface_method == null){
            continue;
          } 
          List<String> path = m_virtualMethodSignatures.get(curr_sig);
          String iface_sig = iface_method.getSignature();

          if(m_virtualMethodSignatures.containsKey(iface_sig)){
            List<String> new_path = new ArrayList<String>();
            new_path.addAll(path);
            List<String> old_path = m_virtualMethodSignatures.get(iface_sig);
            for(String element : old_path){
              if(new_path.contains(element) == false){
                new_path.add(element);
              }
            }
            m_virtualMethodSignatures.put(iface_sig, new_path);
          } else {
            m_virtualMethodSignatures.put(iface_sig, path);
          }
        }
      }

      if(hclass.hasSuperClass()){
        bfs_queue.add(hclass.getSuperClass());
      }
    }

  }

  private void mapPut(Map<String, List<HierarchyGraph>> temp_graphs, String curr_class, 
    HierarchyGraph hgraph){

    List<HierarchyGraph> graphs;
    if(temp_graphs.containsKey(curr_class)){
      graphs = temp_graphs.get(curr_class);
    } else {
      graphs = new ArrayList<HierarchyGraph>();
      temp_graphs.put(curr_class, graphs);
    }
    graphs.add(hgraph);
  }

  public HierarchyGraph getHierarchyGraph(String signature){    
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(signature);
    String class_name = util.getClassName();
    return m_hierarchyGraphs.get(class_name);
  }

  public HierarchyGraph getHierarchyGraph(SootClass soot_class){
    String class_name = soot_class.getName();
    return m_hierarchyGraphs.get(class_name);
  }

  public List<String> getVirtualMethods(String signature){
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(signature);
    String class_name = util.getClassName();

    List<String> ret = new ArrayList<String>();
    ret.add(signature);

    if(util.getMethodName().equals("<init>") || util.getMethodName().equals("<clinit>")){
      return ret;
    }

    if(m_hierarchyGraphs.containsKey(class_name) == false){
      return ret;
    }

    Set<String> new_invokes = RootbeerClassLoader.v().getNewInvokes();  
    if(m_virtualMethodSignatures.containsKey(signature) == false){
      //todo: fix this
      //throw new RuntimeException("cannot find virtual signature: "+signature);
      return ret;
    }

    List<String> virt_sigs = m_virtualMethodSignatures.get(signature); 
    for(String virt_sig : virt_sigs){
      util.parse(virt_sig);
      String virt_class_name = util.getClassName();
      if(new_invokes.contains(virt_class_name) && ret.contains(virt_sig) == false){
        ret.add(virt_sig);
      }
    }
  
    return ret;
  }

  @Override
  public String toString(){
    StringBuilder ret = new StringBuilder();    
    for(HierarchyGraph hgraph : m_hierarchyGraphs.values()){
      ret.append(hgraph.toString());
    }
    return ret.toString();
  }
}

