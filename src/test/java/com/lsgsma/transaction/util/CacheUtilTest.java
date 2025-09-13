package com.lsgsma.transaction.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CacheUtilTest {

    @Test
    void givenValidInputs_whenCallToBuildKey_ProperlyBuild() {

        var expectedKey = "a::b::c";
        assertEquals(expectedKey, CacheUtil.buildCacheKey("a", "b", "c"));

    }
}