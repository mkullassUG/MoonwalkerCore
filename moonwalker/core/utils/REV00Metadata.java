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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class REV00Metadata implements MoonwalkerMetadata
{
	private int romLength;
	
	private HashMap<Integer, String> objectTypeMap;
	
	private IntRangeSet freeSpace;
	private IntRangeSet unassignedSpace;
//	private RangeSet marginSpace;
	private HashMap<String, IntRangeSet> usedSpace;
	
	private int[][] regionCountArr =
	{
		{2, 4},
		{4, 2},
		{3, 3},
		{2, 4},
		{4, 2},
		{3, 3},
		{2, 4},
		{4, 2},
		{3, 3},
		{2, 4},
		{4, 2},
		{3, 3},
		{4, 2},
		{2, 4},
		{3, 3},
		{2, 2}
	};
	private int[] initialTableAddrArr = 
	{
		0x2ddec, 0x2de26, 0x2de60,
		0x2de9a, 0x2ded4, 0x2df0e,
		0x2df48, 0x2df82, 0x2dfbc,
		0x2dff6, 0x2e030, 0x2e06a,
		0x2e0a4, 0x2e0de, 0x2e118
	};
	private int[] initialTableBaseOffsetIndices = 
	{
		1, 2, 3,
		1, 4, 5,
		1, 4, 7,
		1, 6, 7,
		6, 1, 7
	};
	private Point[] initialTableBaseOffsets =
	{
		new Point(-0x80, -0x80),
		new Point(-0x80, 0x60),
		new Point(-0x80, 0x190),
		new Point(0x80, 0x20),
		new Point(-0x80, 0x220),
		new Point(-0x80, 0xD8),
		new Point(-0x80, -0x80),
		new Point(-0x80, 0x140)
	};
	
	private int[] caveAddrArr =
	{
		0x2e152, 0x2e18c, 0x2e1c6, 0x2e1ee, 0x2e1f2, 0x2e1f6,
		0x2e1fa, 0x2e1fe, 0x2e202, 0x2e206, 0x2e20a, 0x2e20e,
		0x2e212, 0x2e216, 0x2e21a, 0x2e21e, 0x2e222, 0x2e226
	};
	
	private int[] stageHorizontalTileCountArr = 
	{
		0xA0, 0x50, 0x78,
		0xA0, 0x50, 0x78,
		0xA0, 0x50, 0x78,
		0xA0, 0x50, 0x78,
		0x50, 0xA0, 0x78,
		0x28
	};
	
	private void initROMSpace(byte[] romContent, IntRange[] suspectedFreeSpace)
	{
		final int margin = 16;
		
		ArrayList<IntRange> freeSpaceList = new ArrayList<>();
		ArrayList<IntRange> unassignedSpaceList = new ArrayList<>();
//		ArrayList<Range> marginSpaceList = new ArrayList<>();
		
		//TODO Test
		for (IntRange r: suspectedFreeSpace)
		{
			int start = r.getStart();
			int end = r.getEnd();
			
			boolean inFreeSpace = true;
			int spaceStart = start;
			int ffCount = 0;
			
			for (int curr = start; curr < end; curr++)
			{
				if ((0xFF & romContent[curr]) != 0xFF)
				{
					if (inFreeSpace)
					{
						inFreeSpace = false;
						freeSpaceList.add(new IntRange(
								spaceStart,
								Math.max(curr - margin, spaceStart)));
						spaceStart = curr;
					}
					else
						ffCount = 0;
				}
				else
				{
					if (!inFreeSpace)
					{
						ffCount++;
						if (ffCount > margin)
						{
							inFreeSpace = true;
							ffCount = 0;
							unassignedSpaceList.add(new IntRange(
									Math.max(spaceStart - margin, start),
									Math.min(curr, end)));
							spaceStart = curr;
						}
					}
				}
			}
			
			if (inFreeSpace)
			{
				freeSpaceList.add(new IntRange(
						spaceStart,
						end));
			}
			else
			{
				unassignedSpaceList.add(new IntRange(
						Math.max(spaceStart - margin, start),
						end));
			}
		}
		
		//TODO implement margin removal around resolved unassigned space
//		for (Range r: unassignedSpaceList)
//		{
//			int start = r.getStart();
//			int end = r.getEnd();
//			
//			if (start == end)
//				continue;
//			
//			int leftMarginEnd = Math.min(start + margin, end);
//			for (int curr = start, i = 0; (curr < end) && (i < margin); curr++, i++)
//			{
//				if ((0xFF & romContent[curr]) != 0xFF)
//				{
//					leftMarginEnd = curr;
//					break;
//				}
//			}
//			marginSpaceList.add(new Range(start, leftMarginEnd));
//			
//			int rightMarginStart = Math.max(end - margin, start);
//			for (int curr = end - 1, i = 0; (curr >= start) && (i < margin); curr--, i++)
//			{
//				if ((0xFF & romContent[curr]) != 0xFF)
//				{
//					rightMarginStart = curr + 1;
//					break;
//				}
//			}
//			marginSpaceList.add(new Range(rightMarginStart, end));
//		}
		
		freeSpace = new IntRangeSet(freeSpaceList);
		unassignedSpace = new IntRangeSet(unassignedSpaceList);
		usedSpace = new HashMap<>();
//		marginSpace = new RangeSet(marginSpaceList);
		
//		Arrays.sort(suspectedFreeSpace, (a, b) -> a.getEnd() - b.getEnd());
//		System.out.println("Suspected free space = actual free space? "
//				+ Arrays.equals(suspectedFreeSpace, freeSpace.getRangeArray()));
//		
////	System.out.println("Suspected:   " + Arrays.toString(suspectedFreeSpace));
////	System.out.println("Actual free: " + freeSpace);
////	System.out.println("Unassigned:  " + unassignedSpace);
////	System.out.println("Margin:  " + marginSpace);
//		
//		System.out.println("Suspected:   " + spaceToHex(suspectedFreeSpace));
//		System.out.println("Actual free: " + spaceToHex(freeSpace));
//		System.out.println("Unassigned:  " + spaceToHex(unassignedSpace));
////	System.out.println("Margin:  " + spaceToHex(marginSpace));
	}
	private void initObjectTypeMap()
	{
		objectTypeMap = new HashMap<>();
		
		objectTypeMap.put(0x0001, "Jackson");
		objectTypeMap.put(0x0002, "Openable object");
		objectTypeMap.put(0x0004, "Extendable stairs trigger (stage 1)");
		objectTypeMap.put(0x0005, "Kid (hidden)");
		objectTypeMap.put(0x0007, "Magic dust");
		objectTypeMap.put(0x0008, "Hat");
		objectTypeMap.put(0x0009, "Palette swapper (Title screen)");
		objectTypeMap.put(0x000A, "Bubbles (gliding)");
		objectTypeMap.put(0x000B, "Comet (horizontal)");
		objectTypeMap.put(0x000C, "Kid (ground)");
		objectTypeMap.put(0x000D, "Sound trigger");
		objectTypeMap.put(0x000E, "Slidable object");
		objectTypeMap.put(0x0013, "Coin");
		objectTypeMap.put(0x0014, "Elevator background (stage 2)");
		objectTypeMap.put(0x0015, "Elevator entrance (stage 2)");
		objectTypeMap.put(0x0016, "Lamp (top sprite, stage 2)");
		objectTypeMap.put(0x0017, "Manhole cover (stage 2)");
		objectTypeMap.put(0x0018, "Hidden Cave (Sign variant, stage 4)");
		objectTypeMap.put(0x0019, "Fire hydrant (stage 2)");
		objectTypeMap.put(0x001A, "Pressurized water stream (stage 2)");
		objectTypeMap.put(0x001B, "Trash can (stage 2)");
		objectTypeMap.put(0x001C, "Palette swapper (stage 2)");
		objectTypeMap.put(0x001D, "Trash can cover (stage 2)");
		objectTypeMap.put(0x001E, "Cave (stage 4)");
		objectTypeMap.put(0x001F, "Web slowdown effect (stage 4)");
		objectTypeMap.put(0x0020, "Water stream (stage 4)");
		objectTypeMap.put(0x0021, "Teleporter (stage 5)");
		objectTypeMap.put(0x0022, "Teleportation ring effect (stage 5)");
		objectTypeMap.put(0x0023, "Elevator (stage 5)");
		objectTypeMap.put(0x0024, "Trapdoor (stage 5)");
		objectTypeMap.put(0x0025, "Turret (stage 5)");
		objectTypeMap.put(0x0026, "Conveyor belt (stage 5)");
		objectTypeMap.put(0x0027, "Turret laser (stage 5)");
		objectTypeMap.put(0x0028, "Electric spark (stage 5)");
		objectTypeMap.put(0x0029, "Control panel hitbox (stage 5)");
		objectTypeMap.put(0x002A, "Bridge (stage 3)");
		objectTypeMap.put(0x002B, "Bridge fragment (stage 3)");
		objectTypeMap.put(0x002D, "Branch (stage 3)");
		objectTypeMap.put(0x002E, "Hidden Cave (Spider variant, stage 4)");
		objectTypeMap.put(0x002F, "Stalactite (stage 4)");
		objectTypeMap.put(0x0030, "Boulder (stage 4)");
		objectTypeMap.put(0x0031, "Bubbles (on shoulder)");
		objectTypeMap.put(0x0032, "Mr. Big");
		objectTypeMap.put(0x0033, "Kid (standing)");
		objectTypeMap.put(0x0034, "Database hitbox (stage 5)");
		objectTypeMap.put(0x0035, "Slide Door (stage 5)");
		objectTypeMap.put(0x0036, "Openable grave (stage 3)");
		objectTypeMap.put(0x0037, "Sound object");
		objectTypeMap.put(0x0038, "Stage intro animation (stage 4)");
		objectTypeMap.put(0x003C, "End screen animation (Jackson)");
		objectTypeMap.put(0x003E, "Cutscene face part");
		objectTypeMap.put(0x0040, "Comet (falling directly on Jackson, visual)");
		objectTypeMap.put(0x0043, "Mecha-Jackson Rocket");
		objectTypeMap.put(0x0044, "Comet (falling, Mecha-Jackson trigger)");
		objectTypeMap.put(0x0049, "Kid marker (stage 4, Mecha-Jackson only)");
		objectTypeMap.put(0x004B, "Post-boss explosion (stage 5)");
		objectTypeMap.put(0x004C, "Spider (stage 4)");
		objectTypeMap.put(0x004F, "Mr. Big's plane animation");
		objectTypeMap.put(0x0050, "Gangster (stage 1)");
		objectTypeMap.put(0x0052, "Bullet (stage 1)");
		objectTypeMap.put(0x0055, "Cat (stage 1)");
		objectTypeMap.put(0x0057, "Street thug (stage 2)");
		objectTypeMap.put(0x0059, "Laser guard (stage 4)");
		objectTypeMap.put(0x005A, "Guard (stage 2)");
		objectTypeMap.put(0x005B, "Laser guard (stage 5)");
		objectTypeMap.put(0x005C, "Double bullet (stage 2)");
		objectTypeMap.put(0x005D, "Dog (stage 2)");
		objectTypeMap.put(0x005E, "Laser beam (stage 5)");
		objectTypeMap.put(0x005F, "Zombie (stage 3)");
		objectTypeMap.put(0x0060, "Zombie (stage 4)");
		objectTypeMap.put(0x0061, "Lady (stage 1)");
		objectTypeMap.put(0x0062, "Billiard player (stage 1)");
		objectTypeMap.put(0x0063, "Door animation (stage 1)");
		objectTypeMap.put(0x0065, "Bird (stage 3)");
		objectTypeMap.put(0x0066, "Jackson's plane hud element (stage 6)");
		objectTypeMap.put(0x0067, "Cutscene face part animation");
		objectTypeMap.put(0x0069, "Jackson's plane animation");
		objectTypeMap.put(0x006B, "Jackson's car animation");
		objectTypeMap.put(0x006C, "Battle plane fight (stage 6)");
		objectTypeMap.put(0x006D, "Mr. Big's battle plane (stage 6)");
		objectTypeMap.put(0x006E, "Slide door animation (stage 5)");
		objectTypeMap.put(0x0076, "Jackson's plane outro animation (unconfirmed) (stage 6)");
		objectTypeMap.put(0x0076, "Flipped 0x0069 (stage 6)");
		objectTypeMap.put(0x0078, "Missile (stage 6)");
		objectTypeMap.put(0x0079, "Mr. Big's plane explosion (stage 6)");
		objectTypeMap.put(0x007C, "Round 1-2 Boss (stage 1)");
		objectTypeMap.put(0x007D, "Explosive");
		objectTypeMap.put(0x007E, "Round 1-3 Boss (stage 1)");
		objectTypeMap.put(0x007F, "Round 2-3 Boss (stage 2)");
		objectTypeMap.put(0x0080, "Round 3-2 Boss (stage 3)");
	}
	
	public REV00Metadata(byte[] romContent)
	{
		ArrayList<Integer> allowedRomLengths = new ArrayList<Integer>(
				Arrays.asList(new Integer[] {0x80000, 0x100000, 0x200000, 0x400000}));
		if (!allowedRomLengths.contains(romContent.length))
			throw new IllegalArgumentException("Invalid ROM length: 0x"
					+ Integer.toHexString(romContent.length) + " b");
		
		if (!Arrays.equals(
				Arrays.copyOfRange(romContent, 0x180, 0x18E),
				new byte[] 
				{
					0x47, 0x4D, 0x20, 0x30, 0x30, 0x30, 0x30,
					0x34, 0x30, 0x32, 0x38, 0x2D, 0x30, 0x30
				}))
			throw new IllegalArgumentException("Invalid ROM header for REV00");
		
		romLength = romContent.length;
		
		IntRange[] suspectedFreeSpace =
		{
			new IntRange(0x16010, 0x17000),
			new IntRange(0x2E3D0, 0x2E800),
			new IntRange(0x32DB0, 0x34700),
			new IntRange(0x35E60, 0x36000),
			new IntRange(0x38DD0, 0x39500),
			new IntRange(0x3AE30, 0x3B000),
			new IntRange(0x45A00, 0x46000),
			new IntRange(0x4F5E0, 0x50000),
			new IntRange(0x59330, 0x5A000),
			new IntRange(0x58720, 0x59000),
			new IntRange(0x5F740, 0x60000),
			new IntRange(0x68720, 0x68800),
			new IntRange(0x6F490, 0x70000),
			new IntRange(0x7FFF0, romContent.length)
		};
		
		initROMSpace(romContent, suspectedFreeSpace);
		initObjectTypeMap();
	}
	
	public String getObjectTypeDesription(short id)
	{
		return objectTypeMap.getOrDefault(0xFFFF & id, "Unknown");
	}
	public Dimension getCameraSize()
	{
		return new Dimension(320, 244);
	}
	
	private String getQualifiedKey(Class<?> cl, String usecase)
	{
		return cl.getTypeName() + "::" + usecase;
	}
	public IntRangeSet getFreeROMSpace(Class<?> cl, String usecase)
	{
		String qualifiedKey = getQualifiedKey(cl, usecase);
		return freeSpace.union(usedSpace.getOrDefault(qualifiedKey, IntRangeSet.EMPTY));
	}
	public void assignROMSpace(Class<?> cl, String usecase, IntRangeSet consumedSpace)
	{
		String qualifiedKey = getQualifiedKey(cl, usecase);
		
		consumedSpace = consumedSpace.intersection(freeSpace.union(unassignedSpace));
		
		freeSpace = freeSpace.difference(consumedSpace);
		unassignedSpace = unassignedSpace.difference(consumedSpace);
		
		usedSpace.put(qualifiedKey, usedSpace.getOrDefault(qualifiedKey, IntRangeSet.EMPTY)
				.union(consumedSpace));
	}
	public void clearROMSpace(Class<?> cl, String usecase, IntRangeSet freedSpace, byte[] rom)
	{
		String qualifiedKey = getQualifiedKey(cl, usecase);
		IntRangeSet currUsedSpace = usedSpace.getOrDefault(qualifiedKey, IntRangeSet.EMPTY);
		freedSpace = freedSpace.intersection(currUsedSpace);
		usedSpace.put(qualifiedKey, currUsedSpace.difference(freedSpace));
		freeSpace = freeSpace.union(freedSpace);
		
		clearROM(rom, freedSpace);
	}
	public void clearAllROMSpace(Class<?> cl, String usecase, byte[] rom)
	{
		String qualifiedKey = getQualifiedKey(cl, usecase);
		IntRangeSet currUsedSpace = usedSpace.getOrDefault(qualifiedKey, IntRangeSet.EMPTY);
		usedSpace.put(qualifiedKey, IntRangeSet.EMPTY);
		freeSpace = freeSpace.union(currUsedSpace);
		
		clearROM(rom, currUsedSpace);
	}
	private void clearROM(byte[] rom, IntRangeSet rs)
	{
		for (IntRange r: rs.getRangeArray())
		{
			int start = r.getStart();
			int end = r.getEnd();
			for (int i = start; i < end; i++)
				rom[i] = (byte) 0xFF;
		}
	}
	
	public int getRegionTableAddress()
	{
		return 0x5A000;
	}
	public int getRegionTableLength()
	{
		return regionCountArr.length;
	}
	public int getVerticalRegionCount(int stageIndex)
	{
		return regionCountArr[stageIndex][0];
	}
	public int getHorizontalRegionCount(int stageIndex)
	{
		return regionCountArr[stageIndex][1];
	}
	public int getMainInitialTableLength()
	{
		return initialTableAddrArr.length;
	}
	public int getMainInitialTableAddress(int stageIndex)
	{
		return initialTableAddrArr[stageIndex];
	}
	public Point getMainInitialTableBaseOffset(int stageIndex)
	{
		int stageType = initialTableBaseOffsetIndices[stageIndex];
		if (stageType < initialTableBaseOffsets.length)
			return initialTableBaseOffsets[stageType];
		return initialTableBaseOffsets[0];
	}
	
	public int getCaveInitialTableLength()
	{
		return caveAddrArr.length;
	}
	public int getCaveInitialTableAddress(int stageIndex)
	{
		return caveAddrArr[stageIndex];
	}
	
	public int getRomLength()
	{
		return romLength;
	}
	
	public int getStageMetadataTableAddress()
	{
		return 0x2DD8E;
	}
	public int getTilesetTableAddress()
	{
		return 0x7B4;
	}
	public int getPaletteTableAddress()
	{
		return 0x2D6EC;
	}
	
	public int getStageWidthInTiles(int stageIndex)
	{
		return stageHorizontalTileCountArr[stageIndex];
	}
	
//	private String spaceToHex(RangeSet rs)
//	{
//		return spaceToHex(rs.getRangeArray());
//	}
//	private String spaceToHex(ArrayList<Range> rList)
//	{
//		return spaceToHex(rList.toArray(l -> new Range[l]));
//	}
//	private String spaceToHex(Range[] rArr)
//	{
//		StringBuilder sb = new StringBuilder("{");
//		for (Range r: rArr)
//		{
//			sb.append("[0x");
//			sb.append(Integer.toHexString(r.getStart()).toUpperCase());
//			sb.append(", 0x");
//			sb.append(Integer.toHexString(r.getEnd()).toUpperCase());
//			sb.append("], ");
//		}
//		if (sb.length() > 2)
//			sb.setLength(sb.length() - 2);
//		sb.append("}");
//		return sb.toString();
//	}
}
