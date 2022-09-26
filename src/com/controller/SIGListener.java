/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller;

import com.model.HeaderTableModel;
import com.model.InvoiceHeader;
import com.model.InvoiceLine;
import com.model.LineTableModel;
import com.view.InvoiceHeaderDialog;
import com.view.InvoiceLineDialog;
import com.view.SIGFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author DELL
 */
public class SIGListener implements ActionListener, ListSelectionListener {

    private SIGFrame sigframe;
    private InvoiceHeaderDialog invoiceheaderdialog;
    private InvoiceLineDialog invoicelinedialog;

    public SIGListener(SIGFrame sigframe) {
        this.sigframe = sigframe;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedRow = sigframe.getInvoicesTable().getSelectedRow();
        if (selectedRow > -1) {
            InvoiceHeader inv = sigframe.getInvoices().get(selectedRow);
            sigframe.getInvNumLbl().setText("" + inv.getNum());
            sigframe.getInvDateLbl().setText(SIGFrame.sdf.format(inv.getDate()));
            sigframe.getInvCustNameLbl().setText(inv.getName());
            sigframe.getInvTotalLbl().setText("" + inv.getTotal());
            ArrayList<InvoiceLine> lines = inv.getLines();
            sigframe.setLineTableModel(new LineTableModel(lines));
        } else {
            sigframe.getInvNumLbl().setText("");
            sigframe.getInvDateLbl().setText("");
            sigframe.getInvCustNameLbl().setText("");
            sigframe.getInvTotalLbl().setText("");
            sigframe.setLineTableModel(new LineTableModel());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case "Load":
                load(null, null);
                break;
            case "Save":
                save();
                break;
            case "Create Invoice":
                createInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "Create Item":
                createItem();
                break;
            case "Delete Item":
                deleteItem();
                break;
            case "newInvoiceOK":
                newInvoiceOK();
                break;
            case "newInvoiceCancel":
                newInvoiceCancel();
                break;
            case "newLineOK":
                newLineOK();
                break;
            case "newLineCancel":
                newLineCancel();
                break;
        }
    }

