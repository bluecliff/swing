import java.io.*;
import java.util.Map;

/**
 * @author lishuangjiang
 * @date 2020/11/26
 */
public class Swing {
    public static void main(String[] args) {
        String inPathPattern = args[1];
        String outPath = args[2];
        Integer userSize = Integer.valueOf(args[3]);
        Double alpha = Double.valueOf(args[4]);
        SwingWrapper swingWrapper = new SwingWrapper(userSize, alpha);

        System.out.println("begin calc:");
        System.out.println(System.currentTimeMillis());
        try {
            Map<Tuple, Double> scores = swingWrapper.calcUserCrossWeight();
            System.out.println("end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void readFile(String inputPath) throws FileNotFoundException {
        File file = new File(inputPath);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }


}

    }
