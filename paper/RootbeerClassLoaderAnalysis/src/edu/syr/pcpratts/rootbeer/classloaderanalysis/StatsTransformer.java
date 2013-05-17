package edu.syr.pcpratts.rootbeer.classloaderanalysis;

import com.javamex.classmexer.MemoryUtil;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import soot.G;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;

public class StatsTransformer extends SceneTransformer {
  private final String phase;

  public StatsTransformer(final String phase){
    this.phase = phase;
  }

  @Override
  protected void internalTransform(String phaseName, Map options) {
    long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    long memoryUsedClasses = MemoryUtil.deepMemoryUsageOfAll(Scene.v().getClasses(), MemoryUtil.VisibilityFilter.ALL);
    long memoryUsedScene = MemoryUtil.deepMemoryUsageOf(Scene.v(), MemoryUtil.VisibilityFilter.ALL);

    int resolvedBodies =0, resolvedSignatures= 0 , resolvedHierarchy = 0, dandling = 0;
    int numMethods = 0;

    for (SootClass sc : Scene.v().getClasses()){
      if (sc.resolvingLevel() == SootClass.DANGLING){
          dandling ++;
      }
      else if (sc.resolvingLevel() == SootClass.HIERARCHY){
          resolvedHierarchy++;
      }
      else if (sc.resolvingLevel() == SootClass.SIGNATURES){
          resolvedSignatures++;
          numMethods += sc.getMethodCount();
      } else if (sc.resolvingLevel() == SootClass.BODIES){
          resolvedBodies++;
          numMethods += sc.getMethodCount();
      }
    }

    G.v().out.println("Dandling: " + dandling + " Hierarchy: " + resolvedHierarchy + " Signatures: " + resolvedSignatures + " Bodies: "+ resolvedBodies +". Num SootMethod: "+ numMethods);
    G.v().out.println("Memory used in "+phase+" phase: " + memoryUsed / (float) FileUtils.ONE_MB + " Mb\tMemory used for SootClass objects: "
            + memoryUsedClasses / (float)FileUtils.ONE_MB + " Mb Scene size: " + memoryUsedScene/ (float)FileUtils.ONE_MB +" Mb");

  }
}