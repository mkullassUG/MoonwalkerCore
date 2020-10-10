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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class IntRangeSet
{
	private IntRange[] rArr;
	
	public static final IntRangeSet EMPTY = new IntRangeSet();
	
	private IntRangeSet()
	{
		rArr = new IntRange[0];
	}
	public IntRangeSet(int start, int end)
	{
		rArr = new IntRange[]{new IntRange(start, end)};
	}
	
	private IntRangeSet(ArrayList<IntRange> ranges, boolean normalized)
	{
		if (normalized)
			rArr = ranges.toArray(l -> new IntRange[l]);
		else
			rArr = normalize(ranges.toArray(l -> new IntRange[l]));
	}
	
	public IntRangeSet(IntRange... ranges)
	{
		rArr = normalize(ranges);
	}
	public IntRangeSet(Collection<IntRange> ranges)
	{
		rArr = normalize(ranges.toArray(l -> new IntRange[l]));
	}
	
	private IntRange[] normalize(IntRange[] rangeArr)
	{
		Objects.requireNonNull(rangeArr);
		ArrayList<IntRange> sortedRanges = new ArrayList<>(Arrays.asList(rangeArr));
		sortedRanges.sort((a, b) ->
		{
			return a.getStart() - b.getStart();
		});
		
		ArrayList<IntRange> normalizedRanges = new ArrayList<>(sortedRanges.size());
		IntRange lastRange = null;
		for (IntRange r: sortedRanges)
		{
			int currStart = r.getStart();
			int currEnd = r.getEnd();
			if (currStart == currEnd)
				continue;
			
			if (lastRange != null)
			{
				int lastEnd = lastRange.getEnd();
				if (lastEnd >= currStart)
					lastRange = new IntRange(Math.min(lastRange.getStart(), currStart),
							Math.max(currEnd, lastEnd));
				else
				{
					normalizedRanges.add(lastRange);
					lastRange = r;
				}
			}
			else
				lastRange = r;
		}
		if (lastRange != null)
			normalizedRanges.add(lastRange);
		
		return normalizedRanges.toArray(l -> new IntRange[l]);
	}
	private IntRangeSet addOneRange(IntRange[] srcArr, IntRange r)
	{
		int rStart = r.getStart();
		int rEnd = r.getEnd();
		
		boolean rangeAdded = false;
		ArrayList<IntRange> rangeList = new ArrayList<>(Arrays.asList(srcArr));
		ArrayList<IntRange> newRangeList = new ArrayList<>(srcArr.length + 1);
		int l = rangeList.size();
		int i = 0;
		for (; i < l; i++)
		{
			IntRange currR = rangeList.get(i);
			if (currR.getEnd() >= rStart)
				break;
		}
		newRangeList.addAll(rangeList.subList(0, i));
		if (i < l)
		{
			int currStart = rangeList.get(i).getStart();
			if (currStart < rStart)
			{
				r = new IntRange(currStart, rEnd);
				rStart = currStart;
			}
		}
		for (; i < l; i++)
		{
			IntRange currR = rangeList.get(i);
			int currStart = currR.getStart();
			int currEnd = currR.getEnd();
			if (currStart > rEnd)
			{
				newRangeList.add(r);
				rangeAdded = true;
				break;
			}
			else if (currEnd >= rEnd)
			{
				newRangeList.add(new IntRange(rStart, currEnd));
				rangeAdded = true;
				i++;
				break;
			}
		}
		if (rangeAdded)
			newRangeList.addAll(rangeList.subList(i, l));
		else
			newRangeList.add(r);
		
		return new IntRangeSet(newRangeList, true);
	}
	
	public IntRangeSet union(IntRangeSet rs)
	{
		Objects.requireNonNull(rs);
		
		if (rArr.length == 0)
			return rs;
		if (rs.rArr.length == 0)
			return this;
		else if (rs.rArr.length == 1)
			return addOneRange(rArr, rs.rArr[0]);
		else if (rArr.length == 1)
			return addOneRange(rs.rArr, rArr[0]);
		
		ArrayList<IntRange> newRangeSet = new ArrayList<>();
		for (IntRange r: rArr)
			newRangeSet.add(r);
		for (IntRange r: rs.rArr)
			newRangeSet.add(r);
		return new IntRangeSet(newRangeSet);
	}
	public IntRangeSet union(IntRange r)
	{
		Objects.requireNonNull(r);
		
		if (rArr.length == 0)
			return new IntRangeSet(r);
		return addOneRange(rArr, r);
	}
	public IntRangeSet difference(IntRangeSet rs)
	{
		Objects.requireNonNull(rs);
		
		ArrayList<IntRange> newRangeSet = new ArrayList<>();
		
		IntRange[] subArr = rs.rArr;
		if (subArr.length == 0)
			return this;
		
		int i = 0;
		int subInd = 0;
		while (i < rArr.length)
		{
			IntRange r = rArr[i];
			int rStart = r.getStart();
			int rEnd = r.getEnd();
			
			while ((subInd < subArr.length) && (rStart >= subArr[subInd].getEnd()))
				subInd++;
			
			if (subInd >= subArr.length)
				break;
			
			
			IntRange subR = subArr[subInd];
			int subStart = subR.getStart();
			int subEnd = subR.getEnd();
			while ((subInd < subArr.length) && (subStart < rEnd))
			{
				if (rStart < subStart)
					newRangeSet.add(new IntRange(rStart, subStart));
				
				if (subEnd < rEnd)
					r = new IntRange(subEnd, rEnd);
				else
				{
					r = null;
					break;
				}
				
				rStart = r.getStart();
				rEnd = r.getEnd();
				subInd++;
				if (subInd < subArr.length)
				{
					subR = subArr[subInd];
					subStart = subR.getStart();
					subEnd = subR.getEnd();
				}
			}
			
			if (r != null)
				newRangeSet.add(r);
			
			i++;
			
			if (subInd >= subArr.length)
				break;
		}
		for (; i < rArr.length; i++)
			newRangeSet.add(rArr[i]);
		
		return new IntRangeSet(newRangeSet, true);
	}
	public IntRangeSet difference(IntRange r)
	{
		return this.difference(new IntRangeSet(r));
	}
	public IntRangeSet intersection(IntRangeSet rs)
	{
		return this.difference(this.difference(rs));
	}
	public IntRangeSet intersection(IntRange r)
	{
		return this.intersection(new IntRangeSet(r));
	}
	
	public IntRange findContinuousRange(int size)
	{
		for (IntRange r: rArr)
		{
			int start = r.getStart();
			int end = r.getEnd();
			if ((end - start) >= size)
				return new IntRange(start, start + size);
		}
		return null;
	}
	
	public IntRange[] getRangeArray()
	{
		return Arrays.copyOf(rArr, rArr.length);
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
		IntRangeSet other = (IntRangeSet) obj;
		if (!Arrays.equals(rArr, other.rArr))
			return false;
		return true;
	}
	@Override
	public int hashCode()
	{
		final int prime = 53;
		int result = 1;
		result = prime * result + Arrays.hashCode(rArr);
		return result;
	}
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		for (IntRange r: rArr)
		{
			sb.append("[");
			sb.append(r.getStart());
			sb.append(", ");
			sb.append(r.getEnd());
			sb.append("], ");
		}
		if (rArr.length > 0)
			sb.setLength(sb.length() - 2);
		sb.append("}");
		return sb.toString();
	}
	
