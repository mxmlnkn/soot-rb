import com.javamex.classmexer.MemoryUtil;
import org.apache.commons.io.FileUtils;
import soot.*;
import soot.jimple.Stmt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StatsMain {

  private static class StatsTransformer extends SceneTransformer {
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

    private static class DependencyTransformer extends SceneTransformer {

        @Override
        protected void internalTransform(String phaseName, Map options) {
            final int MAX_DEPTH = 50;
            final int MAX_LEVEL = SootClass.SIGNATURES ;
            try{
                File output = new File("classdeps-"+MAX_DEPTH+"-level-"+MAX_LEVEL+".dot");
                FileWriter fw = new FileWriter(output);
                fw.write("digraph dep{\n");
                fw.write("rankdir=LR;\n");
                //fw.write("landscape=true;\n");
                Queue<SootClass> worklist = new LinkedList<SootClass>();
                worklist.add(Scene.v().getSootClass("A"));
                Set<Integer> visited = new HashSet<Integer>(MAX_DEPTH);

                SootClass objectClass = Scene.v().getSootClass("java.lang.Object");

                while (worklist.size() > 0){
                    SootClass next = worklist.poll();
                    if (!visited.contains(next.getNumber())){
                        fw.write(next.getNumber() + " [label="+next.getShortName().replace('$','_')+"];\n");
                        visited.add(next.getNumber());

                        if (visited.size() < MAX_DEPTH){
                            Set<SootClass> localWorklist = new HashSet<SootClass>(); //to avoid duplicate edges

                            if (MAX_LEVEL >= SootClass.HIERARCHY){
                            //Simulates the HIERARCHY class loading level
                                if (next.hasSuperclass())
                                    localWorklist.add(next.getSuperclass());
                                if (next.getInterfaceCount() > 0)
                                    localWorklist.addAll(next.getInterfaces());
                                if (next.hasOuterClass())
                                    localWorklist.add(next.getOuterClass());
                            }

                            if (MAX_LEVEL >= SootClass.SIGNATURES){
                            //Simulates the SIGNATURES class loading level
                                for (SootField sf : next.getFields()){
                                   localWorklist.add(sf.getDeclaringClass());
                                }

                                for (SootMethod sm : next.getMethods()){
                                    Type returnType = sm.getReturnType();
                                    if (returnType instanceof RefType)
                                        localWorklist.add(((RefType) returnType).getSootClass());
                                    localWorklist.addAll(sm.getExceptions());
                                    for (Type typ : sm.getParameterTypes()){
                                        if (typ instanceof RefType){
                                            localWorklist.add(((RefType) typ).getSootClass());
                                        }
                                    }
                                }
                            }

                            if (MAX_LEVEL >= SootClass.BODIES){
                            //Simulates the BODIES class loading level
                                for (SootMethod sm : next.getMethods()){
                                    if (sm.isConcrete()){
                                        for (Unit u : sm.retrieveActiveBody().getUnits()){
                                            if (((Stmt)u).containsInvokeExpr()){
                                                SootClass target = ((Stmt) u).getInvokeExpr().getMethod().getDeclaringClass();
                                                localWorklist.add(target);

                                            } else if (((Stmt) u).containsFieldRef()){
                                                SootClass target = ((Stmt) u).getFieldRef().getField().getDeclaringClass();
                                                localWorklist.add(target);
                                            }
                                        }
                                    }
                                }
                            }
                            for (SootClass target : localWorklist){
                                if (target != next){
                                    fw.write(next.getNumber() + " -> "+target.getNumber() + ";\n");
                                    worklist.add(target);
                                }
                            }
                        }
                    }
                }

                fw.write("}");
                fw.close();
            } catch (IOException e){

            }

        }
    }


  public static void main(String[] args){

    PackManager.v().getPack("wjpp").add(new Transform("wjpp.stats", new StatsTransformer("wjpp")));
    PackManager.v().getPack("wjtp").add(new Transform("wjtp.stats", new StatsTransformer("wjtp")));
    PackManager.v().getPack("wjpp").add(new Transform("wjpp.graph", new DependencyTransformer()));

    soot.Main.main(new String[]{
           "-time",
          //  "-rbcl",
            "-w","-prepend-classpath", "-output-format", "none", "-soot-classpath", args[0], "-main-class", "A", "A"
    });
  }
}
