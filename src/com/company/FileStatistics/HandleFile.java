package com.company.FileStatistics;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 文件基础以及扩展功能实现
 * @author guopei
 * @date 2020-03-13 10:55
 */

public class HandleFile {

    //当前exe所在的文件路径
    public static final String address = System.getProperty("exe.path");

    public static Boolean isNotes = false;

    public static void FileHandle(String[] args) {

//        System.out.println("当前路径为：" + address);
        if (args[0].equals("-x")) {
            Swing.SwingStatistics();
            return;
        }
        List<File> fileList = new ArrayList<File>();
        //对发送过来的命令进行判断
        if (isPermit(args[args.length - 1])) {
            File file = new File(address);
            args[args.length - 1] = change(args[args.length - 1]);
            if (file.isDirectory()) {
                //如果是文件夹，获取当下的符合条件的文件
                getFiles(fileList, file.getPath(), args[args.length - 1]);
            }
            else {
                if (file.getName().matches(args[args.length - 1])) {
                    fileList.add(file);
                }
            }
            //命令是递归查询多个文件
            if (args[0].equals("-s")) {
                if (fileList.size() < 1) {
                    System.out.println("查找不到符合条件的文件");
                    return;
                }
                //说明要统计多个文件
                for (File f : fileList) {
                    String response = FileStatistics(f, args[1]);
                    if ("不支持的命令格式".equals(response)) {
                        System.out.println(response);
                        break;
                    }
                    else if (response != null) {
                        System.out.println(response);
                    }
                }
            }
            //命令是查询单个文件
            else {
                if (fileList.size() < 1) {
                    System.out.println("查找不到符合条件的文件");
                    return;
                }
                else if (fileList.size() > 1) {
                    System.out.println("查找到不止一个符合条件的文件，请加入-s命令来递归处理多个文件");
                    return;
                }
                System.out.println(FileStatistics(fileList.get(0), args[0]));
            }
        }
        else {
            System.out.println("文件格式不支持");
        }
    }

    private static void getFiles(List<File> fileList, String path, String matches) {
        File file = new File(path);
        File[] files = file.listFiles();
        for(File fileIndex:files){
            //如果这个文件是目录，则进行递归搜索
            if(fileIndex.isDirectory()){
                getFiles(fileList, fileIndex.getPath(), matches);
            }else {
                //如果文件是符合匹配条件的文件，则将文件放入集合中
                if (fileIndex.getName().matches(matches)) {
                    fileList.add(fileIndex);
                }
            }
        }
    }

    private static Integer WordStatistics(String content) {
        //统计词的数目
        String copy = content;
        Integer num = 0;
        //统计词的个数,将每一行数据切割成多个子字符串
        String[] strings = copy.split("[\\s+,\\.\n\\;\\(\\<\\[\\{]");
        //对切割后的每个字符串进行判断
        for (int i = 0; i < strings.length; i++) {
            //将字符不是字母的去除
            strings[i] = strings[i].replaceAll("[^a-zA-Z]", "");
            if (!strings[i].equals("")) {
                //如果不是空行，单词数+1
                num++;
            }
        }
        return num;
    }

    //对文本通配符转换成java的正则表达式
    public static String change(String matches) {
        String[] split = matches.split("\\.");
        matches = matches.replace(".", "");
        matches = matches.replace(split[split.length - 1], "(." + split[split.length - 1] + ")");
        matches = matches.replaceAll("\\?", ".{1}");
        matches = matches.replaceAll("\\*", ".+");
        matches = matches.replaceAll("!", "^");
        return matches;
    }

