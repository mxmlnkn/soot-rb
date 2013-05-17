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
import java.util.Map;
import java.util.HashMap;

public class StringNumbers {

  private List<String> m_arrayList;
  private Map<String, Integer> m_hashMap;

  public StringNumbers() {
    m_arrayList = new ArrayList<String>();
    m_hashMap = new HashMap<String, Integer>();
  }

  public String getString(int index){
    return m_arrayList.get(index);
  }

  public int addString(String str){
    if(m_hashMap.containsKey(str)){
      return m_hashMap.get(str);
    } else {
      int ret = m_arrayList.size();    
      m_arrayList.add(str);
      m_hashMap.put(str, ret);
      return ret;
    }
  }
}
