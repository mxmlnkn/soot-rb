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
import soot.jimple.*;

public class DfsValueSwitch implements JimpleValueSwitch {

  private Set<Type> m_types;
  private Set<Type> m_instanceOfs;
  private Set<DfsMethodRef> m_methods;
  private Set<SootFieldRef> m_fields;
  private Stmt m_currStmt;
  private ClassConstantReader m_classConstantReader;
  private SootMethod m_method;

  public DfsValueSwitch(){
    m_method = null;
    m_types = new LinkedHashSet<Type>();
    m_methods = new LinkedHashSet<DfsMethodRef>();
    m_fields = new LinkedHashSet<SootFieldRef>();   
    m_instanceOfs = new LinkedHashSet<Type>();
    m_classConstantReader = new ClassConstantReader();
  }
  
  public void run(SootMethod method) {
    m_method = method;
    
    addType(method.getReturnType());
    List<Type> param_types = method.getParameterTypes();
    for(Type param_type : param_types){
      addType(param_type);
    }
    
    if(method.isConcrete() == false){
      return;
    }

    SootResolver.v().resolveClass(method.getDeclaringClass().getName(), SootClass.BODIES);

    Body body = method.retrieveActiveBody();
    Iterator<Unit> iter = body.getUnits().iterator();
    while(iter.hasNext()){
      m_currStmt = (Stmt) iter.next();
      List<ValueBox> boxes = m_currStmt.getUseAndDefBoxes();
      for(ValueBox box : boxes){
        Value value = box.getValue();
        value.apply(this);
      }
    }
  }

  public Set<Type> getAllTypes(){
    Set<Type> ret = new HashSet<Type>();
    ret.addAll(m_types);
    ret.addAll(m_instanceOfs);
    for(SootFieldRef ref : m_fields){
      ret.add(RefType.v(ref.declaringClass()));
      ret.add(ref.type());
    }
    for(DfsMethodRef ref : m_methods){
      SootMethodRef sref = ref.getSootMethodRef();
      ret.add(RefType.v(sref.declaringClass()));
      List<Type> params = sref.parameterTypes();
      for(Type param : params){
        ret.add(param);
      }
      ret.add(sref.returnType());
    }
    return ret;
  }
  
  public Set<Type> getTypes(){
    return m_types;
  }
  
  public Set<DfsMethodRef> getMethodRefs(){
    return m_methods;
  }
  
  public Set<SootFieldRef> getFieldRefs(){
    return m_fields;
  }
  
  public Set<Type> getInstanceOfs(){
    return m_instanceOfs;
  }
  
  public void addType(Type type){
    if(m_types.contains(type) == false){
      m_types.add(type);
    }
  }
  
  public void addMethodRef(SootMethodRef ref){
    DfsMethodRef dfs_ref = new DfsMethodRef(ref, m_currStmt);
    if(m_methods.contains(dfs_ref) == false){
      m_methods.add(dfs_ref);
    }
  }
  
  public void addFieldRef(SootFieldRef ref){
    if(m_fields.contains(ref) == false){
      m_fields.add(ref);
    }
  }

  private void addInstanceOfType(Type type) {
    if(m_instanceOfs.contains(type) == false){
      m_instanceOfs.add(type);
    }
  }
  
  public void caseLocal(Local local) {
    addType(local.getType());
  }

  public void caseDoubleConstant(DoubleConstant dc) {
    addType(dc.getType());
  }

  public void caseFloatConstant(FloatConstant fc) {
    addType(fc.getType());
  }

  public void caseIntConstant(IntConstant ic) {
    addType(ic.getType());
  }

  public void caseLongConstant(LongConstant lc) {
    addType(lc.getType());
  }

  public void caseNullConstant(NullConstant nc) {
    addType(nc.getType());
  }

  public void caseStringConstant(StringConstant sc) {
    addType(sc.getType());
  }

  public void caseClassConstant(ClassConstant cc) {
    String value = cc.getValue();
    Type type = m_classConstantReader.stringToType(value);
    addType(type);
  }

  public void defaultCase(Object o) {
  }

  public void caseAddExpr(AddExpr ae) {
    addType(ae.getType());
  }

