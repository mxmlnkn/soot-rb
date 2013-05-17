
package edu.syr.pcpratts.rootbeer.classloaderanalysis;

import soot.rbclassload.HierarchySootMethod;
import soot.rbclassload.MethodTester;

public class EntryPointDetector implements MethodTester {

  @Override
  public boolean test(HierarchySootMethod hsm) {
    if(hsm.getSubSignature().equals("void main(java.lang.String[])")){
      return true;
    }
    return false;
  }
  
}
