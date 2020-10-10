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
import java.util.ArrayList;
import java.util.Arrays;
import moonwalker.core.structures.MDirectObject;

class MoonwalkerObjectIO
{
	private MoonwalkerObjectIO()
	{}
	
	private static final String MAIN_OBJECT_ARRAY_KEY = "mainObjectArray";
	private static final String CAVE_OBJECT_ARRAY_KEY = "caveObjectArray";
	
	public static MDirectObject[][] readMainObjectArray(byte[] rom, MoonwalkerMetadata meta)
	{
		TrackableArrayWrapper src = new TrackableArrayWrapper(rom);
		return readMainObjectArray(src, meta);
	}
	protected static MDirectObject[][] readMainObjectArray(TrackableArrayWrapper src,
			MoonwalkerMetadata meta)
	{
		int BASE_ADDR = meta.getRegionTableAddress();
		int mapCount = meta.getRegionTableLength();
		
		int[] mapAddr = new int[mapCount];
		for (int i = 0; i < mapCount; i++)
			mapAddr[i] = src.getInt(BASE_ADDR + (i * 4));
		
		ArrayList<ArrayList<MDirectObject>> objectListList = new ArrayList<>(mapCount);
		for (int i = 0; i < mapCount; i++)
		{
			int yRegionCount = meta.getVerticalRegionCount(i);
			int xRegionCount = meta.getHorizontalRegionCount(i);
			
			ArrayList<MDirectObject> objectList = new ArrayList<>();
			
			for (int i0 = 0; i0 < yRegionCount; i0++)
			{
				int yBaseAddr = src.getInt(mapAddr[i] + (4 * i0));
				
				for (int i1 = 0; i1 < xRegionCount; i1++)
				{
					int xBaseAddr = src.getInt(yBaseAddr + (4 * i1));
					int blockCount = (0xFFFF & src.getShort(xBaseAddr)) + 1;
					
					int currAddr = xBaseAddr + 2;
					if (blockCount < 100)
					{
						byte[] blockArr = src.getBlock(currAddr, 16 * blockCount);
						currAddr += blockArr.length;
						
						for (int i2 = 0; i2 < blockCount; i2++)
						{
							int blockOffset = 16 * i2;
							int xOffset = ((0xFF & blockArr[blockOffset + 4]) << 8) | (0xFF & blockArr[blockOffset + 5]);
							int yOffset = ((0xFF & blockArr[blockOffset + 2]) << 8) | (0xFF & blockArr[blockOffset + 3]);
							
							objectList.add(new MDirectObject(i1, xOffset, i0, yOffset,
									((0xFF & blockArr[blockOffset]) << 8) | (0xFF & blockArr[blockOffset + 1]),
									(short) (((0xFF & blockArr[blockOffset + 6]) << 8) | (0xFF & blockArr[blockOffset + 7])),
									Arrays.copyOfRange(blockArr, blockOffset + 8, blockOffset + 16), MDirectObject.Container.REGION_TABLE));
						}
					}
				}
			}
			
			objectListList.add(objectList);
		}
		
		int initialListLen = meta.getMainInitialTableLength();
		for (int i = 0; i < initialListLen; i++)
		{
			ArrayList<MDirectObject> objectList = new ArrayList<>();
			Point baseOffset = meta.getMainInitialTableBaseOffset(i);
			int xBase = baseOffset.x;
			int yBase = baseOffset.y;
			
			int addr = meta.getMainInitialTableAddress(i);
			int listAddr = src.getInt(addr);
			
			int blockCount = (0xFFFF & src.getShort(listAddr)) + 1;
			
			int currAddr = listAddr + 2;
			if (blockCount < 100)
			{
				byte[] blockArr = src.getBlock(currAddr, 16 * blockCount);
				currAddr += blockArr.length;
				
				for (int i2 = 0; i2 < blockCount; i2++)
				{
					int blockOffset = 16 * i2;
					int xOffset = ((0xFF & blockArr[blockOffset + 4]) << 8) | (0xFF & blockArr[blockOffset + 5]);
					int yOffset = ((0xFF & blockArr[blockOffset + 2]) << 8) | (0xFF & blockArr[blockOffset + 3]);
					
					objectList.add(new MDirectObject(xBase + xOffset, yBase + yOffset,
							((0xFF & blockArr[blockOffset]) << 8) | (0xFF & blockArr[blockOffset + 1]),
							(short) (((0xFF & blockArr[blockOffset + 6]) << 8) | (0xFF & blockArr[blockOffset + 7])),
							Arrays.copyOfRange(blockArr, blockOffset + 8, blockOffset + 16), MDirectObject.Container.INITIAL_TABLE));
				}
			}

			ArrayList<MDirectObject> sourceList = objectListList.get(i);
			L0: for (MDirectObject o:objectList)
			{
				for (MDirectObject srcObj:sourceList)
				{
					if (srcObj.equalsIgnoreContainer(o))
					{
						srcObj.setContainer(MDirectObject.Container.ALL_TABLES);
						continue L0;
					}
				}
				sourceList.add(o);
			}
		}
		
		meta.assignROMSpace(MoonwalkerObjectIO.class, MAIN_OBJECT_ARRAY_KEY, src.getRangeSet());
		
		MDirectObject[][] ret = new MDirectObject[mapCount][];
		for (int i = 0; i < ret.length; i++)
			ret[i] = objectListList.get(i).toArray(l -> new MDirectObject[l]);
		
		return ret;
	}
	public static MDirectObject[][] readCaveObjectArray(byte[] rom, MoonwalkerMetadata meta)
	{
		TrackableArrayWrapper src = new TrackableArrayWrapper(rom);
		return readCaveObjectArray(src, meta);
	}
	protected static MDirectObject[][] readCaveObjectArray(TrackableArrayWrapper src, MoonwalkerMetadata meta)
	{
		int mapCount = meta.getCaveInitialTableLength();
		ArrayList<ArrayList<MDirectObject>> objectListList = new ArrayList<>(mapCount);
		for (int i = 0; i < mapCount; i++)
		{
			ArrayList<MDirectObject> objectList = new ArrayList<>();
			
			int addr = meta.getCaveInitialTableAddress(i);
			int listAddr = src.getInt(addr);
			
			int blockCount = (0xFFFF & src.getShort(listAddr)) + 1;
			
			int currAddr = listAddr + 2;
			if (blockCount < 100)
			{
				byte[] blockArr = src.getBlock(currAddr, 16 * blockCount);
				currAddr += blockArr.length;
				
				for (int i2 = 0; i2 < blockCount; i2++)
				{
					int blockOffset = 16 * i2;
					int xOffset = ((0xFF & blockArr[blockOffset + 4]) << 8) | (0xFF & blockArr[blockOffset + 5]);
					int yOffset = ((0xFF & blockArr[blockOffset + 2]) << 8) | (0xFF & blockArr[blockOffset + 3]);
					
					objectList.add(new MDirectObject(xOffset, yOffset,
							((0xFF & blockArr[blockOffset]) << 8) | (0xFF & blockArr[blockOffset + 1]),
							(short) (((0xFF & blockArr[blockOffset + 6]) << 8) | (0xFF & blockArr[blockOffset + 7])),
							Arrays.copyOfRange(blockArr, blockOffset + 8, blockOffset + 16), MDirectObject.Container.INITIAL_TABLE));
				}
			}

			objectListList.add(objectList);
		}
		
		meta.assignROMSpace(MoonwalkerObjectIO.class, CAVE_OBJECT_ARRAY_KEY, src.getRangeSet());
		
		MDirectObject[][] ret = new MDirectObject[mapCount][];
		for (int i = 0; i < ret.length; i++)
			ret[i] = objectListList.get(i).toArray(l -> new MDirectObject[l]);
		
		return ret;
	}
	
	
	public static void writeMainObjectArray(byte[] rom, MDirectObject[][] objectArr, MoonwalkerMetadata meta) throws OutOfSpaceException
	{
		ArrayWrapper src = new ArrayWrapper(rom);
		ArrayWrapper srcCopy = new ArrayWrapper(Arrays.copyOf(rom, rom.length));
		writeMainObjectArray(srcCopy, objectArr, meta);
		src.put(0, srcCopy.getSource());
	}
	public static void writeMainObjectArray(ArrayWrapper src, MDirectObject[][] objectArr,
			MoonwalkerMetadata meta) throws OutOfSpaceException
	{
		meta.clearAllROMSpace(MoonwalkerObjectIO.class, MAIN_OBJECT_ARRAY_KEY, src.getSource());
		
		IntRange[] freeSpace = meta.getFreeROMSpace(MoonwalkerObjectIO.class, MAIN_OBJECT_ARRAY_KEY).getRangeArray();
		int[][] extraSpace = new int[freeSpace.length][2];
		for (int i = 0; i < freeSpace.length; i++)
		{
			extraSpace[i][0] = freeSpace[i].getStart();
			extraSpace[i][1] = freeSpace[i].getEnd();
		}
		
		int mapCount = meta.getRegionTableLength();
		//TODO move to metadata
		int[][] regionTableAddrArr =
		{
			{0x5A048, 0x5A068},
			{0x5A30A, 0x5A32A},
			{0x5A8D8, 0x5A8FC},
			{0x5ADE8, 0x5AE08},
			{0x5B18A, 0x5B1AA},
			{0x5B708, 0x5B72C},
			{0x5BC08, 0x5BC28},
			{0x5BF44, 0x5BF64},
			{0x5C4B2, 0x5C4D6},
			{0x5CA02, 0x5CA22},
			{0x5CE64, 0x5CE84},
			{0x5D342, 0x5D366},
			{0x5E2A8, 0x5E2C8},
			{0x5E832, 0x5E852},
			{0x5EE20, 0x5EE44},
			{0x5F280, 0x5F290}
		};
		int[] limits =
		{
			0x5A2FA,
			0x5A8CC,
			0x5ADE0,
			0x5B17A,
			0x5B6FC,
			0x5BC00,
			0x5BF34,
			0x5C4A6,
			0x5C9FA,
			0x5CE54,
			0x5D336,
			0x5D898 /*0x5E298*/,
			0x5E82A,
			0x5EE14,
			0x5F278,
			0x5F292
		};
		
		int[][] romSpace = new int[extraSpace.length + 1][2];
		for (int i = 0; i < extraSpace.length; i++)
		{
			romSpace[i + 1][0] = extraSpace[i][0];
			romSpace[i + 1][1] = extraSpace[i][1];
 		}
		
		for (int i = 0; i < mapCount; i++)
		{
			int vertRegion = meta.getVerticalRegionCount(i);
			int horRegion = meta.getHorizontalRegionCount(i);
			
			ArrayList<MDirectObject>[][] regionTableList = new ArrayList[vertRegion][horRegion];
			ArrayList<MDirectObject> initialList = new ArrayList<>();
			
			for (MDirectObject obj:objectArr[i])
			{
				boolean inRT = false;
				boolean inIL = false;
				
				switch (obj.getContainer())
				{
					case REGION_TABLE:
						inRT = true;
						break;
					case ALL_TABLES:
						inRT = true;
						inIL = true;
						break;
					case INITIAL_TABLE:
						inIL = true;
				}
				
				int regX = obj.getRegionX();
				int regY = obj.getRegionY();
				if ((regX >= horRegion) || (regY >= vertRegion))
					throw new IllegalArgumentException("Object position outside of the map. ("
							+ obj.getAbsoluteX() + ", " + obj.getAbsoluteY() + ")");
				
				if (inRT)
				{
					if (regionTableList[regY][regX] == null)
						regionTableList[regY][regX] = new ArrayList<>();
					regionTableList[regY][regX].add(obj);
				}
				if (inIL)
					initialList.add(obj);
			}
			
			ArrayList<Integer> addressList = new ArrayList<>();
			ArrayList<ArrayList<byte[]>> writtenDataList = new ArrayList<>();
			int addr = regionTableAddrArr[i][1];
			
			romSpace[0][0] = addr;
			romSpace[0][1] = limits[i];
			
			//Region table
			for (int i0 = 0; i0 < regionTableList.length; i0++)
			{
				for (int i1 = 0; i1 < regionTableList[i0].length; i1++)
				{
					ArrayList<byte[]> dataList = new ArrayList<>();
					if (regionTableList[i0][i1] == null)
						regionTableList[i0][i1] = new ArrayList<>();
					
					int size = regionTableList[i0][i1].size() - 1;
					dataList.add(new byte[] {(byte) ((size & 0xFF00) >> 8), (byte) (size & 0xFF)});
					
					for (MDirectObject obj:regionTableList[i0][i1])
						dataList.add(serializeRegionObject(obj));
					
					writeToRegionTable(src, dataList, romSpace, addressList, writtenDataList);
				}
			}
			int currAddr = regionTableAddrArr[i][0];
			for (int address: addressList)
			{
				src.putInt(currAddr, address);
				currAddr += 4;
			}
			
			//Initial list
			if (i < 0xF)
			{
				Point baseOff = meta.getMainInitialTableBaseOffset(i);
				
				ArrayList<byte[]> dataList = new ArrayList<>();
				
				int size = initialList.size() - 1;
				dataList.add(new byte[] {(byte) ((size & 0xFF00) >> 8), (byte) (size & 0xFF)});
				
				for (MDirectObject obj:initialList)
					dataList.add(serializeInitialObject(obj, -baseOff.x, -baseOff.y));
				
				
				addr = writeMoonwalkerObject(src, dataList, romSpace);
				src.putInt(meta.getMainInitialTableAddress(i), addr);
			}
		}
		
		int l = freeSpace.length;
		ArrayList<IntRange> consumedSpace = new ArrayList<IntRange>(l);
		for (int i = 0; i < l; i++)
		{
			IntRange range = freeSpace[i];
			int[] conSpArr = romSpace[i + 1];
			int rangeStart = range.getStart();
			if (conSpArr[0] > rangeStart)
				consumedSpace.add(new IntRange(rangeStart, conSpArr[0]));
			else
				break;
		}
		
		meta.assignROMSpace(MoonwalkerObjectIO.class, MAIN_OBJECT_ARRAY_KEY, new IntRangeSet(consumedSpace));
	}
	
