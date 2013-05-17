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


public class NumberedType implements Comparable<NumberedType> {

  private long m_number;
  private String m_type;
  
  public NumberedType(String type, long number){
    m_type = type;
    m_number = number;
  }
  
  public String getType(){
    return m_type;
  }
  
  public long getNumber(){
    return m_number;
  }
  
  public int compareTo(NumberedType o) {
    return Long.valueOf(o.m_number).compareTo(Long.valueOf(m_number));
  }

  @Override
  public String toString(){
    String ret = "NumberedType: ["+m_number+", "+m_type.toString()+"]";
    return ret;    
  }
}
