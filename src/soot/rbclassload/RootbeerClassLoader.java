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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.input.CountingInputStream;

import soot.Body;
import soot.ClassSource;
import soot.CoffiClassSource;
import soot.G;
import soot.Scene;
import soot.Singletons;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.coffi.HierarchySootClassFactory;


public class RootbeerClassLoader {

  private Map<String, DfsInfo> m_dfsInfos;
  private DfsInfo m_currDfsInfo;

  private Map<String, Set<String>> m_packageNameCache;
  private ClassHierarchy m_classHierarchy;
  private List<String> m_sourcePaths;
  private Map<String, String> m_classToFilename;
  private String m_tempFolder;
  private List<String> m_classPaths;
  private int m_loadedCount;

  private List<MethodTester> m_entryMethodTesters;
  private List<MethodTester> m_dontFollowMethodTesters;
  private List<MethodTester> m_followMethodTesters;
  private List<MethodTester> m_toSignaturesMethodTesters;

  private List<ClassTester> m_dontFollowClassTesters;
  private List<ClassTester> m_followClassTesters;
  private List<ClassTester> m_toSignaturesClassTesters;

  private Set<String> m_followMethods;
  private Set<String> m_toSignaturesMethods;
  private Set<String> m_followClasses;
  private Set<String> m_toSignaturesClasses;
  private Set<String> m_toHierarchyClasses;

  private List<String> m_loadFields;

  private List<String> m_appClasses;
  private List<String> m_entryPoints;
  private Set<String> m_visited;
  private String m_userJar;
  private Set<String> m_generatedMethods;
  private Set<String> m_newInvokes;
  private Set<String> m_refTypes;
  private Map<String, String> m_remapping;
  private Map<String, HierarchyValueSwitch> m_valueSwitchMap;
  private List<ConditionalCudaEntry> m_conditionalCudaEntries;
  private RemapClassName m_remapClassName;
  private boolean m_loaded;

  private Set<String> m_cgVisitedClasses;
  private Set<String> m_cgVisitedMethods;
  private LinkedList<String> m_cgMethodQueue;
  private StringNumbers m_stringNumbers;

  public RootbeerClassLoader(Singletons.Global g){
    m_dfsInfos = new HashMap<String, DfsInfo>();
    m_packageNameCache = new HashMap<String, Set<String>>();
    m_classHierarchy = new ClassHierarchy();
    m_classToFilename = new HashMap<String, String>();

    String home = System.getProperty("user.home");
    File soot_folder = new File(home + File.separator + ".soot" + File.separator + "rbcl_cache");
    soot_folder.mkdirs();
    m_tempFolder = soot_folder.getAbsolutePath() + File.separator;
    m_loadedCount = 0;

    m_entryMethodTesters = new ArrayList<MethodTester>();
    m_dontFollowMethodTesters = new ArrayList<MethodTester>();
    m_followMethodTesters = new ArrayList<MethodTester>();
    m_toSignaturesMethodTesters = new ArrayList<MethodTester>();

    m_dontFollowClassTesters = new ArrayList<ClassTester>();
    m_followClassTesters = new ArrayList<ClassTester>();
    m_toSignaturesClassTesters = new ArrayList<ClassTester>();

    m_followMethods = new HashSet<String>();
    m_toSignaturesMethods = new HashSet<String>();
    m_followClasses = new HashSet<String>();
    m_toSignaturesClasses = new HashSet<String>();
    m_toHierarchyClasses = new HashSet<String>();

    m_loadFields = new ArrayList<String>();

    m_appClasses = new ArrayList<String>();
    m_userJar = null;

    m_remapClassName = new RemapClassName();
    m_generatedMethods = new HashSet<String>();
    m_conditionalCudaEntries = new ArrayList<ConditionalCudaEntry>();
    m_newInvokes = new HashSet<String>();
    m_refTypes = new HashSet<String>();
    m_valueSwitchMap = new HashMap<String, HierarchyValueSwitch>();

    m_cgVisitedClasses = new HashSet<String>();
    m_cgVisitedMethods = new HashSet<String>();
    m_cgMethodQueue = new LinkedList<String>();
    m_stringNumbers = new StringNumbers();

    m_loaded = false;
    loadBuiltIns();
  }

  public static RootbeerClassLoader v() { 
    return G.v().soot_rbclassload_RootbeerClassLoader(); 
  }