//	public static void main(String[] args)
//	{
//		JFrame frame = new JFrame();
//		frame.setLocationByPlatform(true);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setLayout(new BorderLayout());
//		frame.setTitle("Test");
//		frame.setSize(900, 600);
//		
//		RangeSet[] rsArr = new RangeSet[3];
//		
//		Color[] outlineArr = {Color.BLUE, Color.GREEN, Color.RED};
//		Color[] fillArr =
//		{
//			new Color(0, 0, 255, 128),
//			new Color(0, 255, 0, 128),
//			new Color(255, 0, 0, 128)
//		};
//		
//		JPanel centerPanel = new JPanel()
//		{
//			@Override
//			public void paint(Graphics g)
//			{
//				super.paint(g);
//				int baseX = 25;
//				int baseY = 25;
//				int sizeY = 20;
//				int spaceY = 5;
//				int scaleX = 5;
//				
//				for (int i = 0; i < rsArr.length; i++)
//				{
//					if (rsArr[i] == null)
//						continue;
//					
//					int y = baseY + (i * (sizeY + spaceY));
//					Range[] rArr = rsArr[i].rArr;
//					for (int i0 = 0; i0 < rArr.length; i0++)
//					{
//						int start = rArr[i0].getStart();
//						int end = rArr[i0].getEnd();
//						
//						int x = baseX + (start * scaleX);
//						int sizeX = (end - start) * scaleX;
//						
//						g.setColor(fillArr[i]);
//						g.fillRect(x, y, sizeX, sizeY);
//						g.setColor(outlineArr[i]);
//						g.drawRect(x, y, sizeX, sizeY);
//					}
//				}
//			}
//		};
//		frame.add(centerPanel, BorderLayout.CENTER);
//		
//		JPanel southPanel = new JPanel();
//		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
//		
//		JTextField tfAugend = new JTextField();
//		JTextField tfAddend = new JTextField();
//		JLabel lResult = new JLabel();
//		
//		Runnable runCalc = () ->
//		{
//			String[] augend = tfAugend.getText().split(",");
//			String[] addend = tfAddend.getText().split(",");
//			Range[] augendArr = new Range[augend.length / 2];
//			Range[] addendArr = new Range[addend.length / 2];
//			try
//			{
//				for (int i = 0; i < augendArr.length; i++)
//				{
//					augendArr[i] = new Range(
//							Integer.parseInt(augend[2 * i].trim()),
//							Integer.parseInt(augend[(2 * i) + 1].trim()));
//				}
//				for (int i = 0; i < addendArr.length; i++)
//				{
//					addendArr[i] = new Range(
//							Integer.parseInt(addend[2 * i].trim()),
//							Integer.parseInt(addend[(2 * i) + 1].trim()));
//				}
//				
//				try
//				{
//					rsArr[0] = new RangeSet(augendArr);
//					rsArr[1] = new RangeSet(addendArr);
//					rsArr[2] = rsArr[0].union(rsArr[1]);
//					
//					lResult.setText(rsArr[2].toString());
//					centerPanel.repaint();
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//			catch (Exception e)
//			{}
//		};
//		tfAugend.addKeyListener(new KeyListener()
//		{
//			public void keyTyped(KeyEvent e)
//			{}
//			public void keyReleased(KeyEvent e)
//			{
//				runCalc.run();
//			}
//			public void keyPressed(KeyEvent e)
//			{}
//		});
//		tfAddend.addKeyListener(new KeyListener()
//		{
//			public void keyTyped(KeyEvent e)
//			{}
//			public void keyReleased(KeyEvent e)
//			{
//				runCalc.run();
//			}
//			public void keyPressed(KeyEvent e)
//			{}
//		});
//		
//		southPanel.add(tfAugend);
//		southPanel.add(tfAddend);
//		southPanel.add(lResult);
//		
//		frame.add(southPanel, BorderLayout.SOUTH);
//		
//		frame.setVisible(true);
//		
//		System.out.println("Starting equality, commutativity, associativity "
//				+ "and normalization tests for addition...");
//		Random r = new Random();
//		boolean testSucceeded = true;
//		int l = 10000000;
//		int printStep = l / 50;
//		for (int i = 0; i < l; i++)
//		{
//			Range[] rArr1 = new Range[(int) Math.round(Math.abs(r.nextGaussian() * 12 + 3))];
//			Range[] rArr2 = new Range[(int) Math.round(Math.abs(r.nextGaussian() * 12 + 3))];
//			Range[] rArr3 = new Range[(int) Math.round(Math.abs(r.nextGaussian() * 12 + 3))];
//			
//			for (int i0 = 0; i0 < rArr1.length; i0++)
//			{
//				int n1 = r.nextInt(10000) - 5000;
//				int n2 = r.nextInt(10000) - 5000;
//				rArr1[i0] = new Range(Math.min(n1,  n2), Math.max(n1, n2));
//			}
//			for (int i0 = 0; i0 < rArr2.length; i0++)
//			{
//				int n1 = r.nextInt(10000) - 5000;
//				int n2 = r.nextInt(10000) - 5000;
//				rArr2[i0] = new Range(Math.min(n1,  n2), Math.max(n1, n2));
//			}
//			for (int i0 = 0; i0 < rArr3.length; i0++)
//			{
//				int n1 = r.nextInt(10000) - 5000;
//				int n2 = r.nextInt(10000) - 5000;
//				rArr3[i0] = new Range(Math.min(n1,  n2), Math.max(n1, n2));
//			}
//			
//			RangeSet rs1 = new RangeSet(rArr1);
//			RangeSet rs2 = new RangeSet(rArr2);
//			RangeSet rs3 = new RangeSet(rArr3);
//			
//			boolean test =
//					rs1.equals(rs1)
//					&& rs2.equals(rs2)
//					&& rs3.equals(rs3)
//					
//					&& isNormalized(rs1)
//					&& isNormalized(rs2)
//					&& isNormalized(rs3)
//					
//					&& rs1.union(rs1).equals(rs1)
//					&& rs2.union(rs2).equals(rs2)
//					&& rs3.union(rs3).equals(rs3)
//					
//					&& rs1.union(rs2).equals(rs2.union(rs1))
//					&& rs2.union(rs1).equals(rs1.union(rs2))
//					&& rs1.union(rs3).equals(rs3.union(rs1))
//					&& rs3.union(rs1).equals(rs1.union(rs3))
//					&& rs2.union(rs3).equals(rs3.union(rs2))
//					&& rs3.union(rs2).equals(rs2.union(rs3))
//					
//					&& rs1.union(rs2).union(rs3).equals(rs2.union(rs3).union(rs1))
//					&& rs2.union(rs3).union(rs1).equals(rs1.union(rs2).union(rs3));
//			
//			if (!test)
//			{
//				System.out.println("Tests failed.");
//				System.out.println("RS1: " + rs1);
//				System.out.println("RS2: " + rs2);
//				System.out.println("RS3: " + rs3);
//				System.out.println("RS1 + RS2: " + rs1.union(rs2));
//				System.out.println("RS2 + RS1: " + rs2.union(rs1));
//				System.out.println("RS1 + RS3: " + rs1.union(rs3));
//				System.out.println("RS3 + RS1: " + rs3.union(rs1));
//				System.out.println("RS2 + RS3: " + rs2.union(rs3));
//				System.out.println("RS3 + RS2: " + rs3.union(rs2));
//				System.out.println("RS1 + RS2 + RS3: " + rs1.union(rs2).union(rs3));
//				System.out.println("RS1 + RS3 + RS2: " + rs1.union(rs3).union(rs2));
//				System.out.println("RS2 + RS1 + RS3: " + rs2.union(rs1).union(rs3));
//				System.out.println("RS2 + RS3 + RS1: " + rs2.union(rs3).union(rs1));
//				System.out.println("RS3 + RS1 + RS2: " + rs3.union(rs1).union(rs2));
//				System.out.println("RS3 + RS2 + RS1: " + rs3.union(rs2).union(rs1));
//				testSucceeded = false;
//				break;
//			}
//			
//			if ((i + 1) % printStep == 0)
//				System.out.println((i + 1) + " cycles done ("
//						+ ((i + 1) * 100.0 / l) + "%)");
//		}
//		if (testSucceeded)
//			System.out.println("Tests completed successfully.");
//		
////		System.out.println("Starting tests for subtraction...");
////		Random r = new Random();
////		boolean testSucceeded = true;
////		int l = 10000000;
////		int printStep = l / 50;
////		for (int i = 0; i < l; i++)
////		{
////			Range[] rArr1 = new Range[(int) Math.round(Math.abs(r.nextGaussian() * 12 + 3))];
////			Range[] rArr2 = new Range[(int) Math.round(Math.abs(r.nextGaussian() * 12 + 3))];
////			Range[] rArr3 = new Range[(int) Math.round(Math.abs(r.nextGaussian() * 12 + 3))];
////			
////			for (int i0 = 0; i0 < rArr1.length; i0++)
////			{
////				int n1 = r.nextInt(10000) - 5000;
////				int n2 = r.nextInt(10000) - 5000;
////				rArr1[i0] = new Range(Math.min(n1,  n2), Math.max(n1, n2));
////			}
////			for (int i0 = 0; i0 < rArr2.length; i0++)
////			{
////				int n1 = r.nextInt(10000) - 5000;
////				int n2 = r.nextInt(10000) - 5000;
////				rArr2[i0] = new Range(Math.min(n1,  n2), Math.max(n1, n2));
////			}
////			for (int i0 = 0; i0 < rArr3.length; i0++)
////			{
////				int n1 = r.nextInt(10000) - 5000;
////				int n2 = r.nextInt(10000) - 5000;
////				rArr3[i0] = new Range(Math.min(n1,  n2), Math.max(n1, n2));
////			}
////			
////			RangeSet rs1 = new RangeSet(rArr1);
////			RangeSet rs2 = new RangeSet(rArr2);
////			RangeSet rs3 = new RangeSet(rArr3);
////			
////			boolean test =
////					rs1.difference(rs1).equals(RangeSet.EMPTY)
////					&& rs2.difference(rs2).equals(RangeSet.EMPTY)
////					&& rs3.difference(rs3).equals(RangeSet.EMPTY)
////					&& rs1.difference(RangeSet.EMPTY).equals(rs1)
////					&& rs2.difference(RangeSet.EMPTY).equals(rs2)
////					&& rs3.difference(RangeSet.EMPTY).equals(rs3)
////					
////					&& rs1.difference(rs2).union(rs2).equals(rs1.union(rs2))
////					&& rs2.difference(rs1).union(rs1).equals(rs2.union(rs1))
////					&& rs1.difference(rs3).union(rs3).equals(rs1.union(rs3))
////					&& rs3.difference(rs1).union(rs1).equals(rs3.union(rs1))
////					&& rs2.difference(rs3).union(rs3).equals(rs2.union(rs3))
////					&& rs3.difference(rs2).union(rs2).equals(rs3.union(rs2))
////					
////					&& rs1.difference(rs2).difference(rs3).equals(rs1.difference(rs3).difference(rs2))
////					&& rs1.difference(rs3).difference(rs2).equals(rs1.difference(rs2).difference(rs3))
////					&& rs2.difference(rs1).difference(rs3).equals(rs2.difference(rs3).difference(rs1))
////					&& rs2.difference(rs3).difference(rs1).equals(rs2.difference(rs1).difference(rs3))
////					&& rs3.difference(rs1).difference(rs2).equals(rs3.difference(rs2).difference(rs1))
////					&& rs3.difference(rs2).difference(rs1).equals(rs3.difference(rs1).difference(rs2))
////					
////					&& rs1.difference(rs1.difference(rs2)).equals(rs2.difference(rs2.difference(rs1)))
////					&& rs1.difference(rs1.difference(rs3)).equals(rs3.difference(rs3.difference(rs1)))
////					&& rs2.difference(rs2.difference(rs3)).equals(rs3.difference(rs3.difference(rs2)));
////			
////			if (!test)
////			{
////				System.out.println("Tests failed.");
////				System.out.println("RS1: " + rs1);
////				System.out.println("RS2: " + rs2);
////				System.out.println("RS3: " + rs3);
////				System.out.println("Test 1: " + rs1.difference(rs1).equals(RangeSet.EMPTY));
////				System.out.println("Test 2: " + rs2.difference(rs2).equals(RangeSet.EMPTY));
////				System.out.println("Test 3: " + rs3.difference(rs3).equals(RangeSet.EMPTY));
////				
////				System.out.println("Test 4: " + rs1.difference(RangeSet.EMPTY).equals(rs1));
////				System.out.println("Test 5: " + rs2.difference(RangeSet.EMPTY).equals(rs2));
////				System.out.println("Test 6: " + rs3.difference(RangeSet.EMPTY).equals(rs3));
////				
////				System.out.println("Test 7: " + rs1.difference(rs2).union(rs2).equals(rs1.union(rs2)));
////				System.out.println("Test 8: " + rs2.difference(rs1).union(rs1).equals(rs2.union(rs1)));
////				System.out.println("Test 9: " + rs1.difference(rs3).union(rs3).equals(rs1.union(rs3)));
////				System.out.println("Test 10: " + rs3.difference(rs1).union(rs1).equals(rs3.union(rs1)));
////				System.out.println("Test 11: " + rs2.difference(rs3).union(rs3).equals(rs2.union(rs3)));
////				System.out.println("Test 12: " + rs3.difference(rs2).union(rs2).equals(rs3.union(rs2)));
////				
////				System.out.println("Test 13: " + rs1.difference(rs2).difference(rs3).equals(rs1.difference(rs3).difference(rs2)));
////				System.out.println("Test 14: " + rs1.difference(rs3).difference(rs2).equals(rs1.difference(rs2).difference(rs3)));
////				System.out.println("Test 15: " + rs2.difference(rs1).difference(rs3).equals(rs2.difference(rs3).difference(rs1)));
////				System.out.println("Test 16: " + rs2.difference(rs3).difference(rs1).equals(rs2.difference(rs1).difference(rs3)));
////				System.out.println("Test 17: " + rs3.difference(rs1).difference(rs2).equals(rs3.difference(rs2).difference(rs1)));
////				System.out.println("Test 18: " + rs3.difference(rs2).difference(rs1).equals(rs3.difference(rs1).difference(rs2)));
////				
////				System.out.println("Test 19: " + rs1.difference(rs1.difference(rs2)).equals(rs2.difference(rs2.difference(rs1))));
////				System.out.println("Test 20: " + rs1.difference(rs1.difference(rs3)).equals(rs3.difference(rs3.difference(rs1))));
////				System.out.println("Test 21: " + rs2.difference(rs2.difference(rs3)).equals(rs3.difference(rs3.difference(rs2))));
////				
////				testSucceeded = false;
////				break;
////			}
////			
////			if ((i + 1) % printStep == 0)
////				System.out.println((i + 1) + " cycles done ("
////						+ ((i + 1) * 100.0 / l) + "%)");
////		}
////		if (testSucceeded)
////			System.out.println("Tests completed successfully.");
//	}
//	
//	private static boolean isNormalized(RangeSet rs)
//	{
//		Range[] rArr = rs.rArr;
//		Range lastR = null;
//		for (Range r: rArr)
//		{
//			if (lastR != null)
//			{
//				if (lastR.getEnd() >= r.getStart())
//					return false;
//			}
//			lastR = r;
//		}
//		return true;
//	}
}
