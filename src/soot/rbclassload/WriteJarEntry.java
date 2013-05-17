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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;

public class WriteJarEntry {

  public void write(JarEntry entry, InputStream is, String m_tempFolder) throws Exception {
    String name = entry.getName();
    String filename = m_tempFolder + File.separator + name;            
    if(entry.isDirectory()){
      File file = new File(filename);
      file.mkdirs();
      return;
    } else {
      File file = new File(filename);
      File parent = file.getParentFile();
      parent.mkdirs();
    }
    FileOutputStream fout = new FileOutputStream(filename);
    WriteStream writer = new WriteStream();
    writer.write(is, fout);
    fout.flush();
    fout.close();
  }
  
}
