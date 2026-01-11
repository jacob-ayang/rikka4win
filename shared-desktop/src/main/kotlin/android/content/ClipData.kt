package android.content

class ClipData private constructor(
    val label: CharSequence?,
    private val items: List<Item>,
) {
    class Item(val text: CharSequence?)

    val itemCount: Int
        get() = items.size

    fun getItemAt(index: Int): Item = items[index]

    companion object {
        fun newPlainText(label: CharSequence?, text: CharSequence?): ClipData {
            return ClipData(label, listOf(Item(text)))
        }
    }
}
