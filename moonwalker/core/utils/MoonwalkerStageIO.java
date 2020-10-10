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
import moonwalker.core.structures.MoonwalkerPalette;
import moonwalker.core.structures.MoonwalkerStageArea;

class MoonwalkerStageIO
{
	public static MoonwalkerStageArea readMainStageArea(byte[] rom, int stageIndex, MoonwalkerMetadata meta)
	{
		TPArrayWrapper src = new TPArrayWrapper(rom);
		return readMainStageArea(src, stageIndex, meta);
	}
	protected static MoonwalkerStageArea readMainStageArea(TPArrayWrapper src, int stageIndex, MoonwalkerMetadata meta)
	{
		int srcAddr = src.getInt(meta.getStageMetadataTableAddress() + (stageIndex << 2));

		byte[] tileset = loadTileset(src, stageIndex, meta);
		byte[] tilemap = loadTilemap(src, srcAddr);
		byte[] layerA = loadLayerA(src, srcAddr);
		byte[] layerB = loadLayerB(src, stageIndex, srcAddr);
//		byte[] palette = loadPalette(src, stageIndex, false);
		
//		for (int i = 0; i < palette.length; i++)
//		{
//			System.out.print(hexByte(0xFF & palette[i]));
//			if (i % 2 == 1)
//				System.out.print(" ");
//		}
//		System.out.println();
		
		meta.assignROMSpace(MoonwalkerStageIO.class, "mainStageArea", src.getRangeSet());
		
		return new MoonwalkerStageArea(tileset, tilemap, layerA, layerB, meta.getStageWidthInTiles(stageIndex));
	}
	protected static byte[] loadTilemap(PackableArrayWrapper src, int srcAddr)
	{
		byte[] interDestArr = new byte[0x1000];
		byte[] tilesetDestArr = new byte[0x1000];
		
		int interSrcAddr = src.getInt(srcAddr + 0x2E);
		PackableArrayWrapper interDest = new PackableArrayWrapper(interDestArr);
		loadIntermediate(src, interDest, interSrcAddr);
		
		int tilesetSrcAddr = 0;
		int D0 = 2;
		PackableArrayWrapper tilesetDest = new PackableArrayWrapper(tilesetDestArr);
		writeToTilemap(interDest, tilesetDest, tilesetSrcAddr, D0);
		
		return tilesetDest.pack();
	}
	protected static byte[] loadLayerA(PackableArrayWrapper src, int srcAddr)
	{
		byte[] layerADestArr = new byte[0x1000];
		
		int layerASrcAddr = src.getInt(srcAddr + 0x32);
		int D0 = 1;
		PackableArrayWrapper layerADest = new PackableArrayWrapper(layerADestArr);
		writeToTilemap(src, layerADest, layerASrcAddr, D0);
		
		return layerADest.pack();
	}
	protected static byte[] loadLayerB(PackableArrayWrapper src, int stageIndex, int srcAddr)
	{
		byte[] layerBDestArr = new byte[0x1000];
		
		PackableArrayWrapper layerBDest = new PackableArrayWrapper(layerBDestArr);
		if (word(stageIndex) < 0x10)
		{
			int layerBSrcAddr = src.getInt(srcAddr + 0x36);
			int D0 = 1;
			writeToTilemap(src, layerBDest, layerBSrcAddr, D0);
		}
		
		return layerBDest.pack();
	}
	protected static byte[] loadTileset(PackableArrayWrapper src, int stageIndex, MoonwalkerMetadata meta)
	{
		int tiledataIndex = stageIndex / 3;
		if (tiledataIndex < 5)
		{
			int tiledataArrayAddr = src.getInt(meta.getTilesetTableAddress()
					+ (4 * tiledataIndex));
//			int tiledataArrayAddr = src.getInt(0x7C0 + (4 * tiledataIndex)); //REV01
			return decompressPatterns(src, tiledataArrayAddr);
		}
		else
		{
			//TODO implement cave tilesets
			return new byte[0x1000];
		}
	}
	public static MoonwalkerPalette loadPalette(byte[] rom, int stageIndex, MoonwalkerMetadata meta)
	{
		//TODO implement properly
		
//		byte[] pal = new byte[0x80];
//		ArrayWrapper src = new ArrayWrapper(rom);
//		int addr = src.getInt(0x2D6EC + (src.getShort(src.getInt(0x2DD8E + (stageIndex << 2))) << 3));
//		System.arraycopy(rom, addr, pal, 0, pal.length);
//		
//		return (palRow, palCol, transparent, priority, f1, f2, f3) ->
//		{
//			int ind = (palRow * 0x20) + (palCol * 2);
//			byte b1 = pal[ind];
//			byte b2 = pal[ind + 1];
//			
//			int b = (b1 & 0xE) >> 1;
//			int g = (b2 & 0xE0) >> 5;
//			int r = (b2 & 0xE) >> 1;
//			
//			int[] map = {0, 0x34, 0x57, 0x74, 0x90, 0xAC, 0xCE, 0xFF};
//			r = map[r];
//			g = map[g];
//			b = map[b];
//			
//			int col = (r << 16) | (g << 8) | b;
//			if (transparent)
//				return col | ((palCol == 0)?0:0xFF000000);
//			else
//				return col;
//		};
		
		return loadPalette(new ArrayWrapper(rom), stageIndex, meta);
	}
	protected static MoonwalkerPalette loadPalette(ArrayWrapper src, int stageIndex,
			MoonwalkerMetadata meta)
	{
//		byte[] paletteDestArr = new byte[0x1000];
//		
//		PackedArrayWrapper paletteDest = new PackedArrayWrapper(paletteDestArr);
//		
//		int srcAddr = src.getInt(0x2DD8E + (stageIndex << 2));
//		short D0 = src.getShort(srcAddr);
//		
//		int addr = loadPalettes(src, paletteDest, 0, D0);
//		if (stageIndex < 0x10)
//		{
//			D0 = 0x15;
//			addr = loadPalettes(src, paletteDest, addr, D0);
//			if (readAdditional)
//			{
//				D0 = 0x16;
//				addr = loadPalettes(src, paletteDest, addr, D0);
//			}
//		}
//		
//		return paletteDest.pack();
		
		byte[] pal = new byte[0x80];
		int addr = src.getInt(meta.getPaletteTableAddress() + (src.getShort(src.getInt(
				meta.getStageMetadataTableAddress() + (stageIndex << 2))) << 3));
		System.arraycopy(src.getSource(), addr, pal, 0, pal.length);
		return (palRow, palCol, transparent, priority, f1, f2, f3) ->
		{
			int ind = (palRow * 0x20) + (palCol * 2);
			byte b1 = pal[ind];
			byte b2 = pal[ind + 1];
			
			int b = (b1 & 0xE) >> 1;
			int g = (b2 & 0xE0) >> 5;
			int r = (b2 & 0xE) >> 1;
			
			int[] map = {0, 0x34, 0x57, 0x74, 0x90, 0xAC, 0xCE, 0xFF};
			r = map[r];
			g = map[g];
			b = map[b];
			
			int col = (r << 16) | (g << 8) | b;
			if (transparent)
				return col | ((palCol == 0)?0:0xFF000000);
			else
				return col;
		};
	}
	