  public void loadBuiltIns(){
    addBasicClassHierarchy("java.lang.Object");
    addBasicClassSignatures("java.lang.Class");

    addBasicClassSignatures("java.lang.Void");
    addBasicClassSignatures("java.lang.Boolean");
    addBasicClassSignatures("java.lang.Byte");
    addBasicClassSignatures("java.lang.Character");
    addBasicClassSignatures("java.lang.Short");
    addBasicClassSignatures("java.lang.Integer");
    addBasicClassSignatures("java.lang.Long");
    addBasicClassSignatures("java.lang.Float");
    addBasicClassSignatures("java.lang.Double");

    addBasicClassHierarchy("java.lang.String");
    addBasicClassSignatures("java.lang.StringBuffer");

    addBasicClassHierarchy("java.lang.Error");
    addBasicClassSignatures("java.lang.AssertionError");
    addBasicClassSignatures("java.lang.Throwable");
    addBasicClassSignatures("java.lang.NoClassDefFoundError");
    addBasicClassHierarchy("java.lang.ExceptionInInitializerError");
    addBasicClassHierarchy("java.lang.RuntimeException");
    addBasicClassHierarchy("java.lang.ClassNotFoundException");
    addBasicClassHierarchy("java.lang.ArithmeticException");
    addBasicClassHierarchy("java.lang.ArrayStoreException");
    addBasicClassHierarchy("java.lang.ClassCastException");
    addBasicClassHierarchy("java.lang.IllegalMonitorStateException");
    addBasicClassHierarchy("java.lang.IndexOutOfBoundsException");
    addBasicClassHierarchy("java.lang.ArrayIndexOutOfBoundsException");
    addBasicClassHierarchy("java.lang.NegativeArraySizeException");
    addBasicClassHierarchy("java.lang.NullPointerException");
    addBasicClassHierarchy("java.lang.InstantiationError");
    addBasicClassHierarchy("java.lang.InternalError");
    addBasicClassHierarchy("java.lang.OutOfMemoryError");
    addBasicClassHierarchy("java.lang.StackOverflowError");
    addBasicClassHierarchy("java.lang.UnknownError");
    addBasicClassHierarchy("java.lang.ThreadDeath");
    addBasicClassHierarchy("java.lang.ClassCircularityError");
    addBasicClassHierarchy("java.lang.ClassFormatError");
    addBasicClassHierarchy("java.lang.IllegalAccessError");
    addBasicClassHierarchy("java.lang.IncompatibleClassChangeError");
    addBasicClassHierarchy("java.lang.LinkageError");
    addBasicClassHierarchy("java.lang.VerifyError");
    addBasicClassHierarchy("java.lang.NoSuchFieldError");
    addBasicClassHierarchy("java.lang.AbstractMethodError");
    addBasicClassHierarchy("java.lang.NoSuchMethodError");
    addBasicClassHierarchy("java.lang.UnsatisfiedLinkError");

    addBasicClassHierarchy("java.lang.Thread");
    addBasicClassHierarchy("java.lang.Runnable");
    addBasicClassHierarchy("java.lang.Cloneable");

    addBasicClassHierarchy("java.io.Serializable");	
    addBasicClassHierarchy("java.lang.ref.Finalizer");
    addBasicClassHierarchy("java.lang.ref.FinalReference");
  }

  private void addBasicClassHierarchy(String class_name){
    m_toHierarchyClasses.add(class_name);
  }

  private void addBasicClassSignatures(String class_name){
    m_toSignaturesClasses.add(class_name);
  }

  public StringNumbers getStringNumbers(){
    return m_stringNumbers;
  }

  public void addConditionalCudaEntry(ConditionalCudaEntry entry){
    m_conditionalCudaEntries.add(entry);
  }

  public void addEntryMethodTester(MethodTester method_tester){
    m_entryMethodTesters.add(method_tester);
  }

  public void addDontFollowMethodTester(MethodTester method_tester){
    m_dontFollowMethodTesters.add(method_tester);
  }

  public void addFollowMethodTester(MethodTester method_tester){
    m_followMethodTesters.add(method_tester);
  }

  public void addToSignaturesMethodTester(MethodTester method_tester){
    m_toSignaturesMethodTesters.add(method_tester);
  }

  public void addDontFollowClassTester(ClassTester class_tester){
    m_dontFollowClassTesters.add(class_tester);
  }

  public void addFollowClassTester(ClassTester class_tester){
    m_followClassTesters.add(class_tester);
  }

  public void addToSignaturesClassTester(ClassTester class_tester){
    m_toSignaturesClassTesters.add(class_tester);
  }

  public void addNewInvoke(String class_name){
    m_newInvokes.add(class_name);
  }

  public void addGeneratedMethod(String signature){
    m_generatedMethods.add(signature);
  }

  public void loadField(String field_sig){
    m_loadFields.add(field_sig);
  }

  public List<String> getAllAppClasses(){
    return m_appClasses;
  }

  public ClassHierarchy getClassHierarchy(){
    return m_classHierarchy;
  }

  public void setUserJar(String filename){
    m_userJar = filename;
  }

  public void loadNecessaryClasses(){
    m_sourcePaths = SourceLocator.v().sourcePath();
    m_classPaths = SourceLocator.v().classPath();

    loadHierarchySootClasses();
    buildClassHierarchy();
    findEntryPoints();
    loadFollowsStrings();
    loadToSignaturesString();
    
    int prev_size = -1;
    while(prev_size != m_newInvokes.size()){
      prev_size = m_newInvokes.size();
      for(String entry : m_entryPoints){
        System.out.println("entry point: "+entry);
        //System.out.println("  new_invokes: ");
        //for(String new_invoke : m_newInvokes){
        //  System.out.println("    "+new_invoke);
        //}
        DfsInfo dfs_info = new DfsInfo(entry);
        m_dfsInfos.put(entry, dfs_info);
        m_currDfsInfo = dfs_info;

        loadStringCallGraph();
      }
    }

    //todo: look at these closely
    //cloneLibraryClasses();
    //remapFields();
    //remapTypes();

    //todo: write this. patch StringCallGraph and ClassHierarchy
    //patchState();
    
    m_classHierarchy.buildArrayTypes();
    m_classHierarchy.numberTypes();
    loadScene();

    for(String entry : m_entryPoints){
      m_currDfsInfo = m_dfsInfos.get(entry);
      dfsForRootbeer();
      m_currDfsInfo.expandArrayTypes();
      m_currDfsInfo.finalizeTypes();
    }

    Scene.v().loadDynamicClasses();
  }
 
  public void setLoaded(){
    m_loaded = true;
  }

