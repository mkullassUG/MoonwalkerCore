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

import java.awt.Point;
import moonwalker.core.structures.MDirectObject;
import moonwalker.core.structures.MoonwalkerPalette;
import moonwalker.core.structures.MoonwalkerStageArea;

public class MoonwalkerIO
{
	private MoonwalkerIO() {}
	
	public static MDirectObject[][] loadMainObjectArray(byte[] rom, MoonwalkerMetadata meta)
	{
		return MoonwalkerObjectIO.readMainObjectArray(rom, meta);
	}
	public static MDirectObject[][] loadCaveObjectArray(byte[] rom, MoonwalkerMetadata meta)
	{
		return MoonwalkerObjectIO.readCaveObjectArray(rom, meta);
	}
	public static void saveMainObjectArray(byte[] sourceRom, MDirectObject[][] objectArr,
			MoonwalkerMetadata meta) throws OutOfSpaceException
	{
		MoonwalkerObjectIO.writeMainObjectArray(sourceRom, objectArr, meta);
	}
	//TODO saveCaveObjectArray()
	
	public static MoonwalkerStageArea loadMainStageArea(byte[] rom, int stageIndex,
			MoonwalkerMetadata meta)
	{
		return MoonwalkerStageIO.readMainStageArea(rom, stageIndex, meta);
	}
	//TODO loadCaveArea(), saveMainStageArea(), saveCaveArea()
	
	//TODO remake to account for multiple palletes, savePalette()
	public static MoonwalkerPalette loadPalette(byte[] rom, int stageIndex,
			MoonwalkerMetadata meta)
	{
		return MoonwalkerStageIO.loadPalette(rom, stageIndex, meta);
	}
	
	public static Point getInitialCameraPosition(byte[] rom, int stageIndex,
			MoonwalkerMetadata meta)
	{
		ArrayWrapper wRom = new ArrayWrapper(rom);
		int addr = wRom.getInt(0x2DD8E + (4 * stageIndex));
		Point ret = new Point(wRom.getShort(addr + 6), wRom.getShort(addr + 8));
		//TODO implement proper offset calculation. Code is at 0x1566E
		int[][] offArr = 
		{
			{0, 0},
			{0, 0x50},
			{-0x40, -0x40},
			
			{0, 0},
			{0, 0},
			{0, -0x68},
			
			{0, 0},
			{0, 0},
			{0, 0},
			
			{0, 0},
			{0, 0},
			{0, 0},
			
			{0, 0},
			{0, 0},
			{0, 0},
			
			{0, 0}
		};
		ret.x += offArr[stageIndex][0];
		ret.y += offArr[stageIndex][1];
		return ret;
	}
	//TODO setInitialCameraPosition()
	
	public static void fixChecksum(byte[] rom)
	{
		ArrayWrapper wrap = new ArrayWrapper(rom);
		short buf = 0;
		for (int i = 0x200; i < rom.length; i += 2)
			buf += wrap.getShort(i);
		wrap.putShort(0x18E, buf);
	}
}