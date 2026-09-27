package com.ontariotechu.sofe3980U;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit test for Binary class.
 */
public class BinaryTest 
{
    @Test
    public void normalConstructor()
    {
        Binary binary = new Binary("1001001");
        assertTrue(binary.getValue().equals("1001001"));
    }

    @Test
    public void constructorWithInvalidDigits()
    {
        Binary binary = new Binary("10010012");
        assertTrue(binary.getValue().equals("0"));
    }

    @Test
    public void constructorWithLeadingZeros()
    {
        Binary binary = new Binary("00001001");
        assertTrue(binary.getValue().equals("1001"));
    }

    @Test
    public void constructorEmptyString()
    {
        Binary binary = new Binary("");
        assertTrue(binary.getValue().equals("0"));
    }

    @Test
    public void add()
    {
        Binary binary1 = new Binary("1000");
        Binary binary2 = new Binary("111");
        Binary binary3 = Binary.add(binary1, binary2);
        assertTrue(binary3.getValue().equals("1111"));
    }

    @Test
    public void add2()
    {
        Binary binary1 = new Binary("1010");
        Binary binary2 = new Binary("11");
        Binary binary3 = Binary.add(binary1, binary2);
        assertTrue(binary3.getValue().equals("1101"));
    }

    @Test
    public void or()
    {
        Binary binary1 = new Binary("1010");
        Binary binary2 = new Binary("1100");
        Binary binary3 = Binary.or(binary1, binary2);
        assertTrue(binary3.getValue().equals("1110"));
    }

    @Test
    public void orWithDifferentLengths()
    {
        Binary binary1 = new Binary("10101");
        Binary binary2 = new Binary("110");
        Binary binary3 = Binary.or(binary1, binary2);
        assertTrue(binary3.getValue().equals("10111"));
    }

    @Test
    public void and()
    {
        Binary binary1 = new Binary("1010");
        Binary binary2 = new Binary("1100");
        Binary binary3 = Binary.and(binary1, binary2);
        assertTrue(binary3.getValue().equals("1000"));
    }

    @Test
    public void andWithZeros()
    {
        Binary binary1 = new Binary("1010");
        Binary binary2 = new Binary("0101");
        Binary binary3 = Binary.and(binary1, binary2);
        assertTrue(binary3.getValue().equals("0"));
    }

    @Test
    public void multiply()
    {
        Binary binary1 = new Binary("10");
        Binary binary2 = new Binary("11");
        Binary binary3 = Binary.multiply(binary1, binary2);
        assertTrue(binary3.getValue().equals("110"));
    }

    @Test
    public void multiplyByZero()
    {
        Binary binary1 = new Binary("10101");
        Binary binary2 = new Binary("0");
        Binary binary3 = Binary.multiply(binary1, binary2);
        assertTrue(binary3.getValue().equals("0"));
    }

    @Test
    public void multiplyLarge()
    {
        Binary binary1 = new Binary("101");  // 5
        Binary binary2 = new Binary("110");  // 6
        Binary binary3 = Binary.multiply(binary1, binary2);
        assertTrue(binary3.getValue().equals("11110")); // 30
    }
}