  private void loadStringCallGraph(){
    String entry = m_currDfsInfo.getRootMethodSignature();

    System.out.println("loading forward string call graph for: "+entry+"...");
    m_cgVisitedMethods.clear();
    m_cgMethodQueue.add(entry);
    Set<String> reverse_reachable = new HashSet<String>();

    m_currDfsInfo.getStringCallGraph().addEntryPoint(entry);

    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(entry);
    HierarchySootClass entry_class = m_classHierarchy.getHierarchySootClass(util.getClassName());
    List<HierarchySootMethod> entry_methods = entry_class.getMethods();
    for(HierarchySootMethod entry_method : entry_methods){
      if(entry_method.getName().equals("<init>")){
        m_cgMethodQueue.add(entry_method.getSignature());
        reverse_reachable.add(entry_method.getSignature());
      }
    }
    m_newInvokes.add(util.getClassName());

    for(String follow_signature : m_followMethods){
      m_cgMethodQueue.add(follow_signature);
    }

    for(String follow_class : m_followClasses){
      HierarchySootClass follow_hclass = m_classHierarchy.getHierarchySootClass(follow_class);
      List<HierarchySootMethod> follow_methods = follow_hclass.getMethods();
      for(HierarchySootMethod follow_method : follow_methods){
        m_cgMethodQueue.add(follow_method.getSignature());
      }
    }

    processForwardStringCallGraphQueue();

    System.out.println("loading reverse string call graph for: "+entry+"...");
    reverse_reachable.add(m_currDfsInfo.getRootMethodSignature());

    //int prev_size = -1;
    //while(prev_size != reverse_reachable.size()){
      //prev_size = reverse_reachable.size();
      for(String class_name : m_appClasses){
        if(dontFollowClass(class_name)){
          continue;
        }
        HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(class_name);
        List<HierarchySootMethod> methods = hclass.getMethods();
        for(HierarchySootMethod method : methods){
          String signature = method.getSignature();

          if(dontFollowMethod(signature)){
            continue;
          }

          reverseStringGraphVisit(signature, reverse_reachable);   
        }
      }
    //}

    //System.out.println("StringCallGraph: "+m_currDfsInfo.getStringCallGraph().toString());
  }

  private void processForwardStringCallGraphQueue(){
    MethodSignatureUtil util = new MethodSignatureUtil();
    while(m_cgMethodQueue.isEmpty() == false){
      String bfs_entry = m_cgMethodQueue.removeFirst();

      if(m_cgVisitedMethods.contains(bfs_entry)){
        continue;
      }
      m_cgVisitedMethods.add(bfs_entry);
      
      util.parse(bfs_entry);

      String class_name = util.getClassName();

      HierarchySootMethod hmethod = m_classHierarchy.findMethod(util.getSignature());
      if(hmethod == null){
        continue;
      }

      //m_classHierarchy.findMethod finds the method that has actually been
      //declared in the .class file. Here we add the original signature to the
      //call graph and the concrete signature.
      m_currDfsInfo.getStringCallGraph().addSignature(bfs_entry);
      bfs_entry = hmethod.getSignature();
      util.parse(bfs_entry);
      class_name = util.getClassName();
      m_currDfsInfo.getStringCallGraph().addSignature(bfs_entry);

      //add virtual methods to queue
      List<String> virt_methods = m_classHierarchy.getVirtualMethods(bfs_entry);
      for(String signature : virt_methods){
        if(dontFollow(signature)){
          continue;
        }
        m_cgMethodQueue.add(signature);
      }

      if(hmethod.isConcrete() == false){
        continue;
      }

      //add bfs methods to queue
      HierarchyValueSwitch value_switch = getValueSwitch(bfs_entry);
      m_newInvokes.addAll(value_switch.getNewInvokes());

      for(String dest_sig : value_switch.getMethodRefs()){
        if(dontFollow(dest_sig)){
          continue;
        }
        m_currDfsInfo.getStringCallGraph().addEdge(bfs_entry, dest_sig);
        m_cgMethodQueue.add(dest_sig);        
      }

      for(String array_type : value_switch.getArrayTypes()){
        m_classHierarchy.addArrayType(array_type);
      }

      //add <clinit> of class_refs
      Set<String> class_refs = value_switch.getRefTypes();
      for(String class_ref : class_refs){
        loadHierarchy(class_ref, m_refTypes);

        HierarchySootClass clinit_class = m_classHierarchy.getHierarchySootClass(class_ref);
        if(clinit_class == null){
          continue;
        }
        HierarchySootMethod clinit_method = clinit_class.findMethodBySubSignature("void <clinit>()");
        if(clinit_method == null){
          continue;
        }
        String clinit_sig = clinit_method.getSignature();

        if(dontFollow(clinit_sig)){
          continue;
        }
        m_cgMethodQueue.add(clinit_sig);
      }

      //load ctors to main method
      String subsignature = util.getSubSignature();
      if(subsignature.equals("void main(java.lang.String[])")){
        String ctor_sig = "<"+class_name+": void <init>()>";
        m_cgVisitedMethods.add(ctor_sig);
      }

      if(m_cgVisitedClasses.contains(class_name)){
        continue;
      }
      m_cgVisitedClasses.add(class_name);

      //add <clinit> to queue
      HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(class_name);
      List<HierarchySootMethod> methods = hclass.getMethods();
      for(HierarchySootMethod method : methods){
        String name = method.getName();
        if(name.equals("<clinit>")){
          //System.out.println("loadStringGraph adding ctor: "+method.getSignature());
          if(dontFollow(method.getSignature())){
            continue;
          }

          m_cgMethodQueue.add(method.getSignature());          
        }
      }
    }
  }

