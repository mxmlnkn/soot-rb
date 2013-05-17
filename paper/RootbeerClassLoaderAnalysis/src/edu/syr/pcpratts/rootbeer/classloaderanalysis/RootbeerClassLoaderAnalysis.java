
package edu.syr.pcpratts.rootbeer.classloaderanalysis;

import com.javamex.classmexer.MemoryUtil;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootResolver;
import soot.Transform;
import soot.rbclassload.RootbeerClassLoader;

public class RootbeerClassLoaderAnalysis {

  public void run(String folder){      
    
    Stopwatch watch = new Stopwatch();
    watch.start();
    
    PackManager.v().getPack("wjpp").add(new Transform("wjpp.stats", new StatsTransformer("wjpp")));
    PackManager.v().getPack("wjtp").add(new Transform("wjtp.stats", new StatsTransformer("wjtp")));
    
    RootbeerClassLoader.v().setUserJar("classes/soap2013_example.jar");
    RootbeerClassLoader.v().addEntryMethodTester(new EntryPointDetector());
    
    String[] args = {
      "-pp",
      "-process-dir", "classes/soap2013_example.jar",
      "-rbcl",
      "-w",
      //"-p", "cg", "enabled:true",
      //"-p", "cg", "implicit-entry:false",
      //"-p", "cg.cha", "enabled:false",
      //"-p", "cg.spark", "enabled:true",
    };
    //soot.Main.main(args);
    RootbeerClassLoader.v().loadNecessaryClasses();
          
    long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    long memoryUsedClasses = MemoryUtil.deepMemoryUsageOfAll(Scene.v().getClasses(), MemoryUtil.VisibilityFilter.ALL);
    long memoryUsedScene = MemoryUtil.deepMemoryUsageOf(Scene.v(), MemoryUtil.VisibilityFilter.ALL);
    
    System.out.println("memory_used: "+memoryUsed);
    System.out.println("memory_used_classes: "+memoryUsedClasses);
    System.out.println("memory_used_scene: "+memoryUsedScene);
    
    watch.stop();
    System.out.println("elapsed time: "+watch.elapsedTimeMillis());
  }
  
  public static void main(String[] args) {
    RootbeerClassLoaderAnalysis analysis = new RootbeerClassLoaderAnalysis();
    analysis.run("classes/");
  }
}

/*
     List<String> proc_dir = new ArrayList<String>();
    proc_dir.add(RootbeerPaths.v().getJarContentsFolder());
    
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_rbclassload(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_process_dir(proc_dir);
    Options.v().set_soot_classpath(rootbeer_jar);
    RootbeerClassLoader.v().addEntryMethodTester(m_entryDetector);
    RootbeerClassLoader.v().loadNecessaryClasses();
 */
