package com.tomaszrykala.githubbrowser.compose.testutil

import io.mockk.MockK
import io.mockk.MockKDsl
import kotlin.reflect.KClass

/**
 * Shorthand for Any.mockk(relaxed = true).
 */
inline fun <reified T : Any> mockkR(
    name: String? = null,
    vararg moreInterfaces: KClass<*>,
    relaxUnitFun: Boolean = false,
    block: T.() -> Unit = {}
): T = MockK.useImpl {
    MockKDsl.internalMockk(
        name,
        true,
        *moreInterfaces,
        relaxUnitFun = relaxUnitFun,
        block = block
    )
}