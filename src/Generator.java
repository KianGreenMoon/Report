import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Generator {
    public static void main(String[] args) {
//        Settings settings = new Settings("src/settings.xml");
//        SourceData sourceData = new SourceData("src/source-data.tsv");
//        String pathForNewFile = "test.txt";
        Settings settings = new Settings(args[0]);
        SourceData sourceData = new SourceData(args[1]);
        String pathForNewFile = args[2];

        ArrayList<String[]> personList = sourceData.getPersonInfo();
        ArrayList<String[]> head = new ArrayList<>();
        head.add(new String[] {settings.getTitle(0), settings.getTitle(1), settings.getTitle(2)});

        //Head format
        head = inputLines(head, settings);

        //Input and format:
        personList = inputLines(personList, settings);

        writeInClear(pathForNewFile); //to clear the file
        //Output
        int j = settings.getHeightPage();
        for(String[] person : personList)
        {
            //Names of columns (head)
            if(j == settings.getHeightPage())
            {
                for(String headLine[] : head)
                {
                    String headStringOutPut = "| " + headLine[0] + " | " + headLine[1] + " | " + headLine[2] + " |";
                    writeInLine(headStringOutPut, pathForNewFile);
                    j--;
                }
            }

            //Separator
            if(person[0].matches("\\d++[\\s]*+") || j == settings.getHeightPage() - 1)
                writeInLine(new String(new char[settings.getWidthPage()]).replace("\0", "-"), pathForNewFile);

            //Print
            String outPut = "| " + person[0] + " | " + person[1] + " | " + person[2] + " |";
            writeInLine(outPut, pathForNewFile);
            j--;

            //Separator of blanks
            if(j <= 0)
            {
                writeInLine("~", pathForNewFile);
                j = settings.getHeightPage();
            }
        }
    }

    private static String formatForWrite(String string, Integer settingsWidth) {
        string = string + new String(new char[settingsWidth - string.length()]).replace("\0", " ");
        return string;
    }

    private static ArrayList<String[]> inputLines(ArrayList<String[]> sourcePersonList, Settings settings){
        ArrayList<String[]> linesList = new ArrayList<>();
        for(String[] person : sourcePersonList)
        {
            while(!person[0].equals("") || !person[1].equals("") || !person[2].equals(""))
            {
                String [] outputLine = new String[person.length];
                for(int i = 0; i < outputLine.length; i++)
                    outputLine[i] = "";

                for(int i = 0; i < person.length; i++)
                {
                    if(person[i].length() > settings.getWidth(i))
                    {
                        String [] personSplit = person[i].split("\\b");
                        for (String splitedStr : personSplit)
                        {
                            if (outputLine[i].length() + splitedStr.length()
                                    <= settings.getWidth(i))
                            {
                                outputLine[i] = formatForWrite((outputLine[i] + splitedStr).trim(), settings.getWidth(i));
                                person[i] = person[i].substring(splitedStr.length()).trim();
                                break;
                            } else if (outputLine[i].equals(""))
                            {
                                outputLine[i] = person[i].substring(0, settings.getWidth(i));
                                person[i] = person[i].substring(settings.getWidth(i));
                                break;
                            } else {
                                System.err.println("WTF?? Something is wrong with my code! Check line: " + getLineNumber());
                            }
                        }
                    } else {
                        outputLine[i] = formatForWrite(person[i], settings.getWidth(i));
                        person[i] = "";
                    }
                }
                linesList.add(outputLine);
            }
        }
        return linesList;
    }

    private static void writeInClear(String filePath) {
        writeIn("", false, filePath);
    }
    private static void writeInLine(String str, String filePath) {
        writeIn(str + "\r\n", true, filePath);
    }
    private static void writeIn(String str, boolean append, String filePath) {
        File file = new File(filePath);
        String charsetName = "UTF16";

        try(OutputStream outputStream = new FileOutputStream(file,append);
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, charsetName))
        {
            streamWriter.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[2].getLineNumber();
    }

    private static class Settings {
        private File file;

        private Integer widthPage;
        private Integer heightPage;

        private Integer[] width;
        private String[] title;

//        private Settings() {
//            this("src/settings.xml");
//        }

        private Settings(String pathFile) {
            file = new File(pathFile);

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                document.normalizeDocument();

                XPath xPath = XPathFactory.newInstance().newXPath();

                widthPage = Integer.parseInt(xPath.evaluate("/settings/page/width", document));
                heightPage = Integer.parseInt(xPath.evaluate("/settings/page/height", document));

                NodeList widthsNodeList = (NodeList) xPath.evaluate("/settings/columns/column/width",
                        document, XPathConstants.NODESET);
                width = new Integer[widthsNodeList.getLength()];
                for(int i = 0; i < widthsNodeList.getLength(); i++)
                    width[i] = Integer.parseInt(widthsNodeList.item(i).getTextContent());

                NodeList titlesNodeList = (NodeList) xPath.evaluate("/settings/columns/column/title",
                        document, XPathConstants.NODESET);
                title = new String[widthsNodeList.getLength()];
                for(int i = 0; i < titlesNodeList.getLength(); i++)
                    title[i] = titlesNodeList.item(i).getTextContent();
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        private Integer getWidthPage() {
            return widthPage;
        }

        private Integer getHeightPage() {
            return heightPage;
        }

        private Integer getWidth(int column) {
            return width[column];
        }

        private String getTitle(int column) {
            return title[column];
        }
    }

    private static class SourceData {

        private ArrayList<String[]> personInfo;
        private File file;
        private String charsetName;

//        private SourceData() {
//            this("src/source-data.tsv");
//        }

        private SourceData(String pathFile) {
            personInfo = new ArrayList<>();
            file = new File(pathFile);
            charsetName = "UTF16";

            try(Scanner scanner = new Scanner(
                    new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName))))
            {
                while (scanner.hasNextLine())
                {
                    personInfo.add(scanner.nextLine().trim().split("\t"));
                }
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        private ArrayList<String[]> getPersonInfo() {
            return personInfo;
        }

//        public String getCharsetName() {
//            return charsetName;
//        }

//        private void writeAllData() {
//            for(String[] person : personInfo)
//            {
//                System.out.printf("Номер: %s, Дата: %s, Имя: %s\n", person[0], person[1], person[2]);
//            }
//        }
    }
}
