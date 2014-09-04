package swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DynamicTreeDemo extends JPanel implements ActionListener {
	class DynamicTree extends JPanel {
		class MyTreeModelListener implements TreeModelListener {
			public void treeNodesChanged(TreeModelEvent e) {
				DefaultMutableTreeNode node;
				node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

				/*
				 * If the event lists children, then the changed node is the
				 * child of the node we've already gotten. Otherwise, the
				 * changed node and the specified node are the same.
				 */

				int index = e.getChildIndices()[0];
				node = (DefaultMutableTreeNode) (node.getChildAt(index));

				System.out.println("The user has finished editing the node.");
				System.out.println("New value: " + node.getUserObject());
			}

			public void treeNodesInserted(TreeModelEvent e) {
			}

			public void treeNodesRemoved(TreeModelEvent e) {
			}

			public void treeStructureChanged(TreeModelEvent e) {
			}
		}

		protected DefaultMutableTreeNode rootNode;
		protected DefaultTreeModel treeModel;
		protected JTree tree;

		private Toolkit toolkit = Toolkit.getDefaultToolkit();

		public DynamicTree() {
			super(new GridLayout(1, 0));

			this.rootNode = new DefaultMutableTreeNode("Root Node");
			this.treeModel = new DefaultTreeModel(this.rootNode);

			this.tree = new JTree(this.treeModel);
			this.tree.setEditable(true);
			this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.tree.setShowsRootHandles(true);

			JScrollPane scrollPane = new JScrollPane(this.tree);
			this.add(scrollPane);
		}

		public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
			return this.addObject(parent, child, false);
		}

		public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

			if (parent == null) {
				parent = this.rootNode;
			}

			// It is key to invoke this on the TreeModel, and NOT
			// DefaultMutableTreeNode
			this.treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

			// Make sure the user can see the lovely new node.
			if (shouldBeVisible) {
				this.tree.scrollPathToVisible(new TreePath(childNode.getPath()));
			}
			return childNode;
		}

		/** Add child to the currently selected node. */
		public DefaultMutableTreeNode addObject(Object child) {
			DefaultMutableTreeNode parentNode = null;
			TreePath parentPath = this.tree.getSelectionPath();

			if (parentPath == null) {
				parentNode = this.rootNode;
			} else {
				parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
			}

			return this.addObject(parentNode, child, true);
		}

		/** Remove all nodes except the root node. */
		public void clear() {
			this.rootNode.removeAllChildren();
			this.treeModel.reload();
		}

		/** Remove the currently selected node. */
		public void removeCurrentNode() {
			TreePath currentSelection = this.tree.getSelectionPath();
			if (currentSelection != null) {
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
				MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
				if (parent != null) {
					this.treeModel.removeNodeFromParent(currentNode);
					return;
				}
			}

			// Either there was no selection, or the root was selected.
			this.toolkit.beep();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int newNodeSuffix = 1;
	private static String ADD_COMMAND = "add";
	private static String REMOVE_COMMAND = "remove";

	private static String CLEAR_COMMAND = "clear";

	private DynamicTree treePanel;

	public DynamicTreeDemo() {
		super(new BorderLayout());

		// Create the components.
		this.treePanel = new DynamicTree();
		this.populateTree(this.treePanel);

		JButton addButton = new JButton("Add");
		addButton.setActionCommand(DynamicTreeDemo.ADD_COMMAND);
		addButton.addActionListener(this);

		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(DynamicTreeDemo.REMOVE_COMMAND);
		removeButton.addActionListener(this);

		JButton clearButton = new JButton("Clear");
		clearButton.setActionCommand(DynamicTreeDemo.CLEAR_COMMAND);
		clearButton.addActionListener(this);

		// Lay everything out.
		this.treePanel.setPreferredSize(new Dimension(300, 150));
		this.add(this.treePanel, BorderLayout.CENTER);

		JPanel panel = new JPanel(new GridLayout(0, 3));
		panel.add(addButton);
		panel.add(removeButton);
		panel.add(clearButton);
		this.add(panel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (DynamicTreeDemo.ADD_COMMAND.equals(command)) {
			// Add button clicked
			this.treePanel.addObject("New Node " + this.newNodeSuffix++);
		} else if (DynamicTreeDemo.REMOVE_COMMAND.equals(command)) {
			// Remove button clicked
			this.treePanel.removeCurrentNode();
		} else if (DynamicTreeDemo.CLEAR_COMMAND.equals(command)) {
			// Clear button clicked.
			this.treePanel.clear();
		}
	}

	public void populateTree(DynamicTree treePanel) {
		String p1Name = new String("Parent 1");
		String p2Name = new String("Parent 2");
		String c1Name = new String("Child 1");
		String c2Name = new String("Child 2");

		DefaultMutableTreeNode p1, p2;

		p1 = treePanel.addObject(null, p1Name);
		p2 = treePanel.addObject(null, p2Name);

		treePanel.addObject(p1, c1Name);
		treePanel.addObject(p1, c2Name);

		treePanel.addObject(p2, c1Name);
		treePanel.addObject(p2, c2Name);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("DynamicTreeDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		DynamicTreeDemo newContentPane = new DynamicTreeDemo();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DynamicTreeDemo.createAndShowGUI();
			}
		});
	}
}