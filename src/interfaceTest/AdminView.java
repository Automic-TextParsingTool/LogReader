
 /**
 * @file AdminView.java
 * @authors Leah Talkov, Jerry Tsui
 * @date 8/3/2016
 * Shows the frame for an Admin User. Consists of a JTable that displays
 * the information from the database table, and users can delete, add, or modify
 * entries of the data. The modifications are controlled by the functions 
 * in DataController.  
 */

package interfaceTest;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.*;

import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import java.awt.GridBagConstraints;
import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollBar;

@SuppressWarnings({ "serial", "unused" })
public class AdminView extends JFrame {
	
	/**Reflects the keywords that are currently in the database*/
	protected List<String> keyWords = new ArrayList<String>();
	/**Contains the keywords in the database, and serves as a temp list
	for modifications done to the table. Its contents are placed into
	keywords when the user presses save to default.*/
	List <String> savedWords = new ArrayList<String>();
	/**A list that contains the default values used to load up the JTable*/
	List <String[]> defaultList = new ArrayList<String[]>();
	/**A list that contains the original values, and is serves as a temp list
	for modifications done to the table. */
	List <String[]> list = new ArrayList<String[]>();
	/**Headers for the error JTable */
	private final String[] errorTableColumnHeaders = {"Folder", "Keyword", "Log Error Description",
									"Suggested Solution"};
	/**Headers for the hyperlink JTable*/
	private final String[] hyperlinkColumnHeaders = {"Keyword", "Hyperlink"};
	/**JTable that displays the database contents and changes accordingly for user changes*/
	private JTable tblErrorEntries;
	/**Jtable that displays the corresponding solution hyperlink for each keyword*/
	private JTable tblHyperlinkEntries;
	/**Main panel for the groups tab*/
	protected JPanel pnlGroups;
	/**Contains a list of the current groups*/
	protected JList<String> groupList;
	/**A model for the list of groups which is refreshed when groups are added or deleted*/
	protected DefaultListModel<String> defaultListModel;
	/**A selection of options for when the user wants to delete an entry*/
	Object[] options = {"Yes", "Cancel"};
	/**A model for the error table which is refreshed when the user adds, deletes, or mods an entry*/
	private DefaultTableModel errorTableModel;
	/**A model for the hyperlink table which is refrehsed when the user adds, deletes, or mods an entry*/
	private DefaultTableModel hyperlinkTableModel;
	/**A private dataController that modifies the table content*/
	DataController dc;
	
