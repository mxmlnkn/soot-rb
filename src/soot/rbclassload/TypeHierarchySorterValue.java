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

import soot.Type;
import soot.rbclassload.DfsInfo;
import soot.rbclassload.RootbeerClassLoader;

public class TypeHierarchySorterValue implements Comparable<TypeHierarchySorterValue> {

  private Type m_type;
  private int m_number;
  
  public TypeHierarchySorterValue(Type type){
    m_type = type;
    DfsInfo dfs_info = RootbeerClassLoader.v().getDfsInfo();
    m_number = RootbeerClassLoader.v().getClassNumber(type.toString());
  }
  
  public int compareTo(TypeHierarchySorterValue t) {
    return Integer.valueOf(t.m_number).compareTo(Integer.valueOf(m_number));
  }

  public Type getType(){
    return m_type;
  }

}