  private void reverseStringGraphVisit(String method_sig, Set<String> reachable){    
    MethodSignatureUtil util = new MethodSignatureUtil();
    HierarchyValueSwitch value_switch = getValueSwitch(method_sig);
    for(String dest_sig : value_switch.getMethodRefs()){
      if(dontFollow(dest_sig)){
        continue;
      }
      if(reachable.contains(dest_sig)){
        //System.out.println("loadStringGraph addEdge: "+method_sig+"->"+dest_sig);
        m_currDfsInfo.getStringCallGraph().addEdge(method_sig, dest_sig);
        reachable.add(method_sig);        

        //add to forward dfs
        if(dontFollow(method_sig) == false){
          m_cgMethodQueue.add(method_sig);
        }

        //add virtual methods to queue
        List<String> virt_methods = m_classHierarchy.getVirtualMethods(method_sig);
        for(String virt_method : virt_methods){
          if(reachable.contains(virt_method) == false){
            reachable.add(virt_method);      

            if(dontFollow(virt_method)){
              continue;
            }
            reverseStringGraphVisit(virt_method, reachable);
          }
        }

        //add <clinit> to reachable
        util.parse(method_sig);
        String class_name = util.getClassName();
        HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(class_name);
        List<HierarchySootMethod> methods = hclass.getMethods();
        for(HierarchySootMethod method : methods){
          if(dontFollow(method.getSignature())){
            continue;
          } 

          String name = method.getName();
          if(name.equals("<clinit>")){
            m_cgMethodQueue.add(method.getSignature());   
            reachable.add(method.getSignature());   
          }
        }
      } 
    }

    processForwardStringCallGraphQueue();
  }


  private boolean findClass(Collection<String> jars, String filename, String class_name) throws Exception {
    for(String jar : jars){
      JarInputStream fin = new JarInputStream(new FileInputStream(jar));
      while(true){
        JarEntry entry = fin.getNextJarEntry();
        if(entry == null){
          break;
        }
        if(entry.getName().equals(filename) == false){
          continue;
        }
        m_classToFilename.put(class_name, m_tempFolder + filename);
        WriteJarEntry writer = new WriteJarEntry();
        writer.write(entry, fin, m_tempFolder);
        fin.close();
        return true;
      }
      fin.close();
    }
    return false;
  }

  private String classNameToFilename(String className){
    String filename = className.replace(".", "/");
    filename += ".class";
    return filename;
  }

  public void findClass(String className) throws Exception {
    String package_name = getPackageName(className);
    String filename = className.replace(".", "/");
    filename += ".class";
    Set<String> jar_cache = m_packageNameCache.get(package_name);
    if(jar_cache == null){
      if(package_name.equals("")){
        findClass(m_classPaths, filename, className);
        return;
      } else {
        return;
      }
    }
    if(findClass(jar_cache, filename, className)){
      return;
    }
    //maybe there is a class in the default package, try all if not found in cache.
    if(package_name.equals("")){
      findClass(m_classPaths, filename, className);
    }
  }

  public ClassSource getClassSource(String class_name) {
    String filename = classNameToFilename(class_name);

    File file = new File(m_tempFolder + filename);
    if(file.exists()){
      m_classToFilename.put(class_name, file.getAbsolutePath());
    }

    if(m_classToFilename.containsKey(class_name) == false){
      try {
        findClass(class_name);
      } catch(Exception ex){
        //ignore
      }
    }

    m_loadedCount++;
    //System.out.println("loading: "+class_name+" count: "+m_loadedCount);

    if(m_classToFilename.containsKey(class_name) == false){
      return null;
    }

    try {
      String full_filename = m_classToFilename.get(class_name);
      //System.out.println("loading: "+class_name+" from: "+full_filename);
      InputStream stream = new FileInputStream(full_filename);
		  if(stream == null){
        return null;
      }
		  return new CoffiClassSource(class_name, stream);
    } catch(FileNotFoundException ex){
      ex.printStackTrace();
      return null;
    }
  }

  private void loadHierarchy(String class_name, Set<String> classes){
    LinkedList<String> queue = new LinkedList<String>();
    queue.add(class_name);
    while(queue.isEmpty() == false){
      String curr_type = queue.removeFirst();
      classes.add(curr_type);
      
      HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(curr_type);
      if(hclass == null){
        continue;
      }

      if(hclass.hasSuperClass()){
        queue.add(hclass.getSuperClass());
      }

      for(String iface : hclass.getInterfaces()){
        queue.add(iface);
      }
    }
  }

