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
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import soot.Modifier;

public class HierarchySootMethod {

  private String m_name;
  private String m_returnType;
  private List<String> m_parameterTypes;
  private List<String> m_exceptionTypes;
  private int m_modifiers;
  private List<HierarchyInstruction> m_instructions;
  private HierarchySootClass m_class;

  public HierarchySootMethod(String name, String returnType,
    List<String> parameterTypes, List<String> exceptionTypes,
    int modifiers, List<HierarchyInstruction> instructions){

    m_name = name;
    m_returnType = returnType;
    m_parameterTypes = parameterTypes;
    m_exceptionTypes = exceptionTypes;
    m_modifiers = modifiers;
    m_instructions = instructions;
  }

  public String getName(){
    return m_name;
  }

  public String getReturnType(){
    return m_returnType;
  }

  public List<String> getParameterTypes(){
    return m_parameterTypes;
  }

  public List<String> getExceptionTypes(){
    return m_exceptionTypes;
  }

  public List<HierarchyInstruction> getInstructions(){
    return m_instructions;
  }

  public void setHierarchySootClass(HierarchySootClass hclass){
    m_class = hclass;
  }

  public HierarchySootClass getHierarchySootClass(){
    return m_class;
  }

  public boolean isConcrete(){
    return !Modifier.isInterface(getModifiers()) && !Modifier.isAbstract(getModifiers());
  }

  public int getModifiers(){
    return m_modifiers;
  }

  public String getSignature(){
    StringBuilder ret = new StringBuilder();
    ret.append("<");
    ret.append(m_class.getName());
    ret.append(": ");
    ret.append(getSubSignature());
    ret.append(">");
    return ret.toString();
  }

  public String getSubSignature(){
    StringBuilder ret = new StringBuilder();
    ret.append(m_returnType);
    ret.append(" ");
    ret.append(m_name);
    ret.append("(");
    for(int i = 0; i < m_parameterTypes.size(); ++i){
      String param_type = m_parameterTypes.get(i);
      ret.append(param_type);
      if(i < m_parameterTypes.size() - 1){
        ret.append(",");
      }
    }
    ret.append(")");
    return ret.toString();
  }
}
