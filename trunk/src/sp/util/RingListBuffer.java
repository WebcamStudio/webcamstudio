/*
 * Program ...... SinglePaint
 * File ......... RingListBuffer.java
 * Author ....... Harald Hetzner
 * 
 * Copyright (C) 2006  Harald Hetzner
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 * 
 * Harald Hetzner <singlepaint [at] mkultra [dot] dyndns [dot] org>
 */

package sp.util;

/**
 * This class implements an expandable buffer. The buffer is implemented as a list
 * which reuses nodes. New entries are written to the end of the buffer. Entries
 * are being read from the beginning of the buffer.
 * @author Harald Hetzner
 * @version 1.0
 */
public class RingListBuffer {
  private Node reader;
  private Node writer;

  public RingListBuffer() {
    Node node = new Node(null, null);
    this.writer = new Node(null, node);
    this.reader = this.writer;
    node.next = this.writer;
  }

  /**
   * Puts the specified object to the end of the list
   * @param obj the object to put to the end of the list
   */
  public synchronized void put(Object obj) {
    if (this.writer.next.equals(this.reader)) {
      Node newNode = new Node(null, this.writer.next);
      this.writer.next = newNode;
    }
    this.writer.object = obj;
    this.writer = this.writer.next;
  }

  /**
   * Returns the first object from the list and deletes the list entry for it
   * @return the first object from the list
   */
  public synchronized Object get() {
    Object obj = this.reader.object;
    this.reader.object = null;
    this.reader = this.reader.next;
    return obj;
  }

  private class Node {
    protected Object object;
    protected Node next;

    protected Node(Object object, Node next) {
      this.object = object;
      this.next = next;
    }
  }
}