  private void loadScene(){
    System.out.println("loading scene...");

    System.out.println("finding hierarchy classes reachable from dfs walk...");

    Set<String> all_types = new HashSet<String>();
    for(DfsInfo dfs_info : m_dfsInfos.values()){
      all_types.addAll(dfs_info.getStringCallGraph().getAllTypes());
    }
    all_types.addAll(m_toHierarchyClasses);
    all_types.addAll(m_toSignaturesClasses);
    all_types.addAll(m_refTypes);

    Set<String> all_classes = new HashSet<String>();
    Set<String> visited_classes = new HashSet<String>();
    for(String type : all_types){   
      loadHierarchy(type, all_classes);
    }

    Set<String> all_sigs = new HashSet<String>();
    for(DfsInfo dfs_info : m_dfsInfos.values()){
      all_sigs.addAll(dfs_info.getStringCallGraph().getAllSignatures());
    }

    Set<String> to_signatures = new HashSet<String>();
    for(String signature_class : m_toSignaturesClasses){
      HierarchySootClass signature_hclass = m_classHierarchy.getHierarchySootClass(signature_class);
      if(signature_hclass == null){
        System.out.println("cannot find: "+signature_class);
        continue;
      }
      List<HierarchySootMethod> signature_methods = signature_hclass.getMethods();
      for(HierarchySootMethod signature_method : signature_methods){
        to_signatures.add(signature_method.getSignature());
      }
    }
    to_signatures.addAll(all_sigs);
    to_signatures.addAll(m_toSignaturesMethods);

    for(String signature : to_signatures){
      MethodSignatureUtil util = new MethodSignatureUtil();
      util.parse(signature);
      String class_name = util.getClassName();

      HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(class_name);
      if(hclass == null){
        continue;
      }

      HierarchySootMethod method = hclass.findMethodBySubSignature(util.getSubSignature());
      if(method == null){
        continue;
      }

      loadHierarchy(class_name, all_classes);
      loadHierarchy(method.getReturnType(), all_classes);
      for(String param_type : method.getParameterTypes()){
        loadHierarchy(param_type, all_classes);
      }
      for(String ex_type : method.getExceptionTypes()){
        loadHierarchy(ex_type, all_classes);
      }
    }

    System.out.println("creating empty classes according to type number...");
    //create all empty classes from lowest number to highest
    StringToType string_to_type = new StringToType();
    List<NumberedType> numbered_types = m_classHierarchy.getNumberedTypes();
    visited_classes.clear();
    for(int i = 0; i < numbered_types.size(); i++){
      String type_string = numbered_types.get(i).getType();
      if(string_to_type.isRefType(type_string) == false){
        continue;
      }      
      if(string_to_type.isArrayType(type_string)){
        continue;
      }
      if(visited_classes.contains(type_string)){
        continue;
      }
      if(all_classes.contains(type_string) == false){
        continue;
      }
      visited_classes.add(type_string);
      HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(type_string);
      SootClass empty_class = new SootClass(type_string, hclass.getModifiers());
      Scene.v().addClass(empty_class);
      if(hclass.isApplicationClass()){
        empty_class.setApplicationClass();
      } else {
        empty_class.setLibraryClass();
      }

      if(hclass.hasSuperClass()){
        SootClass superClass = Scene.v().getSootClass(hclass.getSuperClass());
        empty_class.setSuperclass(superClass);
      }
      for(String iface : hclass.getInterfaces()){
        SootClass ifaceClass = Scene.v().getSootClass(iface);
        empty_class.addInterface(ifaceClass);
      }
    }

    System.out.println("collecting fields for classes and adding to declaring class...");
    //collect fields for classes and add to declaring_class
    Set<String> fields_to_load = new HashSet<String>();
    for(String signature : all_sigs){
      HierarchyValueSwitch value_switch = getValueSwitch(signature);
      for(String field_ref : value_switch.getFieldRefs()){
        fields_to_load.add(field_ref);
      }
    }
    fields_to_load.addAll(m_loadFields);

    for(String field_ref : fields_to_load){
      FieldSignatureUtil util = new FieldSignatureUtil();
      util.parse(field_ref);

      String class_name = util.getDeclaringClass();
      String field_name = util.getName();

      while(true){
        HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(class_name);
        if(hclass.hasField(field_name) == false){
          if(hclass.hasSuperClass() == false){
            System.out.println("cannot find field: "+field_ref);
            break;
          }
          class_name = hclass.getSuperClass();
          continue;
        }

        SootClass declaring_class = Scene.v().getSootClass(class_name);
        if(declaring_class.declaresFieldByName(field_name)){
          break;
        }

        int field_modifiers = hclass.getFieldModifiers(field_name);
        Type field_type = string_to_type.convert(util.getType());
        SootField new_field = new SootField(field_name, field_type, field_modifiers);
        declaring_class.addField(new_field);

        break;
      }
    }

    //add empty methods
    System.out.println("adding empty methods...");
    Set<String> visited = new HashSet<String>();
    for(String signature : to_signatures){
      MethodSignatureUtil util = new MethodSignatureUtil();
      util.parse(signature);
      String class_name = util.getClassName();

      if(string_to_type.isArrayType(class_name)){
        continue;
      }

      HierarchySootMethod method = m_classHierarchy.findMethod(util.getSignature());
      if(method == null){
        continue;
      }

      if(visited.contains(method.getSignature())){
        continue;
      }
      visited.add(method.getSignature());

      List<Type> parameterTypes = new ArrayList<Type>();
      for(String paramType : method.getParameterTypes()){
        parameterTypes.add(string_to_type.convert(paramType));
      }
      Type returnType = string_to_type.convert(method.getReturnType());
      int modifiers = method.getModifiers();
      List<SootClass> thrownExceptions = new ArrayList<SootClass>();
      for(String exception : method.getExceptionTypes()){
        SootClass ex_class = Scene.v().getSootClass(exception);
        thrownExceptions.add(ex_class);
      }
      SootMethod soot_method = new SootMethod(method.getName(), parameterTypes,
        returnType, modifiers, thrownExceptions);

      util.parse(method.getSignature());
      class_name = util.getClassName();

      SootClass soot_class = Scene.v().getSootClass(class_name);
      soot_class.addMethod(soot_method);
    }

    System.out.println("adding method bodies...");
    //add method bodies
    visited.clear();
    for(String signature : all_sigs){
      MethodSignatureUtil util = new MethodSignatureUtil();
      util.parse(signature);
      String class_name = util.getClassName();

      if(string_to_type.isArrayType(class_name)){
        continue;
      }

      HierarchySootMethod method = m_classHierarchy.findMethod(util.getSignature());
      if(method == null){
        continue;
      }

      if(visited.contains(method.getSignature())){
        continue;
      }
      visited.add(method.getSignature());

      util.parse(method.getSignature());
      class_name = util.getClassName();
      
      SootClass soot_class = Scene.v().getSootClass(class_name);
      if(soot_class.declaresMethod(method.getSubSignature()) == false){
        continue;
      }

      SootMethod soot_method = soot_class.getMethod(method.getSubSignature());

      if(soot_method.isConcrete() == false){
        continue;
      }

      System.out.println("  loading method: "+soot_method.getSignature());

      Body body = method.getBody(soot_method, "jb");
      SpecialInvokeFixup fixer = new SpecialInvokeFixup();
      body = fixer.fixup(body);
      soot_method.setActiveBody(body);

    }
    System.out.println("Total loaded classes: " + all_classes.size());
    System.out.println("Total loaded methods: " + all_sigs.size());
  }

