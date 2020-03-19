package com.company.FileStatistics;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @description 图形界面统计
 * @author guopei
 * @date 2020-03-15 17:50
 */
public class Swing {

    public static void SwingStatistics() {

        JFrame frame = new JFrame("文件选择");
        frame.setLayout(new FlowLayout());
        JFileChooser chooser = new JFileChooser();
        JButton bOpen = new JButton("打开文件");
        frame.add(bOpen);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 150);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        bOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = chooser.showOpenDialog(frame);
                File file = chooser.getSelectedFile();
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(frame, "计划打开文件:" + file.getAbsolutePath());
                    JOptionPane.showMessageDialog(frame, "文本内容统计:\n" + HandleFile.FileStatistics(file, "-x"));
                }
            }
        });
    }
}