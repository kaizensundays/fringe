package com.kaizensundays.fusion.fringe

import java.awt.Color
import java.awt.Dimension
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

    fun build(): CompletableFuture<String> {

        val done = CompletableFuture<String>()

        val frame = JFrame()

        frame.title = "Observer"
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 200)
        frame.setLocationRelativeTo(null)

        val content = frame.contentPane
        content.background = Color(231, 231, 231)

        val layout = SpringLayout()
        content.layout = layout

        val fldText = JTextField()
        fldText.preferredSize = Dimension(16 * 20, 32)
        frame.add(fldText)

        val button1 = JButton("Clear")
        button1.preferredSize = Dimension(96, 32)
        button1.addActionListener { fldText.text = "" }
        frame.add(button1)

        val button2 = JButton("Ok")
        button2.preferredSize = Dimension(96, 32)
        button2.addActionListener { done.complete(fldText.text.trim()) }
        frame.add(button2)

        val button3 = JButton("Cancel")
        button3.preferredSize = Dimension(96, 32)
        button3.addActionListener { done.complete("") }
        frame.add(button3)

        val scrollPane = JScrollPane(/*table*/)
        content.add(scrollPane)

        layout.putConstraint(SpringLayout.NORTH, button1, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, button1, 16, SpringLayout.WEST, content)

        layout.putConstraint(SpringLayout.NORTH, button2, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, button2, 16, SpringLayout.EAST, button1)

        layout.putConstraint(SpringLayout.NORTH, button3, 16, SpringLayout.NORTH, content)
        layout.putConstraint(SpringLayout.WEST, button3, 16, SpringLayout.EAST, button2)

        layout.putConstraint(SpringLayout.NORTH, fldText, 16, SpringLayout.SOUTH, button1)
        layout.putConstraint(SpringLayout.WEST, fldText, 16, SpringLayout.WEST, content)

        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                done.complete("Done")
            }
        })

        SwingUtilities.invokeLater { frame.isVisible = true }

        return done;
    }

}