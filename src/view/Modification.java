package view;

import controller.sql.modification.Modification_controller_sql;
import dao.DBMS;
import dao.mysql.MySQLDaoOperation;
import dao.oracle.OracleDaoOperation;
import eddql.launch.LoadData;
import view.iconmaker._Icon;
import view.pupupsmessage.PupupMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Objects;

public class Modification {
    String tableName;

    public Modification(String tableName) {
        this.tableName = tableName;
        Home.content.removeAll();

//		Home.content.add(panelsBuilder.getScrollPane(), BorderLayout.WEST);
        // Creating a new panel to set the preferring size will avoid an unnecessarily
        // scrolling
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setPreferredSize(new Dimension(195, 0));
        menuPanel.add(menuPanel());

        Home.content.add(menuPanel, BorderLayout.WEST);
        tablePanel = new TableToBeSelected().select(tableName);
        Home.content.add(tablePanel);
        revalidateHome();
        events();
    }

    private JPanel tablePanel = new JPanel();
    private JPanel northPanel = new JPanel();

    private void switchMenu(JPanel panel) {
        Home.content.remove(northPanel);
        northPanel = new JPanel(new BorderLayout());
        northPanel.add(panel);
        Home.content.add(northPanel, BorderLayout.NORTH);
        revalidateHome();
    }

    private void revalidateHome() {
        Home.content.revalidate();
        Home.content.repaint();
    }

    private void revalidate() {
        Home.content.remove(northPanel);
        revalidateHome();
    }

    private void refreshTable() {
        Home.content.remove(tablePanel);
        tablePanel = new TableToBeSelected().select(tableName);
        Home.content.add(tablePanel);
        revalidateHome();
    }

    private JPanel renameTable() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JLabel renameLabel = getLabel("Rename " + tableName + " to ");
        JTextField field = new JTextField(10);
        JButton renameButton = new JButton("Rename");

        renameButton.addActionListener((e) -> {
            String newName = field.getText();
            if (newName.isBlank()) {
                new PupupMessages().message("Please write a new name", new _Icon().messageIcon());
            } else {
                String res = new Modification_controller_sql().renameTable(tableName, newName);
                if (res.isEmpty()) {
                    new PupupMessages().message("Process completed", new _Icon().successIcon());
                    new LoadData().tablesSectionLoader();
                    this.tableName = newName;
                    refreshTable();
                } else {
                    new PupupMessages().message(res, new _Icon().exceptionIcon());
                }
            }
        });