	private static void writeToRegionTable(ArrayWrapper src,
			ArrayList<byte[]> dataList, int[][] romSpace,
			ArrayList<Integer> addressList, ArrayList<ArrayList<byte[]>> writtenDataList) throws OutOfSpaceException
	{
		for (int i = writtenDataList.size() - 1; i >= 0; i--)
		{
			if (deepEquals(dataList, writtenDataList.get(i)))
			{
				addressList.add(addressList.get(i));
				return;
			}
		}
		
		int addr = writeMoonwalkerObject(src, dataList, romSpace);
		
		addressList.add(addr);
		writtenDataList.add(dataList);
	}
	private static int writeMoonwalkerObject(ArrayWrapper src, ArrayList<byte[]> dataList, int[][] romSpace) throws OutOfSpaceException
	{
		int byteSize = 2 + (16 * (dataList.size() - 1));
		
		int spaceIndex = 0;
		for (;;)
		{
			if ((romSpace[spaceIndex][0] + byteSize) <= romSpace[spaceIndex][1])
				break;
			
			spaceIndex++;
			if (spaceIndex > romSpace.length)
				throw new OutOfSpaceException("Not enough space for all data. Try removing some objects.");
		}
		
		int addr = romSpace[spaceIndex][0];
		
		int currAddr = addr;
		for (byte[] bArr: dataList)
		{
			src.put(currAddr, bArr);
			currAddr += bArr.length;
		}
		
		romSpace[spaceIndex][0] += byteSize;
		
		return addr;
	}
	private static byte[] serializeRegionObject(MDirectObject obj)
	{
		int xOff = obj.getRelativeX();
		int yOff = obj.getRelativeY();
		return serializeMoonwalkerObject(obj, xOff, yOff);
	}
	private static byte[] serializeInitialObject(MDirectObject obj, int xOffset, int yOffset)
	{
		int xOff = obj.getAbsoluteX() + xOffset;
		int yOff = obj.getAbsoluteY() + yOffset;
		
		if (xOff < 0 || yOff < 0)
			throw new IllegalArgumentException("Invalid position: (" + xOff + ", " + yOff + ")");
		
		return serializeMoonwalkerObject(obj, xOff, yOff);
	}
	private static byte[] serializeMoonwalkerObject(MDirectObject obj, int x, int y)
	{
		byte[] ret = new byte[16];
		int address = obj.getAllocationAddress();
		
		int type = 0xFFFF & obj.getType();
		byte[] other = obj.getData();
		
		ret[0] = (byte) ((address & 0xFF00) >> 8);
		ret[1] = (byte) (address & 0xFF);
		ret[2] = (byte) ((y & 0xFF00) >> 8);
		ret[3] = (byte) (y & 0xFF);
		ret[4] = (byte) ((x & 0xFF00) >> 8);
		ret[5] = (byte) (x & 0xFF);
		ret[6] = (byte) ((type & 0xFF00) >> 8);
		ret[7] = (byte) (type & 0xFF);
		for (int i2 = 0; i2 < 8; i2++)
			ret[i2 + 8] = other[i2];
		
		return ret;
	}
	private static boolean deepEquals(ArrayList<byte[]> list1, ArrayList<byte[]> list2)
	{
		int l = list1.size();
		if (l != list2.size())
			return false;
		for (int i = 0; i < l; i++)
		{
			byte[] arr1 = list1.get(i);
			byte[] arr2 = list2.get(i);
			if (arr1 == null)
				return arr2 == null;
			if (arr1.length != arr2.length)
				return false;
			for (int i0 = 0; i0 < arr1.length; i0++)
			{
				if (arr1[i0] != arr2[i0])
					return false;
			}
		}
		return true;
	}
}