  private HierarchyValueSwitch getValueSwitch(String signature){
    if(m_valueSwitchMap.containsKey(signature)){
      return m_valueSwitchMap.get(signature);
    } else {
      HierarchyValueSwitch value_switch = new HierarchyValueSwitch();
      value_switch.run(signature);
      m_valueSwitchMap.put(signature, value_switch);
      return value_switch;
    }
  }

  private void sortApplicationClasses(){
    //System.out.println("sorting application classes...");
    //java.util.Collections.sort(m_appClasses);
  }

  public boolean isLoaded(){
    return m_loaded;
  }

  public Set<String> getNewInvokes(){
    return m_newInvokes;
  }

/*
  private void cloneLibraryClasses(){
    System.out.println("cloning library classes...");
    m_remapping = new HashMap<String, String>();

    BuiltInRemaps built_in = new BuiltInRemaps();

    Set<String> lib_classes = m_stringCG.getLibraryClasses();
    String prefix = Options.v().rbcl_remap_prefix();

    for(String lib_class : lib_classes){
      if(built_in.containsKey(lib_class)){
        m_remapping.put(lib_class, built_in.get(lib_class));
        continue;
      }

      if(m_remapClassName.shouldMap(lib_class) == false){
        m_remapping.put(lib_class, lib_class);
        continue;
      }

      String new_name = prefix+lib_class;
    
      SootClass soot_class = Scene.v().getSootClass(lib_class);
      CloneClass cloner = new CloneClass();
      SootClass new_class = cloner.execute(soot_class, new_name, m_stringCG, m_reachableFields);

      Scene.v().addGeneratedClass(new_class);
      m_remapping.put(lib_class, new_name);
    }
  }

  private void remapFields(){
    System.out.println("remapping fields...");
    Set<String> lib_classes = m_stringCG.getLibraryClasses();
    BuiltInRemaps built_ins = new BuiltInRemaps();

    for(String field_sig : m_reachableFields){
      FieldSignatureUtil util = new FieldSignatureUtil();
      util.parse(field_sig);

      String declaring_class = util.getDeclaringClass();
      if(built_ins.containsKey(declaring_class)){
        if(built_ins.isRealMapping(declaring_class)){
          continue;
        }
      }

      if(isKeepPackage(declaring_class)){
        continue;
      }

      if(lib_classes.contains(declaring_class)){
        String new_class = m_remapClassName.getMapping(declaring_class);
        util.setDeclaringClass(new_class);
      }

      StringToType converter = new StringToType();
      Type field_type = converter.toType(util.getType());

      SootField soot_field = util.getSootField();
      if(field_type instanceof RefType){
        String type_string = util.getType();
        if(lib_classes.contains(type_string)){
          String new_type_string = m_remapClassName.getMapping(type_string);
          Type new_type = converter.toType(new_type_string);
          soot_field.setType(new_type);          
        }
      } else if(field_type instanceof ArrayType){
        ArrayType array_type = (ArrayType) field_type;
        Type base_type = array_type.baseType;
        if(base_type instanceof RefType){
          RefType ref_type = (RefType) base_type;
          String class_name = ref_type.getClassName();
          if(lib_classes.contains(class_name)){
            String new_type_string = m_remapClassName.getMapping(class_name);
            Type new_base_type = converter.toType(new_type_string);
            Type new_type = ArrayType.v(new_base_type, array_type.numDimensions);
            soot_field.setType(new_type); 
          }
        }
      }
    }
  }

  private String remapClassOfMethodSignature(String signature){
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(signature);
 
    String class_name = util.getClassName();
    String new_class_name = m_remapClassName.getMapping(class_name);
    util.setClassName(new_class_name);        

    return util.getSignature();
  }

  private void remapTypes(){    
    System.out.println("remapping types...");
    Iterator<SootClass> iter = Scene.v().getClasses().snapshotIterator();

    //remapping superclass, interfaces and outerclass
    while(iter.hasNext()){
      SootClass soot_class = iter.next();
      if(soot_class.hasSuperclass()){
        SootClass super_class = soot_class.getSuperclass();
        super_class = m_remapClassName.getMapping(super_class);
        soot_class.setSuperclass(super_class);
      }
      Chain<SootClass> interfaces = soot_class.getInterfaces();
      if(interfaces.size() != 0){
        SootClass curr = interfaces.getFirst();
        while(curr != null){
          SootClass next = interfaces.getSuccOf(curr);
          SootClass remapped = m_remapClassName.getMapping(curr);
          if(remapped.equals(curr) == false){
            interfaces.swapWith(curr, remapped);
          }
          curr = next;  
        }
      }
      if(soot_class.hasOuterClass()){
        SootClass outer_class = soot_class.getOuterClass();
        outer_class = m_remapClassName.getMapping(outer_class);
        soot_class.setOuterClass(outer_class);        
      }
    }

    //remapping arguments of all signatures from string call graph
    BuiltInRemaps built_ins = new BuiltInRemaps();
    Set<String> all = m_stringCG.getAllSignatures();
    for(String signature : all){
      MethodSignatureUtil util = new MethodSignatureUtil();
      util.parse(signature);

      String class_name = util.getClassName();
      boolean throw_error = true;
      if(built_ins.isRealMapping(class_name)){
        throw_error = false;
      }

      String declaring_class = util.getClassName();
      if(isKeepPackage(declaring_class)){
        continue;
      }

      try {
        signature = remapClassOfMethodSignature(signature);      
        util.parse(signature);

        SootMethod soot_method = util.getSootMethod();

        RemapMethod remapper = new RemapMethod();
        remapper.fixArguments(soot_method);
      } catch(RuntimeException ex){
        if(throw_error){
          throw ex;
        }
      }
    }

    //remapping bodies
    for(String signature : all){
      MethodSignatureUtil util = new MethodSignatureUtil();
      util.parse(signature);

      String class_name = util.getClassName();
      boolean throw_error = true;
      if(built_ins.isRealMapping(class_name)){
        throw_error = false;
      }

      String declaring_class = util.getClassName();
      if(isKeepPackage(declaring_class)){
        continue;
      }

      try {
        util.remap();

        SootMethod soot_method = util.getSootMethod();
  
        RemapMethod remapper = new RemapMethod();
        remapper.visit(soot_method);
      } catch(RuntimeException ex){
        if(throw_error){
          throw ex;
        }
      }
    }
  }
*/

