package filepeek

import failfast.describe
import org.junit.jupiter.api.Assertions

object LambdaBodyTest {

    val context = describe(LambdaBody::class) {
        it(" `extracts the body") {
            Assertions.assertEquals(
                "name",
                LambdaBody("get", """get { name }.isEqualTo("Ziggy")""").body
            )
        }

        it("works with different method names") {
            Assertions.assertEquals(
                "name",
                LambdaBody("callMe", """callMe { name }.isEqualTo("Ziggy")""").body
            )
        }

    }
}