	public AdminView(UserView view) throws ClassNotFoundException, SQLException {
			
		try {
		     ClassLoader cl = this.getClass().getClassLoader();
		     ImageIcon programIcon = new ImageIcon(cl.getResource("res/logo.png"));
		     setIconImage(programIcon.getImage());
		  } catch (Exception e) {
		     System.out.println("Could not load program icon.");
		  }
		
		//We create the datatable with the database info, and set the lists within DataController
		createDataTable();
		dc = new DataController(this);
		dc.setList(list);
		dc.setDefaultList(defaultList);
		dc.transferData("CHANGE");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Admin Features");
		setBounds(200, 200, 1000, 300);
		setMinimumSize(new Dimension(750, 300));
		setLocationRelativeTo(null);
		
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		add(pnlMain);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		pnlMain.add(tabbedPane);
		
		JPanel pnlTabOne = new JPanel();
		pnlTabOne.setLayout(new BoxLayout(pnlTabOne, BoxLayout.Y_AXIS));
		
		JScrollPane scrollPane = new JScrollPane(tblErrorEntries, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnlTabOne.add(scrollPane);
		
		initTable();
		
		scrollPane.setViewportView(tblErrorEntries);
		
		pnlTabOne.add(Box.createRigidArea(new Dimension(0, 5)));
		
		JPanel pnlTabOneButtons = new JPanel();
		pnlTabOne.add(pnlTabOneButtons);
		pnlTabOneButtons.setLayout(new FlowLayout());
		
		JButton btnAddEntry = new JButton("Add Entry");
		btnAddEntry.setToolTipText("Changes will be made locally");
		btnAddEntry.addActionListener(e -> {
			AddDialog add = new AddDialog(dc);
			add.setVisible(true);
		});
		pnlTabOneButtons.add(btnAddEntry);
		
		JButton btnModifyEntry = new JButton("Modify Entry");
		btnModifyEntry.setToolTipText("Changes will be made locally");
		btnModifyEntry.setHorizontalAlignment(SwingConstants.RIGHT);
		btnModifyEntry.addActionListener(e -> {
			//If the admin wants to modify a value we send DataController the current
			//cell contents of the entry they chose
			if(tblErrorEntries.getSelectedRow() != -1){
				int rowSelect = tblErrorEntries.getSelectedRow();
				ModifyDialog modify = new ModifyDialog((String)tblErrorEntries.getValueAt(rowSelect, 0),
													 (String)tblErrorEntries.getValueAt(rowSelect, 1),
													 (String)tblErrorEntries.getValueAt(rowSelect, 2),
													 (String)tblErrorEntries.getValueAt(rowSelect, 3),
													 dc, rowSelect);
				modify.setVisible(true);
			}
			else JOptionPane.showMessageDialog(null, "Please select an entry");	
		});
		pnlTabOneButtons.add(btnModifyEntry);
		
		JButton btnDeleteEntry = new JButton("Delete Entry");
		btnDeleteEntry.addActionListener(e -> {
			int viewIndex = tblErrorEntries.getSelectedRow();
			if(viewIndex != -1) {
				//Ensures user wants to delete selected entry
				int confirmation = JOptionPane.showOptionDialog(this,
				    "This will delete the entire entry. Are you sure you want to continue?",
				    "",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				    null, options, options[1]);
				//If yes, then we continue delete process
				if (confirmation == JOptionPane.YES_OPTION){
					int modelIndex = tblErrorEntries.convertRowIndexToModel(viewIndex); 
					try {
						dc.deleteData(viewIndex);
					} catch (Exception e1) {
					e1.printStackTrace();
					}
				}
			} 
		});
		pnlTabOneButtons.add(btnDeleteEntry);
		
		JButton btnSaveToDatabase = new JButton("Save to Database");
		btnSaveToDatabase.setToolTipText("Local changes will be saved");
		btnSaveToDatabase.addActionListener(e -> {
			/*
			If the user wants to save to the database, then we call the function
			from DataController which will send all the queries to the database
			to reflect the changes done. The database will then have the same
			contents as the table the admin sees. 
			*/
			try {
				dc.saveDefault();
				String driver = "net.sourceforge.jtds.jdbc.Driver";
				Class.forName(driver);
				Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://vwaswp02:1433/coeus", "coeus", "C0eus");
				Statement stmt = conn.createStatement();

				view.fillKeywords(stmt);
				view.createErrorDictionary(stmt);
				view.updateTreeView();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		btnSaveToDatabase.setAlignmentX(Component.RIGHT_ALIGNMENT);
		pnlTabOneButtons.add(btnSaveToDatabase);
		
		JButton btnRevertLocalChanges = new JButton("Revert local changes");
		btnRevertLocalChanges.setAlignmentX(Component.RIGHT_ALIGNMENT);
		pnlTabOneButtons.add(btnRevertLocalChanges);
		btnRevertLocalChanges.addActionListener(e -> {
			//The table will revert back to the contents of the database, erasing
			//all changes the admin has done
			dc.getList().clear();
			for(int i = 0; i < dc.getDefaultList().size(); i++)
			{
				dc.getList().add(dc.getDefaultList().get(i));
			}
			dc.transferData("DEFAULT");
			resetData();
		});
		
		JPanel pnlTabTwo = new JPanel();
		pnlTabTwo.setLayout(new BoxLayout(pnlTabTwo, BoxLayout.Y_AXIS));
		pnlTabTwo.add(Box.createVerticalGlue());
		
		pnlGroups = createGroupDisplay (view);
		pnlGroups.setBorder(new EmptyBorder(10,5,0,5));
		pnlTabTwo.add(pnlGroups);
		
		JPanel pnlTabTwoButtons = new JPanel();
		pnlTabTwoButtons.setLayout(new FlowLayout());
		pnlTabTwo.add(pnlTabTwoButtons);
		
		JButton btnCreateGroup = new JButton("Create Group");
		btnCreateGroup.setPreferredSize(new Dimension(125, 20));
		btnCreateGroup.setAlignmentX(CENTER_ALIGNMENT);
		btnCreateGroup.addActionListener(e -> {
			//Allows the admin to create groups of keywords and 
			//creates a CreateGroup object
			CreateGroup groupView = new CreateGroup(this, view);
		});
		pnlTabTwoButtons.add(btnCreateGroup);
		
		JButton btnDeleteGroup = new JButton("Delete Group");
		btnDeleteGroup.setPreferredSize(new Dimension(125, 20));
		btnDeleteGroup.setAlignmentX(CENTER_ALIGNMENT);
		btnDeleteGroup.addActionListener(e -> {
			//The admin can delete an existing group
			try {
				removeGroup(view);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		pnlTabTwoButtons.add(btnDeleteGroup);
		pnlTabTwo.add(Box.createVerticalGlue());
		
		JPanel pnlTabThree = new JPanel();
		pnlTabThree.setLayout(new BoxLayout(pnlTabThree, BoxLayout.Y_AXIS));
		pnlTabThree.add(Box.createVerticalGlue());
		
		JScrollPane pnlHyperlinkTable = new JScrollPane();
		pnlTabThree.add(pnlHyperlinkTable);
		
		
		pnlTabThree.add(Box.createVerticalGlue());
		
		tabbedPane.add("Edit Entries", pnlTabOne);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.add("Create Groups", pnlTabTwo);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		tabbedPane.add("Edit Solution Hyperlinks", pnlTabThree);
		tabbedPane.setMnemonicAt(2,  KeyEvent.VK_3);
	}
	
	/**
	 * Fills myData with arrays. Each array represents a entry 
	 * from the database where each entry is loaded into an array index. 
	 * The resulting array is used as a JTable parameter. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	void createDataTable() throws SQLException, ClassNotFoundException {		
		String driver = "net.sourceforge.jtds.jdbc.Driver";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://vwaswp02:1433/coeus", "coeus", "C0eus");

		Statement stmt = conn.createStatement();
		String query2 = "select Keyword, Log_Error_Description, "+
						"Suggested_Solution, Folder from logerrors";
		ResultSet rs = stmt.executeQuery(query2);
		while(rs.next())
		{
			String[] entry = new String[4];
			entry[0] = rs.getString("Folder");
			entry[1] = rs.getString("Keyword");
			entry[2] = rs.getString("Log_Error_Description");
			entry[3] = rs.getString("Suggested_Solution");
			keyWords.add(rs.getString("Keyword"));
			savedWords.add(rs.getString("Keyword"));
			list.add(entry);
			defaultList.add(entry);
		}
		stmt.close();
	}
	
	/**
	 * Creates a JPanel that shows the groups that are currently
	 * in the database. The panel is updated as groups are 
	 * added and deleted. 
	 * @param view The current JFrame for AdminView
	 * @return The Panel that displays the groups
	 */
	JPanel createGroupDisplay (UserView view){
		JPanel pnlGroup = new JPanel();
		pnlGroup.setLayout(new BoxLayout(pnlGroup, BoxLayout.PAGE_AXIS));
		pnlGroup.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	    pnlGroup.add(Box.createHorizontalGlue());

	    defaultListModel = new DefaultListModel<String>();
	    updateGroups(view);
	    groupList = new JList<String>(defaultListModel);
	    JScrollPane scrollPane = new JScrollPane(groupList);
	    pnlGroup.add(scrollPane);
	    return pnlGroup;
	}
	
	/**
	 * Updates the defaultListModel depending on the changes
	 * the Admin has done (if they have created or deleted a group)
	 * @param view The current JFrame for AdminView
	 */
	protected void updateGroups(UserView view){
		defaultListModel.clear();
		for (Map.Entry<String, String> entry : view.GroupInfo.entrySet()){
			String keyWords = entry.getValue();
	    	defaultListModel.addElement(keyWords);
		}
	}
	
	/**
	 * Removes a group from the database. Brings
	 * up a JDialog if the admin presses the delete group button, but has
	 * not selected a group to delete. 
	 * @param view The current JFrame used for AdminView
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void removeGroup(UserView view) throws ClassNotFoundException, SQLException{
		String groupToRemove = groupList.getSelectedValue();
		if (groupToRemove == null){
			JOptionPane.showMessageDialog(this, "No group selected");
			return;
		}
		String driver = "net.sourceforge.jtds.jdbc.Driver";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://vwaswp02:1433/coeus", "coeus", "C0eus");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate("delete from Groups where GroupKeywords = \'" + groupToRemove + "\'");
		view.loadGroupInfo(stmt);
		stmt.close();

		System.out.println(groupToRemove);
		StringBuilder query = new StringBuilder();
		
		updateGroups(view);
		view.createGroupView();
	}
	
	/**
	 * Updates the default table model, and is called by functions 
	 * in the DataController. This happens when a new entry is made, edited,
	 * or deleted, and the table properly reflects the changes made by the user.
	 */
	protected void resetData(){
		DefaultTableModel model = new DefaultTableModel(dc.getData(), errorTableColumnHeaders); 
		tblErrorEntries.setModel(model);
		resizeColumnWidth(tblErrorEntries);
		model.fireTableDataChanged();
	}
	
	
	/**
	 * Initializes the table of errors, suggested solutions, folders, etc
	 * that can be modified by the Administrator
	 */
	private void initTable(){
		errorTableModel = new DefaultTableModel(dc.getData(), errorTableColumnHeaders) {
		    @Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
		       return false;
		    }     
		};
		tblErrorEntries = new JTable(errorTableModel){
			//Renders each columnn to fit the data
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				int rendererWidth = component.getPreferredSize().width;
	            TableColumn tableColumn = getColumnModel().getColumn(column);
	            tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
	            return component;
			}

		};
		resizeColumnWidth(tblErrorEntries);	
		tblErrorEntries.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		
	}
	
	/**
	 * Resizes the columns of a Jtable to fit the contents of the entries given.
	 * @param table The JTable that is being displayed to the admin
	 */
	public void resizeColumnWidth(JTable table) {
	    final TableColumnModel columnModel = table.getColumnModel();
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        int width = 50; // Min width
	        for (int row = 0; row < table.getRowCount(); row++) {
	            TableCellRenderer renderer = table.getCellRenderer(row, column);
	            Component comp = table.prepareRenderer(renderer, row, column);
	            width = Math.max(comp.getPreferredSize().width +1 , width);
	        }
	        columnModel.getColumn(column).setPreferredWidth(width);
	    }
	}
	
	
}
