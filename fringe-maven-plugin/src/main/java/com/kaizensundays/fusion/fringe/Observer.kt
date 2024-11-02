package com.kaizensundays.fusion.fringe

import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.CompletableFuture
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SpringLayout
import javax.swing.SwingUtilities

/**
 * Created: Thursday 10/31/2024, 7:22 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class Observer {

    private fun Array<JTextField>.isOk(): Boolean {
        this.forEach { fld -> fld.text = fld.text.trim() }
        if (this.first().text == this.last().text && this.first().text.isNotBlank()) {
            return true
        }
        Toolkit.getDefaultToolkit().beep()
        return false
    }

    fun build(): CompletableFuture<String> {

        val done = CompletableFuture<String>()

        val frame = JFrame()

        fun complete(value: String) {
            done.complete(value)
            SwingUtilities.invokeLater { frame.dispose() }
        }

        frame.title = "Observer"
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.setLocationRelativeTo(null)

        val content = frame.contentPane
        content.background = Color(231, 231, 231)

        val layout = SpringLayout()
        content.layout = layout

        val textFieldSize = Dimension(16 * 20, 32)

        val fldText1 = JTextField()
        fldText1.preferredSize = textFieldSize
        frame.add(fldText1)

        val fldText2 = JTextField()
        fldText2.preferredSize = textFieldSize
        frame.add(fldText2)

        val button1 = JButton("Clear")
        button1.preferredSize = Dimension(96, 32)
        button1.addActionListener {
            fldText1.text = ""
            fldText2.text = ""
        }
        frame.add(button1)

        val button2 = JButton("Ok")
        button2.preferredSize = Dimension(96, 32)
        button2.addActionListener {
            if (arrayOf(fldText1, fldText2).isOk()) {
                complete(fldText1.text)
            }
        }
        frame.add(button2)

        val button3 = JButton("Cancel")
        button3.preferredSize = Dimension(96, 32)
        button3.addActionListener { complete("") }
        frame.add(button3)

        val scrollPane = JScrollPane()
        content.add(scrollPane)

        layout.putConstraint(SpringLayout.NORTH, button1, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, button1, 16, SpringLayout.WEST, content)

        layout.putConstraint(SpringLayout.NORTH, button2, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, button2, 16, SpringLayout.EAST, button1)

        layout.putConstraint(SpringLayout.NORTH, button3, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, button3, 16, SpringLayout.EAST, button2)

        layout.putConstraint(SpringLayout.NORTH, fldText1, 16, SpringLayout.SOUTH, button1)
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