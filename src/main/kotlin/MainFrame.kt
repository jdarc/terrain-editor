import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.BoxLayout
import javax.swing.border.EmptyBorder

class MainFrame : JFrame("Terrain Editor") {

    private val editorPanel = EditorPanel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(800, 600)

        val buildToolBar = buildToolBar()
        val editorContainerPanel = buildEditorPanel(editorPanel)
        val buildStatusPanel = buildStatusPanel()

        layout = BorderLayout()
        contentPane.add(buildToolBar, BorderLayout.PAGE_START)
        contentPane.add(editorContainerPanel, BorderLayout.CENTER)
        contentPane.add(buildStatusPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(null)
    }

    private fun buildEditorPanel(editorPanel: EditorPanel): JPanel {
        val panel = JPanel()
        panel.background = background.darker()
        panel.border = EmptyBorder(8, 8, 8, 8)
        panel.layout = BorderLayout()
        panel.add(editorPanel, BorderLayout.CENTER)
        return panel
    }

    private fun buildStatusPanel(): JComponent {
        val statusPanel = JPanel()
        statusPanel.preferredSize = Dimension(width, 16)
        statusPanel.border = EmptyBorder(4, 4, 4, 4)
        return statusPanel
    }

    private fun buildToolBar(): JComponent {
        val toolbar = JPanel()
        toolbar.border = EmptyBorder(2, 2, 2, 2)
        toolbar.layout = BoxLayout(toolbar, BoxLayout.X_AXIS)

        val generateButton = JButton("Generate")
        generateButton.addActionListener { editorPanel.generate() }

        val smoothButton = JButton("Smooth")
        smoothButton.addActionListener { editorPanel.smooth() }

        val canyoniseButton = JButton("Canyonise")
        canyoniseButton.addActionListener { editorPanel.canyonise() }

        val erodeModeButton = JToggleButton("Erode")
        erodeModeButton.addActionListener { editorPanel.mode = EditMode.ERODE }

        val lakeModeButton = JToggleButton("Lake")
        lakeModeButton.addActionListener { editorPanel.mode = EditMode.LAKE }

        val buttonGroup = ButtonGroup()
        buttonGroup.add(erodeModeButton)
        buttonGroup.add(lakeModeButton)
        erodeModeButton.isSelected = true

        toolbar.add(Box.createRigidArea(Dimension(2, 0)))
        toolbar.add(generateButton)
        toolbar.add(Box.createHorizontalGlue())
        toolbar.add(smoothButton)
        toolbar.add(Box.createRigidArea(Dimension(2, 0)))
        toolbar.add(canyoniseButton)
        toolbar.add(Box.createHorizontalGlue())
        toolbar.add(erodeModeButton)
        toolbar.add(Box.createRigidArea(Dimension(2, 0)))
        toolbar.add(lakeModeButton)
        toolbar.add(Box.createRigidArea(Dimension(2, 0)))
        return toolbar
    }
}