    //统计更复杂的数据（代码行 / 空行 / 注释行）
    private static Integer[] LineStatistics(String content) {
        //顺序为：空行 / 注释行
        Integer[] lines = {0, 0};

        //统计空行数
        if (content.equals("") || content.matches("[\\{\\}]")) {
            lines[0]++;
        }
        //统计注释行
        if (content.matches("(.*)\\/\\/(.*)")) {
            lines[1]++;
        }
        else if (content.startsWith("/*") && content.endsWith("*/")) {
            //说明使用了/**/的注释形式
            lines[1]++;
        }
        else if (content.matches("(.*)\\/\\*(.*)")) {
            //说明使用了/*的注释形式
            lines[1]++;
            isNotes = true;
        }
        else if (content.matches("(.*)\\*\\/(.*)")) {
            lines[1]++;
            isNotes = false;
        }
        else if (isNotes == true) {
            lines[1]++;
        }
        return lines;
    }

    //正则判断文件格式是否支持进行统计（符合的标准为.c,.java,.cpp,.py,.txt）
    private static Boolean isPermit(String fileType) {
        if (fileType.matches(".+(.c|.java|.cpp|.py|.txt)$")) {
            return true;
        }
        return false;
    }

    /**
     * wc.exe -c file.c     //返回文件 file.c 的字符数
     *
     * wc.exe -w file.c    //返回文件 file.c 的词的数目
     *
     * wc.exe -l file.c      //返回文件 file.c 的行数
     * 扩展功能：
     *     -s   递归处理目录下符合条件的文件。
     *     -a   返回更复杂的数据（代码行 / 空行 / 注释行）。
     * @param file
     * @param type
     * @return
     */
    public static String FileStatistics(File file, String type) {
        //行数
        Integer line = 0;

        //单词数
        Integer wordNum = 0;

        //字符数
        Integer charNum = 0;

        //空行数
        Integer nullLine = 0;

        //注释行
        Integer notesLine = 0;
        try {
            //判断文件类型是否符合
            if (!isPermit(file.getName())) {
                System.out.println("不支持的文件格式");
                return "不支持的文件格式";
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String content = null;
            Boolean isNotes = false;
            while ((content = bufferedReader.readLine()) != null) {
                //去除字符串的首尾空格
                content = content.trim();
                //System.out.println(content);
                switch (type) {
                    case "-c":
                        //返回文件的字符数
                        //将空格全部去除
                        content = content.replaceAll(" +", "");
                        charNum += content.length();
                        break;
                    case "-w":
                        //统计词的数目
                        wordNum += WordStatistics(content);
                        break;
                    case "-a":
                        Integer[] lines = LineStatistics(content);
                        nullLine += lines[0];
                        notesLine += lines[1];
                        break;
                    case "-x":
                        //统计全部信息
                        content = content.replaceAll(" +", "");
                        charNum += content.length();
                        wordNum += WordStatistics(content);
                        Integer[] Lines = LineStatistics(content);
                        nullLine += Lines[0];
                        notesLine += Lines[1];
                        break;
                    case "-l":
                        break;
                    default:
                        return "不支持的命令格式";
                }
                //总行数
                line++;
            }
            switch (type) {
                case "-c":
                    return file.getName() + "文件总字符数：" + charNum;
                case "-w":
                    return file.getName() + "文件的单词数：" + wordNum;
                case "-l":
                    return file.getName() + "文件总行数：" + line;
                case "-a":
                    return file.getName() + ":\r\n" +
                            "文件空行数：" + nullLine + "\r\n" +
                            "文件注释行数：" + notesLine + "\r\n" +
                            "代码行数：" + (line - nullLine - notesLine);
                case "-x":
                    return file.getName() + ":\r\n" +
                            "文件总字符数：" + charNum + "\r\n" +
                            "文件的单词数：" + wordNum + "\r\n" +
                            "文件总行数：" + line + "\r\n" +
                            "文件空行数：" + nullLine + "\r\n" +
                            "文件注释行数：" + notesLine + "\r\n" +
                            "代码行数：" + (line - nullLine - notesLine);
                default:
                    return "不支持的命令格式";
            }
        } catch (FileNotFoundException e) {
            System.out.println("该文件或文件名不存在");
        } catch (IOException e) {
            System.out.println("文件读取错误");
        }
        return null;
    }
}