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

public class HierarchyInstruction {

  private String m_name;
  private List<Operand> m_operands;

  public HierarchyInstruction(String name, List<Operand> operands){
    m_name = name;
    m_operands = operands;
  }

  public String getName(){
    return m_name;
  }

  public List<Operand> getOperands(){
    return m_operands;
  }

  @Override
  public String toString(){
    StringBuilder ret = new StringBuilder();
    ret.append(m_name);
    ret.append(" ");
    if(m_name.equals("ldc1")){
      return ret.toString();
    }
    for(int i = 0; i < m_operands.size(); ++i){
      ret.append(m_operands.get(i).getValue());
      if(i < m_operands.size() - 1){
        ret.append(" ");
      }
    }
    return ret.toString();
  }
}
