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

import java.awt.Dimension;
import java.awt.Point;

public interface MoonwalkerMetadata
{
	public String getObjectTypeDesription(short id);
	public int getRomLength();
	
	public Dimension getCameraSize();
	public int getRegionTableAddress();
	public int getRegionTableLength();
	public int getVerticalRegionCount(int stageIndex);
	public int getHorizontalRegionCount(int stageIndex);
	public int getMainInitialTableLength();
	public int getMainInitialTableAddress(int stageIndex);
	public Point getMainInitialTableBaseOffset(int stageIndex);
	public int getCaveInitialTableLength();
	public int getCaveInitialTableAddress(int stageIndex);
	
	public int getStageMetadataTableAddress();
	public int getTilesetTableAddress();
	public int getPaletteTableAddress();
	
	public int getStageWidthInTiles(int stageIndex);
	
	public IntRangeSet getFreeROMSpace(Class<?> cl, String usecase);
	public void assignROMSpace(Class<?> cl, String usecase, IntRangeSet consumedSpace);
	public void clearROMSpace(Class<?> cl, String usecase, IntRangeSet freedSpace, byte[] rom);
	public void clearAllROMSpace(Class<?> cl, String usecase, byte[] rom);
}
