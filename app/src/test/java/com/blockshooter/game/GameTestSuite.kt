package com.blockshooter.game

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite that runs all tests for the Block Shooter game
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    BlockTest::class,
    BallTest::class,
    CollisionTest::class,
    GameLogicTest::class,
    GoldBlockChainTest::class
)
class GameTestSuite 