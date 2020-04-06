package jokrey.utilities

import jokrey.utilities.misc.RSAAuthHelper
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.lang.NullPointerException
import java.lang.StringBuilder
import java.security.KeyPair
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


/**
 *
 * @author jokrey
 */

fun main() {
    val generatedKeysHolder = DefaultListModel<KeyPair>()

    val frame = JFrame("RSA Auth Helper Frame")
    val midPanel = JPanel(BorderLayout())
    val generateJB = JButton("generate")
    val generatedKeysList = JList(generatedKeysHolder)
    val keysDisplayPanel = JPanel()
    val deleteJB = JButton("delete -: X")
    val pubKJTA = JTextArea()
    val privKJTA = JTextArea()
    val encodedKeysJTA = JTextArea()
    val toClipboard = JButton("copy to clipboard")

    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    generatedKeysList.selectionMode = ListSelectionModel.SINGLE_SELECTION
    generatedKeysList.cellRenderer = object : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            return super.getListCellRendererComponent(list, "$index -: ${if(value==null) "invalid" else encodeKeyPair(value as KeyPair)}", index, isSelected, cellHasFocus)
        }
    }
    generatedKeysList.addListSelectionListener {
        val selIndex = generatedKeysList.selectedIndex
        if(selIndex >= 0 && selIndex < generatedKeysHolder.size()) {
            val selectedKeyPair = generatedKeysHolder.get(selIndex)
            setTextsBasedOnKeyPair(selectedKeyPair, pubKJTA, privKJTA, encodedKeysJTA)
            deleteJB.text = "delete -: $selIndex"
            keysDisplayPanel.isVisible = true
        } else {
            keysDisplayPanel.isVisible = false
        }
    }

    generateJB.addActionListener {
        val newPair = RSAAuthHelper.generateKeyPair()
        generatedKeysHolder.addElement(newPair)
        generatedKeysList.setSelectedValue(newPair, true)
    }

    deleteJB.addActionListener {
        generatedKeysHolder.remove(generatedKeysList.selectedIndex)
    }

    toClipboard.addActionListener {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(encodedKeysJTA.text), null)
    }

    val listener = object : DocumentListener {
        override fun changedUpdate(e: DocumentEvent) = changed(e)
        override fun insertUpdate(e: DocumentEvent) = changed(e)
        override fun removeUpdate(e: DocumentEvent) = changed(e)
        var blocked = false
        fun changed(e: DocumentEvent) {
            if(blocked) return
            blocked=true
            val newKeyPair = when (e.document) {
                pubKJTA.document, privKJTA.document -> {
                    setTextsBasedOnKeyPair(decodeAndVerifyKeyPair(pubKJTA.text, privKJTA.text), null, null, encodedKeysJTA)
                }
                else -> {//encodedKeysJTA
                    setTextsBasedOnKeyPair(decodeAndVerifyKeyPair(encodedKeysJTA.text), pubKJTA, privKJTA, null)
                }
            }
            if(newKeyPair!=null)
                generatedKeysHolder.set(generatedKeysList.selectedIndex, newKeyPair)
            blocked = false
        }
    }
    pubKJTA.lineWrap = true
    pubKJTA.font = Font("monospaced", Font.PLAIN, 12)
    pubKJTA.document.addDocumentListener(listener)
    privKJTA.lineWrap = true
    privKJTA.font = Font("monospaced", Font.PLAIN, 12)
    privKJTA.document.addDocumentListener(listener)
    encodedKeysJTA.lineWrap = true
    encodedKeysJTA.font = Font("monospaced", Font.PLAIN, 12)
    encodedKeysJTA.document.addDocumentListener(listener)

    midPanel.add(JScrollPane(generatedKeysList), BorderLayout.CENTER)
    midPanel.add(generateJB, BorderLayout.SOUTH)
    frame.add(midPanel, BorderLayout.CENTER)
    keysDisplayPanel.layout = BoxLayout(keysDisplayPanel, BoxLayout.Y_AXIS)
    keysDisplayPanel.add(deleteJB)
    val pubKeyJL = JLabel("Public Key:");pubKeyJL.alignmentX = Component.LEFT_ALIGNMENT;keysDisplayPanel.add(pubKeyJL)
    keysDisplayPanel.add(JScrollPane(pubKJTA))
    val privKeyJL = JLabel("Private Key:");privKeyJL.alignmentX = Component.LEFT_ALIGNMENT;keysDisplayPanel.add(privKeyJL)
    keysDisplayPanel.add(JScrollPane(privKJTA))
    val encodedKeysJL = JLabel("Encoded Keys:");encodedKeysJL.alignmentX = Component.LEFT_ALIGNMENT;keysDisplayPanel.add(encodedKeysJL)
    keysDisplayPanel.add(JScrollPane(encodedKeysJTA))
    keysDisplayPanel.add(toClipboard)
    frame.add(keysDisplayPanel, BorderLayout.WEST)

    keysDisplayPanel.isVisible = true
    frame.isVisible = true
    frame.pack()
    frame.setLocationRelativeTo(null)

    keysDisplayPanel.isVisible = false
}