        panel.add(renameLabel);
        panel.add(field);
        panel.add(renameButton);
        return panel;
    }

    private void deleteTable() {
        new PupupMessages().confirm("Confirm " + tableName + " deletion?");
        if (PupupMessages.getAction == 1) {
            try {
                if (DBMS.dbms == 1) {
                    new MySQLDaoOperation().dropTable(tableName);
                } else if (DBMS.dbms == 2) {
                    new OracleDaoOperation().dropTable(tableName);
                }

                new PupupMessages().message(tableName + " deleted successfully!", new _Icon().successIcon());
                new TablesSections().options();
            } catch (SQLException e1) {
                new PupupMessages().message(e1.getMessage(), new _Icon().exceptionIcon());
            }
            new TablesSections().options();
        }

    }

    public void clearAll() {
        new PupupMessages().confirm("Do you really want to clear the table?");
        int y = PupupMessages.getAction;
        if (y == 1) {
            try {
                int z = -1;
                if (DBMS.dbms == 1) {
                    z = new MySQLDaoOperation().deleteAll(tableName);
                } else if (DBMS.dbms == 2) {
                    z = new OracleDaoOperation().clearTable(tableName);
                }


                if (z > 0) {
                    new PupupMessages().message(z + " row(s) of " + tableName + " cleared successfully",
                            new _Icon().successIcon());
                    refreshTable();
                } else {
                    new PupupMessages().message(z + " row affected, process completed! ", new _Icon().messageIcon());
                }
            } catch (SQLException e1) {
                new PupupMessages().message(e1.getMessage(), new _Icon().exceptionIcon());
            }
            revalidate();
        }
    }

    private JPanel addColumn() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel nameLabel = getLabel("Name");
        JTextField nameField = getTextField(15);
        String[] datatype = {"int(2)", "varchar(15)", "real", "blob", "decimal(2,2)", "date"};
        JComboBox<String> comboBox = new JComboBox<String>(datatype);
        comboBox.setSelectedIndex(1);
        // TODO: Optimizing later...(maybe)
        comboBox.addActionListener((e) -> {
            int index = comboBox.getSelectedIndex();
            comboBox.setEditable(index == 0 || index == 1 || index == 4);
        });
        JButton addButton = getButton("Add");

        addButton.addActionListener((e) -> {
            String select = comboBox.getEditor().getItem().toString();// getting the edited value from the combobox
            if (!nameField.getText().isBlank()) {

                select = nameField.getText() + " " + select;

                String res = new Modification_controller_sql().addColumn(select, tableName);
                if (res.isEmpty()) {
                    new PupupMessages().message("Process completed", new _Icon().successIcon());
                    new LoadData().tablesSectionLoader();
                    refreshTable();
                } else {
                    new PupupMessages().message(res, new _Icon().exceptionIcon());
                }
            } else {
                new PupupMessages().message("Please name your column", new _Icon().messageIcon());
            }
        });

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(comboBox);
        panel.add(addButton);

        return panel;
    }

    private JPanel renameColumn() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel renameLabel = getLabel("Rename");
        JComboBox<String> comboBox = new JComboBox<String>(TableToBeSelected.head);
        JLabel label = getLabel("to");
        JTextField newNameField = getTextField(10);
        JButton renameButton = getButton("Rename");

        renameButton.addActionListener((e) -> {
            String newName = newNameField.getText();
            if (newName.isBlank()) {
                new PupupMessages().message("Please write a new name", new _Icon().messageIcon());
            } else {
                String res = new Modification_controller_sql().renameColumn(tableName,
                        Objects.requireNonNull(comboBox.getSelectedItem()).toString(), newName);
                if (res.isEmpty()) {
                    new PupupMessages().message("Renamed completed", new _Icon().successIcon());
                    new LoadData().tablesSectionLoader();
                    refreshTable();
                } else {
                    new PupupMessages().message(res, new _Icon().exceptionIcon());
                }
            }
        });

        panel.add(renameLabel);
        panel.add(comboBox);
        panel.add(label);
        panel.add(newNameField);
        panel.add(renameButton);

        return panel;
    }

    private JPanel deleteColumn() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel deleteLabel = getLabel("Delete column ");
        JComboBox<String> comboBox = new JComboBox<String>(TableToBeSelected.head);

        comboBox.addActionListener((ActionEvent e) -> {
            String x = (String) comboBox.getSelectedItem();
            new PupupMessages().confirm("Would you really like to drop " + x + "?");
            int y = PupupMessages.getAction;
            if (y == 1) {
                String rep = new Modification_controller_sql().dropColumn(tableName, x);
                if (rep.isEmpty()) {
                    new PupupMessages().message("Process Completed", new _Icon().successIcon());
                    new LoadData().tablesSectionLoader();
                    refreshTable();
                } else {
                    new PupupMessages().message(rep, new _Icon().exceptionIcon());
                }
            } else {
                new PupupMessages().message("Process canceled", new _Icon().messageIcon());
            }
        });

        panel.add(deleteLabel);
        panel.add(comboBox);

        return panel;
    }

    private JPanel deleteRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel deleteLabel = getLabel("Delete from " + tableName + " if only ");
        JComboBox<String> comboBox = new JComboBox<String>(TableToBeSelected.head);
        comboBox.setSelectedIndex(0);
        JTextField field = getTextField(10);
        JButton valid_addButton = new JButton("valid&add");
        JButton deleteButton = getButton("Delete");

        valid_addButton.addActionListener((e) -> {

            if (!field.getText().isBlank()) {

                String string = Objects.requireNonNull(comboBox.getSelectedItem()) + field.getText();
                if (!buildQuery.isBlank()) {
                    string = " AND " + string;
                }
                queryBuilder(string);
                // initialize if new constraints hv to b added
                comboBox.setSelectedIndex(0);
                field.setText("");

            } else {
                new PupupMessages().message("Please specify a condition", new _Icon().messageIcon());
            }

        });

        deleteButton.addActionListener((e) -> {
            if (!buildQuery.isBlank()) {
                String rep = new Modification_controller_sql().delete_row(tableName, buildQuery);

                if (rep.equals("s")) {
                    new PupupMessages().message("Deletion successful!", new _Icon().successIcon());
                    new LoadData().tablesSectionLoader();
                    refreshTable();
                } else if (rep.equals("f")) {
                    new PupupMessages().message("0 row(s) affected", new _Icon().failedIcon());
                } else {
                    new PupupMessages().message(rep, new _Icon().exceptionIcon());
                }

            } else {
                new PupupMessages().message("Incorrect condition! " + buildQuery, new _Icon().failedIcon());
            }
        });

        panel.add(deleteLabel);
        panel.add(comboBox);
        panel.add(field);
        panel.add(valid_addButton);
        panel.add(deleteButton);

        return panel;
    }

    private String buildQuery = "";

    public void queryBuilder(String string) {
        buildQuery += string;
    }


    private JPanel updateRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel updateLabel = getLabel("Update " + tableName + " set the value ");
        panel.add(updateLabel);
        JTextField val_field = getTextField(10);
        panel.add(val_field);
        JLabel label = getLabel("on");
        panel.add(label);

        JComboBox<String> comboBox = new JComboBox<String>(TableToBeSelected.head);
        comboBox.setSelectedIndex(0);
        panel.add(comboBox);
        JCheckBox constBox = new JCheckBox("Constraint");
        panel.add(constBox);
        JLabel constLabel = getLabel("whenever");
        panel.add(constLabel);
        JComboBox<String> comboBox2 = new JComboBox<String>(TableToBeSelected.head);
        comboBox2.setSelectedIndex(0);
        comboBox2.setEnabled(false);
        panel.add(comboBox2);
        JTextField const_field = getTextField(10);
        const_field.setEnabled(false);
        panel.add(const_field);
        JButton updateButton = getButton("Update");
        panel.add(updateButton);

        constBox.addActionListener((e) -> {
            const_field.setEnabled(constBox.isSelected());
            comboBox2.setEnabled(constBox.isSelected());
        });

        updateButton.addActionListener((e) -> {
            if (!val_field.getText().isBlank()) {

                if (constBox.isSelected()) {
                    if (!const_field.getText().isBlank()) { // comboBox column to b affected
                        // //comboBox2 Column constraint applied from
                        triggerUpdate(val_field, comboBox, constBox, comboBox2, const_field);
                    } else {
                        new PupupMessages().message("The condition is missing!", new _Icon().messageIcon());
                    }
                } else {
                    triggerUpdate(val_field, comboBox, constBox, comboBox2, const_field);
                }
            } else {
                new PupupMessages().message("Please set a value!", new _Icon().messageIcon());
            }
        });

        return panel;
    }

    private void triggerUpdate(JTextField val_field, JComboBox<String> comboBox, JCheckBox constBox, JComboBox<String> comboBox2, JTextField const_field) {
        String rep = new Modification_controller_sql().updating(val_field.getText(),
                Objects.requireNonNull(comboBox.getSelectedItem()).toString(), const_field.getText(),
                Objects.requireNonNull(comboBox2.getSelectedItem()).toString(), constBox.isSelected(), tableName);
        new PupupMessages().message(rep, new _Icon().messageIcon());
        new LoadData().tablesSectionLoader();
        refreshTable();
    }

    private JLabel getLabel(String text) {
        return new JLabel(text);
    }

    private JButton getButton(String text) {

        return new JButton(text);
    }

    private JTextField getTextField(int dim) {

        return new JTextField(dim);
    }

    private JScrollPane menuPanel() {
        JPanel operationsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(1, 2, 1, 2);
        constraints.gridx = 0;
        constraints.gridy = 0;
        operationsPanel.add(rename_table_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        operationsPanel.add(delete_table_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        operationsPanel.add(clear_table_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 3;
        operationsPanel.add(add_column_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 4;
        operationsPanel.add(rename_column_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 5;
        operationsPanel.add(delete_column_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 6;
        operationsPanel.add(delete_row_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 7;
        operationsPanel.add(update_row_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 8;
        operationsPanel.add(add_constraint_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 9;
        operationsPanel.add(change_datatype_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 10;
        operationsPanel.add(quick_change_button, constraints);
        constraints.gridx = 0;
        constraints.gridy = 11;
        operationsPanel.add(exit_button, constraints);
        exit_button.setForeground(Color.red);
        JScrollPane scrollPane = new JScrollPane(operationsPanel);

        scrollPane.setViewportBorder(null);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    /*
     *
     *
     */

    private JButton getMenuButton(String txt) {
        JButton button = new JButton(txt);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(150, 25));

        return button;
    }

    private void events() {

        rename_table_button.addActionListener((e) -> switchMenu(renameTable()));

        delete_table_button.addActionListener((e) -> deleteTable());

        clear_table_button.addActionListener((e) -> clearAll());

        add_column_button.addActionListener((e) -> switchMenu(addColumn()));

        rename_column_button.addActionListener((e) -> switchMenu(renameColumn()));

        delete_column_button.addActionListener((e) -> switchMenu(deleteColumn()));

        delete_row_button.addActionListener((e) -> switchMenu(deleteRow()));

        update_row_button.addActionListener((e) -> switchMenu(updateRow()));

        add_constraint_button.addActionListener((e) -> {
        });

        quick_change_button.addActionListener((e) -> {
        });

        change_datatype_button.addActionListener((e) -> {
        });

        exit_button.addActionListener((e) -> {
            new TablesSections().options();
        });

    }

    private final JButton rename_table_button = getMenuButton("Rename table");

    private final JButton delete_table_button = getMenuButton("Delete table");
    private final JButton clear_table_button = getMenuButton("Clear table ");
    private final JButton add_column_button = getMenuButton("Add column");
    private final JButton rename_column_button = getMenuButton("Rename column");
    private final JButton delete_column_button = getMenuButton("Delete column");
    private final JButton delete_row_button = getMenuButton("Delete row ");
    private final JButton update_row_button = getMenuButton("Update row");
    private final JButton add_constraint_button = getMenuButton("Add constraint");
    private final JButton change_datatype_button = getMenuButton("Change datatype");
    private final JButton quick_change_button = getMenuButton("Quick change");
    private final JButton exit_button = getMenuButton("Exit");
}
