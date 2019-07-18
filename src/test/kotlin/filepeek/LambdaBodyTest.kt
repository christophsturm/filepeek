package filepeek

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LambdaBodyTest {

    @Test
    fun `extracts the body`() {
        Assertions.assertEquals(
            "name",
            LambdaBody("get", """get { name }.isEqualTo("Ziggy")""").body
        )
    }

    @Test
    fun `works with different method names`() {
        Assertions.assertEquals(
            "name",
            LambdaBody("callMe", """callMe { name }.isEqualTo("Ziggy")""").body
        )
    }
}
