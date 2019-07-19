package filepeek

import kotlin.test.Test
import kotlin.test.assertEquals

class LambdaBodyTest {

    @Test
    fun `extracts the body`() {
        assertEquals(
            "name",
            LambdaBody("get", """get { name }.isEqualTo("Ziggy")""").body
        )
    }

    @Test
    fun `works with different method names`() {
        assertEquals(
            "name",
            LambdaBody("callMe", """callMe { name }.isEqualTo("Ziggy")""").body
        )
    }
}
