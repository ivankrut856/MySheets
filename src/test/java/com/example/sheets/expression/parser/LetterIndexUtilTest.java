package com.example.sheets.expression.parser;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LetterIndexUtilTest {

    @Test
    public void testAAA() {
        assertEquals(703, LetterIndexUtil.toNumberIndex("AAA"));
    }

    @Test
    public void testRev1703() {
        assertEquals(1703, LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(1703)));
    }

    @Test
    public void testRev1726() {
        assertEquals(1726, LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(1726)));
    }

    @Test
    public void testRev972023() {
        assertEquals(972023, LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(972023)));
    }

    @Test
    public void testRev2100() {
        assertEquals(2100, LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(2100)));
    }

    @Test
    public void testRev35244() {
        assertEquals(35244, LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(35244)));
    }

    @Test
    public void testRev4126() {
        assertEquals(4126, LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(4126)));
    }

    @Property
    public boolean reversibilityTest(
        @ForAll("positive") int x
    ) {
        return x == LetterIndexUtil.toNumberIndex(LetterIndexUtil.toLetterIndex(x));
    }

    @Provide
    public Arbitrary<Integer> positive() {
        return Arbitraries.integers().greaterOrEqual(1);
    }

}