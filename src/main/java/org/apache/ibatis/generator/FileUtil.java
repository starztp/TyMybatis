package org.apache.ibatis.generator;

import java.io.*;

/**
 * Created by tianyou on 2020/6/9.
 * 先放在这个包里，后面再做规范化
 */
public class FileUtil {
    /**
     * 将一个文件从一个路径copy到另一个路径下，并删除源文件
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copy(File from, File to) throws IOException {
        InputStream in = new FileInputStream(from.getAbsolutePath());
        OutputStream out = new FileOutputStream(to.getAbsolutePath());

        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = in.read(buff)) != -1) {
            out.write(buff, 0, len);
        }
        in.close();
        out.close();
        from.delete();
    }
}
