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

package moonwalker.core.structures;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class MoonwalkerStageArea
{
	private byte[] layerAmap;
	private byte[] layerBmap;
	private short[][] tilemap;
	private byte[][] tileset;
	private int areaWidth;
	
	public MoonwalkerStageArea(byte[] tileset, byte[] tilemap,
			byte[] layerA, byte[] layerB, int areaWidth)
	{
		if (layerB != null && layerB.length == 0)
			layerB = null;
		
		layerAmap = Arrays.copyOf(layerA, layerA.length);
		if (layerB != null)
			layerBmap = Arrays.copyOf(layerB, layerB.length);
		this.tilemap = new short[tilemap.length / 8][4];
		for (int i = 0; i < tilemap.length; i += 8)
		{
			this.tilemap[i / 8][0] = packShort(tilemap[i], tilemap[i + 1]);
			this.tilemap[i / 8][1] = packShort(tilemap[i + 2], tilemap[i + 3]);
			this.tilemap[i / 8][2] = packShort(tilemap[i + 4], tilemap[i + 5]);
			this.tilemap[i / 8][3] = packShort(tilemap[i + 6], tilemap[i + 7]);
		}
		this.tileset = new byte[tileset.length / 32][32];
		
		for (int i = 0; i < tileset.length; i += 32)
			for (int i0 = 0; i0 < 32; i0++)
				this.tileset[i / 32][i0] = tileset[i + i0];
		
		this.areaWidth = areaWidth;
		if ((layerBmap != null) && (layerAmap.length != layerBmap.length))
			throw new IllegalArgumentException("Layer sizes do not match. (LA="
					+ layerAmap.length + ", LB = " + layerBmap.length + ")");
		if (layerAmap.length % areaWidth != 0)
			throw new IllegalArgumentException(
					"Incorrect length: Stage is not a rectangle. (w="
					+ areaWidth + ", l=" + layerAmap.length + ")");
	}
	
	public BufferedImage createLayerA(MoonwalkerPalette pal, boolean transparent)
	{
		return createLayer(layerAmap, pal, transparent);
	}
	public BufferedImage createLayerB(MoonwalkerPalette pal, boolean transparent)
	{
		return createLayer(layerBmap, pal, transparent);
	}
	private BufferedImage createLayer(byte[] layer, MoonwalkerPalette pal, boolean transparent)
	{
		if (layer == null)
			return null;
		
		BufferedImage ret = new BufferedImage(areaWidth * 8, (layer.length / areaWidth) * 32,
				transparent?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB);
		int width = areaWidth / 2;
		for (int i = 0; i < layer.length; i++)
		{
			int baseX = (i % width) * 16;
			int baseY = (i / width) * 16;
			
			short[] tileIndArr = tilemap[0xFF & layer[i]];
			for (int i0 = 0; i0 < 4; i0++)
			{
				int qX = 8 * (i0 % 2);
				int qY = 8 * (i0 / 2);
				int tileIndex = 0xFFFF & tileIndArr[i0];
				
				int tileIndexMasked = tileIndex & 0x3FF;

				boolean prio = (tileIndex & 0x8000) != 0;
				
				boolean f1 = (tileIndex & 0x4000) != 0;
				boolean f2 = (tileIndex & 0x2000) != 0;
				boolean f3 = (tileIndex & 0x400) != 0;
				
				boolean vflip = (tileIndex & 0x1000) != 0;
				boolean hflip = (tileIndex & 0x800) != 0;
				byte[] quadrant = applyMirroring(tileset[tileIndexMasked], vflip, hflip);
				for (int i1 = 0; i1 < quadrant.length; i1++)
				{
					int ind = i1 * 2;
					int x = baseX + qX + (ind % 8);
					int y = baseY + qY + (ind / 8);
					
					ret.setRGB(x, y, pal.getColor(0, (0xF0 & quadrant[i1]) >> 4,
							transparent, prio, f1, f2, f3));
					ret.setRGB(x + 1, y, pal.getColor(0, 0xF & quadrant[i1],
							transparent, prio, f1, f2, f3));
				}
			}
		}
		return ret;
	}
	
	private static short packShort(byte h, byte l)
	{
		return (short) (((h & 0xFF) << 8) | (l & 0xFF));
	}
	private static byte[] applyMirroring(byte[] src, boolean vflip, boolean hflip)
	{
		if (vflip)
		{
			byte[] ret = new byte[src.length];
			if (hflip)
			{
				for (int i = 0; i < ret.length; i++)
					ret[31 - i] = src[i];
				for (int i = 0; i < ret.length; i++)
					ret[i] = (byte) (((0xF0 & ret[i]) >> 4) | ((0xF & ret[i]) << 4));
			}
			else
			{
				for (int i = 0; i < ret.length; i++)
					ret[(i % 4) + (4 * (7 - (i / 4)))] = src[i];
			}
			return ret;
		}
		else if (hflip)
		{
			byte[] ret = new byte[src.length];
			for (int i = 0; i < ret.length; i++)
				ret[(3 - (i % 4)) + (4 * (i / 4))] = src[i];
			for (int i = 0; i < ret.length; i++)
				ret[i] = (byte) (((0xF0 & ret[i]) >> 4) | ((0xF & ret[i]) << 4));
			return ret;
		}
		return src;
	}
}