    public void load(String headerPath, String linePath) {
        File headerFile = null;
        File lineFile = null;
        if (headerPath == null && linePath == null) {
            JOptionPane.showMessageDialog(sigframe, "Select header file first, then select line file.", "Invoice files", JOptionPane.WARNING_MESSAGE);
            JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(sigframe);
            if (result == JFileChooser.APPROVE_OPTION) {
                headerFile = fc.getSelectedFile();
                result = fc.showOpenDialog(sigframe);
                if (result == JFileChooser.APPROVE_OPTION) {
                    lineFile = fc.getSelectedFile();
                }
            }
        } else {
            headerFile = new File(headerPath);
            lineFile = new File(linePath);
        }

        if (headerFile != null && lineFile != null) {
            try {
                /*
                1,22-11-2020,Ali
                2,13-10-2021,Saleh
                 */
                // collection streams
                List<String> headerLines = Files.lines(Paths.get(headerFile.getAbsolutePath())).collect(Collectors.toList());
                /*
                1,Mobile,3200,1
                1,Cover,20,2
                1,Headphone,130,1	
                2,Laptop,9000,1
                2,Mouse,135,1
                 */
                List<String> lineLines = Files.lines(Paths.get(lineFile.getAbsolutePath())).collect(Collectors.toList());
                //ArrayList<Invoice> invoices = new ArrayList<>();
                sigframe.getInvoices().clear();
                for (String headerLine : headerLines) {
                    String[] parts = headerLine.split(",");  // "1,22-11-2020,Ali"  ==>  ["1", "22-11-2020", "Ali"]
                    String numString = parts[0];
                    String dateString = parts[1];
                    String name = parts[2];
                    int num = Integer.parseInt(numString);
                    Date date = sigframe.sdf.parse(dateString);
                    InvoiceHeader inv = new InvoiceHeader(num, name, date);
                    sigframe.getInvoices().add(inv);
                }
                System.out.println("Check point");
                for (String lineLine : lineLines) {
                    // lineLine = "1,Mobile,3200,1"
                    String[] parts = lineLine.split(",");
                    /*
                    parts = ["1", "Mobile", "3200", "1"]
                     */
                    int num = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int count = Integer.parseInt(parts[3]);
                    InvoiceHeader inv = sigframe.getInvoiceByNum(num);
                    InvoiceLine line = new InvoiceLine(name, price, count, inv);
                    inv.getLines().add(line);
                }
                System.out.println("Check point");
                sigframe.setHeaderTableModel(new HeaderTableModel(sigframe.getInvoices()));
              
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void save() {
        JFileChooser fc = new JFileChooser();
        File headerFile = null;
        File lineFile = null;
        int result = fc.showSaveDialog(sigframe);
        if (result == JFileChooser.APPROVE_OPTION) {
            headerFile = fc.getSelectedFile();
            result = fc.showSaveDialog(sigframe);
            if (result == JFileChooser.APPROVE_OPTION) {
                lineFile = fc.getSelectedFile();
            }
        }
        
        if (headerFile != null && lineFile != null) {
            String headerData = "";
            String lineData = "";
            for (InvoiceHeader inv : sigframe.getInvoices()) {
                headerData += inv.getAsCSV();
                headerData += "\n";
                for (InvoiceLine line : inv.getLines()) {
                    lineData += line.getAsCSV();
                    lineData += "\n";
                }
            }
            try {
                FileWriter headerFW = new FileWriter(headerFile);
                FileWriter lineFW = new FileWriter(lineFile);
                headerFW.write(headerData);
                lineFW.write(lineData);
                headerFW.flush();
                lineFW.flush();
                headerFW.close();
                lineFW.close();
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(sigframe, "Error while writing file(s)", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }

    private void createInvoice() {
        invoiceheaderdialog = new InvoiceHeaderDialog(sigframe);
        invoiceheaderdialog.setLocation(300, 300);
        invoiceheaderdialog.setVisible(true);
    }

    private void deleteInvoice() {

        int selectedRow = sigframe.getInvoicesTable().getSelectedRow();
        if (selectedRow > -1) {
            sigframe.getInvoices().remove(selectedRow);
            sigframe.getHeaderTableModel().fireTableDataChanged();
        }
    }

    private void createItem() {
        if (sigframe.getInvoicesTable().getSelectedRow() > -1) {
            invoicelinedialog = new InvoiceLineDialog(sigframe);
            invoicelinedialog.setLocation(300, 300);
            invoicelinedialog.setVisible(true);
        }
    }

    private void deleteItem() {
        int selectedInvoice = sigframe.getInvoicesTable().getSelectedRow();
        int selectedItem = sigframe.getLinesTable().getSelectedRow();

        if (selectedInvoice > -1 && selectedItem > -1) {
            sigframe.getInvoices().get(selectedInvoice).getLines().remove(selectedItem);
            sigframe.getLineTableModel().fireTableDataChanged();
            sigframe.getHeaderTableModel().fireTableDataChanged();
            sigframe.getInvoicesTable().setRowSelectionInterval(selectedInvoice, selectedInvoice);
        }
    }

    private void newInvoiceOK() {
        String name = invoiceheaderdialog.getCustNameField().getText();
        String dateStr = invoiceheaderdialog.getInvDateField().getText();
        newInvoiceCancel();
        try {
            Date date = sigframe.sdf.parse(dateStr);
            InvoiceHeader inv = new InvoiceHeader(sigframe.getNextInvNum(), name, date);
            sigframe.getInvoices().add(inv);
            sigframe.getHeaderTableModel().fireTableDataChanged();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(sigframe, "Invalid Date Format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newInvoiceCancel() {
        invoiceheaderdialog.setVisible(false);
        invoiceheaderdialog.dispose();
        invoiceheaderdialog = null;
    }

    private void newLineOK() {
        String name = invoicelinedialog.getItemNameField().getText();
        String countStr = invoicelinedialog.getItemCountField().getText();
        String priceStr = invoicelinedialog.getItemPriceField().getText();
        newLineCancel();
        try {
            int c = Integer.parseInt(countStr);
            double prices = Double.parseDouble(priceStr);
            int currentInvoice = sigframe.getInvoicesTable().getSelectedRow();
            InvoiceHeader inv = sigframe.getInvoices().get(currentInvoice);
            InvoiceLine line = new InvoiceLine(name, prices, c, inv);
            inv.getLines().add(line);
            sigframe.getHeaderTableModel().fireTableDataChanged();
            sigframe.getInvoicesTable().setRowSelectionInterval(currentInvoice, currentInvoice);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(sigframe, "Invalid Number Format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newLineCancel() {
        invoicelinedialog.setVisible(false);
        invoicelinedialog.dispose();
        invoicelinedialog = null;
    }

}