  private void dfsForRootbeer(){
    String signature = m_currDfsInfo.getRootMethodSignature();
    
    Set<String> visited = new HashSet<String>();
    System.out.println("doing rootbeer dfs: "+signature);
    LinkedList<String> queue = new LinkedList<String>();
    queue.add(signature);

    for(ConditionalCudaEntry conditional_entry : m_conditionalCudaEntries){
      if(conditional_entry.condition(m_currDfsInfo)){
        queue.add(conditional_entry.getSignature());
      }
    }

    while(queue.isEmpty() == false){
      String curr = queue.get(0);
      queue.removeFirst();
      doDfsForRootbeer(curr, queue, visited);
    }
  }

  private void doDfsForRootbeer(String signature, LinkedList<String> queue, Set<String> visited){
    HierarchyValueSwitch value_switch = getValueSwitch(signature);
    StringToType converter = new StringToType();
    FieldSignatureUtil futil = new FieldSignatureUtil();
    MethodSignatureUtil mutil = new MethodSignatureUtil();

    mutil.parse(signature);
    m_currDfsInfo.addType(mutil.getClassName());
    m_currDfsInfo.addType(mutil.getReturnType());
    m_currDfsInfo.addMethod(signature);

    List<String> virt_methods = m_classHierarchy.getVirtualMethods(signature);
    for(String virt_method : virt_methods){
      if(dontFollow(virt_method)){
        continue;
      }

      if(visited.contains(virt_method)){
        continue;
      }
      visited.add(virt_method);
      //System.out.println("doDfsForRootbeer adding virtual_method to queue: "+virt_method);
      queue.add(virt_method);
    }

    for(String type_str : value_switch.getAllTypes()){
      Type type = converter.convert(type_str);
      m_currDfsInfo.addType(type);
    }    

    for(String method_sig : value_switch.getMethodRefs()){
      if(dontFollow(method_sig)){
        continue;
      }
    
      if(visited.contains(method_sig) == false){
        queue.add(method_sig);
        visited.add(method_sig);
      }
    }

    for(String field_ref : value_switch.getFieldRefs()){
      futil.parse(field_ref);
      SootField soot_field = futil.getSootField();
      m_currDfsInfo.addField(soot_field);
    }

    for(String instanceof_str : value_switch.getInstanceOfs()){
      Type type = converter.convert(instanceof_str);
      m_currDfsInfo.addInstanceOf(type);
    }
  }

  public List<SootMethod> getEntryPoints(){
    List<SootMethod> ret = new ArrayList<SootMethod>();
    
    for(String entry : m_entryPoints){
      System.out.println("getEntryPoints: "+entry);
      MethodSignatureUtil util = new MethodSignatureUtil();
      util.parse(entry);
      ret.add(util.getSootMethod());
    }
    return ret;
  }

  public void loadDfsInfo(SootMethod entry){
    String sig = entry.getSignature();
    m_currDfsInfo = m_dfsInfos.get(sig);
  }

  private void loadHierarchySootClasses(){
    HierarchySootClassFactory hclassFactory = new HierarchySootClassFactory();
    List<String> paths = SourceLocator.v().classPath();
    List<String> local_paths = new ArrayList<String>();
    local_paths.addAll(paths);
    if(m_userJar != null){
      local_paths.add(m_userJar);
    }
    String[] to_cache = new String[local_paths.size()];
    to_cache = local_paths.toArray(to_cache);
    Arrays.sort(to_cache);
    Set<String> visited = new HashSet<String>();
    for(String jar : to_cache){
      if(jar.endsWith(".jar")){
        File file = new File(jar);
        if(file.exists() == false){
          continue;
        }
        if(visited.contains(jar)){
          continue;
        }
        visited.add(jar);
        System.out.println("caching package names for: "+jar);
        try {
          CountingInputStream count_stream = new CountingInputStream(new FileInputStream(jar));
          JarInputStream jin = new JarInputStream(count_stream);
          while(true){
            JarEntry entry = jin.getNextJarEntry();
            if(entry == null){
              break;
            }

            String name = entry.getName();
            String package_name;
            if(name.endsWith(".class")){
              String filename = name;
              name = name.replace(".class", "");
              name = name.replace("/", ".");
              package_name = getPackageName(name);

              boolean app_class = false;
              if(jar.equals(m_userJar)){
                app_class = true;
                m_appClasses.add(name);
              }
              HierarchySootClass hierarchy_class = hclassFactory.create(filename, jin);
              if(hierarchy_class == null){
                System.out.println("invalid class: "+filename);
                continue;
              }
              hierarchy_class.setApplicationClass(app_class);
              m_classHierarchy.put(name, hierarchy_class);

            } else {
              name = name.replace("/", ".");
              package_name = name.substring(0, name.length()-1);
            }
            if(m_packageNameCache.containsKey(package_name)){
              Set<String> jars = m_packageNameCache.get(package_name);
              if(jars.contains(jar) == false){
                jars.add(jar);
              }
            } else {
              Set<String> jars = new HashSet<String>();
              jars.add(jar);
              m_packageNameCache.put(package_name, jars);
            }
          }
          jin.close();
        } catch(Exception ex){
          ex.printStackTrace();
        }
      }
    }
  }