  public void caseAndExpr(AndExpr ae) {
    addType(ae.getType());
  }

  public void caseCmpExpr(CmpExpr ce) {
    addType(ce.getType());
  }

  public void caseCmpgExpr(CmpgExpr ce) {
    addType(ce.getType());
  }

  public void caseCmplExpr(CmplExpr ce) {
    addType(ce.getType());
  }

  public void caseDivExpr(DivExpr de) {
    addType(de.getType());
  }

  public void caseEqExpr(EqExpr eqexpr) {
    addType(eqexpr.getType());
  }

  public void caseNeExpr(NeExpr neexpr) {
    addType(neexpr.getType());
  }

  public void caseGeExpr(GeExpr geexpr) {
    addType(geexpr.getType());
  }

  public void caseGtExpr(GtExpr gtexpr) {
    addType(gtexpr.getType());
  }

  public void caseLeExpr(LeExpr leexpr) {
    addType(leexpr.getType());
  }

  public void caseLtExpr(LtExpr ltexpr) {
    addType(ltexpr.getType());
  }

  public void caseMulExpr(MulExpr me) {
    addType(me.getType());
  }

  public void caseOrExpr(OrExpr orexpr) {
    addType(orexpr.getType());
  }

  public void caseRemExpr(RemExpr re) {
    addType(re.getType());
  }

  public void caseShlExpr(ShlExpr se) {
    addType(se.getType());
  }

  public void caseShrExpr(ShrExpr se) {
    addType(se.getType());
  }

  public void caseUshrExpr(UshrExpr ue) {
    addType(ue.getType());
  }

  public void caseSubExpr(SubExpr se) {
    addType(se.getType());
  }

  public void caseXorExpr(XorExpr xe) {
    addType(xe.getType());
  }

  public void caseInterfaceInvokeExpr(InterfaceInvokeExpr iie) {
    addMethodRef(iie.getMethodRef());
  }

  public void caseSpecialInvokeExpr(SpecialInvokeExpr sie) {
    addMethodRef(sie.getMethodRef());
  }

  public void caseStaticInvokeExpr(StaticInvokeExpr sie) {
    addMethodRef(sie.getMethodRef());
  }

  public void caseVirtualInvokeExpr(VirtualInvokeExpr vie) {
    addMethodRef(vie.getMethodRef());
  }

  public void caseDynamicInvokeExpr(DynamicInvokeExpr die) {
  }

  public void caseCastExpr(CastExpr ce) {
    addType(ce.getCastType());
  }

  public void caseInstanceOfExpr(InstanceOfExpr ioe) {
    addType(ioe.getCheckType());
    addInstanceOfType(ioe.getCheckType());
  }

  public void caseNewArrayExpr(NewArrayExpr nae) {
    addType(nae.getBaseType());
  }

  public void caseNewMultiArrayExpr(NewMultiArrayExpr nmae) {
    addType(nmae.getBaseType());
  }

  public void caseNewExpr(NewExpr ne) {
    addType(ne.getBaseType());
  }

  public void caseLengthExpr(LengthExpr le) {
    addType(le.getType());
  }

  public void caseNegExpr(NegExpr ne) {
    addType(ne.getType());
  }

  public void caseArrayRef(ArrayRef ar) {
    addType(ar.getType());
  }

  public void caseStaticFieldRef(StaticFieldRef sfr) {
    SootFieldRef field_ref = sfr.getFieldRef();
    SootClass field_decl_class = field_ref.declaringClass();
    addType(field_decl_class.getType());

    addType(sfr.getField().getType());
    addFieldRef(sfr.getFieldRef());
  }

  public void caseInstanceFieldRef(InstanceFieldRef ifr) {
    addType(ifr.getBase().getType());
    addFieldRef(ifr.getFieldRef());
  }

  public void caseParameterRef(ParameterRef pr) {
    addType(pr.getType());
  }

  public void caseCaughtExceptionRef(CaughtExceptionRef cer) {
    addType(cer.getType());
  }

  public void caseThisRef(ThisRef tr) {
    addType(tr.getType());
  }
}
