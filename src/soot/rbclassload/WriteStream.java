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

import java.io.InputStream;
import java.io.OutputStream;

public class WriteStream {

  public void write(InputStream fin, OutputStream fout) throws Exception {
    while(true){
      int read_len = 4096;
      byte[] buffer = new byte[read_len];
      int curr_read = fin.read(buffer);
      if(curr_read == -1){
        break;
      }
      byte[] to_write = new byte[curr_read];
      for(int i = 0; i < curr_read; ++i){
        to_write[i] = buffer[i];
      }
      fout.write(to_write);
    }
  }
}
