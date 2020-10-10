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

public class IntRange
{
	private int start;
	private int end;
	
	//Intended use: left inclusive right exclusive. If start == end, the range is empty.
	
	public IntRange(int start, int end)
	{
		if (end < start)
			throw new IllegalArgumentException("Illegal range: End < Start");
		this.start = start;
		this.end = end;
	}
	
	public int getStart()
	{
		return start;
	}
	public int getEnd()
	{
		return end;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntRange other = (IntRange) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		return result;
	}
	@Override
	public String toString()
	{
		return "[" + start + ", " + end + "]";
	}
}
