package com.bridgewalkerapp.androidclient.tests;

import com.bridgewalkerapp.androidclient.BitcoinURI;

import junit.framework.TestCase;

public class BitcoinURITest extends TestCase {
	private void helper(String input, String expectedAddress, long expectedAmount) {
		BitcoinURI uri = BitcoinURI.parse(input);
		assertEquals(expectedAddress, uri.getAddress());
		assertEquals(expectedAmount, uri.getAmount());
	}
	
	public void testStandardFormat() {
		helper("bitcoin:14Z1mazY4HfysZyMaKudFr63EwHqQT2njz",
					"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 0);
		
		helper("bitcoin:14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=42",
					"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 4200000000L); 
		
		helper("bitcoin:14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=0.01",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 1000000L);
		
		helper("bitcoin:14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=0.00000001",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 1L);
	}
	
	public void testBrokenFormat() {
		helper("bitcoin://14Z1mazY4HfysZyMaKudFr63EwHqQT2njz",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 0);
	
		helper("bitcoin://14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=42",
					"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 4200000000L); 		
	}
	
	public void testPlainFormat() {
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 0);
	
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=42",
					"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 4200000000L); 		
	}

	public void testMalformedAmount() {
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 0);
		
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 0);
		
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=asdf",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 0);		
	}
	
	public void testMultipleParameters() {
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?desc=test&amount=1",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 100000000L);
		
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?amount=1&desc=test",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 100000000L);
		
		helper("14Z1mazY4HfysZyMaKudFr63EwHqQT2njz?desc=test&amount=1&desc=test",
				"14Z1mazY4HfysZyMaKudFr63EwHqQT2njz", 100000000L);
	}
}
