/*
    Copyright (C) 2020 Micha³ Kullass

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package moonwalker.core.utils;

import java.nio.ByteBuffer;

class ArrayWrapper
{
	private ByteBuffer buf;
	
	public ArrayWrapper(byte[] src)
	{
		buf = ByteBuffer.wrap(src);
	}
	
	public byte[] getBlock(int ind, int len)
	{
		byte[] ret = new byte[len];
		buf.mark();
		try
		{
			buf.position(ind);
			buf.get(ret);
		}
		finally
		{
			buf.reset();
		}
		return ret;
	}
	public byte getByte(int ind)
	{
		return buf.get(ind);
	}
	public short getShort(int ind)
	{
		return buf.getShort(ind);
	}
	public int getInt(int ind)
	{
		return buf.getInt(ind);
	}
	
	public void put(int ind, byte[] arr)
	{
		buf.mark();
		try
		{
			buf.position(ind);
			buf.put(arr);
		}
		finally
		{
			buf.reset();
		}
	}
	public void putByte(int ind, byte val)
	{
		buf.put(ind, val);
	}
	public void putShort(int ind, short val)
	{
		buf.putShort(ind, val);
	}
	public void putInt(int ind, int val)
	{
		buf.putInt(ind, val);
	}
	
	public byte[] getSource()
	{
		return buf.array();
	}
}
