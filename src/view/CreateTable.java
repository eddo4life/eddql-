package view;

import controller.library.EddoLibrary;
import controller.tables.CreateTableManager;
import dao.DBMS;
import dao.mysql.MySQLConnection;
import dao.mysql.MySQLDaoOperation;
import dao.oracle.OracleDaoOperation;
import eddql.launch.LoadData;
import model.CreateTableModel;
import model.ShowTablesModel;
import view.iconmaker._Icon;
import view.pupupsmessage.PupupMessages;
import view.resize.Resize;
import view.tables.Custom;
import view.tables.JTableUtilities;
import view.tables.Sort;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class CreateTable implements MouseListener, KeyListener {
    public String database;
    private String name;
    private JComboBox<String> rcBox;
    private JButton okButton, exitButton, addButton;// ,searchNextButton;
    private JPanel mainPanel;
    private JLabel tableName;
    private JPanel panel;
    static JPanel pane;
    JLabel cname;
    String query = "";
    JTextField limitTextField;
    ArrayList<String> listNames = new ArrayList<>();
    JTextField tableNameField, columnNameField;
    ArrayList<String> dataTable = new ArrayList<>();
    ArrayList<String> constArrayList = new ArrayList<String>();
    ArrayList<CreateTableModel> dataLine = new ArrayList<>();
    CreateTableModel ctm = new CreateTableModel();

    public CreateTable() {
    }

    /*
     *
     * ========================================================
     *
     */

    public void initialize() {
        constArrayList.clear();

        panel = new JPanel();
        panel.setLayout(new FlowLayout());

        if (Home.content != null) {
            Home.content.removeAll();
        }

        showTables();
    }

    /*
     *
     * ========================================================
     *
     */

    public String getDatabase() {
        return database;
    }

    /*
     *
     * ========================================================
     *
     */

    public void setDatabase(String database) {
        this.database = database;
    }

    /*
     *
     * ========================================================
     *
     */

    public static ArrayList<String> arr = null;
    String[] tabs;
    JTable table;

    public void showTables() {
        try {
            ArrayList<String> showtables;
            if (DBMS.dbms == 1) {
                showtables = new MySQLDaoOperation().showTables();
            } else {
                showtables = new OracleDaoOperation().showTables();
            }
            arr = showtables;
            String title = "(" + showtables.size() + ") tables from " + new MySQLConnection().getDbName();
            if (showtables.size() == 1) {
                title = "(1) table from " + new MySQLConnection().getDbName();
            }
            if (showtables.isEmpty()) {
                title = "";
            }
            pane = new JPanel();
            pane.setLayout(new GridBagLayout());

            table = new JTable();

            table.setEnabled(false);
            table.addMouseListener(this);

            JPanel intern = new JPanel();
            intern.setLayout(new BorderLayout());
            Object[] header = {"#", "Names", "Columns count", "Rows count", "Date", "Time"};
            int i = 0, j = showtables.size(), k = 1;
            tabs = new String[j];
            Home.frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Object[][] obj = new Object[j][6];
            for (ShowTablesModel data : LoadData.tables) {
                obj[i][0] = k;
                obj[i][1] = data.getNames();
                tabs[i] = data.getNames();
                obj[i][2] = data.getColumnCount();
                obj[i][3] = data.getRowCount();
                obj[i][4] = data.getDate();
                obj[i][5] = data.getTime();
                listNames.add(data.getNames().toUpperCase());
                i++;
                k++;
            }
            @SuppressWarnings("rawtypes")
            Class[] columnClass = new Class[]{Integer.class, String.class, Integer.class, Integer.class, String.class,
                    String.class};
            DefaultTableModel defaultTableModel = new DefaultTableModel(obj, header) {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                public Class<?> getColumnClass(int columnIndex) {
                    return columnClass[columnIndex];
                }
            };
            table.setModel(defaultTableModel);
            JTableHeader hea = table.getTableHeader();
            hea.setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());

            JTableUtilities.setCellsAlignment(table, SwingConstants.CENTER, 0);
            Custom tabCustom = new Custom(table, false, false, 30, null, null);
            intern.add(tabCustom.getScrollPane());
            JPanel tableNorth = new JPanel();
            tableNorth.setLayout(new BorderLayout());
            JPanel titlePanel = new JPanel();
            JLabel titleLabel = new JLabel(title);
            titlePanel.add(titleLabel);
            tableNorth.add(titlePanel, BorderLayout.WEST);
            pane.setLayout(new BorderLayout());
            pane.add(tableNorth, BorderLayout.NORTH);
            pane.add(intern, BorderLayout.CENTER);
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(pane, BorderLayout.CENTER);
            panelBuilder = new JPanel();
            panelBuilder.setLayout(new BorderLayout());
            panelBuilder.setVisible(false);
            Object[] ob = tableBuilder();
            panelBuilder.add((JScrollPane) ob[0]);
            int length = (int) ob[1];
            if (length < 100)
                panelBuilder.setPreferredSize(new Dimension(0, length));
            else
                panelBuilder.setPreferredSize(new Dimension(0, 100));

            mainPanel.add(panelBuilder, BorderLayout.NORTH);
            Home.content.add(mainPanel, BorderLayout.CENTER);
            Home.content.revalidate();
            Home.content.repaint();
            Home.frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            new Sort().tableSortFilter(table);
        } catch (SQLException e) {
            if (tableName == null) {
                Insertion.tableName.setText("Exception ...");
                Insertion.tableName.setForeground(Color.red);
                Insertion.tableNameField.setBorder(null);
                Insertion.tableNameField.setText("	" + e.getMessage());
                Insertion.tableNameField.setFocusable(false);

                warning();
            } else {
                tableName.setText("Exception ...");
                tableName.setForeground(Color.red);
                tableNameField.setBorder(null);
                tableNameField.setText("	" + e.getMessage());
                tableNameField.setFocusable(false);
                tableNameField.setEditable(false);
                // searchNextButton.setVisible(false);
                warning();
            }
        } catch (Exception e) {
            new LoadData().tablesSectionLoader();
            showTables();
        }
    }

    private JPanel panelBuilder;
    /*
     *
     * ========================================================
     *
     */

    private int i;

    public Object[] tableBuilder() {
        JTable table = new JTable();
        table.setRowHeight(20);
        Object[][] obj = new Object[dataLine.size()][7];
        i = 0;
        for (CreateTableModel d : dataLine) {
            obj[i][0] = d.getName();
            obj[i][1] = d.getDatatype();
            if (hasLimit(d.getDatatype()))
                obj[i][2] = d.getLimit();
            else {
                obj[i][2] = null;
            }
            if (d.getConstraint().toLowerCase().contains("null"))
                obj[i][3] = "No";
            else
                obj[i][3] = "Yes";
            if (d.getConstraint().toLowerCase().contains("primary"))
                obj[i][4] = "primary";
            if (d.getConstraint().toLowerCase().contains("foreign"))
                obj[i][4] = "foreign";
            obj[i][5] = "Null";
            obj[i][6] = "";
            i++;
        }

        if (table.isEnabled()) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (i > 0) {
                        String id = String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
                        String[] reps = {"Delete", "Update", "Cancel"};

                        int rep = JOptionPane.showOptionDialog(null, "do you want to delete or update?",
                                "delete or update", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                new _Icon().questionIcon(), reps, 0);
                        if (rep == 0) {
                            for (CreateTableModel cm : dataLine) {
                                if (cm.getName().equals(id)) {
                                    dataLine.remove(cm);
                                    break;
                                }
                            }

                        } else if (rep == 1) {
                            columnNameField.setText(id);
                            _data.setSelectedItem(String.valueOf(table.getValueAt(table.getSelectedRow(), 1)));
                            String limit = String.valueOf(table.getValueAt(table.getSelectedRow(), 2));
                            limitTextField.setText(limit);
                            if (limitTextField.getText().equals("null")) {
                                limitTextField.setVisible(false);
                                limitLabel.setVisible(false);
                                limitTextField.setText("2");
                            } else {
                                limitTextField.setVisible(true);
                                limitLabel.setVisible(true);
                            }
                            for (CreateTableModel cm : dataLine) {
                                if (cm.getName().equals(id)) {
                                    boolean exist = false;
                                    String[] constraints = {};
                                    if (cm.getConstraintAff().contains("Not null")) {
                                        cm.setConstraintAff(cm.getConstraintAff().replace("Not null", ""));
                                        constraints = cm.getConstraintAff().split(" ");
                                        exist = true;
                                    } else {
                                        constraints = cm.getConstraintAff().trim().split(" ");
                                    }
                                    for (String constraint : constraints) {
                                        constArrayList(constraint);
                                    }
                                    if (exist) {
                                        constArrayList("Not null");
                                    }
                                    if (cm.getKey() != null) {
                                        if (cm.getKey().equals("Foreign key")) {
                                            tables.setSelectedItem(cm.getTabSelectForReference());
                                            columns.setSelectedItem(cm.getReferences());
                                            foreignAssociated();
                                        }
                                    }
                                    if (cm.getKey() == null || cm.getKey().equals("Primary key")) {
                                        if (southPanel != null) {
                                            p.remove(southPanel);
                                            Home.content.revalidate();
                                            Home.content.repaint();
                                        }
                                    }
                                    toModify = cm;
                                    break;
                                }
                            }

                        }
                    }
                    Home.content.remove(mainPanel);
                    showTables();
                    panelBuilder.setVisible(true);
                }
            });
        }
        JTableHeader hea = table.getTableHeader();
        hea.setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());
        hea.setBackground(Color.darkGray);
        hea.setForeground(Color.white);
        CreateTableManager model = new CreateTableManager(dataLine);
        table.setModel(model);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JTableUtilities.setCellsAlignment(table, SwingConstants.CENTER, 0);
        scrollPane.setViewportBorder(null);
        scrollPane.setBorder(null);
        return new Object[]{scrollPane, i * 20 + 25};
    }

    CreateTableModel toModify = null;

    /*
     *
     * ========================================================
     *
     */

    public void openCreation(String tabName) {
        setName(tabName);
        panelBuilder.setVisible(true);
        create();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getSource() == addButton) {
            add(columnNameField.getText());
        }
    }

    /*
     *
     * ========================================================
     *
     */

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    /*
     *
     * ========================================================
     *
     */

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getSource() == columnNameField) {
            if (columnNameField.getText().isBlank() && !isColumnExist(columnNameField.getText())
                    && !EddoLibrary.isNumber(columnNameField.getText())) {
                if (EddoLibrary.isNumber(limitTextField.getText()) || !hasLimit(ctm.getDatatype())) {
                    columnNameField.setForeground(Color.black);
                    enableButton();
                }
            } else {
                disableButton();
                columnNameField.setForeground(Color.red);
            }
        } else if (e.getSource() == limitTextField) {
            if (!EddoLibrary.isNumber(limitTextField.getText())) {
                limitTextField.setForeground(Color.red);
                if (hasLimit(ctm.getDatatype()))
                    disableButton();
            } else if (Integer.parseInt(limitTextField.getText()) < 1) {
                limitTextField.setForeground(null);
                disableButton();
            } else {

                if (columnNameField.getText().isBlank()) {
                    enableButton();
                }
            }
        }
    }

    /*
     *
     * ========================================================
     *
     */

    public void disableButton() {
        if (okButton != null) {
            okButton.setEnabled(false);
            addButton.setEnabled(false);
        }
    }

    /*
     *
     * ========================================================
     *
     */

    public void enableButton() {
        if (okButton != null) {
            okButton.setEnabled(true);
            addButton.setEnabled(true);
        }
    }

    /*
     *
     * ========================================================
     *
     */

    public void warning() {
        JLabel imgLabel = new JLabel();
        JPanel gr = new JPanel();
        gr.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        gr.add(imgLabel, gbc);
        mainPanel.add(gr, BorderLayout.CENTER);
        Home.content.add(mainPanel, BorderLayout.CENTER);
        Home.content.revalidate();
        Home.content.repaint();
    }

    /*
     *
     * ========================================================
     *
     */


    public static JPanel p;

    public void create() {
        Home.content.remove(panel);
        okButton = new JButton("Create");
        okButton.setEnabled(false);
        okButton.setPreferredSize(new Dimension(70, 30));

        okButton.addActionListener((ActionEvent e) -> {
            if (dataLine.size() > 0) {
                try {
                    query = queryBuilder(dataLine);

                    int exec = -1;
                    if (DBMS.dbms == 1) {
                        exec = new MySQLDaoOperation().createTable(query);
                    } else if (DBMS.dbms == 2) {
                        exec = new OracleDaoOperation().createTable(query);
                    }

                    if (exec >= 0) {
                        new PupupMessages().message("Created successfully", new _Icon().successIcon());
                        // toUpdateTableList
                        new LoadData().tablesSectionLoader();
                        new TablesSections().options();
                    } else {
                        new PupupMessages().message("Process failed", new _Icon().failedIcon());
                    }
                } catch (SQLException e1) {
                    new PupupMessages().message("Operation failed, please consider giving proper name to your fields",
                            new _Icon().exceptionIcon());
                }

                dataLine.clear();

            } else {
                new PupupMessages().message("Please make sure adding a column", new _Icon().failedIcon());
            }
        });

        exitButton = new JButton("Exit");
        exitButton.setPreferredSize(new Dimension(70, 30));
        exitButton.setFocusable(false);

        exitButton.addActionListener((ActionEvent e) -> {
            new TablesSections().options();

        });
        exitButton.setForeground(Color.red);

        p = new JPanel();
        p.setLayout(new BorderLayout());

        JPanel northPanel, westPanel;
        northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
        westPanel = new JPanel();
        westPanel.setLayout(new FlowLayout());

        p.add(northPanel, BorderLayout.NORTH);
        p.add(east(), BorderLayout.CENTER);
        new Resize(p, "tabData");

        Home.content.add(p, BorderLayout.NORTH);

        Home.content.revalidate();
        Home.content.repaint();
    }

    /*
     *
     * ========================================================
     *
     */

    private JComboBox<String> _data;
    private JLabel limitLabel;

    boolean internAction = false;

    public JPanel east() {
        GridBagConstraints c = new GridBagConstraints();
        JPanel eastPanel = new JPanel();
        cname = new JLabel("Column name");
        columnNameField = new JTextField(10);
        columnNameField.addKeyListener(this);
        limitLabel = new JLabel("Limit");
        limitTextField = new JTextField("2", 5);
        limitTextField.addKeyListener(this);
        JLabel dataTypeLabel = new JLabel("Data type");
        JLabel addC = new JLabel("Add constraints");
        String[] dataTypeArray = {"int", "varchar", "real", "blob", "decimal", "date"};
        _data = new JComboBox<String>(dataTypeArray);
        _data.addActionListener((ActionEvent e) -> {
            ctm.setDatatype(Objects.requireNonNull(_data.getSelectedItem()).toString());
            limitTextField.setEnabled(hasLimit(ctm.getDatatype()));
            if (!EddoLibrary.isNumber(limitTextField.getText())) {
                limitTextField.setForeground(Color.red);
                if (hasLimit(ctm.getDatatype()))
                    disableButton();
                else {
                    enableButton();
                }
            }
        });
        eastPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        eastPanel.add(okButton);
        eastPanel.add(exitButton);
        String[] constraints = {"Not null", "Unique", "Primary key", "Foreign key", "Check", "Default"};
        JComboBox<String> acBox = new JComboBox<String>(constraints);
        acBox.addActionListener((ActionEvent e) -> {
            String item = (String) acBox.getSelectedItem();
            if (item != null) {
                if (acBox.getSelectedIndex() > 3) {
                    new PupupMessages().message(item + " constraint is unavailable", new _Icon().messageIcon());
                } else {
                    if (item.equals("Foreign key")) {
                        /*
                         * label references, combobox tables inside the database and the column
                         * associated
                         */
                        if (tabs.length != 0) {
                            if (!constArrayList.contains("Primary key")) {
                                constArrayList(item);
                                foreignAssociated();
                            }
                        }
                    } else {
                        constArrayList(item);
                    }
                    internAction = true;
                    rcBox.removeAllItems();
                    for (int i = 0; i < constraintArray().length; i++) {
                        internAction = true;
                        rcBox.addItem(constraintArray()[i]);
                    }
                    internAction = false;
                }
            }
        });
        eastPanel.add(cname);
        eastPanel.add(columnNameField);
        eastPanel.add(dataTypeLabel);
        eastPanel.add(_data);
        eastPanel.add(limitLabel);
        eastPanel.add(limitTextField);
        eastPanel.add(addC);
        eastPanel.add(acBox);
        JLabel remC = new JLabel("Remove constraints");
        eastPanel.add(remC);
        JPanel rcPanel = new JPanel();
        rcPanel.setLayout(new GridBagLayout());
        rcBox = new JComboBox<String>(constraintArray());
        rcBox.addActionListener((ActionEvent e) -> {
            String constraint = (String) rcBox.getSelectedItem();
            if (!internAction) {
                removeC(constraint);
                rcBox.removeAllItems();
                for (int i = 0; i < constraintArray().length; i++) {
                    rcBox.addItem(constraintArray()[i]);
                }
                assert constraint != null;
                if (constraint.equals("Foreign key")) {
                    if (southPanel != null) {
                        p.remove(southPanel);
                        Home.content.revalidate();
                        Home.content.repaint();
                    }
                }
            }
            internAction = false;
        });
        // rcPanel.add(rcBox);
        eastPanel.add(rcBox);
        addButton = new JButton("Add column");
        addButton.setEnabled(false);
        addButton.addActionListener(e -> {

            if (constArrayList.contains("Foreign key") && ctm.getReferences() == null) {
                new PupupMessages().message("Please select a reference!", new _Icon().messageIcon());
            } else {
                if (!isColumnExist(columnNameField.getText())) {
                    if (toModify != null) {
                        dataLine.remove(toModify);
                    }
                    toModify = null;
                    ctm.setName(columnNameField.getText());
                    if (hasLimit(ctm.getDatatype()))
                        ctm.setLimit(limitTextField.getText());
                    else
                        ctm.setLimit(null);
                    ctm.setConstraint(constraintAnalysis());
                    dataLine.add(ctm);
                    if (southPanel != null) {
                        p.remove(southPanel);
                        Home.content.revalidate();
                        Home.content.repaint();
                    }
                    Home.content.remove(mainPanel);
                    showTables();
                    panelBuilder.setVisible(true);
                    ctm = new CreateTableModel();
                    constArrayList.clear();
                } else {
                    new PupupMessages().message("two columns can not have same name!", new _Icon().exceptionIcon());
                }
            }
        });
        c.gridx = 11;
        c.gridy = 0;
        eastPanel.add(addButton);
        return eastPanel;
    }

    private JPanel southPanel;
    private String tabSelect = "";
    private boolean remove = false;
    private GridBagConstraints c2;
    private JLabel ref, colref;
    private JPanel alternativePanel;
    private JComboBox<String> tables;

    /*
     *
     * ========================================================
     *
     */

    private JComboBox<String> columns;

    void foreignAssociated() {
        remove = false;
        c2 = new GridBagConstraints();
        c2.insets = new Insets(5, 5, 5, 5);
        ref = new JLabel("Table");
        colref = new JLabel("Column Referenced");
        southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        alternativePanel = new JPanel();
        alternativePanel.setLayout(new FlowLayout());
        tables = new JComboBox<String>(tabs);
        tables.addActionListener((ActionEvent e) -> {
            tabSelect = tables.getSelectedItem().toString();
            try {
                columns = new JComboBox<String>(new MySQLDaoOperation().selectColumn(tabSelect));
                ctm.setTabSelectForReference(tabSelect);
                if (remove) {
                    alternativePanel.removeAll();
                    alternativePanel.revalidate();
                    alternativePanel.repaint();
                    tab();
                }
                alternativePanel.add(colref);
                alternativePanel.add(columns);
                remove = true;
                southPanel.add(alternativePanel, BorderLayout.EAST);
                p.add(southPanel, BorderLayout.SOUTH);
                p.revalidate();
                p.repaint();
                Home.content.revalidate();
                Home.content.repaint();
                columns.addActionListener((ActionEvent e1) -> {
                    ctm.setReferences(Objects.requireNonNull(columns.getSelectedItem()).toString());
                });
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        tab();
    }

    /*
     *
     * ========================================================
     *
     */

    private void tab() {
        c2.gridx = 0;
        c2.gridy = 0;
        alternativePanel.add(ref);
        c2.gridx = 1;
        c2.gridy = 0;
        alternativePanel.add(tables);
        southPanel.add(alternativePanel, BorderLayout.EAST);
        p.add(southPanel, BorderLayout.SOUTH);
        Home.content.revalidate();
        Home.content.repaint();
    }

    /*
     *
     * ========================================================
     *
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     *
     * ========================================================
     *
     */

    private void constArrayList(String constraint) {
        boolean add = true;
        boolean isPrimary = constraint.equalsIgnoreCase("Primary key");
        boolean isForeign = constraint.equalsIgnoreCase("Foreign key");
        for (String data : constArrayList) {
            if (constraint.equals(data)) {
                add = false;
            }
            if (isPrimary) {
                if (data.equalsIgnoreCase("Foreign key")) {
                    add = false;
                }
            } else if (isForeign) {
                if (data.equalsIgnoreCase("Primary key")) {
                    add = false;
                }
            }
        }
        if (add) {
            constArrayList.add(constraint);
        }
    }

    /*
     *
     * ========================================================
     *
     */

    private void removeC(String data) {
        int i = constArrayList.size() - 1;
        while (i >= 0) {
            if (data.equals(constArrayList.get(i))) {
                constArrayList.remove(i);
            }
            i--;
        }
    }

    /*
     *
     * ========================================================
     *
     */

    private void add(String data) {
        dataTable.add(data);
    }

    /*
     *
     * ========================================================
     *
     */

    public String[] constraintArray() {
        int j = constArrayList.size();
        String[] r = new String[j];
        if (j > 0) {
            for (int i = 0; i < r.length; i++) {
                r[i] = constArrayList.get(i);
            }
        }

        return r;
    }

    /*
     *
     * ========================================================
     *
     */

    public String queryBuilder(ArrayList<CreateTableModel> qb) {
        StringBuilder queryBuilder = new StringBuilder("CREATE TABLE " + getName() + " (");
        for (CreateTableModel data : qb) {
            queryBuilder.append(data.getName()).append(" ").append(data.getDatatype());
            if (data.getLimit() != null) {
                if (data.getLimit().length() > 0 && hasLimit(data.getDatatype())) {
                    queryBuilder.append("(").append(data.getLimit()).append(") ");
                }
            }
            if (data.getConstraint().length() > 0) {
                queryBuilder.append(data.getConstraint());
            }
            queryBuilder.append(",");
        }
        queryBuilder = new StringBuilder(queryBuilder.substring(0, queryBuilder.length() - 1));
        queryBuilder.append(")");//removed ; since it raised error in oracle

        return queryBuilder.toString();
    }

    /*
     *
     * ========================================================
     *
     */

    public boolean hasLimit(String dataType) {
        return dataType.equalsIgnoreCase("varchar") || dataType.equalsIgnoreCase("int");
    }

    /*
     *
     * ========================================================
     *
     */

    public String constraintAnalysis() {
        StringBuilder line = new StringBuilder();
        if (!constArrayList.isEmpty()) {
            for (String c : constArrayList) {
                line.append(" ").append(c);
            }
        }

        if (line.toString().contains("Primary key")) {
            ctm.setKey("Primary key");
            for (CreateTableModel c : dataLine) {
                if (c.getKey() != null) {
                    if (c.getKey().equals("Primary key")) {
                        new PupupMessages().confirm("SQL table can't have two primary key, wanna update it?");
                        if (PupupMessages.getAction == 1) {
                            ctm.setKey("Primary key");
                            c.setConstraint(c.getConstraint().replace("Primary key", ""));
                            c.setKey(null);
                            break;
                        } else {
                            ctm.setKey(null);
                            line = new StringBuilder(line.toString().replace("Primary key", ""));
                        }
                    }
                }
            }
            ctm.setConstraintAff(line.toString().replace("Primary key", ""));
        } else if (line.toString().contains("Foreign key")) {
            ctm.setConstraintAff(line.toString().replace("Foreign key", ""));
        } else {
            ctm.setConstraintAff(line.toString());
        }

        if (line.toString().contains("Foreign key")) {
            ctm.setKey("Foreign key");
            line = new StringBuilder(line.toString().replace("Foreign key",
                    "Foreign key (" + columnNameField.getText() + ") " + "REFERENCES (" + ctm.getReferences() + ")"));
        }

        return line.toString();
    }

    /*
     *
     * ========================================================
     *
     */

    public boolean isColumnExist(String name) {
        for (CreateTableModel s : dataLine) {
            if (s.getName().equalsIgnoreCase(name) && toModify == null) {
                return true;
            }
        }
        return false;
    }
}
