package jokrey.utilities

import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridLayout
import java.lang.StringBuilder
import java.util.concurrent.ThreadLocalRandom
import javax.swing.*
import kotlin.math.abs


/**Both inclusive*/
data class Dice(val min: Int, val max: Int) {
    constructor(range: Int) : this(1, range)
    val range: Int
        get() = abs(min - (max+1))

    init {
        if(min > max) throw IllegalArgumentException("(min=$min) > (max=$max)")
    }

    companion object {
        val D2 = Dice(2)
        val D3 = Dice(3)
        val D4 = Dice(4)
        val D6 = Dice(6)
        val D8 = Dice(8)
        val D10 = Dice(10)
        val D12 = Dice(12)
        val D16 = Dice(16)
        val D20 = Dice(20)
        val D24 = Dice(24)
        val D30 = Dice(30)
        val D100 = Dice(100)

        fun swingSelector(parent: Component? = null) : Dice {
            val sanInput = JOptionPane.showInputDialog(parent, "Enter min max of custom dice:\nFormat: \"min, max\" || \"range\"").split(",").map { it.trim().replace("[^0-9]", "").toInt() }
            return when (sanInput.size) {
                1 -> {
                    val range = sanInput.first()
                    Dice(range)
                }
                2 -> {
                    val (min, max) = sanInput
                    Dice(min, max)
                }
                else -> throw IllegalArgumentException("requires different input")
            }
        }
    }

    override fun toString(): String {
        return if(min == 1) {
            "D$range"
        } else {
            "D[$min..$max]"
        }
    }
}


data class DiceConfiguration(val dice: List<Dice>) {
    constructor(vararg dice : Dice) : this(dice.toList())
    init {
        if(dice.isEmpty()) throw IllegalArgumentException("no dice in dice configuration")
    }
    val size: Int
        get() = dice.size

    operator fun get(index: Int) = dice[index]

    fun getRandomResults() = getRandomResults(rand(1024))
    fun getRandomResults(seed: ByteArray): DiceResults {
        val results = ArrayList<Int>(dice.size)

        val prng = SHA1PRNG(seed)
        for(d in dice) {
            val raw = prng.nextInt(d.range)
            results.add(d.min + raw)
        }

        return DiceResults(this, results)
    }

    override fun toString() = "Config(${dice.joinToString(",")})"


    companion object {
        fun swingSelector(parent: Component? = null) : DiceConfiguration {
            val panel = JPanel(BorderLayout())
            val diceListHolder = DefaultListModel<Dice>()
            val diceListJL = JList(diceListHolder)
            val footer = JPanel(GridLayout(1, 2))
            val add = JButton("add")
            val remove = JButton("remove")
            footer.add(add)
            footer.add(remove)
            panel.add(JScrollPane(diceListJL), BorderLayout.CENTER)
            panel.add(footer, BorderLayout.SOUTH)
            add.addActionListener { diceListHolder.addElement(Dice.swingSelector(parent)) }
            remove.addActionListener { diceListHolder.remove(diceListJL.selectedIndex) }
            diceListJL.selectionMode = ListSelectionModel.SINGLE_SELECTION

            val result = JOptionPane.showOptionDialog(parent, panel, "select Dice for new configuration.", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, arrayOf("Select", "Cancel"), "Cancel")
            if(result == 0) {
                return DiceConfiguration(diceListHolder.elements().toList())
            } else {
                throw IllegalArgumentException("canceled dialog")
            }
        }
    }
}

data class DiceResults(val config: DiceConfiguration, val results: List<Int>) {
    init {
        if (config.size != results.size) throw IllegalArgumentException("config.size != results.size")
    }

    val size: Int
        get() = results.size

    operator fun get(index: Int) = results[index]

    override fun toString(): String {
        val builder = StringBuilder()
        for((i, d) in config.dice.withIndex()) {
            builder.append(d.toString()).append("=").append(results[i])
            if(i + 1 != size)
                builder.append(", ")
        }
        return builder.toString()
    }
}


fun rand(size: Int): ByteArray {
    val b = ByteArray(size)
    ThreadLocalRandom.current().nextBytes(b)
    return b
}