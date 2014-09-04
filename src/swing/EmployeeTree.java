package swing;

/**
 * Data de Cria��o: 21/03/2014
 *
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

class Employee {
	public String firstName;

	public String lastName;

	public float salary;

	public Employee(String f, String l, float s) {
		this.firstName = f;
		this.lastName = l;
		this.salary = s;
	}

}

class EmployeeCellRenderer implements TreeCellRenderer {
	JLabel firstNameLabel = new JLabel(" ");

	JLabel lastNameLabel = new JLabel(" ");

	JLabel salaryLabel = new JLabel(" ");

	JPanel renderer = new JPanel();

	DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	Color backgroundSelectionColor;

	Color backgroundNonSelectionColor;

	public EmployeeCellRenderer() {
		this.firstNameLabel.setForeground(Color.BLUE);
		this.renderer.add(this.firstNameLabel);

		this.lastNameLabel.setForeground(Color.BLUE);
		this.renderer.add(this.lastNameLabel);

		this.salaryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.salaryLabel.setForeground(Color.RED);
		this.renderer.add(this.salaryLabel);
		this.renderer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.backgroundSelectionColor = this.defaultRenderer.getBackgroundSelectionColor();
		this.backgroundNonSelectionColor = this.defaultRenderer.getBackgroundNonSelectionColor();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Component returnValue = null;
		if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject instanceof Employee) {
				Employee e = (Employee) userObject;
				this.firstNameLabel.setText(e.firstName);
				this.lastNameLabel.setText(e.lastName);
				this.salaryLabel.setText("" + e.salary);
				if (selected) {
					this.renderer.setBackground(this.backgroundSelectionColor);
				} else {
					this.renderer.setBackground(this.backgroundNonSelectionColor);
				}
				this.renderer.setEnabled(tree.isEnabled());
				returnValue = this.renderer;
			}
		}
		if (returnValue == null) {
			returnValue = this.defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
					hasFocus);
		}
		return returnValue;
	}
}

public class EmployeeTree {

	public static void main(String args[]) {
		JFrame frame = new JFrame("Book Tree");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Employee javaBooks[] = { new Employee("A", "F", 9.99f), new Employee("B", "E", 4.99f),
				new Employee("C", "D", 9.95f) };

		Employee netBooks[] = { new Employee("AA", "CC", 9.99f), new Employee("BB", "DD", 9.99f) };

		Vector<Employee> javaVector = new TreeNodeVector<Employee>("A", javaBooks);
		Vector<Employee> netVector = new TreeNodeVector<Employee>("As", netBooks);

		Object rootNodes[] = { javaVector, netVector };

		Vector<Object> rootVector = new TreeNodeVector<Object>("Root", rootNodes);

		JTree tree = new JTree(rootVector);
		TreeCellRenderer renderer = new EmployeeCellRenderer();
		tree.setCellRenderer(renderer);
		JScrollPane scrollPane = new JScrollPane(tree);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.setSize(300, 300);
		frame.setVisible(true);
	}
}

class TreeNodeVector<E> extends Vector<E> {
	String name;

	TreeNodeVector(String name) {
		this.name = name;
	}

	TreeNodeVector(String name, E elements[]) {
		this.name = name;
		for (int i = 0, n = elements.length; i < n; i++) {
			this.add(elements[i]);
		}
	}

	@Override
	public String toString() {
		return "[" + this.name + "]";
	}
}