fun setTextsBasedOnKeyPair(selectedKeyPair: KeyPair?, pubKJTA: JTextArea?, privKJTA: JTextArea?, encodedKeysJTA: JTextArea?) : KeyPair? {
    pubKJTA?.updateText(if(selectedKeyPair==null) "invalid" else "-----BEGIN PUBLIC KEY-----\n" + base64Encode(selectedKeyPair.public.encoded).insertEvery(64, "\n") + "\n-----END PUBLIC KEY-----")
    privKJTA?.updateText(if(selectedKeyPair==null) "invalid" else "-----BEGIN RSA PRIVATE KEY-----\n" + base64Encode(selectedKeyPair.private.encoded).insertEvery(64, "\n") + "\n-----END RSA PRIVATE KEY-----")
    encodedKeysJTA?.updateText(if(selectedKeyPair==null) "invalid" else encodeKeyPair(selectedKeyPair))
    return selectedKeyPair
}

private fun JTextArea.updateText(s: String) {
    try {
        if (this.text != s) this.text = s
    } catch (e : NullPointerException) {}
}

//EITHER WITH OR WITHOUT -----BEGIN  ETC-BASE64 END-----
//EITHER WITH OR WITHOUT LINE BREAKS AFTER 64 characters
fun decodeAndVerifyKeyPair(publicKeyRepresenting: String, privateKeyRepresenting: String) = try {
    val pubKey = unwrapFromSimpleDER(publicKeyRepresenting)
    val privateKey = unwrapFromSimpleDER(privateKeyRepresenting)
    val decoded = RSAAuthHelper.readKeyPair(pubKey, privateKey)
    if(! RSAAuthHelper.verifyKeyPair(decoded))  null
    else                                        decoded
} catch(t: Throwable) {
    null
}
fun decodeAndVerifyKeyPair(encodedKeyPair: String) = try {
    val decoded = decodeKeyPair(encodedKeyPair)
    if(! RSAAuthHelper.verifyKeyPair(decoded))  null
    else                                        decoded
} catch(t: Throwable) {
    null
}

fun unwrapFromSimpleDER(keyRepresentation: String) : ByteArray {
    var endOfHeaderLine = keyRepresentation.indexOf("-\n") + 2
    if(endOfHeaderLine == 1) endOfHeaderLine = 0 //if no occurrence of "-\n"
    var startOfEndLine = keyRepresentation.lastIndexOf("\n-")
    if(startOfEndLine == -1) startOfEndLine = keyRepresentation.length //if no occurrence of "\n-"
    val base64Only = keyRepresentation.substring(endOfHeaderLine, startOfEndLine).replace("\n", "")
//    println("keyRepresentation = ${keyRepresentation}")
//    println("base64Only = ${base64Only}")
    return base64Decode(base64Only)
}


private fun String.insertEvery(every: Int, toInsert: String): String {
    val builder = StringBuilder(this)
    for(i in every until length step every)
        builder.insert(i, toInsert)
    return builder.toString()
}