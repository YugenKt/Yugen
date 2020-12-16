import yugen.Yugen
import yugen.YugenOptions

suspend fun main() {
    println(YugenOptions.userAgent)
    val yugen = Yugen("dfg")

    yugen.connect()
}