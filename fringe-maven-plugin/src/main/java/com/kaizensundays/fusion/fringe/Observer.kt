package com.kaizensundays.fusion.fringe

import com.formdev.flatlaf.FlatIntelliJLaf
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.CompletableFuture
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.SpringLayout
import javax.swing.SwingUtilities

/**
 * Created: Thursday 10/31/2024, 7:22 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class Observer {

    private fun <T : JTextField> Array<T>.isOk(): Boolean {
        this.forEach { fld -> fld.text = fld.text.trim() }
        if (this.first().text == this.last().text && this.first().text.isNotBlank()) {
            return true
        }
        Toolkit.getDefaultToolkit().beep()
        return false
    }

    private fun JPasswordField.getValue(): String {
        return String(this.password)
    }

    fun build(): CompletableFuture<String> {

        val done = CompletableFuture<String>()

        FlatIntelliJLaf.setup()

        val frame = JFrame()

        fun complete(value: String) {
            done.complete(value)
            SwingUtilities.invokeLater { frame.dispose() }
        }

        frame.title = "Observer"
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(380, 220)
        frame.setLocationRelativeTo(null)

        val content = frame.contentPane
        content.background = Color(231, 231, 231)

        val layout = SpringLayout()
        content.layout = layout

        val textFieldSize = Dimension(16 * 20, 32)
        val buttonSize = Dimension(96, 32)

        val fldText1 = JPasswordField()
        fldText1.preferredSize = textFieldSize
        frame.add(fldText1)

        val fldText2 = JPasswordField()
        fldText2.preferredSize = textFieldSize
        frame.add(fldText2)

        val btnClear = JButton("Clear")
        btnClear.preferredSize = buttonSize
        btnClear.addActionListener {
            fldText1.text = ""
            fldText2.text = ""
        }
        frame.add(btnClear)

        val btnOk = JButton("Ok")
        btnOk.preferredSize = buttonSize
        btnOk.addActionListener {
            if (arrayOf(fldText1, fldText2).isOk()) {
                complete(fldText1.getValue())
            }
        }
        frame.add(btnOk)

        val btnCancel = JButton("Cancel")
        btnCancel.preferredSize = buttonSize
        btnCancel.addActionListener { complete("") }
        frame.add(btnCancel)

        layout.putConstraint(SpringLayout.NORTH, btnClear, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, btnClear, 16, SpringLayout.WEST, content)

        layout.putConstraint(SpringLayout.NORTH, btnOk, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, btnOk, 16, SpringLayout.EAST, btnClear)

        layout.putConstraint(SpringLayout.NORTH, btnCancel, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, btnCancel, 16, SpringLayout.EAST, btnOk)

        layout.putConstraint(SpringLayout.NORTH, fldText1, 16, SpringLayout.SOUTH, btnClear)
        layout.putConstraint(SpringLayout.WEST, fldText1, 16, SpringLayout.WEST, content)

        layout.putConstraint(SpringLayout.NORTH, fldText2, 16, SpringLayout.SOUTH, fldText1)
        layout.putConstraint(SpringLayout.WEST, fldText2, 16, SpringLayout.WEST, content)

        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                complete("")
            }
        })

        SwingUtilities.invokeLater { frame.isVisible = true }

        return done;
    }

}