  private void buildClassHierarchy(){
    System.out.println("building class hierarchy...");
    m_classHierarchy.build();
    System.out.println("caching virtual methods...");
    m_classHierarchy.cacheVirtualMethods();
    //System.out.println(m_classHierarchy.toString());
  }

  private void loadFollowsStrings(){
    System.out.println("loading follows strings...");
    loadMethodStrings(m_followMethodTesters, m_followMethods);
    loadClassStrings(m_followClassTesters, m_followClasses);
  }

  private void loadToSignaturesString(){
    System.out.println("loading to_signatures strings...");
    loadMethodStrings(m_toSignaturesMethodTesters, m_toSignaturesMethods);
    loadClassStrings(m_toSignaturesClassTesters, m_toSignaturesClasses);
  }

  private void loadMethodStrings(List<MethodTester> testers, Set<String> dest){
    Collection<HierarchySootClass> classes = m_classHierarchy.getHierarchyClasses();
    for(MethodTester tester : testers){
      for(HierarchySootClass hclass : classes){
        List<HierarchySootMethod> methods = hclass.getMethods();
        for(HierarchySootMethod hmethod : methods){
          if(tester.test(hmethod)){
            dest.add(hmethod.getSignature());
          }
        }
      }
    }
  }

  private void loadClassStrings(List<ClassTester> testers, Set<String> dest){
    Collection<HierarchySootClass> classes = m_classHierarchy.getHierarchyClasses();
    for(ClassTester tester : testers){
      for(HierarchySootClass hclass : classes){
        if(tester.test(hclass)){
          dest.add(hclass.getName());
        }
      }
    }
  }

  private String getPackageName(String className) {
    String[] tokens = className.split("\\.");
    String ret = "";
    for(int i = 0; i < tokens.length - 1; ++i){
      ret += tokens[i];
      if(i < tokens.length - 2){
        ret += ".";
      }
    }
    return ret;
  }

  public int getClassNumber(SootClass soot_class){
    return getClassNumber(soot_class.getName());
  }

  public int getClassNumber(String type_string){
    NumberedType ntype = m_classHierarchy.getNumberedType(type_string);
    return (int) ntype.getNumber();
  }

  private void findEntryPoints(){
    System.out.println("finding entry points...");
    m_entryPoints = new ArrayList<String>();
    System.out.println("app_classes: "+m_appClasses);
    for(String app_class : m_appClasses){
      HierarchySootClass hclass = m_classHierarchy.getHierarchySootClass(app_class);
      List<HierarchySootMethod> methods = hclass.getMethods();
      for(HierarchySootMethod method : methods){
        if(testMethod(m_entryMethodTesters, method)){
          m_entryPoints.add(method.getSignature());
        }
      }
    }
  }

  private boolean dontFollow(String signature){
    if(dontFollowMethod(signature)){
      return true;
    }
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(signature);
    if(dontFollowClass(util.getClassName())){
      return true;
    }
    return false;
  }

  private boolean dontFollowMethod(String signature){
    HierarchySootMethod hmethod = getHierarchySootMethod(signature);
    if(hmethod == null){
      return false;
    }
    return testMethod(m_dontFollowMethodTesters, hmethod);
  }

  private boolean dontFollowClass(String class_name) {
    HierarchySootClass hclass = getHierarchySootClass(class_name);
    if(hclass == null){
      return false;
    }
    return testClass(m_dontFollowClassTesters, hclass);
  }

  private boolean testMethod(List<MethodTester> testers, HierarchySootMethod hmethod){
    for(MethodTester tester : testers){
      if(tester.test(hmethod)){
        return true;
      }
    }
    return false;
  }

  private boolean testClass(List<ClassTester> testers, HierarchySootClass hclass){
    for(ClassTester tester : testers){
      if(tester.test(hclass)){
        return true;
      }
    }
    return false;
  }

  private HierarchySootClass getHierarchySootClass(String class_name){
    return m_classHierarchy.getHierarchySootClass(class_name);
  }

  private HierarchySootMethod getHierarchySootMethod(String signature){
    MethodSignatureUtil util = new MethodSignatureUtil();
    util.parse(signature);
    HierarchySootClass hclass = getHierarchySootClass(util.getClassName());
    if(hclass == null){
      return null;
    }
    return hclass.findMethodBySubSignature(util.getSubSignature());
    
  }

  public void addSignaturesClass(String class_name){
    m_toSignaturesClasses.add(class_name);
  }

  public DfsInfo getDfsInfo(){
    return m_currDfsInfo;
  }

}