	private static void loadIntermediate(PackableArrayWrapper rom, ArrayWrapper ram, int srcAddr)
	{
		int destAddr = 0;
		while (true)
		{
			short i = (short) (0xFF & rom.getByte(srcAddr));
			srcAddr++;
			if (word(i) == 0)
				return;
			
			boolean flag = (i & 0x80) != 0;
			i &= 0x7F;
			
			if (flag)
			{
				i = byteWriteToShort(i, i - 1);
				do
				{
					ram.putByte(word(destAddr), rom.getByte(srcAddr));
					srcAddr++;
					destAddr++;
					i--;
				}
				while (word(i) != 0xFFFF);
			}
			else
			{
				i = byteWriteToShort(i, i - 1);
				byte i0 = rom.getByte(srcAddr);
				srcAddr++;
				do
				{
					ram.putByte(word(destAddr), i0);
					destAddr++;
					i0++;
					i--;
				}
				while (word(i) != 0xFFFF);
			}
		}
	}
	private static void writeToTilemap(PackableArrayWrapper src, ArrayWrapper dest, int srcAddr, int shift)
	{
		int destAddr = 0;
		short counter = 0;
		while (true)
		{
			short i = (short) (0xFF & src.getByte(srcAddr)); //No masking
			srcAddr++;
			if ((0xFF & i) != 0)
			{
				boolean flag = (i & 0x80) != 0;
				i &= 0x7F;
				if (flag)
				{
					i = byteWriteToShort(i, i - 1);
					do
					{
						dest.putByte(destAddr, src.getByte(srcAddr));
						srcAddr++;
						destAddr = shortWrite(destAddr, destAddr + shift);
						i--;
					}
					while (word(i) != 0xFFFF);
				}
				else
				{
					i = byteWriteToShort(i, i - 1);
					byte data = src.getByte(srcAddr);
					srcAddr++;
					do
					{
						dest.putByte(word(destAddr), data);
						destAddr = shortWrite(destAddr, destAddr + shift);
						i--;
					}
					while (word(i) != 0xFFFF);
				}
			}
			else
			{
				destAddr = 0;
				counter++;
				destAddr = shortWrite(destAddr, destAddr + counter);
				if (word(counter) >= word(shift))
					return;
			}
		}
	}
	//Nemesis compression algorithm - a hybrid of RLE and Huffman
	private static byte[] decompressPatterns(ArrayWrapper m, int dataAddr)
	{
		int offset;
		int D3, linearBuffer, D5, D6;
		
		int gotoLabel = 0;
		
		offset = 0xFFFF & m.getShort(dataAddr);
		dataAddr += 2;
		boolean xorMode = (offset & 0x8000) != 0;
		offset = 0xFFFF & (offset << 3);
		
		int dataLength = offset * 4;
		if (dataLength > 0xffff)
			throw new IllegalArgumentException("Invalid data: length greater than VDP capacity");
		ByteBuffer out = ByteBuffer.allocate(dataLength);
		
		D3 = 8;
		int xorBuffer = 0;
		linearBuffer = 0;
		
		ArrayWrapper intermediateBuffer = new ArrayWrapper(new byte[0x200]);
		
		short i;
		short srcData = (short) 0xFFFF;
		short uVar1 = 0;
		short pixelData = 0;
		int srcAddr_plus1;
		
		boolean jumpToGotoLabel = false;
		
		srcData = (short) ((srcData & 0xFF00) + (0xFF & m.getByte(dataAddr)));
		srcAddr_plus1 = dataAddr + 1;
		L0: while (true)
		{
			do
			{
				if (!jumpToGotoLabel)
				{
					if (((byte) (0xFF & srcData)) == -1)
					{
					      break L0;
					}
					pixelData = srcData;
				}
				jumpToGotoLabel = false;
				
				srcData = (short) ((0xFF00 & srcData) + (0xFF & m.getByte(srcAddr_plus1)));
				srcAddr_plus1++;
			} while (0x7f < (0xFF & srcData));
			
			pixelData = (short) ((pixelData & 0xf) | (srcData & 0x70) | ((srcData & 0xf) << 8));
			srcData = (short) (0xF & srcData);
			uVar1 = (short) (8 - srcData);
			if (uVar1 == 0)
			{
				srcData = (short) (((0xFF00 & srcData) + (0xFF & m.getByte(srcAddr_plus1))) * 2);
				srcAddr_plus1++;
				
			    intermediateBuffer.putShort(srcData, pixelData);
			}
			else
			{
			    srcData = (short) ((0xFF & m.getByte(srcAddr_plus1) << (uVar1 & 0x3f)) * 2);
			    srcAddr_plus1++;
			    i = (short) ((1 << (uVar1 & 0x3f)) - 1);
			    do
			    {
			    	intermediateBuffer.putShort(srcData, pixelData);
			    	
			    	srcData += 2;
			    	i--;
			    } while (i != -1);
			}
			
			jumpToGotoLabel = true;
		}
		dataAddr = srcAddr_plus1;
		int D0 = (0xFFFF & srcData);
		int D1 = (0xFFFF & uVar1);
		int D7 = (0xFFFF & pixelData);
		
		D5 = 0xFFFF & ((m.getByte(dataAddr) << 8) | (0xFF & m.getByte(dataAddr + 1)));
		dataAddr += 2;
		D6 = 0x10;
		
		do
		{
			if (gotoLabel == 0)
			{
				D0 = 8;
				D7 = D6;
				D7 -= D0;
				D1 = D5;
				D1 >>= D7;
				D0 *= 2;
				D1 &= 0xFF;
			}

			if ((gotoLabel != 0) || (D1 < 0xfc))
			{
				if (gotoLabel == 0)
				{
					D1 *= 2;
					
					D0 = ((D0 & 0xFF00) | (0xFF & intermediateBuffer.getByte(0xFFFF & D1)));
					
					D6 -= D0;
					if (D6 < 9)
					{
						D6 += 8;
						D5 = 0xFFFF & (D5 << 8);
						D5 = ((D5 & 0xFF00) | (0xFF & m.getByte(dataAddr)));
						dataAddr++;
					}
					
					D1 = ((D1 & 0xFF00) | (0xFF & intermediateBuffer.getByte((0xFFFF & D1) + 1)));
				}
				
				if (gotoLabel == 0x15304)
					gotoLabel = 0;
				
				D0 = D1;
				D1 = D1 & 0xF;
				D0 = D0 & 0xF0;
				D0 >>= 4;
				do
				{
					linearBuffer <<= 4;
					linearBuffer = (linearBuffer & 0xFFFFFF00) | (0xFF & D1 | linearBuffer);
					D3--;
					if (D3 == 0)
					{
						if (xorMode)
						{
							xorBuffer ^= linearBuffer;
							
							out.putInt(xorBuffer);
							
							offset--;
							linearBuffer = (0xFFFF0000 & linearBuffer) | (0xFFFF & offset);
							if ((0xFFFF & linearBuffer) == 0)
							{
								byte[] ret = new byte[dataLength];
								out.flip();
								out.get(ret);
								return ret;
							}
						}
						else
						{
							out.putInt(linearBuffer);
							
							offset--;
							linearBuffer = (0xFFFF0000 & linearBuffer) | (0xFFFF & offset);
							if ((0xFFFF & linearBuffer) == 0)
							{
								byte[] ret = new byte[dataLength];
								out.flip();
								out.get(ret);
								
								return ret;
							}
						}
						
						linearBuffer = 0;
						D3 = 8;
					}
					D0 = ((0xFFFF0000 & D0) | (0xFFFF & (D0 - 1)));
				} while (((short) (0xFFFF & D0)) != -1);
			}
			else
			{
				D0 = 6;
				D6 = (0xFFFF0000 & D6) | (0xFFFF & (D6 - D0));
				if (D6 < 9)
				{
					D6 = (0xFFFF0000 & D6) | (0xFFFF & (D6 + 8));
					D5 = (0xFFFF0000 & D5) | (0xFFFF & (D5 << 8));
					D5 = (0xFFFFFF00 & D5) | (0xFF & m.getByte(dataAddr));
					dataAddr++;
				}
				
				D0 = 7;
				D7 = (0xFFFF0000 & D7) | (0xFFFF & D6);
				D7 = (0xFFFF0000 & D7) | (0xFFFF & (D7 - D0));
				D1 = (0xFFFF0000 & D1) | (0xFFFF & D5);
				D1 = (0xFFFF0000 & D1) | (0xFFFF & (D1 >> D7));
				D0 = (0xFFFF0000 & D0) | (0xFFFF & (D0 * 2));
				D1 = (0xFFFF0000 & D1) | (0xFFFF & ((D0 + 0x71) & D1));
				
				D0 = (0xFFFF0000 & D0) | (0xFFFF & (D0 >> 1));
				D6 = (0xFFFF0000 & D6) | (0xFFFF & (D6 - D0));
				if (D6 < 9)
				{
					D6 = (0xFFFF0000 & D6) | (0xFFFF & (D6 + 8));
					D5 = (0xFFFF0000 & D5) | (0xFFFF & (D5 << 8));
					D5 = (0xFFFFFF00 & D5) | (0xFF & m.getByte(dataAddr));
					dataAddr++;
				}
				
				gotoLabel = 0x15304;
			}
		}
		while (true);
	}
//	private static int loadPalettes(ArrayWrapper rom, ArrayWrapper ram, int destAddr, short D0)
//	{
//		int srcAddr = 0x2D6EC;
//		D0 <<= 3;
//		srcAddr = shortWrite(srcAddr, srcAddr + D0);
//		int dataAddr = rom.getInt(srcAddr);
//		srcAddr += 6; //+4, next 2 bytes specify absolute destAddr
//		srcAddr += 2;
//		int i = shortWrite(0, rom.getShort(srcAddr));
//		srcAddr += 2;
//		do
//		{
//			ram.putInt(word(destAddr), rom.getInt(dataAddr));
//			dataAddr += 4;
//			destAddr += 4;
//			i = shortWrite(i, i - 1);
//		}
//		while (word(i) != 0xFFFF);
//		return destAddr;
//	}
	private static short byteWriteToShort(int original, int newByte)
	{
		return (short) ((original & 0xFF00) | (0xFF & newByte));
	}
	private static int shortWrite(int original, int newByte)
	{
		return (original & 0xFFFF0000) | (0xFFFF & newByte);
	}
	private static int word(int src)
	{
		return 0xFFFF & src;
	}
}
