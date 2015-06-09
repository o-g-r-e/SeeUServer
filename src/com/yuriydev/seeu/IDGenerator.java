package com.yuriydev.seeu;

import java.util.Random;


public class IDGenerator
{
	private static char[] upperCaseChars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	private static char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	private static char[] numbers = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
	public  final static int SIZE_ID = 8;
	
	private static int randInt(int min, int max)
	{
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	private static char[] getRandomArray()
	{
		int ar = randInt(0, 2);
		
		switch (ar)
		{
		case 0:
			return upperCaseChars;
			
		case 1:
			return chars;
			
		case 2:
			return numbers;
		}
		
		return null;
	}
	
	private static char getRandomLetter()
	{
		char[] charArray = getRandomArray();
		if(charArray != null)
			return charArray[randInt(0, charArray.length-1)];
		
		return '#';
	}
	
	private static String genSimpleId()
	{
		StringBuilder stringID = new StringBuilder(SIZE_ID);
		
		for (int i = 0; i < SIZE_ID; i++)
		{
			stringID.append(getRandomLetter());
		}
		
		return stringID.toString();
	}

	public static String generateID()
	{
		return genSimpleId();
	}
}